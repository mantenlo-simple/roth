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

import com.roth.base.util.Data;

import jakarta.servlet.jsp.JspException;

public class Break extends RothTag {
	private static final long serialVersionUID = 5751705640152922123L;

	private static final String[][] ATTRIBUTES = {{"style"}};
	private static final String[] ENTITIES = null;
	private static final String TEMPLATE = """
		<div class="rbreak" %s></div>
		""";
	
	public void setHeight(String height) { 
		if (height == null)
			return;
		String style = cssAttr("height", Data.isNumeric(height) ? height + "px" : height);
		setValue("style", style); 
	}
	
	public int doEndTag() throws JspException {
		render();
		release();
		return EVAL_PAGE;
	}

	@Override
	public String[][] getAttributes() {
		return ATTRIBUTES;
	}

	@Override
	public String[] getEntities() {
		return ENTITIES;
	}

	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
}
