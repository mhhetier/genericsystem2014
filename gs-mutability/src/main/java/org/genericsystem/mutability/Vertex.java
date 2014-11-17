package org.genericsystem.mutability;

import org.genericsystem.kernel.Dependencies;

public class Vertex extends AbstractVertex<Vertex> implements DefaultVertex<Vertex> {
	private final Dependencies<Vertex> instances = buildDependencies();
	private final Dependencies<Vertex> inheritings = buildDependencies();
	private final Dependencies<Vertex> compositesDependencies = buildDependencies();

	@Override
	protected Dependencies<Vertex> getInstancesDependencies() {
		return instances;
	}

	@Override
	protected Dependencies<Vertex> getInheritingsDependencies() {
		return inheritings;
	}

	@Override
	protected Dependencies<Vertex> getCompositesDependencies() {
		return compositesDependencies;
	}

	@Override
	public Vertex newT() {
		return new Vertex().restore(getRoot().pickNewTs(), getRoot().getEngine().getCurrentCache().getTs(), 0L, Long.MAX_VALUE);
	}

	@Override
	public Vertex[] newTArray(int dim) {
		return new Vertex[dim];
	}

	@Override
	public Root getRoot() {
		return (Root) super.getRoot();
	}
}
