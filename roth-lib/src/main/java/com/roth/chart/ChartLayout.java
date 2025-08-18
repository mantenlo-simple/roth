package com.roth.chart;

import java.util.HashMap;
import java.util.Map;

public class ChartLayout {
	private Map<String,Double> padding;
	
	public ChartLayout() {
		padding = new HashMap<>();
	}

	public Map<String, Double> getPadding() { return padding; }
	public void setPadding(Map<String, Double> padding) { this.padding = padding; }
}
