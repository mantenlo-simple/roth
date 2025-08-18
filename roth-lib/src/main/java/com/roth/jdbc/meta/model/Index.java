package com.roth.jdbc.meta.model;

import jdk.jfr.Experimental;

@Experimental
public class Index {
	private String[] columnNames;
	private String name;
	private Boolean unique;
	private Boolean caseInsensitive;

	public Index() { }
	
	public String[] getColumnNames() { return columnNames; }
	public void setColumnNames(String[] columnNames) { this.columnNames = columnNames; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public Boolean getUnique() { return unique; }
	public void setUnique(Boolean unique) { this.unique = unique; }
	
	public Boolean getCaseInsensitive() { return caseInsensitive; }
	public void setCaseInsensitive(Boolean caseInsensitive) { this.caseInsensitive = caseInsensitive; }
}
