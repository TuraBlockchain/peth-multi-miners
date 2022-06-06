package tura.miner.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tura.miner.DriveMonitor;

public class MySingleton {

	private static final MySingleton INSTANCE = new MySingleton();
	public static final ExecutorService es = Executors.newCachedThreadPool(r->{
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		return t;
	});

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
