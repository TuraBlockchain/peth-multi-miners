package tura.miner.controller;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import tura.miner.MinerMonitor;
import tura.miner.util.MySingleton;
import tura.miner.util.Util;

@Path(value = "/api/v1")
public class MyController extends Controller {

	public void status() {
		List<MinerMonitor> miner_mons = MySingleton.getInstance().getDriveMonitor().getMinerMonitors();
		List<Object> m1 = miner_mons.stream().map(o -> {
			Map<String, Object> map = new TreeMap<>();
			String _path = o.getPlotPath().toAbsolutePath().toString();

			map.put("total_space", new File(_path).getTotalSpace());
			map.put("usable_space", new File(_path).getUsableSpace());
			map.put("cid", o.getCid());
			map.put("path", _path);
			o.entrySet().forEach(e->{
				map.put(e.getKey().toString(), e.getValue());
			});
			o.getConf().entrySet().forEach(e->{
				map.put(e.getKey().toString(), e.getValue());
			});
			map.put("ping", ">100");
			try {
				Future<String> future = MySingleton.es.submit(Util.pingServer(map.get("url").toString()));
				String ping_ms = future.get(100, TimeUnit.MILLISECONDS);
				map.put("ping", ping_ms);
			} catch (Exception e1) {
			}
			return map;
		}).collect(Collectors.toUnmodifiableList());
		Map<String, Object> map = new TreeMap<>();
		map.put("miners", m1);
		map.put("version", 1);
		map.put("start_time", MySingleton.getInstance().getStartTime());
		map.put("memory", Util.systemMemory());
		renderJson(map);
	}
}
