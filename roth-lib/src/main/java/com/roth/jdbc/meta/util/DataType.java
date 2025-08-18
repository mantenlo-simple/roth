package com.roth.jdbc.meta.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import com.roth.base.util.Data;
import com.roth.jdbc.util.DbmsType;

public enum DataType {
	TINYINT (Byte.class, null),
	SMALLINT (Short.class, new Class<?>[] { Byte.class }),
	INT (Integer.class, new Class<?>[] { Byte.class, Short.class }),
	BIGINT (Long.class, new Class<?>[] { Byte.class, Short.class, Integer.class }),
	DECIMAL (Double.class, new Class<?>[] { Double.class }),
	CHAR (String.class, null),
	VARCHAR (String.class, null),
	LONGVARCHAR (String.class, null),
	BLOB (byte[].class, null),
	DATE (LocalDate.class, new Class<?>[] { java.sql.Date.class, java.util.Date.class }),
	DATETIME (LocalDateTime.class, new Class<?>[] { java.sql.Timestamp.class, java.util.Date.class }),
	TIME (LocalTime.class, new Class<?>[] { java.sql.Time.class });
	
	Class<?> typeClass;
	Class<?>[] compatibility;
	
	private DataType(Class<?> typeClass, Class<?>[] compatibility) {
		this.typeClass = typeClass;
		this.compatibility = compatibility;
	}
	
	public Class<?> getTypeClass() { return typeClass; }
	
	public boolean isCompatible(Class<?> testClass) {
		return typeClass.equals(testClass) || (compatibility != null && Data.in(testClass, compatibility));
	}
	
	/**
	 * Find the appropriate DataType value for an integer type by precision.
	 * @param precision - maximum number of digits
	 * @return
	 */
	public DataType findIntegerType(Integer precision) {
		if (precision > 19)
			throw new IllegalArgumentException("Integer types may not exceed 19 digits for cross compatibility.");
		return precision < 4 ? TINYINT
			 : precision < 5 ? SMALLINT
			 : precision < 10 ? INT
			 : BIGINT;
	}
	
	/**
	 * Find the appropriate DataType value for a decimal type by precision and scale.
	 * @param precision
	 * @param scale
	 * @return
	 */
	public DataType findDecimalType(Integer precision, Integer scale) {
		if (precision > 39 || scale > 15)
			throw new IllegalArgumentException("Decimal types may not exceed 39 digits of precision and 15 digits of scale for cross compatibility.");
		return DECIMAL;
	}
	
	/**
	 * Find the appropriate DataType for a string by size and whether it is a fixed-size string. 
	 * Fixed-sized strings may not exceed a size of 255.
	 * @param size - maximum number of bytes (may or may not be equivalent to a character depending on character encoding).
	 * @param fixed - whether the string is to be space-padded on the right to keep the length fixed.
	 * @return
	 */
	public DataType findStringType(Integer size, boolean fixed) {
		if (size > 1_073_741_824)
			throw new IllegalArgumentException("String types may not exceed 1GiB for cross compatibility.");
		return size < 256 && fixed ? CHAR
			 : size < 4000 ? VARCHAR
			 : LONGVARCHAR;
	}
	
	private static Map<DbmsType, DataTypeMap> typeMaps;
	
	/**
	 * Register a DBMS-specific type map.  The type map is used to translate 
	 * back and forth between DataType and the DBMS type string.
	 * @param databaseName
	 * @param typeMap
	 * @param registerReverse
	 */
	public static void registerTypeMap(DbmsType dbName, DataTypeMap typeMap) {
		typeMaps.put(dbName, typeMap);
	}
	
	static {
		typeMaps = new HashMap<>();
		registerTypeMap(DbmsType.MYSQL, new MySqlTypeMap());
		registerTypeMap(DbmsType.ORACLE, new OracleTypeMap());
	}
	
	public static DataTypeMap getTypeMap(DbmsType dbName) {
		return typeMaps.get(dbName);
	}
}
