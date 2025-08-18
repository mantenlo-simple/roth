package com.roth.chart;

public class ChartTicks {
	private Boolean beginAtZero;
	private String callback;
	private Boolean display;
	private Double max;
	private Double min;
	
	// callback should be converted to a function by JavaScript
	/* Example
	"function(value, index, values) { return percent ? Math.round(value * 100) + '%' : Math.round(value * 10) / 10; }"
    */

	public Boolean getBeginAtZero() { return beginAtZero; }
	public void setBeginAtZero(Boolean beginAtZero) { this.beginAtZero = beginAtZero; }
	
	public String getCallback() { return callback; }
	public void setCallback(String callback) { this.callback = callback; }
	
	public Boolean getDisplay() { return display; }
	public void setDisplay(Boolean display) { this.display = display; }
	
	public Double getMax() { return max; }
	public void setMax(Double max) { this.max = max; }
	
	public Double getMin() { return min; }
	public void setMin(Double min) { this.min = min; }
}
