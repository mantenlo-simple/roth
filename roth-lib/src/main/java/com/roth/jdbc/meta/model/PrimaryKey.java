package com.roth.jdbc.meta.model;

import com.roth.export.util.CsvRecord;

import jdk.jfr.Experimental;

@Experimental
public class PrimaryKey {
	private String[] columnNames;
	private String name;
	
	public PrimaryKey() { }
	
	/**
	 * CSV Format:  primary,name,column names
	 * Example: primary,example_pk,example_id
	 * Example (multi-column): primary,example_pk,"column_1,column_2"
	 * @param csv
	 */
	public PrimaryKey(CsvRecord csv) {
		// Index 0 is "primary", so we'll skip that.
		name = csv.getString(1);
		String cols = csv.getString(2);
		columnNames = cols == null ? null : cols.split(",");
	}
	
	public String[] getColumnNames() { return columnNames; }
	public void setColumnNames(String[] columnNames) { this.columnNames = columnNames; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
}
