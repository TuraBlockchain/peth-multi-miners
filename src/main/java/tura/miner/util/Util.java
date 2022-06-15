package tura.miner.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.yaml.snakeyaml.Yaml;

import tura.miner.MinerMonitor;
import tura.miner.main.TuraConfig;

public class Util {

	public static final MinerMonitor buildMinerProces(Path miner_bin_path, Map<BigInteger, String> accounts, List<Path> plot_dirs, URL server_url) throws Exception {
		Map<String, Object> m = new TreeMap<>();
		m.put("account_id_to_secret_phrase", accounts);
		m.put("plot_dirs", plot_dirs.stream().map(o -> o.toAbsolutePath().toString()).collect(Collectors.toList()));
		m.put("url", server_url.toString());
		m.put("hdd_reader_thread_count", 0);
		m.put("cpu_threads", 0);
		m.put("cpu_worker_task_count", 4);
		m.put("gpu_threads", 1);
		m.put("console_log_pattern", "{m}{n}");
		String yaml_conf = new Yaml().dump(m);

		Process proc = new ProcessBuilder(miner_bin_path.toString(), "-c", "/dev/stdin").start();
		OutputStream out = proc.getOutputStream();
		out.write(yaml_conf.getBytes());
		out.close();

		MinerMonitor mon = new MinerMonitor(proc);
		MySingleton.es.submit(mon);
		return mon;
	}

	public static final Map<String, Long> systemMemory() {
		Map<String, Long> map = new TreeMap<>();
		try {
			Process process = new ProcessBuilder("free", "-b", "-t").start();
			String[] line = IOUtils.readLines(process.getInputStream(), "UTF-8").stream().filter(s -> s.startsWith("Total:")).findAny().get().split("\\s+");
			map.put("total", Long.valueOf(line[1]));
			map.put("used", Long.valueOf(line[2]));
			map.put("free", Long.valueOf(line[3]));
		} catch (IOException e) {
		}
		return map;
	}

	public static final Map<String, Map<String, String>> diskUsage() {
		Map<String, Map<String, String>> map = new TreeMap<>();
		try {
			Process process = new ProcessBuilder("df").start();
			IOUtils.readLines(process.getInputStream(), "UTF-8").stream().filter(s -> s.startsWith("/")).map(s -> s.split("\\s+")).forEach(s -> {
				Map<String, String> m = new TreeMap<>();
				m.put("device", s[0]);
				m.put("size", s[1]);
				m.put("used", s[2]);
				m.put("avail", s[3]);
				m.put("ratio", s[4]);
				map.put(s[5], m);
			});
		} catch (IOException e) {
		}
		return map;
	}

	public static final int disk_temputure_cel(String device_path) throws Exception {
		if (!device_path.startsWith("/dev/sd")) {
			throw new IllegalArgumentException();
		}
		if (!TuraConfig.isRunningOnRoot()) {
			throw new IOException("must run in root");
		}
		if (!new File(device_path).exists()) {
			throw new IOException("device not exist");
		}
		return new JSONObject(new JSONTokener(new ProcessBuilder("smartctl", "-H", "-j", device_path).start().getInputStream())).getJSONObject("temperature").getInt("current");
	}

	public static final Callable<String> pingServer(String url) {
		return new Callable<String>() {

			@Override
			public String call() throws Exception {
				String host = new URL(url).getHost();
				Process process = new ProcessBuilder("ping", "-c", "1", host).start();
				List<String> lines = IOUtils.readLines(process.getInputStream(), "UTF-8");
				String str = lines.get(lines.size() - 1);
				str = str.substring(str.indexOf("=") + 1);
				str = str.substring(0, str.indexOf("/"));
				str = str.trim();
				return str;
			}
		};
	}

	public static final Process plot(Path plot_bin, Path target, boolean benchmark, long id, long start_nonce, long nonces, PlotProgressListener listener) throws IOException {
		if (!plot_bin.toFile().exists()) {
			throw new FileNotFoundException(plot_bin.toString());
		} else if (!plot_bin.toFile().isFile()) {
			throw new FileNotFoundException("not a file: " + plot_bin.toString());
		} else if (!plot_bin.toFile().canRead()) {
			throw new IOException("cannot read: " + plot_bin.toString());
		} else if (!plot_bin.toFile().canExecute()) {
			throw new IOException("not executable: " + plot_bin.toString());
		}
		if (!target.toFile().exists()) {
			throw new FileNotFoundException(target.toString());
		} else if (!target.toFile().isDirectory()) {
			throw new IOException("not dir: " + target.toString());
		}
		List<String> l = new LinkedList<>();
		l.add(plot_bin.toAbsolutePath().toString());
		if (benchmark) {
			l.add("-b");
		}
		l.addAll(Arrays.asList("--id", Long.toString(id), "--sn", Long.toString(start_nonce), "--n", Long.toString(nonces), "-p", target.toAbsolutePath().toString()));
		Process proc = new ProcessBuilder(l).start();
		BufferedReader reader = proc.inputReader();
		BlockingQueue<String> queue = new LinkedBlockingQueue<>();
		while (true) {
			String line = reader.readLine().trim();
			if (line.isEmpty() || line.equals("[2A")) {
				continue;
			} else if (line.startsWith("Error: ")) {
				reader.close();
				throw new IOException(line.substring("Error: ".length()));
			} else if (line.equals("Starting plotting...")) {
				MySingleton.es.submit(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						String line = null;
						while (true) {
							line = reader.readLine();
							if (line == null) {
								queue.offer(null);
								break;
							} else if (line.isEmpty() || line.equals("[2A")) {
								continue;
							} else {
								queue.offer(line);
							}
						}
						return null;
					}
				});
				break;
			}
		}
		MySingleton.es.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				String line = null;
				while (true) {
					line = queue.take();
					if (line == null) {
						break;
					}
					if (line.startsWith("Hashing:") || line.startsWith("Writing:")) {
						PlotProgressListener.Type type = line.startsWith("H") ? PlotProgressListener.Type.HASH : PlotProgressListener.Type.WRIT;
						line = line.substring(line.lastIndexOf('│') + 1);
						float progress = Float.parseFloat(line.substring(0, line.indexOf('%')).trim());
						line = line.substring(line.indexOf('%') + 1).trim();
						String rate, eta = "";
						if (line.endsWith("B/s")) {
							rate = line;
						} else {
							rate = line.substring(0, line.lastIndexOf(" ")).trim().replace(" ", "");
							eta = line.substring(line.lastIndexOf(" ")).trim();
						}
						listener.onProgress(type, progress, rate, eta);
					}
				}
				return null;
			}
		});
		return proc;
	}
}
