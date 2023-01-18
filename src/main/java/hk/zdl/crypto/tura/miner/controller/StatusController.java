package hk.zdl.crypto.tura.miner.controller;

import java.util.Map;
import java.util.TreeMap;

import com.formdev.flatlaf.util.SystemInfo;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.tura.miner.main.TuraConfig;
import hk.zdl.crypto.tura.miner.util.Util;

@Path(value = "/api/v1/status")
public class StatusController extends Controller {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void index() {
		var map = new TreeMap<>();
		map.put("version", 1);
		if (SystemInfo.isLinux || SystemInfo.isMacOS_10_11_ElCapitan_orLater) {
			Map disk = Util.diskUsage().get("/");
			map.put("disk", Util.diskUsage().get("/"));
			if (SystemInfo.isLinux) {
				map.put("memory", Util.systemMemory());
				if (TuraConfig.isRunningOnRoot()) {
					try {
						disk.put("temp_cel", Util.disk_temputure_cel(disk.get("device").toString().replaceAll("\\d", "")));
					} catch (Exception e) {
					}
				}
				try {
					map.put("CPU Temp", Util.isa_temputure_cel());
				} catch (Exception x) {
				}
			}
		}

		renderJson(map);

	}
}
