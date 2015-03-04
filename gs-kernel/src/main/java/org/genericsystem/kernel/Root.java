package org.genericsystem.kernel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.genericsystem.api.defaults.DefaultLifeManager;
import org.genericsystem.kernel.Config.MetaAttribute;
import org.genericsystem.kernel.Config.MetaRelation;
import org.genericsystem.kernel.Config.SystemMap;

public class Root extends Generic implements DefaultRoot {

	private final TsGenerator generator = new TsGenerator();
	private Context<Generic> context;
	private final SystemCache systemCache;
	private final Archiver archiver;
	private final Provider provider = new Provider();

	private boolean initialized = false;

	public Root(Class<?>... userClasses) {
		this(Statics.ENGINE_VALUE, userClasses);
	}

	public Root(Serializable value, Class<?>... userClasses) {
		this(value, null, userClasses);
	}

	@Override
	public Root getRoot() {
		return this;
	}

	public Root(Serializable value, String persistentDirectoryPath, Class<?>... userClasses) {
		getProvider().init(this, DefaultLifeManager.TS_SYSTEM, null, Collections.emptyList(), value, Collections.emptyList(), DefaultLifeManager.SYSTEM_TS);
		startContext();
		systemCache = new SystemCache(this, getClass());
		systemCache.mount(Arrays.asList(MetaAttribute.class, MetaRelation.class, SystemMap.class), userClasses);
		flushContext();
		archiver = new Archiver(this, persistentDirectoryPath);
		initialized = true;
		shiftContext();
	}

	protected void startContext() {
		context = new Transaction(this, pickNewTs());
	}

	protected void flushContext() {
		// //Autoflush
	}

	protected void shiftContext() {
		context = new Transaction(this, pickNewTs());
	}

	@Override
	public long pickNewTs() {
		return generator.pickNewTs();
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public final Generic getMetaAttribute() {
		return find(MetaAttribute.class);
	}

	@Override
	public final Generic getMetaRelation() {
		return find(MetaRelation.class);
	}

	@Override
	public Context<Generic> getCurrentCache() {
		return context;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Custom extends Generic> Custom find(Class<?> clazz) {
		return (Custom) systemCache.get(clazz);
	}

	@Override
	public Class<?> findAnnotedClass(Generic vertex) {
		return systemCache.getByVertex(vertex);
	}

	@Override
	public void close() {
		archiver.close();
	}

	public static class TsGenerator {
		private final long startTime = System.currentTimeMillis() * Statics.MILLI_TO_NANOSECONDS - System.nanoTime();
		private final AtomicLong lastTime = new AtomicLong(0L);

		public long pickNewTs() {
			long nanoTs;
			long current;
			for (;;) {
				nanoTs = startTime + System.nanoTime();
				current = lastTime.get();
				if (nanoTs - current > 0)
					if (lastTime.compareAndSet(current, nanoTs))
						return nanoTs;
			}
		}
	}

	@Override
	public Context<Generic> buildTransaction() {
		return new Transaction(this, pickNewTs());
	}

	@Override
	public Generic getMap() {
		return find(SystemMap.class);
	}

	@Override
	public Provider getProvider() {
		return provider;
	}

	class Provider {

		private Map<Generic, Vertex> map = new ConcurrentHashMap<Generic, Vertex>();

		public long getTs(Generic generic) {
			return map.get(generic).getTs();
		}

		Vertex getVertex(Generic generic) {
			return map.get(generic);
		}

		public Generic getMeta(Generic generic) {
			return getVertex(generic).getMeta();
		}

		public List<Generic> getSupers(Generic generic) {
			return getVertex(generic).getSupers();
		}

		public Serializable getValue(Generic generic) {
			return getVertex(generic).getValue();
		}

		public List<Generic> getComponents(Generic generic) {
			return getVertex(generic).getComponents();
		}

		public Dependencies<Generic> getDependencies(Generic generic) {
			return getVertex(generic).getDependencies();
		}

		public LifeManager getLifeManager(Generic generic) {
			return getVertex(generic).getLifeManager();
		}

		public Generic init(Generic generic, long ts, Generic meta, List<Generic> supers, Serializable value, List<Generic> components, long[] otherTs) {
			Vertex result = map.putIfAbsent(generic, new Vertex(generic, ts, meta, supers, value, components, otherTs));
			assert result == null;
			return generic.init(Root.this);
		}

	}
}
