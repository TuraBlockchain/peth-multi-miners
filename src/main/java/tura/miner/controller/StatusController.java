package tura.miner.controller;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import tura.miner.MinerProcessHelper;
import tura.miner.main.TuraConfig;
import tura.miner.util.MySingleton;
import tura.miner.util.Util;

@Path(value = "/api/v1/status")
public class StatusController extends Controller {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void index() {
		Map disk = Util.diskUsage().get("/");
		if (TuraConfig.isRunningOnRoot()) {
			try {
				disk.put("temp_cel", Util.disk_temputure_cel(disk.get("device").toString()));
			} catch (Exception e) {
			}
		}
		Map<String, Object> map = new TreeMap<>();
		map.put("version", 1);
		map.put("start_time", MySingleton.getInstance().getStartTime());
		map.put("memory", Util.systemMemory());
		map.put("disk", disk);
		map.put("miner", MinerProcessHelper.me.minerProperties().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue())));
		renderJson(map);
	}
}
