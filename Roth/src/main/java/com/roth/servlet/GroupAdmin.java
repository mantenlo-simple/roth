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
package com.roth.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import jakarta.servlet.annotation.WebServlet;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.groupadmin.db.GroupAdminUtil;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.UserBean;
import com.roth.portal.model.UserDomainBean;
import com.roth.portal.model.UserGroupBean;
import com.roth.portal.model.UserProfile;
import com.roth.portal.model.UserRoleBean;
import com.roth.servlet.annotation.ActionServletSecurity;

@WebServlet(urlPatterns = "/GroupAdmin/*")
@ActionServletSecurity(roles = "GroupAdmin")
public class GroupAdmin extends ActionServlet {
	private static final long serialVersionUID = 1L;

	protected String setToCsv(Set<String> set) {
	    String result = "";
	    Iterator<String> i = set.iterator();
	    while (i.hasNext()) result += (Data.isEmpty(result) ? "" : ",") + i.next();
	    return result;
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_index.jsp"),
	                     @Forward(name = "reload", path = "_users.jsp") })
	public String begin(ActionConnection conn) {
	    try {
	    	GroupAdminUtil util = new GroupAdminUtil();
	        putBean(util.getUsers(conn.getUserName()), "users", "request", conn);
	        LinkedHashMap<String,String> groups = util.getGroups(getUserName(conn));
	        putBean(groups, "groups", "request", conn);
	        putBean(new PortalUtil().getDomains(), "domains", "request", conn);
	        if (groups != null)  putBean(util.getRoles(setToCsv(groups.keySet())), "roles", "request", conn);
	    }
	    catch (SQLException e) { e.printStackTrace(); }
	    return Data.nvl(getCallingActionName(conn)).equals("save") ? "reload" : "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "user/_edit.jsp") })
	public String edit(ActionConnection conn) {
	    try {
	        String userid = conn.getString("userid");
	        Long domainId = conn.getLong("domainId");
	        GroupAdminUtil util = new GroupAdminUtil();
	        String filter = util.applyParameters("userid = {1} and domain_id = {2}", userid, domainId);
	        UserBean user = (userid == null) ? new UserBean() : (UserBean)util.get(UserBean.class, filter); 
	        putBean(user, "user", "request", conn);
	        putBean(new PortalUtil().getDomainId(conn.getDomainName()), "domainId", "request", conn);
	        putBean(new PortalUtil().getDomains(), "domains", "request", conn);
	        putBean(UserProfile.getUserProfile(userid, domainId), "profile", "request", conn);
	    }
	    catch (SQLException e) { e.printStackTrace(); }
	    return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "begin") },
	        responses = { @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "user", scope = "request", beanClass = UserBean.class),
	                @Bean(name = "profile", beanClass = UserProfile.class, scope = "request") })
    public String save(ActionConnection conn) {
	    UserBean user = getBean(0, conn);
	    UserProfile profile = getBean(1, conn);
        try {
            GroupAdminUtil util = new GroupAdminUtil();
            boolean isNew = user.getUpdatedDts() == null;
            if (!isNew && (user.getPassword() == null)) {
                UserBean bean = util.get(UserBean.class, util.applyParameters("userid = {1} AND domain_id = {2}", user.getUserid(), user.getDomainId()));
                user.setPassword(bean.getPassword());
            }
            user.setProtect("N");
            user.setUpdatedBy(getUserName(conn));
            util.save(user);
            profile.setUserid(user.getUserid());
			profile.setDomainId(user.getDomainId());
			profile.setUpdatedBy(getUserName(conn));
			UserProfile.setUserProfile(profile);
        }
        catch (SQLException e) { 
            String errorMessage = "<span>An unknown error has occurred.</span>";
            if (e.getMessage().contains("Duplicate entry")) 
                errorMessage = "<span>A user with the specified ID already exists.</span>";
            else 
                e.printStackTrace();
            try { 
                conn.getResponse().getWriter().write(errorMessage); 
                return "failure"; 
            }
            catch (IOException e2) { e2.printStackTrace(); }
        }
        return "success";
    }
	
	@Action(forwards = { @Forward(name = "success", action = "begin") })
    public String delete(ActionConnection conn) {
	    // Hmmm... probably dont' want to do this here...
        return "success";
    }
	
	// ------- Domains -------
	@Action(forwards = { @Forward(name = "success", path = "user/__domains.jsp") })
    public String loadDomains(ActionConnection conn) {
		String userid = conn.getString("userid");
		try { conn.getRequest().setAttribute("userDomains", new GroupAdminUtil().getUserDomains(userid, conn.getUserName())); } 
		catch (Exception e) { Log.logException(e, conn.getUserName()); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadDomains") })
	@Post(beans = { @Bean(name = "userDomains", scope = "request", beanClass = UserDomainBean.class, typeClass = ArrayList.class) })
	public String saveDomains(ActionConnection conn) {
		ArrayList<UserDomainBean> beans = getBean(0, conn);
		String userid = conn.getString("userid");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setUpdatedBy(getUserName(conn));
		try { new PortalUtil().saveUserDomains(beans, userid, null); }
		catch (SQLException e) { Log.logException(e, conn.getUserName()); }
    	return "success";
	}
	
	// ------- Groups --------
	@Action(forwards = { @Forward(name = "success", path = "user/__groups.jsp") })
	public String loadGroups(ActionConnection conn) {
	    try {
	        GroupAdminUtil util = new GroupAdminUtil();
	        putBean(util.getGroups(getUserName(conn)), "groups", "request", conn);
	        String userid = conn.getString("userid");
	        
	        ArrayList<UserGroupBean> groups = util.getUserGroups(getUserName(conn), userid);
			if (groups != null)
				for (int i = 1; i < groups.size(); i++)
				    if (groups.get(i).getLevel() > 0)
				    	for (int j = 0; j < i; j++)
				    		if (groups.get(j).getLevel() == 0)
				    			groups.get(j).addChild(groups.get(i));
			
			putBean(groups, "userGroups", "request", conn);
	    }
	    catch (SQLException e) { e.printStackTrace(); }
	    return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadGroups") })
    @Post(beans = { @Bean(name = "userGroups", scope = "request", beanClass = UserGroupBean.class, typeClass = ArrayList.class) })
    public String saveGroups(ActionConnection conn) {
        ArrayList<UserGroupBean> beans = getBean(0, conn);
        String userid = conn.getString("userid");
        for (int i = 0; i < beans.size(); i++)
            if (beans.get(i) != null)
                beans.get(i).setCreatedBy(conn.getRequest().getUserPrincipal().getName());
        try { new GroupAdminUtil().saveUserGroups(beans, userid, null); }
        catch (SQLException e) { e.printStackTrace(); }
        return "success";
    }
	
	// ------- Roles --------
    @Action(forwards = { @Forward(name = "success", path = "user/__roles.jsp") })
    public String loadRoles(ActionConnection conn) {
        try {
        	GroupAdminUtil util = new GroupAdminUtil();
            LinkedHashMap<String,String> groups = util.getGroups(getUserName(conn));
            String userid = conn.getRequest().getParameter("userid");
            if (groups != null) putBean(util.getUserRoles(setToCsv(groups.keySet()), userid), "userRoles", "request", conn);
        }
        catch (SQLException e) { e.printStackTrace(); }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", action = "loadRoles") })
    @Post(beans = { @Bean(name = "userRoles", scope = "request", beanClass = UserRoleBean.class, typeClass = ArrayList.class) })
    public String saveRoles(ActionConnection conn) {
        ArrayList<UserRoleBean> beans = getBean(0, conn);
        String userid = conn.getRequest().getParameter("userid");
        for (int i = 0; i < beans.size(); i++)
            beans.get(i).setCreatedBy(conn.getRequest().getUserPrincipal().getName());
        try { new GroupAdminUtil().saveUserRoles(beans, userid, null); }
        catch (SQLException e) { e.printStackTrace(); }
        return "success";
    }
    
    // ------- Properties --------
    @Action(forwards = { @Forward(name = "success", path = "user/__properties.jsp") })
    public String loadProperties(ActionConnection conn) {
        return "success";
    }
}
 