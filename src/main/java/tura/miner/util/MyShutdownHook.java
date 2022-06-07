package tura.miner.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.jfinal.log.Log;

import tura.miner.DriveMonitor;

public class MyShutdownHook extends Thread {

	private final DriveMonitor dm;
	private final Path root_path;

	public MyShutdownHook(DriveMonitor dm, Path root_path) {
		this.dm = dm;
		this.root_path = root_path;
	}

	@Override
	public void run() {
		try {
			Files.list(root_path).forEach(dm::onEntryDelete);
		} catch (IOException e) {
			Log.getLog(getClass()).error(e.getMessage(), e);
		}
	}
}
