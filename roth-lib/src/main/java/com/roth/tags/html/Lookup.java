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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;

public class Lookup extends InputTag {
	private static final long serialVersionUID = -8151239384929063345L;
	
	public void setAction(String action) { setValue("action", action); }
	
	public int doEndTag() throws JspException {
		String value = (String)getValue("value");
		if (value == null) {
			Object dsValue = getDataSourceValue();
			value = (dsValue == null) ? "" : Data.obj2Str(dsValue);
			setValue("value", value);
		}
		String id = (String)getValue("id");
		if (id == null) { id = generateId(); setValue("id", id); }
		String action = getActionUrl();
		String parameters = "";
		String onclick = isReadOnly() ? "" : attr("onclick", "Roth.execDialog('" + id + "_lookup', '" + action + "', '" + parameters + "', 'Lookup', 'search');");
		String name = getRemoveValue("name");
		setValue("name", "_na");
		println(getDropdown("search", attr("keyid", id) + onclick));
		setValue("name", name);
		println(getInput("hidden", true));
		release();
		return EVAL_PAGE;
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
		return (Data.isEmpty(action)) ? "#" : request.getContextPath() + action;
	}
}
