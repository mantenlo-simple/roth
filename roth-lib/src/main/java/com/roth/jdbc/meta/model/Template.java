package com.roth.jdbc.meta.model;

import java.util.List;

import com.roth.export.annotation.JsonCollection;

import jdk.jfr.Experimental;

@Experimental
public class Template {
	private List<Column> columns;
	private String name;
	private Table table;
	
	public List<Column> getColumns() { return columns; }
	@JsonCollection(elementClass = Column.class)
	public void setColumns(List<Column> columns) { this.columns = columns; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public Table getTable() { return table; }
	public void setTable(Table table) { this.table = table; }
}
