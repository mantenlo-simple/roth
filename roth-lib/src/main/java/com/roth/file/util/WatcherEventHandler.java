package com.roth.file.util;

import java.nio.file.WatchEvent.Kind;

public interface WatcherEventHandler {
	void handle(String filename, Kind<?> eventKind);
}
