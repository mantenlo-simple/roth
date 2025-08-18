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

public class Row extends HtmlTag {
	private static final long serialVersionUID = -3160351459355442083L;

	// Attributes
	public void setCssClass(String cssClass) { setValue("class", cssClass); }
	public void setStyle(String style) { setValue("style", style); }
	
	// Event Handlers
	public void setOnClick(String onClick) { setValue("onclick", onClick); }
	public void setOnDblClick(String onDblClick) { setValue("ondblclick", onDblClick); }
	
	public int doEndTag() throws JspException {
		int rowIndex = ((Integer)pageContext.getAttribute("rowIndex")).intValue();
		DataGrid ancestor = (DataGrid)findAncestorWithClass(this, DataGrid.class);
		
		if (rowIndex > -1) {
			if (getValue("class") != null) ancestor.setValue("rowClass_" + rowIndex, getValue("class"));
			if (getValue("style") != null) ancestor.setValue("rowStyle_" + rowIndex, getValue("style"));
			if (getValue("onclick") != null) ancestor.setValue("rowOnClick_" + rowIndex, getValue("onclick"));
			if (getValue("ondblclick") != null) ancestor.setValue("rowOnDblClick_" + rowIndex, getValue("ondblclick"));
		}
		
		release();
		return EVAL_PAGE;
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
