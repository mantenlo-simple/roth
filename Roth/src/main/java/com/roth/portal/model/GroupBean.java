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

@JdbcTable(name = "`group`",
           primaryKeyColumns = {"group_id"})
public class GroupBean extends DataGridTreeNode implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long groupId;
    private Long domainId;
    private Long parentGroupId;
    private String groupName;
    private String description;
    private String lineage;
    private String createdBy;
    private LocalDateTime createdDts;
    private String updatedBy;
    private LocalDateTime updatedDts;
        
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getDomainId() { return domainId; }
	public void setDomainId(Long domainId) { this.domainId = domainId; }
    
    public Long getParentGroupId() { return parentGroupId; }
    public void setParentGroupId(Long parentGroupId) { this.parentGroupId = parentGroupId; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLineage() { return lineage; }
    public void setLineage(String lineage) { this.lineage = lineage; }
    
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
    	boolean result = groupId == null; 
    	if (result) { groupId = Long.valueOf(-1); lineage = ""; }
    	updatedDts = LocalDateTime.now();
    	return result; 
    }
    
	@Override
	public Long getNodeId() { return groupId; }
	@Override
	public Long getParentNodeId() { return parentGroupId; }
	@Override
	public String getNodeLineage() { return lineage; }
}