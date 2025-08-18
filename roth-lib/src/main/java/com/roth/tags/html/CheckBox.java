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

public class CheckBox extends InputTag {
	private static final long serialVersionUID = 7961706972930243089L;

	public void setBoolValues(String boolValues) { setValue("_boolValues", boolValues); }
	public void setWrap(boolean wrap) { setValue("_wrap", wrap); }
	
	public int doEndTag() throws JspException {
		String value = (String)getRemoveValue("value");
		String boolValues = Data.nvl((String)getValue("_boolValues"), "false|true");
		String[] bool = boolValues.split("\\|");
		String id = (String)getValue("id");
		if (id == null) { id = generateId(); setValue("id", id); }
		//String hid = generateId();
		//setValue("_hid", hid);
		//String onclick = "_$('" + hid + "').value = this.checked ? '" + bool[1] + "' : '" + bool[0] + "';" +
		//		Data.nvl((String)getValue("onclick"));
		//setValue("onclick", onclick);
		if (value == null) {
			Object dsValue = getDataSourceValue();
			value = (dsValue == null) ? bool[0] : dsValue.toString();
		}
		setValue("value", value);
		if (value.equals(bool[1])) setValue("checked", "checked");
		//println(getInput("checkbox"));
		println(getCheckBox(id, getName(), getEscapedValue(), (String)getValue("_label"), (String)getValue("title"), 
				bool[0], bool[1], (String)getRemoveValue("onclick"), getHTMLAttributes()));
		release();
		return EVAL_PAGE;
	}
}
