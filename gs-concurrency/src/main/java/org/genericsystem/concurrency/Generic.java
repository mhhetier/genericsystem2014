package org.genericsystem.concurrency;

import org.genericsystem.concurrency.vertex.LifeManager;
import org.genericsystem.concurrency.vertex.Root;
import org.genericsystem.concurrency.vertex.Vertex;

public class Generic extends AbstractGeneric<Generic> implements GenericService<Generic> {

	private final boolean throwExistException;

	public Generic(boolean throwExistException) {
		this.throwExistException = throwExistException;
	}

	@Override
	public Generic newT(boolean throwExistException) {
		return new Generic(throwExistException);
	}

	@Override
	public Generic[] newTArray(int dim) {
		return new Generic[dim];
	}

	@Override
	public boolean isThrowExistException() {
		return throwExistException;
	}

	@Override
	public LifeManager getLifeManager() {
		org.genericsystem.kernel.Vertex unwrap = unwrap();
		if (unwrap instanceof Root)
			return ((Root) unwrap).getLifeManager();
		return ((Vertex) unwrap).getLifeManager();
	}
}
