package com.roth.chart;

public class ChartGridLines {
	private Boolean circular;
	private String color;
	private Boolean display;
	private Boolean drawOnChartArea;
	private Boolean drawTicks;
	
	public Boolean getCircular() { return circular; }
	public void setCircular(Boolean circular) { this.circular = circular; }
	
	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }
	
	public Boolean getDisplay() { return display; }
	public void setDisplay(Boolean display) { this.display = display; }
	
	public Boolean getDrawOnChartArea() { return drawOnChartArea; }
	public void setDrawOnChartArea(Boolean drawOnChartArea) { this.drawOnChartArea = drawOnChartArea; }
	
	public Boolean getDrawTicks() { return drawTicks; }
	public void setDrawTicks(Boolean drawTicks) { this.drawTicks = drawTicks; }
}
