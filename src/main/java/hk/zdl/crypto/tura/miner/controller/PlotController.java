package hk.zdl.crypto.tura.miner.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.formdev.flatlaf.util.SystemInfo;
import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.pearlet.plot.PlotProgressListener;
import hk.zdl.crypto.pearlet.plot.PlotUtil;
import hk.zdl.crypto.tura.miner.MinerProcessManager;

@Path(value = "/api/v1/plot")
public class PlotController extends Controller {

	static {
		new Thread(PlotController.class.getSimpleName()) {

			@Override
			public void run() {
				while (true) {
					Entry entry = null;
					try {
						entry = queue.take();
						entry.call();
					} catch (Exception e) {
						if (entry != null) {
							entry.prog.write_eta = e.getMessage();
						}
					}
				}
			}
		}.start();
	}
	private static final BlockingQueue<Entry> queue = new LinkedBlockingQueue<>();
	private static final List<PlotProgress> plot_progress = Collections.synchronizedList(new LinkedList<>());
	private static final Gson gson = new Gson();
	private static File plotter_bin = null;

	public void add() throws Exception {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		String path;
		BigInteger id;
		long sn, nounces;
		var jobj = new JSONObject(getRawData());
		id = jobj.getBigInteger("id");
		sn = jobj.optLong("start_nounce", Math.abs(new Random().nextLong()));
		nounces = jobj.getLong("nounces");
		path = jobj.getString("target_path");
		if (plotter_bin == null) {
			plotter_bin = copy_plotter();
		}
		var prog = new PlotProgress(id, path);
		prog.restart = jobj.optBoolean("restart");
		var entry = new Entry(() -> PlotUtil.plot(plotter_bin.toPath(), Paths.get(path), false, id, sn, nounces, prog, new LinkedList<>()), prog);
		queue.offer(entry);
		plot_progress.add(prog);
		renderText("plot plan added!");
	}

	public void del() throws Exception {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		var jobj = new JSONObject(getRawData());
		var index = jobj.getInt("index");
		var prog = plot_progress.get(index);
		var itr = queue.iterator();
		while (itr.hasNext()) {
			var x = itr.next();
			if (x.prog.equals(prog)) {
				itr.remove();
				break;
			}
		}
		plot_progress.remove(index);
		renderText("plot plan deleted!");
	}

	public void list() {
		renderText(gson.toJson(plot_progress), "application/json");
	}

	public void clear_done() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		var itr = plot_progress.iterator();
		while (itr.hasNext()) {
			var x = itr.next();
			if (x.isDone()) {
				itr.remove();
			}
		}
		renderText("ok");
	}

	protected static void restart_miner(BigInteger id) {
		MinerProcessManager.me.stop_miner(id);
		MinerProcessManager.me.start_miner(id, true);
	}

	private static File copy_plotter() throws IOException {
		String suffix = "";
		if (SystemInfo.isWindows) {
			suffix = ".exe";
		}
		File tmp_file = File.createTempFile("plotter-", suffix);
		tmp_file.deleteOnExit();
		String in_filename = "";
		if (SystemInfo.isLinux) {
			in_filename = "signum-plotter";
		} else if (SystemInfo.isWindows) {
			in_filename = "signum-plotter.exe";
		} else if (SystemInfo.isMacOS) {
			in_filename = "signum-plotter-x86_64-apple-darwin.zip";
		}
		var in = PlotController.class.getClassLoader().getResourceAsStream("plotter/" + in_filename);
		var out = new FileOutputStream(tmp_file);
		IOUtils.copy(in, out);
		out.flush();
		out.close();
		in.close();
		if (SystemInfo.isMacOS) {
			var zipfile = new ZipFile(tmp_file);
			var entry = zipfile.stream().findAny().get();
			in = zipfile.getInputStream(entry);
			tmp_file = File.createTempFile("plotter-", ".app");
			tmp_file.deleteOnExit();
			out = new FileOutputStream(tmp_file);
			IOUtils.copy(in, out);
			out.flush();
			out.close();
			in.close();
			zipfile.close();
		}
		tmp_file.setExecutable(true);
		return tmp_file;
	}

	public static class PlotProgress implements PlotProgressListener {
		BigInteger id;
		boolean restart;
		String path, hash_rate, hash_eta, write_rate, write_eta;
		float hash_progress, write_progress;

		public PlotProgress(BigInteger id, String path) {
			this.id = id;
			this.path = path;
		}

		@Override
		public void onProgress(Type type, float progress, String rate, String eta) {
			switch (type) {
			case HASH:
				hash_progress = progress;
				hash_rate = rate;
				hash_eta = eta;
				break;
			case WRIT:
				write_progress = progress;
				write_rate = rate;
				write_eta = eta;
				break;
			default:
				break;

			}
			if (restart && type == Type.WRIT && progress >= 100) {
				restart_miner(id);
			}
		}

		boolean isDone() {
			return write_progress >= 100;
		}

		@Override
		public String toString() {
			return gson.toJson(this);
		}
	}

	private static final class Entry implements Callable<Process> {
		final Callable<Process> call;
		final PlotProgress prog;

		Entry(Callable<Process> call, PlotProgress prog) {
			super();
			this.call = call;
			this.prog = prog;
		}

		@Override
		public Process call() throws Exception {
			return call.call();
		}
	}

}
