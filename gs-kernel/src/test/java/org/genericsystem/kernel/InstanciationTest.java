package org.genericsystem.kernel;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.genericsystem.kernel.exceptions.ExistsException;
import org.testng.annotations.Test;

@Test
public class InstanciationTest extends AbstractTest {

	public void testRootInstanciation() {
		Root root = new Root();
		assert root.getMeta().equals(root);
		assert root.getSupersStream().count() == 0;
		assert root.getComponentsStream().count() == 0;
		assert Statics.ENGINE_VALUE.equals(root.getValue());
		assert root.isAlive();
		assert root.isMeta();
	}

	public void testTypeInstanciation() {
		Root root = new Root();
		Vertex car = root.addInstance("Car");

		// log.info(root.info());
		// log.info(car.info());

		assert car.getMeta().equals(root);
		assert car.getSupersStream().count() == 0;
		assert car.getComponentsStream().count() == 0;
		assert "Car".equals(car.getValue());
		assert car.isAlive();
		assert car.isStructural();
		assert car.isInstanceOf(root);
		assert !car.inheritsFrom(root);
	}

	public void testTwoTypeInstanciationDifferentNames() {
		Root root = new Root();
		Vertex car = root.addInstance("Car");
		Vertex robot = root.addInstance("Robot");

		// log.info(root.info());
		// log.info(car.info());
		// log.info(robot.info());

		assert car.getMeta().equals(root);
		assert car.getSupersStream().count() == 0;
		assert car.getComponentsStream().count() == 0;
		assert "Car".equals(car.getValue());
		assert car.isAlive();
		assert car.isStructural();
		assert car.isInstanceOf(root);
		assert !car.inheritsFrom(root);

		assert robot.getMeta().equals(root);
		assert robot.getSupersStream().count() == 0;
		assert robot.getComponentsStream().count() == 0;
		assert "Robot".equals(robot.getValue());
		assert robot.isAlive();
		assert robot.isStructural();
		assert robot.isInstanceOf(root);
		assert !robot.inheritsFrom(root);
	}

	public void testTwoTypeInstanciationSameNamesAddInstance() {
		Root root = new Root();
		Vertex car = root.addInstance("Car");

		// log.info(root.info());
		// log.info(car.info());

		new RollbackCatcher() {

			@Override
			public void intercept() {
				Vertex car2 = root.addInstance("Car");
			}
		}.assertIsCausedBy(ExistsException.class);
	}

	public void testTwoTypeInstanciationSameNamesSetInstance() {
		Root root = new Root();
		Vertex car = root.addInstance("Car");
		Vertex car2 = root.setInstance("Car");

		// log.info(root.info());
		// log.info(car.info());
		// log.info(car2.info());

		assert car == car2;
		assert car.getMeta().equals(root);
		assert car.getSupersStream().count() == 0;
		assert car.getComponentsStream().count() == 0;
		assert "Car".equals(car.getValue());
		assert car.isAlive();
		assert car.isStructural();
		assert car.isInstanceOf(root);
		assert !car.inheritsFrom(root);

	}

	public void testTwoTypeInstanciationWithInheritance() {
		Root root = new Root();
		Vertex vehicle = root.addInstance("Vehicle");
		Vertex car = root.addInstance(Arrays.asList(vehicle), "Car");
		// log.info(root.info());
		// log.info(vehicle.info());
		// log.info(car.info());

		assert vehicle.getMeta().equals(root);
		assert car.getMeta().equals(root);

		assert root.getSupersStream().count() == 0;
		assert vehicle.getSupersStream().count() == 0;
		assert car.getSupersStream().count() == 1;

		assert car.isInstanceOf(root);
		assert !car.inheritsFrom(root);

		assert car.inheritsFrom(vehicle);
		assert !car.isInstanceOf(vehicle);
		assert !vehicle.isInstanceOf(car);

		// isAlive test
		assert root.isAlive();
		assert vehicle.isAlive();
		assert car.isAlive();

	}

	public void testTypeInstanciationWithSelfInheritance() {
		Root root = new Root();
		Vertex vehicle = root.addInstance("Vehicle");
		// log.info(vehicle.info());
		new RollbackCatcher() {

			@Override
			public void intercept() {
				Vertex vehicle2 = root.addInstance(Arrays.asList(vehicle), "Vehicle");

			}
		}.assertIsCausedBy(ExistsException.class);
	}

