package org.genericsystem.concurrency;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.genericsystem.concurrency.Archiver.DependenciesOrder;
import org.genericsystem.kernel.Statics;
import org.genericsystem.kernel.annotations.SystemGeneric;
import org.testng.annotations.Test;

@Test
public class PersistenceTest extends AbstractTest {

	private final String directoryPath = System.getenv("HOME") + "/test/snapshot_save";

	public void testDefaultConfiguration() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
	}

	public void testAnnotType() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot, Vehicle.class);
		root.getCurrentCache().flush();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot, Vehicle.class);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
		assert engine.find(Vehicle.class) instanceof Vehicle : engine.find(Vehicle.class).info();
	}

	public void testAnnotType2() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot, Vehicle.class);
		root.getCurrentCache().flush();
		root.close();

		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
		engine.getCurrentCache().flush();
		engine.close();

		Engine engine2 = new Engine(Statics.ENGINE_VALUE, snapshot, Vehicle.class);
		compareGraph(engine, engine2, engine2.getCurrentCache().getTs());

		assert engine2.find(Vehicle.class) instanceof Vehicle : engine2.find(Vehicle.class).info();
	}

	@SystemGeneric
	public static class Vehicle extends Generic {
	}

	public void testType() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		root.addInstance("Vehicle");
		root.getCurrentCache().flush();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
		assert null != engine.getInstance("Vehicle");
	}

	public void testHolder() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		Generic vehicle = root.addInstance("Vehicle");
		Generic vehiclePower = vehicle.setAttribute("power");
		Generic myVehicle = vehicle.addInstance("myVehicle");
		myVehicle.setHolder(vehiclePower, "123");
		root.getCurrentCache().flush();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
	}

	public void testAddAndRemove() throws InterruptedException {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		Generic vehicle = root.addInstance("Vehicle");
		Generic car = root.addInstance(vehicle, "Car");
		Generic truck = root.addInstance(vehicle, "Truck");
		assert vehicle.getLifeManager().getDesignTs() < truck.getLifeManager().getDesignTs();
		car.remove();
		root.getCurrentCache().flush();
		assert vehicle.getLifeManager().getDesignTs() < truck.getLifeManager().getDesignTs();
		assert vehicle.getLifeManager().getBirthTs() == truck.getLifeManager().getBirthTs();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
	}

	public void testLink() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		Generic vehicle = root.addInstance("Vehicle");
		Generic color = root.addInstance("Color");
		Generic vehicleColor = vehicle.setAttribute("VehicleColor", color);
		Generic myVehicle = vehicle.addInstance("myVehicle");
		Generic red = color.addInstance("red");
		myVehicle.setHolder(vehicleColor, "myVehicleRed", red);
		root.getCurrentCache().flush();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
	}

	public void testHeritageMultiple() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		Generic vehicle = root.addInstance("Vehicle");
		Generic robot = root.addInstance("Robot");
		root.addInstance(Arrays.asList(vehicle, robot), "Transformer");
		root.getCurrentCache().flush();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
	}

	public void testHeritageMultipleDiamond() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		Generic nommable = root.addInstance("Nommable");
		Generic vehicle = root.addInstance(nommable, "Vehicle");
		Generic robot = root.addInstance(nommable, "Robot");
		root.addInstance(Arrays.asList(vehicle, robot), "Transformer");
		root.getCurrentCache().flush();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
	}

	public void testTree() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		Generic tree = root.addTree("Tree");
		Generic rootTree = tree.addRoot("Engine");
		Generic child = rootTree.setNode("Child");
		rootTree.setNode("Child2");
		child.setNode("Child3");
		root.getCurrentCache().flush();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
	}

	public void testInheritanceTree() {
		String snapshot = cleanDirectory(directoryPath + new Random().nextInt());
		Engine root = new Engine(Statics.ENGINE_VALUE, snapshot);
		Generic tree = root.addTree("Tree");
		Generic rootTree = tree.addRoot("Engine");
		Generic child = rootTree.setInheritingNode("Child");
		rootTree.setInheritingNode("Child2");
		child.setInheritingNode("Child3");
		root.getCurrentCache().flush();
		root.close();
		Engine engine = new Engine(Statics.ENGINE_VALUE, snapshot);
		compareGraph(root, engine, engine.getCurrentCache().getTs());
	}

	private static String cleanDirectory(String directoryPath) {
		File file = new File(directoryPath);
		if (file.exists())
			for (File f : file.listFiles())
				f.delete();
		return directoryPath;
	}

	private void compareGraph(Generic persistedNode, Generic readNode, long ts) {
		List<Generic> persistVisit = new ArrayList<>(new DependenciesOrder<Generic>(ts).visit(persistedNode));
		List<Generic> readVisit = new ArrayList<>(new DependenciesOrder<Generic>(ts).visit(readNode));
		assert persistVisit.size() == readVisit.size() : persistVisit + " \n " + readVisit;
		for (int i = 0; i < persistVisit.size(); i++) {
			LifeManager persistLifeManager = persistVisit.get(i).getLifeManager();
			LifeManager readLifeManager = readVisit.get(i).getLifeManager();
			assert persistLifeManager.getBirthTs() == readLifeManager.getBirthTs();
			// assert persistLifeManager.getLastReadTs() == readLifeManager.getLastReadTs();
			assert persistLifeManager.getDeathTs() == readLifeManager.getDeathTs();
			assert persistLifeManager.getDesignTs() == readLifeManager.getDesignTs() : persistVisit.get(i).info() + " / " + persistLifeManager.getDesignTs();

		}
	}

	// private void compareGraph(Generic persistedNode, Generic readNode) {
	// DependenciesOrder<Generic> persistVisit = new DependenciesOrder<Generic>().visit(persistedNode);
	// DependenciesOrder<Generic> readVisit = new DependenciesOrder<Generic>().visit(readNode);
	// assert persistVisit.size() == readVisit.size() : persistVisit + " \n " + readVisit;
	// for (Generic persist : persistVisit) {
	// for (Generic read : readVisit)
	// if (persist == read)
	// assert false : persistVisit + " \n " + readVisit;
	// }
	// LOOP: for (Generic persist : persistVisit) {
	// for (Generic read : readVisit)
	// if (persist.genericEquals(read))
	// continue LOOP;
	// assert false : persistVisit + " \n " + readVisit;
	// }
	// }

}
