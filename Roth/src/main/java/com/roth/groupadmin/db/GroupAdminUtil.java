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
package com.roth.groupadmin.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.roth.jdbc.annotation.ConnectionDataSource;
import com.roth.jdbc.annotation.SQLFileContext;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.UserBean;
import com.roth.portal.model.UserDomainBean;
import com.roth.portal.model.UserGroupBean;
import com.roth.portal.model.UserRoleBean;

@ConnectionDataSource(jndiName = "roth")
@SQLFileContext(path = "/com/roth/groupadmin/sql")
public class GroupAdminUtil extends TableUtil {
    private static final long serialVersionUID = -8334730839718441597L;

    public GroupAdminUtil() throws SQLException { super(); }
    
    public ArrayList<UserBean> getUsers(String userid) throws SQLException {
    	int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
    	Long domainId = new PortalUtil().getDomainId(_domain);
        String statement = getSQLFile("getUsers.sql");
        statement = applyParameters(statement, _userid, domainId);
        return execQuery(statement, ArrayList.class, UserBean.class);
    }
    
    public LinkedHashMap<String,String> getGroups(String userid) throws SQLException {
    	int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
    	Long domainId = new PortalUtil().getDomainId(_domain);
        String statement = getSQLFile("getGroups.sql");
        statement = applyParameters(statement, _userid, domainId);
        return execQuery(statement, LinkedHashMap.class);
    }
    
    public String[] getRoles(String groupIds) throws SQLException {
        String statement = getSQLFile("getRoles.sql");
        statement = applyParameters(statement, groupIds);
        return execQuery(statement, String[].class);
    }
    
    // Get User-Domains
 	public ArrayList<UserDomainBean> getUserDomains(String userid, String adminUserId) throws SQLException {
 		String statement = getSQLFile("getUserDomains.sql");
 		statement = applyParameters(statement, userid, adminUserId);
 		return execQuery(statement, ArrayList.class, UserDomainBean.class);
 	}
 	
 	// Save User-Domains
 	public void saveUserDomains(ArrayList<UserDomainBean> beans, String userid, Long domainId) throws SQLException {
 		ArrayList<UserDomainBean> deletes = new ArrayList<UserDomainBean>();
 		ArrayList<UserDomainBean> inserts = new ArrayList<UserDomainBean>();
 		
 		for (int i = 0; i < beans.size(); i++) {
 			UserDomainBean bean = beans.get(i);
 			Object key = (userid != null) ? bean.getUserid() : bean.getDomainId();
 			
 			if ((key != null) && (bean.getUpdatedDts() == null))
 				inserts.add(bean);
 			else if ((key == null) && (bean.getUpdatedDts() != null)) { 
 				deletes.add(bean);
 				if (userid != null) bean.setUserid(userid);
 				else bean.setDomainId(domainId);
 			}
 		}
 		
 		if (deletes.size() > 0) delete(deletes);
 		if (inserts.size() > 0) save(inserts);
 	}
 	
 	public ArrayList<UserGroupBean> getUserGroups(String mgrId, String userid) throws SQLException {
 		int dPos = mgrId.indexOf("@");
        String _domain = dPos < 0 ? "default" : mgrId.substring(dPos + 1);
        String _mgrid = dPos < 0 ? mgrId : mgrId.substring(0, dPos);
    	Long domainId = new PortalUtil().getDomainId(_domain);
        String statement = getSQLFile("getUserGroups.sql");
        statement = applyParameters(statement, _mgrid, domainId, userid);
        return execQuery(statement, ArrayList.class, UserGroupBean.class);
    }
    
    public void saveUserGroups(ArrayList<UserGroupBean> beans, String userid, Long groupId) throws SQLException {
        ArrayList<UserGroupBean> deletes = new ArrayList<UserGroupBean>();
        ArrayList<UserGroupBean> inserts = new ArrayList<UserGroupBean>();
        
        for (int i = 0; i < beans.size(); i++) {
            UserGroupBean bean = beans.get(i);
            if (bean == null) continue;
            Object key = (userid != null) ? bean.getUserid() : bean.getGroupId();
            
            if ((key != null) && (bean.getCreatedDts() == null))
                inserts.add(bean);
            else if ((key == null) && (bean.getCreatedDts() != null)) { 
                deletes.add(bean);
                if (userid != null) bean.setUserid(userid);
                else bean.setGroupId(groupId);
            }
        }
        
        if (deletes.size() > 0) delete(deletes);
        if (inserts.size() > 0) save(inserts);
    }
    
    public ArrayList<UserRoleBean> getUserRoles(String groupIds, String userid) throws SQLException {
    	int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
    	Long domainId = new PortalUtil().getDomainId(_domain);
        String statement = getSQLFile("getUserRoles.sql");
        statement = applyParameters(statement, groupIds, _userid, domainId);
        return execQuery(statement, ArrayList.class, UserRoleBean.class);
    }
    
    public void saveUserRoles(ArrayList<UserRoleBean> beans, String userid, String roleName) throws SQLException {
        ArrayList<UserRoleBean> deletes = new ArrayList<UserRoleBean>();
        ArrayList<UserRoleBean> inserts = new ArrayList<UserRoleBean>();
        
        for (int i = 0; i < beans.size(); i++) {
            UserRoleBean bean = beans.get(i);
            String key = (userid != null) ? bean.getUserid() : bean.getRoleName();
            
            if ((key != null) && (bean.getCreatedDts() == null))
                inserts.add(bean);
            else if ((key == null) && (bean.getCreatedDts() != null)) { 
                deletes.add(bean);
                if (userid != null) bean.setUserid(userid);
                else bean.setRoleName(roleName);
            }
        }
        
        if (deletes.size() > 0) delete(deletes);
        if (inserts.size() > 0) save(inserts);
    }
}
