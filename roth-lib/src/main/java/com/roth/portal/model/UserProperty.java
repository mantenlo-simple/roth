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

import java.sql.SQLException;

import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.util.Portal;

@JdbcTable(name = "user_property",
           primaryKeyColumns = {"userid", "domain_id", "property_name"})
@PermissiveBinding
public class UserProperty extends AbstractProperty {
    private static final long serialVersionUID = 1L;

    private String userid;
    private Long domainId;
    
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }
    
    public static UserProperty getUserProperty(String userid, String propertyName) throws SQLException {
    	String _userid = userid.split("@")[0];
    	String domainName = (_userid.equals(userid)) ? "default" : userid.split("@")[1];
    	return getUserProperty(_userid, new Portal().getDomainId(domainName), propertyName);
    }
    
    public static UserProperty getUserProperty(String userid, Long domainId, String propertyName) throws SQLException {
    	TableUtil t = new TableUtil("roth");
    	String filter = t.applyParameters("userid = {1} AND domain_id = {2} AND property_name = {3}", userid, domainId, propertyName);
    	UserProperty p = t.get(UserProperty.class, filter);
    	if (p == null) {
    		p = new UserProperty();
    		p.setUserid(userid);
    		p.setDomainId(domainId);
    		p.setPropertyName(propertyName);
    		p.setUpdatedBy(userid);
    	}
    	return p;
    }
    public static void setUserProperty(UserProperty p) throws SQLException { new TableUtil("roth").save(p); }
    public static void removeUserProperty(UserProperty p) throws SQLException { new TableUtil("roth").delete(p); }
}