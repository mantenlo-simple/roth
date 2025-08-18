package com.roth.export.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

public class CsvInputStream implements Closeable{
	private char delimiter;
	private char quote;
	private InputStream input;
	private String overflow;

	public CsvInputStream(InputStream input) {
		this.input = input;
		delimiter = ',';
		quote = '"';
		overflow = "";
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
	 * Read one row from the stream to a CsvRecord.
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public CsvRecord read() throws IOException, ParseException {
		if (overflow == null)
			return null;
		String record = "";
		boolean quoted = false;
		boolean escaped = false;
		byte[] buffer = overflow.isEmpty() ? new byte[1024] : overflow.getBytes();
		int len = overflow.isEmpty() ? input.read(buffer) : overflow.length();
		while (len > 0) {
			for (int i = 0; i < len; i++) {
				char c = (char) buffer[i];
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
				if ((c == '\n' || c == '\r') && !quoted) {
					if (record.isEmpty())
						continue;
					overflow = new String(Arrays.copyOfRange(buffer, i + 1, len));
					return newRecord(record);
				}
				else
					record += c;
			}
			buffer = new byte[1024];
			len = input.read(buffer);
		}
		overflow = null;
		if (record.isEmpty())
			throw new ParseException("Unexpected end of CSV file.", -1);
		return newRecord(record);
	}	
	
	/**
	 * Create a CsvRecord from a CSV-formatted row.
	 * @param source
	 * @return
	 */
	protected final CsvRecord newRecord(String source) {
		CsvRecord record = new CsvRecord();
		record.setDelimiter(delimiter);
		record.setQuote(quote);
		record.setBytes(source.getBytes());
		return record;
	}
	
	/**
	 * Read one row from the stream to a CsvProvider.
	 * @param <T>
	 * @param clazz
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public <T extends CsvProvider> T read(Class<T> clazz) throws IOException, ParseException {
		try {
			T result = Data.newInstance(clazz);
			result.fromCsv(read());
			return result;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			Log.logException(e, null);
		}
		return null;
	}
	
	/**
	 * Read all rows from the stream to an ArrayList of CsvProviders.
	 * @param <T>
	 * @param clazz
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public <T extends CsvProvider> List<T> readAll(Class<T> clazz) throws IOException, ParseException {
		List<T> result = new ArrayList<>();
		while (canRead())
			result.add(read(clazz));
		return result;
	}
	
	/**
	 * Check whether the stream is in a readable state.  This returns true when more records are available; false when all records have been read.
	 * @return
	 */
	public final boolean canRead() {
		return overflow != null;
	}

	/**
	 * Close the stream.
	 */
	@Override
	public void close() throws IOException {
		input.close();
		overflow = null;
	}
}
