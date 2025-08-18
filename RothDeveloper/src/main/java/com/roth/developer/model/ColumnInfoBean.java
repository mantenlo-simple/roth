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
package com.roth.developer.model;

import java.io.Serializable;

import com.roth.jdbc.annotation.PermissiveBinding;

@PermissiveBinding()
public class ColumnInfoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String columnDefault;
    private String columnName;
    private Long columnSequence;
    private String columnType;
    private String nullConstraint;
    private String tableId;

    public String getColumnDefault() { return columnDefault; }
    public void setColumnDefault(String columnDefault) { this.columnDefault = columnDefault; }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public Long getColumnSequence() { return columnSequence; }
    public void setColumnSequence(Long columnSequence) { this.columnSequence = columnSequence; }

    public String getColumnType() { return columnType; }
    public void setColumnType(String columnType) { this.columnType = columnType; }

    public String getNullConstraint() { return nullConstraint; }
    public void setNullConstraint(String nullConstraint) { this.nullConstraint = nullConstraint; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

}