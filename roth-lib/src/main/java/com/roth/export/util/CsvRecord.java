package com.roth.export.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.roth.base.util.Data;

public class CsvRecord {
	private char delimiter;
	private char quote;
	private List<String> values;
	private int column;
	
	public CsvRecord() {
		delimiter = ',';
		quote = '"';
		values = new ArrayList<>();
		column = 0;
	}
	
	public CsvRecord(String source) {
		this();
		parse(source);
	}
	
	public char getDelimiter() { return delimiter; }
	public void setDelimiter(char delimiter) { this.delimiter = delimiter; }
	
	public char getQuote() { return quote; }
	public void setQuote(char quote) { this.quote = quote; }
	
	public final String quote(String source) {
		return quote + 
			   source.replace("" + quote, "" + quote + quote)
		             .replace("\r\n", "\n")
		             .replace("\r", "\n") +
		       quote;
	}
	
	public final String unquote(String source) {
		String result = source.replace("" + quote + quote, "" + quote).replace("\r", "\n").replace("\n\n", "\n").trim();
		return result.substring(1, result.length() - 1);
	}
	
	public final String unquote(StringBuilder source) {
		return unquote(source.toString());
	}
	
	public final String escape(String source) {
		if (source == null)
			return "";
		boolean hasQuote = source.indexOf(quote) > -1;
		boolean hasComma = source.indexOf(delimiter) > -1;
		boolean hasBreak = source.contains("\r") || source.contains("\n");
		return hasQuote || hasComma || hasBreak ? quote(source) : source;
	}
	
	public String getString() { return values.get(column++); }
	public String getString(int index) { return index < values.size() ? values.get(index) : null; }
	public void putString(String source) { values.add(source); } 
	
	public Integer getInteger() { return Data.strToInteger(getString()); }
	public Integer getInteger(int index) { return Data.strToInteger(getString(index)); }
	public void putInteger(Integer source) { putString(Data.integerToStr(source)); }
	
	public Long getLong() { return Data.strToLong(getString()); }
	public Long getLong(int index) { return Data.strToLong(getString(index)); }
	public void putLong(Long source) { putString(Data.longToStr(source)); }
	
	public Float getFloat() { return Data.strToFloat(getString()); }
	public Float getFloat(int index) { return Data.strToFloat(getString(index)); }
	public void putFloat(Float source) { putString(Data.floatToStr(source)); }
	
	public Double getDouble() { return Data.strToDouble(getString()); }
	public Double getDouble(int index) { return Data.strToDouble(getString(index)); }
	public void putDouble(Double source) { putString(Data.doubleToStr(source)); }
	
	public BigDecimal getBigDecimal() { return Data.strToBigDecimal(getString()); }
	public BigDecimal getBigDecimal(int index) { return Data.strToBigDecimal(getString(index)); }
	public void putBigDecimal(BigDecimal source) { putString(Data.bigDecimalToStr(source)); }
	
	public Boolean getBoolean() { return Boolean.parseBoolean(getString()); }
	public Boolean getBoolean(int index) { String value = getString(index); return value == null ? null : Boolean.parseBoolean(value); }
	public void putBoolean(Boolean source) { putString(source == null ? "" : source.toString()); }
	
	public LocalDate getLocalDate() { return Data.strToLocalDate(getString(), Data.ISO_DATE); }
	public LocalDate getLocalDate(int index) { return Data.strToLocalDate(getString(index), Data.ISO_DATE); }
	public void putDate(LocalDate source) { putString(Data.dateToStr(source, Data.ISO_DATE)); }
	public void putDate(java.util.Date source) { putString(Data.dateToStr(source, Data.ISO_DATE)); }
	public void putDate(java.sql.Date source) { putString(Data.dateToStr(source, Data.ISO_DATE)); }
	
	public LocalDateTime getLocalDateTime() { return Data.strToLocalDateTime(getString(),Data.ISO_DATETIME); }
	public LocalDateTime getLocalDateTime(int index) { return Data.strToLocalDateTime(getString(index), Data.ISO_DATETIME); }
	public void putDateTime(LocalDateTime source) { putString(Data.dateToStr(source, Data.ISO_DATETIME)); }
	public void putDateTime(java.util.Date source) { putString(Data.dateToStr(source, Data.ISO_DATETIME)); }
	public void putDateTime(java.sql.Timestamp source) { putString(Data.dateToStr(source, Data.ISO_DATETIME)); }
	
	public LocalTime getLocalTime() { return Data.strToLocalTime(getString(), Data.ISO_TIME); }
	public LocalTime getLocalTime(int index) { return Data.strToLocalTime(getString(index), Data.ISO_TIME); }
	public void putTime(LocalTime source) { putString(Data.dateToStr(source, Data.ISO_TIME)); }
	public void putTime(java.util.Date source) { putString(Data.dateToStr(source, Data.ISO_TIME)); }
	public void putTime(java.sql.Time source) { putString(Data.dateToStr(source, Data.ISO_TIME)); }
	
	public int size() {
		return values == null ? 0 : values.size();
	}
	
	public byte[] getBytes() {
		return toString().getBytes();
	}
	
	public void setBytes(byte[] source) {
		parse(new String(source));
	}
	
	protected final void parse(String csvRecord) {
		StringBuilder value = newValue("");
		boolean quoted = false;
		boolean escaped = false;
		for (int i = 0; i < csvRecord.length(); i++) {
			char c = csvRecord.charAt(i);
			if (c == quote && !quoted)
				quoted = true;
			else if (c == quote && !escaped)
				escaped = true;
			else if (c == quote)
				escaped = false;
			else if (quoted && escaped) {
				quoted = false;
				escaped = false;
			}
			if (c == delimiter && !quoted)
				value = addValue(value);
			else
				value.append(c);
		}
		if (value.toString().startsWith("" + quote))
			value = new StringBuilder(unquote(value));
		values.add(value.toString());
		column = 0;
	}
	
	protected StringBuilder newValue(String start) {
		return new StringBuilder(start);
	}
	
	protected StringBuilder addValue(StringBuilder value) {
		values.add(value.toString().startsWith("" + quote) ? unquote(value) : value.toString());
		return newValue("");
	}
	
	public String toString() {
		StringBuilder csvRecord = new StringBuilder("");
		for (String value : values)
			csvRecord.append((csvRecord.isEmpty() ? "" : delimiter) + escape(value));
		return csvRecord.toString();
	}
}
