package com.roth.chart;

import java.util.HashMap;
import java.util.Map;

import com.roth.export.annotation.JsonMap;

public class ChartOptions {
	private ChartAnnotation annotation;
	private Double aspectRatio;
	private Double circumference;
	private String cutout;
	private Map<String,Object> elements;
	private Double hoverOffset;
	private Map<String,Object> interacion;
	private ChartLayout layout;
	private Map<String,Object> plugins;
	private Boolean responsive;
	private Double rotation;
	private Map<String,ChartScale> scales;
	
	public ChartOptions() { }
	
	public ChartOptions(String label) {
		aspectRatio = 16d/9;
		elements = new HashMap<>(
				Map.of("mode", "index", 
					   "intersect", false));
		interacion = new HashMap<>();
		layout = new ChartLayout();
		plugins = new HashMap<>(
				Map.of("title", Map.of("display", true, "text", label), 
					   "tooltip", new ChartTooltip("nearest"),
					   "legend", Map.of("display", true)));
		responsive = true;
		scales = new HashMap<>();
	}

	public ChartAnnotation getAnnotation() { return annotation; }
	public void setAnnotation(ChartAnnotation annotation) { this.annotation = annotation; }

	public Double getAspectRatio() { return aspectRatio; }
	public void setAspectRatio(Double aspectRatio) { this.aspectRatio = aspectRatio; }

	public Double getCircumference() { return circumference; }
	public void setCircumference(Double circumference) { this.circumference = circumference; }

	public String getCutout() { return cutout; }
	public void setCutout(String cutout) { this.cutout = cutout; }

	@JsonMap(keyClass = String.class, valueClass = Object.class)
	public Map<String, Object> getElements() { return elements; }
	public void setElements(Map<String, Object> elements) { this.elements = elements; }

	public Double getHoverOffset() { return hoverOffset; }
	public void setHoverOffset(Double hoverOffset) { this.hoverOffset = hoverOffset; }

	@JsonMap(keyClass = String.class, valueClass = Object.class)
	public Map<String, Object> getInteracion() { return interacion; }
	public void setInteracion(Map<String, Object> interacion) { this.interacion = interacion; }
	
	public ChartLayout getLayout() { return layout; }
	public void setLayout(ChartLayout layout) { this.layout = layout; }

	@JsonMap(keyClass = String.class, valueClass = Object.class)
	public Map<String, Object> getPlugins() { return plugins; }
	public void setPlugins(Map<String, Object> plugins) { this.plugins = plugins; }

	public Boolean getResponsive() { return responsive; }
	public void setResponsive(Boolean responsive) { this.responsive = responsive; }

	public Double getRotation() { return rotation; }
	public void setRotation(Double rotation) { this.rotation = rotation; }

	@JsonMap(keyClass = String.class, valueClass = ChartScale.class)
	public Map<String, ChartScale> getScales() { return scales; }
	public void setScales(Map<String, ChartScale> scales) { this.scales = scales; }
}
