package com.roth.jdbc.meta.util;

import java.sql.SQLException;
import java.util.List;

import com.roth.base.util.Data;
import com.roth.base.util.GenericComparator;
import com.roth.jdbc.meta.model.Column;
import com.roth.jdbc.meta.model.ColumnInfoBean;
import com.roth.jdbc.meta.model.ForeignKey;
import com.roth.jdbc.meta.model.ForeignKeyInfoBean;
import com.roth.jdbc.meta.model.Index;
import com.roth.jdbc.meta.model.IndexInfoBean;
import com.roth.jdbc.meta.model.PrimaryKey;
import com.roth.jdbc.meta.model.Table;
import com.roth.jdbc.meta.model.TableInfoBean;
import com.roth.jdbc.util.DbmsType;

import jdk.jfr.Experimental;

@Experimental
public class MySqlProcessor extends MetaProcessor {

	private static final String LF = "\n";
	
	public MySqlProcessor() { super(DbmsType.MYSQL); }

	@SuppressWarnings("unchecked")
	@Override
	public String alterTable(Table oldDefinition, Table newDefinition) {
		// Columns
		oldDefinition.getColumns().sort(new GenericComparator("name", 1));
		newDefinition.getColumns().sort(new GenericComparator("name", 1));
		int oldi = 0;
		int newi = 0;
		StringBuilder statement = new StringBuilder("");
		ColumnMap map = new MySqlColumnMap();
		while (oldi < oldDefinition.getColumns().size() || newi < newDefinition.getColumns().size()) {
			Column oldc = oldi < oldDefinition.getColumns().size() ? oldDefinition.getColumns().get(oldi) : null;
			Column newc = newi < newDefinition.getColumns().size() ? newDefinition.getColumns().get(newi) : null;
			Integer comp = oldc == null ? 1 : newc == null ? -1 : oldc.compareTo(newc);
			String diff = map.toSqlDiff(oldc, newc);
			boolean delimit = !statement.isEmpty() && !diff.isEmpty();
			statement.append((delimit ? "," : "") + diff);
			
			if (comp < 0)
				oldi++;
			else if (comp > 0)
				newi++;
		}
		
		
		
		      // Sort columns in each by name.  This will make for a more efficient comparison.
		
		/*
		 
		 - Check for differences in columns
		     - Changes in Data Type / Size / Precision
		     - Added Columns
		     - Removed Columns
		 - Check for differences in foreign keys
		     - Added Foreign Keys
		     - Removed Foreign Keys
		 - Check for differences in indexes
		     - Changes to index defintion
		     - Added Indexes
		     - Removed Indexes
			
		 - Generate ALTER TABLE statement(s).
		 - Generate DROP / CREATE FOREIGN KEY statements.
		 - Generate DROP / CREATE INDEX statements.
		 */
		return null;
	}

	@Override
	public String createTable(Table definition) {
		StringBuilder result = new StringBuilder(String.format("CREATE TABLE %s.%s(%s    ", definition.getSchema(), definition.getName(), LF));
		// Columns
		for (Column column : definition.getColumns())
			result.append(String.format("%s,%s    ", new MySqlColumnMap().toSqlString(column), LF));
		// Primary Key
		result.append(String.format("PRIMARY KEY(%s)", Data.join(definition.getPrimaryKey().getColumnNames(), ",")));
		// Foreign keys
		for (ForeignKey foreign : definition.getForeignKeys())
			result.append(String.format(",%s    %s", LF, createForeignKey(foreign)));
		// Indexes
		for (Index index : definition.getIndexes())
			result.append(String.format(",%s    %s", LF, createIndex(index)));
		result.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");
		return result.toString();
	}
	
	protected String createForeignKey(ForeignKey definition) {
		String result = "FOREIGN KEY " + definition.getName() + " (" + Data.join(definition.getColumnNames(), ",") + ")" +
	                    " REFERENCES " + definition.getRefTableName() + " (" + Data.join(definition.getRefColumnNames(), ",") + ")";
		if (Data.nvl(definition.getCascadeDelete(), false))
			result += " ON DELETE CASCADE";
		return result;
	}
	
