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
package com.roth.jdbc.meta.model;

import java.io.Serializable;

import com.roth.jdbc.annotation.PermissiveBinding;

@PermissiveBinding()
public class IndexInfoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String columns;
    private String indexName;
    private String primaryKey;
    private String schema;
    private String tableId;
    private String uniqueConstraint;

    public String getColumns() { return columns; }
    public void setColumns(String columns) { this.columns = columns; }

    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }

    public String getPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(String primaryKey) { this.primaryKey = primaryKey; }
    
    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getUniqueConstraint() { return uniqueConstraint; }
    public void setUniqueConstraint(String uniqueConstraint) { this.uniqueConstraint = uniqueConstraint; }
}