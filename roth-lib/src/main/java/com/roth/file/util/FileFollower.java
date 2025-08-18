package com.roth.file.util;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.base.util.RandomReadString;

public class FileFollower implements Callable<Boolean> {
	private String filepath;
	private FollowerEventHandler handler;
	
	private boolean following = true;
	private long offset = 0;
	
	private FileFollower(String filepath, FollowerEventHandler handler) {
		this.filepath = filepath;
		this.handler = handler;
	}
    
	
	
	
    public static FileFollower follow(String filepath, FollowerEventHandler handler) {
    	ExecutorService executor = Executors.newSingleThreadExecutor();
    	FileFollower follower = new FileFollower(filepath, handler);
		executor.submit(follower);
		return follower;
    }
    
    public void cancel() {
    	following = false;
    }




    /**
	 * This is not intended for direct use, though good luck trying.  Please use the static follow method.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Boolean call() throws IOException, InterruptedException {
		following = true;
		try {
			RandomReadString text = Data.readTextFile(filepath, offset);
			offset = text.newOffset();
	    	handler.handle(text.value());
	    	DirectoryWatcher.watch(filepath, h -> {
	    		try {
	    			RandomReadString update = Data.readTextFile(filepath, offset);
		    		offset = update.newOffset();
		    		handler.handle(update.value());	
				} catch (IOException e) {
					Log.logException(e, null);
				}
	    	});
		} catch (IOException e) {
			Log.logException(e, null);
		}
		while(following) {
			// do nothing.
		}
		return true;
	}
}
