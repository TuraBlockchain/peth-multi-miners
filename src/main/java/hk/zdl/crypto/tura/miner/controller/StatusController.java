package hk.zdl.crypto.tura.miner.controller;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.formdev.flatlaf.util.SystemInfo;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.tura.miner.MinerProcessManager;
import hk.zdl.crypto.tura.miner.main.TuraConfig;
import hk.zdl.crypto.tura.miner.util.Util;

@Path(value = "/api/v1/status")
public class StatusController extends Controller {

	private static final long start_time = System.currentTimeMillis();

	public void index() {
		var file = new File(".");
		var map = new TreeMap<>();
		map.put("start time", start_time);
		map.put("version", hk.zdl.crypto.pearlet.util.Util.getAppVersion());
		map.put("build", hk.zdl.crypto.pearlet.util.Util.getTime(getClass()));
		map.put("memory", Util.systemMemory());
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
			if (TuraConfig.isRunningOnRoot()) {
				try {
					disk.put("temp_cel", Util.disk_temputure_cel(Util.diskUsage().get("/").get("device").toString().replaceAll("\\d", "")));
				} catch (Exception x) {
					Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
				}
			}
			try {
				var a = new TreeMap<>();
				a.put("temp_cel", Util.isa_temputure_cel());
				map.put("cpu", a);
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			}
		}
		if (map.get("cpu") == null) {
			var t = Util.cpu_temp();
			if (t.isPresent()) {
				map.put("cpu", Collections.singletonMap("temp_cel", t.get()));
			}
		}
		var s_u_count = MyDb.server_url_count();
		var miner = new TreeMap<>();
		miner.put("account count", MyDb.getAccountCount());
		miner.put("active miners", MinerProcessManager.me.list_miners().size());
		miner.put("plot file count", s_u_count > 0 ? MinerProcessManager.me.list_miners().stream().mapToInt(o -> o.getFileCount()).sum() / s_u_count : 0);
		miner.put("plot file size", s_u_count > 0 ? MinerProcessManager.me.list_miners().stream().map(o -> o.getCapacity()).reduce(BigDecimal::add).orElseGet(() -> BigDecimal.ZERO)
				.divide(new BigDecimal(s_u_count), RoundingMode.HALF_DOWN) : 0);
		map.put("miner", miner);
		renderJson(map);

	}

}
