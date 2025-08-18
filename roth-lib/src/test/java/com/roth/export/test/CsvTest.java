package com.roth.export.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.roth.base.util.Data;
import com.roth.export.util.CsvInputStream;
import com.roth.export.util.CsvOutputStream;
import com.roth.export.util.CsvRecord;

public class CsvTest {
	@Test
	void testCsvInputStream() {
		String filename = "csvinputtest.csv";
		int row = 0;
		try (CsvInputStream input = new CsvInputStream(this.getClass().getResourceAsStream(filename))) {
			while (input.canRead()) {
				CsvRecord record = input.read();
				if (row == 0) {
					assertEquals(record.getString(), "Column One");
					assertEquals(record.getString(), "Column Two");
					assertEquals(record.getString(), "Column Three");
				}
				else if (row == 1) {
					assertEquals(record.getString(), "This");
					assertEquals(record.getString(), "is");
					assertEquals(record.getString(), "a test.");
				}
				else if (row == 2) {
					assertEquals(record.getString(), "This is a \"full\" string with quotes, commas, and\na carriage return.");
					assertEquals(record.getString(), "filler");
					assertEquals(record.getString(), "Filler 2");
				}
				else if (row == 3) {
					assertEquals(record.getString(), "This is another \"full\" string with\na carriage return and quotes");
					assertEquals(record.getString(), "And yet\nanother with a carriage return");
					assertEquals(record.getString(), "And a final\none with a couple\nof carriage\nreturns");
				}
				else if (row == 4) {
					assertEquals(record.getString(), "three");
					assertEquals(record.getInteger(), 2);
					assertEquals(record.getLong(), 1L);
				}
				else {
					fail("There shouldn't be more than 5 rows.");
				}
				row++;
			}
		} catch (IOException e) {
			fail("Unable to read test file.");
		} catch (ParseException e) {
			assertTrue(row > 4);
		}
	}
	
	@Test
	void testCsvOutputStream() {
		String filename = "csvoutputtest.csv";
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); CsvOutputStream output = new CsvOutputStream(bytes)) {
			CsvRecord record = new CsvRecord();
			record.putString("Column 1");
			record.putString("Column 2");
			record.putString("Column 3");
			record.putString("Column 4");
			record.putString("Column 5");
			output.write(record);
			record = new CsvRecord();
			record.putInteger(1);
			record.putDate(LocalDate.of(2019, 12, 31));
			record.putString("This is a string\nwith a couple\nof carriage returns");
			record.putDateTime(LocalDateTime.of(2019, 12, 31, 14, 31, 25));
			record.putFloat(23.4f);
			output.write(record);
			record = new CsvRecord();
			record.putInteger(2);
			record.putDate(LocalDate.of(2019, 12, 31));
			record.putString("This is a simple string");
			record.putDateTime(LocalDateTime.of(2019, 12, 31, 14, 32, 41));
			record.putFloat(0.258f);
			output.write(record);
			String result = new String(bytes.toByteArray());
			String check = Data.readTextFile(this.getClass(), filename);
			assertEquals(result, check);
		} catch (IOException e) {
			fail("Unable to read test file.");
		} 
	}
}
