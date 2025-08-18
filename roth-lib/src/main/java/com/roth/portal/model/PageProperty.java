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

import com.roth.jdbc.util.TableUtil;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.StateBean;

import java.sql.SQLException;
import java.time.LocalDateTime;

@JdbcTable(name = "page_property",
           primaryKeyColumns = {"book_id", "portlet_id", "property_name"})
public class PageProperty implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long bookId;
    private Long portletId;
    private String propertyName;
    private String propertyValue;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    
    public Long getPortletId() { return portletId; }
    public void setPortletId(Long portletId) { this.portletId = portletId; }

    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }

    public String getPropertyValue() { return propertyValue; }
    public void setPropertyValue(String propertyValue) { this.propertyValue = propertyValue; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    public static PageProperty getPageProperty(Long bookId, Long portletId, String propertyName) throws SQLException {
    	TableUtil t = new TableUtil("roth");
    	String filter = "book_id = {1} AND portlet_id = {2} AND property_name = {3}";
    	filter = t.applyParameters(filter, bookId, portletId, propertyName);
    	PageProperty p = t.get(PageProperty.class, filter);
    	if (p == null) {
    		p = new PageProperty();
    		p.setBookId(bookId);
    		p.setPortletId(portletId);
    		p.setPropertyName(propertyName);
    		p.setUpdatedBy("");
    	}
    	return p;
    }
    
    public static void setUserProperty(PageProperty p) throws SQLException {
    	TableUtil t = new TableUtil("roth");
    	t.save(p);
    }
    
    public static void removeUserProperty(PageProperty p) throws SQLException {
    	TableUtil t = new TableUtil("roth");
    	t.delete(p);
    }
    
    @Override
    public boolean isNew() { 
    	boolean result = updatedDts == null; 
    	updatedDts = LocalDateTime.now(); 
    	return result; 
    }
}