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

@JdbcTable(name = "desktop_link",
           primaryKeyColumns = {"desktop_id", "sequence"})
public class DesktopLinkBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long desktopId;
    private Long sequence;
    private String linkTitle;
    private String linkUri;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public Long getDesktopId() { return desktopId; }
    public void setDesktopId(Long desktopId) { this.desktopId = desktopId; }

    public Long getSequence() { return sequence; }
    public void setSequence(Long sequence) { this.sequence = sequence; }

    public String getLinkTitle() { return linkTitle; }
    public void setLinkTitle(String linkTitle) { this.linkTitle = linkTitle; }

    public String getLinkUri() { return linkUri; }
    public void setLinkUri(String linkUri) { this.linkUri = linkUri; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    @Override
    public boolean isNew() { boolean result = updatedDts == null; updatedDts = LocalDateTime.now(); return result; }
}