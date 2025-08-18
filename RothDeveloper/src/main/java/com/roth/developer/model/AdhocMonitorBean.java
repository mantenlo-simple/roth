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

@JdbcTable(name = "adhoc_monitor",
           primaryKeyColumns = {"adhoc_monitor_id"})
public class AdhocMonitorBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long adhocMonitorId;
    private String description;
    private LocalDateTime endDts;
    private String errorHi;
    private String errorLo;
    private String execCache;
    private LocalDateTime execDts;
    private String execFrequency;
    private Integer execInterval;
    private String jndiname;
    private String matchType;
    private String name;
    private String statement;
    private String status;
    private String validHi;
    private String validLo;
    private String warningHi;
    private String warningLo;

    public Long getAdhocMonitorId() { return adhocMonitorId; }
    public void setAdhocMonitorId(Long adhocMonitorId) { this.adhocMonitorId = adhocMonitorId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getEndDts() { return endDts; }
    public void setEndDts(LocalDateTime endDts) { this.endDts = endDts; }

    public String getErrorHi() { return errorHi; }
    public void setErrorHi(String errorHi) { this.errorHi = errorHi; }

    public String getErrorLo() { return errorLo; }
    public void setErrorLo(String errorLo) { this.errorLo = errorLo; }

    public String getExecCache() { return execCache; }
    public void setExecCache(String execCache) { this.execCache = execCache; }

    public LocalDateTime getExecDts() { return execDts; }
    public void setExecDts(LocalDateTime execDts) { this.execDts = execDts; }

    public String getExecFrequency() { return execFrequency; }
    public void setExecFrequency(String execFrequency) { this.execFrequency = execFrequency; }

    public Integer getExecInterval() { return execInterval; }
    public void setExecInterval(Integer execInterval) { this.execInterval = execInterval; }

    public String getJndiname() { return jndiname; }
    public void setJndiname(String jndiname) { this.jndiname = jndiname; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getValidHi() { return validHi; }
    public void setValidHi(String validHi) { this.validHi = validHi; }

    public String getValidLo() { return validLo; }
    public void setValidLo(String validLo) { this.validLo = validLo; }

    public String getWarningHi() { return warningHi; }
    public void setWarningHi(String warningHi) { this.warningHi = warningHi; }

    public String getWarningLo() { return warningLo; }
    public void setWarningLo(String warningLo) { this.warningLo = warningLo; }

    @Override
    public boolean isNew() { 
    	boolean result = adhocMonitorId == null;
    	if (result) adhocMonitorId = -1L;
    	return result; 
    }
}