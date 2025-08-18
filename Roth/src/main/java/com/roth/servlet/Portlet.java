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
import com.roth.portal.model.PortletBean;
import com.roth.portal.model.PortletRoleBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;

@WebServlet(urlPatterns = "/Portlet/*")
@ActionServletSecurity(roles = "PortalAdmin")
@Navigation(contextPath = "/configuration",
	        simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class Portlet extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp"),
                         @Forward(name = "ajax", path = "_list.jsp") })
    public String load(ActionConnection conn) {
		conn.getRequest().setAttribute("tabpage", "portlet");
		//String method = Data.nvl(conn.getRequest().getParameter("_method"));
		try { conn.getRequest().setAttribute("portlets", new PortalUtil().getList(PortletBean.class, "portlet_name != 'default'")); } 
		catch (Exception e) { e.printStackTrace(); }
		//return method.equalsIgnoreCase("ajax") ? "ajax" : "success";
		return isCallingActionName(BEGIN, conn) ? SUCCESS : "ajax";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") })
	public String edit(ActionConnection conn) {
		PortletBean bean = new PortletBean();
		Long portletId = Data.strToLong(conn.getRequest().getParameter("portletId"));
		String portletName = conn.getRequest().getParameter("portletName");
		try {
			TableUtil util = new TableUtil("roth");
			if (portletId != null)
				bean = util.get(PortletBean.class, util.applyParameters("portlet_id = {1}", portletId));
			else if (portletName != null)
				bean = util.get(PortletBean.class, util.applyParameters("portlet_name = {1}", portletName));
		}
		catch (SQLException e) { e.printStackTrace(); }
		putBean(bean, "portlet", "request", conn);
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") })
	@Post(beans = { @Bean(name = "portlet", beanClass = PortletBean.class, scope = "request") })
	public String save(ActionConnection conn) {
		PortletBean portlet = getBean(0, conn);
		portlet.setUpdatedBy(getUserName(conn));
		try { new PortalUtil().save(portlet); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load"),
			             @Forward(name = "return", path = "/configuration/index.jsp") })
	public String delete(ActionConnection conn) {
		PortletBean portlet = new PortletBean();
		portlet.setPortletId(Long.valueOf(conn.getRequest().getParameter("portletId")));
		try { new PortalUtil().delete(portlet); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__roles.jsp") })
    public String loadRoles(ActionConnection conn) {
		Long portletId = Long.valueOf(conn.getRequest().getParameter("portletId"));
		try { conn.getRequest().setAttribute("portletRoles", new PortalUtil().getPortletRoles(null, portletId)); } 
		catch (Exception e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadRoles") })
	@Post(beans = { @Bean(name = "portletRoles", scope = "request", beanClass = PortletRoleBean.class, typeClass = ArrayList.class) })
	public String saveRoles(ActionConnection conn) {
		ArrayList<PortletRoleBean> beans = getBean(0, conn);
		Long portletId = Long.valueOf(conn.getRequest().getParameter("portletId"));
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(getUserName(conn));
		try { new PortalUtil().savePortletRoles(beans, null, portletId); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
}
