package hk.zdl.crypto.tura.miner.controller;

import org.json.JSONObject;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.render.TextRender;

import hk.zdl.crypto.tura.miner.MinerProcessManager;

@Path(value = "/api/v1/miner")
public class MinerController extends Controller {

	public void index() {
		if (!getRequest().getMethod().equals("GET")) {
			renderError(405);
			return;
		}
		renderJson(MinerProcessManager.me.list_miners());
	}

	public void start() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
			return;
		}

		try {
			var jobj = new JSONObject(getRawData());
			var id = jobj.getBigInteger("id");
			MinerProcessManager.me.start_miner(id);
		} catch (Exception e) {
			renderError(400, new TextRender(e.getMessage()));
			return;
		}
		renderText("ok");
	}

	public void stop() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
			return;
		}

		try {
			var jobj = new JSONObject(getRawData());
			var id = jobj.getBigInteger("id");
			MinerProcessManager.me.stop_miner(id);
		} catch (Exception e) {
			renderError(400);
			return;
		}
		renderText("ok");
	}

}
