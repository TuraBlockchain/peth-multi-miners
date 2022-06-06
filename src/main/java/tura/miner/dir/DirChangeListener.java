package tura.miner.dir;

import java.nio.file.Path;

public interface DirChangeListener {

	public void onEntryCreate(Path path);

	public void onEntryDelete(Path path);
}
