package org.prime.templates;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * Empty topology.
 * 
 * @author TEFFAHI Oussama
 */
public class EmptyTopology {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	
	public static void main(String[] args) {
		try {
			//Begin Initialisation of CloudSim and iFogSim
			Log.disable();
			Calendar calendar = Calendar.getInstance();
			CloudSim.init(1, calendar, false);
			String appId = "app1";
			FogBroker broker = new FogBroker("broker");
			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			Controller controller = null;
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
			//End Initialisation of CloudSim and iFogSim
			
			//Begin creating physical topology
			
			/* Example of creating cloud node
			FogDevice cloud = createFogDevice("cloud", 8000, 8000, 100, 100000, 0, 0, 20, 15);
			cloud.setParentId(-1); //cloud has no parent, negative id does not exist
			fogDevices.add(cloud); //all devices made must be added to the list
			moduleMapping.addModuleToDevice("Module_Cloud", "cloud"); //module becomes VM on device. Module with the same name must be created in the method createApplication
			*/
			
			/* Example of creating a Fog node
			FogDevice node = createFogDevice("N1", 4000, 4000, 100000, 100000, 1, 0, 10, 5);
			noeud.setParentId(cloud.getId());
			noeud.setUplinkLatency(0.1); //propagation time of data sent to parent. All time measurements in iFogSim are IN SECONDS
			fogDevices.add(node); //all devices made must be added to the list
			moduleMapping.addModuleToDevice("Module_Fog", "N1"); //module becomes VM on device
			*/
			
			/* Example of creating a Sensor-only object (for example a camera)
			FogDevice obj = createFogDevice("O1", 2000, 2000, 100000, 100000, 2, 0, 5, 2.5); //creating the object itself
			obj.setParentId(node.getId()); //parent is closest Fog node
			obj.setUplinkLatency(0.1); //propagation time of data sent to parent.
			fogDevices.add(obj); //all devices made must be added to the list
			moduleMapping.addModuleToDevice("Module_Object", "O1"); //module becomes VM on device
			
			//creating a PERIODIC sensor called S1 with an activation period of 100 seconds, sending a tuple named "AnyTupleNameYouWant" to its parent device every 100s.
			Sensor s = new Sensor("S1", "AnyTupleNameYouWant", broker.getId(), appId, new DeterministicDistribution(100));
			s.setGatewayDeviceId(obj.getId()); //setting the object itself as the parent of the sensor
			s.setLatency(0.001); //time delay between creation of the data and its arrival to the object itself (IN SECONDS)
			sensors.add(s); //every sensor must be added to the list of sensors
			*/
			//End of creation of physical topology
			
			//Begin Initializing simulation and start it
			//Simulation parameters (like simulated time) can be found in class org.fog.utils.Config
			controller = new Controller("master-controller", fogDevices, sensors, actuators);
			controller.submitApplication(application, (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
			CloudSim.startSimulation(); //starting simulation. This call ends when the simulation is over.
			//Simulation is over and results are printed by the class org.fog.placement.Controller
			//Additional results can be printed here manually by looping through the fogDevices list and accessing the FogDevice object properties
		} 
		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
		
	/**
	 * Creates a standard Fog Device that can emit, receive and execute tuples. Understanding how the method works is not necessary
	 * @param nodeName a unique name for the device
	 * @param mips the capacity of the node, in Million Instruction Per Second
	 * @param ram the RAM. Useless as iFogSim (currently) does not take this into consideration.
	 * @param upBw bandwidth for sending tuples to parent device, in Kilo/Mega/Giga/Tera bytes/bits PER SECOND.
	 * @param downBw bandwidth for sending tuples to children devices, in Kilo/Mega/Giga/Tera bytes/bits PER SECOND.
	 * @param level the device's level in the topology, cloud having the smallest level number (usually 0 or 1) and then incremented for each new level.
	 * @param ratePerMips used by iFogSim to compute the overall consumption of all resources by the device. Is not useful for any advanced work as you will need to study each resource individually.
	 * @param busyPower energy consumption in Watts when at 100% CPU usage (when at least one tuple is being executed)
	 * @param idlePower energy consumption in Watts when at 0% CPU usage (when no tuple is being executed)
	 * @return FogDevice
	 */
	private static FogDevice createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower)
			);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fogdevice.setLevel(level);
		return fogdevice;
	}
	
	/**
	 * Creating the software aspect of the topology, by definind modules and tuples exchanged between them
	 */
	private static Application createApplication(String appId, int userId){
		Application application = Application.createApplication(appId, userId);
		
		/*
		//Example of module creation
		application.addAppModule("Module_Object", 10); //the 2nd parameter's value is not important as iFogSim (as it currently is) does not care about RAM usage
		application.addAppModule("Module_Fog", 10);
		application.addAppModule("Module_Cloud", 10);
		*/
		
		/*
		//Example of defining tuples. Sensors and actuators are considered as having a module of the same name positioned on them.
		application.addAppEdge("RAW", "Module_Object", 10, 1, "RAW", Tuple.UP, AppEdge.SENSOR); //AppEdge.SENSOR edge type is for when the edge's source is a sensor
		application.addAppEdge("Module_Objet", "Module_Fog", 500, 100, "EXEC", Tuple.UP, AppEdge.MODULE); //AppEdge.MODULE edge type is for when the edge is between two modules
		application.addAppEdge("Module_Fog", "Module_Cloud", 8000, 50, "RESULT", Tuple.UP, AppEdge.MODULE); 
		//AppEdge.ACTUATOR edge type is for when the edge's destination is an actuator
		*/
		
		/*
		//Example of defining dependencies between tuples
		//when a module Module_Object finishes executing a tuple RAW, exactly ONE tuple EXEC is emitted by the node hosting the module
		application.addTupleMapping("Module_Object", "RAW", "EXEC", new FractionalSelectivity(1.0));
		//when a module Module_Fog finishes executing a tuple EXEC, exactly ONE tuple RESULT is emitted by the node hosting the module
		application.addTupleMapping("Module_Fog", "EXEC", "RESULT", new FractionalSelectivity(1.0));
		*/
		
		/*Example of AppLoops
		//AppLoops are a way to get the average time of a certain 'loop' of tuples defined by the succession of their destination modules, from the emittion of the first tuple to the execution of the last one (last tuple's execution on iFogSim-Prime only)
		//They can be quite buggy some times so it is advised to do multiple test runs on small-scale instances of the topology to ensure apploop times are correct
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("RAW");add("Module_Objet");add("Module_Fog");add("Module_Cloud");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}}; //multiple loops can be watched on the same simulation
		
		application.setLoops(loops);
		//AppLoop results are printed by default in the org.fog.placement.Controller class at the end of the simulation
		*/
		return application;
	}
}