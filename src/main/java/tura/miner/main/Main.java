package tura.miner.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jfinal.log.Log;
import com.jfinal.server.undertow.UndertowServer;

import tura.miner.DriveMonitor;
import tura.miner.dir.DirChangeWatcher;
import tura.miner.util.MyShutdownHook;
import tura.miner.util.MySingleton;

public class Main {

	public static void main(String[] args) throws Throwable {
		Log log = Log.getLog(Main.class);
		log.info("starting...");
		if (args.length < 1) {
			log.fatal("must specify external drive watch dir");
			System.exit(1);
		}
		Path path = Paths.get(args[0]);
		log.info("Path to watch: " + path);
		DriveMonitor dm = new DriveMonitor();
		Runtime.getRuntime().addShutdownHook(new MyShutdownHook(dm, path));
		MySingleton.getInstance().setDriveMonitor(dm);
		Files.list(path).forEach(dm::onEntryCreate);
		DirChangeWatcher watcher = new DirChangeWatcher(path);
		watcher.addDirChangeListener(dm);
		watcher.start();
		log.info("watcher has started...");
		UndertowServer.start(TuraConfig.class, 8080, false);
	}

}
