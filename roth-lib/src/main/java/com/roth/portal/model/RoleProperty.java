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
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.util.Portal;

@JdbcTable(name = "role_property",
           primaryKeyColumns = {"role_name", "property_name"})
public class RoleProperty extends AbstractProperty {
    private static final long serialVersionUID = 1L;

    private String roleName;
        
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public static RoleProperty getRoleProperty(String userid, String roleName, String propertyName) throws SQLException {
    	TableUtil t = new TableUtil("roth");
    	String filter = t.applyParameters("role_name = {1} AND property_name = {2}", roleName, propertyName);
    	RoleProperty p = t.get(RoleProperty.class, filter);
    	if (p == null) {
    		p = new RoleProperty();
    		p.setRoleName(roleName);
    		p.setPropertyName(propertyName);
    		p.setUpdatedBy(userid);
    	}
    	return p;
    }
    
    public static String getAggregateRoleProperty(String userid, String propertyName) throws SQLException {
        return new Portal().getAggregateRoleProperty(userid, propertyName);
    }
    public static void setRoleProperty(RoleProperty p) throws SQLException { new TableUtil("roth").save(p); }
    public static void removeRoleProperty(RoleProperty p) throws SQLException { new TableUtil("roth").delete(p); }
}