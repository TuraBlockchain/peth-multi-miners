package hk.zdl.crypto.tura.miner.interceptor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.tura.miner.util.Util;

public class AuthInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		var auth_method = Util.getAuthMethod();
		if (auth_method == Util.Auth.NONE || auth_method == Util.Auth.PASSWORD && !Util.hasPassword() || auth_method == Util.Auth.PASSPHRASE && MyDb.getAccountCount() == 0) {
			inv.invoke();
		} else {
			var header_auth = inv.getController().getRequest().getHeader("Authorization");
			if (header_auth != null && header_auth.startsWith("Basic ")) {
				var txt = header_auth.substring(6);
				txt = Charset.defaultCharset().decode(ByteBuffer.wrap(Base64.getDecoder().decode(txt))).toString();
				var txt_arr = txt.split("[:]");
				if (txt_arr.length > 1) {
					txt = txt_arr[1];
					if (auth_method == Util.Auth.PASSPHRASE) {
						if (MyDb.hasAccount(txt)) {
							inv.invoke();
							return;
						}
					} else if (auth_method == Util.Auth.PASSWORD) {
						if (Util.validete_password(txt.toCharArray())) {
							inv.invoke();
							return;
						}
					}
				}
			}
			inv.getController().getResponse().setHeader("WWW-Authenticate", "Basic");
			inv.getController().renderError(401);
			inv.invoke();
		}
	}

}
