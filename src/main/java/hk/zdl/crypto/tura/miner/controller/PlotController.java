package hk.zdl.crypto.tura.miner.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.formdev.flatlaf.util.SystemInfo;
import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;

import hk.zdl.crypto.tura.miner.util.PlotProgressListener;
import hk.zdl.crypto.tura.miner.util.Util;

@Path(value = "/api/v1/plot")
public class PlotController extends Controller {

	static {
		new Thread() {

			@Override
			public void run() {
				while (true) {
					try {
						q.take().run();
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}.start();
	}
	private static final BlockingQueue<Runnable> q = new LinkedBlockingQueue<>();
	private static final List<PlotProgress> plot_progress = new LinkedList<>();
	private static final Gson gson = new Gson();
	private static File plotter_bin = null;

	public void add() throws Exception {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		String path;
		BigInteger id;
		long sn, nounces;
		JSONObject jobj = new JSONObject(getRawData());
		id = jobj.getBigInteger("id");
		sn = jobj.optLong("start_nounce", Math.abs(new Random().nextLong()));
		nounces = jobj.getLong("nounces");
		path = jobj.getString("target_path");
		if (plotter_bin == null) {
			plotter_bin = copy_plotter();
		}
		PlotProgress prog = new PlotProgress(path);
		prog.id = id;
		q.offer(() -> {
			try {
				Util.plot(plotter_bin.toPath(), Paths.get(path), false, id, sn, nounces, prog).waitFor();
			} catch (Exception e) {
				Logger.getLogger(getClass()).error(e.getMessage(), e);
			}
		});
		plot_progress.add(prog);
		renderText("plot queued!");
	}

	public void list() {
		renderText(gson.toJson(plot_progress), "application/json");
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
		InputStream in = PlotController.class.getClassLoader().getResourceAsStream("plotter/" + in_filename);
		FileOutputStream out = new FileOutputStream(tmp_file);
		IOUtils.copy(in, out);
		out.flush();
		out.close();
		in.close();
		if (SystemInfo.isMacOS) {
			ZipFile zipfile = new ZipFile(tmp_file);
			ZipEntry entry = zipfile.stream().findAny().get();
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

	public static final class PlotProgress implements PlotProgressListener {
		BigInteger id;
		String path, hash_rate, hash_eta, write_rate, write_eta;
		float hash_progress, write_progress;

		public PlotProgress(String path) {
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
		}

		@Override
		public String toString() {
			return "PlotProgress [id=" + id + ", path=" + path + ", hash_rate=" + hash_rate + ", hash_eta=" + hash_eta + ", write_rate=" + write_rate + ", write_eta=" + write_eta + ", hash_progress="
					+ hash_progress + ", write_progress=" + write_progress + "]";
		}
	}

}
