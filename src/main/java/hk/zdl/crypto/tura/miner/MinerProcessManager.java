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

	private MinerProcessManager() {
	}

	public synchronized final void start_miner(BigInteger id, boolean auto_restart) {
		for (var m : miners) {
			if (m.getProperty("id").equals(id.toString())) {
				throw new IllegalStateException("There's already a process for " + id);
			}
		}
		var passphrase = MyDb.getAccount(id.toString()).get().getStr("PASSPHRASE");
		var plot_dirs = MyDb.getMinerPaths(id.toString());
		if (plot_dirs.isEmpty()) {
			throw new IllegalStateException("No plot path for this wallet id.");
		}
		MyDb.list_server_url().stream().map(o -> o.getStr("URL")).forEach(server_url -> {
			try {
				var miner_mon = Util.buildMinerProces(id, passphrase, plot_dirs, new URL(server_url));
				miner_mon.setProperty("id", id.toString());
				miner_mon.setProperty("plot_dirs", plot_dirs.stream().map(o -> o.toAbsolutePath().toString()).toList());
				miners.add(miner_mon);
				miner_mon.set_auto_restart_process(auto_restart);
				miner_mon.start();
			} catch (Exception e) {
			}
		});
	}

	public synchronized final void stop_miner(BigInteger id) {
		var itr = miners.iterator();
		while (itr.hasNext()) {
			var m = itr.next();
			if (id.toString().equals(m.getProperty("id"))) {
				m.destroyForcibly();
				itr.remove();
				break;
			}
		}
	}

	public final List<MinerMonitor> list_miners() {
		return Collections.unmodifiableList(miners);
	}

	public final void stop_all() {
		var itr = miners.iterator();
		while (itr.hasNext()) {
			var m = itr.next();
			m.destroyForcibly();
			itr.remove();
		}
	}

	public final void start_all() {
		MyDb.getAccounts().stream().map(r -> r.getStr("ADDRESS")).filter(s -> MyDb.getMinerPaths(s).size() > 0).map(BigInteger::new).forEach(i -> MinerProcessManager.me.start_miner(i, true));
	}
}
