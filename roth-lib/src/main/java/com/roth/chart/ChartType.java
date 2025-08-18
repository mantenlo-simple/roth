package com.roth.chart;

import com.roth.export.annotation.JsonEnum;

@JsonEnum(valueMethod = "fromString")
public enum ChartType {
	BAR ("bar", ChartCategory.CARTESIAN),
	BUBBLE ("bubble", ChartCategory.CARTESIAN),
	COMBO ("bar", ChartCategory.CARTESIAN),
	DOUGHNUT ("doughnut", ChartCategory.SEGMENT),
	LINE ("line", ChartCategory.CARTESIAN),
	PIE ("pie", ChartCategory.SEGMENT),
	POLAR_AREA ("polarArea", ChartCategory.POLAR),
	RADAR ("radar", ChartCategory.POLAR),
	SCATTER ("scatter", ChartCategory.CARTESIAN);

	private ChartCategory category;
	private String type;
	
	private ChartType(String type, ChartCategory category) {
		this.type = type;
		this.category = category;
	}
	
	public ChartCategory getCategory() { return category; }
	public String getType() { return type; }
	
	@Override
	public String toString() { return type; }
	
	public static ChartType fromString(String source) {
		for (ChartType ct : values())
			if (ct.getType().equals(source))
				return ct;
		throw new IllegalArgumentException(String.format("No ChartType value exists for type: '%s'.", source));
	}
}
