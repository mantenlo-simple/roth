package com.roth.export.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.roth.base.log.Log;
import com.roth.export.util.JsonUtil;

import test.roth.export.TestClass;

public class JsonUtilTest {

	private String testSource = """
			[
			    {
			        "name": "Django",
			        "value": 2.342,
			        "id": 1423323,
			        "map": {
			            "1st": "first",
			            "2nd": "second"
			        },
			        "ldt": "2024-01-01T08:32:00",
			        "jud": "2023-12-31T15:16:02",
			        "ld": "2017-04-24T00:00:00",
			        "jsd": "2004-11-14T00:00:00",
			        "isIt": false,
			        "ott": "ONE",
			        "num": 3,
			        "list": [
			            1,
			            2,
			            3,
			            5,
			            8,
			            13,
			            21
			        ]
			    },
			    {
			        "name": "Torinado",
			        "value": 18.2,
			        "id": 9879516354,
			        "map": {
			            "3rd": "third",
			            "4th": "fourth"
			        },
			        "ldt": "2024-01-01T08:32:00",
			        "jud": "2023-12-31T15:16:02",
			        "jsd": "2004-11-14T00:00:00",
			        "ott": "THREE",
			        "num": 2,
			        "list": [
			            34,
			            55,
			            89
			        ]
			    }
			]
			""";
	
	@BeforeAll
	public static void begin() {
		String logfilepath = "/development/environment-2024-Jan/workspace/roth-lib/reports/jsontest.out";
		try {
			Files.deleteIfExists(Path.of(logfilepath));
		}
		catch (IOException e) {
			fail(e.getMessage());
		}
		Log.setConfig("""
				{
					"loggers": [
						{
							"logLevel": "INFO",
							"logStackTrace": true,
							"logFilename": "logfilepath"
						}
					]
				}
				""", null);
		//Log.setLogFilename(logfilepath);
		//Log.setLogLevel(Log.LOG_INFO);
		//Log.setLogStackTrace(true);
	}
	
	@AfterAll
	public static void end() {
		//Log.setLogFilename(null);
		//Log.setLogLevel(Log.LOG_EXCEPTION);
		//Log.setLogStackTrace(false);
		Log.setConfig("", null);
	}
	
	@Test
	void testJson() {
		String result = "Didn't get set.";
		try {
			@SuppressWarnings("unchecked")
			List<TestClass> obj = JsonUtil.jsonToObj(testSource, ArrayList.class, TestClass.class);
			result = JsonUtil.objToJson(obj);
			
			String strippedSource = stripWhitespace(testSource);
			String strippedResult = stripWhitespace(result);
			Log.println(String.format("%d %s", hashSum(strippedSource), strippedSource));
			Log.println(String.format("%d %s", hashSum(strippedResult), strippedResult));
			
			assertEquals(hashSum(strippedSource), hashSum(strippedResult));
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | DateTimeParseException e) {
			fail(e.getMessage());
		}
	}

	private long hashSum(String source) {
		long result = source.length() * 1000;
		for (int i = 0; i < source.length(); i++)
			result += source.charAt(i);
		return result;
	}
	
	private String stripWhitespace(String source) {
		return source.replace(" ", "").replace("\t", "").replace("\n", "").replace("\r", "");
		/*
		StringBuilder result = new StringBuilder(source.length());
	    boolean inQuotes = false;
	    boolean escapeMode = false;
	    for (char character : source.toCharArray()) {
	        if (escapeMode) {
	            result.append(character);
	            escapeMode = false;
	        } else if (character == '"') {
	            inQuotes = !inQuotes;
	            result.append(character);
	        } else if (character == '\\') {
	            escapeMode = true;
	            result.append(character);
	        } else if (!inQuotes && character == ' ') {
	            continue;
	        } else {
	            result.append(character);
	        }
	    }
	    return result.toString();
	    */
	}
}
