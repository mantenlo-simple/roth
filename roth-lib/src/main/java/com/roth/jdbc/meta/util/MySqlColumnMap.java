package com.roth.jdbc.meta.util;

import com.roth.base.util.Data;
import com.roth.jdbc.meta.model.Column;
import com.roth.jdbc.meta.model.ColumnInfoBean;
import com.roth.jdbc.util.DbmsType;

import jdk.jfr.Experimental;

@Experimental
public class MySqlColumnMap implements ColumnMap {

	@Override
	public DbmsType databaseName() {
		return DbmsType.MYSQL;
	}

	@Override
	public Column toColumn(ColumnInfoBean info) {
		Column column = new Column();
		column.setName(info.getColumnName());
		column.setAutoIncrement(processType(column, info.getColumnType()));
		column.setDefaultValue(info.getColumnDefault());
		column.setRequired(info.getNullConstraint().equals("NOT NULL") ? true : null);
		return column;
	}

	private Boolean processType(Column column, String type) {
		Boolean autoIncrement = type.contains("AUTO_INCREMENT") ? true : null;
		String adjType = type.replace("AUTO_INCREMENT", "").trim();
		column.setSize(size(adjType));
		column.setPrecision(precision(adjType));
		adjType = adjType.contains("(") ? adjType.substring(0, adjType.indexOf('(')) : adjType;
		column.setDataType(DataType.getTypeMap(databaseName()).fromDbmsType(adjType, column.getSize(), column.getPrecision()));
		return autoIncrement;
	}

	private Integer size(String type) {
		Integer size = null;
		int s = type.indexOf("(");
		if (s > -1) {
			int c = type.indexOf(",");
			int e = c > -1 ? c : type.indexOf(")");
			size = Integer.valueOf(type.substring(s + 1, e));
		}
		return size;
	}
	
	private Integer precision(String type) {
		Integer precision = null;
		int s = type.indexOf(",");
		if (s > -1)
			precision = Integer.valueOf(type.substring(s + 1, type.indexOf(")")));
		return precision;
	}
	

	@Override
	public String toSqlString(Column column) {
		String name = column.getName();
		String type = DataType.getTypeMap(databaseName()).toDbmsType(column.getDataType(), column.getSize(), column.getPrecision(), Data.nvl(column.isRequired(), false), Data.nvl(column.isAutoIncrement(), false));
		return String.format("%s %s", name, type);
	}

	@Override
	public String toSqlDiff(Column o, Column n) {
		Integer comp = o.compareTo(n);
		if (comp == null)
			return "";
		if (comp < 0)
			return String.format("DROP COLUMN %s", o.getName());
		return String.format("%s COLUMN %s", comp > 0 ? "ADD" : "CHANGE", toSqlString(n)); 
	}

}
