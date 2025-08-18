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
import java.time.LocalDateTime;

import com.roth.jdbc.annotation.PermissiveBinding;

@PermissiveBinding()
public class TableInfoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDateTime created;
    private String schema;
    private String tableId;
    private String tableName;

    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

}