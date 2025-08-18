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

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import com.roth.base.util.Data;
import com.roth.tags.el.Resource;
import com.roth.tags.ui.util.ParameterData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.TagSupport;

public abstract class RothTag extends TagSupport {
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
	
	// All attributes
	/**
	 * Used by &lt;input&gt;.  Deprecated from &lt;form&gt;, so don't use it there.
	 * @param accept
	 */
	public void setAccept(String accept) { setValue("accept", accept); }
	
	/**
	 * Used by &lt;form&gt;.
	 * @param acceptCharset
	 */
	public void setAcceptCharset(String acceptCharset) { setValue("accept-charset", acceptCharset); }
	
	/**
	 * Used by all tags.
	 * @param accesskey
	 */
	public void setAccesskey(String accesskey) { setValue("accesskey", accesskey); }
	
	/**
	 * Used by &lt;form&gt;.
	 * @param action
	 */
	public void setAction(String action) { setValue("action", action); }
	
/*	
	allow // iframe              NOT NEEDED
	alt // area img input        NOT NEEDED -- MAYBE LATER?
	as // <link>                 ?
	async // <script>            NOT NEEDED -- MAYBE if we do a Script tag?
	*/
	
	/**
	 * Global attribute.
	 * @param autocapitalize
	 */
	public void setAutoCapitalize(String autoCapitalize) { setValue("autocapitalize", autoCapitalize); }
	
	/**
	 * Used with &lt;form>, &lt;input>, &lt;select>, &lt;textarea>
	 * @param autocomplete
	 */
	public void setAutoComplete(Boolean autoComplete) { if (autoComplete) setValue("autocomplete", ""); }
	
	/**
	 * Used with &lt;input>
	 * @param autoFocus
	 */
	public void setAutoFocus(Boolean autoFocus) { if (autoFocus) setValue("autofocus", ""); }
	
	/*
	autoplay // <audio>, <video>             ?
	capture // <input>                       ?
	charset // <meta>                        NOT NEEDED
	checked // <input>                       NOT NEEDED -- This is accomplished with the value
	cite // <blockquote>, <del>, <ins>, <q>  NOT NEEDED
*/	
	
	/**
	 * Global attribute.  Corresponds to HTML's class attribute.
	 * @param cssClass
	 */
	public void setCssClass(String cssClass) { setValue("class", cssClass); }
	
/*	
	cols // <textarea>                       ? -- Probably NOT NEEDED, as this is handled differently in the TextArea tag.
	colspan // <td>, <th>                    NOT NEEDED
	content // <meta>                        NOT NEEDED
	contenteditable // global                ? -- Might be useful.
	controls // <audio>, <video>             ?
	coords // <area>                         ? -- probably NOT NEEDED
	crossorigin // <audio>, <img>, <link>, <script>, <video>    ? -- might be useful
	data // <object>                         Java Object converted to data-* attributes.
	data-* // global                         NOT NEEDED -- the previous data attribute takes care of this.
	datetime // <del>, <ins>, <time>         NOT NEEDED -- this obviated by the CalendarSelect tag.
	decoding // <img>                        NOT NEEDED
	default // <track>                       NOT NEEDED
	defer // <script>                        ? -- Maybe if we create a Script tag similar to the Style tag.
*/	
	
	// dir // global
	public void setDir(String dir) { setValue("dir", dir); }
	
/*	
	dirname // <input>, <textarea>
*/		
	
	// disabled // <button>, <fieldset>, <input>, <optgroup>, <option>, <select>, <textarea>
	public void setDisabled(boolean disabled) { setValue("disabled", Boolean.toString(disabled)); }
	
/*	
	download // <a>, <area>
	draggable // global
	enctype // <form>
	enterkeyhint // <textarea>, contentEditable
	for // <label>, <output>
	form // <button>, <fieldset>, <input>, <label>, <meter>, <object>, <output>, <progress>, <select>, <textarea>
	formaction // <input>, <button>
	formenctype // <button>, <input>
	formmethod // <button>, <input>
	formnovalidate // <button>, <input>
	formtarget // <button>, <input>
	headers // <td>, <th>
	hidden // global
	high // <meter>
	href // <a>, <area>, <base>, <link>
	hreflang // <a>, <link>
	http-equiv // <meta>
*/	
	
