package com.roth.tags.html;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.SQLException;
import java.util.regex.Matcher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.portal.model.Theme;
import com.roth.portal.util.Portal;
import com.roth.portal.util.StaticTheme;
import com.roth.tags.el.Resource;

public class Page extends HtmlTag {
	private static final long serialVersionUID = -608821442846061090L;

	protected static final String LINK_ATTR = attr("type", "text/css") + attr("rel", "stylesheet");
	protected static final String SCRIPT_ATTR = attr("type", "text/javascript");
	private static String PORTAL_ROOT = "/Roth";
	
	public static String getPortalRoot() { return PORTAL_ROOT; }
	public static void setPortalRoot(String portalRoot) { PORTAL_ROOT = portalRoot; }
	
	private String pageStart;
	private String pageEnd;
	
	protected String hostname;
	protected String userid;
	protected String className = null;
	
	public void setThemeId(String themeId) { if (!Data.isEmpty(themeId)) setValue("themeId", themeId); }
	public void setOnLoad(String onLoad) { setValue("onload", onLoad); }
	
	@Override
	public int doStartTag() throws JspException {
		pageContext.setAttribute("contextRoot", pageContext.getServletContext().getContextPath());
		pageContext.setAttribute("portalRoot", PORTAL_ROOT);
		
		Principal userp = ((HttpServletRequest)pageContext.getRequest()).getUserPrincipal(); 
		userid = (userp != null) ? userp.getName() : "anonymous";
		if (userid.endsWith("@default")) userid = userid.replaceAll("@default", "");
		hostname = pageContext.getRequest().getServerName();
		Theme theme = getTheme();
		
		// Theme CSS & JavaScript
		String themeTags = "";
		if (!Data.isEmpty(theme.getCustomCssUri())) {
			String[] includes = Data.splitLF(theme.getCustomCssUri());
			for (String include : includes)
				themeTags += getLinkTag(include);
		}
		if (!Data.isEmpty(theme.getCustomJsUri())) {
			String[] includes = Data.splitLF(theme.getCustomJsUri());
			for (String include : includes)
				themeTags += getScriptTag(include, "");
		}
		
		getPage(themeTags, pageContext.getRequest().getLocale().getLanguage());
		println(pageStart);
		return EVAL_BODY_INCLUDE;
	}
	
	@Override
	public int doEndTag() throws JspException {
		println(pageEnd);
		release();
		return EVAL_PAGE;
	}
	
	/**
	 * Get a link tag (i.e. for CSS references).
	 * @param href
	 * @return
	 */
	protected String getLinkTag(String href) {
		return "\t\t" + tagStart("link", LINK_ATTR + attr("href", href), true) + "\n";
	}
	
	/**
	 * Get a script tag.
	 * @param src
	 * @param body
	 * @return
	 */
	protected String getScriptTag(String src, String body) {
		return "\t\t" + (!Data.isEmpty(src) ? tag("script", SCRIPT_ATTR + attr("src", src), "") : tag("script", "", body)) + "\n";
	}
	
	/**
	 * Get the page title.
	 * @return
	 */
	protected String getTitle() {
		return Data.nvl(Data.obj2Str(getValue("title")), Resource.getString(pageContext, "com/roth/tags/html/resource/common", "accessError"));
	}
	
	/**
	 * Get the theme to use.
	 * @param hostname
	 * @param userid
	 * @return
	 */
	protected Theme getTheme() {
		String staticTheme = null;
		try { staticTheme = StaticTheme.getTheme(pageContext.getServletContext().getRealPath("/"), hostname); }
		catch (Exception e) { Log.logException(e, userid); }
		Theme theme = new Theme();
		theme.parseStaticTheme(staticTheme);
		boolean mobile = ((HttpServletRequest)pageContext.getRequest()).getHeader("User-Agent").indexOf("Mobi") != -1;
				
		if (getValue("themeId") != null)
			try { theme = new Portal().getTheme(new BigDecimal(getValue("themeId").toString()), hostname, mobile); }
		    catch (SQLException e) { Log.logException(e, userid); }
		
		pageContext.setAttribute("theme", theme);
		return theme;
	}
	
	/**
	 * Get page.html and parse it.
	 * @param themeTags
	 * @param language
	 */
	private void getPage(String themeTags, String language) {
		String[] page = Data.readTextFile(getClass(), "/com/roth/tags/html/resource/page.html").split("PH_CONTENT");
		String onload = (getValue("onload") == null) ? "" : attr("onload", getValue("onload").toString());
		pageStart = page[0].replaceAll(Data.getULRegEx("PH_TITLE"), getTitle())
					       .replaceAll(Data.getULRegEx("PH_THEME"), themeTags)
					       .replaceAll(Data.getULRegEx("PH_LOCALE"), language)
					       .replaceAll(Data.getULRegEx("PH_CLASS"), Data.nvl(className))
					       .replaceAll(Data.getULRegEx("PH_ONLOAD"), Matcher.quoteReplacement(onload))
					       .replaceAll(Data.getULRegEx("CONTEXT_ROOT"), pageContext.getServletContext().getContextPath())
					       .replaceAll(Data.getULRegEx("PORTAL_ROOT"), PORTAL_ROOT);
		pageEnd = page[1];
	}
	@Override
	public String[][] getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String[] getEntities() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getTemplate() {
		// TODO Auto-generated method stub
		return null;
	}
}
