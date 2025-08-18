package com.roth.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.roth.export.annotation.JsonCollection;
import com.roth.export.annotation.JsonExtension;

@JsonExtension(extensionClass = ChartDatasetCartesian.class, field = "type", values = {"bar", "bubble", "combo", "line", "polar_area", "radar", "scatter"})
@JsonExtension(extensionClass = ChartDatasetPie.class, field = "type")
public abstract class ChartDataset implements Serializable {
	private static final long serialVersionUID = -6804633574567644142L;
	
	private ChartType type;
	private String label;
	private String pointStyle;
	private Boolean fill;
	private Double tension;
	private List<Double> data;
	
	public ChartDataset() {
		data = new ArrayList<>();
	}

	public ChartType getType() { return type; }
	public void setType(ChartType type) { this.type = type; }

	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }

	public String getPointStyle() { return pointStyle; }
	public void setPointStyle(String pointStyle) { this.pointStyle = pointStyle; }

	public Boolean getFill() { return fill; }
	public void setFill(Boolean fill) { this.fill = fill; }

	public Double getTension() { return tension; }
	public void setTension(Double tension) { this.tension = tension; }

	public List<Double> getData() { return data; }
	@JsonCollection(elementClass = Double.class)
	public void setData(List<Double> data) { this.data = data; }
}