	// id // global
	public void setId(String id) { setValue("id", id); }
	
/*	
	integrity // <link>, <script>
	inputmode // <textarea>, <contenteditable>
	ismap // <img>
*/	
	
	
	
	
	// Attributes
	//   (See "Attribute Support Functions" below.)
	// Common Attribute Setters
	public void setStyle(String style) { setValue("style", style); }
	public void setTitle(String title) { setValue("title", localize(title)); }
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
	
	/* =========================================
	   COMMON TAG METHODS
	   ========================================= */
	
	public static String cssAttr(String name, String value) { 
		return Data.isEmpty(value) ? "" : "%s:%s;".formatted(name, value);
	}
	
	public static String htmlAttr(String name, String value) { 
		return Data.isEmpty(value) ? "" : " %s=\"%s\"".formatted(name, value);
	}
	
	public String generateAttr() {
		Enumeration<String> values = getValues();
		StringBuilder result = new StringBuilder();
		while (values.hasMoreElements()) {
			String key = values.nextElement();
			if (key.startsWith("_"))
				continue;
			Object value = getValue(key);
			result.append(htmlAttr(key, value.toString()));
		}
		return result.toString();
	}
	
	/**
	 * Generate tag ready for render.
	 * @throws JspTagException
	 */
	public String generateHtml() throws JspTagException {
		String[][] attributes = getAttributes();
		String[] entities = getEntities();
		String result = getTemplate();
		for (int i = 0; i < attributes.length; i++) {
			StringBuilder attributeSet = new StringBuilder(""); 
			for (String attributeName : attributes[i]) {
				String attributeValue = getTypedValue("%s_%d".formatted(attributeName, i));
				attributeSet.append("%s=\"%s\" ".formatted(attributeName, attributeValue));
			}
			String replacement = getTypedValue(attributeSet.toString());
			result.replace("{ATTRIBUTES_%d}".formatted(i), replacement);
		}
		for (String e : entities) {
			String replacement = getTypedValue(e);
			result.replace("{%s}".formatted(e), replacement);
		}
		return result;
	}
	
	/*
	 * Generate an ID unique within the context of the page.
	 */
	public String generateId() {
		Integer g = (Integer)pageContext.getSession().getServletContext().getAttribute("HtmlTagIdGen");
		if (g == null) g = -1;
		g++;
		pageContext.getSession().getServletContext().setAttribute("HtmlTagIdGen", g);
		String core = Data.dateToStr(new Date(), "DDDHHmmss");
		return "autogen" + core + g.toString();
	}
	
	/*
	 * Get the tag's ID attribute.  If one doesn't exist, then an ID is generated
	 * to ensure that all tags have unique IDs.
	 */
	public String getId() {
		if (getValue("id") == null)
			setValue("id", generateId());
		return (String)getValue("id"); 
	}
	
	/*
	 * Get the value from the tag value set and then remove it.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRemoveValue(String k) {
		Object result = getValue(k);
		removeValue(k);
		return (T)result;
	}
	
	/*
	 * Get a value from the tag value set as the intended type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTypedValue(String k) { return (T)getValue(k); }
	
	/*
	 * Get a value from the tag value set as the intended type.  If the value is null, then the defaultValue is returned.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTypedValue(String k, T defaultValue) { return Data.nvl((T)getValue(k), defaultValue); }
	
	/**
	 * Determines whether the user has access to use the tag (e.g., clicking a button, or entering/changing the value of an input).
	 * Used with the rolesAllowed attribute.
	 * @return
	 */
	protected boolean hasAccess() {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String rolesAllowed = getTypedValue("_rolesAllowed");
		if (rolesAllowed == null)
			return true;
		if (request.getUserPrincipal() == null)
			return false;
		else {
			String[] roles = rolesAllowed.split(",");
			for (String role : roles)
				if (request.isUserInRole(role))
					return true;
			return false;
		}
	}
	
