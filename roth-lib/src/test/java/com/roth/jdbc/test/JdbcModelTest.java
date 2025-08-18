package com.roth.jdbc.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.roth.base.log.Log;
import com.roth.base.log.LogLevel;
import com.roth.jdbc.annotation.ConnectionParams;
import com.roth.jdbc.meta.util.MetaUtil;
import com.roth.jdbc.util.JdbcTest;

public class JdbcModelTest {
	@ConnectionParams(driver = "com.mysql.cj.jdbc.Driver", username = "roth", password = "3mGX&*21", 
			          url = "jdbc:mysql://localhost:3306/roth?verifyServerCertificate=false&useSSL=false&allowPublicKeyRetrieval=true&useLegacyDatetimeCode=false&nullDatabaseMeansCurrent=true") 
	private class TestMetaUtil extends MetaUtil {
		private static final long serialVersionUID = -8951882782886389082L;
		TestMetaUtil() throws SQLException { super(); }
	}
	
	private JdbcTest jtest;
	
	public JdbcModelTest() {
		try { 
			jtest = new JdbcTest(new TestMetaUtil(), "roth");
			jtest.setOutputLevel(LogLevel.WARNING);
		}
		catch (SQLException e) {
			fail(e.getMessage());	
		}
	}
	
	@BeforeAll
	public static void begin() {
		String logfilepath = "/development/roth-24-11-2025-08/workspace/roth-lib/reports/jdbctest.out";
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
							"logFilename": "%s"
						}
					]
				}
				""".formatted(logfilepath), null);
		//Log.setLogFilename(logfilepath);
		//Log.setLogLevel(Log.LOG_INFO);
		//Log.setLogStackTrace(true);
	}
	
	@AfterAll
	public static void end() {
		Log.setConfig("", null);
		//Log.setLogFilename(null);
		//Log.setLogLevel(Log.LOG_EXCEPTION);
		//Log.setLogStackTrace(false);
	}
	
	@Test
	public void testModels() {
		try {
			Log.logInfo("Starting JdbcTestTest.testModels...", null);
			int result = jtest.testModels("com/roth", "build/main/classes", Thread.currentThread().getContextClassLoader());
			Log.logInfo("Test Complete JdbcTestTest.testModels...", null);
			assertTrue(result > -1, "There are problems with one or more of the models.  Please refer to 'jdbctest.out' for more information.");
		} 
		catch (SQLException | IOException | SecurityException | ClassNotFoundException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	@Disabled
	public void testUtils() {
		try {
			Log.logInfo("Starting JdbcTestTest.testUtils...", null);
			int result = jtest.testUtils("com/roth", "build/main/classes", Thread.currentThread().getContextClassLoader());
			Log.logInfo("Test Complete JdbcTestTest.testUtils...", null);
			assertTrue(result > -1, "There are problems with one or more of the utils.  Please refer to 'jdbctest.out' for more information.");
		} 
		catch (SQLException | IOException | SecurityException | ClassNotFoundException e) {
			fail(e.getMessage());
		}
	}
}
