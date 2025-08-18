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
package com.roth.tags.html;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.portal.model.Desktop;
import com.roth.portal.model.Link;
import com.roth.portal.model.MenuItem;
import com.roth.portal.model.Theme;
import com.roth.portal.util.Portal;
import com.roth.portal.util.StaticTheme;
import com.roth.tags.el.Resource;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

public class Portlet extends Page {
	private static final long serialVersionUID = -2874631273864049101L;

	private Theme defTheme;
	private Desktop desktop;
	
	private String copyright;
	private String version;
	private String links;
	private String menu;
	private String usermenu;
	private String portletTitle;
	private String portletUri;
	private String pdtid;
	
	// Attributes
	public void setCaption(String caption) { setValue("caption", caption); }
	public void setServletPath(String servletPath) { setValue("servletPath", servletPath); }
	
	/*
	 * 
	 * 
	 * Self-register portlets?
	 * 
	 * Needed: 
	 *  - Portlet Name
	 *  - Description
	 *  - Portlet URI ( == servletPath)
	 *  - Roles (comma-delimited)
	 * 
	 * 
	 */
	
	
	
	
	@Override
	public int doStartTag() throws JspException {
		className = "portlet-body";
		int result = super.doStartTag();
		getVersion();
		getCopyright();
		getLinks();
		if (desktop.getTheme().getThemeId() != null) { 
			getMenu();
			getUserMenu();
		}
		String header = Data.nvl(desktop.getTheme().getCustomHeaderHtml(), defTheme.getCustomHeaderHtml());
		println(header.replaceAll(Data.getULRegEx("<desktoptitle/>"), Data.evl(desktop.getDesktopTitle(), "&nbsp;"))
				      .replaceAll(Data.getULRegEx("<menu/>"), Data.nvl(menu))
				      .replaceAll(Data.getULRegEx("<usermenu/>"), Data.nvl(usermenu))
				      .replaceAll(Data.getULRegEx("<portlettitle/>"), Data.evl(portletTitle, "&nbsp;"))
				      .replaceAll(Data.getULRegEx("<links/>"), links)
				      .replaceAll(Data.getULRegEx("<copyright/>"), copyright)
				      .replaceAll(Data.getULRegEx("<version/>"), version));
		
		//if (desktop.getTheme().getThemeId() == null)
		if (Data.isEmpty(portletUri))
			println("<div style=\"height: 400px; text-align: center; font-size: 24px; font-weight: bold; color: darkred;\"><br/><br/><br/>ERROR: Unable to connect to configuration database.</div>");
		else if (desktop.getTheme().getThemeId() != null) 
			println("<input type=\"hidden\" id=\"_param_themeId\" name=\"_na\" value=\"" + desktop.getThemeId() + "\"/>");
		
		//if (desktop.getTheme().getThemeId() == null)
		if (Data.isEmpty(portletUri))
			result = SKIP_BODY;
		return result;
	}
	
	@Override
	public int doEndTag() throws JspException {
		String footer = Data.nvl(desktop.getTheme().getCustomFooterHtml(), defTheme.getCustomFooterHtml());
		println(footer.replaceAll(Data.getULRegEx("<links/>"), links)
				      .replaceAll(Data.getULRegEx("<copyright/>"), copyright)
			          .replaceAll(Data.getULRegEx("<version/>"), version));
		menu = null;
		return super.doEndTag();
	}
	
	@Override
	protected String getTitle() {
		return ((desktop == null) || (desktop.getDesktopTitle() == null) ? portletTitle : desktop.getDesktopTitle() + " - " + portletTitle);
	}
	
