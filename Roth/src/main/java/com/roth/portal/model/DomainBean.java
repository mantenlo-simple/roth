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
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "domain",
           primaryKeyColumns = {"domain_id"})
@PermissiveBinding
public class DomainBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private String description;
    private Long domainId;
    private String domainName;
    private Integer lockoutCount;
    private Integer lockoutTime;
    private String pwdAllowReset;
    private Integer pwdChallengeMin;
    private Integer pwdMaxExpiredAge;
    private Integer pwdMinLength;
    private Integer pwdRememberCount;
    private String pwdRequireMixed;
    private String pwdRequireNumber;
    private String pwdRequireSpecial;
    private Integer pwdShelfLife;
    private String pwdSource;
    private Integer sessionTimeout;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }

    public Integer getLockoutCount() { return Data.nvl(lockoutCount, 5); }
	public void setLockoutCount(Integer lockoutCount) { this.lockoutCount = lockoutCount; }
	
	public Integer getLockoutTime() { return Data.nvl(lockoutTime, 5); }
	public void setLockoutTime(Integer lockoutTime) { this.lockoutTime = lockoutTime; }
	
	public String getPwdAllowReset() { return pwdAllowReset; }
    public void setPwdAllowReset(String pwdAllowReset) { this.pwdAllowReset = pwdAllowReset; }
    
    public Integer getPwdChallengeMin() { return pwdChallengeMin; }
    public void setPwdChallengeMin(Integer pwdChallengeMin) { this.pwdChallengeMin = pwdChallengeMin; }
    
    public Integer getPwdMaxExpiredAge() { return pwdMaxExpiredAge; }
	public void setPwdMaxExpiredAge(Integer pwdMaxExpiredAge) { this.pwdMaxExpiredAge = pwdMaxExpiredAge; }
	
	public Integer getPwdMinLength() { return pwdMinLength; }
	public void setPwdMinLength(Integer pwdMinLength) { this.pwdMinLength = pwdMinLength; }
	
	public Integer getPwdRememberCount() { return pwdRememberCount; }
	public void setPwdRememberCount(Integer pwdRememberCount) { this.pwdRememberCount = pwdRememberCount; }
	
	public String getPwdRequireMixed() { return pwdRequireMixed; }
	public void setPwdRequireMixed(String pwdRequireMixed) { this.pwdRequireMixed = pwdRequireMixed; }
	
	public String getPwdRequireNumber() { return pwdRequireNumber; }
	public void setPwdRequireNumber(String pwdRequireNumber) { this.pwdRequireNumber = pwdRequireNumber; }
	
	public String getPwdRequireSpecial() { return pwdRequireSpecial; }
	public void setPwdRequireSpecial(String pwdRequireSpecial) { this.pwdRequireSpecial = pwdRequireSpecial; }
	
	public Integer getPwdShelfLife() { return pwdShelfLife; }
	public void setPwdShelfLife(Integer pwdShelfLife) { this.pwdShelfLife = pwdShelfLife; }
	
	public Integer getSessionTimeout() { return sessionTimeout; }
	public void setSessionTimeout(Integer sessionTimeout) { this.sessionTimeout = sessionTimeout; }
	
	/* pwdSource is only used when the password is not validated against the portal user table.
	 * The valid source strings could be: 
	 *   JNDI://datasourcename/tablename(userfieldname:passwordfieldname);encryption
	 *     example: JNDI://roth/user(userid:password);SHA
	 * or 
	 *   LDAP://hostname/pathstring
	 *     example: LDAP://ldap.host.com/CN=Users,DC=ldap,DC=host,DC=com
	 */
	public String getPwdSource() { return pwdSource; }
	public void setPwdSource(String pwdSource) { this.pwdSource = pwdSource; }
	
	public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    @Override
    public boolean isNew() { 
    	boolean result = domainId == null; 
    	if (result) domainId = Long.valueOf(-1);
    	updatedDts = LocalDateTime.now();
    	return result;
    }
}