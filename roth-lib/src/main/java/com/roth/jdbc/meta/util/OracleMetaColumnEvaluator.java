package com.roth.jdbc.meta.util;

import com.roth.jdbc.meta.model.Column;

import jdk.jfr.Experimental;

@Experimental
public class OracleMetaColumnEvaluator implements MetaColumnEvaluator {

	@Override
	public String format(Column meta) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Column parse(String fieldDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/*
	
	What do we need:
	
	Convert database-specific data type to MetaColumn
	
	
	
	
	Convert MetaColumn to database-specific data type
	
	
	MetaTypes: 
	       Fixed-String
	       Variable-String
	       Integer
	       Decimal
	       Date-Time
	       Binary
	       Json
	       
	       
	String dbType;
	MetaType metaType;
	Boolean required;   // NULL (false) / NOT NULL (true)
	Long size;          // 
	Integer precision;  // decimal places
	
	
	
		String --> CHAR, VARCHAR2, CLOB
		Byte, Short, Integer, Long, BigInteger --> NUMBER 
		Float, Double, BigDecimal --> NUMBER
		LocalDate, LocalDateTime, LocalTime --> DATE
		Byte[] --> BLOB
	
	*/
}
