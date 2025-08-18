package com.roth.servlet.util;

import java.io.Serializable;

import com.roth.base.util.Data;

public class ColumnEntry implements Serializable {
	private static final long serialVersionUID = 4575072997591447819L;
	
	private String caption;
	private String dataSource;
	private Boolean key;
	private Boolean visible;
	private String width;
	private String dataSourceMap;
	private String boolValues;
	private IndicatorEntry indicator;
	
	public String getCaption() { return caption; }
	public void setCaption(String caption) { this.caption = caption; }
	
	public String getDataSource() { return dataSource; }
	public void setDataSource(String dataSource) { this.dataSource = dataSource; }
	
	public boolean isKey() { return Data.nvl(key, false); }
	public void setKey(boolean key) { this.key = key; }
	
	public boolean isVisible() { return Data.nvl(visible, true); }
	public void setVisible(boolean visible) { this.visible = visible; }
	
	public String getWidth() { return width; }
	public void setWidth(String width) { this.width = width; }
	
	public String getDataSourceMap() { return dataSourceMap; }
	public void setDataSourceMap(String dataSourceMap) { this.dataSourceMap = dataSourceMap; }
	
	public String getBoolValues() { return boolValues; }
	public void setBoolValues(String boolValues) { this.boolValues = boolValues; }
	
	public IndicatorEntry getIndicator() { return indicator; }
	public void setIndicator(IndicatorEntry indicator) { this.indicator = indicator; }
}