package com.roth.jdbc.meta.model;

import com.roth.base.util.Data;

import jdk.jfr.Experimental;

@Experimental
public class ForeignKey {
	private Boolean cascadeDelete;
	private String[] columnNames;
	private String name;
	private String refTableName;
	private String[] refColumnNames;
	
	public ForeignKey() { }
	
	public ForeignKey(String name, String[] columnNames, String refTableName, String[] refColumnNames, Boolean cascadeDelete) {
		this.name = name;
		this.columnNames = columnNames;
		this.refTableName = refTableName;
		this.refColumnNames = refColumnNames;
		this.cascadeDelete = cascadeDelete;
	}
	
	public Boolean getCascadeDelete() { return cascadeDelete; }
	public void setCascadeDelete(Boolean cascadeDelete) { this.cascadeDelete = cascadeDelete; }
	
	public String[] getColumnNames() { return columnNames; }
	public void setColumnNames(String[] columnNames) { this.columnNames = columnNames; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getRefTableName() { return refTableName; }
	public void setRefTableName(String refTableName) { this.refTableName = refTableName; }
	
	public String[] getRefColumnNames() { return refColumnNames; }
	public void setRefColumnNames(String[] refColumnNames) { this.refColumnNames = refColumnNames; }
	
	/**
	 * Compares the foreign key to another foreign key. The following values may be
	 * returned:<br/> 
	 * null -> The other foreign key is the same.<br/>
	 * -1 - This foreign key's name is less than the other foreign key's name alphabetically.<br/> 
	 *  0 - The other foreign key has the same name, but is otherwise different.<br/>
	 *  1 - This foreign key's name is greater than the other foreign key's name alphabetically.<br/>
	 * @param other
	 * @return
	 */
	public Integer compareTo(ForeignKey other) {
		if (other == null)
			throw new IllegalArgumentException("It is invalid to compare a ForeignKey to null.");
		int nameComp = name.compareTo(other.name);
		if (nameComp != 0)
			return nameComp;
		boolean matches = Data.nullSafeMatch(cascadeDelete, other.cascadeDelete) &&
						  Data.nullSafeMatch(columnNames, other.columnNames) &&
						  Data.nullSafeMatch(refTableName, other.refTableName) &&
						  Data.nullSafeMatch(refColumnNames, other.refColumnNames);
		return matches ? null : 0;
	}
}
