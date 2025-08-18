package com.roth.jdbc.meta.model;

import java.io.Serializable;
import java.util.List;

import com.roth.export.annotation.JsonCollection;

public class Manifest implements Serializable {
	private static final long serialVersionUID = 3199406692293012818L;

	private List<String> drops;
	private String jndiName;
	private String schema;
	private List<String> tables;
	
	public List<String> getDrops() { return drops; }
	@JsonCollection(elementClass = String.class)
	public void setDrops(List<String> drops) { this.drops = drops; }
	
	public String getJndiName() { return jndiName; }
	public void setJndiName(String jndiName) { this.jndiName = jndiName; }

	public String getSchema() { return schema; }
	public void setSchema(String schema) { this.schema = schema; }
	
	public List<String> getTables() { return tables; }
	@JsonCollection(elementClass = String.class)
	public void setTables(List<String> tables) { this.tables = tables; }
}
