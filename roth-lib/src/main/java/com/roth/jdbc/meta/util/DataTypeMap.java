package com.roth.jdbc.meta.util;

public interface DataTypeMap {
	/**
	 * Derive a data type from the dbms type information.
	 * @param dbmsType
	 * @param a - the first type parameter.  char -> size, number, date/time -> precision
	 * @param b - the second type parameter.  number -> scale
	 * @return
	 */
	DataType fromDbmsType(String dbmsType, Integer a, Integer b);
	
	/**
	 * Derive a dbms type from a data type and associated information.
	 * @param datatype
	 * @param a
	 * @param b
	 * @return
	 */
	String toDbmsType(DataType dataType, Integer a, Integer b, boolean required, boolean autoIncrement);
}
