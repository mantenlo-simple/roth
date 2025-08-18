package com.roth.export.util;

public interface CsvProvider {
	public void fromCsv(CsvRecord source);
	public CsvRecord toCsv();
}
