package hk.zdl.crypto.tura.miner;

import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import tura.miner.util.Util;

public class MinerProcessManager {
	
	public static final MinerProcessManager me = new MinerProcessManager();

	private List<MinerMonitor> miners = Collections.synchronizedList(new LinkedList<MinerMonitor>());

	public synchronized final void start_miner(BigInteger id) throws Exception {
		for(MinerMonitor m:miners) {
			if(m.getProperty("id").equals(id.toString())) {
				throw new IllegalStateException("There's already a process for "+id);
			}
		}
		String passphrase = MyDb.getAccount(id.toString()).get().getStr("PASSPHRASE");
		Path miner_bin_path = null;
		Map<BigInteger, String> accounts = Collections.singletonMap(id, passphrase);
		List<Path> plot_dirs = MyDb.getMinerPaths(id.toString());
		URL server_url = new URL(MyDb.get_server_url().get());
		MinerMonitor miner_mon = Util.buildMinerProces(miner_bin_path, accounts, plot_dirs, server_url);
		miner_mon.setProperty("id", id.toString());
		miners.add(miner_mon);
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
