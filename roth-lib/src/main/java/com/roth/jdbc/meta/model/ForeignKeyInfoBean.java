package com.roth.jdbc.meta.model;

import java.io.Serializable;

import com.roth.jdbc.annotation.PermissiveBinding;

@PermissiveBinding()
public class ForeignKeyInfoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String columnNames;
    private String constraintName;
    private String deleteRule;
    private String referencedColumnNames;
    private String referencedConstraintName;
    private String referencedSchema;
    private String referencedTableName;
    private String tableId;

    public String getColumnNames() { return columnNames; }
    public void setColumnNames(String columnNames) { this.columnNames = columnNames; }

    public String getConstraintName() { return constraintName; }
    public void setConstraintName(String constraintName) { this.constraintName = constraintName; }

    public String getDeleteRule() { return deleteRule; }
    /**
     * MySQL: "RESTRICT", "CASCADE", "SET NULL", "NO ACTION", "SET DEFAULT"<br/>
     * Oracle: "RESTRICT", "CASCADE", "NO ACTION"<br/>
     * PostgreSQL: "RESTRICT", "CASCADE", "SET NULL", "NO ACTION", "SET DEFAULT"<br/>
     * MS SQL Server: "CASCADE", "SET NULL", "NO ACTION", "SET DEFAULT"<br/>
     * @return
     */
	public void setDeleteRule(String deleteRule) { this.deleteRule = deleteRule; }
	
	public String getReferencedColumnNames() { return referencedColumnNames; }
    public void setReferencedColumnNames(String referencedColumnNames) { this.referencedColumnNames = referencedColumnNames; }

    public String getReferencedConstraintName() { return referencedConstraintName; }
    public void setReferencedConstraintName(String referencedConstraintName) { this.referencedConstraintName = referencedConstraintName; }

    public String getReferencedSchema() { return referencedSchema; }
    public void setReferencedSchema(String referencedSchema) { this.referencedSchema = referencedSchema; }

    public String getReferencedTableName() { return referencedTableName; }
    public void setReferencedTableName(String referencedTableName) { this.referencedTableName = referencedTableName; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
}