	protected String createIndex(Index definition) {
		String unique = Data.nvl(definition.getUnique(), false).booleanValue() ? "UNIQUE " : "";
		return String.format("%sINDEX %s (%s)", unique, definition.getName(), Data.join(definition.getColumnNames(), ","));
	}

	private String logTrigger = """
			CREATE TRIGGER {SCHEMA_NAME}.{TABLE_NAME}_log_au 
			AFTER UPDATE ON {SCHEMA_NAME}.{TABLE_NAME}
			FOR EACH ROW
			BEGIN
			    {CONDITIONS}\
			    END IF;
			END;
			""";  // Note: the IF start is applied as part of CONDITIONS.
	public String getLogTrigger() { return logTrigger; }
	public void setLogTrigger(String logTrigger) { this.logTrigger = logTrigger; }

	private String logCondition = "IF NEW.{COLUMN_NAME} <=> OLD.{COLUMN_NAME} = 0 THEN\n{INSERT}\n";
	public String getLogCondition() { return logCondition; }
	public void setLogCondition(String logCondition) { this.logCondition = logCondition; }

	private String logCondElse = "ELSE";
	public String getLogCondElse() { return logCondElse; }
	public void setLogCondElse(String logCondElse) { this.logCondElse = logCondElse; }
	
	private String logInsert = """
			        INSERT INTO {SCHEMA_NAME}.{TABLE_NAME}_log ({TABLE_NAME}_id, field_name, old_value, new_value, changed_by, changed_dts)
			        VALUES (NEW.{TABLE_NAME}_id, '{COLUMN_NAME}', OLD.{COLUMN_NAME}, NEW.{COLUMN_NAME}, NEW.updated_by, NEW.updated_dts);\
			""";
	public String getLogInsert() { return logInsert; }
	public void setLogInsert(String logInsert) { this.logInsert = logInsert; }

	@Override
	public String createLogTrigger(Table definition) {
		StringBuilder conditions = new StringBuilder("");
		for (Column column : definition.getColumns())
			if (!Data.in(column.getName(), definition.getPrimaryKey().getColumnNames()) && !Data.in(column.getName(), new String[] {"created_by", "created_dts", "updated_by", "updated_dts"})) {
				if (!conditions.isEmpty())
					conditions.append("    " + "END IF;\n    "); // + logCondElse;
				conditions.append(logCondition.replace(COLUMN_NAME, column.getName())
						                      .replace(INSERT, logInsert
						                    		  .replace(SCHEMA_NAME, definition.getSchema())
				                    		          .replace(TABLE_NAME, definition.getName())
						                    		  .replace(COLUMN_NAME, column.getName())));
			}
		return logTrigger
				.replace(SCHEMA_NAME, definition.getSchema())
				.replace(TABLE_NAME, definition.getName())
				.replace(CONDITIONS, conditions.toString());
	}

	private String archiveTrigger = """
			CREATE TRIGGER {SCHEMA_NAME}.{TABLE_NAME}_archive_bd BEFORE DELETE ON {TABLE_NAME}
			FOR EACH ROW
			BEGIN
			    {INSERT}
			END;
			""";
	public String getArchiveTrigger() { return archiveTrigger; }
	public void setArchiveTrigger(String archiveTrigger) { this.archiveTrigger = archiveTrigger; }
	
	private String archiveInsert = """
			    INSERT INTO {SCHEMA_NAME}.{TABLE_NAME}_arc ({COLUMNS}, archive_status, archive_dts)
			    SELECT {COLUMNS}, 'D', SYSDATE()
			"""; 
	private String applyArchiveInsert(String schemaName, String tableName, StringBuilder columns, StringBuilder primaryCondition) {
		return archiveInsert
				.replace(SCHEMA_NAME, schemaName)
				.replace(TABLE_NAME, tableName)
	            .replace(COLUMNS, columns)
	            .replace(PRIMARY_CONDITION, primaryCondition);
	}

