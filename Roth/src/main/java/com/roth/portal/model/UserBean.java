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
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.StateBean;
import com.roth.portal.db.PortalUtil;
import com.roth.realm.RothRealm;

@JdbcTable(schema = "roth", name = "user", primaryKeyColumns = {"userid", "domain_id"})
@PermissiveBinding
public class UserBean implements Serializable, StateBean {
	private static final long serialVersionUID = -8317238518850899043L;

	private String userid;
	private Long domainId;
	private String domain;
	private String password;
	private String protect;
	private String locked;
    private LocalDateTime lockedDts;
    private String name;
	private LocalDateTime expireDts;
	private String passwordReset;
	private String createdBy;
	private LocalDateTime createdDts;
	private String updatedBy;
	private LocalDateTime updatedDts;
	private String[] groupNames;
	private String x590Thumbprint;
	
	private HashMap<String,String> passwordHistory;
	
	public String getUserid() { return userid; }
	public void setUserid(String userid) { this.userid = Data.trim(userid); }
	
	public Long getDomainId() { return domainId; }
	public void setDomainId(Long domainId) { this.domainId = domainId; }
	
	public String getDomain() { return domain; }
	public void setDomain(String domain) { this.domain = domain; }
	
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public void setPasswordNew(String password) { if (password != null) this.password = RothRealm.digest(password, "SHA3-512"); }
	
	public String getProtect() { return protect; }
	public void setProtect(String protect) { this.protect = protect; }
	
	public String getLocked() { return locked; }
	public void setLocked(String locked) { this.locked = locked; }
	
	public LocalDateTime getLockedDts() { return lockedDts; }
	public void setLockedDts(LocalDateTime lockedDts) { this.lockedDts = lockedDts; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = Data.trim(name); }
	
	public LocalDateTime getExpireDts() { return expireDts; }
	public void setExpireDts(LocalDateTime expireDts) { this.expireDts = expireDts; }
	
	public String getPasswordReset() { return passwordReset; }
	public void setPasswordReset(String passwordReset) { this.passwordReset = passwordReset; }
	
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	public LocalDateTime getCreatedDts() { return createdDts; }
	public void setCreatedDts(LocalDateTime createdDts) { this.createdDts = createdDts; }
	
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
	
	public LocalDateTime getUpdatedDts() { return updatedDts; }
	public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }
	
	public String getX590Thumbprint() { return x590Thumbprint; }
	public void setX590Thumbprint(String x590Thumbprint) { this.x590Thumbprint = x590Thumbprint; }
	
	public String sayHello(String name) { return "Hello, " + name; }

	public HashMap<String,String> getPasswordHistory() { 
		if (passwordHistory == null)
			try { passwordHistory = new PortalUtil().getPasswordHistory(userid, domainId); }
			catch (SQLException e) { Log.logException(e, userid); }
		if (passwordHistory == null)
			passwordHistory = new HashMap<String,String>();
		return passwordHistory; 
	}
	
	public String[] getGroupNames() { return groupNames; }
    public void setGroupNames(String[] groupNames) { this.groupNames = groupNames; }
    public void setGroupNameList(String groupNames) { this.groupNames = (groupNames == null) ? null : groupNames.split(","); }
	
	@Override
	public boolean isNew() { 
		boolean result = updatedDts == null; 
		updatedDts = LocalDateTime.now();
		if (createdBy == null)  {
			createdBy = updatedBy;
			createdDts = updatedDts;
		}
		if (protect == null) protect = "N";
		return result; 
	}
}
