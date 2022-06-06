package tura.miner.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import tura.miner.MinerMonitor;
import tura.miner.util.MySingleton;

@Path(value = "/api/v1")
public class MyController extends Controller {

	public void status() {
		List<MinerMonitor> miner_mons = MySingleton.getInstance().getDriveMonitor().getMinerMonitors();
		List<Object> m1 = miner_mons.stream().map(o -> {
			Map<String, Object> map = new HashMap<>();
			map.put("cid", o.getCid());
			map.put("path", o.getPlotPath().toAbsolutePath().toString());
			o.entrySet().forEach(e->{
				map.put(e.getKey().toString(), e.getValue());
			});
			return map;
		}).collect(Collectors.toUnmodifiableList());
		Map<String, Object> map = new HashMap<>();
		map.put("miners", m1);
		map.put("version", 1);
		map.put("start_time", MySingleton.getInstance().getStartTime());
		renderJson(map);
	}
}
