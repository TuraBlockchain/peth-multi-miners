package hk.zdl.crypto.tura.miner.controller;

import java.net.URL;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.pearlet.persistence.MyDb;

@Path(value = "/api/v1/miner/configure/server_url")
public class MinerConfServController extends Controller {
	public static final String str_server_url = "mining_server_url";

	public void index() {
		try {
			switch(getRequest().getMethod()) {
			case "GET":
				var o = MyDb.get_server_url();
				if(o.isPresent()) {
					renderText(o.get());
				}else {
					renderText("");
				}
				break;
			case "POST":
				String line = getRawData();
				try {
					new URL(line);
				} catch (Exception e) {
					renderError(400);
				}
				MyDb.update_server_url(line);
				renderText("ok");
				break;
			default:
				renderError(405);
			}
		} catch (Exception e) {
			throw e;
		}
	}
}
