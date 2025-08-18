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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.roth.base.util.Data;
import com.roth.tags.el.Resource;

public abstract class HtmlTag extends TagSupport {
	private static final long serialVersionUID = 1855079332367989479L;
	
	/*
	public static Properties HTML_BASE;
	
	static {
		try (InputStream input = new FileInputStream("/com/roth/tags/html/resource/html_entities.properties")) {
			HTML_BASE = new Properties();
			HTML_BASE.load(input);
		}
		catch (IOException e) {
			Log.logException(e, null);
		}
	}
	*/
	
	// Attributes
	//   (See "Attribute Support Functions" below.)
	// Common Attribute Setters
	public void setCssClass(String cssClass) { setValue("class", cssClass); }
	public void setDisabled(boolean disabled) { setValue("disabled", Boolean.toString(disabled)); }
	public void setId(String id) { setValue("id", id); }
	public void setStyle(String style) { setValue("style", style); }
	public void setTitle(String title) { setValue("title", localize(title)); }
	public void setDir(String dir) { setValue("dir", dir); }
	public void setLang(String lang) { setValue("lang", lang); }
	public void setAccessKey(String accessKey) { setValue("accesskey", accessKey); }
	public void setTabIndex(String tabIndex) { setValue("tabindex", tabIndex); }
	// Common Event Attribute Setters
	// onBlur
	// onChange (I don't really see this as common to all visible elements...)
	public void setOnClick(String onClick) { setValue("onclick", onClick); }
	// onContextMenu
	// onCopy
	// onCut
	public void setOnDblClick(String onDblClick) { setValue("ondblclick", onDblClick); }
	// onDrag
	// onDragEnd
	// onDragEnter
	// onDragLeave
	// onDragOver
	// onDragStart
	// onDrop
	// onFocus
	// onInput
	// onInvalid
	public void setOnKeyDown(String onKeyDown) { setValue("onkeydown", onKeyDown); }
	public void setOnKeyPress(String onKeyPress) { setValue("onkeypress", onKeyPress); }
	public void setOnKeyUp(String onKeyUp) { setValue("onkeyup", onKeyUp); }
	public void setOnMouseDown(String onMouseDown) { setValue("onmousedown", onMouseDown); }
	public void setOnMouseMove(String onMouseMove) { setValue("onmousemove", onMouseMove); }
	public void setOnMouseOut(String onMouseOut) { setValue("onmouseout", onMouseOut); }
	public void setOnMouseOver(String onMouseOver) { setValue("onmouseover", onMouseOver); }
	public void setOnMouseUp(String onMouseUp) { setValue("onmouseup", onMouseUp); }
	// onMouseWheel
	// onPaste
	// onScroll
	// onSelect
	// onWheel
	
	/* ================================================================================
	 * 
	 * Abstract Getters
	 * 
	 * These getters are used by the base class to understand how to facilitate the
	 * functionality of descendant classes.
	 * 
	 * ================================================================================ */
	
	/**
	 * Get the attribute names supported by the descendant tag class.  Only attributes
	 * listed here are used in rendering the tag.
	 * @return
	 */
	public abstract String[][] getAttributes();
	
	/**
	 * Get the entity names to expect.
	 * @return
	 */
	public abstract String[] getEntities();
	
	/**
	 * Get the template for rendering the descendant tag class.
	 * @return
	 */
	public abstract String getTemplate();
	
	/**
	 * Generate tag ready for render.
	 * @throws JspTagException
	 */
	public String generateHtml() throws JspTagException {
		String[][] attr = getAttributes();
		String[] ent = getEntities();
		String result = getTemplate();
		for (int i = 0; i < attr.length; i++) {
			StringBuilder tribs = new StringBuilder(""); 
			for (String a : attr[i])
				tribs.append("%s=\"%s\" ".formatted(a, getStringValue("%s_%d".formatted(a, i))));
			result.replace("{ATTRIBUTES_%d}".formatted(i), getStringValue(tribs.toString()));
		}
		for (String e : ent)
			result.replace("{%s}".formatted(e), getStringValue(e));
		return result;
	}
	
	public void render() throws JspTagException {
		render(generateHtml());
	}
	
	public void render(String html) throws JspTagException {
		print(html);
	}
	
	// Common Attribute Getters
	public String getId() {
		if (getValue("id") == null)
			setValue("id", generateId());
		return (String)getValue("id"); 
	}
	
	// Output Functions
	protected void print(String output) throws JspTagException {
		try { pageContext.getOut().print(output); }
        catch (IOException e) { throw new JspTagException(e.getMessage()); }
	}
	
