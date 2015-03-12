package org.genericsystem.cache;

import org.genericsystem.kernel.Generic;

public interface DefaultEngine extends org.genericsystem.defaults.DefaultRoot<Generic> {

	Cache start(Cache cache);

	void stop(Cache cache);

	@Override
	default Cache getCurrentCache() {
		return (Cache) getRoot().getCurrentCache();
	}

	GarbageCollector getGarbageCollector();

}