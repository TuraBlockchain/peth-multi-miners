package hk.zdl.crypto.tura.miner.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import hk.zdl.crypto.tura.miner.MinerMonitor;
import hk.zdl.crypto.tura.miner.main.TuraConfig;

public class Util {
	private static final ExecutorService es = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		return t;
	});

	public static MinerMonitor buildMinerProces(BigInteger id, String passphrase, List<Path> plot_dirs, URL server_url) throws Exception {
		var conf_file = LocalMiner.build_conf_file(id.toString(), passphrase, plot_dirs, server_url, null);
		var miner_bin = LocalMiner.copy_miner();
		var proc = LocalMiner.build_process(miner_bin, conf_file);
		var mon = new MinerMonitor(proc);
		es.submit(mon);
		return mon;
	}

	public static final Map<String, Long> systemMemory() {
		Map<String, Long> map = new TreeMap<>();
		try {
			Process process = new ProcessBuilder("free", "-b", "-t").start();
			List<String> list = IOUtils.readLines(process.getInputStream(), "UTF-8");
			String[] line = list.get(list.size() - 1).replace("：", " ").split("\\s+");
			map.put("total", Long.valueOf(line[1]));
			map.put("used", Long.valueOf(line[2]));
			map.put("free", Long.valueOf(line[3]));
		} catch (IOException e) {
		}
		return map;
	}

	public static final Map<String, Map<String, Object>> diskUsage() {
		Map<String, Map<String, Object>> map = new TreeMap<>();
		try {
			Process process = new ProcessBuilder("df").start();
			IOUtils.readLines(process.getInputStream(), "UTF-8").stream().filter(s -> s.startsWith("/")).map(s -> s.split("\\s+")).forEach(s -> {
				Map<String, Object> m = new TreeMap<>();
				m.put("device", s[0]);
				m.put("size", Long.valueOf(s[1]));
				m.put("used", Long.valueOf(s[2]));
				m.put("avail", Long.valueOf(s[3]));
				m.put("ratio", s[4]);
				map.put(s[s.length - 1], m);
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
		return IOUtils.readLines(new ProcessBuilder("smartctl", "-a", device_path).start().inputReader()).stream().filter(s -> s.startsWith("194 Temperature_Celsius"))
				.map(s -> s.substring(s.lastIndexOf(' ') + 1)).mapToInt(Integer::parseInt).findAny().getAsInt();
	}

	public static final int isa_temputure_cel() throws Exception {
		return new JSONObject(new JSONTokener(new ProcessBuilder("sensors", "-j").start().getInputStream())).getJSONObject("coretemp-isa-0000").getJSONObject("Package id 0").getNumber("temp1_input")
				.intValue();
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

	public static final Process plot(Path plot_bin, Path target, boolean benchmark, BigInteger id, long start_nonce, long nonces, PlotProgressListener listener) throws IOException {
		if (!Files.exists(plot_bin.toAbsolutePath())) {
			plot_bin = findPath(plot_bin);
		}
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
		l.addAll(Arrays.asList("--id", id.toString(), "--sn", Long.toString(start_nonce), "--n", Long.toString(nonces), "-p", target.toAbsolutePath().toString()));
		Process proc = new ProcessBuilder(l).start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		BufferedReader reader = proc.inputReader();
		BlockingQueue<String> queue = new LinkedBlockingQueue<>();
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			} else {
				line = line.trim();
			}
			if (line.isEmpty() || line.equals("[2A")) {
				continue;
			} else if (line.startsWith("Error: ")) {
				reader.close();
				throw new IOException(line.substring("Error: ".length()));
			} else if (line.equals("Starting plotting...")) {
				es.submit(new Callable<Void>() {

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
								if (line.contains("鈹傗")) {
									byte[] bArr = line.getBytes("GBK");
									line = new String(bArr, "UTF-8");
									line = line.replace("�?", "│");
								}
								queue.offer(line);
							}
						}
						return null;
					}
				});
				break;
			}
		}
		es.submit(new Callable<Void>() {

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

	private static final Path findPath(Path p) throws IOException {
		return IOUtils.readLines(new ProcessBuilder().command("which", p.toString()).start().getInputStream(), "UTF-8").stream().map(Paths::get).findFirst().get();
	}

}
