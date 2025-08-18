package com.roth.file.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

public class DirectoryWatcher implements Callable<Boolean> {
	private static final String DIRECTORY_NULL_ERROR = "directory cannot be null";
	private static final String HANDLER_NULL_ERROR = "handler cannot be null";
	
	private String directory;
	private WatcherEventHandler handler;
	
	private DirectoryWatcher(String directory, WatcherEventHandler handler) {
		boolean noDirectory = Data.isEmpty(directory);
		boolean noHandler = handler == null;
		if (noDirectory || noHandler)
			throw new IllegalArgumentException(String.format("DirectoryWatcher cannot be initialized due to the following errors: %s", 
					(noDirectory ? DIRECTORY_NULL_ERROR : "") + (noHandler ? HANDLER_NULL_ERROR : "")));
		this.directory = directory;
		this.handler = handler;
	}
	
	/**
	 * This is not intended for direct use, though good luck trying.  Please use the static watch methods.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Boolean call() throws IOException, InterruptedException {
		try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
			Path path = Paths.get(directory);
			path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			WatchKey key;
			// This is essentially an eternal loop.  This means that this class should be instantiated in a new thread. 
			while ((key = watcher.take()) != null) {
				WatchEvent<?> prevEvent = null;
				for (WatchEvent<?> event : key.pollEvents()) {
					if (prevEvent == null || !prevEvent.context().equals(event.context()) || !prevEvent.kind().equals(event.kind()))
						handler.handle(event.context().toString(), event.kind());
					prevEvent = event;
				}
				key.reset();
			}
		} catch (IOException | InterruptedException e) {
            Log.logException(e, "[SYSTEM]");;
        }		
		return true;
	}
	
	/**
	 * Watch for changes to any file in the specified directory.  The supplied handler will be called if any changes occur.
	 * @param directory
	 * @param handler
	 */
	public static void watch(String directory, WatcherEventHandler handler) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(new DirectoryWatcher(directory, handler));
	}
	
	/**
	 * Watch for changes to a specific file.  This must include the full path.  The handler receives the event kind that occurred with the file.<br/>
	 * Example:<br/>
	 * <code>
	 * DirectoryWatcher.watch("/path/to/file/filename.ext", k -> someMethod(k));
	 * </code>
	 * @param filename
	 * @param handler
	 */
	public static void watch(String filepath, Consumer<Kind<?>> handler) {
		String directory = Paths.get(filepath).getParent().toString();
		String filename = Paths.get(filepath).getFileName().toString();
		watch(directory, fileHandler(directory, filename, handler));
	}
	
	private static final Kind<?>[] FILE_EVENT_KINDS = { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE };
	
	private static WatcherEventHandler fileHandler(String directory, String filename, Consumer<Kind<?>> handler) {
		return new WatcherEventHandler() {
			String filepath = "%s/%s".formatted(directory, filename);
			FileTime lastModified = getLastModified();
			
			private FileTime getLastModified() {
				try { return Files.getLastModifiedTime(Paths.get(filepath), LinkOption.NOFOLLOW_LINKS); }
				catch (IOException e) { Log.logException(e, "[SYSTEM]"); return null; }
			}
			
			@Override
			public void handle(String name, Kind<?> eventKind) {
				if (filename.equals(name) && Data.in(eventKind, FILE_EVENT_KINDS)) {
					FileTime lastModified = getLastModified();
					if (!lastModified.equals(this.lastModified)) {
						Log.log("EVENT: ", "DirectoryWatcher handler called for file %s and event %s.".formatted(filepath, eventKind.toString()), "%s.fileHandler".formatted(DirectoryWatcher.class.getCanonicalName()), "[SYSTEM]", false, null);
						handler.accept(eventKind);
						this.lastModified = lastModified;
					}
				}
			}
	    };
	}
}
