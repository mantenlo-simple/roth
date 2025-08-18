package com.roth.base.log;

public class LogFilter {
	private LogLevel logLevel = LogLevel.EXCEPTION;
	private String filter;
	
	public LogLevel getLogLevel() { return logLevel; }
	public void setLogLevel(LogLevel logLevel) { this.logLevel = logLevel; }
	
	public String getFilter() { return filter; }
	public void setFilter(String filter) { this.filter = filter; }
}
