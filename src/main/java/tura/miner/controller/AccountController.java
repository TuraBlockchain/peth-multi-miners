package tura.miner.controller;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
			Set<BigInteger> set = jedis.smembers(str_accounts).stream().map(s->s.substring(0, s.indexOf(','))).map(BigInteger::new).collect(Collectors.toUnmodifiableSet());
			JSONArray jarr = new JSONArray();
			set.stream().forEach(jarr::put);
			renderText(jarr.toString(), "application/json");
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
			BigInteger id = jobj.getBigInteger("id");
			String passphase = jobj.getString("passphase").trim();
			long result = jedis.sadd(str_accounts, id.toString() + "," + passphase);
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
			BigInteger id = jobj.getBigInteger("id");
			Optional<String> opt = jedis.smembers(str_accounts).stream().filter(s->s.startsWith(id.toString()+",")).findAny();
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
