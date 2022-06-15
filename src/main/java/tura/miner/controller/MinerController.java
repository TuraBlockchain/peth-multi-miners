package tura.miner.controller;

import java.util.Map;
import java.util.TreeMap;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import tura.miner.MinerProcessHelper;

@Path(value = "/api/v1/miner")
public class MinerController extends Controller {

	public void status() {
		Map<String, Object> map = new TreeMap<>();
		MinerProcessHelper.me.minerProperties().stream().forEach(e->map.put(e.getKey().toString(),e.getValue()));
		renderJson(map);
	}
	
	public void restart() throws Exception{
		MinerProcessHelper.me.rebuildMinerProcess();
	}
}
