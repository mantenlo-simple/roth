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
package com.roth.realm;

import java.util.List;

import org.apache.catalina.realm.GenericPrincipal;

public class RothPrincipal extends GenericPrincipal {
	private static final long serialVersionUID = 6662716220097062016L;
	private Integer daysToExpire;
	private String userid;
	private Long domainId;
	private String domainName;
	private String sslId;
	
	public RothPrincipal(String name, List<String> roles, String userid, Long domainId, String domainName) {
		super(name, roles);
		this.userid = userid; 
		this.domainId = domainId;
		this.domainName = domainName;
	}
	
	public Integer getDaysToExpire() { return daysToExpire; }
	public void setDaysToExpire(Integer daysToExpire) { this.daysToExpire = daysToExpire; }
	
	public String getUserName() { return getUserPrincipal().getName(); } 
	public String getUserid() { return userid; }
	public Long getDomainId() { return domainId; }
	public String getDomainName() { return domainName; }
	
	public String getSslId() { return sslId; }
	public void setSslId(String sslId) { this.sslId = sslId; }
}
