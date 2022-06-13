package tura.miner.controller;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

@Path(value = "/api/v1/miner/configure/account")
public class AccountController extends Controller {

	public void index() {
		renderText("");
	}

	public void add() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		renderText("");
	}

	public void del() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		renderText("");
	}
}
