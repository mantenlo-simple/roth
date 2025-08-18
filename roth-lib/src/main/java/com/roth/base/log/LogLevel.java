package com.roth.base.log;

import java.util.Arrays;

public enum LogLevel {
	OFF (-1),
	EXCEPTION (0),
	ERROR (1),
	WARNING (2), 
	INFO (3),
	DEBUG (4);
	
	private int level;
	
	private LogLevel(int level) { this.level = level; }
	
	public int getLevel() { return level; }
	
	public static LogLevel ofLevel(int level) {
		return Arrays.stream(values()).filter(e -> { return e.getLevel() == level; }).findFirst().orElse(null);
	}
}
