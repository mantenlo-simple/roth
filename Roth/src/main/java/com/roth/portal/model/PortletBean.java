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

@JdbcTable(name = "portlet",
           primaryKeyColumns = {"portlet_id"})
public class PortletBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long portletId;
    private String portletName;
    private String portletUri;
    private String description;
    private Long applicationId;
    private String createdBy;
    private LocalDateTime createdDts;
    private String updatedBy;
    private LocalDateTime updatedDts;
    
    private String[] roles;

    public Long getPortletId() { return portletId; }
    public void setPortletId(Long portletId) { this.portletId = portletId; }

    public String getPortletName() { return portletName; }
    public void setPortletName(String portletName) { this.portletName = portletName; }

    public String getPortletUri() { return portletUri; }
    public void setPortletUri(String portletUri) { this.portletUri = portletUri; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	public LocalDateTime getCreatedDts() { return createdDts; }
	public void setCreatedDts(LocalDateTime createdDts) { this.createdDts = createdDts; }
	
	public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    public String[] getRoles() { return roles; }
	public void setRoles(String[] roles) { this.roles = roles; }
	
	@Override
    public boolean isNew() { 
    	boolean result = portletId == null; 
    	if (result) portletId = Long.valueOf(-1);
    	updatedDts = LocalDateTime.now();
    	return result; 
    }
}