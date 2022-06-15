package tura.miner;

import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jfinal.plugin.redis.Redis;

import redis.clients.jedis.Jedis;
import tura.miner.controller.AccountController;
import tura.miner.controller.MinerConfServController;
import tura.miner.controller.MinerPathController;
import tura.miner.main.TuraConfig;
import tura.miner.util.Util;

import java.util.Set;
import java.util.stream.Collectors;

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
		Jedis jedis = Redis.use(TuraConfig.p.get(TuraConfig.str_redis_cache_name)).getJedis();
		try {
			Path miner_bin_path = Paths.get(TuraConfig.p.get(TuraConfig.str_miner_bin_uri));
			Map<BigInteger, String> accounts = jedis.smembers(AccountController.str_accounts).stream()
					.collect(Collectors.toMap(s -> new BigInteger(s.substring(0, s.indexOf(","))), s -> s.substring(s.indexOf(',') + 1)));
			List<Path> plot_dirs = jedis.smembers(MinerPathController.str_miner_path).stream().map(Paths::get).toList();
			String s = jedis.get(MinerConfServController.str_server_url);
			if (miner_mon != null) {
				miner_mon.destroyForcibly();
			}
			if (s == null)
				return;
			if (accounts.isEmpty() || plot_dirs.isEmpty())
				return;
			miner_mon = Util.buildMinerProces(miner_bin_path, accounts, plot_dirs, new URL(s));
		} finally {
			jedis.close();
		}

	}
}
