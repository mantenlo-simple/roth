package com.roth.chart;

import java.util.HashMap;
import java.util.Map;

import com.roth.base.annotation.Ignore;

public class ChartAnnotation {
	private String type;        // "line"
	private String mode;        // "vertical" "horizontal"
	private String scaleID;     // "x-axis-0"
	private String value;       // Might be a function call?
	private String borderColor; // CSS color
	private Map<String,Object> label;
	
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public String getMode() { return mode; }
	public void setMode(String mode) { this.mode = mode; }

	public String getScaleID() { return scaleID; }
	public void setScaleID(String scaleID) { this.scaleID = scaleID; }

	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }

	public String getBorderColor() { return borderColor; }
	public void setBorderColor(String borderColor) { this.borderColor = borderColor; }

	public Map<String, Object> getLabel() {
		if (label == null)
			label = new HashMap<>();
		return label; 
	}
	public void setLabel(Map<String, Object> label) { this.label = label; }

	@Ignore
	void setLabelAlt(String content, Boolean enabled, String position) {
		getLabel();
		label.put("content", content);
		label.put("enabled", enabled);
		label.put("position", position); // "bottom" "top" "left" "right"
	}
}
