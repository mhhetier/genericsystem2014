package org.genericsystem.mutability;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.genericsystem.api.core.Snapshot;
import org.genericsystem.api.defaults.DefaultContext;
import org.genericsystem.api.exception.AliveConstraintViolationException;
import org.genericsystem.api.exception.RollbackException;
import org.genericsystem.cache.Cache.ContextEventListener;
import org.genericsystem.kernel.annotations.InstanceClass;

public class Cache implements DefaultContext<Generic>, ContextEventListener<org.genericsystem.cache.Generic> {
	private final Engine engine;
	private final org.genericsystem.cache.Cache<org.genericsystem.cache.Generic> cache;
	private final Map<Generic, org.genericsystem.cache.Generic> mutabilityMap = new IdentityHashMap<>();
	private final Map<org.genericsystem.cache.Generic, Set<Generic>> reverseMultiMap = new IdentityHashMap<>();

	private final Deque<Map<Generic, org.genericsystem.cache.Generic>> revertMutations = new ArrayDeque<>();

	public Cache(Engine engine, org.genericsystem.cache.Engine cacheEngine) {
		this.engine = engine;
		put(engine, cacheEngine);
		this.cache = cacheEngine.newCache(this);
		revertMutations.push(new IdentityHashMap<>());
	}

	@Override
	public Engine getRoot() {
		return engine;
	}

	public Cache start() {
		cache.start();
		return engine.start(this);
	}

	public void stop() {
		cache.stop();
		engine.stop(this);
	}

	protected org.genericsystem.cache.Generic unwrap(Generic mutable) {
		if (mutable == null)
			return null;

		org.genericsystem.cache.Generic result = mutabilityMap.get(mutable);
		if (result == null)
			cache.discardWithException(new AliveConstraintViolationException("Your mutable is not still available"));
		return result;
	}

	protected Generic wrap(Class<?> clazz, org.genericsystem.cache.Generic generic) {
		if (generic == null)
			return null;
		Set<Generic> resultSet = reverseMultiMap.get(generic);
		if (resultSet != null)
			return resultSet.iterator().next();
		Generic result;
		InstanceClass instanceClassAnnotation = null;
		Class<?> findAnnotedClass = generic.getRoot().findAnnotedClass(generic.getMeta());
		if (findAnnotedClass != null)
			instanceClassAnnotation = findAnnotedClass.getAnnotation(InstanceClass.class);
		if (clazz != null) {
			if (instanceClassAnnotation != null && !instanceClassAnnotation.value().isAssignableFrom(clazz))
				cache.discardWithException(new InstantiationException(clazz + " must extends " + instanceClassAnnotation.value()));
			result = (Generic) newInstance(clazz);
		} else
			result = (Generic) newInstance(instanceClassAnnotation != null ? instanceClassAnnotation.value() : Object.class);
		put(result, generic);
		return result;
	}

	protected Generic wrap(org.genericsystem.cache.Generic generic) {
		return wrap(null, generic);
	}

	private void put(Generic mutable, org.genericsystem.cache.Generic generic) {
		mutabilityMap.put(mutable, generic);
		Set<Generic> set = Collections.newSetFromMap(new IdentityHashMap<Generic, Boolean>());
		set.add(mutable);
		reverseMultiMap.put(generic, set);
	}

	@Override
	public void triggersMutationEvent(org.genericsystem.cache.Generic oldDependency, org.genericsystem.cache.Generic newDependency) {
		Set<Generic> resultSet = reverseMultiMap.get(oldDependency);
		if (resultSet != null) {
			for (Generic mutable : resultSet) {
				if (!revertMutations.peek().containsKey(mutable))
					revertMutations.peek().put(mutable, oldDependency);
				mutabilityMap.put(mutable, newDependency);
			}
			reverseMultiMap.remove(oldDependency);
			reverseMultiMap.put(newDependency, resultSet);
		}
	}

