package tura.miner.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySingleton {

	private static final MySingleton INSTANCE = new MySingleton();
	public static final ExecutorService es = Executors.newCachedThreadPool(r->{
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		return t;
	});

	private final long start_time = System.currentTimeMillis();

	private MySingleton() {

	}

	public long getStartTime() {
		return start_time;
	}

	public static MySingleton getInstance() {
		return INSTANCE;
	}

}
