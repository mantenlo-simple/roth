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

@JdbcTable(name = "user_group",
           primaryKeyColumns = {"userid", "domain_id", "group_id"})
public class UserGroupBean extends DataGridTreeNode implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private String userid;
    private Long domainId;
    private Long groupId;
    private String createdBy;
    private LocalDateTime createdDts;
    
    // These fields are not part of the table.
    private String groupName;
    private Long parentGroupId;
    private String lineage;
    
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    public Long getDomainId() { return domainId; }
	public void setDomainId(Long domainId) { this.domainId = domainId; }
	
	public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDts() { return createdDts; }
    public void setCreatedDts(LocalDateTime createdDts) { this.createdDts = createdDts; }
    
    // This field is not part of the table.
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public Long getParentGroupId() { return parentGroupId; }
	public void setParentGroupId(Long parentGroupId) { this.parentGroupId = parentGroupId; }
	
	public String getLineage() { return lineage; }
	public void setLineage(String lineage) { this.lineage = lineage; }
	
	@Override
    public boolean isNew() { boolean result = createdDts == null; createdDts = LocalDateTime.now(); return result; }
    
    @Override
	public Long getNodeId() { return groupId; }
	@Override
	public Long getParentNodeId() { return parentGroupId; }
	@Override
	public String getNodeLineage() { return lineage; }
}