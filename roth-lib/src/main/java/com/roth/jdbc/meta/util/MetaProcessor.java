package com.roth.jdbc.meta.util;

import java.sql.SQLException;

import com.roth.jdbc.meta.model.Column;
import com.roth.jdbc.meta.model.ColumnInfoBean;
import com.roth.jdbc.meta.model.Table;
import com.roth.jdbc.meta.model.TableInfoBean;
import com.roth.jdbc.util.DbmsType;

public abstract class MetaProcessor {
	static final String SCHEMA_NAME = "{SCHEMA_NAME}";
	static final String TABLE_NAME = "{TABLE_NAME}";
	static final String COLUMN_NAME = "{COLUMN_NAME}";
	static final String CONDITIONS = "{CONDITIONS}";
	static final String COLUMNS = "{COLUMNS}";
	static final String INSERT = "{INSERT}";
	static final String PRIMARY = "{PRIMARY}";
	static final String PRIMARY_CONDITION = "{PRIMARY_CONDITION}";
	
	private DbmsType dbName;
	private String compareString;
	private String triggerString;
	
	protected MetaProcessor(DbmsType dbName) {
		this.dbName = dbName;
	}
	
	/**
	 * Get the compare string used in a logging trigger.
	 * @return
	 */
	public String getCompareString() { return compareString; }
	
	/**
	 * Get the trigger create clause for a logging trigger.
	 * @return
	 */
	public String getTriggerString() { return triggerString; }
	
	/**
	 * Identify the database name (type) that this processor is for.
	 * This is used to verify validity when paired with a JNDI data source.
	 * @return
	 */
	public DbmsType getDbName() { return dbName; }
	
	public DataTypeMap getDataTypeMap() { return DataType.getTypeMap(dbName); }
	
	/**
	 * Create a script to alter a table, and/or add/remove foreign keys or indexes.
	 * @param oldDefinition
	 * @param newDefinition
	 * @return
	 */
	public abstract String alterTable(Table oldDefinition, Table newDefinition);
	
	/**
	 * Create a script to create a table, including foriegn keys and indexes.
	 * @param definition
	 * @return
	 */
	public abstract String createTable(Table definition);
	
	/**
	 * Create a script to create a trigger for logging column changes.
	 * @param definition
	 * @return
	 */
	public abstract String createLogTrigger(Table definition);
	
	/**
	 * Create a script to create a trigger for archiving/deleting records.
	 * @param definition
	 * @return
	 */
	public abstract String createArchiveTrigger(Table definition, String arcSchema);
	
	/**
	 * Translate a TableInfoBean to a Table.
	 * @param info
	 * @param jndiName
	 * @return
	 */
	public abstract Table infoToTable(TableInfoBean info, String jndiName) throws SQLException;
	
	/**
	 * Translate a ColumnInfoBean to a Column.
	 */
	public abstract Column toColumn(ColumnInfoBean info);
	
	public abstract ColumnInfoBean toColumnInfo(Column column);
	
	public abstract String toDbmsString(Column column);
}
