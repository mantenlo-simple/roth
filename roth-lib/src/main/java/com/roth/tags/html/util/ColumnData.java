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
package com.roth.tags.html.util;

import java.io.Serializable;

import com.roth.base.util.Data;

public class ColumnData implements Serializable {
	private static final long serialVersionUID = -2723332866986704623L;

	private String caption;
	private Object dataMap;
	private String dataSource;
	private String formatName;
	private boolean key;
	private boolean movable;
	private String pattern;
	private boolean sizable;
	private boolean sortable;
	private boolean visible;
	private String width;
	
	public ColumnData(Boolean movable, Boolean sizable, Boolean sortable) {
		visible = true;
		if (movable != null) this.movable = movable.booleanValue();
		if (sizable != null) this.sizable = sizable.booleanValue();
		if (sortable != null) this.sortable = sortable.booleanValue();
		width = "6em";
	}
	
	public String getCaption() { return caption; }
	public void setCaption(String caption) { this.caption = caption; }
	
	public Object getDataMap() { return dataMap; }
	public void setDataMap(Object dataMap) { this.dataMap = dataMap; }

	public String getDataSource() { return dataSource; }
	public void setDataSource(String dataSource) { this.dataSource = dataSource; }
	
	public String getFormatName() { return formatName; }
	public void setFormatName(String formatName) { this.formatName = formatName; }

	public boolean getKey() { return key; }
	public void setKey(boolean key) { this.key = key; }
	
	public boolean getMovable() { return movable; }
	public void setMovable(boolean movable) { this.movable = movable; }
	
	public String getPattern() { return pattern; }
	public void setPattern(String pattern) { this.pattern = pattern; }

	public boolean getSizable() { return sizable; }
	public void setSizable(boolean sizable) { this.sizable = sizable; }
	
	public boolean getSortable() { return sortable; }
	public void setSortable(boolean sortable) { this.sortable = sortable; }
	
	public boolean getVisible() { return visible; }
	public void setVisible(boolean visible) { this.visible = visible; }
	
	public String getWidth() { return width == null ? null : width + (Data.isNumeric(width) ? "px" : ""); }
	public void setWidth(String width) { this.width = width; }
}
