package tura.miner.main;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.template.Engine;

public class TuraConfig extends JFinalConfig {

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

}
