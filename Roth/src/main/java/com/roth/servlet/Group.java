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

import com.roth.base.util.Data;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.GroupBean;
import com.roth.portal.model.GroupRoleBean;
import com.roth.portal.model.UserGroupBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;

@WebServlet(urlPatterns = "/Group/*")
@ActionServletSecurity(roles = {"SecurityAdmin", "DomainAdmin"})
@Navigation(contextPath = "/configuration",
            simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class Group extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp"),
                         @Forward(name = "ajax", path = "_list.jsp") })
	public String load(ActionConnection conn) {
		conn.getRequest().setAttribute("tabpage", "group");
		try {
			boolean securityAdmin = conn.getRequest().isUserInRole("SecurityAdmin");
			PortalUtil util = new PortalUtil();
			String where = securityAdmin ? "1 = 1" : util.applyParameters("domain_id = {1}", util.getDomainId(conn.getDomainName()));

			Long domainId = conn.getLong("fDomainId");
			String groupName = conn.getString("fGroupName");
			//String roleName = conn.getString("fRoleName");
			
			if (domainId != null)
				where += util.applyParameters(" AND domain_id = {1}", domainId);
			if (!Data.isEmpty(groupName))
			    where += util.applyParameters(" AND group_name LIKE '%{sql: 1}%'", groupName);
			
			ArrayList<GroupBean> groups = util.getList(GroupBean.class, where, "get_group_sort(group_id)", null);
			if (groups != null)
				for (int i = 1; i < groups.size(); i++)
				    if (groups.get(i).getLevel() > 0)
				    	for (int j = 0; j < i; j++)
				    		if (groups.get(j).getLevel() == 0)
				    			groups.get(j).addChild(groups.get(i));
			conn.getRequest().setAttribute("groups", groups);
			putBean(util.getDomains(), "domains", "request", conn);
		} 
		catch (Exception e) { e.printStackTrace(); }
		return isCallingActionName("begin", conn) ? "success" : "ajax";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") })
	public String edit(ActionConnection conn) {
		GroupBean bean = new GroupBean();
		Long groupId = Data.strToLong(conn.getRequest().getParameter("groupId"));
		String groupName = conn.getRequest().getParameter("groupName");
		String params = conn.getRequest().getParameter("_params");
		Long parentGroupId = (params == null) ? null : Data.strToLong(Data.parseEncodedParam(params, "groupId"));
		if (parentGroupId != null) bean.setParentGroupId(parentGroupId);
		try {
			PortalUtil util = new PortalUtil();
			if (groupId != null)
				bean = new TableUtil("roth").get(GroupBean.class, util.applyParameters("group_id = {1}", groupId));
			else if (groupName != null) {
				bean = new TableUtil("roth").get(GroupBean.class, util.applyParameters("group_name = {1}", groupName));
			}
			String filter = (bean.getGroupId() == null) 
					      ? null 
					      : util.applyParameters("group_id != {1} AND lineage NOT LIKE CONCAT('% ', {1}, ' %') ", bean.getGroupId());
			boolean securityAdmin = conn.getRequest().isUserInRole("SecurityAdmin");
			if (!securityAdmin) 
				filter = (filter == null) ? "domain_id = {1}" : filter + "AND domain_id = {1}";
			if (filter != null)
				filter = util.applyParameters(filter, util.getDomainId(conn.getDomainName()));
			putBean(util.getList(GroupBean.class, filter, "get_group_sort(group_id)", null), "groups", "request", conn);
			putBean(util.getDomains(), "domains", "request", conn);
		}
		catch (SQLException e) { e.printStackTrace(); }
		putBean(bean, "group", "request", conn);
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") })
	@Post(beans = { @Bean(name = "group", beanClass = GroupBean.class, scope = "request") })
	public String save(ActionConnection conn) {
		GroupBean group = getBean(0, conn);
		try { 
			PortalUtil util = new PortalUtil();
			boolean securityAdmin = conn.getRequest().isUserInRole("SecurityAdmin");
			if (!securityAdmin) group.setDomainId(util.getDomainId(conn.getDomainName()));
			group.setUpdatedBy(getUserName(conn));
			util.saveGroup(group); 
		}
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load"),
			             @Forward(name = "return", path = "/configuration/index.jsp") })
	public String delete(ActionConnection conn) {
		GroupBean group = new GroupBean();
		group.setGroupId(Data.strToLong(conn.getRequest().getParameter("groupId")));
		try { 
			PortalUtil util = new PortalUtil();
			util.deleteDescendantGroups(group.getGroupId());
			//util.delete(group);
		}
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__users.jsp") })
    public String loadUsers(ActionConnection conn) {
		Long groupId = conn.getLong("groupId");
		Long domainId = conn.getLong("domainId");
		try { conn.getRequest().setAttribute("groupUsers", new PortalUtil().getUserGroups(null, domainId, groupId)); } 
		catch (Exception e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadUsers") })
	@Post(beans = { @Bean(name = "groupUsers", scope = "request", beanClass = UserGroupBean.class, typeClass = ArrayList.class) })
	public String saveUsers(ActionConnection conn) {
		ArrayList<UserGroupBean> beans = getBean(0, conn);
		Long groupId = Data.strToLong(conn.getRequest().getParameter("groupId"));
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(getUserName(conn));
		try { new PortalUtil().saveUserGroups(beans, null, groupId); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__roles.jsp") })
    public String loadRoles(ActionConnection conn) {
		Long groupId = Data.strToLong(conn.getRequest().getParameter("groupId"));
		try {
			PortalUtil util = new PortalUtil();
			Long domainId = conn.getRequest().isUserInRole("SecurityAdmin") ? null : util.getDomainId(conn.getDomainName());
			conn.getRequest().setAttribute("groupRoles", util.getGroupRoles(groupId, null, domainId)); 
		} 
		catch (Exception e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadRoles") })
	@Post(beans = { @Bean(name = "groupRoles", scope = "request", beanClass = GroupRoleBean.class, typeClass = ArrayList.class) })
	public String saveRoles(ActionConnection conn) {
		ArrayList<GroupRoleBean> beans = getBean(0, conn);
		Long groupId = Data.strToLong(conn.getRequest().getParameter("groupId"));
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(getUserName(conn));
		try { new PortalUtil().saveGroupRoles(beans, groupId, null); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
}
