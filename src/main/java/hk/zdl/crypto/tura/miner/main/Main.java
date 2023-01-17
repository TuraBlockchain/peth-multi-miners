package hk.zdl.crypto.tura.miner.main;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Taskbar;
import java.awt.TrayIcon;
import java.math.BigInteger;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.formdev.flatlaf.util.SystemInfo;
import com.jfinal.server.undertow.UndertowServer;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.Util;
import hk.zdl.crypto.tura.miner.MinerProcessManager;

public class Main {

	public static void main(String[] args) throws Throwable {
		System.setProperty("derby.system.home", Files.createTempDirectory("derby-").toAbsolutePath().toString());
		var server = UndertowServer.create(TuraConfig.class);
		server.setPort(Util.getProp().getInt("local_port"));
		server.start();
		MyDb.create_missing_tables();

		MyDb.getAccounts().stream().map(r -> r.getStr("ADDRESS")).map(BigInteger::new).forEach(i -> {
			try {
				MinerProcessManager.me.start_miner(i);
			} catch (Exception e) {
				Logger.getLogger(Main.class).error(e.getMessage(), e);
			}
		});
		if (SystemInfo.isMacOS || SystemInfo.isWindows) {
			try {
				var app_icon = ImageIO.read(Util.getResource("app_icon.png"));
				Taskbar.getTaskbar().setIconImage(app_icon);
				var quit_menu_item = new MenuItem("Quit");
				quit_menu_item.addActionListener((e) -> {
					server.stop();
					System.exit(0);
				});
				var menu = new PopupMenu();
				menu.add(quit_menu_item);
				var trayIcon = new TrayIcon(app_icon, "tura miner", menu);
				trayIcon.setImageAutoSize(true);
				SystemTray.getSystemTray().add(trayIcon);
			} catch (Exception e) {
			}

		}
	}

}