	protected void println(String output) throws JspTagException {
		try { pageContext.getOut().println(output); }
        catch (IOException e) { throw new JspTagException(e.getMessage()); }
	}
	
	public static String attr(String name, String value) {
		return htmlAttr(name, value);
		//return Data.isEmpty(value) ? "" : " " + name + "=\"" + value + "\"";
	}
	
	public static String cssAttr(String name, String value) { 
		return Data.isEmpty(value) ? "" : "%s:%s;".formatted(name, value);
	}
	
	public static String htmlAttr(String name, String value) { 
		return Data.isEmpty(value) ? "" : " %s=\"%s\"".formatted(name, value);
	}
	
	// Attribute Support Functions
	public String getHTMLAttributes() {
		StringBuilder html = new StringBuilder(attr("id", id));
		Enumeration<String> e = getValues();
		while (e.hasMoreElements()) {
			String k = e.nextElement();
			if (k.equals("_required") && this.getBooleanValue(k, false))
				html.append(" required");
			else if (k.equals("_noRender") && this.getBooleanValue(k, false))
				html.append(" noRender");
			else if (!k.startsWith("_")) { 
				String value = Data.obj2Str(getValue(k));
				if (k.equals("value"))
					value = value.replace("\"", "&quot;");
				html.append(attr(k, value));
			}
		}
		return html.toString();
	}
	
	public String getStringValue(String k) { return (String)getValue(k); }
	public String getStringValue(String k, String defaultValue) { return Data.nvl(getStringValue(k), defaultValue); }
	
	public Boolean getBooleanValue(String k) { return (Boolean)getValue(k); }
	public Boolean getBooleanValue(String k, Boolean defaultValue) { return Data.nvl(getBooleanValue(k), defaultValue); }
	
	public Integer getIntegerValue(String k) { return (Integer)getValue(k); }
	public Integer getIntegerValue(String k, Integer defaultValue) { return Data.nvl(getIntegerValue(k), defaultValue); }
	
	public Long getLongValue(String k) { return (Long)getValue(k); }
	public Long getLongValue(String k, Long defaultValue) { return Data.nvl(getLongValue(k), defaultValue); }
	
	public Float getFloatValue(String k) { return (Float)getValue(k); }
	public Float getFloatValue(String k, Float defaultValue) { return Data.nvl(getFloatValue(k), defaultValue); }
	
	public Double getDoubleValue(String k) { return (Double)getValue(k); }
	public Double getDoubleValue(String k, Double defaultValue) { return Data.nvl(getDoubleValue(k), defaultValue); }
	
	public BigDecimal getBigDecimalValue(String k) { return (BigDecimal)getValue(k); }
	public BigDecimal getBigDecimalValue(String k, BigDecimal defaultValue) { return Data.nvl(getBigDecimalValue(k), defaultValue); }
	
	public LocalDate getLocalDateValue(String k) { return (LocalDate)getValue(k); }
	public LocalDate getLocalDateValue(String k, LocalDate defaultValue) { return Data.nvl(getLocalDateValue(k), defaultValue); }
	
	public LocalDateTime getLocalDateTimeValue(String k) { return (LocalDateTime)getValue(k); }
	public LocalDateTime getLocalDateTimeValue(String k, LocalDateTime defaultValue) { return Data.nvl(getLocalDateTimeValue(k), defaultValue); }
	
	public LocalTime getLocalTimeValue(String k) { return (LocalTime)getValue(k); }
	public LocalTime getLocalTimeValue(String k, LocalTime defaultValue) { return Data.nvl(getLocalTimeValue(k), defaultValue); }
	
	@SuppressWarnings("unchecked")
	public <T> T getRemoveValue(String k) {
		Object result = getValue(k);
		removeValue(k);
		return (T)result;
	}
	
	public String generateId() {
		Integer g = (Integer)pageContext.getSession().getServletContext().getAttribute("HtmlTagIdGen");
		if (g == null) g = -1;
		g++;
		pageContext.getSession().getServletContext().setAttribute("HtmlTagIdGen", g);
		String core = Data.dateToStr(new Date(), "DDDHHmmss");
		return "autogen" + core + g.toString();
	}
	
	/**
	 * If source starts with a '#', then it is assumed to be a localization key, unless it is escaped with another '#'.
	 * If not determined to be a localization key, then the source is returned without lookup, only replacing '##' with '#'.
	 * If source is determined to be a localization key, then it is looked up in the path associated with 
	 * request['__roth_resource_path'] is used, and if that is not found, then it will use the common path in roth-lib's class loader.
	 * @param source
	 * @param path
	 * @return
	 */
	public String localize(String source) {
		if (source == null || !source.startsWith("##"))
			return source;
		String key = source.substring(2);
		String result = Resource.getString(pageContext, key);
		if (result == null)
			result = String.format("UNKNOWN RESOURCE KEY: %s", source);
		return result;
	}
	
