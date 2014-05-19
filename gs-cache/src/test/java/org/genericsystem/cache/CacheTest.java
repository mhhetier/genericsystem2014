package org.genericsystem.cache;

import java.util.Arrays;

import org.genericsystem.kernel.Vertex;
import org.testng.annotations.Test;

@Test
public class CacheTest extends AbstractTest {

	public void testTypeInheritings() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		assert vehicle.isAlive();
		Generic car = engine.addInstance(Arrays.asList(vehicle), "Car");
		assert vehicle.getInheritings().stream().anyMatch(g -> g.equals(car));
	}

	public void testCacheInheritings() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		assert vehicle.getVertex() == null;
		Vertex car = engine.getVertex().addInstance(vehicle.getVertex(), "Car");
		assert vehicle.getInheritings().filter(car::equiv).size() == 1;
	}
}
