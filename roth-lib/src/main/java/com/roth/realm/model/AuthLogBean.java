package com.roth.realm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "auth_log",
           primaryKeyColumns = {"auth_log_id"})
@PermissiveBinding()
public class AuthLogBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Long authLogId;
    private Long domainId;
    private LocalDateTime eventDts;
    private Integer pwdHash;
    private String remoteHost;
    private String status;
    private String userid;
    
    public AuthLogBean() {}
    public AuthLogBean(String userid, Long domainId, Integer pwdHash, String remoteHost, boolean validated) {
    	this.userid = userid;
    	this.domainId = domainId;
    	this.pwdHash = pwdHash;
    	this.remoteHost = remoteHost;
    	status = validated ? "S" : "F";
    	eventDts = LocalDateTime.now();
    }
    
    public Long getAuthLogId() { return authLogId; }
	public void setAuthLogId(Long authLogId) { this.authLogId = authLogId; }
	
	public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public LocalDateTime getEventDts() { return eventDts; }
	public void setEventDts(LocalDateTime eventDts) { this.eventDts = eventDts; }
	
	public Integer getPwdHash() { return pwdHash; }
	public void setPwdHash(Integer pwdHash) { this.pwdHash = pwdHash; }
	
	public String getRemoteHost() { return remoteHost; }
    public void setRemoteHost(String remoteHost) { this.remoteHost = remoteHost; }

    public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	
	public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    @Override
    public boolean isNew() {
    	boolean result = authLogId == null;
    	if (result)
    		authLogId = -1L;
    	if (domainId == null) domainId = -999999999L;
    	return result; 
    }
}