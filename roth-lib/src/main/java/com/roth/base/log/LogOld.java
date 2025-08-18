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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import com.roth.base.util.Data;

public class LogOld {
	public static final int LOG_EXCEPTION = 0;
	public static final int LOG_ERROR = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_INFO = 3;
	public static final int LOG_DEBUG = 4;
	
	private static boolean logStackTrace = true; //false;
		
	private static String logCodes = "";
	private static int logLevel = LOG_EXCEPTION;
	
	private static String logFilename;
	private static String errFilename;
	
	public static final String[] LOG_LEVELS = {"EXCEPTION", "ERROR", "WARNING", "INFO", "DEBUG"};

	private LogOld() {}
	
	public static boolean getLogStackTrace() { return logStackTrace; }
	public static void setLogStackTrace(boolean logStackTrace) { LogOld.logStackTrace = logStackTrace; }
	
	public static String getLogCodes() { return logCodes; }
	public static void setLogCodes(String logCodes) { LogOld.logCodes = logCodes; } 
	
	public static int getLogLevel() { return logLevel; }
	public static void setLogLevel(int logLevel) { LogOld.logLevel = logLevel < LOG_EXCEPTION ? LOG_EXCEPTION : logLevel > LOG_DEBUG ? LOG_DEBUG : logLevel; }

	public static String getLogFilename() { return logFilename; }
	public static void setLogFilename(String logFilename) { LogOld.logFilename = logFilename; }
	
	public static String getErrFilename() { return errFilename; }
	public static void setErrFilename(String errFilename) { LogOld.errFilename = errFilename; }

	/**
	 * Do not use this method for general logging.
	 * @param value
	 */
	public static void print(String value) { logPrint(value); }
	/**
	 * Do not use this method for general logging.
	 * @param value
	 */
	public static void println(String value) { logPrintln(value); }
	
	private static synchronized void logPrint(String value) {
		if (logFilename != null)
			filePrint(value, logFilename);
		else
			System.out.print(value);
	}
	
	private static synchronized void logPrintln(String value) {
		logPrint(value + "\n");
	}
	
	private static synchronized void errPrint(String value) {
		if (errFilename != null || logFilename != null)
			filePrint(value, Data.nvl(errFilename, logFilename));
		else
			System.err.print(value);
	}
	
	private static synchronized void errPrintln(String value) {
		errPrint(value + "\n");
	}
	
	private static synchronized void filePrint(String value, String filename) {
		try (OutputStream os = new FileOutputStream(new File(filename), true)) {
			os.write(value.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Extract the stack trace from the supplied exception, and print to the configured output.
	 * @param exception
	 */
	private static void printStackTrace(Exception exception) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(outputStream)) {
			exception.printStackTrace(printStream);
	        print(outputStream.toString());
	    } catch (IOException e) {
			e.printStackTrace();
			exception.printStackTrace();
		}
	}
	
	/**
	 * Generic log function.  This is called by logException, logError, logWarning, logInfo, and logDebug.
	 * @param msgType the type of message to log (i.e. LOG_EXCEPTION, LOG_ERROR, etc.)
	 * @param msg the message to log
	 * @param loc the location from which the message was logged
	 */
	public static void log(String msgType, String msg, String origin, String userid, boolean error) {
		String dts = Data.dtsToStr(LocalDateTime.now());
		String message = String.format("%s (userid: %s) %s\"%s\" - %s", dts, Data.nvl(userid, "[not available]"), msgType, msg, origin);
		if (error)
			errPrintln(message);
		else
			logPrintln(message);
	}
	
	/**
	 * Get the estimated origin method of the exception.
	 * @param t
	 * @param e
	 * @return
	 */
	protected static String getOrigin(Thread t, Exception e) {
		StackTraceElement[] te = t.getStackTrace();
		if (e == null)
			return te[3].getClassName() + "." + te[3].getMethodName() + ":" + te[3].getLineNumber();
		else {
			StackTraceElement[] ee = e.getStackTrace();
			for (int i = 0; i < ee.length; i++)
				if (ee[i].getClassName().equals(te[3].getClassName()) &&
					ee[i].getMethodName().equals(te[3].getMethodName()))
					return ee[i].getClassName() + "." + ee[i].getMethodName() + ":" + ee[i].getLineNumber();
		}
		return null;
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
		if ((logCode != null) && (!("," + logCodes + ",").contains("," + logCode + ",")))
			return;
		
		String origin = getOrigin(Thread.currentThread(), e);
		String msgType = "EXCEPTION: " + e.getClass().getCanonicalName();
		log(msgType, (msg == null ? "" : msg + " | ") + e.getMessage(), origin, userid, true);
		
		if (logStackTrace)
			printStackTrace(e);
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
		if (logLevel < LOG_ERROR) 
			return;
		if ((logCode != null) && (!("," + logCodes + ",").contains("," + logCode + ",")))
			return;
		
		String origin = getOrigin(Thread.currentThread(), e);
		String msgType = "ERROR: " + ((e == null) ? "" : e.getClass().getCanonicalName());
		log(msgType, msg == null ? "" : msg, origin, userid, true);
		
		if (logStackTrace && (e != null))
			printStackTrace(e);
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
		if (logLevel < LOG_WARNING) 
			return;
		if ((logCode != null) && (!("," + logCodes + ",").contains("," + logCode + ",")))
			return;
		
		String origin = getOrigin(Thread.currentThread(), null);
		String msgType = "WARNING: ";
		log(msgType, msg, origin, userid, false);
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
		if (logLevel < LOG_INFO) 
			return;
		if ((logCode != null) && (!("," + logCodes + ",").contains("," + logCode + ",")))
			return;
		
		String origin = getOrigin(Thread.currentThread(), null);
		String msgType = "INFO: ";
		log(msgType, msg, origin, userid, false);
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
		if (logLevel < LOG_DEBUG)
			return;
		if ((logCode != null) && (!("," + logCodes + ",").contains("," + logCode + ",")))
			return;
		
		String origin = getOrigin(Thread.currentThread(), null);
		String msgType = "DEBUG: ";
		log(msgType, msg, origin, userid, false);
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
		if (logLevel < LOG_DEBUG)
			return;
		if ((logCode != null) && (!("," + logCodes + ",").contains("," + logCode + ",")))
			return;
		String origin = getOrigin(Thread.currentThread(), null);
		String msgType = "DEBUG (OBJECT): ";
		if (obj == null)
			log(msgType, "null", origin, userid, false);
		else {
			Method[] methods = obj.getClass().getMethods();
			String message = "Instance of: " + obj.getClass().getCanonicalName();
			for (Method method : methods) {
				if (!method.getName().startsWith("get") || method.getParameterCount() > 0)
					continue;
				try {
					Object mOut = method.invoke(obj);
					message += "\n    " + method.getName() + " = " + Data.objToStrNvl(mOut, "null");
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					logException(e, userid, logCode);
				}
			}
			log(msgType, message, origin, userid, false);
		}
	}
}