	// General Support Functions
	/**
	 * <b>getTag</b><br><br>
	 * Creates the HTML for a tag using the given attributes and body.
	 * @param name - The tag name.
	 * @param attributes - The tag attributes.
	 * @param body - The tag body.
	 * @return the HTML for the tag.
	 */
	public static String tag(String name, String attributes, String body) {
		return tagStart(name, attributes, body == null) + 
			   Data.nvl(body) +
		       ((body == null) ? "" : tagEnd(name));
	}
	
	public static String tagStart(String name) { return tagStart(name, null, false); }
	public static String tagStart(String name, String attributes) { return tagStart(name, attributes, false); }
	public static String tagStart(String name, String attributes, boolean selfEnding) {
		return "<" + name + Data.nvl(attributes) + ((selfEnding) ? "/>" : ">");
	}
	
	public static String tagEnd(String name) { return "</" + name + ">"; }
	
	protected String getLogLoc(String method) {
	    HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
	    String userid = (request.getUserPrincipal() == null) ? "[not logged in]" : request.getUserPrincipal().getName();
        return userid + " @ " +
               pageContext.getServletContext().getContextPath().replaceAll("/", "") + "/" + 
               this.getClass().getCanonicalName() + "." + method;
    }
	
	// =========================================================================================
	
	public static Map<String,String> templates;
	
	static {
		templates = new HashMap<>();
		String source = Data.readTextFile(HtmlTag.class, "/com/roth/tags/html/resource/templates.html");
		int t = 0;
		do {
			t = source.indexOf("<template ", t);
			if (t > -1) {
				int i = source.indexOf(" id=", t);
				int ei = source.indexOf("\"", i + 5);
				String id = source.substring(i + 5, ei); 
				int et = source.indexOf("</template>", t);
				String template = source.substring(ei + 2, et).trim();
				templates.put(id, template);
				t = et;
			}
		} while(t > -1);
	}
	
	public static String getTemplate(String id) {
		return Data.nvl(templates.get(id)).replace("\n", "\\n").replace("\r", "");
	}
	
	protected static String applyTemplate(String id, Map<String,String> parameters) {
		String template = templates.get(id);
		for (Entry<String,String> e : parameters.entrySet())
			template = template.replace(String.format("${%s}", e.getKey()), e.getValue());
		return template;
	}
	
	protected static String applyTemplate2(String template, Map<String,String> parameters) {
		for (Entry<String,String> e : parameters.entrySet())
			template = template.replace(String.format("${%s}", e.getKey()), e.getValue());
		return template;
	}
	
	/**
	 * Get all specified event handler attributes for the input.
	 * @return
	 */
	public String getEvents() {
		return getEvents(null);
	}
	
	/**
	 * Get all specified event handler attributes for the input.
	 * @param limiter A comma-delimited list of event names to include or exclude.<br/>
	 * Inclusion Example (only include these): "onclick,onchange"<br/>
	 * Exclusion Example (only exclude these): "!onclick,onchange"
	 * @return
	 */
	public String getEvents(String limiter) {
		String result = "";
		boolean exclude = limiter != null && limiter.startsWith("!");
		String[] events = limiter == null ? new String[] {} : exclude ? limiter.substring(1).split(",") : limiter.split(",");
		Enumeration<String> e = getValues();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			if (key.startsWith("on") && ((exclude && !Data.in(key, events)) || (!exclude && (events.length == 0 || Data.in(key, events)))))
				result += String.format(" %s=\"%s\"", key, getValue(key));
		}
		return result;
	}

	// NEW CODE
	
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
		//return String.format(
		//	"""
		//	<i class="fa%s fa-%s"></i>
		//	""", _style, _name);
		return String.format("<i class=\"fa%s fa-%s\"></i>\n", _style, _name);
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
		//return String.format(
		//	"""
		//	<i class="fa%s fa-%s"></i>
		//	""", _style, _name);
		return String.format("<i class=\"fa%s fa-%s\" %s></i>\n", _style, _name, Data.nvl(attr));
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
		//return String.format(
		//	"""
		//	<span class="fa-stack">
		//		%s
		//	</span>
		//	""", Arrays.asList(names).stream().map(name -> getIcon(name)).reduce("", String::concat));
		return String.format("<span class=\"fa-stack\">\n%s\n</span>\n", Arrays.asList(names).stream().map(name -> getIcon(name)).reduce("", String::concat));
	}
}
