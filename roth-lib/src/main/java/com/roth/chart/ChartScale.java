package com.roth.chart;

public class ChartScale {
	private String callback;
	private Boolean display;
	private ChartGridLines grid;
	private ChartScalePosition position;
	private Boolean stacked;
	private ChartTicks ticks;
	private ChartScaleType type; // ChartScaleType.LINEAR
	
	public String getCallback() { return callback; }
	public void setCallback(String callback) { this.callback = callback; }
	
	public Boolean isDisplay() { return display; }
	public void setDisplay(Boolean display) { this.display = display; }
	
	public ChartGridLines getGrid() { return grid; }
	public void setGrid(ChartGridLines grid) { this.grid = grid; }
	
	public ChartScalePosition getPosition() { return position; }
	public void setPosition(ChartScalePosition position) { this.position = position; }
	
	public Boolean getStacked() { return stacked; }
	public void setStacked(Boolean stacked) { this.stacked = stacked; }
	
	public ChartTicks getTicks() { return ticks; }
	public void setTicks(ChartTicks ticks) { this.ticks = ticks; }
	
	public ChartScaleType getType() { return type; }
	public void setType(ChartScaleType type) { this.type = type; }
	
	public ChartTicks setTicks(boolean beginAtZero) {
		ticks = new ChartTicks();
		ticks.setBeginAtZero(beginAtZero);
		return ticks;
	}
	public ChartTicks setTicks(Double min, Double max) {
		return setTicks(min, max, null);
	}
	public ChartTicks setTicks(Double min, Double max, String callback) { 
		ticks = new ChartTicks();
		ticks.setBeginAtZero(min == 0);
		ticks.setMin(min);
		ticks.setMax(max);
		return ticks;
	}
	public void hideGrid() {
		grid = new ChartGridLines();
		//grid.setDisplay(false);
		grid.setColor("#0000000a");
	}
	public void hideTicks() {
		ticks = new ChartTicks();
		ticks.setDisplay(false);
	}
}
