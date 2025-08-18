/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.roth.jdbc.meta.util;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.NoTest;
import com.roth.jdbc.annotation.SQLFileContext;
import com.roth.jdbc.meta.model.ColumnInfoBean;
import com.roth.jdbc.meta.model.ForeignKeyInfoBean;
import com.roth.jdbc.meta.model.IndexInfoBean;
import com.roth.jdbc.meta.model.Table;
import com.roth.jdbc.meta.model.TableInfoBean;
import com.roth.jdbc.meta.model.TriggerInfoBean;
import com.roth.jdbc.util.DbmsType;
import com.roth.jdbc.util.JdbcUtil;

import jdk.jfr.Experimental;

@NoTest
@Experimental
@SQLFileContext(path = "/com/roth/jdbc/meta/sql")
public class MetaUtil extends JdbcUtil {
	private static final long serialVersionUID = 2182944833141062013L;

	private static final String MESSAGE = "MESSAGE: ";
	private static final String SYSTEM = "[SYSTEM]";

	private static Map<DbmsType, Class<? extends MetaProcessor>> processors;
	static {
		processors = new HashMap<>();
		processors.put(DbmsType.MYSQL, MySqlProcessor.class);
	}
	/**
	 * Register an implementation of MetaProcessor.
	 * @param databaseName
	 * @param processor
	 */
	public static void registerMetaProcessor(DbmsType dbName, Class<? extends MetaProcessor> processor) {
		processors.put(dbName, processor);
	}

	protected MetaUtil() throws SQLException {
		super();
		suppressSettings = true;
	}
	
	public MetaUtil(String jndiname) throws SQLException {
		super(jndiname);
		suppressSettings = true;
	}
	
	// Supported database names: informix, mysql, oracle
	
	public List<String> getSchemas() throws SQLException {
		String statement = getSQLFileNull(getDbName().getDbmsName()[0].toLowerCase() + "_schemas.sql");
		if (statement == null)
			throw new SQLException("No schema info statement is defined for this database name (" + getDbName() + ").");
		Log.logDebug(statement, null, "getSchemas");
		return execQuery(statement, ArrayList.class, String.class);
	}
	
