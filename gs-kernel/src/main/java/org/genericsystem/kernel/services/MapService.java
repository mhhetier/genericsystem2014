package org.genericsystem.kernel.services;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

import org.genericsystem.kernel.VertexService;
import org.genericsystem.kernel.annotations.SystemGeneric;

public interface MapService<T extends VertexService<T>> extends SystemPropertiesService<T>, CompositesInheritanceService<T>, UpdatableService<T> { // extends CompositesInheritanceService<T>, UpdatableService<T> {

	@Override
	default Serializable getSystemPropertyValue(Class<?> propertyClass, int pos) {
		Optional<T> key = getKey(new AxedPropertyClass(propertyClass, pos));
		if (key.isPresent()) {
			Optional<Serializable> result = getValues(key.get()).stream().findFirst();
			if (result.isPresent())
				return result.get();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	default void setSystemPropertyValue(Class<T> propertyClass, int pos, Serializable value) {
		setKey(new AxedPropertyClass(propertyClass, pos)).setInstance(value, (T) this);
	}

	@SuppressWarnings("unchecked")
	default T getMap() {
		return (T) ((VertexService) getRoot()).find(SystemMap.class);
	}

	// @SuppressWarnings("unchecked")
	// default T setMap() {
	// T map = getMap();
	// if (map == null) {
	// map = getRoot().setInstance(SystemMap.class, getRoot()).enablePropertyConstraint();
	// assert map == getMap();
	// }
	// return map;
	// }

	default Stream<T> getKeys() {
		T map = getMap();
		return map == null ? Stream.empty() : getAttributes(map).stream();
	}

	default Optional<T> getKey(AxedPropertyClass property) {
		return getKeys().filter(x -> x.getValue().equals(property)).findFirst();
	}

	@SuppressWarnings("unchecked")
	default T setKey(AxedPropertyClass property) {
		T root = getRoot();
		return root.setInstance(getMap(), (Serializable) property, root);
	}

	@SystemGeneric
	public static class SystemMap {

	}
}