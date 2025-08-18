package com.roth.chart;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.roth.base.util.Data;
import com.roth.export.annotation.JsonCollection;

public class ChartDatasetPie extends ChartDataset {
	private static final long serialVersionUID = -6804633574567644142L;
	
	private List<String> backgroundColor;
	private List<String> borderColor;
	private List<Double> borderWidth;
	
	public ChartDatasetPie() {
		super();
	}

	public List<String> getBackgroundColor() { return backgroundColor; }
	@JsonCollection(elementClass = String.class)
	public void setBackgroundColor(List<String> backgroundColor) { this.backgroundColor = backgroundColor; }

	public List<String> getBorderColor() { return borderColor; }
	@JsonCollection(elementClass = String.class)
	public void setBorderColor(List<String>  borderColor) { this.borderColor = borderColor; }

	public List<Double> getBorderWidth() { return borderWidth; }
	@JsonCollection(elementClass = Double.class)
	public void setBorderWidth(List<Double> borderWidth) { this.borderWidth = borderWidth; }

	/**
	 * Create a chart data set.
	 * @param type
	 * @param axisId
	 * @param fill
	 * @param label
	 * @param fieldName
	 * @param color
	 * @param source
	 * @param pointStyle
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	static ChartDatasetPie createDataset(ChartType type, String label, String fieldName, List<String> color, List<?> source, Class<?> rowClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ChartDatasetPie result = new ChartDatasetPie();
		result.setType(type);
		result.setLabel(label);
		result.setBackgroundColor(color);
		result.setBorderColor(color);
		//result.setBorderWidth(2d);
		result.setTension(0.4);
		for (Object report: source) {
			Method getter = Data.getDeclaredMethod(rowClass, Data.getGetterName(fieldName));
			Number value = (Number)getter.invoke(report);
			result.getData().add(value.doubleValue());
		}
		return result;
	}
}
