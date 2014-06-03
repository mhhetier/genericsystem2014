package org.genericsystem.kernel;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.genericsystem.kernel.exceptions.ReferentialIntegrityConstraintViolationException;
import org.genericsystem.kernel.exceptions.RollbackException;
import org.testng.annotations.Test;

@Test
public class VertexRestructurationTest extends AbstractTest {

	public void test001_setValue_Type() {
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex vehicle2 = vehicle.setValue("Vehicle2");
		assert "Vehicle2".equals(vehicle2.getValue());
		assert vehicle2.isAlive();
	}

	public void test003_setValue_InstanceOfType() {
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		String valueCar = "Car";
		Vertex car = vehicle.addInstance(valueCar);
		String newValue = "elciheV";
		Vertex newVehicle = vehicle.setValue(newValue);
		assert newValue.equals(newVehicle.getValue());
		assert valueCar.equals(car.getValue());
		assert engine == newVehicle.getMeta();
		assert engine.computeAllDependencies().contains(newVehicle);
		Vertex newCar = newVehicle.getInstances().iterator().next();
		assert newValue.equals(newCar.getMeta().getValue());
	}

	public void test004_setValue_noCollateralDommage() {
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		vehicle.addInstance("Car");
		String caveValue = "Cave";
		Vertex cave = engine.addInstance(caveValue);
		vehicle.setValue("elciheV");
		assert caveValue.equals(cave.getValue());
		assert engine == cave.getMeta();
		assert cave.getInstances().size() == 0;
		assert cave.isAlive();
	}

	public void test007_setValue_Type() {
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		String valueCar = "Car";
		Vertex car = vehicle.addInstance(valueCar);
		String valueNewBeetle = "NewBeetle";
		Vertex newBeetle = car.addInstance(valueNewBeetle);
		String newValue = "elciheV";

		Vertex newVehicle = vehicle.setValue(newValue);

		LinkedHashSet<Vertex> engineAliveDependencies = newVehicle.computeAllDependencies();
		assert engineAliveDependencies.size() == 3;
		assert !engineAliveDependencies.contains(vehicle);
		assert !engineAliveDependencies.contains(car);
		assert !engineAliveDependencies.contains(newBeetle);

		Vertex vertex1asNewVehicle = findElement(newValue, engineAliveDependencies.stream().collect(Collectors.toList()));
		assert vertex1asNewVehicle != null;
		assert engine.equals(vertex1asNewVehicle.getMeta());

		Vertex vertex2asNewCar = findElement(valueCar, engineAliveDependencies.stream().collect(Collectors.toList()));
		assert vertex2asNewCar != null;
		assert vertex1asNewVehicle.equals(vertex2asNewCar.getMeta());

		Vertex vertex3asNewNewBeetle = findElement(valueNewBeetle, engineAliveDependencies.stream().collect(Collectors.toList()));
		assert vertex3asNewNewBeetle != null;
		assert vertex2asNewCar.equals(vertex3asNewNewBeetle.getMeta());
	}

	public void test008_setValue_Type() {
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		String valueCar = "Car";
		Vertex car = vehicle.addInstance(valueCar);
		String valueNewBeetle = "NewBeetle";
		Vertex newBeetle = car.addInstance(valueNewBeetle);
		String newValue = "raC";

		Vertex newCar = car.setValue(newValue);

		LinkedHashSet<Vertex> engineAliveDependencies = newCar.computeAllDependencies();
		assert engineAliveDependencies.size() == 2;
		assert !engineAliveDependencies.contains(car);
		assert !engineAliveDependencies.contains(newBeetle);

		Vertex vertex1asNewCar = findElement(newValue, engineAliveDependencies.stream().collect(Collectors.toList()));
		assert vertex1asNewCar != null;
		assert vehicle.equals(vertex1asNewCar.getMeta());

		Vertex vertex2asNewNewBeetle = findElement(valueNewBeetle, engineAliveDependencies.stream().collect(Collectors.toList()));
		assert vertex2asNewNewBeetle != null;
		assert vertex1asNewCar.equals(vertex2asNewNewBeetle.getMeta());
	}

