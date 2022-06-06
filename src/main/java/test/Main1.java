package test;

import java.io.File;
import java.nio.file.Path;

import tura.miner.dir.DirChangeListener;
import tura.miner.dir.DirChangeWatcher;

public class Main1 {

	public static void main(String[] args) throws Throwable {
		Path path = new File("/media/david").toPath();
		DirChangeWatcher watcher = new DirChangeWatcher(path);
		watcher.addDirChangeListener(new DirChangeListener() {

			@Override
			public void onEntryCreate(Path path) {
				System.out.println("create" + "\n" + path);
			}

			@Override
			public void onEntryDelete(Path path) {
				System.out.println("delete" + "\n" + path);
			}
		});
		watcher.start();
	}

}
