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

import com.roth.jdbc.util.TableUtil;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.DomainRoleBean;
import com.roth.portal.model.GroupRoleBean;
import com.roth.portal.model.PortletRoleBean;
import com.roth.portal.model.RoleBean;
import com.roth.portal.model.RolePropertyBean;
import com.roth.portal.model.UserRoleBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;
import com.roth.tags.el.Util;

@WebServlet(urlPatterns = "/Role/*")
@ActionServletSecurity(roles = "SecurityAdmin")
@Navigation(contextPath = "/configuration",
	        simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class Role extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp"),
                         @Forward(name = "ajax", path = "_list.jsp") })
    public String load(ActionConnection conn) {
		conn.getRequest().setAttribute("tabpage", "role");
		try {
			PortalUtil util = new PortalUtil();
			String where = "role_name NOT IN ('Anonymous', 'Authenticated')";
			
			String roleName = conn.getString("fRoleName");
			
			if (roleName != null)
				where += util.applyParameters(" AND role_name LIKE '%{sql: 1}%'", roleName);
			
			conn.getRequest().setAttribute("roles", util.getList(RoleBean.class, where)); 
		} 
		catch (Exception e) { e.printStackTrace(); }
		return isCallingActionName("begin", conn) ? "success" : "ajax";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") })
	public String edit(ActionConnection conn) {
		RoleBean bean = new RoleBean();
		String roleName = conn.getRequest().getParameter("roleName");
		if (roleName != null)
			try {
				TableUtil util = new TableUtil("roth");
				bean = util.get(RoleBean.class, util.applyParameters("role_name = {1}", roleName)); 
			}
			catch (SQLException e) { e.printStackTrace(); }
		putBean(bean, "role", "request", conn);
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") })
	@Post(beans = { @Bean(name = "role", beanClass = RoleBean.class, scope = "request") })
	public String save(ActionConnection conn) {
		RoleBean role = getBean(0, conn);
		role.setUpdatedBy(getUserName(conn));
		try { new PortalUtil().save(role); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load"),
			             @Forward(name = "return", path = "/configuration/index.jsp") },
			responses = { @Response(name = "protected", httpStatusCode = 403, message = "Built-in roles cannot be deleted.") })
	public String delete(ActionConnection conn) {
		RoleBean role = new RoleBean();
		role.setRoleName(conn.getString("roleName"));
		String[] protectedRoles = {"GroupAdmin", "Developer", "DomainAdmin", "ExternalUser", "InternalUser",
				                   "manager-gui", "PortalAdmin", "ReportAdmin", "SecurityAdmin", "SystemAdmin"};
		if (Util.in(role.getRoleName(), protectedRoles)) 
			return "protected";
		try { new PortalUtil().delete(role); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__domains.jsp") })
    public String loadDomains(ActionConnection conn) {
		String roleName = conn.getRequest().getParameter("roleName");
		try { 
			PortalUtil util = new PortalUtil();
			conn.getRequest().setAttribute("roleDomains", util.getDomainRoles(null, roleName));
		} 
		catch (Exception e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadDomains") })
	@Post(beans = { @Bean(name = "roleDomains", scope = "request", beanClass = DomainRoleBean.class, typeClass = ArrayList.class) })
	public String saveDomains(ActionConnection conn) {
		ArrayList<DomainRoleBean> beans = getBean(0, conn);
		String roleName = conn.getRequest().getParameter("roleName");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(getUserName(conn));
		try { new PortalUtil().saveDomainRoles(beans, null, roleName); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__users.jsp") })
    public String loadUsers(ActionConnection conn) {
		String roleName = conn.getRequest().getParameter("roleName");
		try { 
			PortalUtil util = new PortalUtil();
			conn.getRequest().setAttribute("roleUsers", util.getUserRoles(null, null, roleName, false));
			putBean(util.getDomains(), "domains", "request", conn);
		} 
		catch (Exception e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadUsers") })
	@Post(beans = { @Bean(name = "roleUsers", scope = "request", beanClass = UserRoleBean.class, typeClass = ArrayList.class) })
	public String saveUsers(ActionConnection conn) {
		ArrayList<UserRoleBean> beans = getBean(0, conn);
		String roleName = conn.getRequest().getParameter("roleName");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(getUserName(conn));
		try { new PortalUtil().saveUserRoles(beans, null, roleName); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__groups.jsp") })
    public String loadGroups(ActionConnection conn) {
		String roleName = conn.getRequest().getParameter("roleName");
		try {
			ArrayList<GroupRoleBean> groups = new PortalUtil().getGroupRoles(null, roleName, null);
			if (groups != null)
				for (int i = 1; i < groups.size(); i++)
				    if (groups.get(i).getLevel() > 0)
				    	for (int j = 0; j < i; j++)
				    		if (groups.get(j).getLevel() == 0)
				    			groups.get(j).addChild(groups.get(i));
			
			putBean(groups, "roleGroups", "request", conn);
		} 
		catch (Exception e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadGroups") })
	@Post(beans = { @Bean(name = "roleGroups", scope = "request", beanClass = GroupRoleBean.class, typeClass = ArrayList.class) })
	public String saveGroups(ActionConnection conn) {
		ArrayList<GroupRoleBean> beans = getBean(0, conn);
		String roleName = conn.getRequest().getParameter("roleName");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(getUserName(conn));
		try { new PortalUtil().saveGroupRoles(beans, null, roleName); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__portlets.jsp") })
    public String loadPortlets(ActionConnection conn) {
		String roleName = conn.getRequest().getParameter("roleName");
		try { conn.getRequest().setAttribute("rolePortlets", new PortalUtil().getPortletRoles(roleName, null)); } 
		catch (Exception e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadPortlets") })
	@Post(beans = { @Bean(name = "rolePortlets", scope = "request", beanClass = PortletRoleBean.class, typeClass = ArrayList.class) })
	public String savePortlets(ActionConnection conn) {
		ArrayList<PortletRoleBean> beans = getBean(0, conn);
		String roleName = conn.getRequest().getParameter("roleName");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(getUserName(conn));
		try { new PortalUtil().savePortletRoles(beans, roleName, null); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__properties.jsp") })
    public String loadProperties(ActionConnection conn) {
	    String roleName = conn.getRequest().getParameter("roleName");
        try { 
        	PortalUtil util = new PortalUtil();
        	String filter = util.applyParameters("role_name = {1}", roleName);
        	conn.getRequest().setAttribute("roleProperties", util.getList(RolePropertyBean.class, filter)); 
        } 
        catch (Exception e) { e.printStackTrace(); }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", path = "___editproperty.jsp") })
    public String editProperty(ActionConnection conn) {
        String roleName = conn.getRequest().getParameter("roleName");
        String propertyName = conn.getRequest().getParameter("propertyName");
        if (propertyName != null)
            try { 
            	PortalUtil util = new PortalUtil();
            	String filter = util.applyParameters("role_name = {1} AND property_name = {2}", roleName, propertyName);
            	putBean(util.get(RolePropertyBean.class, filter), "roleProperty", "request", conn); 
            }
            catch (SQLException e) { e.printStackTrace(); }
        else {
            RolePropertyBean bean = new RolePropertyBean();
            bean.setRoleName(roleName);
            putBean(bean, "roleProperty", "request", conn);
        }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", action = "loadProperties") })
    @Post(beans = { @Bean(name = "roleProperty", scope = "request", beanClass = RolePropertyBean.class) })
    public String saveProperty(ActionConnection conn) {
        RolePropertyBean bean = getBean(0, conn);
        bean.setUpdatedBy(getUserName(conn));
        try { new PortalUtil().save(bean); }
        catch (SQLException e) { e.printStackTrace(); }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", action = "loadProperties") })
    @Post(beans = { @Bean(name = "roleProperty", scope = "request", beanClass = RolePropertyBean.class) })
    public String deleteProperty(ActionConnection conn) {
        RolePropertyBean bean = getBean(0, conn);
        try { new PortalUtil().delete(bean); }
        catch (SQLException e) { e.printStackTrace(); }
        return "success";
    }
}
