package com.roth.jdbc.model;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import com.roth.base.util.Data;

public class ResultSetRow extends ResultBean {
	private static final long serialVersionUID = 1531804451555182968L;

	public static final int TYPE_BYTE = 0;
	
	private ArrayList<String> columnNames;
	private ArrayList<Integer> columnTypes;
	
	public ResultSetRow() {
		columnNames = new ArrayList<String>();
		columnTypes = new ArrayList<Integer>();
	}
	
	public void resetMetaData() {
		columnNames.clear();
		columnTypes.clear();
	}
	
	public void addMetaData(ResultSetMetaData metadata, Integer[] ignore) throws SQLException {
		for (int i = 1; i <= metadata.getColumnCount(); i++) {
			Integer ii = i;
			if (Data.in(ii, ignore))
				continue;
			columnNames.add(metadata.getColumnName(i));
			columnTypes.add(metadata.getColumnType(i));
		}
	}
	
	public void resetData() { reset(); }
	
	public void addData(ResultSet resultset) throws SQLException {
		for (int i = 0; i < columnNames.size(); i++)
		{}
	}
	
	protected static Object getValue(ResultSet resultSet, int columnIndex, int sqlType) throws Exception {
        if (resultSet.getString(columnIndex) == null) return null;
        return (sqlType == Types.BOOLEAN) ? Boolean.valueOf(resultSet.getBoolean(columnIndex))
             : (sqlType == Types.BIT) ? Boolean.valueOf(resultSet.getBoolean(columnIndex))
             : (sqlType == Types.TINYINT) ? Byte.valueOf(resultSet.getByte(columnIndex))
             : (sqlType == Types.DOUBLE) ? Double.valueOf(resultSet.getDouble(columnIndex))
             : (sqlType == Types.FLOAT) ? Float.valueOf(resultSet.getFloat(columnIndex))
             : (sqlType == Types.REAL) ? Float.valueOf(resultSet.getFloat(columnIndex))
             : (sqlType == Types.INTEGER) ? Integer.valueOf(resultSet.getInt(columnIndex))
             : (sqlType == Types.BIGINT) ? Long.valueOf(resultSet.getLong(columnIndex))
             : (sqlType == Types.SMALLINT) ? Short.valueOf(resultSet.getShort(columnIndex))
             : (sqlType == Types.VARCHAR) ? resultSet.getString(columnIndex)
             : (sqlType == Types.CHAR) ? resultSet.getString(columnIndex)
             : (sqlType == Types.DECIMAL) ? resultSet.getBigDecimal(columnIndex)
             : (sqlType == Types.NUMERIC) ? resultSet.getBigDecimal(columnIndex)
             : (sqlType == Types.DATE) ? new Date(resultSet.getDate(columnIndex).getTime())
             : (sqlType == Types.TIME) ? new Date(resultSet.getTime(columnIndex).getTime())
             : (sqlType == Types.TIMESTAMP) ? new Date(resultSet.getTimestamp(columnIndex).getTime())
             : resultSet.getString(columnIndex);
    }
	
	public Boolean getBoolean(String columnName) { return (Boolean)getObject(columnName); }
	public Boolean getBoolean(int index) { return getBoolean(columnNames.get(index)); }
	
	public Byte getByte(String columnName) { return (Byte)getObject(columnName); }
	public Byte getByte(int index) { return getByte(columnNames.get(index)); }
	
}
