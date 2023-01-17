package hk.zdl.crypto.tura.miner.controller;

import java.math.BigInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.render.TextRender;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import signumj.crypto.SignumCrypto;

@Path(value = "/api/v1/miner/configure/account")
public class AccountController extends Controller {

	public void index() {
		if (!getRequest().getMethod().equals("GET")) {
			renderError(405);
			return;
		}
		var jarr = new JSONArray();
		MyDb.getAccounts().stream().map(o -> o.getStr("ADDRESS")).map(BigInteger::new).forEach(jarr::put);
		renderText(jarr.toString(), "application/json");
	}

	public void add() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
			return;
		}
		try {
			var jobj = new JSONObject(getRawData());
			var id = jobj.getBigInteger("id");
			var passphase = jobj.getString("passphrase").trim();
			if (!SignumCrypto.getInstance().getAddressFromPassphrase(passphase).getID().equals(id.toString())) {
				renderError(409,new TextRender("Passphrase does not match with id"));
				return;
			}
			var result = MyDb.insertAccount(id.toString(), passphase);
			renderText(result ? "1" : "0");
		} catch (JSONException e) {
			renderError(400);
			return;
		}
	}

	public void del() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
			return;
		}
		try {
			var jobj = new JSONObject(getRawData());
			var id = jobj.getBigInteger("id");
			var result = MyDb.deleteAccount(id.toString());
			renderText(result ? "1" : "0");
		} catch (JSONException e) {
			renderError(400);
		}
	}
}
