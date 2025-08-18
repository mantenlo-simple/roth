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

import jakarta.servlet.annotation.WebServlet;

import com.roth.base.util.Data;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.BookBean;
import com.roth.portal.model.DesktopBean;
import com.roth.portal.model.DesktopLinkBean;
import com.roth.portal.model.ThemeBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;

@WebServlet(urlPatterns = "/Desktop/*")
@ActionServletSecurity(roles = "PortalAdmin")
@Navigation(contextPath = "/configuration",
            simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class Desktop extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp"),
                         @Forward(name = "ajax", path = "_list.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String load(ActionConnection conn) {
		conn.getRequest().setAttribute("tabpage", "desktop");
		//String method = Data.nvl(conn.getRequest().getParameter("_method"));
		try { 
			//conn.getRequest().setAttribute("desktops", new PortalUtil().getList(DesktopBean.class, null, "desktop_name", null));
			putBean(new PortalUtil().getList(DesktopBean.class, null, "desktop_name", null), "desktops", "request", conn);
		} 
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		//return method.equalsIgnoreCase("ajax") ? "ajax" : "success";
		return isCallingActionName(BEGIN, conn) ? SUCCESS : "ajax";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String edit(ActionConnection conn) {
		DesktopBean bean = new DesktopBean();
		//Long desktopId = Data.strToLong(conn.getRequest().getParameter("desktopId"));
		Long desktopId = conn.getLong("desktopId");
		String desktopName = conn.getString("desktopName");
		PortalUtil util = null;
		try {
			util = new PortalUtil();
			if (desktopId != null)
				bean = new TableUtil("roth").get(DesktopBean.class, util.applyParameters("desktop_id = {1}", desktopId));
			else if (desktopName != null)
				bean = new TableUtil("roth").get(DesktopBean.class, util.applyParameters("desktop_name = {1}", desktopName));
		}
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		putBean(bean, "desktop", "request", conn);
		
		try { 
			conn.getRequest().setAttribute("bookList", util.getList(BookBean.class, null, "book_name", null));
			conn.getRequest().setAttribute("themeList", util.getList(ThemeBean.class, null, "theme_name", null));
		}
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "desktop", scope = "request", beanClass = DesktopBean.class) })
	public String save(ActionConnection conn) {
		DesktopBean desktop = getBean(0, conn);
		desktop.setUpdatedBy(conn.getRequest().getUserPrincipal().getName());
		try { new PortalUtil().save(desktop); }
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load"),
                         @Forward(name = "return", path = "/configuration/index.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String delete(ActionConnection conn) {
		DesktopBean desktop = new DesktopBean();
		desktop.setDesktopId(Data.strToLong(conn.getRequest().getParameter("desktopId")));
		try { new PortalUtil().delete(desktop); }
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__links.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String loadLinks(ActionConnection conn) {
		Long desktopId = Data.strToLong(conn.getRequest().getParameter("desktopId"));
		try { conn.getRequest().setAttribute("desktopLinks", new PortalUtil().getList(DesktopLinkBean.class, "desktop_id = '" + desktopId + "'", "sequence", null)); }
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "___linkedit.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String editLink(ActionConnection conn) {
		DesktopLinkBean bean = new DesktopLinkBean();
		bean.setDesktopId(Data.strToLong(conn.getRequest().getParameter("desktopId")));
		bean.setSequence(Data.strToLong(conn.getRequest().getParameter("sequence")));
		if (bean.getSequence() != null)
			try {
				TableUtil util = new TableUtil("roth");
				bean = util.get(DesktopLinkBean.class, util.applyParameters("desktop_id = {1} AND sequence = {2}", bean.getDesktopId(), bean.getSequence())); 
			}
		    catch (Exception e) { return returnLogException(e, conn, "failure"); }
		putBean(bean, "desktopLink", "request", conn);
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadLinks") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "desktopLink", scope = "request", beanClass = DesktopLinkBean.class) })
	public String saveLink(ActionConnection conn) {
		DesktopLinkBean bean = getBean(0, conn);
		bean.setUpdatedBy(getUserName(conn));
		try { new PortalUtil().save(bean); }
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadLinks") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String deleteLink(ActionConnection conn) {
		DesktopLinkBean bean = new DesktopLinkBean();
		bean.setDesktopId(Data.strToLong(conn.getRequest().getParameter("desktopId")));
		bean.setSequence(Data.strToLong(conn.getRequest().getParameter("sequence")));
		try { 
			PortalUtil util = new PortalUtil();
			util.delete(bean);
			util.decDesktopLinks(bean);
		}
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadLinks") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String moveLink(ActionConnection conn) {
		Long desktopId = Data.strToLong(conn.getRequest().getParameter("desktopId"));
		Long sequenceA = Data.strToLong(conn.getRequest().getParameter("sequence"));
		Long sequenceB = sequenceA;
		String direction = conn.getRequest().getParameter("direction");
		if (direction.equals("up")) sequenceA--; else sequenceB++;
		try { new PortalUtil().swapDesktopLinks(desktopId, sequenceA, sequenceB); }
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
}
