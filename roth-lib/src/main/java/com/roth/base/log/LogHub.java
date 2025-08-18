package com.roth.base.log;

import java.util.ArrayList;
import java.util.List;

import com.roth.base.annotation.Ignore;
import com.roth.base.util.Data;
import com.roth.export.annotation.JsonCollection;

public class LogHub {
	private List<String> logCodes = new ArrayList<>();
	private LogLevel logLevel = LogLevel.EXCEPTION;
	private boolean logStackTrace = false;
	private List<Logger> loggers = new ArrayList<>();
	
	public List<String> getLogCodes() { return logCodes; }
	@JsonCollection(elementClass = String.class)
	public void setLogCodes(List<String> logCodes) { this.logCodes = logCodes; }
	@Ignore
	public List<String> getLogCodes(String origin) { return findLogger(origin).getLogCodes(); }
	@Ignore
	public void setLogCodes(String origin, List<String> logCodes) { findLogger(origin).setLogCodes(logCodes); }
	
	public LogLevel getLogLevel() { return logLevel; }
	public void setLogLevel(LogLevel logLevel) { this.logLevel = logLevel; }
	@Ignore
	public LogLevel getLogLevel(String origin) { return findLogger(origin).getLogLevel(); };
	@Ignore
	public void setLogLevel(String origin, LogLevel logLevel) { findLogger(origin).setLogLevel(logLevel); }
	
	public boolean getLogStackTrace() { return logStackTrace; }
	public void setLogStackTrace(boolean logStackTrace) { this.logStackTrace = logStackTrace; }
	@Ignore
	public boolean getLogStackTrace(String origin) { return findLogger(origin).getLogStackTrace(); }
	@Ignore
	public void setLogStackTrace(String origin, boolean logStackTrace) { findLogger(origin).setLogStackTrace(logStackTrace); }
	
	public List<Logger> getLoggers() { return loggers; }
	@JsonCollection(elementClass = Logger.class)
	public void setLoggers(List<Logger> loggers) { this.loggers = loggers; }
	
	private Logger findLogger(String origin) {
		if (loggers.isEmpty())
			loggers.add(new Logger(this));
		return loggers.stream().filter(l -> l.isQualified(origin)).findFirst().get();
	}
	
	/**
	 * Check the configuration and ensure a global logger is available.  If none
	 * was configured, then add one.  If more than one exists, log an exception
	 * (on the first one, duh).
	 */
	void checkConfig() {
		int globalLoggers = 0;
		for (Logger logger : loggers)
			if (Data.isEmpty(logger.getLogFilters()))
				globalLoggers++;
		if (globalLoggers < 1)
			loggers.add(new Logger(this));
		else if (globalLoggers > 1)
			throw new IllegalStateException("The configuration file rothlog.json describes more than one global logger.  All but the first will be ignored.");
	}
	
	void print(String value, boolean error, String origin) {
		findLogger(origin).print(value, error);
	}
	
	void println(String value, boolean error, String origin) {
		findLogger(origin).println(value, error);
	}
	
	public void log(String msgType, String msg, String origin, String userid, boolean error, Throwable t) {
		findLogger(origin).log(msgType, msg, origin, userid, error, t);
	}
	
	public void logException(String msg, String userid, Exception e, String logCode, String origin) {
		findLogger(origin).logException(msg, userid, e, logCode, origin);
	}
	
	public void logError(String msg, String userid, Exception e, String logCode, String origin) {
		findLogger(origin).logError(msg, userid, e, logCode, origin);
	}
	
	public void logWarning(String msg, String userid, String logCode, String origin) {
		findLogger(origin).logWarning(msg, userid, logCode, origin);
	}
	
	public void logInfo(String msg, String userid, String logCode, String origin) {
		findLogger(origin).logInfo(msg, userid, logCode, origin);
	}
	
	public void logDebug(String msg, String userid, String logCode, String origin) {
		findLogger(origin).logDebug(msg, userid, logCode, origin);
	}
	
	public void logObject(Object obj, String userid, String logCode, String origin) {
		findLogger(origin).logObject(obj, userid, logCode, origin);
	}
}
