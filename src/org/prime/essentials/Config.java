package org.prime.essentials;

public class Config {
	//iFogSim simulation config
	public static int MAX_SIMULATION_TIME = 10000; //defines the simulated time, i.e the duration of time your topology is supposed to have existed
	public static int RESOURCE_MANAGE_INTERVAL = 100; //defines the interval of time between updates of resource consumption for FogDevices
	
	//iFogSim FogDevice config
	public static String FOG_DEVICE_ARCH = "x86";
	public static String FOG_DEVICE_OS = "Linux";
	public static String FOG_DEVICE_VMM = "Xen";
	public static double FOG_DEVICE_TIMEZONE = 10.0;
	public static double FOG_DEVICE_COST = 3.0;
	public static double FOG_DEVICE_COST_PER_MEMORY = 0.05;
	public static double FOG_DEVICE_COST_PER_STORAGE = 0.001;
	public static double FOG_DEVICE_COST_PER_BW = 0.0;
	
	//iFogSim-Prime additional simulation config
	public static boolean LOG_TUPLE_ARRIVALS = false; //log FogDevice tuple arrivals that are not sendToSelf calls
	public static boolean LOG_ALL_TUPLE_ARRIVALS = false; //log all FogDevice tuple arrivals including sendToSelf calls
	public static boolean SHOW_CONTROLLER_SIM_RESULTS = true; //print out simulation results as implemented by default in the Controller class
	public static boolean LAST_APPLOOP_TUPLES_EXEC = true; //if set to TRUE, execution time of last tuple in any AppLoop will be counted in the AppLoop delay. Set to FALSE otherwise
}
