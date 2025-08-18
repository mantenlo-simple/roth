package com.roth.jdbc.meta.util;

public class MySqlTypeMap implements DataTypeMap {

	@Override
	public DataType fromDbmsType(String dbmsType, Integer a, Integer b) {
		return switch(dbmsType.toUpperCase()) {
		case "TINYINT" -> DataType.TINYINT;
		case "SMALLINT" -> DataType.SMALLINT;
		case "INT" -> DataType.INT;
		case "BIGINT" -> DataType.BIGINT;
		case "DECIMAL" -> DataType.DECIMAL;
		case "CHAR" -> DataType.CHAR;
		case "VARCHAR" -> DataType.VARCHAR;
		case "TEXT", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT" -> DataType.LONGVARCHAR;
		case "BLOB", "TINYBLOB", "MEDIUMBLOB", "LONGBLOB" -> DataType.BLOB;
		case "DATE" -> DataType.DATE;
		case "DATETIME" -> DataType.DATETIME;
		case "TIME" -> DataType.TIME;
		default -> throw new IllegalArgumentException(String.format("Invalid dbmsType value supplied: [%s]", dbmsType));
		};
	}

	@Override
	public String toDbmsType(DataType dataType, Integer a, Integer b, boolean required, boolean autoIncrement) {
		String dbmsType = switch (dataType) { 
		case TINYINT -> "TINYINT";
		case SMALLINT -> "SMALLINT";
		case INT -> "INT";
		case BIGINT -> "BIGINT";
		case DECIMAL -> "DECIMAL";
		case CHAR -> "CHAR";
		case VARCHAR -> "VARCHAR";
		case LONGVARCHAR -> "LONGTEXT";
		case BLOB -> "LONGBLOB";
		case DATE -> "DATE";
		case DATETIME -> "DATETIME";
		case TIME -> "TIME";
		};
		String params = "";
		if (a != null)
			params = String.format("(%d)", a);
		if (b != null && b > 0)
			params = params.replace(")", String.format(",%d)", b));
		String req = required ? " NOT NULL" : "";
		String auto = autoIncrement ? " AUTO_INCREMENT" : "";
		return dbmsType + params + req + auto;
	}	
		
		
		
		
		/*
		
		{
		case FIXED_STRING:
			if (column.getSize() > 255)
				throw new IllegalArgumentException("FIXED_STRING size cannot be larger than 255 in MySQL.");
			type = " CHAR(" + column.getSize() + ")"; 
			break;
		case VARIABLE_STRING:
			if (column.getSize() > 65_535)
				throw new IllegalArgumentException("VARIABLE_STRING size cannot be larger than 65,535 in MySQL.");
			type = " VARCHAR(" + column.getSize() + ")"; 
			break;
		case INTEGER:
			if (column.getSize() > 19)
				throw new IllegalArgumentException("INTEGER size cannot be larger than 19 in MySQL.");
			String intType = column.getSize() <= 3 ? " TINYINT(" : column.getSize() <= 5 ? " SMALLINT(" : column.getSize() <= 10 ? " INT(" : " BIGINT(";
			type = intType + column.getSize() + ")"; 
			break;
		case DECIMAL:
			if (column.getPrecision() > 15)
				throw new IllegalArgumentException("DECIMAL precision cannot be larger than 15 in MySQL.");
			type = " DECIMAL(" + column.getSize() + (column.getPrecision() != null ? "," + column.getPrecision() : "") + ")";
			break;
		case DATE:
			type = " DATE";
			break;
		case TIME:
			if (column.getPrecision() > 6)
				throw new IllegalArgumentException("TIME precision cannot be larger than 6 in MySQL.");
			type = " TIME";
			if (column.getPrecision() != null && column.getPrecision() > 0)
				type += "(" + column.getPrecision() + ")";
			break;
		case DATE_TIME:
			if (column.getPrecision() > 6)
				throw new IllegalArgumentException("DATETIME precision cannot be larger than 6 in MySQL.");
			type = " DATETIME";
			if (column.getPrecision() != null && column.getPrecision() > 0)
				type += "(" + column.getPrecision() + ")";
			break;
		case ZONED_DATE_TIME, LOCAL_DATE_TIME:
			throw new IllegalArgumentException("ZONED_DATE_TIME and LOCAL_DATE_TIME are not implemented for MySQL.");
		case BINARY:
			type = " BLOB";
			break;
		case JSON:
			type = " JSON";
			break;
		}
		return null; */

}
