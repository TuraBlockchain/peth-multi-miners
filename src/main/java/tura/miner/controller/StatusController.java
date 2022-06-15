package tura.miner.controller;

import java.util.Map;
import java.util.TreeMap;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import tura.miner.util.MySingleton;
import tura.miner.util.Util;

@Path(value = "/api/v1/status")
public class StatusController extends Controller {

	public void index() {
		Map<String, Object> map = new TreeMap<>();
		map.put("version", 1);
		map.put("start_time", MySingleton.getInstance().getStartTime());
		map.put("memory", Util.systemMemory());
		renderJson(map);
	}
}
