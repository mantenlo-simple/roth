package com.roth.base.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.roth.base.util.Data;

public class LoggerOld {
	
	private List<String> logCodes = new ArrayList<>();
	private LogLevel logLevel = LogLevel.EXCEPTION;
	private List<LogFilter> logFilters = new ArrayList<>();
	private boolean logStackTrace = false;
	
	private String logFilename;
	private String errFilename;
	
	public LoggerOld() {}
	
	public LoggerOld(String logFilename, String errFilename) {
		this.logFilename = logFilename;
		this.errFilename = errFilename;
	}
	
	public LoggerOld(LogHub hub) {
		logCodes = hub.getLogCodes();
		logLevel = hub.getLogLevel();
	}

	public List<String> getLogCodes() { return logCodes; }
	public synchronized void setLogCodes(List<String> logCodes) { this.logCodes = logCodes; }

	public LogLevel getLogLevel() { return logLevel; }
	public synchronized void setLogLevel(LogLevel logLevel) { this.logLevel = logLevel; }
	
	public List<LogFilter> getLogFilters() { return logFilters; }
	public synchronized void setLogFilters(List<LogFilter> logFilters) { this.logFilters = logFilters; }

	public boolean isLogStackTrace() { return logStackTrace; }
	public synchronized void setLogStackTrace(boolean logStackTrace) { this.logStackTrace = logStackTrace; }

	public String getLogFilename() { return logFilename; }
	public synchronized void setLogFilename(String logFilename) { this.logFilename = logFilename; }

	public String getErrFilename() { return errFilename; }
	public synchronized void setErrFilename(String errFilename) { this.errFilename = errFilename; }

	
	public boolean isQualified(String origin) {
		if (logFilters == null)
			return true;
		return logFilters.stream().filter(f -> origin.startsWith(f.getFilter())).findFirst().map(f -> true).orElse(false);
	}
	
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
	protected static String getOrigin(Thread t, Exception e) {
		StackTraceElement[] te = t.getStackTrace();
		return e == null ? stackRef(te[3]) : Arrays.stream(e.getStackTrace()).filter(se -> refEquals(se, te[3])).findFirst().map(se -> stackRef(se)).orElse(null);
	}
	
	/*
	private static String parseStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
	*/
	
