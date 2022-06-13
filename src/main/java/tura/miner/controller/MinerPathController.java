package tura.miner.controller;

import java.io.File;
import java.util.Set;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.plugin.redis.Redis;

import redis.clients.jedis.Jedis;
import tura.miner.main.TuraConfig;

@Path(value = "/api/v1/miner_path")
public class MinerPathController extends Controller {

	private static final Jedis jedis = Redis.use(TuraConfig.p.get(TuraConfig.str_redis_cache_name)).getJedis();

	private static final String str_miner_path = "miner_path";

	public void add() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		String path = getRawData();
		if (path == null || path.trim().isEmpty()) {
			renderError(400);
		}
		path = path.trim();
		if (!new File(path).exists()) {
			renderError(410);
		}
		long result = jedis.sadd(str_miner_path, path);
		jedis.close();
		if (result == 1) {
			renderText("ok");
		} else {
			renderError(409);
		}
	}

	public void del() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		String path = getRawData();
		if (path == null || path.trim().isEmpty()) {
			renderError(400);
		}
		path = path.trim();
		long result = jedis.srem(str_miner_path, path);
		jedis.close();
		renderText(Long.toString(result));
	}

	public void list() {
		if (!getRequest().getMethod().equals("GET")) {
			renderError(405);
		}
		Set<String> set = jedis.smembers(str_miner_path);
		jedis.close();
		renderText(new Gson().toJson(set), "application/json");
	}
}
