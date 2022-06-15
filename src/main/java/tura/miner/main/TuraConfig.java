package tura.miner.main;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.plugin.redis.serializer.JdkSerializer;
import com.jfinal.template.Engine;

public class TuraConfig extends JFinalConfig {

	public static final String str_redis_cache_name = "redis_cache_name";
	public static final String str_redis_host = "redis_host";
	public static final String str_redis_port = "redis_port";
	public static final String str_redis_pw = "redis_password";
	public static final String str_miner_bin_uri = "rotura-miner-uri";
	public static final String str_plotter_bin_uri = "rotura-plotter-uri";

	public static Prop p;
	static {
		p = PropKit.use("conf.properties");
	}

	@Override
	public void configConstant(Constants me) {
	}

	@Override
	public void configRoute(Routes me) {
		me.scan("tura.miner.controller.");
	}

	@Override
	public void configEngine(Engine me) {

	}

	@Override
	public void configPlugin(Plugins me) {
		RedisPlugin redis = new RedisPlugin(p.get(str_redis_cache_name), p.get(str_redis_host), p.getInt(str_redis_port), p.get(str_redis_pw));
		redis.setSerializer(new JdkSerializer());
		me.add(redis);
	}

	@Override
	public void configInterceptor(Interceptors me) {

	}

	@Override
	public void configHandler(Handlers me) {

	}

}
