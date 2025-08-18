package com.roth.chart;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.roth.base.util.Data;

public class ChartDatasetCartesian extends ChartDataset {
	private static final long serialVersionUID = -6804633574567644142L;
	
	private String backgroundColor;
	private String borderColor;
	private Double borderWidth;
	private String yAxisID;
	
	public ChartDatasetCartesian() {
		super();
	}

	public String getBackgroundColor() { return backgroundColor; }
	public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

	public String getBorderColor() { return borderColor; }
	public void setBorderColor(String  borderColor) { this.borderColor = borderColor; }

	public Double getBorderWidth() { return borderWidth; }
	public void setBorderWidth(Double borderWidth) { this.borderWidth = borderWidth; }

	public String getYAxisID() { return yAxisID; }
	public void setYAxisID(String yAxisID) { this.yAxisID = yAxisID; }
	
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
	static ChartDatasetCartesian createDataset(ChartType type, String axisId, String label, String fieldName, String color, List<?> source, Class<?> rowClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ChartDatasetCartesian result = new ChartDatasetCartesian();
		result.setType(type);
		result.setYAxisID(axisId);
		result.setLabel(label);
		result.setBackgroundColor(color);
		result.setBorderColor(color);
		result.setBorderWidth(2d);
		result.setTension(0.4);
		for (Object report: source) {
			Method getter = Data.getDeclaredMethod(rowClass, Data.getGetterName(fieldName));
			Number value = (Number)getter.invoke(report);
			result.getData().add(value.doubleValue());
		}
		return result;
	}
}
