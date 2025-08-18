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
import com.roth.portal.model.LinkBean;
import com.roth.portal.model.LinkRoleBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;

@WebServlet(urlPatterns = "/Link/*")
@ActionServletSecurity(roles = "Authenticated")
@Navigation(contextPath = "/configuration")
public class Link extends ActionServlet {
	private static final long serialVersionUID = 1L;

	// No begin action is necessary, so return 404, if called.
	@Action(responses = { @Response(name = "404", httpStatusCode = 404) })
	public String begin(ActionConnection conn) { return "404"; }
	
	@Action(forwards = { @Forward(name = "success", path = "_list.jsp") })
	public String load(ActionConnection conn) {
		try {
			TableUtil util = new TableUtil("roth");
			String allLinks = conn.getRequest().getParameter("allLinks");
			boolean all = conn.getRequest().isUserInRole("PortalAdmin") && (allLinks != null) && allLinks.equals("true");
			String where = (all) ? null : util.applyParameters("updated_by = {1}", getUserName(conn));
			putBean(util.getList(LinkBean.class, where, "link_title", null), "links", "request", conn);
		}
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") })
	public String edit(ActionConnection conn) {
		LinkBean bean = new LinkBean();
		Long linkId = Data.strToLong(conn.getRequest().getParameter("linkId"));
		if (linkId != null)
			try {
				TableUtil util = new TableUtil("roth");
				bean = util.get(LinkBean.class, util.applyParameters("link_id = {1}", linkId)); 
			}
			catch (SQLException e) { e.printStackTrace(); }
		putBean(bean, "link", "request", conn);
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") })
	@Post(beans = { @Bean(name = "link", scope = "request", beanClass = LinkBean.class) })
	public String save(ActionConnection conn) {
		LinkBean bean = getBean(0, conn);
		bean.setUpdatedBy(getUserName(conn));
		try { new TableUtil("roth").save(bean); }
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") })
	public String delete(ActionConnection conn) {
		LinkBean bean = new LinkBean();
		bean.setLinkId(Data.strToLong(conn.getRequest().getParameter("linkId")));
		try { new TableUtil("roth").delete(bean); }
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__roles.jsp") })
	public String loadRoles(ActionConnection conn) {
		try {
			Long linkId = Data.strToLong(conn.getRequest().getParameter("linkId"));
			putBean(new PortalUtil().getLinkRoles(linkId), "linkRoles", "request", conn);
		}
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadRoles") })
	@Post(beans = { @Bean(name = "linkRoles", scope = "request", beanClass = LinkRoleBean.class, typeClass = ArrayList.class) })
	public String saveRoles(ActionConnection conn) {
		ArrayList<LinkRoleBean> beans = getBean(0, conn);
		Long linkId = Data.strToLong(conn.getRequest().getParameter("linkId"));
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setUpdatedBy(conn.getRequest().getUserPrincipal().getName());
		try { new PortalUtil().saveLinkRoles(beans, linkId); }
		catch (SQLException e) { e.printStackTrace(); }
    	return "success";
	}
}
