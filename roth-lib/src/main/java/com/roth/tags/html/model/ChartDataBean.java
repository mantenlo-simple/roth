package com.roth.tags.html.model;

import java.io.Serializable;

import com.roth.tags.html.util.ChartUtil.ChartType;
import com.roth.tags.html.util.ChartUtil.DataType;
import com.roth.tags.html.util.ChartUtil.PointStyle;

public class ChartDataBean implements Serializable {
	private static final long serialVersionUID = -1142476426723279120L;
	
	ChartType type; 
	String axisId;
	boolean fill;
	String label;
	String color;
	String dataSource;
	DataType dataType;
	PointStyle pointStyle;
	
	public ChartType getType() { return type; }
	public void setType(ChartType type) { this.type = type; }
	
	public String getAxisId() { return axisId; }
	public void setAxisId(String axisId) { this.axisId = axisId; }
	
	public boolean getFill() { return fill; }
	public void setFill(boolean fill) { this.fill = fill; }
	
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	
	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }
	
	public String getDataSource() { return dataSource; }
	public void setDataSource(String dataSource) { this.dataSource = dataSource; }
	
	public DataType getDataType() { return dataType; }
	public void setDataType(DataType dataType) { this.dataType = dataType; }
	
	public PointStyle getPointStyle() { return pointStyle; }
	public void setPointStyle(PointStyle pointStyle) { this.pointStyle = pointStyle; }
}
