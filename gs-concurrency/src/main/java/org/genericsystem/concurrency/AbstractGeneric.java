package org.genericsystem.concurrency;

import java.io.Serializable;
import java.util.List;

import org.genericsystem.kernel.Dependencies;

public abstract class AbstractGeneric<T extends AbstractGeneric<T>> extends org.genericsystem.cache.AbstractGeneric<T> implements DefaultGeneric<T>, Comparable<T> {

	private LifeManager lifeManager;

	@Override
	public DefaultEngine<T> getRoot() {
		return (DefaultEngine<T>) super.getRoot();
	}

	@Override
	protected T init(T meta, List<T> supers, Serializable value, List<T> components) {
		return super.init(meta, supers, value, components).restore(getRoot().pickNewTs(), Long.MAX_VALUE, 0L, Long.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	protected T restore(Long designTs, long birthTs, long lastReadTs, long deathTs) {
		lifeManager = new LifeManager(designTs, birthTs, lastReadTs, deathTs);
		return (T) this;
	}

	public LifeManager getLifeManager() {
		return lifeManager;
	}

	boolean isAlive(long ts) {
		return lifeManager.isAlive(ts);
	}

	// TODO remove this
	@Override
	protected T getDirectInstance(Serializable value, List<T> components) {
		return super.getDirectInstance(value, components);
	}

	@Override
	public int compareTo(T vertex) {
		long birthTs = lifeManager.getBirthTs();
		long compareBirthTs = vertex.getLifeManager().getBirthTs();
		return birthTs == compareBirthTs ? Long.compare(lifeManager.getDesignTs(), vertex.getLifeManager().getDesignTs()) : Long.compare(birthTs, compareBirthTs);
	}

	@Override
	protected abstract Dependencies<T> getInstancesDependencies();

	@Override
	protected abstract Dependencies<T> getInheritingsDependencies();

	@Override
	protected abstract Dependencies<T> getCompositesDependencies();
}
