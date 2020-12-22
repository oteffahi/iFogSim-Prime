package org.fog.utils;

public class NetworkUsageMonitor {

	private static double networkUsage = 0.0;
	
	public static void sendingTuple(double tupleNwSize){
		networkUsage += tupleNwSize;
	}
	
	public static double getNetworkUsage(){
		return networkUsage;
	}
}
