package tura.miner.controller;

import java.net.URL;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.plugin.redis.Redis;

import redis.clients.jedis.Jedis;
import tura.miner.main.TuraConfig;

@Path(value = "/api/v1/miner/configure/server_url")
public class MinerConfServController extends Controller {
	private static final Jedis jedis = Redis.use(TuraConfig.p.get(TuraConfig.str_redis_cache_name)).getJedis();
	private static final String str_server_url = "mining_server_url";

	public void index() {
		try {
			switch(getRequest().getMethod()) {
			case "GET":
				renderText(jedis.get(str_server_url));
				break;
			case "POST":
				String line = getRawData();
				try {
					new URL(line);
				} catch (Exception e) {
					renderError(400);
				}
				jedis.set(str_server_url, line);
				renderText("ok");
				break;
			default:
				renderError(405);
			}
		} catch (Exception e) {
			throw e;
		}finally {
			jedis.close();
		}
	}
}