	public void test3TypeInstanciationWithMultipleInheritence() {
		Root root = new Root();
		Vertex car = root.addInstance("Car");
		Vertex robot = root.addInstance("Robot");
		Vertex transformer = root.addInstance(Arrays.asList(car, robot), "Transformer");

		// log.info(car.info());
		// log.info(robot.info());
		// log.info(transformer.info());

		assert car.getMeta().equals(root);
		assert robot.getMeta().equals(root);
		assert transformer.getMeta().equals(root);

		assert root.getSupersStream().count() == 0;
		assert car.getSupersStream().count() == 0;
		assert robot.getSupersStream().count() == 0;
		assert transformer.getSupersStream().count() == 2;

		assert transformer.getSupersStream().anyMatch(car::equals); // isAlive test
		assert transformer.getSupersStream().anyMatch(robot::equals);
		//
		assert car.getComponentsStream().count() == 0;
		assert robot.getComponentsStream().count() == 0;
		assert transformer.getComponentsStream().count() == 0;
		//
		assert root.isAlive();
		assert car.isAlive();
		assert robot.isAlive();
		assert transformer.isAlive();

	}

	public void test5TypeInstanciationWithMultipleInheritence() {
		Root root = new Root();
		Vertex vehicle = root.addInstance("Vehicle");
		Vertex car = root.addInstance(Arrays.asList(vehicle), "Car");
		Vertex device = root.addInstance("Device");
		Vertex robot = root.addInstance(Arrays.asList(device), "Robot");
		Vertex transformer = root.addInstance(Arrays.asList(car, robot), "Transformer");

		// log.info(vehicle.info());
		// log.info(car.info());
		// log.info(device.info());
		// log.info(robot.info());
		// log.info(transformer.info());

		assert car.getMeta().equals(root);
		assert vehicle.getMeta().equals(root);
		assert device.getMeta().equals(root);
		assert robot.getMeta().equals(root);
		assert transformer.getMeta().equals(root);

		assert root.getSupersStream().count() == 0;
		assert vehicle.getSupersStream().count() == 0;
		assert car.getSupersStream().count() == 1;
		assert device.getSupersStream().count() == 0;
		assert robot.getSupersStream().count() == 1;
		assert transformer.getSupersStream().count() == 2;

		assert transformer.getSupersStream().anyMatch(car::equals);
		assert transformer.getSupersStream().anyMatch(robot::equals);

		car.getSupersStream().anyMatch(vehicle::equals);
		robot.getSupersStream().anyMatch(device::equals);
		Stream<Vertex> allSupers = Statics.concat(transformer.getSupersStream(), superVertex -> Stream.concat(Stream.of(superVertex), superVertex.getSupersStream()));

		final Predicate<Vertex> condition = x -> Statics.concat(transformer.getSupersStream(), superVertex -> Stream.concat(Stream.of(superVertex), superVertex.getSupersStream())).anyMatch(x::equals);

		assert condition.test(vehicle);
		assert condition.test(car);
		assert condition.test(robot);
		assert condition.test(device);

		assert root.isAlive();
		assert vehicle.isAlive();
		assert car.isAlive();
		assert device.isAlive();
		assert robot.isAlive();
		assert transformer.isAlive();
	}

	public void test6TypeInstanciationWithMultipleInheritence() {
		Root root = new Root();
		Vertex vehicle = root.addInstance("Vehicle");
		Vertex car = root.addInstance(Arrays.asList(vehicle), "Car");
		Vertex device = root.addInstance("Device");
		Vertex robot = root.addInstance(Arrays.asList(device), "Robot");
		Vertex transformer = root.addInstance(Arrays.asList(car, robot), "Transformer");
		Vertex transformer2 = root.addInstance(Arrays.asList(transformer), "Transformer2");

		// log.info(vehicle.info());
		// log.info(car.info());
		// log.info(device.info());
		// log.info(robot.info());
		// log.info(transformer.info());

		assert car.getMeta().equals(root);
		assert vehicle.getMeta().equals(root);
		assert device.getMeta().equals(root);
		assert robot.getMeta().equals(root);
		assert transformer.getMeta().equals(root);
		assert transformer2.getMeta().equals(root);

		assert root.getSupersStream().count() == 0;
		assert vehicle.getSupersStream().count() == 0;
		assert car.getSupersStream().count() == 1;
		assert device.getSupersStream().count() == 0;
		assert robot.getSupersStream().count() == 1;
		assert transformer.getSupersStream().count() == 2;

		assert transformer.getSupersStream().anyMatch(car::equals);
		assert transformer.getSupersStream().anyMatch(robot::equals);

		car.getSupersStream().anyMatch(vehicle::equals);
		robot.getSupersStream().anyMatch(device::equals);
		Stream<Vertex> allSupers = Statics.concat(transformer.getSupersStream(), superVertex -> Stream.concat(Stream.of(superVertex), superVertex.getSupersStream()));

		final Predicate<Vertex> condition = x -> Statics.concat(transformer.getSupersStream(), superVertex -> Stream.concat(Stream.of(superVertex), superVertex.getSupersStream())).anyMatch(x::equals);

		assert condition.test(vehicle);
		assert condition.test(car);
		assert condition.test(robot);
		assert condition.test(device);

		assert root.isAlive();
		assert vehicle.isAlive();
		assert car.isAlive();
		assert device.isAlive();
		assert robot.isAlive();
		assert transformer.isAlive();
	}
}
