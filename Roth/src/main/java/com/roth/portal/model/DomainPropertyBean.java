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

@JdbcTable(name = "domain_property",
           primaryKeyColumns = {"domain_id", "property_name"})
public class DomainPropertyBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long domainId;
    private String propertyName;
    private String propertyValue;
    private String createdBy;
    private LocalDateTime createdDts;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }

    public String getPropertyValue() { return propertyValue; }
    public void setPropertyValue(String propertyValue) { this.propertyValue = propertyValue; }

    public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	public LocalDateTime getCreatedDts() { return createdDts; }
	public void setCreatedDts(LocalDateTime createdDts) { this.createdDts = createdDts; }
	
	public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    @Override
    public boolean isNew() { boolean result = updatedDts == null; updatedDts = LocalDateTime.now(); return result; }
}