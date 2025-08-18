package com.roth.jdbc.meta.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.roth.base.util.Data;
import com.roth.jdbc.util.DbmsType;

import jdk.jfr.Experimental;

@Experimental
public class MetaColumnBackup {
	private DbmsType dbName;
	//private String defaultValue;
	private boolean fixed;
	private boolean json;
	private boolean oracle122;
	private Integer precision;
	private boolean required;
	private Integer size;
	private Class<?> type;
	
	public MetaColumnBackup(DbmsType dbName) { 
		this.dbName = dbName; 
	}
	
	/**
	 * Get whether to use Oracle 12.2 VARCHAR2 max length.
	 * @return
	 */
	public boolean isOracle122() { return oracle122; }
	
	/**
	 * Set whether to use Oracle 12.2 VARCHAR2 max length.
	 * @param oracle122
	 */
	public void setOracle122(boolean oracle122) { this.oracle122 = oracle122; }

	/**
	 * Get whether the column is required (i.e. can it be null?).
	 * @param required
	 */
	public void setRequired(boolean required) { this.required = required; }
	
	/**
	 * Set whether the column is required (i.e. can it be null?).
	 * @return
	 */
	public boolean isRequired() { return required; }
	
	/**
	 * Parse a string representing a database column type (e.g. "CHAR(1) NOT NULL DEFAULT 'N'").
	 * @param type
	 */
	public void parseType(String source) {
		String compType = source.toUpperCase();
		fixed = compType.startsWith("CHAR");
		json = compType.startsWith("JSON") || compType.startsWith("NVARCHAR");
		required = compType.contains("NOT NULL");
		if (source.contains("(") ) {
			String ps = source.substring(source.indexOf('(') + 1, source.indexOf(')'));
			String[] psv = ps.split(",");
			size = Data.strToInteger(psv[0].trim());
			if (psv.length > 1)
				precision = Data.strToInteger(psv[1].trim());
		}
		if (compType.startsWith("CHAR") || compType.startsWith("VARCHAR") || compType.startsWith("LVARCHAR") || 
			compType.startsWith("CLOB") || compType.startsWith("NCLOB") || compType.startsWith("NVARCHAR") || 
			compType.startsWith("JSON"))
			type = String.class;
		else if (compType.startsWith("TINYINT") || (compType.startsWith("NUMBER") && size <= 3))
			type = Byte.class;
		else if (compType.startsWith("SMALLINT") || (compType.startsWith("NUMBER") && size <= 5))
			type = Short.class;
		else if (compType.startsWith("INT") || (compType.startsWith("NUMBER") && size <= 10))
			type = Integer.class;
		else if (compType.startsWith("BIGINT") || (compType.startsWith("NUMBER") && size <= 19))
			type = Long.class;
		else if (compType.startsWith("NUMBER") && size == null)
			type = BigInteger.class;
		else if ((compType.startsWith("DECIMAL") || compType.startsWith("NUM")) && precision <= 7)
			type = Float.class;
		else if ((compType.startsWith("DECIMAL") || compType.startsWith("NUM")) && precision <= 15)
			type = Double.class;
		else if ((compType.startsWith("DECIMAL") || compType.startsWith("NUM")) && precision > 15)
			type = BigDecimal.class;
		else if (compType.startsWith("DATETIME") || compType.startsWith("TIMESTAMP") || (compType.startsWith("DATE") && dbName == DbmsType.ORACLE))
			type = LocalDateTime.class;
		else if (compType.startsWith("DATE"))
			type = LocalDate.class;
		else if (compType.startsWith("TIME"))
			type = LocalTime.class;
		else if (compType.startsWith("BLOB") || compType.startsWith("BYTEA") || compType.startsWith("FILESTREAM"))
			type = byte[].class;
	}
	
	/**
	 * Format to a string representing a database column type (e.g. "CHAR(1) NOT NULL DEFAULT 'N'").
	 * @return
	 */
	public String formatType() {
		String result = "";
		if (type == String.class)
			result = formatStringType();
		else if (type == Byte.class)
			result = null; //getIntegerType();
		return result + (required ? " NOT NULL" : "");
	}
	
	private String formatStringType() {
		if (dbName == DbmsType.DB2)
			return formatDB2StringType();
		else if (dbName == DbmsType.INFORMIX)
			return formatInformixStringType();
		else if (dbName == DbmsType.MYSQL)
			return formatMySQLStringType();
		else if (dbName == DbmsType.ORACLE)
			return formatOracleStringType();
		else if (dbName == DbmsType.POSTGRESQL)
			return formatPostgreSQLStringType();
		else if (dbName == DbmsType.SQL_SERVER)
			return formatSQLServerStringType();
		else
			return null;
	}
	
	private String formatDB2StringType() {
		String result = fixed ? "CHAR" : size == null || size > 32_740 ? "CLOB" : "VARCHAR";
		if (size != null)
			result += "(" + size + ")";
		return result;
	}
	
	private String formatInformixStringType() {
		String result = fixed ? "CHAR" : size == null || size > 32_739 ? "CLOB" :  size != null && size > 255 ? "LVARCHAR" : "VARCHAR";
		if (size != null)
			result += "(" + size + ")";
		return result;
	}
	
	private String formatMySQLStringType() {
		String result = json ? "JSON" : fixed ? "CHAR" : size == null || size > 65_535 ? "CLOB" : "VARCHAR";
		if (size != null)
			result += "(" + size + ")";
		return result;
	}
	
	private String formatOracleStringType() {
		String result = fixed ? "CHAR" : size == null || size > (oracle122 ? 32_768 : 4_000) ? "NCLOB" : "VARCHAR2";
		if (size != null)
			result += "(" + size + ")";
		return result;
	}
	
	private String formatPostgreSQLStringType() {
		String result = json ? "JSON" : fixed ? "CHAR" : "VARCHAR";
		if (size != null)
			result += "(" + size + ")";
		return result;
	}
	
	private String formatSQLServerStringType() {
		String result = fixed ? "CHAR" : size == null || size > 8_000 ? "CLOB" : "VARCHAR";
		if (size != null)
			result += "(" + size + ")";
		return result;
	}


	
	
	
	/*
	  
	Common Attributes: NULL / NOT NULL
	
	String
	    type: CHAR, VARCHAR, VARCHAR2, or CLOB
	    attributes: SIZE (i.e. max length)
	    
	Integer
	    type: NUMBER, TINYINT, SMALLINT, INT, BIGINT
	    attributes: DIGITS (i.e. max digits)    
	
	Decimal
	    type: NUMBER, DECIMAL, NUMERIC
	    attributes: DIGITS (i.e. max digits left of decimal), PRECISION (i.e. max digits right of decimal)
	    
	Date
	    type: DATE
	    attributes: FSP (i.e. fraction of second precision)
	    
	DateTime
		type: DATE, DATETIME, DATETIME YEAR TO FRACTION, TIMESTAMP
		attributes: FSP (i.e. fraction of second precision)
		
	Time
		type: TIME, DATE, DATETIME HOUR TO FRACTION , TIMESTAMP
		attributes: FSP (i.e. fraction of second precision)
	    
	 
	 */
	
	
}
