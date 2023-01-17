package hk.zdl.crypto.tura.miner.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.formdev.flatlaf.util.SystemInfo;
import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.render.TextRender;

import hk.zdl.crypto.tura.miner.util.PlotProgressListener;
import hk.zdl.crypto.tura.miner.util.Util;

@Path(value = "/api/v1/plot")
public class PlotController extends Controller {

	private static final ExecutorService plot_single_thread = Executors.newSingleThreadExecutor();
	private static final List<PlotProgress> plot_progress = new LinkedList<>();
	private static final Gson gson = new Gson();
	private static File plotter_bin = null;

	public void add() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		String path = null;
		BigInteger id = null;
		long sn = 0, nounces = 0;
		boolean queue = false;
		try {
			JSONObject jobj = new JSONObject(getRawData());
			id = jobj.getBigInteger("id");
			sn = jobj.getLong("start_nounce");
			nounces = jobj.getLong("nounces");
			path = jobj.getString("target_path");
			queue = jobj.optBoolean("queue");
		} catch (JSONException e) {
			renderError(400);
		}
		PlotProgress prog = new PlotProgress(path);
		if (queue) {
			var _path = path;
			var _id = id;
			var _sn = sn;
			var _n = nounces;
			try {
				if (plotter_bin == null) {
					plotter_bin = copy_plotter();
				}
			} catch (IOException e) {
				renderError(500, new TextRender(e.getMessage()));
				return;
			}
			plot_single_thread.submit(() -> Util.plot(plotter_bin.toPath(), Paths.get(_path), false, _id, _sn, _n, prog));
			plot_progress.add(prog);
			renderText("plot queued!");
		} else {
			try {
				if (plotter_bin == null) {
					plotter_bin = copy_plotter();
				}
				Util.plot(plotter_bin.toPath(), Paths.get(path), false, id, sn, nounces, prog);
			} catch (IOException e) {
				if (e.getMessage().contains("insufficient disk space")) {
					renderError(507, new TextRender(e.getMessage()));
				} else {
					renderError(500, new TextRender(e.getMessage()));
				}
			}
			plot_progress.add(prog);
			renderText("plot started!");
		}
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
			return "PlotProgress [path=" + path + ", hash_progress=" + hash_progress + ", hash_rate=" + hash_rate + ", hash_eta=" + hash_eta + ", write_progress=" + write_progress + ", write_rate="
					+ write_rate + ", write_eta=" + write_eta + "]";
		}
	}

}
