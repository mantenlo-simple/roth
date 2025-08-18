package com.roth.servlet.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

import jakarta.servlet.http.HttpServletRequest;

/**
 * A parameter parser for a String[] (use Integer as T) or an HttpServletRequest (use String as T).
 * Other types are currently not supported. Unsupported types or combinations will result in null results.
 * @param <T>
 */
public abstract class ParameterParser<T> {
	private String[] array;
	private HttpServletRequest request;
	private HashMap<String,String> formats;
	private String username;
	
	/**
	 * Initialize an array-based parser.
	 * @param source
	 */
	protected ParameterParser(String[] source, HashMap<String,String> formats, String username) {
		array = source;
		this.formats = formats;
		this.username = username;
	}
	
	/**
	 * Initialized a request parameter-based parser.
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	protected ParameterParser(HttpServletRequest request) {
		this.request = request;
		formats = (HashMap<String,String>)request.getSession().getAttribute("formats");
		username = request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();
	}
	
	public String getParameter(String name) {
		return request.getParameter(name);
	}
	
	/**
	 * Return the string for the given reference.
	 * @param reference
	 * @return
	 */
	public String getString(T reference) {
		String result = null;
		if (reference instanceof String name && request != null)
			result = getParameter(name);
		else if (reference instanceof Integer index && array != null)
			result = index >= 0 && index < array.length ? array[index] : null;
		return result;
	}
	public String getString(T reference, String defaultValue) { return Data.nvl(getString(reference), defaultValue); }
	
	public Integer getInteger(T reference) { return Data.strToInteger(getString(reference)); }
	public Integer getInteger(T reference, Integer defaultValue) { return Data.nvl(getInteger(reference), defaultValue); }
	
	public Long getLong(T reference) { return Data.strToLong(getString(reference)); }
	public Long getLong(T reference, Long defaultValue) { return Data.nvl(getLong(reference), defaultValue); }
	
	public Float getFloat(T reference) { return Data.strToFloat(getString(reference)); }
	public Float getFloat(T reference, Float defaultValue) { return Data.nvl(getFloat(reference), defaultValue); }

	public Double getDouble(T reference) { return Data.strToDouble(getString(reference)); }
	public Double getDouble(T reference, Double defaultValue) { return Data.nvl(getDouble(reference), defaultValue); }

	public BigDecimal getBigDecimal(T reference) { return Data.strToBigDecimal(getString(reference)); }
	public BigDecimal getBigDecimal(T reference, BigDecimal defaultValue) { return Data.nvl(getBigDecimal(reference), defaultValue); }
	
