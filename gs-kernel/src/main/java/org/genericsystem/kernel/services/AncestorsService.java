package org.genericsystem.kernel.services;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.genericsystem.kernel.Snapshot;
import org.genericsystem.kernel.services.SystemPropertiesService.WeakPredicate;

public interface AncestorsService<T extends AncestorsService<T>> extends SignatureService<T> {

	default int getLevel() {
		return getMeta().getLevel() + 1;
	}

	default boolean isRoot() {
		return false;
	}

	default T getRoot() {
		return getMeta().getRoot();
	}

	default boolean isMeta() {
		return getLevel() == 0;
	}

	default boolean isStructural() {
		return getLevel() == 1;
	}

	default boolean isFactual() {
		return getLevel() == 2;
	}

	List<T> getSupers();

	Stream<T> getSupersStream();

	default boolean hasSuperSameMeta() {
		return getSupersStream().anyMatch(x -> getMeta().equals(x.getMeta()));
	}

	// default Stream<T> getSuprasStream() {
	// return isRoot() || hasSuperSameMeta() ? getSupersStream() : Stream.concat(Stream.of(getMeta()), getSupersStream());
	// }

	default boolean inheritsFrom(T superVertex) {
		if (this == superVertex || equals(superVertex))
			return true;
		if (getLevel() != superVertex.getLevel())
			return false;
		return getSupersStream().anyMatch(vertex -> vertex.inheritsFrom(superVertex));
	}

	default boolean isInstanceOf(T metaVertex) {
		return getMeta().inheritsFrom(metaVertex);
	}

	default boolean isAttributeOf(T vertex) {
		return isRoot() || getComponentsStream().anyMatch(component -> vertex.inheritsFrom(component) || vertex.isInstanceOf(component));
	}

	default boolean isAlive() {
		return equals(getAlive());
	}

	@Override
	@SuppressWarnings("unchecked")
	default T getAlive() {
		T pluggedMeta = getMeta().getAlive();
		if (pluggedMeta == null)
			return null;
		for (T instance : (Snapshot<T>) (((DependenciesService<?>) pluggedMeta).getInstances()))
			if (equiv(instance))
				return instance;
		return null;
	}

	@SuppressWarnings("unchecked")
	default T getWeakAlive() {
		T pluggedMeta = getMeta().getAlive();
		if (pluggedMeta == null)
			return null;
		for (T instance : (Snapshot<T>) (((DependenciesService<?>) pluggedMeta).getInstances()))
			if (weakEquiv(instance))
				return instance;
		return null;
	}

	WeakPredicate getWeakPredicate();

	default boolean equiv(AncestorsService<? extends AncestorsService<?>> service) {
		if (isRoot()) {
			if (this == service)
				return true;
			return Objects.equals(getValue(), service.getValue()) && AncestorsService.equivComponents(getComponents(), service.getComponents());
		}
		return service == null ? false : equiv(service.getMeta(), service.getValue(), service.getComponents());
	}

	default boolean equiv(AncestorsService<?> meta, Serializable value, List<? extends AncestorsService<?>> components) {
		return getMeta().equiv(meta) && Objects.equals(getValue(), value) && equivComponents(getComponents(), components);
	}

	static boolean equivComponents(List<? extends AncestorsService<?>> components, List<? extends AncestorsService<?>> otherComponents) {
		if (otherComponents.size() != components.size())
			return false;
		Iterator<? extends AncestorsService<?>> otherComponentsIt = otherComponents.iterator();
		return components.stream().allMatch(x -> x.equiv(otherComponentsIt.next()));
	}

	default boolean weakEquiv(AncestorsService<? extends AncestorsService<?>> service) {
		return service == this ? true : weakEquiv(service.getMeta(), service.getValue(), service.getComponents());
	}

	default boolean weakEquiv(AncestorsService<?> meta, Serializable value, List<? extends AncestorsService<?>> components) {
		return getMeta().weakEquiv(meta) && getMeta().getWeakPredicate().test(getValue(), getComponents(), value, components);
	}

}
