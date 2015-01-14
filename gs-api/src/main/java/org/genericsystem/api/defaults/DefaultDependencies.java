package org.genericsystem.api.defaults;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.genericsystem.api.core.IVertex;
import org.genericsystem.api.core.Snapshot;

public interface DefaultDependencies<T extends DefaultVertex<T>> extends IVertex<T> {

	@SuppressWarnings("unchecked")
	@Override
	default boolean isAlive() {
		return getCurrentCache().isAlive((T) this);
	}

	@Override
	default boolean isAncestorOf(T dependency) {
		return equals(dependency) || (!dependency.isMeta() && isAncestorOf(dependency.getMeta())) || dependency.getSupers().stream().anyMatch(this::isAncestorOf) || dependency.getComponents().stream().filter(x -> x != null).anyMatch(this::isAncestorOf);
	}

	@Override
	default DefaultContext<T> getCurrentCache() {
		return (DefaultContext<T>) getRoot().getCurrentCache();
	}

	@SuppressWarnings("unchecked")
	@Override
	default Snapshot<T> getInstances() {
		return getCurrentCache().getInstances((T) this);
	}

	@SuppressWarnings("unchecked")
	@Override
	default Snapshot<T> getInheritings() {
		return getCurrentCache().getInheritings((T) this);
	}

	@SuppressWarnings("unchecked")
	@Override
	default Snapshot<T> getComposites() {
		return getCurrentCache().getComposites((T) this);
	}

	@SuppressWarnings("unchecked")
	@Override
	default Snapshot<T> getAllInheritings() {
		return () -> Stream.concat(Stream.of((T) this), getInheritings().get().flatMap(inheriting -> inheriting.getAllInheritings().get())).distinct();
	}

	@Override
	default Snapshot<T> getAllInstances() {
		return () -> getAllInheritings().get().flatMap(inheriting -> inheriting.getInstances().get());
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getInstance(T superT, Serializable value, T... components) {
		return getInstance(Collections.singletonList(superT), value, components);
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getInstance(Serializable value, T... components) {
		return getInstance(Collections.emptyList(), value, components);
	}

	public static <T extends DefaultVertex<T>> Predicate<T> valueFilter(Serializable value) {
		return attribute -> Objects.equals(attribute.getValue(), value);
	}

	@SuppressWarnings("unchecked")
	default Predicate<T> componentsFilter(T... components) {
		return attribute -> {
			int subIndex = 0;
			loop: for (T component : components) {
				for (; subIndex < attribute.getComponents().size(); subIndex++) {
					T subTarget = attribute.getComponents().get(subIndex);
					if (subTarget.isSpecializationOf(component)) {
						if (isSingularConstraintEnabled(subIndex))
							return true;
						subIndex++;
						continue loop;
					}
				}
				return false;
			}
			return true;
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getAttribute(Serializable value, T... components) {
		return getAttributes().get().filter(valueFilter(value)).filter(componentsFilter(components)).findFirst().orElse(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getHolder(T attribute, Serializable value, T... components) {
		return getHolders(attribute).get().filter(valueFilter(value)).filter(componentsFilter(components)).findFirst().orElse(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getRelation(Serializable value, T... components) {
		return getRelations().get().filter(valueFilter(value)).filter(componentsFilter(components)).findFirst().orElse(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getLink(T relation, Serializable value, T... components) {
		return getLinks(relation).get().filter(valueFilter(value)).filter(componentsFilter(components)).findFirst().orElse(null);
	}
}