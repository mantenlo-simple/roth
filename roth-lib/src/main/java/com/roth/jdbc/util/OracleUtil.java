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
package com.roth.jdbc.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.roth.base.log.Log;
import com.roth.jdbc.annotation.NoTest;

//import oracle.jdbc.OracleResultSet;

@NoTest
public abstract class OracleUtil extends JdbcUtil {
	private static final long serialVersionUID = 324889527815941783L;

	public OracleUtil() throws SQLException {
		super();
	}
	
	public OracleUtil(String jndiName) throws SQLException {
		super(jndiName);
	}
	
	/*
	 * Usage:
	 *  
	 *     See com.flyingj.common.db.JdbcUtil for usage notes.
	 * 
	 *     The difference lies in how the statement is created, and how the execQuery 
	 *     works (please see example for statement creation).  In this class, execQuery
	 *     takes one further step than JdbcUtil's execQuery: it expects a cursor, and
	 *     opens it to retrieve the result set.
	 *     
	 *     Please note: You cannot mix JdbcUtil-style statements and OracleUtil-style statement
	 *     in the same descendant class.
	 *     
	 * Example:
	 * 
	 * @JdbcUtil.ConnectionDataSource(jndiName = "CPBDB")
	 * public class MyDbUtil extends JdbcUtil {
	 *     private static final long serialVersionUID = 1L;
	 *     
	 *     // Example method
	 *     public LinkedHashMap<String,String> getSites(String siteCode, String siteName) throws SQLException {
	 *         String statement = createStatement("pricebk.site_tb.get_sites", siteCode, siteName);
	 *         return execQuery(statement, LinkedHashMap.class);
	 *     }
	 * }
	 * 
	 */
	
	/**
	 * Executes a SELECT SQL statement which returns an Oracle cursor.
	 * <i>Use with the</i> <code>createStatement</code> <i>function.</i>
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class with which to instantiate the return object.
	 * @return The object containing the parsed result set.
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected <T> T execQuery(String statement, Class resultClass) throws SQLException {
		return execQuery(statement, resultClass, null);
	}
	
	/**
	 * Executes a SELECT SQL statement which returns an Oracle cursor.
	 * <i>Use with the</i> <code>createStatement</code> <i>function.</i>
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class with which to instantiate the return object.
	 * @param elementClass The class with which to instantiate the return object's elements.
	 * @return The object containing the parsed result set.
	 * @throws SQLException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "resource" })
	@Override
	protected <T> T execQuery(String statement, Class resultClass, Class elementClass) throws SQLException {
		Object result = null;
		Connection conn = openConnection();
		PreparedStatement s = null;
		//OracleResultSet ors = null;
		ResultSet rs = null;
		
		try {
			s = conn.prepareStatement(statement);
			//ors = (OracleResultSet) s.executeQuery();
			rs = s.executeQuery();
			
			//if (ors.next()) {
			if (rs.next()) {
				//rs = ors.getCursor(1);
				rs = (ResultSet)rs.getObject(1);
				ResultSetMetaData m = rs.getMetaData();
				result = processResultSet(rs, m, resultClass, elementClass);
			}
		}
		catch (SQLException e) {
			Log.logException(e, "<unavailable>");
			conn.rollback();
			throw e;
		}
		catch (Exception e) { Log.logException(e, "<unavailable>"); }
		finally {
			try { if (rs != null) rs.close(); if (s != null) s.close(); }
			catch (SQLException e) { Log.logException(e, "<unavailable>"); }
			closeConnection();
		}
		
		return (T)result;
	}
	
	/**
	 * Creates a statement from a stored function name.
	 * 
	 * @param storedFunctionName The stored function name to use.
	 * @param params Parameters to be used in preparing statement.
	 * @return statement prepared for execution.
	 */
	public String createStatement(String storedFunctionName, Object... params) {
		String result = "SELECT " + storedFunctionName + '(';
		
		for (int i = 0; i < params.length; i++)
			result += ((i > 0) ? ", " : "") + evalParamObject(params[i]);
		
		result += ") FROM dual";
		
		return result;
	}
}