	@Override
	protected Theme getTheme() {
		portletTitle = "";
		String servletPath = (getValue("servletPath") == null) ? "default" : (String)getValue("servletPath"); 
		portletUri = (servletPath.equals("default")) ? servletPath : ((HttpServletRequest)pageContext.getRequest()).getContextPath() + servletPath;
		
        pdtid = pageContext.getRequest().getParameter("_pdtid");
		Desktop[] d = null;
		try {
			Portal p = new Portal();
			d = p.getDesktops(portletUri, userid, Data.strToInteger(pdtid));
		}
		catch (SQLException e) { Log.logException(e, userid); }
		desktop = ((d == null) || (d.length == 0)) ? new Desktop() : d[0];
		desktop.setHostname(hostname);
		desktop.setMobile(((HttpServletRequest)pageContext.getRequest()).getHeader("User-Agent").indexOf("Mobi") != -1);
		
		String staticTheme = null;
		try { staticTheme = StaticTheme.getTheme(pageContext.getServletContext().getRealPath("/"), hostname); }
		catch (Exception e) { Log.logException(e, userid); }
		defTheme = new Theme();
		defTheme.parseStaticTheme(staticTheme);
				
		if (desktop.getTheme().getThemeId() == null) {
			desktop.getTheme().parseStaticTheme(staticTheme);
		}
		
		pageContext.setAttribute("theme", desktop.getTheme());
		
		if ((getValue("themeId") != null) && (desktop != null))
			desktop.setThemeId(new BigDecimal(getValue("themeId").toString()));
		
		if (servletPath.equals("default"))
			desktop.setDesktopTitle("Default");
		
		getMenu();
		
		return desktop.getTheme();
	}
	
	
	
	
	
	
	
	
	protected String getDiv(String styleClass) {
		return getDiv(styleClass, null, null, null);
	}
	
	protected String getDiv(String styleClass, String body) {
	    return getDiv(styleClass, null, body, null);
	}
	
	protected String getDiv(String styleClass, String id, String body) {
	    return getDiv(styleClass, id, body, null);
	}
	
	protected String getDiv(String styleClass, String id, String body, String onmouseover) {
		String class_ = (styleClass == null) ? "" : " class=\"" + styleClass + "\"";
		String id_ = (Data.isEmpty(id)) ? "" : " id=\"" + id + "\"";
		String onmouseover_ = (onmouseover == null) ? "" : " onmouseover=\"" + onmouseover + "\""; 
		String body_ = (body == null) ? "" : body;
		return "<div" + class_ + id_ + onmouseover_ + ">" + body_ + "</div>";	
	}
	
	protected String getAnchor(String styleClass, String id, String href, String body) {
		return getAnchor(styleClass, id, href, body, null);
	}
	
	protected String getAnchor(String styleClass, String id, String href, String body, String onmouseover) {
		String class_ = (styleClass == null) ? "" : " class=\"" + styleClass + "\"";
		String id_ = (Data.isEmpty(id)) ? "" : " id=\"" + id + "\"";
		String href_ = (Data.isEmpty(href)) ? "" : " href=\"" + href + "\"";
		String onmouseover_ = (onmouseover == null) ? "" : " onmouseover=\"" + onmouseover + "\""; 
		String body_ = (body == null) ? "" : body;
		return "<span" + class_ + id_ + href_ + onmouseover_ + ">" + body_ + "</span>";
	}

	protected void getLinks() {
		links = "";
		Link[] l = (desktop == null) ? null : desktop.getTheme().getLinks();
		
		if (l != null)
			for (int i = 0; i < l.length; i++) {
				String target = (l[i].getTarget() == null) ? "" : " target=\"" + l[i].getTarget() + "\"";
				
				links += ((i > 0) ? " &nbsp;|&nbsp; " : "") + tag("a", " href=\"" + l[i].getLinkUri() + "\"" + target, l[i].getTitle());
			}
	}
	
	protected void getCopyright() {
		String year = new SimpleDateFormat("yyyy").format(new Date());
		copyright = ((desktop == null) || (desktop.getTheme() == null)) ? "" : Data.isEmpty(desktop.getTheme().getCopyrightName()) ? "" : "Copyright &copy; " + year + " " + desktop.getTheme().getCopyrightName();
	}
	
