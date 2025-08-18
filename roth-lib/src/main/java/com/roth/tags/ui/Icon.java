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
package com.roth.tags.ui;

import jakarta.servlet.jsp.JspException;

import java.util.Arrays;

import com.roth.base.util.Data;
import com.roth.tags.el.Resource;

public class Icon extends ActionTag {
	private static final long serialVersionUID = -1277739412609961935L;

	// Attribute Setters
	public void setIconName(String iconName) { setValue("iconName", iconName.toLowerCase()); }
	public void setKeyId(String keyId) { setValue("keyId", keyId); }
	public void setOverlayName(String overlayName) { setValue("overlayName", overlayName.toLowerCase()); }
	public void setOverlayType(String overlayType) { setValue("overlayType", overlayType.toLowerCase()); }
	public void setCalendar(String calendar) { setValue("calendar", calendar); }
	public void setType(String type) { processType(type); }
	
	// doEndTag
	public int doEndTag() throws JspException {
		if (getValue("iconName") == null)
			throw new JspException("The iconName or type attribute is required.");
		//String body = getIcon((String)getRemoveValue("iconName"), (String)getRemoveValue("overlayName"));
		String title = getTypedValue("title");
		title = title == null ? "" : String.format("title=\"%s\"", title);
		
		String body = getIcon((String)getRemoveValue("iconName"), title);
		print(getValue("action") != null || getValue("onclick") != null ? getAnchor(body) : body);
		release();
		return EVAL_PAGE;
	}
	
	public static String getIcon(String iconName, String action, String onClick) {
		String body = getIcon(iconName);
		return getSimpleAnchor(action, onClick, body);
	}
	
	protected void processType(String type) {
		setValue("iconName", type);
		setValue("title", Resource.getString(pageContext, "com/jp/html/resource/icon", type));
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

	/**
	 * Creates the HTML code for a Font Awesome icon.
	 * The name parameter is the icon name.  The default style is 'solid'.  
	 * If a different style is desired, the name should be prefixed with a
	 * single character identifying the style, followed by a period.<br/>  
	 * <br/>
	 * Examples:<br/>
	 * name = "plus" -- This translates to "fas fa-plus".<br/>
	 * name = 'r.plus" -- This translates to "far fa-plus".<br/>
	 * <br/>
	 * Styles: 's' - solid, 'r' - regular, 'l' - light, 't' - thin (new in 6.0), and 'd' - duotone<br/>
	 * <br/>
	 * Note: This framework only includes the free icon set.  
	 * The pro set may be used by overriding the JavaScript inclusion.
	 * @param name
	 * @return
	 */
	public static String getIcon(String name) {
		if (name == null)
			throw new IllegalArgumentException("The name argument may not be null.");
		String[] n = name.split("\\.");
		String _style = n.length > 1 ? n[0] : "s";
		String _name = n.length > 1 ? n[1] : n[0];
		return String.format(
			"""
			<i class="fa%s fa-%s"></i>
			""", _style, _name);
	}
	
	/**
	 * Creates the HTML code for a Font Awesome icon.
	 * The name parameter is the icon name.  The default style is 'solid'.  
	 * If a different style is desired, the name should be prefixed with a
	 * single character identifying the style, followed by a period.<br/>  
	 * <br/>
	 * Examples:<br/>
	 * name = "plus" -- This translates to "fas fa-plus".<br/>
	 * name = 'r.plus" -- This translates to "far fa-plus".<br/>
	 * <br/>
	 * Styles: 's' - solid, 'r' - regular, 'l' - light, 't' - thin (new in 6.0), and 'd' - duotone<br/>
	 * <br/>
	 * Note: This framework only includes the free icon set.  
	 * The pro set may be used by overriding the JavaScript inclusion.
	 * @param name
	 * @param onClick
	 * @return
	 */
	public static String getIcon(String name, String attr) {
		if (name == null)
			throw new IllegalArgumentException("The name argument may not be null.");
		String[] n = name.split("\\.");
		String _style = n.length > 1 ? n[0] : "s";
		String _name = n.length > 1 ? n[1] : n[0];
		return """
				<i class="fa%s fa-%s" %s></i>
				""".formatted(_style, _name, Data.nvl(attr));
	}
	
	/**
	 * Creates the HTML code for a Font Awesome stacked icon.
	 * The parameters are the icon names.
	 * @param name
	 * @return
	 * @see #getIcon
	 */
	public static String getStackedIcon(String ... names) {
		if (names == null)
			throw new IllegalArgumentException("The names argument may not be null.");
		return """
				<span class="fa-stack">
					%s
				</span>
				""".formatted(Arrays.asList(names).stream().map(name -> getIcon(name)).reduce("", String::concat));
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * <b>getIcon</b><br><br>
	 * Creates an HTML span element, which contains the named icon.
	 * @param iconName - The icon to use.
	 * @return the created HTML.
	 */
//	public static String getIcon(String iconName) { 
//		return getIcon(iconName, null);
//	}
	
	/**
	 * <b>getIcon</b><br><br>
	 * Creates an HTML span element, which contains the named icon.
	 * @param iconName - The icon to use.
	 * @param overlay - An overlay icon (generated by getIcon), if applicable.
	 * @return the created HTML.
	 */
//	public static String getIcon(String iconName, String overlayName) {
//		return getIcon(iconName, overlayName, false, false);
//	}
	
	/**
	 * <b>getIcon</b><br><br>
	 * Creates an HTML span element, which contains the named icon.
	 * @param iconName - The icon to use.
	 * @param overlay - An overlay icon (generated by getIcon), if applicable.
	 * @param padLeft - Whether to place a space to the left of the span.
	 * @param padRight - Whether to place a space to the right of the span.
	 * @return the created HTML.
	 */
/*
	public static String getIcon(String iconName, String overlayName, boolean padLeft, boolean padRight) {
 		if (Data.isEmpty(iconName)) return "";
		String a = "";
		       a += (padLeft) ? "margin-left: 0.3em;" : "";
		       a += (padRight) ? "margin-right: 0.3em;" : "";
		if (padLeft || padRight) a = attr("style", a);
		//String padLeft_ = (padLeft) ? " " : "";
		//String padRight_ = (padRight) ? " " : "";
		return /*padLeft_ +*/ /*getIcon(iconName, overlayName, a);// + padRight_;
	}
	
	public static String getIcon(String iconName, String overlayName, String attr) {
		//String class_ = " class=\"jimg j" + iconName + "\"";
		//String overlay = (overlayName == null) ? "" : getIcon("overlay_" + overlayName);
		//return tag("span", class_ + Data.nvl(attr), overlay); //"&nbsp;");
		if (overlayName == null)
			return tag("i", attr("class", "fas fa-" + iconName) + Data.nvl(attr), "");
		else
			return tag("span", Data.nvl(attr) + attr("class", "fa-stack"),
					   tag("i", attr("class", "fas fa-" + iconName + " fa-stack-1x"), "") +
					   tag("i", attr("class", "fas fa-" + overlayName + " fa-stack-sm"), "")
					  );
	}
*/
}