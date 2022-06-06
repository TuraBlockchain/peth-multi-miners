package test;

import java.io.File;
import java.nio.file.Path;

import com.jfinal.log.Log;

import tura.miner.DriveMonitor;
import tura.miner.dir.DirChangeWatcher;

public class Main {

	public static void main(String[] args) throws Throwable {
		Log log = Log.getLog(Main.class);
		log.info("starting...");
		if(args.length<1) {
			log.fatal("must specify external drive watch dir");
			System.exit(1);
		}
		Path path = new File(args[0]).toPath();
		log.info("Path to watch: "+path);
		DirChangeWatcher watcher = new DirChangeWatcher(path);
		watcher.addDirChangeListener(new DriveMonitor());
		watcher.start();
		log.info("watcher has started...");
	}

}
