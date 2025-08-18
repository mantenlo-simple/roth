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

@JdbcTable(name = "user_profile",
           primaryKeyColumns = {"userid", "domain_id"})
public class UserProfileBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long domainId;
    private String emailWork;
    private String phoneCell;
    private String phoneWork;
    private String updatedBy;
    private LocalDateTime updatedDts;
    private String userid;
    private String userImage;

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public String getEmailWork() { return emailWork; }
    public void setEmailWork(String emailWork) { this.emailWork = emailWork; }

    public String getPhoneCell() { return phoneCell; }
    public void setPhoneCell(String phoneCell) { this.phoneCell = phoneCell; }

    public String getPhoneWork() { return phoneWork; }
    public void setPhoneWork(String phoneWork) { this.phoneWork = phoneWork; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    public String getUserImage() { return userImage; }
    public void setUserImage(String userImage) { this.userImage = userImage; }

    @Override
    public boolean isNew() { return userid == null || domainId == null; }
}