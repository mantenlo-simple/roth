/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.roth.base.log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import com.roth.base.util.Data;
import com.roth.export.util.JsonUtil;
import com.roth.file.util.DirectoryWatcher;

public class Log {
	private static final String CONF_FOLDER =  "%s/conf".formatted(System.getProperty("catalina.base"));
	private static final String CONF_FILENAME = "%s/rothlog.json".formatted(CONF_FOLDER);
	
	private static LogHub hub;
	static {
		refreshConfig();	
		DirectoryWatcher.watch(CONF_FILENAME, k -> Log.refreshConfig());
	}
	
	public static Boolean refreshConfig() {
		initDefaultConfig();
		// Check for config file.
		// - If the file exists, then initialize hub using the config file.
		// - Otherwise default the hub with a single console logger.
		if (new File(CONF_FILENAME).exists()) {
			try {
				String config = Data.readTextFile(CONF_FILENAME, Charset.forName("UTF-8"));
				hub = JsonUtil.jsonToObj(config, LogHub.class);
				hub.checkConfig();
				log("INIT: ", "Log configuration loaded from config file.%n%s%n%s".formatted(CONF_FILENAME, config), "roth.log.Log.refreshConfig", "[SYSTEM]", false, null);
			} catch (Exception e) {
				logException(e, "[SYSTEM]"); // This comes second because hub needs to be initialized to log this exception.
			}
		}
		return true;
	}
	
	private static void initDefaultConfig() {
		hub = new LogHub();
		hub.getLoggers().add(new Logger(hub));
		log("INIT: ", "Log configuration set to default.", "roth.log.Log.initDefaultConfig", "[SYSTEM]", false, null);
	}
	
	/**
	 * This class cannot, should not be instantiated.
	 */
	private Log() {}
	
	public static LogLevel getLogLevel() { return hub.getLogLevel(getOrigin(Thread.currentThread(), null)); }
	public static void setLogLevel(LogLevel logLevel) { hub.setLogLevel(getOrigin(Thread.currentThread(), null), logLevel); }
	
	public static Boolean getLogStackTrace() { return hub.getLogStackTrace(getOrigin(Thread.currentThread(), null)); }
	public static void setLogStackTrace(Boolean logStackTrace) { hub.setLogStackTrace(getOrigin(Thread.currentThread(), null), logStackTrace); }
	
	public static List<String> getLogCodes() { return hub.getLogCodes(getOrigin(Thread.currentThread(), null)); }
	public static void setLogCodes(List<String> logCodes) { hub.setLogCodes(getOrigin(Thread.currentThread(), null), logCodes); }
	
	public static String getConfig(String userid) {
		try {
			return JsonUtil.objToJson(hub);
		} catch (InvocationTargetException | IllegalAccessException e) {
			logException(e, userid);
			return null;
		}
	}
	public static void setConfig(String config, String userid) {
		if (hub == null) {
			hub = new LogHub();
		}
		try {
			hub = JsonUtil.jsonToObj(config, LogHub.class);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			try {
				Data.writeTextFile("/development/roth-log-stacktrace.txt", Logger.parseStackTrace(e));
			} catch (IOException e1) {
				// might as well give up at this point...
			}
			logException(e, userid);
		}
	}
	
	public static LogHub getHub() { return hub; }
	
	private static String stackRef(StackTraceElement e) {
		return String.format("%s.%s:%d", e.getClassName(), e.getMethodName(), e.getLineNumber());
	}
	
	private static boolean refEquals(StackTraceElement a, StackTraceElement b) {
		return a.getClassName().equals(b.getClassName()) && a.getMethodName().equals(b.getMethodName());
	}
	
	/**
	 * Get the estimated origin method of the exception.
	 * @param t
	 * @param e
	 * @return
	 */
	private static String getOrigin(Thread t, Exception e) {
		StackTraceElement[] te = t.getStackTrace();
		return e == null ? stackRef(te[3]) : Arrays.stream(e.getStackTrace()).filter(se -> refEquals(se, te[3])).findFirst().map(se -> stackRef(se)).orElse(null);
	}
	
	public static void print(String value) {
		hub.print(value, false, getOrigin(Thread.currentThread(), null));
	}
	
	public static void print(String value, boolean error) {
		hub.print(value, error, getOrigin(Thread.currentThread(), null));
	}

	public static void println(String value) {
		hub.println(value, false, getOrigin(Thread.currentThread(), null));
	}

	public static void println(String value, boolean error) {
		hub.println(value, error, getOrigin(Thread.currentThread(), null));
	}
	
	public static void log(String msgType, String msg, String origin, String userid, boolean error, Throwable t) {
		hub.log(msgType, msg, origin, userid, error, t);
	}
	
