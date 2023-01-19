package hk.zdl.crypto.tura.miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class MinerMonitor extends Thread {

	private final Process proc;
	private final BufferedReader reader;
	private final Map<String, Object> map = new TreeMap<>();
	private int file_count = 0;
	private double capacity = 0;

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

	public double getCapacity() {
		return capacity;
	}

	@Override
	public void run() {
		map.put("start time", System.currentTimeMillis());
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				map.put("last refresh", System.currentTimeMillis());
				var level = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
				line = line.substring(line.indexOf(']') + 1);
				if (level.equals("ERROR")) {
					var err = new TreeMap<>();
					err.put("msg", line);
					err.put("time", System.currentTimeMillis());
					map.put("last error", err);
				} else if (line.startsWith("path=")) {
					line = line.split(", ")[1];
					line = line.substring(line.indexOf('=') + 1);
					file_count += Integer.parseInt(line);
				} else if (line.startsWith("plot files loaded:")) {
					String cap = line.substring(line.indexOf("total capacity=") + "total capacity=".length());
					cap = cap.replace(" TiB", "");
					capacity = Double.parseDouble(cap);
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
			}
		} catch (IOException e) {
		}
	}
}
