package hk.zdl.crypto.tura.miner.main;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.template.Engine;

public class TuraConfig extends JFinalConfig {

	private static boolean is_root;
	static {
		try {
			is_root = IOUtils.readLines(new ProcessBuilder("id", "-u").start().getInputStream(), "UTF-8").stream().map(Integer::parseInt).findFirst().get() == 0;
		} catch (IOException e) {
		}
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
	}

	@Override
	public void configInterceptor(Interceptors me) {

	}

	@Override
	public void configHandler(Handlers me) {

	}

	public static final boolean isRunningOnRoot() {
		return is_root;
	}
}
