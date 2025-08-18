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

import java.sql.SQLException;
import java.util.ArrayList;

import jakarta.servlet.annotation.WebServlet;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.DomainBean;
import com.roth.portal.model.UserBean;
import com.roth.portal.model.UserGroupBean;
import com.roth.portal.model.UserProfile;
import com.roth.portal.model.UserPropertyBean;
import com.roth.portal.model.UserRoleBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;

@WebServlet(urlPatterns = "/User/*")
@ActionServletSecurity(roles = {"SecurityAdmin", "DomainAdmin"})
@Navigation(contextPath = "/configuration",
		    simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class User extends ActionServlet {
	private static final long serialVersionUID = 1L;
	
	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp"),
			             @Forward(name = "ajax", path = "_list.jsp") },
			responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String load(ActionConnection conn) {
		try { 
			conn.getRequest().setAttribute("tabpage", "user");
			PortalUtil util = new PortalUtil();
			boolean securityAdmin = conn.getRequest().isUserInRole("SecurityAdmin");
			String where = securityAdmin ? "userid != 'anonymous'" : util.applyParameters("userid != 'anonymous' AND domain_id = {1}", util.getDomainId(conn.getDomainName()));
			
			Long domainId = conn.getLong("fDomainId");
			String userid = conn.getString("fUserid");
			String name = conn.getString("fName");
			//String roleName = conn.getString("fRoleName");
			
			if (domainId != null)
				where += util.applyParameters(" AND domain_id = {1}", domainId);
			if (!Data.isEmpty(userid))
			    where += util.applyParameters(" AND userid LIKE '%{sql: 1}%'", userid);
			if (!Data.isEmpty(name))
				where += util.applyParameters(" AND name LIKE '%{sql: 1}%'", name);
			
			putBean(util.getList(UserBean.class, where), "users", "request", conn);
			putBean(util.getDomains(), "domains", "request", conn);
			putBean(util.getRoles(), "roles", "request", conn);
		} 
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return isCallingActionName("begin", conn) ? "success" : "ajax";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") },
			responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String edit(ActionConnection conn) {
		try {
			String userid = conn.getString("userid");
			Long domainId = conn.getLong("domainId");
			PortalUtil util = new PortalUtil();
			boolean securityAdmin = conn.getRequest().isUserInRole("SecurityAdmin");
			if (!securityAdmin) domainId = util.getDomainId(conn.getDomainName());
			if (userid != null) {
				String filter = util.applyParameters("userid = {1} AND domain_id = {2}", userid, domainId);
				UserBean user = util.get(UserBean.class, filter);
				putBean(user, "user", "request", conn);
			}
			putBean(util.getDomains(), "domains", "request", conn);
			putBean(util.getDomainId(conn.getDomainName()), "domainId", "request", conn);
			putBean(UserProfile.getUserProfile(userid, domainId), "profile", "request", conn);
			DomainBean d = util.get(DomainBean.class, util.applyParameters("domain_id = {1}", domainId));
			putBean(d == null ? null : d.getPwdShelfLife(), "pwdShelfLife", "request", conn);
		}
		catch (SQLException e) { return returnLogException(e, conn, "failure", "Error loading user:<br/>", true); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") },
			responses = { @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "user", beanClass = UserBean.class, scope = "request"),
			        @Bean(name = "profile", beanClass = UserProfile.class, scope = "request") })
	public String save(ActionConnection conn) {
		try { 
			UserBean user = getBean(0, conn);
			UserProfile profile = getBean(1, conn);
			user.setUpdatedBy(conn.getUserName());
			PortalUtil util = new PortalUtil();
			boolean securityAdmin = conn.getRequest().isUserInRole("SecurityAdmin");
			if (!securityAdmin) user.setDomainId(util.getDomainId(conn.getDomainName()));
			util.save(user);
			profile.setUserid(user.getUserid());
			profile.setDomainId(user.getDomainId());
			profile.setUpdatedBy(getUserName(conn));
			UserProfile.setUserProfile(profile);
		}
		catch (SQLException e) { return returnLogException(e, conn, "failure", "Error saving user:<br/>", true); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load"),
			             @Forward(name = "return", path = "/configuration/index.jsp") },
			responses = { @Response(name = "failure", httpStatusCode = 500),
			              @Response(name = "admin", httpStatusCode = 403, message = "The 'admin' user cannot be deleted.") })
	public String delete(ActionConnection conn) {
		try { 
			UserBean user = new UserBean();
			user.setUserid(conn.getString("userid"));
			user.setDomainId(conn.getLong("domainId"));
			if (user.getUserid().equals("admin") && user.getDomainId() == 0)
				return "admin";
			new PortalUtil().delete(user); 
		}
		catch (SQLException e) { return returnLogException(e, conn, "failure", "Error deleting user:<br/>", true); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__groups.jsp") },
			responses = { @Response(name = "failure", httpStatusCode = 500) })
    public String loadGroups(ActionConnection conn) {
		try { 
			String userid = conn.getString("userid");
			Long domainId = conn.getLong("domainId");
			
			ArrayList<UserGroupBean> groups = new PortalUtil().getUserGroups(userid, domainId, null);
			if (groups != null)
				for (int i = 1; i < groups.size(); i++)
				    if (groups.get(i).getLevel() > 0)
				    	for (int j = 0; j < i; j++)
				    		if (groups.get(j).getLevel() == 0)
				    			groups.get(j).addChild(groups.get(i));
			
			putBean(groups, "userGroups", "request", conn);
		} 
		catch (SQLException e) { return returnLogException(e, conn, "failure", "Error loading groups:<br/>", true); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadGroups") })
	@Post(beans = { @Bean(name = "userGroups", scope = "request", beanClass = UserGroupBean.class, typeClass = ArrayList.class) })
	public String saveGroups(ActionConnection conn) {
		ArrayList<UserGroupBean> beans = getBean(0, conn);
		String userid = conn.getRequest().getParameter("userid");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(conn.getRequest().getUserPrincipal().getName());
		try { new PortalUtil().saveUserGroups(beans, userid, null); }
		catch (SQLException e) { return returnLogException(e, conn, "failure", "Error saving groups:<br/>", true); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__roles.jsp") })
    public String loadRoles(ActionConnection conn) {
		String userid = conn.getRequest().getParameter("userid");
		Long domainId = conn.getLong("domainId");
		try { 
			PortalUtil util = new PortalUtil();
			boolean filterByDomain = !conn.getRequest().isUserInRole("SecurityAdmin");
			conn.getRequest().setAttribute("userRoles", util.getUserRoles(userid, domainId, null, filterByDomain)); 
		} 
		catch (SQLException e) { return returnLogException(e, conn, "failure", "Error loading roles:<br/>", true); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadRoles") })
	@Post(beans = { @Bean(name = "userRoles", scope = "request", beanClass = UserRoleBean.class, typeClass = ArrayList.class) })
	public String saveRoles(ActionConnection conn) {
		ArrayList<UserRoleBean> beans = getBean(0, conn);
		String userid = conn.getRequest().getParameter("userid");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(conn.getRequest().getUserPrincipal().getName());
		try { new PortalUtil().saveUserRoles(beans, userid, null); }
		catch (SQLException e) { return returnLogException(e, conn, "failure", "Error saving roles:<br/>", true); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__properties.jsp") })
    public String loadProperties(ActionConnection conn) {
        String userid = conn.getRequest().getParameter("userid");
        Long domainId = conn.getLong("domainId");
        try {
        	PortalUtil util = new PortalUtil();
        	String filter = util.applyParameters("userid = {1} AND domain_id = {2}", userid, domainId);
        	conn.getRequest().setAttribute("userProperties", util.getList(UserPropertyBean.class, filter)); 
        } 
        catch (SQLException e) { return returnLogException(e, conn, "failure", "Error loading properties:<br/>", true); }
        return "success";
    }
	
	@Action(forwards = { @Forward(name = "success", path = "___editproperty.jsp") })
    public String editProperty(ActionConnection conn) {
        String userid = conn.getRequest().getParameter("userid");
        Long domainId = conn.getLong("domainId");
        String propertyName = conn.getRequest().getParameter("propertyName");
        if (propertyName != null)
            try { 
            	PortalUtil util = new PortalUtil();
                String filter = util.applyParameters("userid = {1} AND domain_id = {2} AND property_name = {3}", userid, domainId, propertyName);
                putBean(util.get(UserPropertyBean.class, filter), "userProperty", "request", conn); 
            }
        	catch (SQLException e) { Log.logException(e, conn.getUserName()); }
        else {
            UserPropertyBean bean = new UserPropertyBean();
            bean.setUserid(userid);
            bean.setDomainId(domainId);
            putBean(bean, "userProperty", "request", conn);
        }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", action = "loadProperties") })
    @Post(beans = { @Bean(name = "userProperty", scope = "request", beanClass = UserPropertyBean.class) })
    public String saveProperty(ActionConnection conn) {
        UserPropertyBean bean = getBean(0, conn);
        bean.setUpdatedBy(getUserName(conn));
        try { new PortalUtil().save(bean); }
        catch (SQLException e) { Log.logException(e, conn.getUserName()); }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", action = "loadProperties") })
    @Post(beans = { @Bean(name = "userProperty", scope = "request", beanClass = UserPropertyBean.class) })
    public String deleteProperty(ActionConnection conn) {
        UserPropertyBean bean = getBean(0, conn);
        try { new PortalUtil().delete(bean); }
        catch (SQLException e) { Log.logException(e, conn.getUserName()); }
        return "success";
    }
}
