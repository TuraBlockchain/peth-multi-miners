package hk.zdl.crypto.tura.miner.controller;

import java.math.BigInteger;
import java.net.URL;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.tura.miner.MinerProcessManager;

@Path(value = "/api/v1/miner/configure/server_url")
public class MinerConfServController extends Controller {
	public static final String str_server_url = "mining_server_url";

	public void index() {
		try {
			switch (getRequest().getMethod()) {
			case "GET":
				var o = MyDb.get_server_url();
				if (o.isPresent()) {
					renderText(o.get());
				} else {
					renderText("");
				}
				break;
			case "POST":
				String line = getRawData();
				try {
					new URL(line);
				} catch (Exception e) {
					renderError(400);
					return;
				}
				MyDb.update_server_url(line);
				renderText("ok");
				new Thread(() -> {
					var id_list = MinerProcessManager.me.list_miners().stream().map(x -> x.getProperty("id")).toList();
					for (var x : id_list) {
						try {
							MinerProcessManager.me.start_miner(new BigInteger(x), true);
						} catch (Exception e) {
						}
					}
				}).start();
				break;
			default:
				renderError(405);
			}
		} catch (Exception e) {
			throw e;
		}
	}
}
