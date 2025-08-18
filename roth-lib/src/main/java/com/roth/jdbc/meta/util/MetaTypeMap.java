package com.roth.jdbc.meta.util;

import java.util.ArrayList;

import com.roth.jdbc.meta.model.Column;
import com.roth.jdbc.meta.model.Type;

import jdk.jfr.Experimental;

@Experimental
public class MetaTypeMap {
	private ArrayList<MetaTypeMapEntry> refList;
	
	public MetaTypeMap() {
		refList = new ArrayList<>();
		
	}
	
	public void add(MetaTypeMapEntry ref) {
		refList.add(ref);
	}
	
	public Column getMetaColumn(String databaseType, Long size, Byte precision) {
		return null;
	}
	
	public Class<?> getJavaType(String databaseType, Long size, Byte precision) {
		return null;
	}
	
	public Class<?> getJavaType(Type metaType, Long size, Byte precision) {
		return null;
	}
	
	public String getDatabaseType(Type metaType, Long size, Byte precision) {
		return null;
	}
	
	/*
	
	Oracle
	
	MetaType     Size(min)  Size(max)  Precision(min)  Precision(max)  JavaClass   DatabaseType
	
	FIXED_STRING          1       4000                                 String         CHAR(s)
	VARIABLE_STRING       1       4000                                 String         VARCHAR2(s)
	VARIABLE_STRING    4001          ?                                 String         CLOB/CLOB(s)
	INTEGER               1          3                                 Byte           NUMBER(s)
	INTEGER               4          5                                 Short          NUMBER(s)
	INTEGER               6         10                                 Integer        NUMBER(s)
	INTEGER              11         19                                 Long           NUMBER(s)
	INTEGER              20         38                                 BigInteger     NUMBER(s)
	(Note for INTEGER.  NUMBER defined with no size defaults to NUMBER(38))
	DECIMAL                                         1               6  Float          NUBMER(s,p)
	DECIMAL                                         7              15  Double         NUBMER(s,p)                 
	DECIMAL                                        16               ?  BigDecimal     NUBMER(s,p)
	DATE                                                               LocalDate      n/a
	TIME                                                               LocalTime      n/a
	DATE_TIME                                                          LocalDateTime  DATE
	DATE_TIME                                       1               6  LocalDateTime  TIMESTAMP(p)    (if p omitted, defaults to 6)
	ZONED_DATE_TIME                                 1               6  ZonedDateTime  TIMESTAMP(p) WITH TIME ZONE (explicit time zone)
	LOCAL_DATE_TIME                                 1               6  ZonedDateTime  TIMESTAMP(p) WITH LOCAL TIME ZONE (relative time zone)
	BINARY                                                             byte[]         BLOB
	JSON                                                               String         VARCHAR2 or BLOB (with IS JSON check constraint)
	
	(Note" There is no date-only and no time-only type in Oracle)
	
	JSON Format: 
	
	[
		{ 
			metaType: "FIXED_STRING",
			sizeMin: 1,
			sizeMax: 4000,
			precisionMin: null,
			precisionMax: null,
			javaClass: "java.lang.String",
			databaseType: "CHAR(s)"
		},
		{ 
			metaType: "VARIABLE_STRING",
			sizeMin: 1,
			sizeMax: 4000,
			precisionMin: null,
			precisionMax: null,
			javaClass: "java.lang.String",
			databaseType: "VARCHAR2(s)"
		},
		...
	]
	
	CSV Format:
	# An empty first value means that this is a break; the second value is then the database name.
	,MySQL,,,,,
	FIXED_STRING,1,255,,,java.lang.String,CHAR(s)
	VARIABLE_STRING,1,65535,,,java.lang.String,VARCHAR(s)
	VARIABLE_STRING,65536,16777215,,,java.lang.String,MEDIUMTEXT(s)
	VARIABLE_STRING,16777216,4294967295,,,java.lang.String,LONGTEXT(s)
	INTEGER,1,3,,,Byte,TINYINT
	INTEGER,4,5,,,Short,SMALLINT
	INTEGER,6,10,,,Integer,INT
	INTEGER,11,19,,,Long,BIGINT
	DECIMAL,,,1,6,Float,DECIMAL(s,p)
	DECIMAL,,,7,15,Double,DECIMAL(s,p)  
	DATE,,,,,LocalDate,DATE               
	DATE_TIME,,,,,LocalDateTime,DATETIME
	DATE_TIME,,,1,6,LocalDateTime,TIMESTAMP[(p)]
	TIME,,,,,LocalTime,TIME
	TIME,,,1,6,LocalTime,TIME[(p)]
	ZONED_DATE_TIME,,,1,6,ZonedDateTime,TIMESTAMP(p) WITH TIME ZONE
	LOCAL_DATE_TIME,,,1,6,ZonedDateTime,TIMESTAMP(p) WITH LOCAL TIME ZONE
	BINARY,,,,,byte[],BLOB
	JSON,,,,,String,VARCHAR2 or BLOB (with IS JSON check constraint)
	
	
	,Oracle,,,,,
	FIXED_STRING,1,4000,,,java.lang.String,CHAR(s)
	VARIABLE_STRING,1,4000,,,java.lang.String,VARCHAR2(s)
	VARIABLE_STRING,4001,?,,,java.lang.String,CLOB[(s)]
	INTEGER,1,3,,,Byte,NUMBER(s)
	INTEGER,4,5,,,Short,NUMBER(s)
	INTEGER,6,10,,,Integer,NUMBER(s)
	INTEGER,11,19,,,Long,NUMBER(s)
	INTEGER,20,38,,,BigInteger,NUMBER(s)
	DECIMAL,,,1,6,Float,NUBMER(s,p)
	DECIMAL,,,7,15,Double,NUBMER(s,p)                 
	DECIMAL,,,16,?,BigDecimal,NUBMER(s,p)
	DATE_TIME,,,,,LocalDateTime,DATE
	DATE_TIME,,,1,6,LocalDateTime,TIMESTAMP[(p)]
	ZONED_DATE_TIME,,,1,6,ZonedDateTime,TIMESTAMP(p) WITH TIME ZONE
	LOCAL_DATE_TIME,,,1,6,ZonedDateTime,TIMESTAMP(p) WITH LOCAL TIME ZONE
	BINARY,,,,,byte[],BLOB
	JSON,,,,,String,VARCHAR2 or BLOB (with IS JSON check constraint)
	
	
	*/
}
