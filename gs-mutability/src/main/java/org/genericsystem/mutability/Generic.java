package org.genericsystem.mutability;

import java.io.Serializable;
import java.util.List;

import org.genericsystem.defaults.DefaultVertex;
import org.genericsystem.kernel.LifeManager;

public interface Generic extends DefaultVertex<Generic> {

	@Override
	default Engine getRoot() {
		throw new IllegalStateException();
	}

	@Override
	default Cache getCurrentCache() {
		return getRoot().getCurrentCache();
	}

	@Override
	default boolean isSystem() {
		return getCurrentCache().unwrap(this).isSystem();
	}

	default LifeManager getLifeManager() {
		return getCurrentCache().unwrap(this).getLifeManager();
	}

	// @Override
	// default int compareTo(Generic vertex) {
	// long birthTs = getLifeManager().getBirthTs();
	// long compareBirthTs = vertex.getLifeManager().getBirthTs();
	// return birthTs == compareBirthTs ? Long.compare(getTs(), vertex.getTs()) : Long.compare(birthTs, compareBirthTs);
	// }

	default long getTs() {
		return getCurrentCache().unwrap(this).getTs();
	}

	@Override
	default Generic getMeta() {
		return getCurrentCache().wrap(getCurrentCache().unwrap(this).getMeta());
	}

	@Override
	default List<Generic> getSupers() {
		return getCurrentCache().wrap(getCurrentCache().unwrap(this).getSupers());
	}

	@Override
	default Serializable getValue() {
		return getCurrentCache().unwrap(this).getValue();
	}

	@Override
	default List<Generic> getComponents() {
		return getCurrentCache().wrap(getCurrentCache().unwrap(this).getComponents());
	}

	@Override
	default String info() {
		return getCurrentCache().unwrap(this).info();
	}

}
