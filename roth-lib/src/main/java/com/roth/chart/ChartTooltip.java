package com.roth.chart;

public class ChartTooltip {
	private ChartCallbacks callbacks;
	private Boolean enabled;
	private String external;
	private Boolean intersect;
	private String mode;
	private String position;
	
	public ChartTooltip() {
		callbacks = new ChartCallbacks();
	}
	public ChartTooltip(String position) {
		this();
		this.position = position; 
	}
	
	public ChartCallbacks getCallbacks() { return callbacks; }
	public void setCallbacks(ChartCallbacks callbacks) { this.callbacks = callbacks; }
	
	public Boolean getEnabled() { return enabled; }
	public void setEnabled(Boolean enabled) { this.enabled = enabled; }
	
	public String getExternal() { return external; }
	public void setExternal(String external) { this.external = external; }
	
	public Boolean getIntersect() { return intersect; }
	public void setIntersect(Boolean intersect) { this.intersect = intersect; }
	
	public String getMode() { return mode; }
	public void setMode(String mode) { this.mode = mode; }
	
	public String getPosition() { return position; }
	public void setPosition(String position) { this.position = position; }
}
