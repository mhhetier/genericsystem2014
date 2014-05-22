package org.genericsystem.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.genericsystem.kernel.Dependencies;
import org.genericsystem.kernel.Dependencies.CompositesDependencies;
import org.genericsystem.kernel.Snapshot;
import org.genericsystem.kernel.Vertex;
import org.genericsystem.kernel.services.AncestorsService;
import org.genericsystem.kernel.services.BindingService;
import org.genericsystem.kernel.services.CompositesInheritanceService;
import org.genericsystem.kernel.services.DependenciesService;
import org.genericsystem.kernel.services.DisplayService;
import org.genericsystem.kernel.services.FactoryService;
import org.genericsystem.kernel.services.InheritanceService;

public interface GenericService<T extends GenericService<T>> extends AncestorsService<T>, DependenciesService<T>, InheritanceService<T>, BindingService<T>, CompositesInheritanceService<T>, FactoryService<T>, DisplayService<T> {

	default T wrap(Vertex vertex) {
		return vertex.isRoot() ? getRoot() : build().initFromSupers(wrap(vertex.getAlive().getMeta()), vertex.getAlive().getSupersStream().map(this::wrap).collect(Collectors.toList()), vertex.getValue(),
				vertex.getAlive().getComponentsStream().map(this::wrap).collect(Collectors.toList()));
	}

	default Vertex unwrap() {
		Vertex alive = getVertex();
		if (alive != null)
			return alive;
		// TODO I don't understand why we build here a new Vertex in unwrap method ...
		// It's not responsibility of unwrap method to build a Vertex...
		alive = getMeta().getVertex();
		return alive.build().initFromOverrides(alive, getSupersStream().map(GenericService::getVertex).collect(Collectors.toList()), getValue(), getComponentsStream().map(GenericService::getVertex).collect(Collectors.toList()));
	}

	@Override
	default boolean isAlive() {
		return equiv(getAlive());
	}

	@Override
	default Dependencies<T> getInstances() {
		return getVertex().getInstances().project(this::wrap, GenericService::unwrap);
	}

	@Override
	default Dependencies<T> getInheritings() {
		return getVertex().getInheritings().project(this::wrap, GenericService::unwrap);
	}

	@Override
	default CompositesDependencies<T> getMetaComposites() {
		return getVertex().getMetaComposites().projectComposites(this::wrap, GenericService::unwrap);
	}

	@Override
	default CompositesDependencies<T> getSuperComposites() {
		return getVertex().getSuperComposites().projectComposites(this::wrap, GenericService::unwrap);
	}

	@Override
	default T getInstance(Serializable value, @SuppressWarnings("unchecked") T... components) {
		Vertex vertex = getVertex();
		if (vertex == null)
			return null;

		// TODO change GenericService::getVertex with :unwrap
		vertex = vertex.getInstance(value, Arrays.stream(components).map(GenericService::getVertex).collect(Collectors.toList()).toArray(new Vertex[components.length]));
		if (vertex == null)
			return null;
		return wrap(vertex);
	}

	@Override
	default Snapshot<T> getInheritings(T origin, int level) {
		return getVertex().getInheritings(origin.getVertex(), level).project(this::wrap);
	}

	@Override
	default Snapshot<T> getMetaComposites(T meta) {
		return getVertex().getMetaComposites(meta.getVertex()).project(this::wrap);
	}

	@Override
	default Snapshot<T> getSuperComposites(T superVertex) {
		return getVertex().getSuperComposites(superVertex.getVertex()).project(this::wrap);
	}
}
