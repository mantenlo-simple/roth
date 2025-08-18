package com.roth.chart;

import com.roth.export.annotation.JsonEnum;

@JsonEnum(valueMethod = "fromString")
public enum ChartScaleType {
	LINEAR,
	LOGARITHMIC,
	CATEGORY,
	TIME,
	TIMESERIES;
	
	public String getType() { return name().toLowerCase(); }
	
	@Override
	public String toString() { return getType(); }

	public static ChartScaleType fromString(String source) {
		return ChartScaleType.valueOf(source.toUpperCase());
	}
}
