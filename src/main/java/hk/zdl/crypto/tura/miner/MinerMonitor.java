package hk.zdl.crypto.tura.miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class MinerMonitor implements Runnable {

	private final Process proc;
	private final BufferedReader reader;
	private final Map<String,Object> map = new TreeMap<>();;

	public MinerMonitor(Process proc) {
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

	@Override
	public void run() {
		map.put("start_time", System.currentTimeMillis());
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
				if (line.startsWith("plot files loaded:")) {
					String cap = line.substring(line.indexOf("total capacity=") + "total capacity=".length());
					map.put("total capacity", cap);
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
