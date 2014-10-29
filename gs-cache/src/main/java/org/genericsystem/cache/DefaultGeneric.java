package org.genericsystem.cache;

import org.genericsystem.kernel.AbstractVertex;

public interface DefaultGeneric<T extends AbstractGeneric<T, V>, V extends AbstractVertex<V>> extends DefaultVertex<T> {

	@Override
	abstract DefaultEngine<T, V> getRoot();

	default Cache<T, V> getCurrentCache() {
		return getRoot().getCurrentCache();
	}
}