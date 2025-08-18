package com.roth.chart;

import java.util.Map;

public class ChartConfig {
	private ChartData data;
	private ChartOptions options;
	private ChartType type;
	
	public ChartConfig() { }
	
	public ChartConfig(ChartType type, String title) {
		this.type = type;
		options = new ChartOptions(title);
		if (type.getCategory() == ChartCategory.CARTESIAN) {
			options.getInteracion().put("mode", "index");
			options.getInteracion().put("intersect", false);
			options.getElements().put("radius", 3);
		}
	}
	
	public ChartConfig(ChartType type, String title, String interactionMode) {
		this(type, title);
		setInteractionMode(interactionMode);
	}

	public ChartData getData() { return data; }
	public void setData(ChartData data) { this.data = data; }

	public ChartOptions getOptions() { return options; }
	public void setOptions(ChartOptions options) { this.options = options; }

	public ChartType getType() { return type; }
	public void setType(ChartType type) { this.type = type; }
	
	public ChartScale addScale(String name, ChartScalePosition position) {
		ChartScale result = new ChartScale();
		options.getScales().put(name, result);
		result.setDisplay(true);
		result.setPosition(position);
		return result;
	}
	
	public ChartScale addLinearScale(String name, ChartScalePosition position) {
		return addLinearScale(name, position, false);
	}
	
	public ChartScale addLinearScale(String name, ChartScalePosition position, boolean beginAtZero) {
		ChartScale result = addScale(name, position);
		result.setType(ChartScaleType.LINEAR);
		if (beginAtZero) {
			result.setTicks(new ChartTicks());
			result.getTicks().setBeginAtZero(beginAtZero);
		}
		return result;
	}
	
	public ChartScale addStackedScale(String name, ChartScalePosition position) {
		ChartScale result = addScale(name, position);
		result.setStacked(true);
		return result;
	}
	
	public ChartScale addStackedLinearScale(String name, ChartScalePosition position) {
		ChartScale result = addLinearScale(name, position);
		result.setStacked(true);
		return result;
	}
	
	public ChartTooltip addExternalTooltip(String handler, String position) {
		ChartTooltip tooltip = new ChartTooltip();
		tooltip.setEnabled(false);
		tooltip.setExternal(handler);
		tooltip.setMode("index");
		tooltip.setIntersect(false);
		tooltip.setPosition(position);
		options.getPlugins().put("tooltip", tooltip);
		return tooltip;
	}
	
	public ChartTooltip tooltip() {
		return (ChartTooltip)options.getPlugins().get("tooltip");
	}
	
	public ChartTooltip tooltip(String mode, boolean intersect, String position) {
		ChartTooltip tooltip = (ChartTooltip)options.getPlugins().get("tooltip");
		tooltip.setMode(mode);
		tooltip.setIntersect(intersect);
		tooltip.setPosition(position);
		return tooltip; 
	}
	
	public void setInteractionMode(String mode) {
		options.getInteracion().put("mode", mode);
	}
	
	public void setThumbnail() {
		switch (type.getCategory()) {
		case CARTESIAN:
			options.getElements().put("point", Map.of(
				"radius", "1", 
				"hoverRadius", "1",
				"hitRadius", "0"));
			options.getPlugins().put("legend", Map.of("display", false));
			options.getPlugins().put("tooltip", Map.of("enabled", false));
			options.getScales().forEach((n, s) -> {
				s.setDisplay(false);
				s.hideTicks();
				s.hideGrid();
			});
			if (options.getScales().get("x") == null) {
				ChartScale x = new ChartScale();
				x.setDisplay(false);
				x.hideTicks();
				x.hideGrid();
				options.getScales().put("x", x);
			}
			if (options.getScales().get("y") == null) {
				ChartScale y = new ChartScale();
				y.setDisplay(false);
				y.hideTicks();
				y.hideGrid();
				options.getScales().put("y", y);
			}
			break;
		case SEGMENT:
			options.getPlugins().put("legend", Map.of("display", false));
			options.getPlugins().put("title", Map.of("display", false));
			options.getPlugins().put("tooltip", Map.of("enabled", false));
			break;
		case POLAR:
			break;
		}
	}
}
