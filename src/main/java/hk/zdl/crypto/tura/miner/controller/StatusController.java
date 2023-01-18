package hk.zdl.crypto.tura.miner.controller;

import java.io.File;
import java.util.TreeMap;

import com.formdev.flatlaf.util.SystemInfo;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.tura.miner.main.TuraConfig;
import hk.zdl.crypto.tura.miner.util.Util;

@Path(value = "/api/v1/status")
public class StatusController extends Controller {

	public void index() {
		var file = new File(".");
		var map = new TreeMap<>();
		map.put("version", 1);
		var disk = new TreeMap<>();
		disk.put("device", "default");
		disk.put("size", file.getTotalSpace());
		disk.put("avail", file.getUsableSpace());
		var used = file.getTotalSpace() - file.getUsableSpace();
		var ratio = 1.0 * used / file.getTotalSpace();
		disk.put("used", used);
		disk.put("ratio", (int) (ratio * 100) + "%");
		map.put("disk", disk);
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

		renderJson(map);

	}
}
