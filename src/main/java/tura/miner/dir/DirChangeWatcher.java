package tura.miner.dir;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;

public class DirChangeWatcher extends Thread {

	private final List<DirChangeListener> listsners = new LinkedList<DirChangeListener>();
	private final Path path;

	public DirChangeWatcher(Path path) {
		this.path = path;
	}

	public boolean addDirChangeListener(DirChangeListener l) {
		return listsners.add(l);
	}

	public boolean removeDirChangeListener(DirChangeListener l) {
		return listsners.remove(l);
	}

	@Override
	public void run() {
		try {
			WatchService watcher = path.getFileSystem().newWatchService();
			path.register(watcher, ENTRY_CREATE, ENTRY_DELETE);
			while (true) {
				WatchKey key = watcher.take();
				for (WatchEvent<?> event : key.pollEvents()) {
					Path p = Path.of(path.toAbsolutePath().toString(), ((Path)event.context()).getFileName().toString());
					if (event.kind().equals(ENTRY_CREATE)) {
						listsners.forEach(l -> l.onEntryCreate(p));
					} else if (event.kind().equals(ENTRY_DELETE)) {
						listsners.forEach(l -> l.onEntryDelete(p));
					}
				}
				key.reset();
			}
		} catch (Exception e) {
		}
	}

}
