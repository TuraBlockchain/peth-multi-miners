package hk.zdl.crypto.tura.miner.controller;

import java.nio.file.Paths;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.pearlet.persistence.MyDb;

@Path(value = "/api/v1/miner_path")
public class MinerPathController extends Controller {

	public void add() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		var jobj = new JSONObject(getRawData());
		var id = jobj.get("id").toString();
		var path = jobj.getString("path").trim();
		var result = MyDb.addMinerPath(id, Paths.get(path));
		if (result) {
			renderText("ok");
		} else {
			renderError(409);
		}
	}

	public void del() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		var jobj = new JSONObject(getRawData());
		var id = jobj.get("id").toString();
		var path = jobj.getString("path").trim();
		var result = MyDb.delMinerPath(id, Paths.get(path));
		if (result) {
			renderText("1");
		} else {
			renderText("0");
		}
	}

	public void list() {
		if (!getRequest().getMethod().equals("GET")) {
			renderError(405);
		}
		var id = getPara("id");
		var paths = MyDb.getMinerPaths(id);
		renderText(new Gson().toJson(paths), "application/json");
	}
}
