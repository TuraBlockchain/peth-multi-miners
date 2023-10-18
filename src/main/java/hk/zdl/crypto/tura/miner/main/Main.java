package hk.zdl.crypto.tura.miner.main;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Taskbar;
import java.awt.Taskbar.Feature;
import java.awt.TrayIcon;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.extras.FlatDesktop;
import com.jfinal.server.undertow.UndertowServer;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.Util;
import hk.zdl.crypto.tura.miner.MinerProcessManager;

public class Main {

	private static final UndertowServer server = UndertowServer.create(TuraConfig.class);

	public static void main(String[] args) throws Throwable {
		System.setProperty("derby.system.home", Files.createTempDirectory(null).toFile().getAbsolutePath());
		server.setPort(Util.getProp().getInt("local_port"));
		server.setResourcePath("src/main/webapp,classpath");
		server.start();
		MyDb.create_missing_tables();

		MinerProcessManager.me.start_all();
		add_tray_icon();
		FlatDesktop.setAboutHandler(() -> {
			try {
				Desktop.getDesktop().browse(new URI("https://tura.world"));
			} catch (Exception e) {
			}
		});
	}

	private static final void add_tray_icon() {
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			var app_icon = ImageIO.read(Util.getResource("app_icon.png"));
			var quit_menu_item = new MenuItem("Quit");
			quit_menu_item.addActionListener((e) -> {
				if (JOptionPane.showConfirmDialog(null, "Are you sure to quit tura miner?", "Quit", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
					server.stop();
					MinerProcessManager.me.list_miners().forEach(o -> o.destroyForcibly());
					System.exit(0);
				}
			});
			var add_path_item = new MenuItem("Add Miner Path...");
			add_path_item.addActionListener((e) -> {
				var file_dialog = new JFileChooser();
				file_dialog.setDialogType(JFileChooser.OPEN_DIALOG);
				file_dialog.setMultiSelectionEnabled(false);
				file_dialog.setDragEnabled(false);
				file_dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int i = file_dialog.showOpenDialog(null);
				if (i != JFileChooser.APPROVE_OPTION) {
					return;
				}
				var file = file_dialog.getSelectedFile();
				try {
					i = add_miner_path(file.toPath());
					JOptionPane.showMessageDialog(null, "Paths added: " + i);
				} catch (Exception x) {
					Logger.getGlobal().log(Level.SEVERE, x.getMessage(), x);
				}
			});
			var menu = new PopupMenu();
			menu.add(add_path_item);
			menu.add(quit_menu_item);
			if (SystemTray.isSupported()) {
				var trayIcon = new TrayIcon(app_icon, "tura miner", menu);
				trayIcon.setImageAutoSize(true);
				SystemTray.getSystemTray().add(trayIcon);
			}
			if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Feature.ICON_IMAGE)) {
				Taskbar.getTaskbar().setIconImage(app_icon);
			}
		} catch (Exception x) {
			Logger.getGlobal().log(Level.SEVERE, x.getMessage(), x);
		}
	}

	private static final int add_miner_path(Path p) throws Exception {
		var file_in_it = Files.list(p).filter(a -> Files.isRegularFile(a) && a.toFile().getName().contains("_")).filter(a -> {
			var s = a.toFile().getName();
			s = s.substring(0, s.indexOf('_'));
			try {
				return new BigInteger(s).compareTo(BigInteger.ZERO) >= 0;
			} catch (Exception e) {
				return false;
			}
		}).toList();
		var dir_in_it = Files.list(p).filter(a -> Files.isDirectory(a)).filter(a -> {
			try {
				return new BigInteger(a.toFile().getName()).compareTo(BigInteger.ZERO) >= 0;
			} catch (Exception e) {
				return false;
			}
		}).toList();
		if (file_in_it.size() > 0 && dir_in_it.isEmpty()) {
			var id_set = new TreeSet<String>();
			file_in_it.stream().map(a -> a.toFile().getName()).map(s -> s.substring(0, s.indexOf('_'))).forEach(id_set::add);
			if (id_set.size() == 1) {
				return add_miner_path(id_set.first(), p);
			}
		} else if (file_in_it.isEmpty() && dir_in_it.size() > 0) {
			return dir_in_it.stream().mapToInt(a -> add_miner_path(a.toFile().getName(), a)).sum();
		}
		return 0;
	}

	private static final int add_miner_path(String id, Path p) {
		return MyDb.addMinerPath(id, p) ? 1 : 0;
	}
}
