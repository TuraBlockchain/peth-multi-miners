package hk.zdl.crypto.tura.miner.main;

import java.math.BigInteger;
import java.nio.file.Files;

import org.apache.log4j.Logger;

import com.jfinal.server.undertow.UndertowServer;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.Util;
import hk.zdl.crypto.tura.miner.MinerProcessManager;

public class Main {

	public static void main(String[] args) throws Throwable {
		System.setProperty("derby.system.home", Files.createTempDirectory("derby-").toAbsolutePath().toString());
		MyDb.create_missing_tables();
		UndertowServer.start(TuraConfig.class, Util.getProp().getInt("local_port"), false);

		MyDb.getAccounts().stream().map(r -> r.getStr("ADDRESS")).map(BigInteger::new).forEach(i -> {
			try {
				MinerProcessManager.me.start_miner(i);
			} catch (Exception e) {
				Logger.getLogger(Main.class).error(e.getMessage(), e);
			}
		});
	}

}
