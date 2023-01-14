package hk.zdl.crypto.tura.miner.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.render.TextRender;

import tura.miner.util.PlotProgressListener;
import tura.miner.util.Util;

@Path(value = "/api/v1/plot")
public class PlotController extends Controller {

	private static final List<PlotProgress> plot_progress = new LinkedList<>();
	private static final Gson gson = new Gson();

	public void add() {
		if (!getRequest().getMethod().equals("POST")) {
			renderError(405);
		}
		String _path = null;
		BigInteger id = null;
		long sn = 0, nounces = 0;
		try {
			JSONObject jobj = new JSONObject(getRawData());
			id = jobj.getBigInteger("id");
			sn = jobj.getLong("start_nounce");
			nounces = jobj.getLong("nounces");
			_path = jobj.getString("target_path");
		} catch (JSONException e) {
			renderError(400);
		}
		PlotProgress prog = new PlotProgress(_path);
		try {
			Util.plot(Paths.get(""), Paths.get(_path), false, id, sn, nounces, prog);
		} catch (IOException e) {
			if (e.getMessage().contains("insufficient disk space")) {
				renderError(507, new TextRender(e.getMessage()));
			} else {
				renderError(500, new TextRender(e.getMessage()));
			}
		}
		plot_progress.add(prog);
		renderJson("plot started!");
	}

	public void list() {
		renderText(gson.toJson(plot_progress), "application/json");
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