	protected void getVersion() {
		version = getManifestVersion(pageContext.getServletContext());
	}
	
	protected static String getManifestVersion(ServletContext context) {
		String version = context.getServletContextName() + " ";
		String title = context.getServletContextName() + " ";
		String extVersion = "appVersion=" + context.getServletContextName() + "%20";
		try {
		    Properties prop = new Properties();
		    prop.load(context.getResourceAsStream("/META-INF/MANIFEST.MF"));
		    version += prop.getProperty("Specification-Version", "0.0");
		    title += prop.getProperty("Implementation-Version", "0.0.0");
		    extVersion += prop.getProperty("Implementation-Version", "0.0.0");
		    extVersion += "&appCopyright=" + prop.getProperty("Implementation-Copyright", "");
		}
		catch (IOException e) { version += "0.0"; extVersion += "0.0"; }
		try {
		    Properties prop = new Properties();
		    String c = Portlet.class.getSimpleName() + ".class";
		    String cp = Portlet.class.getResource(c).toString();
		    String mp = cp.substring(0, cp.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
		    prop.load(new URI(mp).toURL().openStream());
		    //prop.load(Paths.get(mp).toUri().toURL().openStream());
		    
		    
		    //version += " / ";
		    //version += prop.getProperty("Extension-Name", "") + " ";
		    //version += prop.getProperty("Specification-Version", "0.0");
		    extVersion += "&libVersion=";
		    extVersion += prop.getProperty("Extension-Name", "") + "%20";
		    extVersion += prop.getProperty("Implementation-Version", "0.0.0");
		}
		catch (IOException | URISyntaxException e) { /* eat it */ Log.logException(e, null); }
	    return "<a href=\"javascript:Roth.execDialog('version', '/Roth/about.jsp', '" + extVersion + "', 'About Roth', 'info');void(0);\" title=\"" + title + "\">" + version + "</a>";
	}
	
	protected String getBind(String id, String uri) {
		String _uri = Data.nvl(uri);
		String pdtidParam = pdtid == null ? "" : ((_uri.contains("?")) ? "&" : "?") + "pdtid=" + pdtid;
		if (_uri.startsWith("javascript:") || _uri.startsWith("/Roth")) pdtidParam = "";
		return "menuBind['" + id + "'] = '" + _uri + pdtidParam + "';\n";
	}
	
	//protected String getDropMenu(String body) {
	//	return getDiv("jmenudrop", body);
	//}
	
	protected String escapePortletUri(String source) {
        if (source == null) return null;
        return source.replaceAll("'", "\\\\'");
	}
	
	/*
	protected String getMenuItem(MenuItem menuItem) {
		String body = "";
		String uri = menuItem.getPortletUri(); //escapePortletUri(menuItem.getPortletUri());
		String itemSeq = menuItem.getSequence().toString();
		MenuItem p = menuItem.getParent();
		
		while (p != null) {
			if (p.getSequence() != null) 
				itemSeq = p.getSequence().toString() + "." + itemSeq; 
			p = p.getParent(); 
		}
		
		String id = Data.isEmpty(uri) ? "" : "pm_" + itemSeq;
			
		if ((menuItem.getPortletUri() != null) && menuItem.getPortletUri().equals(portletUri))
			portletTitle = menuItem.getTitle();
		
		MenuItem[] items = menuItem.getMenuItems(userid);
		
		if (items != null)
			for (int i = 0; i < items.length; i++)
				body += getMenuItem(items[i]);
		
		if (!Data.isEmpty(body)) body = getDropMenu(body);
		
		return Data.isEmpty(body) 
			 ? getAnchor("jmenuitem", id, uri, Data.nvl(menuItem.getTitle())) 
			 : getDiv("jmenuitem", id, Data.nvl(menuItem.getTitle()) + Data.nvl(body));
	}
	*/
	
	protected String getMenuItem(MenuItem menuItem) {
		String body = "";
		String uri = menuItem.getPortletUri(); //escapePortletUri(menuItem.getPortletUri());
		String itemSeq = menuItem.getSequence().toString();
		MenuItem p = menuItem.getParent();
		
		while (p != null) {
			if (p.getSequence() != null) 
				itemSeq = p.getSequence().toString() + "." + itemSeq; 
			p = p.getParent(); 
		}
		
		String id = /*Data.isEmpty(uri) ? "" :*/ "pm_" + itemSeq;
			
		if ((menuItem.getPortletUri() != null) && menuItem.getPortletUri().equals(portletUri))
			portletTitle = menuItem.getTitle();
		
		MenuItem[] items = menuItem.getMenuItems(userid);
		
		if (items != null)
			for (int i = 0; i < items.length; i++)
				body += getMenuItem(items[i]);
		
		String dropMenu = Data.isEmpty(body) ? null : Menu.getDropMenu(body, false);
		return com.roth.tags.html.MenuItem.getMenuItem(id, menuItem.getTitle(), null, dropMenu, 1, uri, null);
	}
	
	/*
	protected void getMenu() {
		// Generate menu
		MenuItem[] items;
		if (portletUri.equalsIgnoreCase("default")) {
			String caption = (String)getRemoveValue("caption"); 
			if (caption != null) portletTitle = caption;
			items = new MenuItem[0];
		}
		else {
			items = (desktop.getBook() == null) ? null : desktop.getBook().getMenuItems(userid);
		}
		
		String body = "";
		
		if (items != null)
			for (int i = 0; i < items.length; i++)
				body += getMenuItem(items[i]);
		
		if (desktop.getMobile()) {
			body = "<i class=\"fas fa-bars\"></i>" + "<div class=\"jmenudrop\">" + body + "</div>"; 
		}
		
		menu = getDiv("jmenu", null, body + getDiv("jbreak", ""), "Roth.menu.doMouseOver(event);");
	}
	*/
	
	protected void getMenu() {
		if (menu != null)
			return;
		// Generate menu
		MenuItem[] items;
		if (portletUri.equalsIgnoreCase("default")) {
			String caption = (String)getRemoveValue("caption"); 
			if (caption != null) portletTitle = caption;
			items = new MenuItem[0];
		}
		else {
			items = (desktop.getBook() == null) ? null : desktop.getBook().getMenuItems(userid);
		}
		
		String body = "";
		
		if (items != null)
			for (int i = 0; i < items.length; i++)
				body += getMenuItem(items[i]);
		
		//if (desktop.getMobile()) {
		//	body = "<i class=\"fas fa-bars\"></i>" + "<div class=\"jmenudrop\">" + body + "</div>"; 
		//}
		
		//menu = getDiv("jmenu", null, body + getDiv("jbreak", ""), "Roth.menu.doMouseOver(event);");
		menu = desktop.getMobile() ? Menu.getIconMenu("mainMenu", "bars", null, null, body, false, false) : Menu.getMenu("mainMenu", body, false);
	}
	
	//protected static final String ITEM_SEPARATOR = "<div class=\"jmenusep\"></div>";
	protected static final String ITEM_SEPARATOR = "<li class=\"separator\"></li>";
	protected static final String RESOURCE_PATH = "com/roth/tags/html/resource/common";
	
	protected String getUserItem(String id, String href, String caption, String iconName) {
		//return getAnchor("juseritem", id, href, body);
		return com.roth.tags.html.MenuItem.getMenuItem(id, caption, iconName, null, 1, href, null);
	}
	
	/*
	protected void getUserMenu() {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		boolean loggedin = request.getUserPrincipal() != null;
		boolean isAdmin = request.isUserInRole("SystemAdmin");
		boolean isGroupAdmin = request.isUserInRole("GroupAdmin");
		
		int seq = 0;
		String body = "";
		
		if (!loggedin) {
			String menuLabel = desktop.getMobile() ? "<i class=\"fas fa-sign-in-alt\"></i>" : Resource.getString(pageContext, RESOURCE_PATH, "login");
			String redirect = Util.registerPath("/Roth/index.jsp");
			usermenu = getDiv("jusermenu", null, getAnchor("juseritem", "pm_user", "javascript:doAuthentication('" + redirect + "');void(0);", menuLabel), "Roth.menu.doMouseOver(event);");
		}
		else {
			body += getUserItem("pm_user." + seq++, "javascript:Roth.editProfile();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "editProfile")) +
				    getUserItem("pm_user." + seq++, "javascript:Roth.changePassword();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "changePassword"));
			
			if (isAdmin || isGroupAdmin) {
				body += ITEM_SEPARATOR;
				if (isAdmin)
					body += getUserItem("pm_user." + seq++, "javascript:Roth.logSettings();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "logSettings"));
				if (isGroupAdmin)
					body += getUserItem("pm_user." + seq++, "javascript:Roth.groupAdmin();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "groupAdmin"));
			}
			
			body += ITEM_SEPARATOR +
				    getUserItem("pm_user." + seq++, "javascript:logout();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "logout"));
			
			body = getDiv("juserdrop", body);
			String menuLabel = desktop.getMobile() ? "<i class=\"fas fa-user\"></i>" : userid;
			usermenu = getDiv("jusermenu", null, getDiv("jusername", menuLabel + body), "Roth.menu.doMouseOver(event);");
		}
	}
	*/
	
	protected void getUserMenu() {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		boolean loggedin = request.getUserPrincipal() != null;
		boolean isAdmin = request.isUserInRole("SystemAdmin");
		boolean isGroupAdmin = request.isUserInRole("GroupAdmin");
		
		int seq = 0;
		String body = "";
		
		if (!loggedin) {
			String menuLabel = desktop.getMobile() ? "<i class=\"fas fa-sign-in-alt\"></i>" : Resource.getString(pageContext, RESOURCE_PATH, "login");
			String redirect = request.getRequestURI(); //  Util.registerPath("/Roth/index.jsp");
			//usermenu = getDiv("jusermenu", null, getAnchor("juseritem", "pm_user", "javascript:doAuthentication('" + redirect + "');void(0);", menuLabel), "Roth.menu.doMouseOver(event);");
			usermenu = com.roth.tags.html.MenuItem.getMenuItem("um_login", menuLabel, null, "javascript:doAuthentication('" + redirect + "');void(0);", null);
			usermenu = Menu.getMenu("usermenu", usermenu, true);
		}
		else {
			body += getUserItem("pm_user." + seq++, "javascript:Roth.editProfile();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "editProfile"), "address-card") +
				    getUserItem("pm_user." + seq++, "javascript:Roth.changePassword();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "changePassword"), "key");
			
			if (isAdmin || isGroupAdmin) {
				body += ITEM_SEPARATOR;
				if (isAdmin)
					body += getUserItem("pm_user." + seq++, "javascript:Roth.logSettings();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "logSettings"), "cog");
				if (isGroupAdmin)
					body += getUserItem("pm_user." + seq++, "javascript:Roth.groupAdmin();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "groupAdmin"), "users");
			}
			
			body += ITEM_SEPARATOR +
				    getUserItem("pm_user." + seq++, "javascript:logout();void(0);", Resource.getString(pageContext, RESOURCE_PATH, "logout"), "sign-out-alt");
			
			//body = getDiv("juserdrop", body);
			//String menuLabel = desktop.getMobile() ? "<i class=\"fas fa-user\"></i>" : userid;
			//usermenu = getDiv("jusermenu", null, getDiv("jusername", menuLabel + body), "Roth.menu.doMouseOver(event);");

			//body = Menu.getDropMenu(body);
			String menuIcon = desktop.getMobile() ? "user" : null;
			String caption = desktop.getMobile() ? null : userid;
			usermenu = Menu.getIconMenu("usermenu", menuIcon, caption, null, body, true, true); 
		}
	}
	
}
