package hk.zdl.crypto.tura.miner.main;

import org.apache.log4j.Logger;

import com.jfinal.server.undertow.UndertowServer;

import hk.zdl.crypto.pearlet.util.Util;
import hk.zdl.crypto.tura.miner.MinerProcessHelper;

public class Main {

	public static void main(String[] args) throws Throwable {
		UndertowServer.start(TuraConfig.class, Util.getProp().getInt("local_port"), false);
		try {
			MinerProcessHelper.me.rebuildMinerProcess();
		} catch (Exception e) {
			Logger.getLogger(Main.class).error(e.getMessage(),e);
		}
	}

}
