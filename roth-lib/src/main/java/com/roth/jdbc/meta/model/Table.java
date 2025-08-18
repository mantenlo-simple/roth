package com.roth.jdbc.meta.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.roth.base.annotation.Ignore;
import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.export.annotation.JsonCollection;
import com.roth.export.util.CsvRecord;
import com.roth.export.util.JsonUtil;

import jdk.jfr.Experimental;

@Experimental
public class Table {
	private List<Column> columns;
	private List<ForeignKey> foreignKeys;
	private List<Index> indexes;
	private String name;
	private PrimaryKey primaryKey;
	private String schema;
	private String template;
	
	// Special
	private boolean log;
	private boolean archive;
	
	public Table() {
		columns = new ArrayList<>();
		foreignKeys = new ArrayList<>();
		indexes = new ArrayList<>();
	}
	
	/**
	 * CSV Format:  table,schema,name
	 * Example: table,my_schema,example
	 * @param csv
	 */
	public Table(CsvRecord record) {
		this();
		// Index 0 is "table", so we'll skip that.
		schema = record.getString(1);
		name = record.getString(2);
		log = Data.nvl(record.getBoolean(3), false);
		archive = Data.nvl(record.getBoolean(4), false);
	}
	
	public List<Column> getColumns() { return columns; }
	@JsonCollection(elementClass = Column.class)
	public void setColumns(List<Column> columns) { this.columns = columns; }
	
	public List<ForeignKey> getForeignKeys() { return foreignKeys; }
	@JsonCollection(elementClass = ForeignKey.class)
	public void setForeignKeys(List<ForeignKey> foreignKeys) { this.foreignKeys = foreignKeys; }
	
	public List<Index> getIndexes() { return indexes; }
	@JsonCollection(elementClass = Index.class)
	public void setIndexes(List<Index> indexes) { this.indexes = indexes; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public PrimaryKey getPrimaryKey() { return primaryKey; }
	public void setPrimaryKey(PrimaryKey primaryKey) { this.primaryKey = primaryKey; }
	
	public String getSchema() { return schema; }
	public void setSchema(String schema) { this.schema = schema; }
	
	public String getTemplate() { return template; }
	public void setTemplate(String template) { this.template = template; }

	// Special	
	public boolean hasLog() { return log; }
	public void setLog(boolean log) { this.log = log; }
	
	public boolean hasArchive() { return archive; }
	public void setArchive(boolean archive) { this.archive = archive; }
	
	// Log Table Support
	private String logSource = Data.readTextFile(getClass(), "logTableSource.json");
	
	@Ignore
	public String getLogSource() { return logSource; }
	/**
	 * Overrides the JSON template for a log table.<br/>
	 * <b>Replacement keys:</b><br/>
	 * <b>{SCHEMA}</b> - is replaced by the origin table's schema.<br/>
	 * <b>{NAME}</b> - is replaced by the origin table's name.<br/>
	 * @param logSource
	 */
	public void setLogSource(String logSource) { this.logSource = logSource; }

	private static final String PLACEHOLDER_SCHEMA = "{SCHEMA}";
	private static final String PLACEHOLDER_NAME = "{NAME}";
	
	@Ignore
	public Table getLogTable() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String result = logSource
				.replace(PLACEHOLDER_SCHEMA, schema)
				.replace(PLACEHOLDER_NAME, name);
		Log.logDebug(result, null, "getLogTable");
		return JsonUtil.jsonToObj(result, getClass());
	}
	
	
	/*
	private String archiveSource = Data.getTextFile(getClass(), "archiveTableSource.json");
			
	@Ignore
	public String getArchiveSource() { return archiveSource; }
	/**
	 * Sets the JSON template for an archive table.  Replacement keys:<br/>
	 * {SCHEMA} - is replaced by the origin table's schema.<br/>
	 * {NAME} - is replaced by the origin table's name.<br/>
	 * {COLUMNS} - is replaced by the origin table's columns.<br/>
	 * {PRIMARY} - is replaced by the origin table's primary key column references.<br/> 
	 * @param archveSource
	 * /
	public void setArchiveSource(String archiveSource) { this.archiveSource = archiveSource; }

	public String archiveTable(String arcSchema) {
		String result = archiveSource
				.replace(PLACEHOLDER_SCHEMA, schema)
				.replace(PLACEHOLDER_NAME, name)
				.replace("{COLUMNS}", 0);
		String columns = "";
		for (Column c : this.columns)
			columns += c.getSource() + "\n";
		String primary = "";
		for (String p : this.primaryKey.getColumnNames())
			primary += (primary.isEmpty() ? "" : ",") + p;
		for (String line : archiveSource)
			result += line.replace("{SCHEMA}", Data.nvl(arcSchema, schema)).replace("{NAME}", name).replace("{COLUMNS}", columns).replace("{PRIMARY}", primary) + "\n";
		Log.logDebug(result, null, "archiveTable");
		return result;
	}
	*/
}
