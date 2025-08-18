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
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import com.roth.tags.html.util.ColumnData;

public class Column extends BodyTagSupport {
	private static final long serialVersionUID = -6939976189171355548L;
	
	public void setCaption(String caption) { setColumnDataValue("caption", caption); }
	public void setCssClass(String cssClass) { setValue("cssClass", cssClass); }
	public void setDataMap(Object dataMap) { if (dataMap != null) setColumnDataValue("dataMap", dataMap); }
	public void setDataSource(String dataSource) { setColumnDataValue("dataSource", dataSource); }
	public void setFormatName(String formatName) { setColumnDataValue("formatName", formatName); }
	public void setKey(boolean key) { setColumnDataValue("key", key); }
	public void setMovable(boolean movable) { setColumnDataValue("movable", movable); }
	public void setPattern(String pattern) { setColumnDataValue("pattern", pattern); }
	public void setSizable(boolean sizable) { setColumnDataValue("sizable", sizable); }
	public void setSortable(boolean sortable) { setColumnDataValue("sortable", sortable); }
	public void setStyle(String style) { setValue("style", style); }
	public void setVisible(boolean visible) { setColumnDataValue("visible", visible); }
	public void setWidth(String width) { setColumnDataValue("width", width); }
	
	public ColumnData getColumnData() { return (ColumnData)getValue("columnData"); }
	public void setColumnDataValue(String name, Object value) {
		DataGrid ancestor = (DataGrid)findAncestorWithClass(this, DataGrid.class);
		if (getValue("columnData") == null) setValue("columnData", new ColumnData(ancestor.getColMoving(), ancestor.getColSizing(), ancestor.getColSorting()));
		if (name.equals("caption"))
			((ColumnData)getValue("columnData")).setCaption((String)value);
		else if (name.equals("dataMap"))
			((ColumnData)getValue("columnData")).setDataMap(value);
		else if (name.equals("dataSource"))
			((ColumnData)getValue("columnData")).setDataSource((String)value);
		else if (name.equals("formatName"))
			((ColumnData)getValue("columnData")).setFormatName((String)value);
		else if (name.equals("key"))
			((ColumnData)getValue("columnData")).setKey(Boolean.class.cast(value).booleanValue());
		else if (name.equals("movable"))
			((ColumnData)getValue("columnData")).setMovable(Boolean.class.cast(value).booleanValue());
		else if (name.equals("pattern"))
			((ColumnData)getValue("columnData")).setPattern((String)value);
		else if (name.equals("sizable"))
			((ColumnData)getValue("columnData")).setSizable(Boolean.class.cast(value).booleanValue());
		else if (name.equals("sortable"))
			((ColumnData)getValue("columnData")).setSortable(Boolean.class.cast(value).booleanValue());
		else if (name.equals("visible"))
			((ColumnData)getValue("columnData")).setVisible(Boolean.class.cast(value).booleanValue());
		else if (name.equals("width"))
			((ColumnData)getValue("columnData")).setWidth((String)value);
	}
	
	public int doEndTag() throws JspException {
		int rowIndex = ((Integer)pageContext.getAttribute("rowIndex")).intValue();
		DataGrid ancestor = (DataGrid)findAncestorWithClass(this, DataGrid.class);
		
		if (rowIndex == -1)
			ancestor.addColumn((ColumnData)getValue("columnData"));
		else if ((bodyContent != null) && (getColumnData().getDataSource() != null)) 
			ancestor.setValue(rowIndex + "_" + getColumnData().getDataSource() + "_value", bodyContent.getString());
		
		String cssClass = (String)getValue("cssClass");
		if (cssClass != null) ancestor.setValue(rowIndex + "_" + getColumnData().getDataSource() + "_class", cssClass);
		String style = (String)getValue("style");
		if (style != null) ancestor.setValue(rowIndex + "_" + getColumnData().getDataSource() + "_style", style);
			
		release();
		return EVAL_PAGE;
	}
}
