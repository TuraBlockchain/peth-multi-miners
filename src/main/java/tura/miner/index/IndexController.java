package tura.miner.index;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

@Path(value = "/", viewPath = "/index")
public class IndexController extends Controller {

	public void index() {
		renderText("ok");
	}
}
