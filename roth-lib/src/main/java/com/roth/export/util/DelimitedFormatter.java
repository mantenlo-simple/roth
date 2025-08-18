package com.roth.export.util;

import java.io.OutputStream;
import java.util.Collection;

public class DelimitedFormatter implements ExportFormatter {

	private String delimiter;
	private String quote;
	
	public DelimitedFormatter(String delimiter, String quote) {
		this.delimiter = delimiter;
		this.quote = quote;
	}
	
	public String getDelimiter() { return delimiter; }
	public void setDelimiter(String delimiter) { this.delimiter = delimiter; }

	public String getQuote() { return quote; }
	public void setQuote(String quote) { this.quote = quote; }

	@Override
	public void formatData(Collection<?> data, OutputStream output) {
		
	}
}
