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

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;

import jakarta.servlet.annotation.WebServlet;

import org.apache.tomcat.util.http.fileupload.FileItem;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.export.util.JsonUtil;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.ThemeBean;
import com.roth.portal.model.ThemeLinkBean;
import com.roth.portal.model.ThemeOverrideBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;

@WebServlet(urlPatterns = "/Theme/*")
@ActionServletSecurity(roles = "PortalAdmin")
@Navigation(contextPath = "/configuration", simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class Theme extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp"),
			@Forward(name = "ajax", path = "_list.jsp") })
	public String load(ActionConnection conn) throws Exception {
		conn.getRequest().setAttribute("tabpage", "theme");
		putBean(new PortalUtil().getList(ThemeBean.class, null, "theme_name", null), "themes", "request", conn);
		return isCallingActionName(BEGIN, conn) ? SUCCESS : "ajax";
	}

	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") })
	public String edit(ActionConnection conn) {
		ThemeBean bean = new ThemeBean();
		Long themeId = Data.strToLong(conn.getRequest().getParameter("themeId"));
		String themeName = conn.getParameter("themeName");
		try {
			TableUtil util = new TableUtil("roth");
			if (themeId != null)
				bean = util.get(ThemeBean.class, util.applyParameters("theme_id = {1}", themeId));
			else if (themeName != null)
				bean = util.get(ThemeBean.class, util.applyParameters("theme_name = {1}", themeName));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		putBean(bean, "theme", "request", conn);
		try {
			putBean(new PortalUtil().getThemes(themeId), "themes", "request", conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", action = "load") })
	@Post(beans = { @Bean(name = "theme", scope = "request", beanClass = ThemeBean.class) })
	public String save(ActionConnection conn) {
		ThemeBean theme = getBean(0, conn);
		theme.setUpdatedBy(getUserName(conn));
		try {
			new PortalUtil().save(theme);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", action = "load"),
			@Forward(name = "return", path = "/configuration/index.jsp") })
	public String delete(ActionConnection conn) {
		ThemeBean theme = new ThemeBean();
		theme.setThemeId(Data.strToLong(conn.getRequest().getParameter("themeId")));
		try {
			new PortalUtil().delete(theme);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp") })
	public String preview(ActionConnection conn) {
		conn.getRequest().setAttribute("tabpage", "theme");
		conn.getRequest().setAttribute("previewThemeId", Data.strToLong(conn.getRequest().getParameter("themeId")));
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", path = "__links.jsp") })
	public String loadLinks(ActionConnection conn) {
		Long themeId = Data.strToLong(conn.getRequest().getParameter("themeId"));
		try {
			conn.getRequest().setAttribute("themeLinks",
					new PortalUtil().getList(ThemeLinkBean.class, "theme_id = '" + themeId + "'", "sequence", null));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", path = "___linkedit.jsp") })
	public String editLink(ActionConnection conn) {
		ThemeLinkBean bean = new ThemeLinkBean();
		bean.setThemeId(Data.strToLong(conn.getRequest().getParameter("themeId")));
		bean.setSequence(Data.strToLong(conn.getRequest().getParameter("sequence")));
		if (bean.getSequence() != null)
			try {
				TableUtil util = new TableUtil("roth");
				bean = util.get(ThemeLinkBean.class, util.applyParameters("theme_id = {1} AND sequence = {2}",
						bean.getThemeId(), bean.getSequence()));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		putBean(bean, "themeLink", "request", conn);
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", action = "loadLinks") })
	@Post(beans = { @Bean(name = "themeLink", scope = "request", beanClass = ThemeLinkBean.class) })
	public String saveLink(ActionConnection conn) {
		ThemeLinkBean bean = getBean(0, conn);
		bean.setUpdatedBy(getUserName(conn));
		try {
			new PortalUtil().save(bean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", action = "loadLinks") })
	public String deleteLink(ActionConnection conn) {
		ThemeLinkBean bean = new ThemeLinkBean();
		bean.setThemeId(Data.strToLong(conn.getRequest().getParameter("themeId")));
		bean.setSequence(Data.strToLong(conn.getRequest().getParameter("sequence")));
		try {
			PortalUtil util = new PortalUtil();
			util.delete(bean);
			util.decThemeLinks(bean);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", action = "loadLinks") })
	public String moveLink(ActionConnection conn) {
		Long themeId = Data.strToLong(conn.getRequest().getParameter("themeId"));
		Long sequenceA = Data.strToLong(conn.getRequest().getParameter("sequence"));
		Long sequenceB = sequenceA;
		String direction = conn.getRequest().getParameter("direction");
		if (direction.equals("up"))
			sequenceA--;
		else
			sequenceB++;
		try {
			new PortalUtil().swapThemeLinks(themeId, sequenceA, sequenceB);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", path = "__overrides.jsp") })
	public String loadOverrides(ActionConnection conn) {
		Long themeId = Data.strToLong(conn.getRequest().getParameter("themeId"));
		try {
			PortalUtil util = new PortalUtil();
			conn.getRequest().setAttribute("themeOverrides",
					util.getList(ThemeOverrideBean.class, "theme_id = '" + themeId + "'", "host_name", null));
			putBean(util.getThemes(), "themes", "request", conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", path = "___overrideedit.jsp") })
	public String editOverride(ActionConnection conn) throws SQLException {
		ThemeOverrideBean bean = new ThemeOverrideBean();
		bean.setThemeId(Data.strToLong(conn.getRequest().getParameter("themeId")));
		bean.setHostName(conn.getRequest().getParameter("hostName"));
		if (bean.getHostName() != null) {
			TableUtil util = new TableUtil("roth");
			bean = util.get(ThemeOverrideBean.class,
					util.applyParameters("theme_id = {1} AND host_name = {2}", bean.getThemeId(), bean.getHostName()));
		}
		putBean(bean, "themeOverride", "request", conn);
		putBean(new PortalUtil().getThemes(), "themes", "request", conn);
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", action = "loadOverrides") })
	@Post(beans = { @Bean(name = "themeOverride", scope = "request", beanClass = ThemeOverrideBean.class) })
	public String saveOverride(ActionConnection conn) throws SQLException {
		ThemeOverrideBean bean = getBean(0, conn);
		bean.setUpdatedBy(getUserName(conn));
		new PortalUtil().save(bean);
		return "success";
	}

	@Action(forwards = { @Forward(name = "success", action = "loadOverrides") })
	public String deleteOverride(ActionConnection conn) throws SQLException {
		ThemeOverrideBean bean = new ThemeOverrideBean();
		bean.setThemeId(Data.strToLong(conn.getRequest().getParameter("themeId")));
		bean.setHostName(conn.getRequest().getParameter("hostName"));
		new PortalUtil().delete(bean);
		return "success";
	}

	@Action(responses = { @Response(name = "success") })
	public String exportTheme(ActionConnection conn) throws SQLException {
		Long themeId = conn.getLong("themeId");
		PortalUtil util = new PortalUtil();
		String filter = util.applyParameters("theme_id = {1}", themeId);
		ThemeBean theme = util.get(ThemeBean.class, filter);
		
		theme.setLinks(util.getList(ThemeLinkBean.class, filter));
		theme.setOverrides(util.getList(ThemeOverrideBean.class, filter));
		conn.getResponse().setContentType("application/json");
		conn.getResponse().addHeader("Content-Disposition",
				"attachment; filename=\"" + theme.getThemeName().toLowerCase() + ".json\"");
		conn.printJson(theme);

		/*
		try {
			System.out.println(JsonUtil.objToJson(theme, true));
		} catch (InvocationTargetException | IllegalAccessException e) {
			Log.logException(e, conn.getUserName());
		}

		ArrayList<ThemeLinkBean> links = util.getList(ThemeLinkBean.class, filter);
		ArrayList<ThemeOverrideBean> overrides = util.getList(ThemeOverrideBean.class, filter);
		conn.getResponse().setContentType("application/octet-stream");
		conn.getResponse().addHeader("Content-Disposition",
				"attachment; filename=\"" + theme.getThemeName().toLowerCase() + ".thm\"");
		conn.println(theme.getStaticTheme(links, overrides));
		*/
		return "success";
	}

	@Action(responses = { @Response(name = "success") })
	public String importTheme(ActionConnection conn) throws Exception {
		String message = "";
		for (FileItem item : conn.getFiles(16 * KILOBYTE)) {
			if (item.getFieldName().startsWith("_") || item.getName() == null || !item.getName().endsWith(".thm"))
				continue;
			String staticTheme = item.getString("UTF-8");
			ArrayList<ThemeLinkBean> links = new ArrayList<>();
			ArrayList<ThemeOverrideBean> overrides = new ArrayList<>();
			ThemeBean theme = new ThemeBean();
			theme.setStaticTheme(staticTheme, links, overrides);
			ArrayList<Object> toInsert = new ArrayList<>();
			toInsert.add(theme);
			toInsert.addAll(links);
			toInsert.addAll(overrides);
			PortalUtil util = new PortalUtil();
			util.delete(theme); // If it already exists, delete it to be overwritten.
			util.insert(toInsert);
			message += (message.isEmpty() ? "" : "<br/>") + "Theme " + theme.getThemeName() + " successfully imported.";
		}
		if (message.isEmpty())
			message = "No themes were imported.";
		conn.println(message);
		return "success";
	}
}
