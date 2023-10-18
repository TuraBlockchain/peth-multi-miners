package hk.zdl.crypto.tura.miner.interceptor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.tura.miner.controller.AccountController;
import hk.zdl.crypto.tura.miner.controller.StatusController;

public class AuthInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		if (inv.getController().getClass().equals(StatusController.class)) {
			inv.invoke();
		} else if (inv.getController().getClass().equals(AccountController.class) && MyDb.getAccountCount() == 0) {
			inv.invoke();
		} else {
			var header_auth = inv.getController().getRequest().getHeader("Authorization");
			if (header_auth != null && header_auth.startsWith("Basic ")) {
				var txt = header_auth.substring(6);
				txt = Charset.defaultCharset().decode(ByteBuffer.wrap(Base64.getDecoder().decode(txt))).toString();
				var txt_arr = txt.split("[:]");
				if (txt_arr.length > 1 && MyDb.hasAccount(txt_arr[1])) {
					inv.invoke();
					return;
				}
			}
			inv.getController().getResponse().setHeader("WWW-Authenticate", "Basic");
			inv.getController().renderError(401);
			inv.invoke();
		}
	}

}