	public void test020_setValue_Inheritance() {
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		String valueOptions = "Options";
		Vertex options = engine.addInstance(vehicle, valueOptions);
		String newValue = "elciheV";

		Vertex newVehicle = vehicle.setValue(newValue);

		assert newVehicle.isAlive();
		assert !vehicle.isAlive();
		assert !options.isAlive();

		assert newValue.equals(newVehicle.getValue());
		assert engine.equals(newVehicle.getMeta());
		assert engine.computeAllDependencies().contains(newVehicle);
		assert newVehicle.computeAllDependencies().size() == 2;
		assert newVehicle.computeAllDependencies().contains(newVehicle);
		Vertex newOptions = newVehicle.computeAllDependencies().stream().collect(Collectors.toList()).get(0);
		assert newOptions.isAlive();
		if (newValue.equals(newOptions.getValue()))
			newOptions = newVehicle.computeAllDependencies().stream().collect(Collectors.toList()).get(1);
		assert engine.equals(newOptions.getMeta());
		assert options.getValue().equals(newOptions.getValue());
		List<Vertex> newOptionsSupers = newOptions.getSupersStream().collect(Collectors.toList());
		assert newOptionsSupers.size() == 1;
		Vertex newVehicleFromNewOptions = newOptionsSupers.get(0);
		assert newValue.equals(newVehicleFromNewOptions.getValue());
	}

	public void test040_setValue_Component() {
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		String valuePower = "Power";
		Vertex power = engine.addInstance(valuePower, vehicle);
		String newValue = "elciheV";

		Vertex newVehicle = vehicle.setValue(newValue);

		assert newValue.equals(newVehicle.getValue());
		assert !power.isAlive();
		assert engine.equals(newVehicle.getMeta());
		assert engine.computeAllDependencies().contains(newVehicle);
		Vertex newPower = findElement("Power", engine.computeAllDependencies().stream().collect(Collectors.toList()));
		assert newPower.getComponentsStream().count() == 1;
		Vertex componentOfPower = newPower.getComponents().get(0);
		assert newVehicle.getValue().equals(componentOfPower.getValue());
		assert engine.equals(componentOfPower.getMeta());
	}

	public void test060_setValue_Type_Inheritance_Component() {
		Vertex engine = new Root();
		Vertex machine = engine.addInstance("Machine");
		Vertex vehicle = engine.addInstance(machine, "Vehicle");
		String valuePower = "Power";
		Vertex power = engine.addInstance(valuePower, vehicle);
		Vertex car = vehicle.addInstance("Car");
		String newValue = "enihcaM";

		Vertex newMachine = machine.setValue(newValue);

		assert engine.isAlive();
		assert !machine.isAlive();
		assert !vehicle.isAlive();
		assert !power.isAlive();
		assert !car.isAlive();

		assert engine.equals(engine.getMeta());
		assert engine.equals(machine.getMeta());
		assert engine.equals(vehicle.getMeta());
		assert engine.equals(power.getMeta());
		assert vehicle.equals(car.getMeta());

		assert newValue.equals(newMachine.getValue());
		assert newMachine.getComponents().size() == 0;
		assert newMachine.getSupersStream().count() == 0;
		assert newMachine.computeAllDependencies().size() == 4;
		assert newMachine.getInstances().size() == 0;
		assert newMachine.getInheritings().size() == 1;

		assert engine.getComponents().size() == 0;
		assert engine.getSupersStream().count() == 0;
		assert engine.computeAllDependencies().size() == 5;
		assert engine.getInstances().size() == 3;
		assert engine.getInheritings().size() == 0;

		Vertex newVehicle = findElement("Vehicle", newMachine.computeAllDependencies().stream().collect(Collectors.toList()));
		assert newVehicle != null;
		assert newVehicle.getComponents().size() == 0;
		assert newVehicle.getSupersStream().count() == 1;
		assert newVehicle.computeAllDependencies().size() == 3;
		assert newVehicle.getInstances().size() == 1;
		assert newVehicle.getInheritings().size() == 0;

		Vertex newPower = findElement("Power", newMachine.computeAllDependencies().stream().collect(Collectors.toList()));
		assert newPower != null;
		assert newPower.getComponents().size() == 1;
		assert newPower.getSupersStream().count() == 0;
		assert newPower.computeAllDependencies().size() == 1;
		assert newPower.getInstances().size() == 0;
		assert newPower.getInheritings().size() == 0;

		Vertex newCar = findElement("Car", newMachine.computeAllDependencies().stream().collect(Collectors.toList()));
		assert newCar != null;
		assert newCar.getComponents().size() == 0;
		assert newCar.getSupersStream().count() == 0;
		assert newCar.computeAllDependencies().size() == 1;
		assert newCar.getInstances().size() == 0;
		assert newCar.getInheritings().size() == 0;
	}

