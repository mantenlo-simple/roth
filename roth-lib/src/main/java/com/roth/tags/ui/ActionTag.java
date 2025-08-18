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

import java.util.Vector;

import com.roth.base.util.Data;
import com.roth.tags.html.util.ParameterData;

import jakarta.servlet.http.HttpServletRequest;

public abstract class ActionTag extends RothTag {
	private static final long serialVersionUID = -8658755771567478855L;

	@SuppressWarnings("unchecked")
	public void addParameter(ParameterData parameter) {
		if (getValue("_parameters") == null) setValue("_parameters", new Vector<ParameterData>());
		((Vector<ParameterData>)getValue("_parameters")).add(parameter);
	}
	
	// Attribute Setters
	public void setAction(String action) { setValue("action", action); }
	public void setCoords(String coords) { setValue("coords", coords); }
	public void setFormReset(boolean formReset) { setValue("formReset", Boolean.toString(formReset)); }
	public void setFormSubmit(boolean formSubmit) { setValue("formSubmit", Boolean.toString(formSubmit)); }
	public void setHref(String href) { setValue("href", href); }
	public void setHrefLang(String hreflang) { setValue("hreflang", hreflang); }
	public void setRel(String rel) { setValue("rel", rel); }
	public void setRev(String rev) { setValue("rev", rev); }
	public void setFormId(String formId) { setValue("formId", formId); }
	public void setShape(String shape) { setValue("shape", shape); }
	public void setTarget(String target) { setValue("target", target); }
	// Common Event Attribute Setters
	public void setOnBlur(String onBlur) { setValue("onblur", onBlur); }
	public void setOnFocus(String onFocus) { setValue("onfocus", onFocus); }
	
	public String getAnchorStart() {
		setValue("href", getActionUrl());
		evalHrefClickMap();
		boolean disabled = Data.nvl((String)getRemoveValue("disabled")).equals("true");
		
		if (disabled) {
			setValue("whenclick", getValue("onclick"));
			setValue("onclick", "return false;");
			setValue("class", getValue("class") + " disabled");
		}
		else if (getValue("onclick") != null) {
			String onclick = getTypedValue("onclick");
			setValue("onclick", onclick + (onclick.endsWith(";") ? "" : ";") + " return false;");
		}
		
		return "<a" + generateAttr() + ">";
	}
	
	public String getAnchorEnd() { return "</a>"; }
	public String getAnchor(String body) { return getAnchorStart() + body.trim() + getAnchorEnd(); }
	
	
	public static String getSimpleAnchor(String action, String onClick, String body) {
		return String.format("""
				<a href="%s" onclick="%s">%s</a>
				""", action, onClick, body);
	}
	
	
	@SuppressWarnings("unchecked")
	public String getParameters() {
		Vector<ParameterData> parameters = (Vector<ParameterData>)getValue("_parameters");
		String result = "";
		
		if (parameters != null)
			for (int i = 0; i < parameters.size(); i++)
				result += ((i == 0) ? "?" : "&") + parameters.get(i).getName() + "=" + parameters.get(i).getValue();
		return result;
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
	
	// NEW CODE
	/*
	
	public static String getButton(String type, String id, String href, String onclick, String label, String icon, String title, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("type", Data.nvl(type));
		params.put("href", Data.nvl(href, "#"));
		params.put("onclick", (Data.isEmpty(onclick) ? "" : onclick) + (Data.isEmpty(href) ? " return false;" :  ""));
		params.put("caption", label == null ? "" : String.format("<span>%s</span>", label));
		params.put("icon", Data.nvl(icon));
		params.put("title", Data.nvl(title));
		params.put("attributes", attributes);
		return applyTemplate("button", params);
	}
	
	public static String getTabset(String id, String tabs, String onselect, boolean fullWidth) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (tabs == null)
			throw new IllegalArgumentException("The tabs argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("fullwidth", fullWidth ? "full-width" : "");
		params.put("onselect", Data.nvl(onselect));
		params.put("tabs", tabs);
		return applyTemplate("tabset", params);
	}
	
	public static String getTab(int index, String id, String pageid, String label, String icon, String href, String title, boolean selected) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("index", Integer.toString(index));
		params.put("pageid", pageid);
		params.put("caption", Data.nvl(label));
		params.put("icon", Data.nvl(icon));
		params.put("href", Data.nvl(href, "#"));
		params.put("title", Data.nvl(title));
		params.put("checked", selected ? "checked" : "");
		return applyTemplate("tab", params);
	}
	*/
}
