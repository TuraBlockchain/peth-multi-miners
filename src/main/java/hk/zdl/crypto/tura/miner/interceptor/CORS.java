package hk.zdl.crypto.tura.miner.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

public class CORS implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		inv.getController().getResponse().setHeader("Access-Control-Allow-Origin", "*");
		inv.invoke();
	}

}
