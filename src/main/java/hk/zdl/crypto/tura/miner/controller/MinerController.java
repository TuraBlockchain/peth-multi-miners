package hk.zdl.crypto.tura.miner.controller;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.tura.miner.MinerProcessHelper;

@Path(value = "/api/v1/miner")
public class MinerController extends Controller {
	
	public void restart() throws Exception{
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		MinerProcessHelper.me.rebuildMinerProcess();
		renderText("ok");
	}
}