	/**
	 * Return the localized string for the given source.
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
	
	/*
	 * Use the page context to print text to page renderer.
	 */
	protected void print(String output) throws JspTagException {
		try { pageContext.getOut().print(output); }
        catch (IOException e) { throw new JspTagException(e.getMessage()); }
	}
	
	/*
	 * Use the page context to print a line to page renderer.
	 */
	protected void println(String output) throws JspTagException {
		try { pageContext.getOut().println(output); }
        catch (IOException e) { throw new JspTagException(e.getMessage()); }
	}
	
	public void render() throws JspTagException {
		render(generateHtml());
	}
	
	public void render(String html) throws JspTagException {
		print(html);
	}
	
	/* =========================================
	   ACTION TAG METHODS
	   ========================================= */
	
	@SuppressWarnings("unchecked")
	public void addParameter(ParameterData parameter) {
		if (getValue("_parameters") == null) 
			setValue("_parameters", new Vector<ParameterData>());
		((Vector<ParameterData>)getValue("_parameters")).add(parameter);
	}
	
	@SuppressWarnings("unchecked")
	public String getParameters() {
		Vector<ParameterData> parameters = (Vector<ParameterData>)getValue("_parameters");
		StringBuilder result = new StringBuilder("");
		if (parameters != null)
			for (int i = 0; i < parameters.size(); i++) {
				result.append(i > 1 ? "&" : "?");
				result.append("%s=%s".formatted(parameters.get(i).name(), parameters.get(i).value()));
			}
		return result.toString();
	}
	
	/**
	 * <b>getActionUrl</b><br><br>
	 * Translates a servlet action to a host-root-relative URL.
	 * @return the host-root-relative URL for the action attribute.
	 */
	public String getActionUrl() {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String action = (String)getRemoveValue("action");
		// A "#" is returned for an empty URL because some browsers will not
		// allow an anchor to function without a valid href, even if it has
		// an onclick event handler.
		return (Data.isEmpty(action)) ? "#" : request.getContextPath() + action + getParameters();
	}
	
	/**
	 * <b>evalHrefClickMap</b><br><br>
	 * Evaluates the href and onclick attributes, and adjusts them for form
	 * submission or reset, if applicable.
	 * @param attributes - The list of attributes.
	 */
	public void evalHrefClickMap() {
		String href = Data.nvl((String)getValue("href"));
		String onclick = (String)getValue("onclick");
		boolean submitForm = Data.nvl((String)getRemoveValue("formSubmit")).equals("true");
		boolean resetForm = Data.nvl((String)getRemoveValue("formReset")).equals("true");
		/* If submitForm or resetForm is true, then the href should be "#".  
		   If an actual href exists, it will be used later in the submission 
		   of the form, if submitForm is true.  It will not be used at all
		   if form reset is true. */
		setValue("href", (submitForm || resetForm) ? "#" : href);
		String onclick_ = (onclick == null) ? "" : onclick.trim();
		/* If an onclick event handler is present, then make sure it has a
		   semicolon at the end. */
		if (!onclick_.equals("") && (onclick_.charAt(onclick_.length() - 1) != ';')) 
			onclick_ += ";";
		 
		if (submitForm) {
			// Prepare the href for use in the form submission.
			String href_ = (href.equals("#")) ? "" : ", '" + href + "'";
			// Append the onclick event handler with the form submission code.
			onclick_ += " submitForm(this" + href_ + "); return false;";
		}
		else if (resetForm)
			onclick_ += " resetForm(this); return false;";
		else if (getValue("href").equals("#"))
			onclick_ += " return false;";
		
		if (!Data.isEmpty(onclick_)) 
			setValue("onclick", onclick_);
	}
}
