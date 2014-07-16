package org.genercisystem.impl;

import java.io.Serializable;
import java.util.Collections;

import org.genericsystem.impl.EngineService;
import org.genericsystem.kernel.Root;
import org.genericsystem.kernel.Statics;
import org.genericsystem.kernel.Vertex;
import org.genericsystem.kernel.services.ApiService;

public class Engine extends Generic implements EngineService<Generic> {

	private final Root root;

	public Engine() {
		this(Statics.ENGINE_VALUE);
	}

	public Engine(Serializable engineValue) {
		super(false);
		root = buildRoot(engineValue);
		init(null, Collections.emptyList(), engineValue, Collections.emptyList());
	}

	@Override
	public Root buildRoot(Serializable value) {
		return new Root(value);
	}

	@Override
	protected Vertex getVertex() {
		return root;
	}

	@Override
	public Generic getMeta() {
		return this;
	}

	@Override
	public Engine getRoot() {
		return this;
	}

	@Override
	public Generic getAlive() {
		return this;
	}

	@Override
	public boolean equiv(ApiService<? extends ApiService<?>> service) {
		return EngineService.super.equiv(service);
	}

	@Override
	public Generic find(Class<?> clazz) {
		return wrap(root.find(clazz));
	}

}
