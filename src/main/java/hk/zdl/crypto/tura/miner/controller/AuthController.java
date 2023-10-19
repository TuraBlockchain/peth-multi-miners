package hk.zdl.crypto.tura.miner.controller;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.tura.miner.util.Util;
import hk.zdl.crypto.tura.miner.util.Util.Auth;

@Path(value = "/api/v1/miner/configure/auth")
public class AuthController extends Controller {

	public void method() throws Exception {
		if (getRequest().getMethod().equals("GET")) {
			renderText(Util.getAuthMethod().name());
			return;
		} else if (getRequest().getMethod().equals("POST")) {
			var txt = getRawData();
			Util.setAuthMethod(Auth.valueOf(txt));
			renderText("ok");
			return;
		}
	}

	public void password() throws Exception {
		if (getRequest().getMethod().equals("POST")) {
			var txt = getRawData();
			Util.store_new_pw(txt.toCharArray());
			renderText("ok");
			return;
		} else {
			renderError(405);
			return;
		}
	}
}
