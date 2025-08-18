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

import com.roth.base.util.Data;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.EnhancedBean;
import com.roth.jdbc.model.Insertable;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "role",
           primaryKeyColumns = {"role_name"})
public class RoleBean implements Serializable, StateBean, EnhancedBean, Insertable {
    private static final long serialVersionUID = 1L;

    private String roleName;
    private String description;
    private String createdBy;
    private LocalDateTime createdDts;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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
    
	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof RoleBean otherRole)
			return Data.eq(roleName, otherRole.roleName) &&
				   Data.eq(description, otherRole.description) &&
				   Data.eq(updatedBy, otherRole.updatedBy) &&
				   Data.eq(updatedDts, otherRole.updatedDts);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Data.complexHashCode(roleName, description, updatedBy, updatedDts);
	}

	@Override
	public EnhancedBean copy() {
    	RoleBean dest = new RoleBean();
    	dest.roleName = roleName;
    	dest.description = description;
    	dest.updatedBy = updatedBy;
    	dest.updatedDts = updatedDts;
    	return dest;
    }
	
	@Override
	public void merge(EnhancedBean source) {
		if (source instanceof RoleBean sourceRole) {
			description = sourceRole.description;
			updatedBy = sourceRole.updatedBy;
			updatedDts = sourceRole.updatedDts;
		}
	}
	@Override
	public void prepare() {
		// Do nothing.		
	}
}
