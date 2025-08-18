package com.roth.jdbc.meta.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.roth.base.annotation.Ignore;
import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.export.util.JsonUtil;
import com.roth.jdbc.meta.util.DataType;

import jdk.jfr.Experimental;

@Experimental
public class Column {
	private Boolean autoIncrement;
	private String defaultValue;
	private String name;
	private Integer precision;
	private Boolean required;
	private Integer size;
	private DataType dataType;
	
	public Column() { }
	
	public Column(String name, DataType dataType, Integer size, Integer precision) {
		this(name, dataType, size, precision, false, false);
	}
	
	public Column(String name, DataType dataType, Integer size, Integer precision, Boolean required) {
		this(name, dataType, size, precision, required, false);
	}
	
	public Column(String name, DataType dataType, Integer size, Integer precision, Boolean required, Boolean autoIncrement) {
		this.name = name;
		this.dataType = dataType;
		this.size = size;
		this.precision = precision;
		this.required = required;
		this.autoIncrement = autoIncrement;
	}
	
	public Boolean getAutoIncrement() { return autoIncrement; }
	public void setAutoIncrement(Boolean autoIncrement) { this.autoIncrement = autoIncrement; }
	public Boolean isAutoIncrement() { return getAutoIncrement(); }

	public String getDefaultValue() { return defaultValue; }
	public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public Integer getPrecision() { return precision; }
	public void setPrecision(Integer precision) { this.precision = precision; }

	public void setRequired(Boolean required) { this.required = required; }
	public Boolean getRequired() { return required; }
	public Boolean isRequired() { return getRequired(); }
	
	public Integer getSize() { return size; }
	public void setSize(Integer size) { this.size = size; }

	public DataType getDataType() { return dataType; }
	public void setDataType(DataType dataType) { this.dataType = dataType; }
	
	// Audit Columns Support
	private String auditSource = Data.readTextFile(getClass(), "auditColumns.json");
	
	@Ignore
	public String getAuditSource() { return auditSource; }
	/**
	 * Overrides the JSON template for audit columns.<br/>
	 * @param auditSource
	 */
	public void setAuditSource(String auditSource) { this.auditSource = auditSource; }
	
	@SuppressWarnings("unchecked")
	@Ignore
	public List<Column> getAuditColumns() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String result = auditSource;
		Log.logDebug(result, null, "getAuditColumns");
		return JsonUtil.jsonToObj(result, ArrayList.class, getClass());
	}
	
	/**
	 * Compares the column to another column. The following values may be
	 * returned:<br/> 
	 * null -> The other column is the same.<br/>
	 * -1 - This column's name is less than the other column's name alphabetically.<br/> 
	 *  0 - The other column has the same name, but is otherwise different.<br/>
	 *  1 - This column's name is greater than the other column's name alphabetically.<br/>
	 * @param other
	 * @return
	 */
	public Integer compareTo(Column other) {
		if (other == null)
			throw new IllegalArgumentException("It is invalid to compare a Column to null.");
		int nameComp = name.compareTo(other.name);
		if (nameComp != 0)
			return nameComp;
		boolean matches = Data.nullSafeMatch(autoIncrement, other.autoIncrement) &&
						  Data.nullSafeMatch(defaultValue, other.defaultValue) &&
						  Data.nullSafeMatch(precision, other.precision) &&
						  Data.nullSafeMatch(required, other.required) &&
						  Data.nullSafeMatch(size, other.size) &&
						  Data.nullSafeMatch(dataType, other.dataType);
		return matches ? null : 0;
	}
}