	private static String parseStackTrace(Throwable t) {
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
			t.printStackTrace(pw);
			return sw.toString();
		} 
		catch (IOException e) {
			return String.format("Logger.parseStackTrace failed with error: %s", e.getMessage());
		}
	}
	
	private void printStackTrace(Throwable t) {
		println(parseStackTrace(t), logFilename, errFilename, true);
	}
	
	private static synchronized void filePrint(String value, String filename) {
		try (OutputStream os = new FileOutputStream(new File(filename), true)) {
			os.write(value.getBytes());
			os.flush();
		} catch (IOException e) {
			System.err.println(parseStackTrace(e));
		}
	}
	
	private static synchronized void print(String value, String logFilename, String errFilename, boolean error) {
		String filename = error ? Data.nvl(errFilename, logFilename, "") : logFilename;
		if (!Data.isEmpty(filename))
			filePrint(value, filename);
		else if (error)
			System.err.print(value);
		else
			System.out.print(value);
	}
	private static synchronized void println(String value, String logFilename, String errFilename, boolean error) { 
		print(String.format("%s\n", value), logFilename, errFilename, error); 
	}
	
	/**
	 * Generic log function.  This is called by logException, logError, logWarning, logInfo, and logDebug.
	 * @param msgType the type of message to log (i.e. LOG_EXCEPTION, LOG_ERROR, etc.)
	 * @param msg the message to log
	 * @param loc the location from which the message was logged
	 */
	public void log(String msgType, String msg, String origin, String userid, boolean error) {
		String dts = Data.dtsToStr(LocalDateTime.now());
		String message = String.format("%s (userid: %s) %s\"%s\" - %s", dts, Data.nvl(userid, "[not available]"), msgType, msg, origin);
		println(message, logFilename, errFilename, error);
	}
	
	/**
	 * Log an exception message with an ancillary message.  If this class's logStackTrace 
	 * is true, then that exceptions stack trace will also be printed.
	 * @param msg
	 * @param e
	 */
	public void logException(String msg, Exception e) {
		logException(msg, null, e, null);
	}
	
	/**
	 * Log an exception message with an ancillary message.  If this class's logStackTrace 
	 * is true, then that exceptions stack trace will also be printed.
	 * @param msg
	 * @param userid
	 * @param e
	 */
	public void logException(String msg, String userid, Exception e) {
		logException(msg, userid, e, null);
	}
	
	/**
	 * Log an exception message.  If this class's logStackTrace is true, then that 
	 * exceptions stack trace will also be printed.
	 * @param e
	 */
	public void logException(Exception e) {
		logException(null, null, e, null);
	}
	
	/**
	 * Log an exception message.  If this class's logStackTrace is true, then that 
	 * exceptions stack trace will also be printed.
	 * @param e
	 * @param userid
	 */
	public void logException(Exception e, String userid) {
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
	public void logException(Exception e, String userid, String logCode) {
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
	public void logException(String msg, String userid, Exception e, String logCode) {
		if ((logCode != null) && (!Data.in(logCode, logCodes)))
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
	 * @param e
	 */
	public void logError(String msg, Exception e) {
		logError(msg, null, e, null);
	}
	
	/**
	 * Log an error message.  If an exception is provided, and this class's logStackTrace 
	 * is true, then that exceptions stack trace will also be printed.
	 * @param msg
	 * @param userid
	 * @param e
	 */
	public void logError(String msg, String userid, Exception e) {
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
	public void logError(String msg, String userid, Exception e, String logCode) {
		if (logLevel.getLevel() < LogLevel.ERROR.getLevel()) 
			return;
		if ((logCode != null) && (!Data.in(logCode, logCodes)))
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
	 */
	public void logWarning(String msg) {
		logWarning(msg, null, null);
	}
	
	/**
	 * Log a warning message.
	 * @param msg
	 * @param userid
	 */
	public void logWarning(String msg, String userid) {
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
	public void logWarning(String msg, String userid, String logCode) {
		if (logLevel.getLevel() < LogLevel.WARNING.getLevel()) 
			return;
		if ((logCode != null) && (!Data.in(logCode, logCodes)))
			return;
		
		String origin = getOrigin(Thread.currentThread(), null);
		String msgType = "WARNING: ";
		log(msgType, msg, origin, userid, false);
	}
	
	/**
	 * Log an info message.
	 * @param msg
	 */
	public void logInfo(String msg) {
		logInfo(msg, null, null);
	}
	
	/**
	 * Log an info message.
	 * @param msg
	 * @param userid
	 */
	public void logInfo(String msg, String userid) {
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
	public void logInfo(String msg, String userid, String logCode) {
		if (logLevel.getLevel() < LogLevel.INFO.getLevel()) 
			return;
		if ((logCode != null) && (!Data.in(logCode, logCodes)))
			return;
		
		String origin = getOrigin(Thread.currentThread(), null);
		String msgType = "INFO: ";
		log(msgType, msg, origin, userid, false);
	}

	/**
	 * Log a debug message.
	 * @param msg
	 */
	public void logDebug(String msg) {
		logDebug(msg, null, null);
	}
	
	/**
	 * Log a debug message.
	 * @param msg
	 * @param userid
	 */
	public void logDebug(String msg, String userid) {
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
	public void logDebug(String msg, String userid, String logCode) {
		if (logLevel.getLevel() < LogLevel.DEBUG.getLevel())
			return;
		if ((logCode != null) && (!Data.in(logCode, logCodes)))
			return;
		
		String origin = getOrigin(Thread.currentThread(), null);
		String msgType = "DEBUG: ";
		log(msgType, msg, origin, userid, false);
	}
	
	/**
	 * Log the output of all public getters in an object.  This is a variation of logDebug. 
	 * @param obj
	 */
	public void logObject(Object obj) {
		logObject(obj, null, null);
	}
		
	/**
	 * Log the output of all public getters in an object.  This is a variation of logDebug. 
	 * @param obj
	 * @param userid
	 */
	public void logObject(Object obj, String userid) {
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
	public void logObject(Object obj, String userid, String logCode) {
		if (logLevel.getLevel() < LogLevel.DEBUG.getLevel())
			return;
		if ((logCode != null) && (!Data.in(logCode, logCodes)))
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
