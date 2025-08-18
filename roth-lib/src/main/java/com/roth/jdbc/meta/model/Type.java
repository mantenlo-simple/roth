package com.roth.jdbc.meta.model;

import jdk.jfr.Experimental;

@Experimental
public enum Type {
	FIXED_STRING,
	VARIABLE_STRING,
	INTEGER,
	DECIMAL,
	DATE,
	TIME,
	/**
	 * No time zone information
	 */
	DATE_TIME,
	/**
	 * Explicit time zone information
	 */
	ZONED_DATE_TIME,
	/**
	 * Relative (local to database session) time zone information
	 */
	LOCAL_DATE_TIME,
	BINARY,
	JSON;
	
	public static Type getFromString(String source) {
		switch (source.toLowerCase()) {
		case "blob": return BINARY;
		case "char": return FIXED_STRING;
		case "date": return DATE;
		case "datetime": return DATE_TIME;
		case "decimal": return DECIMAL;
		case "integer": return INTEGER; 
		case "json" : return JSON;
		case "local": return LOCAL_DATE_TIME;
		case "time": return TIME;
		case "varchar": return VARIABLE_STRING;
		case "zoned": return ZONED_DATE_TIME;
		default: return null;
		}
	}
}
