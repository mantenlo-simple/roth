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

import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "adhoc_save",
           primaryKeyColumns = {"adhoc_save_id"})
public class AdhocSaveBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long adhocSaveId;
    private String description;
    private String jndiname;
    private Integer maxLength;
    private Integer rowLimit;
    private String statement;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public Long getAdhocSaveId() { return adhocSaveId; }
    public void setAdhocSaveId(Long adhocSaveId) { this.adhocSaveId = adhocSaveId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getJndiname() { return jndiname; }
    public void setJndiname(String jndiname) { this.jndiname = jndiname; }

    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }

    public Integer getRowLimit() { return rowLimit; }
    public void setRowLimit(Integer rowLimit) { this.rowLimit = rowLimit; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    @Override
    public boolean isNew() { 
    	boolean result = adhocSaveId == null;
    	if (result) adhocSaveId = -1L;
    	return result;  
    }
}