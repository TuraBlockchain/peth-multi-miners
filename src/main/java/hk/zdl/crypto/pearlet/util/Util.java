package hk.zdl.crypto.pearlet.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class Util {

	private static final ExecutorService es = Executors.newCachedThreadPool((r) -> {
		Thread t = new Thread(r, "");
		t.setDaemon(true);
		return t;
	});

	public static final Prop getProp() {
		return PropKit.use("config.txt");
	}

	public static final String getDBURL() {
		Prop prop = getProp();
		return getDBURL(prop.get("appName"), prop.get("appVersion"), prop.get("appAuthor"), prop.get("dbName"));
	}

	public static final String getDBURL(String app_name, String app_version, String author, String db_name) {
		AppDirs appDirs = AppDirsFactory.getInstance();
		String user_dir = appDirs.getUserDataDir(app_name, app_version, author, false);
		user_dir += File.separator + db_name;
		user_dir = user_dir.replace('\\', '/');
		String db_url = "jdbc:derby:directory:" + user_dir + ";create=true";
		return db_url;
	}

	private static final Properties user_settings = new Properties();

	public static final Properties getUserSettings() {
		if (user_settings.isEmpty()) {
			try {
				loadUserSettings();
			} catch (IOException e) {
			}
		}
		return user_settings;
	}

	public static final void loadUserSettings() throws IOException {
		Prop prop = getProp();
		AppDirs appDirs = AppDirsFactory.getInstance();
		String user_dir = appDirs.getUserDataDir(prop.get("appName"), prop.get("appVersion"), prop.get("appAuthor"), false);
		user_dir += File.separator + "settings.txt";
		var path = Paths.get(user_dir);
		if (Files.exists(path)) {
			user_settings.load(Files.newInputStream(path));
		}
	}

	public static final void saveUserSettings() throws IOException {
		AppDirs appDirs = AppDirsFactory.getInstance();
		Prop prop = getProp();
		String user_dir = appDirs.getUserDataDir(prop.get("appName"), prop.get("appVersion"), prop.get("appAuthor"), false);
		user_dir += File.separator + "settings.txt";
		user_settings.store(Files.newOutputStream(Paths.get(user_dir), StandardOpenOption.CREATE), null);
	}

	public static final String getAppVersion() {
		var text = Util.class.getPackage().getImplementationVersion();
		if (text == null) {
			text = getProp().get("appVersion");
		}
		return text;
	}

	public static final InputStream getResourceAsStream(String path) {
		return Util.class.getClassLoader().getResourceAsStream(path);
	}

	public static final URL getResource(String path) {
		return Util.class.getClassLoader().getResource(path);
	}

	public static final <T> Future<T> submit(Callable<T> task) {
		return es.submit(task);
	}

	public static final Future<?> submit(Runnable task) {
		return es.submit(task);
	}

	public static Long getTime(Class<?> cl) {
		try {
			String rn = cl.getName().replace('.', '/') + ".class";
			JarURLConnection j = (JarURLConnection) cl.getClassLoader().getResource(rn).openConnection();
			return j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
		} catch (Exception e) {
			return null;
		}
	}
}