	/**
	 * Returns the indexed sub-path segment as a Boolean.
	 * 
	 * @param index
	 * @return
	 */
	public Boolean getBoolean(T reference) {
		String p = getString(reference);
		return p == null ? null : Boolean.valueOf(p.toLowerCase());
	}
	/**
	 * Returns the indexed sub-path segment as a Boolean.  If the parameter
	 * is null, then the defaultValue is returned.
	 * 
	 * @param index
	 * @param defaultValue
	 * @return
	 */
	public Boolean getBoolean(T reference, Boolean defaultValue) {
		String p = getString(reference);
		return p == null ? defaultValue : Boolean.valueOf(p.toLowerCase());
	}
	/**
	 * Returns the indexed sub-path segment as a Boolean.  The boolValues argument defines
	 * how to interpret the parameter.  Format for boolValues follows this pattern: "false|true",
	 * so if you are using 'N' and 'Y' to represent false and true, use pattern: "N|Y".
	 * 
	 * @param index
	 * @param boolValues
	 * @return
	 */
	public Boolean getBoolean(T reference, String boolValues) {
		return getBoolean(reference, boolValues, null);
	}
	/**
	 * Returns the indexed sub-path segment as a Boolean.  The boolValues argument defines
	 * how to interpret the parameter.  Format for boolValues follows this pattern: 
	 * "false|true", so if you are using 'N' and 'Y' to represent false and true, 
	 * use pattern: "N|Y".  If the parameter is null, then the defaultValue is returned.
	 * 
	 * @param index
	 * @param boolValues
	 * @param defaultValue
	 * @return
	 */
	public Boolean getBoolean(T reference, String boolValues, Boolean defaultValue) {
		if (boolValues == null || !boolValues.contains("|"))
			throw new IllegalArgumentException("The boolValues argument is required.  Examples: \"false|true\" or \"N|Y\".");
		String p = getString(reference);
		if (p == null)
			return defaultValue;
		String[] bools = boolValues.split("\\|");
		return p.equals(bools[0]) ? false : p.equals(bools[1]) ? true : defaultValue;
	}
	/**
	 * Returns the indexed sub-path segment as a Date
	 * @param name
	 * @return
	 */
	public Date getDate(T reference) {
		LocalDateTime result = getLocalDateTime(reference);
		return result == null ? null : Date.from(result.atZone(ZoneId.systemDefault()).toInstant());
	}
	/**
	 * Returns the indexed sub-path segment as a Date.  If the parameteter
	 * is null, then defaultValue is returned.
	 * @param name
	 * @return
	 */
	public Date getDate(T reference, Date defaultValue) {
		return Data.nvl(getDate(reference), defaultValue);
	}
	/**
	 * Returns the indexed sub-path segment as a LocalDate.
	 * @param index
	 * @return
	 */
	public LocalDate getLocalDate(T reference) {
		LocalDateTime result = getLocalDateTime(reference);
		return result== null ? null : result.toLocalDate();
	}
	/**
	 * Returns the indexed sub-path segment as a LocalDate.  If the parameter
	 * is null, then the defaultValue is returned.
	 * @param index
	 * @param defaultValue
	 * @return
	 */
	public LocalDate getLocalDate(T reference, LocalDate defaultValue) {
		return Data.nvl(getLocalDate(reference), defaultValue);
	}
	/**
	 * Returns the indexed sub-path segment as a LocalDateTime.
	 * @param name
	 * @return
	 */
	public LocalDateTime getLocalDateTime(T reference) {
		String segment = getString(reference);
		if (segment == null)
			return null;
		try { return Data.expToDts(segment, LocalDateTime.now(), formats); }
		catch (ParseException e) { throw new IllegalArgumentException(e.getMessage()); }
	}
	/**
	 * Returns the indexed sub-path segment as a LocalDateTime.  If the parameter
	 * is null, then the defaultValue is returned.
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public LocalDateTime getLocalDateTime(T reference, LocalDateTime defaultValue) {
		return Data.nvl(getLocalDateTime(reference), defaultValue);
	}
	/**
	 * Returns the indexed sub-path segment as a LocalTime.
	 * @param index
	 * @return
	 */
	public LocalTime getLocalTime(T reference) {
		LocalDateTime result = getLocalDateTime(reference);
		return result== null ? null : result.toLocalTime();
	}
	/**
	 * Returns the indexed sub-path segment as a LocalTime.  If the parameter
	 * is null, then the defaultValue is returned.
	 * @param index
	 * @param defaultValue
	 * @return
	 */
	public LocalTime getLocalTime(T reference, LocalTime defaultValue) {
		return Data.nvl(getLocalTime(reference), defaultValue);
	}
	/**
	 * Returns the named parameter as an Enum of the specified class.
	 * @param <T>
	 * @param name
	 * @param enumClass
	 * @return
	 */
	public <P extends Enum<P>> P getEnum(T reference, Class<P> enumClass) {
		String p = getString(reference);
		try {
			return p == null ? null : (P)Enum.valueOf(enumClass, p);
		} catch (IllegalArgumentException e) {
			Log.logException(e, username);
			return null;
		}
		
	}
	/**
	 * Returns the named parameter as an Enum of the specified class.  If the parameter
	 * is null, then the defaultValue is returned.
	 * @param <T>
	 * @param name
	 * @param enumClass
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <P extends Enum<P>> P getEnum(T reference, Class<P> enumClass, Enum<P> defaultValue) {
		return (P)Data.nvl(getEnum(reference, enumClass), defaultValue);
	}
}
