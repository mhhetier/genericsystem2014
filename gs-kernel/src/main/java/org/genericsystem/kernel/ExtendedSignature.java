package org.genericsystem.kernel;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.genericsystem.kernel.services.MapService.SystemMap;

public abstract class ExtendedSignature<T extends ExtendedSignature<T>> extends Signature<T> {

	protected List<T> supers;

	@Override
	@SuppressWarnings("unchecked")
	public T init(T meta, List<T> supers, Serializable value, List<T> components) {
		super.init(meta, value, components);
		this.supers = supers;
		checkDependsMetaComponents();
		checkSupers(supers);
		checkDependsSuperComponents(supers);
		return (T) this;
	}

	private void checkDependsMetaComponents() {
		if (getValue().toString().equals(SystemMap.class.toString()) || getValue().toString().contains("PropertyConstraint"))
			return;
		assert getMeta().getComponents() != null;
		if (!(getMeta().componentsDepends(getComponents(), getMeta().getComponents())))
			rollbackAndThrowException(new IllegalStateException("Inconsistant components : " + getComponents() + " " + getMeta().getComponents()));
	}

	private void checkSupers(List<T> supers) {
		supers.forEach(Signature::checkIsAlive);
		if (!supers.stream().allMatch(superVertex -> superVertex.getLevel() == getLevel()))
			rollbackAndThrowException(new IllegalStateException("Inconsistant supers : " + getSupers()));
		if (!supers.stream().allMatch(superVertex -> getMeta().inheritsFrom(superVertex.getMeta())))
			rollbackAndThrowException(new IllegalStateException("Inconsistant supers : " + getSupers()));
		if (!supers.stream().noneMatch(this::equals))
			rollbackAndThrowException(new IllegalStateException("Supers loop detected : " + info()));
	}

	private void checkDependsSuperComponents(List<T> supers) {
		getSupersStream().forEach(superVertex -> {
			if (!superVertex.isSuperOf(getMeta(), supers, getValue(), getComponents()))
				rollbackAndThrowException(new IllegalStateException("Inconsistant components : " + getComponentsStream().collect(Collectors.toList())));
		});
	}

	@Override
	public List<T> getSupers() {
		return supers;
	}

}
