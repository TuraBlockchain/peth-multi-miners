package tura.miner.util;

import tura.miner.DriveMonitor;

public class MySingleton {

	private static final MySingleton INSTANCE = new MySingleton();

	private DriveMonitor dm;
	private final long start_time = System.currentTimeMillis();

	private MySingleton() {

	}

	public long getStartTime() {
		return start_time;
	}

	public DriveMonitor getDriveMonitor() {
		return dm;
	}

	public void setDriveMonitor(DriveMonitor dm) {
		this.dm = dm;
	}

	public static MySingleton getInstance() {
		return INSTANCE;
	}

}