	public List<TableInfoBean> getTables(String schema) throws SQLException {
		String statement = getSQLFileNull(getDbName().getDbmsName()[0].toLowerCase() + "_tables.sql");
		if (statement == null)
			throw new SQLException("No table info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, schema);
		Log.logDebug(statement, null, "getTables");
		return execQuery(statement, ArrayList.class, TableInfoBean.class);
	}
	
	public List<ColumnInfoBean> getColumns(String tableId) throws SQLException {
		String statement = getSQLFileNull(getDbName().getDbmsName()[0].toLowerCase() + "_columns.sql");
		if (statement == null)
			throw new SQLException("No column info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, tableId);
		Log.logDebug(statement, null, "getColumns");
		return execQuery(statement, ArrayList.class, ColumnInfoBean.class);
	}
	
	public List<IndexInfoBean> getIndexes(String tableId) throws SQLException {
		String statement = getSQLFileNull(getDbName().getDbmsName()[0].toLowerCase() + "_indexes.sql");
		if (statement == null)
			throw new SQLException("No index info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, tableId);
		Log.logDebug(statement, null, "getIndexes");
		return execQuery(statement, ArrayList.class, IndexInfoBean.class);
	}
	
	public List<ForeignKeyInfoBean> getForeignKeys(String tableId) throws SQLException {
		String statement = getSQLFileNull(getDbName().getDbmsName()[0].toLowerCase() + "_foreignkeys.sql");
		if (statement == null)
			throw new SQLException("No foreign key info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, tableId);
		Log.logDebug(statement, null, "getForeignKeys");
		return execQuery(statement, ArrayList.class, ForeignKeyInfoBean.class);
	}
	
	public List<TriggerInfoBean> getTriggers(String tableId) throws SQLException {
		String statement = getSQLFileNull(getDbName().getDbmsName()[0].toLowerCase() + "_triggers.sql");
		if (statement == null)
			throw new SQLException("No trigger info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, tableId);
		Log.logDebug(statement, null, "getTriggers");
		return execQuery(statement, ArrayList.class, TriggerInfoBean.class);
	}
	
	/**
	 * Create a log table for a given table name.  The table name specified should be the name of the original table.
	 * The new log table's name will be the table name with the added suffix "_log".<br/>
	 * <br/>
	 * e.g.  If tableName is "some_table" then the new log table name will be "some_table_log".<br/>
	 * <br/>
	 * @param tableName
	 * @throws SQLException
	 */
	public void createLogTable(String tableName) throws SQLException {
		String statement = getSQLFileNull(getDbName().getDbmsName()[0].toLowerCase() + "_create_log_table.sql");
		if (statement == null)
			throw new SQLException("No log table creation statement is defined for this database name(" + getDbName() + ").");
		statement = applyParameters(statement, tableName);
		Log.logDebug(statement, null, "createLogTable");
		execUpdate(statement);
	}
	/*
	private Map<String, Object> defineVariables;
	private Map<String, String> defineTypes;
	
	private void define(CsvRecord record) {
		String name = record.getString(1);
		String type = record.getString(2);
		defineTypes.put(name, type);
		switch (type) {
		case "columns":
			defineVariables.put(name, new ArrayList<String>());
			break;
		case "template":
			defineVariables.put(name, "");
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void set(CsvRecord record) {
		String name = record.getString(1);
		String value = record.getString(2);
		String type = defineTypes.get(name);
		Object variable = defineVariables.get(name);
		if ("template".equals(type))
			defineVariables.put(name, (String)variable + value + "\n");
		else if ("columns".equals(type))
			((List<String>)variable).add(value);
		else
			defineVariables.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	private void get(CsvRecord record, Table table) {
		String name = record.getString(1);
		String type = defineTypes.get(name);
		Object variable = defineVariables.get(name);
		if ("columns".equals(type)) {
			table.getColumns().addAll(Column.variableColumns((List<String>)variable));
		}
	}
	*/
	
	/**
	 * 
	 * Predefined template names: 
	 * &nbsp; &nbsp; logTable - template for a log table.
	 * &nbsp; &nbsp; logTrigger - template for a log trigger (after update on source table).
	 * &nbsp; &nbsp; logExclude - column names to exclude in logging
	 * &nbsp; &nbsp; archiveTable - template for an archive table.
	 * &nbsp; &nbsp; archiveTrigger - template for an archive trigger (before delete on source table).
	 * @param csvDefinition
	 */
	/*
	public void createMetaObjects(String csvDefinition) {
		defineVariables = new HashMap<>();
		defineTypes = new HashMap<>();
		defineVariables.put("log", "_log");
		defineVariables.put("archive", "_arc");
		String[] definition = Data.splitLF(csvDefinition);
		if (definition == null)
			throw new IllegalArgumentException("An invalid argument was specified.");
		List<Table> tables = new ArrayList<>();
		Table table = null;
		String errorline = "";
		try {
			for (String line : definition) {
				String _line = line.trim();
				errorline = _line;
				if (_line.startsWith("#") || _line.isEmpty())
					continue;
				CsvRecord record = new CsvRecord();
				record.setBytes(_line.getBytes());
				switch (record.getString(0)) {
				case "schema":
					defineVariables.put("schema", record.getString(1));
					defineVariables.put("schema_arc", Data.nvl(record.getString(2), record.getString(1)));
					break;
				case "define":
					define(record);
					break;
				case "set":
					set(record);
					break;
				case "get":
					get(record, table);
					break;
				case "table":
					if (table != null)
						tables.add(table);
					table = new Table(record);
					break;
				case "column":
					table.getColumns().add(new Column(record, _line));
					break;
				case "primary":
					table.setPrimaryKey(new PrimaryKey(record));
					break;
				case "foreign":
					table.getForeignKeys().add(new ForeignKey(record));
					break;
				case "index":
					table.getIndexes().add(new Index(record));
					break;
				default:
					Log.logWarning("createMetaObjects.Invalid input line: " + _line, null);
				}
			}
		} catch (NumberFormatException e) {
			Log.log("EXCEPTION", "Line: " + errorline, MetaUtil.class.getCanonicalName() + ".createMetaObjects", null, true);
			throw e;		
		}
		
		if (table != null)
			tables.add(table);
		
		String create = "";
		try {
			MetaProcessor processor = Data.newInstance(processors.get(getDbName()));
			if (tables.size() > 0)
				for (Table t : tables) {
					create = processor.createTable(t);
					Log.logInfo("\n" + create + "\n", null, "createMetaObjects");
					execUpdate(create);
					if (t.hasLog()) {
						createMetaObjects(t.logTable());
						create = processor.createLogTrigger(t);
						Log.logInfo("\n" + create + "\n", null, "createMetaObjects");	
						execUpdate(create);
					}
					/* Need to find a way to get this working properly with the log tables.
					if (t.hasArchive()) {
						String arcSchema = (String)defineVariables.get("schema_arc");
						String archive = t.archiveTable(arcSchema);
						createMetaObjects(archive);
						create = processor.createArchiveTrigger(t, arcSchema);
						Log.logInfo("\n" + create + "\n", null, "createMetaObjects");
						execUpdate(create);
					}
					* /
				}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SQLException | NullPointerException e) {
			Log.log("EXCEPTION", "Create: " + create, MetaUtil.class.getCanonicalName() + ".createMetaObjects", null, true);
			Log.logException(e, null);
		}
	}
	*/
	
	public void createMetaTable(Table table) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {
		MetaProcessor processor = Data.newInstance(processors.get(getDbName()));
		String create = processor.createTable(table);
		Log.logInfo("\n" + create + "\n", null, "createMetaObjects");
		execUpdate(create);
		if (table.hasLog()) {
			createMetaTable(table.getLogTable());
			create = processor.createLogTrigger(table);
			Log.logInfo("\n" + create + "\n", null, "createMetaObjects");	
			execUpdate(create);
		}
		/* Need to find a way to get this working properly with the log tables.
		if (t.hasArchive()) {
			String arcSchema = (String)defineVariables.get("schema_arc");
			String archive = t.archiveTable(arcSchema);
			createMetaObjects(archive);
			create = processor.createArchiveTrigger(t, arcSchema);
			Log.logInfo("\n" + create + "\n", null, "createMetaObjects");
			execUpdate(create);
		}
		*/
	}
	
	public void createMetaTables(List<Table> tables) {
		try {
			if (tables != null)
				for (Table table : tables)
					createMetaTable(table);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SQLException | NullPointerException e) {
			Log.logException(e, null);
		}
	}
	
	public void initTable(String init) {
		String origin = "com.roth.jdbc.meta.MetaUtil.initTable";
		List<String> statements = breakdown(init);
		for (String statement : statements) {
			try {
				execUpdate(statement);
			}
			catch (SQLException e) {
				Log.log(MESSAGE, String.format("Init statement failed with exception to follow [\n%s\n].", statement), origin, SYSTEM, false, null);
				Log.logException(e, null);
			}
		}
	}
	
	public List<String> breakdown(String init) {
		List<String> result = new ArrayList<>();
		if (init != null) {
			String[] lines = Data.splitLF(init);
			String statement = "";
			for (String line : lines)
				if (line.trim().equals(";")) {
					result.add(statement.trim());
					statement = "";
				}
				else
					statement += line + "\n";
			if (!statement.equals(""))
				result.add(statement.trim());
		}
		return result;
	}
	
	
	public boolean dropTable(String tableName, boolean quiet) {
		String statement = "DROP TABLE {sql: 1}";
		statement = applyParameters(statement, tableName);
		try {
			execUpdate(statement);
			return true;
		}
		catch (SQLException e) {
			if (!quiet)
				Log.logException(e, null);
			return false;
		}
	}
}
