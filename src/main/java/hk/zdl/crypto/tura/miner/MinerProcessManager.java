package hk.zdl.crypto.tura.miner;

import java.math.BigInteger;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.tura.miner.util.Util;

public class MinerProcessManager {
	
	public static final MinerProcessManager me = new MinerProcessManager();

	private List<MinerMonitor> miners = Collections.synchronizedList(new LinkedList<MinerMonitor>());

	public synchronized final void start_miner(BigInteger id) throws Exception {
		for(var m:miners) {
			if(m.getProperty("id").equals(id.toString())) {
				throw new IllegalStateException("There's already a process for "+id);
			}
		}
		var passphrase = MyDb.getAccount(id.toString()).get().getStr("PASSPHRASE");
		var plot_dirs = MyDb.getMinerPaths(id.toString());
		if(plot_dirs.isEmpty()) {
			throw new IllegalStateException("No plot path for this wallet id.");
		}
		var server_url = new URL(MyDb.get_server_url().get());
		var miner_mon = Util.buildMinerProces(id, passphrase, plot_dirs, server_url);
		miner_mon.setProperty("id", id.toString());
		miner_mon.setProperty("plot_dirs",plot_dirs.stream().map(o->o.toAbsolutePath().toString()).toList());
		miners.add(miner_mon);
		miner_mon.start();
	}
	
	public synchronized final void stop_miner(BigInteger id) throws Exception {
		var itr = miners.iterator();
		while(itr.hasNext()) {
			var m = itr.next();
			if(m.getProperty("id").equals(id.toString())) {
				m.destroyForcibly();
				itr.remove();
				break;
			}
		}
	}
	
	public final List<MinerMonitor> list_miners(){
		return Collections.unmodifiableList(miners);
	}
}
