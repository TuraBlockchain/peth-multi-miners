package hk.zdl.crypto.tura.miner;

import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import tura.miner.util.Util;

public class MinerProcessHelper {

	public static final MinerProcessHelper me = new MinerProcessHelper();

	private MinerMonitor miner_mon;

	private MinerProcessHelper() {

	}

	public Set<Entry<Object, Object>> minerProperties() {
		if (miner_mon == null)
			return Collections.emptySet();
		return miner_mon.entrySet();
	}

	public synchronized final void rebuildMinerProcess() throws Exception {
		Path miner_bin_path = null;
		Map<BigInteger, String> accounts = Collections.emptyMap();
		List<Path> plot_dirs = Collections.emptyList();
		String s = MyDb.get_server_url().get();
		if (miner_mon != null) {
			miner_mon.destroyForcibly();
		}
		if (s == null)
			return;
		if (accounts.isEmpty() || plot_dirs.isEmpty())
			return;
		miner_mon = Util.buildMinerProces(miner_bin_path, accounts, plot_dirs, new URL(s));

	}
}
