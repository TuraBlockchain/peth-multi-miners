package tura.miner.controller;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.plugin.redis.Redis;

import redis.clients.jedis.Jedis;
import tura.miner.main.TuraConfig;

@Path(value = "/api/v1/miner/configure/account")
public class AccountController extends Controller {
	private static final Jedis jedis = Redis.use(TuraConfig.p.get(TuraConfig.str_redis_cache_name)).getJedis();

	public static final String str_accounts = "accounts";

	public void index() {
		if (!getRequest().getMethod().equals("GET")) {
			renderError(405);
		}
		try {
			Set<Integer> set = jedis.smembers(str_accounts).stream().map(s->s.substring(0, s.indexOf(','))).map(Integer::parseInt).collect(Collectors.toUnmodifiableSet());
			renderText(new Gson().toJson(set), "application/json");
		} catch (Exception e) {
			throw e;
		}finally {
			jedis.close();
		}
	}

	public void add() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		try {
			JSONObject jobj = new JSONObject(getRawData());
			int id = jobj.getInt("id");
			String passphase = jobj.getString("passphase").trim();
			long result = jedis.sadd(str_accounts, Integer.toString(id) + "," + passphase);
			renderText(Long.toString(result));
		} catch (JSONException e) {
			renderError(400);
		}finally {
			jedis.close();
		}
		renderText("ok");
	}

	public void del() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		try {
			JSONObject jobj = new JSONObject(getRawData());
			int id = jobj.getInt("id");
			Optional<String> opt = jedis.smembers(str_accounts).stream().filter(s->s.startsWith(Integer.toString(id)+",")).findAny();
			if(opt.isPresent()) {
				jedis.srem(str_accounts, opt.get());
				renderText("1");
			}else {
				renderText("0");
			}
		} catch (JSONException e) {
			renderError(400);
		}finally {
			jedis.close();
		}
	}
}
