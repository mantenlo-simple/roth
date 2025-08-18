package com.roth.base.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.roth.base.log.Log;

public class NullStreamThread extends Thread {
    private InputStream input;
    private String type;
    
    NullStreamThread(InputStream input, String type) {
    	this.input = input;
    	this.type = type;
    }
    
    public void run() {
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    		String line = null;
    		while ((line = reader.readLine()) != null) {
    			Log.logDebug(type + " > " + line, null);
    		}
    	}
    	catch (IOException e) { Log.logException(e, null); }
    }
}
