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

@JdbcTable(name = "adhoc_hist",
           primaryKeyColumns = {"adhoc_hist_id"})
public class AdhocHistBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long adhocHistId;
    private String description;
    private LocalDateTime endDts;
    private String execBy;
    private LocalDateTime execDts;
    private String jndiname;
    private Integer maxLength;
    private Integer rowLimit;
    private String statement;

    public Long getAdhocHistId() { return adhocHistId; }
    public void setAdhocHistId(Long adhocHistId) { this.adhocHistId = adhocHistId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getEndDts() { return endDts; }
    public void setEndDts(LocalDateTime endDts) { this.endDts = endDts; }

    public String getExecBy() { return execBy; }
    public void setExecBy(String execBy) { this.execBy = execBy; }

    public LocalDateTime getExecDts() { return execDts; }
    public void setExecDts(LocalDateTime execDts) { this.execDts = execDts; }

    public String getJndiname() { return jndiname; }
    public void setJndiname(String jndiname) { this.jndiname = jndiname; }

    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }

    public Integer getRowLimit() { return rowLimit; }
    public void setRowLimit(Integer rowLimit) { this.rowLimit = rowLimit; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    @Override
    public boolean isNew() { 
    	boolean result = adhocHistId == null;
    	if (result) adhocHistId = -1L;
    	return result; 
    }
}