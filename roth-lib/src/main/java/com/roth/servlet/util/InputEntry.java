package com.roth.servlet.util;

import java.io.Serializable;

import com.roth.base.util.Data;

public class InputEntry implements Serializable {
	private static final long serialVersionUID = 3821232952294277349L;
	
	private String dataSource; // only needs to be the field name
	private String label;
	private String optionsDataSource;
	private Boolean required;
	private String type; // break, spacer, hidden, textBox, textArea, calendarSelect, select, checkBox, radioGroup
	private String width;
	private String height;
	private Integer maxLength; 
	private Boolean readonly;
	private Boolean showTime;
	private Boolean nullable;
	private String boolValues;
	private IndicatorEntry indicator;
	private String onChange;
	private Boolean worm; // Write Once Read Many i.e., readonly on update
	
	public String getDataSource() { return dataSource; }
	public void setDataSource(String dataSource) { this.dataSource = dataSource; }
	
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	
	public String getOptionsDataSource() { return optionsDataSource; }
	public void setOptionsDataSource(String optionsDataSource) { this.optionsDataSource = optionsDataSource; }
	
	public boolean isRequired() { return Data.nvl(required, false); }
	public void setRequired(boolean required) { this.required = required; }
	
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	
	public String getWidth() { return width; }
	public void setWidth(String width) { this.width = width; }
	
	public String getHeight() { return height; }
	public void setHeight(String height) { this.height = height; }
	
	public Integer getMaxLength() { return maxLength; }
	public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
	
	public boolean isReadonly() { return Data.nvl(readonly, false); }
	public void setReadonly(boolean readonly) { this.readonly = readonly; }
	
	public boolean isShowTime() { return Data.nvl(showTime, false); }
	public void setShowTime(boolean showTime) { this.showTime = showTime; }
	
	public boolean isNullable() { return Data.nvl(nullable, false); }
	public void setNullable(boolean nullable) { this.nullable = nullable; }
	
	public String getBoolValues() { return boolValues; }
	public void setBoolValues(String boolValues) { this.boolValues = boolValues; }
	
	public IndicatorEntry getIndicator() { return indicator; }
	public void setIndicator(IndicatorEntry indicator) { this.indicator = indicator; }
	
	public String getOnChange() { return onChange; }
	public void setOnChange(String onChange) { this.onChange = onChange; }
	
	public boolean getWorm() { return Data.nvl(worm, false); }
	public void setWorm(boolean worm) { this.worm = worm; }
	/*
	public static InputEntry formBreak() { return formBreak("0"); }
	public static InputEntry formBreak(String height) {
		return new InputEntry(null, null, null, false, "break", null, height, null, false, false, null, null);
	}
	
	public static InputEntry spacer(String width) {
		return new InputEntry(null, null, null, false, "spacer", width, null, null, false, false, null, null);
	}
	
	public static InputEntry hidden(String dataSource) {
		return new InputEntry(dataSource, null, null, false, "hidden", null, null, null, false, false, null, null);
	}
	
	public static InputEntry textBox(String label, String dataSource, boolean required, String width, Integer maxLength) {
		return new InputEntry(dataSource, label, null, required, "textBox", width, null, maxLength, false, false, null, null);
	}
	
	public static InputEntry textArea(String label, String dataSource, boolean required, String width, String height, Integer maxLength) {
		return new InputEntry(dataSource, label, null, required, "textArea", width, height, maxLength, false, false, null, null);
	}
	
	public static InputEntry select(String label, String dataSource, String optionsDataSource, boolean nullable, boolean required, String width) {
		return new InputEntry(dataSource, label, optionsDataSource, required, "select", width, null, null, false, nullable, null, null);
	}
	
	public static InputEntry calendarSelect(String label, String dataSource, boolean required, String width, boolean showTime) {
		return new InputEntry(dataSource, label, null, required, "calendarSelect", width, null, null, showTime, false, null, null);
	}
	
	public static InputEntry checkBox(String label, String dataSource, String boolValues) {
		return new InputEntry(dataSource, label, null, false, "checkBox", null, null, null, false, false, boolValues, null);
	}
	
	// radioGroup
	
	public static InputEntry indicator(String indicator) {
		return new InputEntry(null, null, null, false, "indicator", null, null, null, false, false, null, indicator);
	}
	*/
}