	@Override
	public String createArchiveTrigger(Table definition, String arcSchema) {
		// TODO Auto-generated method stub
		StringBuilder columns = new StringBuilder("");
		StringBuilder primaryCondition = new StringBuilder("");
		for (Column column : definition.getColumns()) {
			if (Data.in(column.getName(), definition.getPrimaryKey().getColumnNames()))
				primaryCondition.append((primaryCondition.isEmpty() ? "" : "AND ") + "{PRIMARY} = OLD.{PRIMARY}".replace(PRIMARY, column.getName()));
			columns.append((columns.isEmpty() ? "" : ", ") + column.getName());
		}
		String insert = applyArchiveInsert(definition.getSchema(), definition.getName(), columns, primaryCondition);
		if (definition.hasLog())
			insert += applyArchiveInsert(definition.getSchema(), definition.getName() + "_log", columns, primaryCondition);
		return archiveTrigger
				.replace(SCHEMA_NAME, definition.getSchema())
				.replace(TABLE_NAME, definition.getName())
				.replace(INSERT, insert);
	}

	@Override
	public Table infoToTable(TableInfoBean info, String jndiName) throws SQLException {
		Table table = new Table();
		table.setSchema(info.getSchema());
		table.setName(info.getTableName());
		MetaUtil util = new MetaUtil(jndiName);
		// Read indexes to find primary key, and process other indexes
		List<IndexInfoBean> ixinfo = util.getIndexes(info.getTableId());
		if (ixinfo != null)
			for (IndexInfoBean ix : ixinfo) {
				if ("Y".equals(ix.getPrimaryKey())) {
					PrimaryKey pkey = new PrimaryKey();
					pkey.setName(ix.getIndexName());
					pkey.setColumnNames(ix.getColumns().replace(" ", "").split(","));
					table.setPrimaryKey(pkey);
					continue;
				}
				Index index = new Index();
				index.setName(ix.getIndexName());
				index.setColumnNames(ix.getColumns().replace(" ", "").split(","));
				index.setUnique("UNIQUE".equals(ix.getUniqueConstraint()) ? true : null);
				table.getIndexes().add(index);
			}
		// Read foreign keys
		List<ForeignKeyInfoBean> fkinfo = util.getForeignKeys(info.getTableId());
		if (fkinfo != null)
			for (ForeignKeyInfoBean fk : fkinfo) {
				ForeignKey foreign = new ForeignKey();
				foreign.setName(fk.getConstraintName());
				foreign.setColumnNames(fk.getColumnNames().replace(" ", "").split(","));
				foreign.setRefTableName(fk.getReferencedTableName());
				foreign.setRefColumnNames(fk.getReferencedColumnNames().replace(" ", "").split(","));
				foreign.setCascadeDelete("CASCADE".equals(fk.getDeleteRule()) ? true : null);
				table.getForeignKeys().add(foreign);
			}
		// Read columns
		ColumnMap map = new MySqlColumnMap();
		List<ColumnInfoBean> cinfo = util.getColumns(info.getTableId());
		if (cinfo != null) // This check is ridiculous because a table should exist without columns
			for (ColumnInfoBean col : cinfo)
				table.getColumns().add(map.toColumn(col));
		return table;
	}

	@Override
	public Column toColumn(ColumnInfoBean info) {
		if (info == null)
			return null;
		Column result = new Column();
		result.setName(info.getColumnName());
		result.setRequired("NOT NULL".equals(info.getNullConstraint()));
		result.setAutoIncrement(info.getColumnType().toUpperCase().contains("AUTO_INCREMENT"));
		result.setDataType(getDataTypeMap().fromDbmsType(LF, null, null));
		return result;
	}

	@Override
	public ColumnInfoBean toColumnInfo(Column column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toDbmsString(Column column) {
		// TODO Auto-generated method stub
		return null;
	}
}