	public void test100_remove_instance_NormalStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex myVehicule = engine.addInstance("MyVehicule");

		// when
		myVehicule.remove(RemoveStrategy.NORMAL);

		// then
		assert vehicle.isAlive();
		assert !myVehicule.isAlive();
		assert engine.computeAllDependencies().stream().count() == 2;
		assert engine.computeAllDependencies().contains(engine);
		assert engine.computeAllDependencies().contains(vehicle);
		assert vehicle.computeAllDependencies().stream().count() == 1;
		assert vehicle.computeAllDependencies().contains(vehicle);
	}

	public void test101_remove_instance_NormalStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex myVehicule1 = vehicle.addInstance("MyVehicule1");
		Vertex myVehicule2 = vehicle.addInstance("MyVehicule2");
		Vertex myVehicule3 = vehicle.addInstance("MyVehicule3");

		// when
		myVehicule2.remove(RemoveStrategy.NORMAL);
		myVehicule1.remove(RemoveStrategy.NORMAL);

		// then
		assert vehicle.isAlive();
		assert !myVehicule1.isAlive();
		assert !myVehicule2.isAlive();
		assert myVehicule3.isAlive();
		assert engine.computeAllDependencies().stream().count() == 3;
		assert engine.computeAllDependencies().contains(engine);
		assert engine.computeAllDependencies().contains(vehicle);
		assert vehicle.computeAllDependencies().stream().count() == 2;
		assert vehicle.computeAllDependencies().contains(vehicle);
		assert vehicle.computeAllDependencies().contains(myVehicule3);
		assert myVehicule3.computeAllDependencies().stream().count() == 1;
		assert myVehicule3.computeAllDependencies().contains(myVehicule3);
	}

	public void test102_remove_typeWithInstance() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex myVehicle = vehicle.addInstance("MyVehicule");

		boolean referentialIntegrityConstraintViolationExceptionHasBeenTriggered = false;

		// when
		try {
			vehicle.remove(RemoveStrategy.NORMAL);
		} catch (RollbackException e) {
			if (e.getCause() instanceof ReferentialIntegrityConstraintViolationException)
				referentialIntegrityConstraintViolationExceptionHasBeenTriggered = true;
		} finally {
			// then
			assert referentialIntegrityConstraintViolationExceptionHasBeenTriggered;
			assert vehicle.isAlive();
			assert myVehicle.isAlive();
		}
	}

	public void test103_remove_SubType() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");

		// when
		car.remove(RemoveStrategy.NORMAL);

		// then
		assert vehicle.isAlive();
		assert !car.isAlive();
		assert engine.computeAllDependencies().stream().count() == 2;
		assert engine.computeAllDependencies().contains(engine);
		assert engine.computeAllDependencies().contains(vehicle);
		assert vehicle.computeAllDependencies().stream().count() == 1;
		assert vehicle.computeAllDependencies().contains(vehicle);
	}

	public void test104_remove_attribute() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex power = engine.addInstance("Power", vehicle);

		// when
		vehicle.remove(RemoveStrategy.NORMAL);
		// then
		assert engine.isAlive();
		assert !vehicle.isAlive();
		assert !power.isAlive();
	}

	public void test105_remove_attribute_withInstance_KO() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex power = engine.addInstance("Power", vehicle);
		Vertex car = vehicle.addInstance("Car");

		boolean referentialIntegrityConstraintViolationExceptionHasBeenTriggered = false;

		// when
		try {
			vehicle.remove(RemoveStrategy.NORMAL);
		} catch (RollbackException e) {
			if (e.getCause() instanceof ReferentialIntegrityConstraintViolationException)
				referentialIntegrityConstraintViolationExceptionHasBeenTriggered = true;
		} finally {
			// then
			assert referentialIntegrityConstraintViolationExceptionHasBeenTriggered;
			assert engine.isAlive();
			assert vehicle.isAlive();
			assert power.isAlive();
			assert car.isAlive();
		}
	}

	public void test106_remove_TypeWithSubType_KO() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");

		boolean referentialIntegrityConstraintViolationExceptionHasBeenTriggered = false;

		// when
		try {
			vehicle.remove(RemoveStrategy.NORMAL);
		} catch (RollbackException e) {
			if (e.getCause() instanceof ReferentialIntegrityConstraintViolationException)
				referentialIntegrityConstraintViolationExceptionHasBeenTriggered = true;
		} finally {
			// then
			assert referentialIntegrityConstraintViolationExceptionHasBeenTriggered;
			assert engine.isAlive();
			assert vehicle.isAlive();
			assert car.isAlive();
		}
	}

	public void test107_remove_relation_KO() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");
		Vertex color = engine.addInstance("Color");
		Vertex red = color.addInstance("red");
		Vertex vehicleColor = engine.addInstance("VehicleColor", vehicle, color);
		Vertex carRed = vehicleColor.addInstance("CarRed", car, red);

		boolean referentialIntegrityConstraintViolationExceptionHasBeenTriggered = false;

		try {
			// when
			vehicleColor.remove(RemoveStrategy.NORMAL);
		} catch (RollbackException e) {
			if (e.getCause() instanceof ReferentialIntegrityConstraintViolationException)
				referentialIntegrityConstraintViolationExceptionHasBeenTriggered = true;
		} finally {
			// then
			assert referentialIntegrityConstraintViolationExceptionHasBeenTriggered;
			assert engine.isAlive();
			assert vehicle.isAlive();
			assert car.isAlive();
			assert color.isAlive();
			assert red.isAlive();
			assert vehicleColor.isAlive();
			assert carRed.isAlive();
		}
	}

	public void test108_remove_relationFromTarget() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");
		Vertex color = engine.addInstance("Color");
		Vertex red = color.addInstance("red");
		Vertex vehicleColor = engine.addInstance("VehicleColor", vehicle, color);
		Vertex carRed = vehicleColor.addInstance("CarRed", car, red);

		// when
		red.remove(RemoveStrategy.NORMAL);

		// then
		assert engine.isAlive();
		assert vehicle.isAlive();
		assert car.isAlive();
		assert color.isAlive();
		assert !red.isAlive();
		assert vehicleColor.isAlive();
		assert !carRed.isAlive();
	}

	public void test109_remove_link() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");
		Vertex color = engine.addInstance("Color");
		Vertex red = color.addInstance("red");
		Vertex vehicleColor = engine.addInstance("VehicleColor", vehicle, color);
		Vertex carRed = vehicleColor.addInstance("CarRed", car, red);

		// when
		carRed.remove(RemoveStrategy.NORMAL);

		// then
		assert engine.isAlive();
		assert vehicle.isAlive();
		assert car.isAlive();
		assert color.isAlive();
		assert red.isAlive();
		assert vehicleColor.isAlive();
		assert !carRed.isAlive();
	}

	public void test110_remove_Type_ForceStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");

		// when
		vehicle.remove(RemoveStrategy.FORCE);

		// then
		assert !vehicle.isAlive();
		assert engine.computeAllDependencies().stream().count() == 1;
		assert engine.computeAllDependencies().contains(engine);
	}

	public void test111_remove_typeWithInstance_ForceStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex myVehicle = vehicle.addInstance("MyVehicule");

		// when
		vehicle.remove(RemoveStrategy.FORCE);
		// then
		assert !vehicle.isAlive();
		assert !myVehicle.isAlive();
		assert engine.computeAllDependencies().stream().count() == 1;
		assert engine.computeAllDependencies().contains(engine);
	}

	public void test112_remove_TypeWithSubType_ForceStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");

		// when
		vehicle.remove(RemoveStrategy.FORCE);

		// then
		assert !vehicle.isAlive();
		assert !car.isAlive();
		assert engine.computeAllDependencies().stream().count() == 1;
		assert engine.computeAllDependencies().contains(engine);
	}

	public void test113_remove_attribute_ForceStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex power = engine.addInstance("Power", vehicle);

		// when
		vehicle.remove(RemoveStrategy.FORCE);

		// then
		assert engine.isAlive();
		assert !vehicle.isAlive();
		assert !power.isAlive();
		assert engine.computeAllDependencies().stream().count() == 1;
		assert engine.computeAllDependencies().contains(engine);
	}

	public void test114_remove_relation_ForceStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");
		Vertex color = engine.addInstance("Color");
		Vertex red = color.addInstance("red");
		Vertex vehicleColor = engine.addInstance("VehicleColor", vehicle, color);
		Vertex carRed = vehicleColor.addInstance("CarRed", car, red);

		// when
		vehicleColor.remove(RemoveStrategy.FORCE);

		// then
		assert engine.isAlive();
		assert vehicle.isAlive();
		assert car.isAlive();
		assert color.isAlive();
		assert red.isAlive();
		assert !vehicleColor.isAlive();
		assert !carRed.isAlive();
	}

	public void test115_remove_instanceBaseOfRelation_ForceStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");
		Vertex color = engine.addInstance("Color");
		Vertex red = color.addInstance("red");
		Vertex vehicleColor = engine.addInstance("VehicleColor", vehicle, color);
		Vertex carRed = vehicleColor.addInstance("CarRed", car, red);

		// when
		car.remove(RemoveStrategy.FORCE);

		// then
		assert engine.isAlive();
		assert vehicle.isAlive();
		assert !car.isAlive();
		assert color.isAlive();
		assert red.isAlive();
		assert vehicleColor.isAlive();
		assert !carRed.isAlive();
	}

	public void test120_remove_Type_ConserveStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");

		// when
		vehicle.remove(RemoveStrategy.CONSERVE);

		// then
		assert !vehicle.isAlive();
		assert engine.computeAllDependencies().stream().count() == 1;
		assert engine.computeAllDependencies().contains(engine);
	}

	public void test121_remove_SubType_ConserveStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = vehicle.addInstance("Car");

		// when
		vehicle.remove(RemoveStrategy.CONSERVE);

		// then
		assert !vehicle.isAlive();
		assert !car.isAlive();

		List<Vertex> engineDependencies = engine.computeAllDependencies().stream().collect(Collectors.toList());
		assert engineDependencies.size() == 2;
		Vertex newCar = findElement("Car", engineDependencies);
		assert newCar.isAlive();
		assert "Car".equals(newCar.getValue());
		assert newCar.computeAllDependencies().size() == 1;
		assert newCar.computeAllDependencies().contains(newCar);
	}

	public void test122_remove_with2SubTypes_ConserveStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");
		Vertex automatic = engine.addInstance(vehicle, "Automatic");

		// when
		vehicle.remove(RemoveStrategy.CONSERVE);

		// then
		assert !vehicle.isAlive();
		assert !car.isAlive();
		assert !automatic.isAlive();

		List<Vertex> engineDependencies = engine.computeAllDependencies().stream().collect(Collectors.toList());
		assert engineDependencies.size() == 3;
		assert engine.getAllInstances().count() == 2;

		Vertex newCar = findElement("Car", engineDependencies);
		assert newCar.isAlive();
		assert "Car".equals(newCar.getValue());
		assert newCar.computeAllDependencies().size() == 1;
		assert newCar.getAllInstances().count() == 0;
		assert newCar.computeAllDependencies().contains(newCar);

		Vertex newAutomatic = findElement("Automatic", engineDependencies);
		assert newAutomatic.isAlive();
		assert "Automatic".equals(newAutomatic.getValue());
		assert newAutomatic.computeAllDependencies().size() == 1;
		assert newAutomatic.getAllInstances().count() == 0;
		assert newAutomatic.computeAllDependencies().contains(newAutomatic);
	}

	public void test123_remove_SubSubTypes_ConserveStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = engine.addInstance(vehicle, "Car");
		Vertex automatic = engine.addInstance(car, "Automatic");

		// when
		vehicle.remove(RemoveStrategy.CONSERVE);

		// then
		assert !vehicle.isAlive();
		assert !car.isAlive();
		assert !automatic.isAlive();

		List<Vertex> engineDependencies = engine.computeAllDependencies().stream().collect(Collectors.toList());
		assert engineDependencies.size() == 3;
		assert engine.getAllInstances().count() == 2;

		Vertex newCar = findElement("Car", engineDependencies);
		assert newCar.isAlive();
		assert "Car".equals(newCar.getValue());
		assert newCar.computeAllDependencies().size() == 2;
		assert newCar.getSupersStream().count() == 0;

		Vertex newAutomatic = findElement("Automatic", newCar.computeAllDependencies().stream().collect(Collectors.toList()));
		assert newAutomatic.isAlive();
		assert "Automatic".equals(newAutomatic.getValue());
		assert newAutomatic.computeAllDependencies().size() == 1;
		assert newAutomatic.getSupersStream().count() == 1;
		assert newAutomatic.getSupersStream().collect(Collectors.toList()).contains(newCar);
	}

	public void test124_remove_TypeWithAttribute_ConserveStrategy() {
		// given
		Vertex engine = new Root();
		Vertex vehicle = engine.addInstance("Vehicle");
		Vertex car = vehicle.addInstance("Car");
		Vertex options = engine.addInstance(vehicle, "Options");

		// when
		vehicle.remove(RemoveStrategy.CONSERVE);

		// then
		assert !vehicle.isAlive();
		assert !options.isAlive();
		assert !car.isAlive();

		List<Vertex> engineDependencies = engine.computeAllDependencies().stream().collect(Collectors.toList());
		assert engineDependencies.size() == 3;
		assert engine.getAllInstances().count() == 2;

		Vertex newCar = findElement("Car", engineDependencies);
		Vertex newOptions = findElement("Options", engineDependencies);
		assert newCar != null;
		assert newCar.getInheritings().stream().count() == 1;
		assert newOptions.equals(newCar.getInheritings().stream().collect(Collectors.toList()).get(0));

		assert newOptions != null;
		assert newOptions.getSupersStream().count() == 1;
		assert newCar.equals(newOptions.getSupersStream().collect(Collectors.toList()).get(0));

	}

	/**
	 * @param value
	 *            the value of the vertex expected
	 * @param vertexList
	 *            the list of vertex in which we find the value
	 * @return first vertex with the expected value, null otherwise
	 */
	private Vertex findElement(Serializable value, List<Vertex> vertexList) {
		assert value != null;
		for (Vertex element : vertexList)
			if (value.equals(element.getValue()))
				return element;
		return null;
	}

}
