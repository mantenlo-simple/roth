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
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.roth.base.util.Data;
import com.roth.jdbc.model.StateBean;

public abstract class AbstractProperty implements Serializable, StateBean {
	private static final long serialVersionUID = -2307373544014537703L;

	private String propertyName;
    private String propertyValue;
    private String createdBy;
    private LocalDateTime createdDts;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }

    public String getPropertyValue() { return propertyValue; }
    public void setPropertyValue(String propertyValue) { this.propertyValue = propertyValue; }

    public String getString() { return propertyValue; }
    public void setString(String propertyValue) { this.propertyValue = propertyValue; }
    public void setString(String propertyValue, String defaultValue) { this.propertyValue = Data.nvl(propertyValue, defaultValue); }
    
    public Integer getInteger() { return Data.strToInteger(propertyValue); }
    public Integer getInteger(Integer defaultValue) { return propertyValue == null ? defaultValue : getInteger(); }
    public void setInteger(Integer propertyValue) { this.propertyValue = propertyValue == null ? null : propertyValue.toString(); }
    
    public Long getLong() { return Data.strToLong(propertyValue); }
    public Long getLong(Long defaultValue) { return propertyValue == null ? defaultValue : getLong(); }
    public void setLong(Long propertyValue) { this.propertyValue = propertyValue == null ? null : propertyValue.toString(); }
    
    public Float getFloat() { return Data.strToFloat(propertyValue); }
    public Float getFloat(Float defaultValue) { return propertyValue == null ? defaultValue : getFloat(); }
    public void setFloat(Float propertyValue) { this.propertyValue = propertyValue == null ? null : propertyValue.toString(); }
    
    public Double getDouble() { return Data.strToDouble(propertyValue); }
    public Double getDouble(Double defaultValue) { return propertyValue == null ? defaultValue : getDouble(); }
    public void setDouble(Double propertyValue) { this.propertyValue = propertyValue == null ? null : propertyValue.toString(); }
    
    public BigDecimal getBigDecimal() { return Data.strToBigDecimal(propertyValue); }
    public BigDecimal getBigDecimal(BigDecimal defaultValue) { return propertyValue == null ? defaultValue : getBigDecimal(); }
    public void setBigDecimal(BigDecimal propertyValue) { this.propertyValue = propertyValue == null ? null : propertyValue.toString(); }
    
    public LocalDateTime getDate() { return Data.strToLocalDateTime(propertyValue); }
    public LocalDateTime getDate(LocalDateTime defaultValue) { return propertyValue == null ? defaultValue : getDate(); }
    public void setDate(LocalDateTime propertyValue) { this.propertyValue = Data.dateToStr(propertyValue); }
    
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
    	updatedDts = LocalDateTime.now(); 
    	return result; 
    }
}
