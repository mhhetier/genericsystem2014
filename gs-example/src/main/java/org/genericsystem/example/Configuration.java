package org.genericsystem.example;

import org.genericsystem.mutability.Engine;
import org.genericsystem.mutability.Generic;

public class Configuration {
	public void mountDataBase() {
		// Create an engine named myDataBase and which is persistent
		Engine engine = new Engine("myDataBase", System.getenv("HOME") + "/my_directory_path");

		// Create a vehicle with a power
		Generic vehicle = engine.addInstance("Vehicle");
		Generic power = vehicle.addAttribute("Power");

		// Instantiate a vehicle with a power of 100
		Generic myVehicle = vehicle.addInstance("myVehicle");
		myVehicle.addHolder(power, 100);
	}
}
