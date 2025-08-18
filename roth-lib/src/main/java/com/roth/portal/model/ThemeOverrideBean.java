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
package com.roth.portal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "theme_override",
           primaryKeyColumns = {"theme_id", "host_name"})
public class ThemeOverrideBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long altThemeId;
    private String hostName;
    private Long themeId;
    private String createdBy;
    private LocalDateTime createdDts;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public Long getAltThemeId() { return altThemeId; }
    public void setAltThemeId(Long altThemeId) { this.altThemeId = altThemeId; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public Long getThemeId() { return themeId; }
    public void setThemeId(Long themeId) { this.themeId = themeId; }

    public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	public LocalDateTime getCreatedDts() { return createdDts; }
	public void setCreatedDts(LocalDateTime createdDts) { this.createdDts = createdDts; }
	
	public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    @Override
    public boolean isNew() {
    	boolean result = updatedDts == null;
    	return result; 
    }
}