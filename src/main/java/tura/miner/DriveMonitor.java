package tura.miner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.jfinal.log.Log;

import tura.miner.dir.DirChangeListener;
import tura.miner.util.Util;

public class DriveMonitor implements DirChangeListener {

	private final String miner_suffix = "miner", plot_suffix = "plots";
	private final List<MinerMonitor> miner_mons = Collections.synchronizedList(new LinkedList<>());
	private static final Log log = Log.getLog(DriveMonitor.class);
	
	public List<MinerMonitor> getMinerMonitors() {
		return Collections.unmodifiableList(miner_mons);
	}

	private Path toPlotPath(Path path) {
		return Path.of(path.toAbsolutePath().toString(), plot_suffix);
	}

	@Override
	public synchronized void onEntryCreate(Path path) {
		log.info("drive insert: " + path);
		try {
			Path miner_dir = Util.checkAndCopyMinerFiles(Path.of(path.toAbsolutePath().toString(), miner_suffix));
			MinerMonitor mon = Util.execDockerMiner(miner_dir, toPlotPath(path));
			String cid = mon.getCid();
			log.info("Docker container with id " + cid + " has started.");
			mon.start();
			miner_mons.add(mon);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void onEntryDelete(Path path) {
		log.info("drive remove: " + path);
		Path p = toPlotPath(path);
		try {
			MinerMonitor mon = miner_mons.stream().filter(o -> o.getPlotPath().equals(p)).findAny().get();
			mon.setRunning(false);
			String cid = mon.getCid();
			miner_mons.remove(mon);
			int i = Util.killDockerMiner(cid);
			if (i == 0) {
				log.info("Docker container with id " + cid + " has stoped.");
			} else {
				log.error("Something went wrong when trying to stop Docker container with id " + cid);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
