package hk.zdl.crypto.tura.miner.controller;

import java.math.BigInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.pearlet.persistence.MyDb;

@Path(value = "/api/v1/miner/configure/account")
public class AccountController extends Controller {

	public void index() {
		if (!getRequest().getMethod().equals("GET")) {
			renderError(405);
		}
		var jarr = new JSONArray();
		MyDb.getAccounts().stream().map(o -> o.getStr("address")).map(BigInteger::new).forEach(jarr::put);
		renderText(jarr.toString(), "application/json");
	}

	public void add() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		try {
			var jobj = new JSONObject(getRawData());
			var id = jobj.get("id").toString();
			var passphase = jobj.getString("passphrase").trim();
			var result = MyDb.insertAccount(id, passphase);
			renderText(result ? "1" : "0");
		} catch (Exception e) {
			renderError(400);
		}
		renderText("ok");
	}

	public void del() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		try {
			var jobj = new JSONObject(getRawData());
			var id = jobj.get("id").toString();
			var result = MyDb.deleteAccount(id);
			renderText(result ? "1" : "0");
		} catch (JSONException e) {
			renderError(400);
		}
	}
}
