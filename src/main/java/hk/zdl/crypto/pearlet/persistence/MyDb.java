package hk.zdl.crypto.pearlet.persistence;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class MyDb {

	public static final List<String> getTables() {
		return Db.query("select st.tablename from sys.systables st LEFT OUTER join sys.sysschemas ss on (st.schemaid = ss.schemaid) where ss.schemaname ='APP'");
	}

	public static final boolean create_table(String table_name) {
		Prop prop = PropKit.use("sql/create_tables.txt");
		String sql = prop.get(table_name);
		if (sql == null || sql.isBlank()) {
			return false;
		}
		try {
			var conn = Db.use().getConfig().getConnection();
			var st = conn.createStatement();
			st.execute(sql);
			st.getResultSet();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	public static final void create_missing_tables() {
		List<String> tables = getTables();
		Prop prop = PropKit.use("sql/create_tables.txt");
		prop.getProperties().keySet().stream().map(o -> o.toString().trim().toUpperCase()).filter(s -> !tables.contains(s)).map(s -> s.toLowerCase()).forEach(MyDb::create_table);
	}

	public static final List<Record> list_server_url() {
		return Db.find("select * from networks");
	}

	public static final boolean insert_server_url(String str) {
		return Db.save("networks", new Record().set("URL", str));
	}

	public static final synchronized boolean update_server_url(int id, String url) {
		var o = Db.findFirst("select * from networks where id = ?", id);
		if (o == null) {
			return false;
		} else {
			o.set("URL", url);
			return Db.update("networks", "ID", o);
		}
	}

	public static final boolean delete_server_url(int id) {
		return Db.deleteById("networks", "id", id);
	}

	public static final Optional<Record> getAccount(String address) {
		var r = Db.findFirst("select * from ACCOUNTS WHERE ADDRESS = ?", address);
		return r == null ? Optional.empty() : Optional.of(r);
	}

	public static final List<Record> getAccounts() {
		return Db.find("select * from ACCOUNTS");
	}

	public static final int getAccountCount() {
		return Db.queryInt("SELECT COUNT(*) FROM ACCOUNTS");
	}

	public static final boolean insertAccount(String address, String passphrase) {
		int i = Db.queryInt("SELECT COUNT(*) FROM ACCOUNTS WHERE ADDRESS = ?", address);
		if (i > 0) {
			return false;
		}
		var o = new Record().set("ADDRESS", address).set("PASSPHRASE", passphrase);
		return Db.save("ACCOUNTS", "ID", o);
	}

	public static final boolean deleteAccount(String address) {
		var r = Db.findFirst("select * from ACCOUNTS WHERE ADDRESS = ?", address);
		if (r != null) {
			return Db.deleteById("ACCOUNTS", "ID", r.getInt("ID"));
		} else {
			return false;
		}
	}

	public static final List<Path> getMinerPaths(String id) {
		return Db.find("SELECT PATH FROM APP.MPATH WHERE ACCOUNT = ?", id).stream().map(o -> Paths.get(o.getStr("PATH"))).toList();
	}

	public static final boolean addMinerPath(String id, Path path) {
		int i = Db.queryInt("SELECT COUNT(*) FROM APP.MPATH WHERE ACCOUNT = ? AND PATH = ?", id, path.toAbsolutePath().toString());
		if (i > 0) {
			return false;
		}
		var o = new Record().set("ACCOUNT", id).set("PATH", path.toAbsolutePath().toString());
		return Db.save("MPATH", "ID", o);
	}

	public static final boolean delMinerPath(String id, Path path) {
		var r = Db.findFirst("SELECT * FROM APP.MPATH WHERE ACCOUNT = ? AND PATH = ?", id, path.toAbsolutePath().toString());
		if (r != null) {
			return Db.deleteById("MPATH", "ID", r.get("ID"));
		} else {
			return false;
		}
	}
}
