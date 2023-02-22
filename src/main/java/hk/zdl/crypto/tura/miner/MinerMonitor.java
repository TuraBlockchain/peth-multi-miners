package hk.zdl.crypto.tura.miner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.json.JSONObject;

public class MinerMonitor extends Thread {

	private final Process proc;
	private final BufferedReader reader;
	private final Map<String, Object> map = new TreeMap<>();
	private File conf_file = null;
	private int file_count = 0;
	private BigDecimal capacity = new BigDecimal(0);
	private boolean auto_restart_process = true;

	public MinerMonitor(Process proc) {
		super(MinerMonitor.class.getSimpleName());
		this.proc = proc;
		reader = proc.inputReader();
	}

	public void destroy() {
		proc.destroy();
	}

	public Process destroyForcibly() {
		return proc.destroyForcibly();
	}

	public String getProperty(String key) {
		return map.get(key).toString();
	}

	public void setProperty(String key, Object value) {
		map.put(key, value);
	}

	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	public int getFileCount() {
		return file_count;
	}

	public BigDecimal getCapacity() {
		return capacity;
	}

	public void set_conf_file(File conf_file) {
		this.conf_file = conf_file;
	}

	public void set_auto_restart_process(boolean auto_restart_process) {
		this.auto_restart_process = auto_restart_process;
	}

	@Override
	public void run() {
		map.put("start time", System.currentTimeMillis());
		var stop_by_this = false;
		try {
			while (true) {
				var line = reader.readLine();
				if (line == null) {
					break;
				}
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				map.put("last refresh", System.currentTimeMillis());
				if (conf_file != null) {
					conf_file.delete();
					conf_file = null;
				}
				var level = "";
				if (line.indexOf('[') > -1 && line.indexOf(']') > -1) {
					level = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
					line = line.substring(line.indexOf(']') + 1);
				} else if (line.startsWith("message: {")) {
					var jobj = new JSONObject(line.substring(line.indexOf('{'), line.lastIndexOf('}') + 1));
					level = "ERROR";
					line = jobj.optString("result");
				}
				if (level.equals("ERROR")) {
					if (line.contains("=>")) {
						line = line.substring(line.indexOf("=>") + 2).trim();
					}
					map.put("last error", build_status_obj(line));
					if (line.equals("No mining licence")) {
						stop_by_this = true;
						proc.destroyForcibly();
						map.put("status", build_status_obj("No Mining Licence"));
					} else if (line.equals("connection outage...")) {
						map.put("status", build_status_obj("Network Error"));
					} else if (line.equals("outage resolved.")) {
						map.put("status", build_status_obj("Running"));
					}
				} else {
					map.put("status", build_status_obj("Running"));
				}
				if (line.startsWith("path=")) {
					line = line.split(", ")[1];
					line = line.substring(line.indexOf('=') + 1);
					file_count += Integer.parseInt(line);
				} else if (line.startsWith("plot files loaded:")) {
					var cap = line.substring(line.indexOf("total capacity=") + "total capacity=".length());
					cap = cap.replace(" TiB", "");
					capacity = new BigDecimal(cap);
					map.put("file count", file_count);
					map.put("capacity", capacity);
				} else if (line.startsWith("new block:")) {
					Stream.of(line.substring("new block:".length() + 1).split(",")).map(s -> s.split("=")).forEach(o -> {
						map.put(o[0].trim(), Long.parseLong(o[1].trim()));
					});
				} else if (line.startsWith("round finished:")) {
					Stream.of(line.substring("round finished:".length() + 1).split(",")).map(s -> s.split("=")).forEach(o -> {
						map.put(o[0].trim(), o[1].trim());
					});
				}
				if (map.containsKey("file count")) {
					if ((int) map.get("file count") == 0) {
						stop_by_this = true;
						proc.destroyForcibly();
						map.put("status", build_status_obj("No Plot Files"));
					}
				}
			}
		} catch (IOException e) {
			map.put("status", build_status_obj(e.getMessage()));
			return;
		}
		if (!stop_by_this) {
			map.put("status", build_status_obj("Process Terminated"));
			if (auto_restart_process) {
				new Thread() {

					@Override
					public void run() {
						var id = new BigInteger(getProperty("id"));
						try {
							MinerProcessManager.me.stop_miner(id);
						} catch (Exception e) {
						}
						try {
							MinerProcessManager.me.start_miner(id, true);
						} catch (Exception e) {
						}
					}
				}.start();
			}
		}
	}

	private static final Map<String, Object> build_status_obj(String msg) {
		var o = new TreeMap<String, Object>();
		o.put("msg", msg);
		o.put("time", System.currentTimeMillis());
		return o;
	}
}
