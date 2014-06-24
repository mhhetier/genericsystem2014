package org.genericsystem.cache;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.genericsystem.kernel.Statics;
import org.genericsystem.kernel.exceptions.ExistsException;
import org.genericsystem.kernel.exceptions.NotAliveException;
import org.genericsystem.kernel.services.RemovableService.RemoveStrategy;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

@Test
public class GenericTest extends AbstractTest {

	public void testEngine() {
		Engine engine = new Engine();
		assert engine.getAlive() != null;
		assert engine.getComponentsStream().count() == 0;
		assert engine.getInheritings().stream().count() == 0;
		// assert engine.getInstances().stream().count() == 0;
		assert engine.getLevel() == 0;
		assert engine.getMeta().equals(engine);
		assert engine.getRoot().equals(engine);
		assert engine.getSupersStream().count() == 0;
		assert engine.getValue().equals(Statics.ENGINE_VALUE);
		assert engine.isAlive();
		assert engine.isMeta();
		assert engine.isRoot();
	}

	public void testGetInstances() {
		Engine engine = new Engine();
		// assert engine.getInstances().isEmpty();
		Generic vehicleVertex = engine.addInstance("Vehicle");
		Generic powerVehicleVertex = engine.addInstance("Power", vehicleVertex);
		Generic vehicle = engine.getInstances().filter(g -> g.getValue().equals("Vehicle")).stream().findFirst().get();
		// Generic metaAttribut = engine.getInstances().filter(g -> g.getValue().equals("Engine") && g.getComponentsStream().count() == 1).stream().findFirst().get();
		Generic powerVehicle = engine.getInstances().filter(g -> g.getValue().equals("Power")).stream().findFirst().get();
		assert vehicle.getAlive().equiv(vehicleVertex) : engine.getInstances();
		assert powerVehicle.getAlive().equiv(powerVehicleVertex) : engine.getInstances();
	}

	public void testAddInstance() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		assert engine.getInstances().contains(vehicle) : engine.getInstances().stream().collect(Collectors.toList());
		new RollbackCatcher() {

			@Override
			public void intercept() {
				engine.addInstance("Vehicle");
			}
		}.assertIsCausedBy(ExistsException.class);
	}

	public void testSetInstance() {
		Engine engine = new Engine();
		Generic vehicle = engine.setInstance("Vehicle");
		assert engine.getInstances().contains(vehicle);
		assert engine.setInstance("Vehicle").equals(vehicle);
	}

	public void testAddInheriting() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		Generic car = engine.addInstance(vehicle, "Car");
		assert vehicle.getInheritings().contains(car);
	}

	public void testAddAttribute() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		assert vehicle.isAlive();
		Generic car = engine.addInstance(vehicle, "Car");
		Generic vehiclePower = engine.addInstance("VehiclePower", vehicle);
		Generic carPower = engine.addInstance("CarPower", car);
		assert car.getAttributes(engine).containsAll(Arrays.asList(vehiclePower, carPower)) : car.getAttributes(engine);
		// assert car.getAttributes(engine).size() == 2;
	}

	public void testAddAttributeWithOverride() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		Generic car = engine.addInstance(vehicle, "Car");
		Generic vehiclePower = engine.addInstance("VehiclePower", vehicle);
		Generic carPower = engine.addInstance(vehiclePower, "CarPower", car);
		assert car.getAttributes(engine).contains(carPower) : car.getAttributes(engine);
		assert !car.getAttributes(engine).contains(vehiclePower) : car.getAttributes(engine);
		// assert car.getAttributes(engine).size() == 1;
	}

	public void testAddAttributeWithOverride2() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		Generic car = engine.addInstance(vehicle, "Car");
		Generic sportCar = engine.addInstance(car, "SportCar");
		Generic vehiclePower = engine.addInstance("VehiclePower", vehicle);
		Generic carPower = engine.addInstance(vehiclePower, "CarPower", car);
		Generic sportCarPower = engine.addInstance(vehiclePower, "SportCarPower", sportCar);
		assert sportCar.getAttributes(engine).containsAll(Arrays.asList(carPower, sportCarPower)) : car.getAttributes(engine) + " " + sportCarPower.info();
		// assert sportCar.getAttributes(engine).size() == 2;
	}

	public void testAddAttributeWithOverride3() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		Generic car = engine.addInstance(vehicle, "Car");
		Generic sportCar = engine.addInstance(car, "SportCar");
		Generic vehiclePower = engine.addInstance("VehiclePower", vehicle);
		Generic carPower = engine.addInstance(vehiclePower, "CarPower", car);
		Generic sportCarPower = engine.addInstance(carPower, "SportCarPower", sportCar);
		assert sportCar.getAttributes(engine).contains(sportCarPower) : car.getAttributes(engine) + " " + sportCarPower.info();
		assert !sportCar.getAttributes(engine).contains(vehiclePower) : car.getAttributes(engine) + " " + sportCarPower.info();
		assert !sportCar.getAttributes(engine).contains(carPower) : car.getAttributes(engine) + " " + sportCarPower.info();
		// assert sportCar.getAttributes(engine).size() == 1;
	}

	public void testMultiInheritance() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		Generic robot = engine.addInstance("Robot");
		Generic transformer = engine.addInstance(Lists.newArrayList(vehicle, robot), "transformer");
		Generic vehiclePower = engine.addInstance("VehiclePower", vehicle);
		Generic robotPower = engine.addInstance("RobotPower", robot);
		assert transformer.getAttributes(engine).containsAll(Arrays.asList(robotPower, vehiclePower)) : transformer.getAttributes(engine);
		// assert transformer.getAttributes(engine).size() == 2;
	}

	public void testMultiInheritance2() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		Generic robot = engine.addInstance("Robot");
		Generic transformer = engine.addInstance(Lists.newArrayList(vehicle, robot), "transformer");
		Generic vehiclePower = engine.addInstance("VehiclePower", vehicle);
		Generic robotPower = engine.addInstance("RobotPower", robot);
		Generic transformerPower = engine.addInstance(Lists.newArrayList(vehiclePower, robotPower), "TransformerPower", transformer);
		assert transformer.getAttributes(engine).contains(transformerPower) : transformer.getAttributes(engine);
		assert !transformer.getAttributes(engine).contains(robotPower) : transformer.getAttributes(engine);
		assert !transformer.getAttributes(engine).contains(vehiclePower) : transformer.getAttributes(engine);
		// assert transformer.getAttributes(engine).size() == 1;
	}

	public void testMultiInheritance3() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		Generic robot = engine.addInstance("Robot");
		Generic transformer = engine.addInstance(Lists.newArrayList(vehicle, robot), "transformer");
		engine.addInstance("Power", vehicle);
		engine.addInstance("Power", robot);
		Generic transformerPower = engine.addInstance("Power", transformer);
		assert transformer.getAttributes(engine).contains(transformerPower) : transformer.getAttributes(engine);
		// assert transformer.getAttributes(engine).size() == 1;
	}

	// TODO : update this test by fixing UpdatableService for Cache layer
	public void testRemove() {
		Engine engine = new Engine();
		Generic vehicle = engine.addInstance("Vehicle");
		vehicle.remove(RemoveStrategy.NORMAL);
		new RollbackCatcher() {
			@Override
			public void intercept() {
				engine.addInstance(vehicle, "Car");
			}
		}.assertIsCausedBy(NotAliveException.class);
	}
}