package org.genericsystem.impl;

import java.io.Serializable;
import java.util.Objects;

import org.genericsystem.kernel.Root;
import org.genericsystem.kernel.RootService;
import org.genericsystem.kernel.Statics;
import org.genericsystem.kernel.services.SignatureService;

public interface EngineService<T extends GenericService<T, U>, U extends EngineService<T, U>> extends RootService<T, U>, GenericService<T, U> {

	@Override
	default T find(Class<?> clazz) {
		return wrap(getVertex().find(clazz));
	}

	default Root buildRoot() {
		return buildRoot(Statics.ENGINE_VALUE);
	}

	public Root buildRoot(Serializable value);

	@Override
	default int getLevel() {
		return 0;
	}

	@Override
	default boolean isRoot() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getRoot() {
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getMeta() {
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	default T getAlive() {
		return (T) this;
	}

	@Override
	default boolean equiv(SignatureService<? extends SignatureService<?>> service) {
		if (this == service)
			return true;
		return Objects.equals(getValue(), service.getValue()) && SignatureService.equivComponents(getComponents(), service.getComponents());
	}

}
