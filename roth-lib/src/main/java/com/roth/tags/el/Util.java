/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.roth.tags.el;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

/**
 * 
 * @author James M. Payne
 *
 */
public class Util implements Serializable {
	private static final long serialVersionUID = 342902884886824183L;

	public static boolean bitSet(int a, int bit) { return (a & bit) == bit; }
	public static int bitAnd(int a, int b) { return a & b; }
	public static int bitOr(int a, int b) { return a | b; }
	public static int bitXor(int a, int b) { return a ^ b; }
	public static int bitNot(int a) { return ~a; }
	public static int bitNor(int a, int b) { return ~(a | b); }
	public static int bitNand(int a, int b) { return ~(a & b); }
	
	public static int index2bit(int index) { 
		int result = 1; 
		for (int i = 0; i < index; i++) 
			result *= 2; 
		return result; 
	}
	
	public static int lesserOf(int a, int b) { return (a < b) ? a : b; }
	public static int greaterOf(int a, int b) { return (a > b) ? a : b; }
	
	public static Object now(String type) {
		switch (type) {
			case "ZonedDateTime": return ZonedDateTime.now();
			case "OffsetDateTime": return OffsetDateTime.now();
			case "LocalDateTime": return LocalDateTime.now();
			case "LocalDate": return LocalDate.now();
			case "LocalTime": return LocalTime.now();
			case "util.Date": return new java.util.Date();
			case "sql.Date": return java.sql.Date.valueOf(LocalDate.now());
			case "sql.Time": return java.sql.Time.valueOf(LocalTime.now());
			case "sql.Timestamp": return java.sql.Timestamp.valueOf(LocalDateTime.now());
			default: throw new IllegalArgumentException("The type argument must be one of 'ZonedDateTime', 'OffsetDateTime', 'LocalDateTIme', 'LocalDate', 'LocalTime',"
					+ "'util.Date', 'sql.Date', 'sql.Time', or 'sql.Timestamp'.");
		} 
	}
	
	public static Object date(String type, int year, int month, int date, int hourOfDay, int minute, int second) {
		LocalDateTime origin = LocalDateTime.of(year, month, date, hourOfDay, minute, second, 0);
		switch (type) {
			case "ZonedDateTime": return origin.atZone(ZoneId.systemDefault());
			case "OffsetDateTime": return origin.atOffset(ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()));
			case "LocalDateTime": return origin;
			case "LocalDate": return origin.toLocalDate();
			case "LocalTime": return origin.toLocalTime();
			case "util.Date": return java.util.Date.from(origin.atZone(ZoneId.systemDefault()).toInstant());
			case "sql.Date": return java.sql.Date.valueOf(origin.toLocalDate());
			case "sql.Time": return java.sql.Time.valueOf(origin.toLocalTime());
			case "sql.Timestamp": return java.sql.Timestamp.valueOf(origin);
			default: throw new IllegalArgumentException("The type argument must be one of 'ZonedDateTime', 'OffsetDateTime', 'LocalDateTIme', 'LocalDate', 'LocalTime',"
					+ "'util.Date', 'sql.Date', 'sql.Time', or 'sql.Timestamp'.");
		} 
	}
	
	public static Object truncDate(Object source) {
		if (source == null)
			return null;
		else if (source instanceof LocalDate || source instanceof LocalTime ||
				 source instanceof java.sql.Date || source instanceof java.sql.Time)
			return source;
		else if (source instanceof ZonedDateTime)
			return ((ZonedDateTime)source).truncatedTo(ChronoUnit.DAYS);
		else if (source instanceof OffsetDateTime)
			return ((OffsetDateTime)source).truncatedTo(ChronoUnit.DAYS);
		else if (source instanceof LocalDateTime)
			return ((LocalDateTime)source).truncatedTo(ChronoUnit.DAYS);
		else if (source instanceof java.util.Date)
			return Data.truncDate((java.util.Date) source);
		else if (source instanceof java.sql.Timestamp)
			return java.sql.Timestamp.from(((java.sql.Timestamp)source).toInstant().truncatedTo(ChronoUnit.DAYS));
		else
			throw new IllegalArgumentException("The source argument is not a valid type.");
	}
	
	public static final Object dateAdd(Object source, String unit, int count) {
		return Data.dateAdd(source, unit, count);
	}
	
	public static final String formatDate(Object source, String format) {
		if (source instanceof String ssource)
			return Data.dateToStr(ssource.length() == 10 ? Data.strToDate(ssource) : Data.strToDts(ssource), format);
		return Data.dateToStr(source, format);
	}
	
	public static LocalDateTime truncLocalDateTime(LocalDateTime source) {
		return source.truncatedTo(ChronoUnit.DAYS);
	}
	
	public static boolean in(Object item, Object[] array) {
		return Data.in(item, array);
	}
	
	public static String jspInstId(PageContext pageContext) {
		String jspInstId = (String)pageContext.getAttribute("jspInstId");
		if (jspInstId == null) {
			jspInstId = Long.toUnsignedString(Long.valueOf(Data.dateToStr(LocalDateTime.now(), "HHmmssSSS")), Character.MAX_RADIX);
			pageContext.setAttribute("jspInstId", jspInstId);
		}
		return jspInstId;
	}
	
	public static String encrypt(String source, long seed) {
		return Data.encrypt(source, seed);
	}
	
	public static String decrypt(String source, long seed) {
		return Data.decrypt(source, seed);
	}
	
	public static String encodeURL(PageContext pageContext, String url) {
		return ((HttpServletResponse)pageContext.getResponse()).encodeURL(url);
	}
	
	public static String encodeRedirectURL(PageContext pageContext, String url) {
		return ((HttpServletResponse)pageContext.getResponse()).encodeRedirectURL(url);
	}
	
	public static String contextRoot(PageContext pageContext) {
		return pageContext.getServletContext().getContextPath();
	}
	
	private static ArrayList<String> registeredPaths;
	
	static {
		registeredPaths = new ArrayList<>();
	}
	
	public static boolean isRegisteredPath(String path) {
		int s = 1;
		for (int i = 0; i < 3; i++)
			s = path.indexOf('/', s);
		String checkPath = s > 0 ? path.substring(0, s) : path;
		boolean result = registeredPaths.contains(checkPath);
		Log.logDebug("Checking if path is registered: " + path + " | " + checkPath + " | " + result, null, "isRegisteredPath");
		return result;
	}
	
	public static String registerPath(String path) {
		if (!isRegisteredPath(path)) {
			Log.logDebug("Registering path: " + path, null, "registerPath");
			registeredPaths.add(path);
		}
		return path;
	}
	
	public static Object nvl(Object source, Object defaultValue) {
		return Data.nvl(source, defaultValue);
	}
	
	public static Object nnvl(Object source, Object defaultValue) {
		return source != null ? source : defaultValue;
	}
	
	public static Integer toInteger(String source) { return Integer.valueOf(source); }
	public static Long toLong(String source) { return Long.valueOf(source); }
	public static Float toFloat(String source) { return Float.valueOf(source); }
	public static Double toDouble(String source) { return Double.valueOf(source); }
	public static String toString(Object source) { return source == null ? "" : source.toString(); }
	
	public static String clipLength(String source, int length) {
		if (source.length() <= length)
			return source;
		return String.format("%s&mldr;", source.substring(0, length));
	}
	
	public static String prefix(String source, String prefix) { return Data.isEmpty(source) ? "" : Data.nvl(prefix) + source; }
	
	public static String formatNumber(Number source, String format) {
		return String.format(format, source);
	}
}
