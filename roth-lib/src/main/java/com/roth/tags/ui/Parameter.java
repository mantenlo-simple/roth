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
import jakarta.servlet.jsp.tagext.TagSupport;

import com.roth.tags.ui.util.ParameterData;

public class Parameter extends TagSupport {
	private static final long serialVersionUID = -8056835049809568438L;

	public void setName(String name) { setValue("name", name); }
	public void setValue(String value) { setValue("value", value); }
	
	public int doEndTag() throws JspException {
		RothTag a = (RothTag)findAncestorWithClass(this, RothTag.class);
		a.addParameter(new ParameterData((String)getValue("name"), (String)getValue("value")));
		release();
		return EVAL_PAGE;
	}
}
