package org.genericsystem.concurrency;

public class GenericBuilder extends AbstractBuilder<Generic> {

	public GenericBuilder(Cache<Generic, ?> context) {
		super(context);
	}

	@Override
	protected Generic newT() {
		return new Generic();
	}

	@Override
	protected Generic[] newTArray(int dim) {
		return new Generic[dim];
	}

}