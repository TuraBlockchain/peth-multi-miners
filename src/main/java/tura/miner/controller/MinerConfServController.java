package tura.miner.controller;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

@Path(value = "/api/v1/miner/configure/server_url")
public class MinerConfServController extends Controller {

	public void index() {
		renderText("");
	}
}
