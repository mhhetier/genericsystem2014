package org.genericsystem.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.genericsystem.kernel.exceptions.NotAliveException;
import org.genericsystem.kernel.services.BindingService;
import org.genericsystem.kernel.services.DisplayService;
import org.genericsystem.kernel.services.ExceptionAdviserService;

public abstract class Signature<T extends Signature<T>> implements DisplayService<T>, ExceptionAdviserService<T>, BindingService<T> {
	protected T meta;
	protected List<T> components;
	protected Serializable value;

	// TODO : change scope to final
	protected int level = 0;

	@SuppressWarnings("unchecked")
	protected T init(int level, T meta, Serializable value, List<T> components) {
		if (meta != null) {
			meta.checkIsAlive();
			this.meta = meta;
		} else
			this.meta = (T) this;
		this.value = value;
		this.components = new ArrayList<>(components);
		for (int i = 0; i < components.size(); i++) {
			T component = components.get(i);
			if (component != null) {
				component.checkIsAlive();
				this.components.set(i, component);
			}
			// FIXME : why doing next iteration?
			else
				this.components.set(i, (T) this);
		}
		this.level = level;
		// FIXME : what happen if meta == null => components should be equals to this: hmmmm...
		checkDependsMetaComponents();
		return (T) this;
	}

	public void checkIsAlive() {
		if (!isAlive())
			rollbackAndThrowException(new NotAliveException(info()));
	}

	private void checkDependsMetaComponents() {
		if (!(componentsDepends(getComponents(), getMeta().getComponents())))
			rollbackAndThrowException(new IllegalStateException("Inconsistant components : " + getComponentsStream().collect(Collectors.toList())));
	}

	@Override
	public T getMeta() {
		return meta;
	}

	@Override
	public List<T> getComponents() {
		return components;
	}

	@Override
	public Serializable getValue() {
		return value;
	}

	@Override
	public Stream<T> getComponentsStream() {
		return components.stream();
	}

	@Override
	public String toString() {
		return Objects.toString(getValue());
	}

	@Override
	public int getLevel() {
		return level;
	}
}
