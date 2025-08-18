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

import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;

public class Wrap extends InputTag {
	private static final long serialVersionUID = 3031346893919120977L;
	
	public int doStartTag() throws JspException {
		String c = "roth-wrap ";
		String type = (String)getValue("_type");
		if (type != null && Data.in(type, new String[] {"button", "input"})) 
			c += type; 
		/*(type.equalsIgnoreCase("button")) ? "jwrpb" :
			                   (type.equalsIgnoreCase("input")) ? "jwrpi" : "";*/
		String style = Data.nvl((String)getValue("style"));
		String color = (String)getValue("_color");
		if (color != null) style += " background-color: " + color + ";";
		if (!Data.isEmpty(style)) style = attr("style", style); 
		String title = (String)getValue("title");
		title = title == null ? "" : attr("title", title);
		String id = (String)getValue("id");
		id = id == null ? "" : attr("id", id);
		print(tagStart("div", id + attr("class", c) + style + title));
		String label = (String)getValue("_label");
		if (label != null) print(tag("label", null /*attr("class", "jlbl")*/, label));
		return EVAL_BODY_INCLUDE; 
	}
	
	public int doEndTag() throws JspException {
		println(tagEnd("div"));
		release();
		return EVAL_PAGE;
	}
	
	public Form getForm() { return (Form)findAncestorWithClass(this, Form.class); }
	
	public boolean getReadOnly() { 
		Form form = (Form)findAncestorWithClass(this, Form.class);
		Wrap wrap = (Wrap)findAncestorWithClass(this, Wrap.class);
		Boolean ancestorReadonly = null;
        if ((wrap != null) && (wrap.getForm() == form)) 
        	ancestorReadonly = wrap.getReadOnly();
        else if (form != null) 
        	ancestorReadonly = form.getReadOnly();
		boolean readonly = false;
        if (!getBooleanValue("_override", false) && (ancestorReadonly != null) && (ancestorReadonly)) 
            readonly = true;
        else
            readonly = getBooleanValue("readonly", false); 
        return readonly;
	}

	// Attribute Setters
	public void setColor(String color) { setValue("_color", color); }
	public void setType(String type) { setValue("_type", type); }
}