	/**
	 * Log an exception message with an ancillary message.  If this class's logStackTrace 
	 * is true, then that exceptions stack trace will also be printed.
	 * @param msg
	 * @param userid
	 * @param e
	 */
	public static void logException(String msg, String userid, Exception e) {
		logException(msg, userid, e, null);
	}
	
	/**
	 * Log an exception message.  If this class's logStackTrace is true, then that 
	 * exceptions stack trace will also be printed.
	 * @param e
	 * @param userid
	 */
	public static void logException(Exception e, String userid) {
		logException(null, userid, e, null);
	}
	
	/**
	 * Log an exception message.  If this class's logStackTrace is true, then that 
	 * exceptions stack trace will also be printed.
	 * If a logCode is provided, then this will only log when the logCode is included in 
	 * this class's logCodes.
	 * @param e
	 * @param userid
	 * @param logCode
	 */
	public static void logException(Exception e, String userid, String logCode) {
		logException(null, userid, e, logCode);
	}
	
	/**
	 * Log an exception message with an ancillary message.  If this class's logStackTrace 
	 * is true, then that exceptions stack trace will also be printed.
	 * If a logCode is provided, then this will only log when the logCode is included in 
	 * this class's logCodes.
	 * @param msg
	 * @param userid
	 * @param e
	 * @param logCode
	 */
	public static void logException(String msg, String userid, Exception e, String logCode) {
		hub.logException(msg, userid, e, logCode, getOrigin(Thread.currentThread(), e));
	}
	
	/**
	 * Log an error message.  If an exception is provided, and this class's logStackTrace 
	 * is true, then that exceptions stack trace will also be printed.
	 * @param msg
	 * @param userid
	 * @param e
	 */
	public static void logError(String msg, String userid, Exception e) {
		logError(msg, userid, e, null);
	}
	
	/**
	 * Log an error message.  If an exception is provided, and this class's logStackTrace 
	 * is true, then that exceptions stack trace will also be printed.
	 * If a logCode is provided, then this will only log when the logCode is included in 
	 * this class's logCodes.
	 * @param msg
	 * @param userid
	 * @param e
	 * @param logCode
	 */
	public static void logError(String msg, String userid, Exception e, String logCode) {
		hub.logError(msg, userid, e, logCode, getOrigin(Thread.currentThread(), e));
	}
	
	/**
	 * Log a warning message.
	 * @param msg
	 * @param userid
	 */
	public static void logWarning(String msg, String userid) {
		logWarning(msg, userid, null);
	}
	
	/**
	 * Log a warning message.
	 * If a logCode is provided, then this will only log when the logCode is included in 
	 * this class's logCodes.
	 * @param msg
	 * @param userid
	 * @param logCode
	 */
	public static void logWarning(String msg, String userid, String logCode) {
		hub.logWarning(msg, userid, logCode, getOrigin(Thread.currentThread(), null));
	}
	
	/**
	 * Log an info message.
	 * @param msg
	 * @param userid
	 */
	public static void logInfo(String msg, String userid) {
		logInfo(msg, userid, null);
	}
	
	/**
	 * Log an info message.
	 * If a logCode is provided, then this will only log when the logCode is included in 
	 * this class's logCodes.
	 * @param msg
	 * @param userid
	 * @param logCode
	 */
	public static void logInfo(String msg, String userid, String logCode) {
		hub.logInfo(msg, userid, logCode, getOrigin(Thread.currentThread(), null));
	}
	
	/**
	 * Log a debug message.
	 * @param msg
	 * @param userid
	 */
	public static void logDebug(String msg, String userid) {
		logDebug(msg, userid, null);
	}
	
	/**
	 * Log a debug message.
	 * If a logCode is provided, then this will only log when the logCode is included in 
	 * this class's logCodes.
	 * @param msg
	 * @param userid
	 * @param logCode
	 */
	public static void logDebug(String msg, String userid, String logCode) {
		hub.logDebug(msg, userid, logCode, getOrigin(Thread.currentThread(), null));
	}
		
	/**
	 * Log the output of all public getters in an object.  This is a variation of logDebug. 
	 * @param obj
	 * @param userid
	 */
	public static void logObject(Object obj, String userid) {
		logObject(obj, userid, null);
	}
	
	/**
	 * Log the output of all public getters in an object.  This is a variation of logDebug.
	 * If a logCode is provided, then this will only log when the logCode is included in 
	 * this class's logCodes.
	 * @param obj
	 * @param userid
	 * @param logCode
	 */
	public static void logObject(Object obj, String userid, String logCode) {
		hub.logObject(obj, userid, logCode, getOrigin(Thread.currentThread(), null));
	}
}
