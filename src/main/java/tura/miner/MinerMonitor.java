package tura.miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class MinerMonitor extends Thread {

	private final String cid;
	private final Path plot_path;
	private final BufferedReader reader;
	private final Properties prop = new Properties();
	private final Map<String, Object> conf = new TreeMap<>();
	private boolean running = true;

	public MinerMonitor(String cid, Path plot_path, BufferedReader reader) {
		this.cid = cid;
		this.plot_path = plot_path;
		this.reader = reader;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getCid() {
		return cid;
	}

	public Path getPlotPath() {
		return plot_path;
	}

	public Map<String, Object> getConf() {
		return conf;
	}

	public String getProperty(String key) {
		return prop.getProperty(key);
	}

	public Set<Entry<Object, Object>> entrySet() {
		return prop.entrySet();
	}

	@Override
	public void run() {
		try {
			while (running) {
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
					prop.setProperty("total capacity", cap);
				} else if (line.startsWith("new block:")) {
					Stream.of(line.substring("new block:".length() + 1).split(",")).map(s -> s.split("=")).forEach(o -> {
						prop.put(o[0].trim(), Long.parseLong(o[1].trim()));
					});
				} else if (line.startsWith("round finished:")) {
					Stream.of(line.substring("round finished:".length() + 1).split(",")).map(s -> s.split("=")).forEach(o -> {
						prop.setProperty(o[0].trim(), o[1].trim());
					});
				}
			}
		} catch (IOException e) {
		}
	}
}
