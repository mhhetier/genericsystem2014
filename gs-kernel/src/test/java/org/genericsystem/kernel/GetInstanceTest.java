package org.genericsystem.kernel;

import java.util.Arrays;
import java.util.Collections;

import org.genericsystem.api.core.exceptions.AmbiguousSelectionException;
import org.testng.annotations.Test;

@Test
public class GetInstanceTest extends AbstractTest {

	public void test001_getInstance_filterValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic car = root.addInstance(vehicle, "Car");
		Generic myBmw = car.addInstance("myBmw");

		assert vehicle.getInstance("myBmw") == null;
		assert car.getInstance("myBmw") == myBmw;
	}

	public void test002_getInstance_filterValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic myBmw = vehicle.addInstance("myBmw");
		Generic car = root.addInstance(vehicle, "Car");
		Generic myBmwCar = car.addInstance("myBmw");

		assert vehicle.getInstance("myBmw") == myBmw;
		assert car.getInstance("myBmw") == myBmwCar;
	}

	public void test003_getInstance_filterValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic vehiclePower = vehicle.addAttribute("power");
		Generic car = root.addInstance(vehicle, "Car");

		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		Generic myBmw115 = vehiclePower.addInstance(115, myBmw);
		vehiclePower.addInstance(116, myBmw);
		vehiclePower.addInstance(116, myAudi);

		assert vehiclePower.getInstance(115) == myBmw115;
	}

	public void test004_getInstance_filterValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.addRelation("vehicleColor", color);
		Generic car = root.addInstance(vehicle, "Car");

		Generic red = color.addInstance("red");
		Generic blue = color.addInstance("blue");
		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		Generic myBmwRed = vehicleColor.addInstance("myBmwRed", myBmw, red);
		vehicleColor.addInstance("myAudiRed", myAudi, red);
		vehicleColor.addInstance("myBmwBlue", myBmw, blue);

		assert vehicleColor.getInstance("myBmwRed") == myBmwRed;
	}

	public void test001_getInstance_filterValueComponents() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.addRelation("vehicleColor", color);
		Generic car = root.addInstance(vehicle, "Car");

		Generic red = color.addInstance("red");
		Generic blue = color.addInstance("blue");
		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		Generic myBmwRed = vehicleColor.addInstance("", myBmw, red);
		Generic myAudiRed = vehicleColor.addInstance("", myAudi, red);
		Generic myBmwBlue = vehicleColor.addInstance("", myBmw, blue);

		assert vehicleColor.getInstance("", myBmw, red) == myBmwRed;
		assert vehicleColor.getInstance("", myAudi) == myAudiRed;
		assert vehicleColor.getInstance("", blue) == myBmwBlue;
	}

	public void test001_getInstance_filterComponents() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.addRelation("vehicleColor", color);
		Generic car = root.addInstance(vehicle, "Car");

		Generic red = color.addInstance("red");
		Generic blue = color.addInstance("blue");
		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		Generic myBmwRed = vehicleColor.addInstance("", myBmw, red);
		Generic myAudiRed = vehicleColor.addInstance("", myAudi, red);
		Generic myBmwBlue = vehicleColor.addInstance("", myBmw, blue);

		assert vehicleColor.getInstance(myBmw, red) == myBmwRed;
		assert vehicleColor.getInstance(myAudi) == myAudiRed;
		assert vehicleColor.getInstance(blue) == myBmwBlue;
	}

	public void test001_getInstance_filterOverridesValue() {
		Root root = new Root();
		Generic car = root.addInstance("Car");
		Generic vehicle = root.addInstance("Vehicle");
		Generic carVehicle = root.addInstance(vehicle, "Car");

		assert root.getInstance(Collections.emptyList(), "Car") == car;
		assert root.getInstance(vehicle, "Car") == carVehicle;
		catchAndCheckCause(() -> root.getInstance("Car"), AmbiguousSelectionException.class);
	}

	public void test002_getInstance_filterOverridesValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic vehiclePower = vehicle.addAttribute("power");
		Generic car = root.addInstance("Car");
		Generic carVehicle = root.addInstance(vehicle, "Car");

		Generic carPower = car.addAttribute("power");
		Generic carVehiclePower = carVehicle.addAttribute("power");

		assert !carPower.inheritsFrom(vehiclePower);
		assert carVehiclePower.inheritsFrom(vehiclePower);

		catchAndCheckCause(() -> root.getRoot().getMetaAttribute().getInstance("power"), AmbiguousSelectionException.class);
		assert root.getRoot().getMetaAttribute().getInstances("power").size() == 3;
		assert root.getRoot().getMetaAttribute().getInstances("power").containsAll(Arrays.asList(vehiclePower, carPower, carVehiclePower));
		catchAndCheckCause(() -> root.getRoot().getMetaAttribute().getInstance(Collections.emptyList(), "power"), AmbiguousSelectionException.class);
		assert root.getRoot().getMetaAttribute().getInstances(Collections.emptyList(), "power").size() == 2;
		assert root.getRoot().getMetaAttribute().getInstances(Collections.emptyList(), "power").containsAll(Arrays.asList(vehiclePower, carPower));
		assert root.getRoot().getMetaAttribute().getInstance(vehiclePower, "power") == carVehiclePower;
	}

	public void test001_getInstance_filterOverridesValueComponents() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic vehiclePower = vehicle.addAttribute("power");
		Generic car = root.addInstance(vehicle, "Car");
		Generic trunck = root.addInstance(vehicle, "Trunck");
		Generic bike = root.addInstance(vehicle, "Bike");
		Generic carPower = car.addAttribute("carPower");
		Generic bikePower = bike.addAttribute(vehiclePower, "power");
		Generic trunckPower = trunck.addAttribute("power");

		assert !carPower.inheritsFrom(vehiclePower);
		assert trunckPower.inheritsFrom(vehiclePower);

		assert root.getRoot().getMetaAttribute().getInstance("power", trunck) == trunckPower;
		assert root.getRoot().getMetaAttribute().getInstance(Collections.emptyList(), "power", trunck) == trunckPower;
		assert root.getRoot().getMetaAttribute().getInstance(vehiclePower, "power", bike) == bikePower;
	}

	public void test002_getInstance_filterOverridesValueComponents() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic vehiclePower = vehicle.addAttribute("power");
		Generic car = root.addInstance("Car");
		Generic carVehicle = root.addInstance(vehicle, "Car");

		Generic carPower = car.addAttribute("power");
		Generic carVehiclePower = carVehicle.addAttribute("power");

		assert !carPower.inheritsFrom(vehiclePower);
		assert carVehiclePower.inheritsFrom(vehiclePower);

		assert root.getRoot().getMetaAttribute().getInstance("power", car) == carPower;
		assert root.getRoot().getMetaAttribute().getInstance("power", carVehicle) == carVehiclePower;

		assert root.getRoot().getMetaAttribute().getInstance(Collections.emptyList(), "power", car) == carPower;
		assert root.getRoot().getMetaAttribute().getInstance(vehiclePower, "power", carVehicle) == carVehiclePower;
	}

	public void test001_getInstances_filterValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic vehiclePower = vehicle.addAttribute("power");
		Generic car = root.addInstance(vehicle, "Car");

		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		vehiclePower.addInstance(115, myBmw);
		vehiclePower.addInstance(116, myBmw);
		vehiclePower.addInstance(116, myAudi);

		assert vehiclePower.getInstances(116).size() == 2;
	}

	public void test002_getInstances_filterValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.addRelation("vehicleColor", color);
		Generic car = root.addInstance(vehicle, "Car");

		Generic red = color.addInstance("red");
		Generic blue = color.addInstance("blue");
		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		vehicleColor.addInstance("", myBmw, red);
		vehicleColor.addInstance("", myAudi, red);
		vehicleColor.addInstance("", myBmw, blue);

		assert vehicleColor.getInstances("").size() == 3;
	}

	public void test001_getInstances_filterValueComponents() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.addRelation("vehicleColor", color);
		Generic car = root.addInstance(vehicle, "Car");

		Generic red = color.addInstance("red");
		Generic blue = color.addInstance("blue");
		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		vehicleColor.addInstance("", myBmw, red);
		vehicleColor.addInstance("", myAudi, red);
		vehicleColor.addInstance("", myBmw, blue);

		assert vehicleColor.getInstances("", myBmw).size() == 2;
		assert vehicleColor.getInstances("", myBmw, red).size() == 2;
	}

	public void test001_getInstances_filterComponents() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.addRelation("vehicleColor", color);
		Generic car = root.addInstance(vehicle, "Car");

		Generic red = color.addInstance("red");
		Generic blue = color.addInstance("blue");
		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		vehicleColor.addInstance("", myBmw, red);
		vehicleColor.addInstance("", myAudi, red);
		vehicleColor.addInstance("", myBmw, blue);

		assert vehicleColor.getInstances(myBmw).size() == 2;
		assert vehicleColor.getInstances(blue).size() == 1;
	}

	public void test001_getAllInstances_filterValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic myBmw = vehicle.addInstance("myBmw");
		vehicle.addInstance("myAudi");
		Generic car = root.addInstance(vehicle, "Car");
		Generic myBmwCar = car.addInstance("myBmw");
		car.addInstance("myAudi");

		assert vehicle.getAllInstances("myBmw").containsAll(Arrays.asList(myBmw, myBmwCar)) : vehicle.getAllInstances("myBmw").info();
		assert car.getAllInstances("myBmw").first() == myBmwCar;
	}

	public void test001_getAllInstances_filterComponentsValue() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.addRelation("vehicleColor", color);
		Generic car = root.addInstance(vehicle, "Car");

		Generic red = color.addInstance("red");
		Generic blue = color.addInstance("blue");
		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		Generic myBmwRed = vehicleColor.addInstance("", myBmw, red);
		vehicleColor.addInstance("", myAudi, red);
		Generic myBmwBlue = vehicleColor.addInstance("", myBmw, blue);

		assert vehicleColor.getAllInstances("", myBmw).size() == 2;
		assert vehicleColor.getAllInstances("", myBmw).containsAll(Arrays.asList(myBmwRed, myBmwBlue));
	}

	public void test001_getAllInstances_filterComponents() {
		Root root = new Root();
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.addRelation("vehicleColor", color);
		Generic car = root.addInstance(vehicle, "Car");

		Generic red = color.addInstance("red");
		Generic blue = color.addInstance("blue");
		Generic myBmw = car.addInstance("myBmw");
		Generic myAudi = car.addInstance("myAudi");
		Generic myBmwRed = vehicleColor.addInstance("", myBmw, red);
		vehicleColor.addInstance("", myAudi, red);
		Generic myBmwBlue = vehicleColor.addInstance("myBmwBlue", myBmw, blue);

		assert vehicleColor.getAllInstances(myBmw).size() == 2;
		assert vehicleColor.getAllInstances(myBmw).containsAll(Arrays.asList(myBmwRed, myBmwBlue));
	}
}
