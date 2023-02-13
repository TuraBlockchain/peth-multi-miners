package hk.zdl.crypto.tura.miner.main;

import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Taskbar;
import java.awt.TrayIcon;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.util.SystemInfo;
import com.jfinal.server.undertow.UndertowServer;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.Util;
import hk.zdl.crypto.tura.miner.MinerProcessManager;

public class Main {

	public static void main(String[] args) throws Throwable {
		System.setProperty("derby.system.home", Files.createTempDirectory(null).toFile().getAbsolutePath());
		var server = UndertowServer.create(TuraConfig.class);
		server.setPort(Util.getProp().getInt("local_port"));
		server.setResourcePath("src/main/webapp,classpath");
		server.start();
		MyDb.create_missing_tables();

		MyDb.getAccounts().stream().map(r -> r.getStr("ADDRESS")).filter(s -> MyDb.getMinerPaths(s).size() > 0).map(BigInteger::new).forEach(i -> {
			try {
				MinerProcessManager.me.start_miner(i);
			} catch (Exception e) {
				Logger.getLogger("").log(Level.SEVERE, e.getMessage(), e);
			}
		});
		if (SystemInfo.isMacOS || SystemInfo.isWindows) {
			if (!GraphicsEnvironment.isHeadless()) {
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
					var menu = new PopupMenu();
					menu.add(quit_menu_item);
					var trayIcon = new TrayIcon(app_icon, "tura miner", menu);
					trayIcon.setImageAutoSize(true);
					SystemTray.getSystemTray().add(trayIcon);
					if (Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
						Taskbar.getTaskbar().setIconImage(app_icon);
					}
				} catch (Exception e) {
					Logger.getLogger("").log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

}
