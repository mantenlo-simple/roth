package com.roth.chart;

import com.roth.export.annotation.JsonEnum;

@JsonEnum(valueMethod = "fromString")
public enum ChartScalePosition {
	BOTTOM,
	LEFT,
	RIGHT,
	TOP;
	
	public String getType() { return name().toLowerCase(); }
	
	@Override
	public String toString() { return getType(); }

	public static ChartScalePosition fromString(String source) {
		return ChartScalePosition.valueOf(source.toUpperCase());
	}
}
