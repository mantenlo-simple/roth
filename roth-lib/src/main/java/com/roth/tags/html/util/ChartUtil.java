package com.roth.tags.html.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.roth.base.util.Data;
import com.roth.tags.html.model.ChartDataBean;

public class ChartUtil {
	public static final String[] CHART_COLORS = { 
		"\"rgb(255, 99, 132)\"",
		"\"rgb(255, 159, 64)\"",
		"\"rgb(255, 205, 86)\"",
		"\"rgb(75, 192, 192)\"",
		"\"rgb(54, 162, 235)\"", 
		"\"rgb(153, 102, 255)\"",
        "\"rgb(201, 203, 207)\"", 
        "\"slategray\"",
        "\"rgb(159, 128, 86)\"", 
        "\"rgb(211, 84, 0)\""};
	
	public enum ChartType {
		// Cartesian
		BAR ("bar", true),
		LINE ("line", true),
		BUBBLE ("bubble", true),
		SCATTER ("scatter", true),
		// Radial
		RADAR ("radar", false),
		DOUGHNUT ("doughnut", false),
		PIE ("pie", false),
		POLAR ("polar", false);
		
		private final String name;
		private final boolean cartesian;
		
		ChartType(String name, boolean cartesian) { 
			this.name = name;
			this.cartesian = cartesian;
		}
		
		public String getName() { return name; }
		public boolean isCartesian() { return cartesian; }
		public boolean isRadial() { return !cartesian; }
	}
	
	public enum DataType {
		INTEGER,
		FLOAT;
	}
	
	public enum PointStyle {
		CIRCLE ("circle"),
		CROSS ("cross"),
		CROSS_ROT ("cross_rot"),
		DASH ("dash"),
		LINE ("line"),
		RECT ("rect"),
		RECT_ROUNDED ("rect_rounded"),
		RECT_ROT ("rect_rot"),
		STAR ("star"),
		TRIANGLE ("triangle");
		
		private final String name;
		
		PointStyle(String name) { 
			this.name = name;
		}
		
		public String getName() { return name; }
	}
	
	public static final String CHART_AXIS_1 = "yAxis1";
	public static final String CHART_AXIS_2 = "yAxis2";
	
   	/**
	 * Get a chart JSON object to represent a chart.
	 * @param label
	 * @param source
	 * @return
	 */ // Bar/Line data
	public static String getCatesianChartJson(List<Object> source, String xAxisLabelGetterMethod, List<ChartDataBean> chartData) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
		String result = "";
		result += "{\"labels\":[";
		String data = "";
		Method method = null;
		for (Object bean : source) {
			if (method == null)
				method = Data.getDeclaredMethod(bean.getClass(), xAxisLabelGetterMethod);
			data += (data.isEmpty() ? "" : ",") + "" + (String)(method.invoke(bean)) + "";
		}
		result += data + "],\"datasets\":[";
		for (ChartDataBean bean : chartData) {
			result += getCartesianChartData(source, bean);			
		}
		result += "]}";
		return result;
	}
	
	/**
	 * Gets a Cartesian (line, bar, bubble, or scatter) chart JSON data set for use in a chart object.
	 * @param source
	 * @param chartDataSet
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	//public static String getChartDataset(String type, String axisId, boolean fill, String label, String color, String getterMethod, ArrayList<Object> source, String pointStyle) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	private static String getCartesianChartData(List<Object> source, ChartDataBean chartData) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String result = "{\"type\":\"" + chartData.getType() + "\"," + 
	                     "\"label\":\"" + chartData.getLabel() + "\"," +
	                     "\"backgroundColor\":" + chartData.getColor() + "," +
	                     "\"borderColor\":" + chartData.getColor() + "," +
	                     "\"borderWidth\":2," +
	                     (chartData.getPointStyle() == null ? "" : "\"pointStyle\":\"" + chartData.getPointStyle().getName() + "\",") +
				         "\"yAxisID\":\"" + chartData.getAxisId() + "\"," +
				         "\"fill\":" + chartData.getFill() + "," +
				         "\"data\":[";
		String data = "";
		for (Object bean : source) {
			Method method = Data.getDeclaredMethod(bean.getClass(), "get" + Data.upcaseFirst(chartData.getDataSource()));
			if (chartData.getDataType() == DataType.INTEGER)
				data += (data.isEmpty() ? "" : ",") + (Integer)(method.invoke(bean));
			else
				data += (data.isEmpty() ? "" : ",") + String.format("%.2f", (Double)(method.invoke(bean)));
		}
		result += data + "]}";
		return result;
	}
}
