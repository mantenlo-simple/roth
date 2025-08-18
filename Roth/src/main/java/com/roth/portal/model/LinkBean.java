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

@JdbcTable(name = "link",
           primaryKeyColumns = {"link_id"})
public class LinkBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long linkId;
    private String linkName;
    private String linkTitle;
    private String linkUri;
    private String linkIcon;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public Long getLinkId() { return linkId; }
    public void setLinkId(Long linkId) { this.linkId = linkId; }

    public String getLinkName() { return linkName; }
    public void setLinkName(String linkName) { this.linkName = linkName; }

    public String getLinkTitle() { return linkTitle; }
    public void setLinkTitle(String linkTitle) { this.linkTitle = linkTitle; }

    public String getLinkUri() { return linkUri; }
    public void setLinkUri(String linkUri) { this.linkUri = linkUri; }

    public String getLinkIcon() { return linkIcon; }
    public void setLinkIcon(String linkIcon) { this.linkIcon = linkIcon; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    @Override
    public boolean isNew() { 
    	setUpdatedDts(LocalDateTime.now()); 
    	boolean result = linkId == null; 
    	if (result) linkId = Long.valueOf(-1); 
    	return result; 
    }
}