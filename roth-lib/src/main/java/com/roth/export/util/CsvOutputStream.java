package com.roth.export.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public class CsvOutputStream implements Closeable {
	private char delimiter;
	private char quote;
	private OutputStream output;
	private boolean hasRecords;
	
	public CsvOutputStream(OutputStream output) {
		this.output = output;
		delimiter = ',';
		quote = '"';
		hasRecords = false;
	}
	
	/**
	 * Get the currently set delimiter.
	 * @return
	 */
	public char getDelimiter() { return delimiter; }
	/**
	 * Set the delimiter (default ',').
	 * @param delimiter
	 */
	public void setDelimiter(char delimiter) { this.delimiter = delimiter; }
	
	/**
	 * Get the currently set quotation character.
	 * @return
	 */
	public char getQuote() { return quote; }
	/**
	 * Set the quotation character (default '"').
	 * @param quote
	 */
	public void setQuote(char quote) { this.quote = quote; }
	
	/**
	 * Write a CsvRecord to the stream.
	 * @param record
	 * @throws IOException
	 */
	public void write(CsvRecord record) throws IOException {
		record.setDelimiter(delimiter);
		record.setQuote(quote);
		if (hasRecords)
			output.write("\n".getBytes());
		output.write(record.getBytes());
		hasRecords = true;
	}
	
	/**
	 * Write a CsvProvider to the stream.
	 * @param object
	 * @throws IOException
	 */
	public void write(CsvProvider object) throws IOException {
		write(object.toCsv());
	}

	/**
	 * Close the stream.
	 */
	@Override
	public void close() throws IOException {
		output.close();
		hasRecords = false;
	}
}