	@Override
	public void triggersRefreshEvent() {
		Iterator<Entry<org.genericsystem.cache.Generic, Set<Generic>>> iterator = reverseMultiMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<org.genericsystem.cache.Generic, Set<Generic>> entry = iterator.next();
			if (!cache.isAlive(entry.getKey())) {
				for (Generic mutable : entry.getValue())
					mutabilityMap.remove(mutable);
				iterator.remove();
			}
		}
	}

	@Override
	public void triggersClearEvent() {
		for (Entry<Generic, org.genericsystem.cache.Generic> entry : revertMutations.peek().entrySet()) {
			org.genericsystem.cache.Generic newDependency = mutabilityMap.get(entry.getKey());
			mutabilityMap.put(entry.getKey(), entry.getValue());
			if (newDependency != null) {
				Set<Generic> set = reverseMultiMap.get(newDependency);
				set.remove(entry.getKey());
				if (set.isEmpty())
					reverseMultiMap.remove(newDependency);
				set = reverseMultiMap.get(entry.getValue());
				if (set == null)
					reverseMultiMap.put(entry.getValue(), set = Collections.newSetFromMap(new IdentityHashMap<Generic, Boolean>()));
				set.add(entry.getKey());
			}
		}
		revertMutations.pop();
		revertMutations.push(new IdentityHashMap<>());

	}

	protected List<Generic> wrap(List<org.genericsystem.cache.Generic> listT) {
		return listT.stream().map(this::wrap).collect(Collectors.toList());
	}

	protected List<org.genericsystem.cache.Generic> unwrap(List<Generic> listM) {
		return listM.stream().map(this::unwrap).collect(Collectors.toList());
	}

	protected Generic[] wrap(org.genericsystem.cache.Generic... array) {
		return Arrays.asList(array).stream().map(this::wrap).collect(Collectors.toList()).toArray(new Generic[array.length]);
	}

	protected org.genericsystem.cache.Generic[] unwrap(Generic... listM) {
		return engine.getConcurrencyEngine().coerceToTArray(Arrays.asList(listM).stream().map(this::unwrap).collect(Collectors.toList()).toArray());
	}

	@Override
	public void triggersFlushEvent() {
		revertMutations.push(new IdentityHashMap<>());
	}

	@Override
	public boolean isAlive(Generic mutable) {
		org.genericsystem.cache.Generic generic = mutabilityMap.get(mutable);
		return generic != null && cache.isAlive(generic);
	}

	public void pickNewTs() {
		cache.pickNewTs();// triggers refresh automatically
	}

	public void flush() {
		cache.tryFlush(); // triggers flush automatically
	}

	public void flushLater() {
		cache.flush();
	}

	public long getTs() {
		return cache.getTs();
	}

	public void clear() {
		cache.clear();// triggers clear and refresh automatically
	}

	private final static ProxyFactory PROXY_FACTORY = new ProxyFactory();
	private final static MethodFilter METHOD_FILTER = method -> method.getName().equals("getEngine");

	@SuppressWarnings("unchecked")
	<T> T newInstance(Class<?> clazz) {
		PROXY_FACTORY.setSuperclass(clazz);
		if (!Generic.class.isAssignableFrom(clazz))
			PROXY_FACTORY.setInterfaces(new Class[] { Generic.class });
		T instance = null;
		try {
			instance = (T) PROXY_FACTORY.createClass(METHOD_FILTER).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			cache.discardWithException(e);
		}
		((ProxyObject) instance).setHandler(engine);
		return instance;
	}

	public void mount() {
		cache.mount();
		revertMutations.push(new IdentityHashMap<>());
	}

	public void unmount() {
		cache.unmount();// triggersClearEvent
		revertMutations.pop();
	}

	public int getCacheLevel() {
		return cache.getCacheLevel();
	}

	@Override
	public Generic getInstance(Generic meta, List<Generic> overrides, Serializable value, Generic... components) {
		return wrap(unwrap(meta).getInstance(unwrap(overrides), value, unwrap(components)));
	}

	@Override
	public Snapshot<Generic> getInheritings(Generic generic) {
		return () -> unwrap(generic).getInheritings().get().map(this::wrap);
	}

	@Override
	public Snapshot<Generic> getInstances(Generic generic) {
		return () -> unwrap(generic).getInstances().get().map(this::wrap);
	}

	@Override
	public Snapshot<Generic> getComposites(Generic generic) {
		return () -> unwrap(generic).getComposites().get().map(this::wrap);
	}

	@Override
	public void discardWithException(Throwable exception) throws RollbackException {
		assert false;
		cache.discardWithException(exception);
	}

	@Override
	public Generic[] newTArray(int i) {
		return wrap(cache.newTArray(i));
	}

	@Override
	public Generic addInstance(Generic meta, List<Generic> overrides, Serializable value, List<Generic> components) {
		return wrap(cache.addInstance(unwrap(meta), unwrap(overrides), value, unwrap(components)));
	}

	@Override
	public Generic update(Generic update, List<Generic> overrides, Serializable newValue, List<Generic> newComponents) {
		return wrap(cache.update(unwrap(update), unwrap(overrides), newValue, unwrap(newComponents)));
	}

	@Override
	public Generic setInstance(Generic meta, List<Generic> overrides, Serializable value, List<Generic> components) {
		return wrap(cache.setInstance(unwrap(meta), unwrap(overrides), value, unwrap(components)));
	}

	@Override
	public void forceRemove(Generic generic) {
		cache.forceRemove(unwrap(generic));
	}

	@Override
	public void remove(Generic generic) {
		cache.remove(unwrap(generic));
	}

	@Override
	public void conserveRemove(Generic generic) {
		cache.conserveRemove(unwrap(generic));
	}

}
