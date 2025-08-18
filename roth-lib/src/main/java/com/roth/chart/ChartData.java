package com.roth.chart;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.roth.export.annotation.JsonCollection;

public class ChartData implements Serializable {
	private static final long serialVersionUID = -850303645742094088L;
	
	private List<?> source;
	private Class<?> rowClass;
	
	private List<String> labels;
	private List<ChartDataset> datasets;

	public ChartData() { }
	
	public ChartData(List<?> source, Class<?> rowClass) {
		this.source = source;
		this.rowClass = rowClass;
		
		labels = new ArrayList<>();
		datasets = new ArrayList<>();
	}
	
	public List<String> getLabels() { return labels; }
	@JsonCollection(elementClass = String.class)
	public void setLabels(List<String> labels) { this.labels = labels; }
	
	public List<ChartDataset> getDatasets() { return datasets; }
	@JsonCollection(elementClass = ChartDataset.class)
	public void setDatasets(List<ChartDataset> datasets) { this.datasets = datasets; }
	
	public void addLine(String axisId, String label, String fieldName, String color) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ChartDataset dataset = ChartDatasetCartesian.createDataset(ChartType.LINE, axisId, label, fieldName, color, source, rowClass);
		datasets.add(dataset);
	}
	
	public void addBar(String axisId, String label, String fieldName, String color) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ChartDataset dataset = ChartDatasetCartesian.createDataset(ChartType.BAR, axisId, label, fieldName, color, source, rowClass);
		datasets.add(dataset);
	}
	
	public void addSegment(String label, String fieldName, String color) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ChartDataset dataset = ChartDatasetPie.createDataset(ChartType.PIE, label, fieldName, new ArrayList<>(), source, rowClass);
		datasets.add(dataset);
	}
}
