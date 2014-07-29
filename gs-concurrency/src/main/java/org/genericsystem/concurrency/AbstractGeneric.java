package org.genericsystem.concurrency;

import org.genericsystem.cache.EngineService;
import org.genericsystem.cache.GenericService;

public abstract class AbstractGeneric<T extends AbstractGeneric<T, U, V, W>, U extends EngineService<T, U, V, W>, V extends AbstractVertex<V, W>, W extends RootService<V, W>> extends org.genericsystem.cache.AbstractGeneric<T, U, V, W> implements
		GenericService<T, U, V, W> {

	@Override
	protected T wrap(V vertex) {
		return super.wrap(vertex);
	}
}
