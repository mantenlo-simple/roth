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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.ConnectionDataSource;
import com.roth.jdbc.annotation.ConnectionParams;
import com.roth.jdbc.annotation.Limit;
import com.roth.jdbc.annotation.NoTest;
import com.roth.jdbc.annotation.SQLFileContext;
import com.roth.jdbc.setting.model.JdbcSettings;

@NoTest
public abstract class JdbcUtil implements Serializable {
	private static final long serialVersionUID = -1682635349806695047L;

	/*
	public static final String INFORMIX = "Informix"; //"Informix Dynamic Server"; or "IDS/UNIX32" if using universal driver.
	public static final String MYSQL = "MySQL";
	public static final String ORACLE = "Oracle";
	public static final String POSTGRESQL = "PostgreSQL";
	public static final String SQL_SERVER = "Microsoft SQL Server";
	public static final String DB2 = "DB2"; //"DB2/AIX64" if using universal driver.
	*/
	
	private static JdbcSettings settings;
	protected static boolean suppressSettings;
	public static JdbcSettings getSettings() {
		if (!suppressSettings && settings == null)
			settings = new JdbcSettings();
		return settings;
	}
	
	private static boolean testMode = false;
	private static ConnectionParams testParams = null;
	/**
	 * This initializes test mode.  This effects all descendants of JdbcUtil until
	 * the JVM is stopped (i.e., you can't turn it off once initialized).  Once 
	 * initialized all calls will be automatically rolled back (i.e., no commits).
	 * This is ONLY for use with unit testing, so if you use it wrong, don't come 
	 * complaining to me.
	 * @param connParams
	 */
	public static void initTestMode(ConnectionParams connectionParams) { 
		testMode = true;
		testParams = connectionParams;
	}
	
	protected class MethodCache implements Serializable {
		private static final long serialVersionUID = 4994552497590892403L;
		
		private Method _method;
		private Class<?> _clazz;
		
		public Method getMethod() { return _method; }
		public void setMethod(Method method) { _method = method; }
		
		public Class<?> getClazz() { return _clazz; }
		public void setClazz(Class<?> clazz) { _clazz = clazz; }
	}
	
	/*
	 * Usage:
	 * 
	 *     public Object execQuery(String statement, Class returnClass);
	 *     public Object execQuery(String statement, Class returnClass, Class elementClass);
	 *     
	 *     returnClass may be: 
	 *     1) a primitive (boolean, byte, double, float, int, long, or short),
	 *     2) a simple class (java.lang.Boolean, java.lang.Byte, java.lang.Double, java.lang.Float, 
	 *                        java.lang.Integer, java.lang.Long, java.lang.Short, java.lang.String, 
	 *                        java.math.BigDecimal, java.sql.Date, java.sql.Time, java.sql.Timestamp, 
	 *                        or java.util.Date),
	 *     3) a serializable POJO, 
	 *     4) an array of any of 1, 2 or 3, 
	 *     5) a collection of any of 1, 2, or 3 (elementClass must be specified),
	 *     6) or a map (Note: a map is assumed to be <String,String>).  
	 *            
	 *     If 1 or 2 is used (or an array or collection of 1 or 2), only one column may be selected by statement.
	 *     If 6 is used, only two columns may be selected by statement.
	 *     
	 * Example:
	 * 
	 * @JdbcUtil.ConnectionDataSource(jndiName = "CPBDB")
	 * public class MyDbUtil extends JdbcUtil {
	 *     private static final long serialVersionUID = 1L;
	 *     
	 *     // Example method
	 *     public Vector<Site> getSites(String where) throws SQLException {
	 *         String statement = "...";
	 *         statement = applyParameters(statement, where);
	 *         return execQuery(statement, Vector.class, Site.class);
	 *     }
	 *     
	 *     public Site[] getSites(String where) throws SQLException {
	 *         String statement = "...";
	 *         statement = applyParameters(statement, where);
	 *         return execQuery(statement, Site[].class);
	 *     }
	 *     
	 *     public Site getSite(String siteCode) throws SQLException {
	 *         String statement = "...";
	 *         statement = applyParameters(statement, siteCode);
	 *         return execQuery(statement, Site.class);
	 *     }
	 *     
	 *     public LinkedHashMap<String,String> getSites(String siteCode, String siteName) throws SQLException {
	 *         String statement = "...";
	 *         statement = applyParameters(statement, siteCode, siteName);
	 *         return execQuery(statement, LinkedHashMap.class);
	 *     }
	 * }
	 * 
	 */
	
	//private String dbName = null;
	//protected String getDbName() { return dbName; }
	private DbmsType dbName = null;
	protected DbmsType getDbName() { return dbName; }
	
	private String logCode = null;
	public String getLogCode() { return Data.nvl(logCode, "jdbcutil"); }
	public void setLogCode(String logCode) { this.logCode = logCode; }
	private static final String LOG_NL = "\n     ";
	
	public JdbcUtil() throws SQLException {
		init(null);
	}
	
	public JdbcUtil(String jndiName) throws SQLException {
		init(jndiName);
	}
	
	private Boolean _managed;
	public JdbcUtil(String jndiName, boolean managed) throws SQLException {
		_managed = managed;
		init(jndiName);
	}
	
	public JdbcUtil(String username, String password) throws SQLException {
		_username = username;
		_password = password;
		init(null);
	}
	
	private boolean suppressBinary;
	public boolean getSuppressBinary() { return suppressBinary; }
	public void setSuppressBinary(boolean suppressBinary) { this.suppressBinary = suppressBinary; }
	
	private String dts() { return LOG_NL + Data.dtsToStr(LocalDateTime.now()) + " - "; };
	
	protected void init(String jndiName) throws SQLException {
		if (jndiName != null) setJndiName(jndiName);
		_conn = null;
		String debug = toString() + "-init" + dts() + "Opening connection to " + getJndiName();
		suppressOpenCallback = true;
		openConnection();
		suppressOpenCallback = false;
		debug += dts() + "Connection opened; Receiving meta data";
		try { 
			dbName = DbmsType.valueOfDbName(_conn.getMetaData().getDatabaseProductName()); 
			debug += dts() + "Meta data received";
		}
		finally {
			debug += dts() + "Finally: Closing connection";
			closeConnection(); 
			debug += dts() + "Finally: Connection closed";
			Log.logDebug(debug, null, getLogCode());
		}
	}
	
	/**
	 * Executes a SELECT SQL statement.
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class with which to instantiate the return object.
	 * @return The object containing the parsed result set.
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	protected <T> T execQuery(String statement, Class resultClass) throws SQLException {
		return _execQuery(statement, resultClass, null);
	}
	
	/**
	 * Executes a SELECT SQL statement.
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class with which to instantiate the return object.
	 * @param elementClass The class with which to instantiate the return object's elements.
	 * @return The object containing the parsed result set.
	 * @throws SQLException
	 */
	@SuppressWarnings({ "rawtypes" })
	protected <T> T execQuery(String statement, Class resultClass, Class elementClass) throws SQLException {
		return _execQuery(statement, resultClass, elementClass);
	}
	
	/**
	 * Executes a SELECT SQL statement.
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class with which to instantiate the return object.
	 * @return The object containing the parsed result set.
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	protected <T> T execQuery(JdbcStatement statement, Class resultClass) throws SQLException {
		return _execQuery(statement, resultClass, null);
	}
	
	/**
	 * Executes a SELECT SQL statement.
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class with which to instantiate the return object.
	 * @param elementClass The class with which to instantiate the return object's elements.
	 * @return The object containing the parsed result set.
	 * @throws SQLException
	 */
	@SuppressWarnings({ "rawtypes" })
	protected <T> T execQuery(JdbcStatement statement, Class resultClass, Class elementClass) throws SQLException {
		return _execQuery(statement, resultClass, elementClass);
	}
	
	/**
	 * Executes a SELECT SQL statement.
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class with which to instantiate the return object.
	 * @param elementClass The class with which to instantiate the return object's elements.
	 * @return The object containing the parsed result set.
	 * @throws SQLException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> T _execQuery(Object statement, Class resultClass, Class elementClass) throws SQLException {
		Object result = null;
		Connection conn = openConnection();
		PreparedStatement s = null;
		ResultSet rs = null;
		
		try {
			useSchema(conn);
			
			// TODO: When testMode == true, the statement should be wrapped: "SELECT * FROM (%s)"			
			
			if (statement instanceof String strStatement)
				s = conn.prepareStatement(strStatement);
			else if (statement instanceof JdbcStatement jstStatement)
				s = (jstStatement).prepare(conn);
			else
				throw new SQLException("Invalid statement type.");
			
			if (getSettings() != null && getSettings().getQueryTimeout() != null)
				s.setQueryTimeout(settings.getQueryTimeout());
			
			rs = s.executeQuery();
			ResultSetMetaData m = s.getMetaData();
			if (m == null)
				m = rs.getMetaData();
			result = processResultSet(rs, m, resultClass, elementClass);
			rs.close(); rs = null;
			s.close(); s = null;
			closeConnection();
		}
		catch (SQLException e) {
			Log.logError("Error during statement preparation or execution.", null, e);
			if (!isManaged()) conn.rollback();
			throw e;
		}
		catch (Exception e) { Log.logError("Unexpected Error.", null, e); }
		finally {
			restoreSchema(conn);
			try { if (rs != null) rs.close(); }
			catch (SQLException e) { Log.logError("Unable to close resultset.", null, e); }
			try { if (s != null) s.close(); }
			catch (SQLException e) { Log.logError("Unable to close statement.", null, e); }
			closeConnection();
		}
		
		return (T)result;
	}
	
	/*
	private static HashMap<String, ArrayList<MethodCache>> classCacheMap;
	private static ArrayList<MethodCache> getClassCache(String className) {
		if (classCacheMap == null)
			return null;
		return classCacheMap.get(className);
	}
	private static void putClassCache(String className, ArrayList<MethodCache> classCache) {
		if (classCacheMap == null)
			classCacheMap = new HashMap<>();
		classCacheMap.put(className, classCache);
	}
	*/
	
	protected static HashMap<String, Class<? extends Binder>> binderCache;
	
	protected static Class<? extends Binder> getBinder(String className, ClassLoader classLoader) {
		// First check binderCache
		String classLoaderHash = Data.toHexString(classLoader.hashCode(), 8);
		Class<? extends Binder> result = null;
		if (binderCache != null)
			 result = binderCache.get(classLoaderHash + "." + className);
		// Second check the classLoader to see if it's there
		if (result == null)
			try { result = Class.forName(className, false, classLoader).asSubclass(Binder.class); } 
			catch (ClassNotFoundException e) { }
		return result;
	}
	
	protected static void putBinder(Class<? extends Binder> binderClass, String className, ClassLoader classLoader) {
		String classLoaderHash = Data.toHexString(classLoader.hashCode(), 8);
		if (binderCache == null)
			binderCache = new HashMap<>();
		binderCache.put(classLoaderHash + "." + className, binderClass);
	}
	
	static {
		putBinder(MapBinder.class, Map.class.getCanonicalName(), MapBinder.class.getClassLoader());
	}
	
	/**
	 * Processes a result set.
	 * @param <T>
	 * @param resultSet The result set to process.
	 * @param metaData The meta data for the result set.
	 * @param resultClass The class with which to instantiate the return object.
	 * @param elementClass The class with which to instantiate the return object's elements.
	 * @return The object containing the parsed result set.
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> T processResultSet(ResultSet resultSet, ResultSetMetaData metaData, Class resultClass, Class elementClass) throws Exception {
		Object result = null;
		Limit limit = this.getClass().getAnnotation(Limit.class);
		
		// Set reference variables that help guide the creation of result object(s).
		boolean isArray = resultClass.isArray() && resultClass != byte[].class;
		boolean isCollection = elementClass != null;
		boolean isMap = Map.class.isAssignableFrom(resultClass);
		boolean isSingular = !isArray && !isCollection && !isMap;
		Class<?> objClass = (elementClass != null) ? elementClass : (isArray) ? resultClass.getComponentType() : resultClass;
		boolean isAtomic = !isMap && isAtomic(objClass);
		
		// Initialized the result object.
		result = (isArray) ? new ArrayList() 
		       : (isAtomic && !isCollection) ? null 
		       : Data.newInstance(resultClass);
		
		// Initialize the method cache.
		/*
		ArrayList<MethodCache> mc = getClassCache(resultClass.getCanonicalName());
		boolean classCacheFound = mc != null;
		if (!classCacheFound)
			mc = new ArrayList<MethodCache>();
		*/
		
		// recordCount is used to keep track of the row number.  Yes, the ResultSet
		// class has a row() function, but it is optional, and can't be counted on.
		int recordCount = 0;
		
		// These are only used in the event that the return type is a Map.
		String mapKey = null;
		String mapValue = null;
		Binder binder = null;
		
		if (!isMap && !isAtomic) {
			String binderClassName = null;
			Class<? extends Binder> binderClass = null;
			if (Map.class.isAssignableFrom(objClass)) {
				binderClassName = MapBinder.class.getCanonicalName();
				binderClass = MapBinder.class;
			}
			else {
				binderClassName = BinderGenerator.getBinderClassName(objClass, metaData);
				binderClass = getBinder(binderClassName, objClass.getClassLoader());
			}
			// Search for an existing binder class that fits the object, class loader, and metadata signature.
			if (binderClass != null) {
				binder = Data.newInstance(binderClass);
				Log.logInfo("Binder class found: " + binderClassName, null, "binder");
			}
			// If not found, then generate, compile, and cache a binder class for the object, class loader, and metadata signature.
			String binderCode = null;
			if (binderClass == null) {
				try {
					binderCode = BinderGenerator.generateCode(objClass, metaData, suppressBinary);
					binderClass = BinderGenerator.getCompiledBinderClass(binderClassName, binderCode, objClass);
					putBinder(binderClass, binderClassName, objClass.getClassLoader());
					binder = Data.newInstance(binderClass);
				}
				catch (Exception e) {
					Log.logException(e, null);
				}
				Log.logInfo("Binder class generated and compiled: " + binderClassName, null, "binder");
			}
			// If the compile fails, then write the binder class source code to a file in the rothTemp folder for inclusion manually.
			// This failure is probably due to an unknown element in the class path.
			if (binderClass == null) {
				String rothTemp = (String)Data.getWebEnv("rothTemp");
				String message = """
						Creating a container environment variable with the name 'rothTemp' and a valid path will allow this code to generate the binder class for you.
						Example (in web.xml):
						    <env-entry>
						        <env-entry-name>rothTemp</env-entry-name> 
						        <env-entry-type>java.lang.String</env-entry-type> 
						        <env-entry-value>${catalina.base}/tmp/</env-entry-value> 
						    </env-entry>
						or (in context.xml):
						    <Environment name="rothTemp" type="java.lang.String" value="${catalina.base}/tmp/"/>
						""";
				if (rothTemp != null) {
					String binderPath = rothTemp + BinderGenerator.getBinderFilePath(objClass, metaData);;
					
					File directory = new File(binderPath.substring(0, binderPath.lastIndexOf("/")));
					if (!directory.exists())
						directory.mkdirs();
					
					if (!new File(binderPath).exists())
						try (BufferedWriter writer = new BufferedWriter(new FileWriter(binderPath))) {
							//writer.write(BinderGenerator.generateCode(objClass, metaData, suppressBinary));
							writer.write(binderCode);
						}
					message = "A generated binder class has been created at the following path: \n    " + binderPath + "\n"
							+ "More than one binder class may apply to a bean class, depending on the metadata involved, therefore "
							+ "the generated binder class name includes a hash representing the metadata involved.  The generated "
							+ "binder class's package name is derived from the bean class's package name, replacing the word 'model' "
							+ "with the word 'binder'.  The presence of the binder class in the same class loader is all that is "
							+ "necessary for the binder class to be accessible.";
				}
				 
				Log.logWarning("A binder class has not been specified for class " + objClass.getCanonicalName() + ".\n"
						+ "Implementing the a binder class will dramatically improve performance.\n"
						+ message, null);

				// Revert to a reflection binder class other binders aren't available.
				binder = new ReflectionBinder(metaData);
				((ReflectionBinder) binder).setSuppressBinary(suppressBinary);
				Log.logInfo("Binder class reverted to reflection binder for: " + objClass.getCanonicalName(), null, "binder");
			}
		}
		
		while (resultSet.next()) {
			recordCount ++;
			
			if ((limit != null) && (limit.rows() > 0) && (recordCount > limit.rows())) 
				break;
			
			if (isSingular && (recordCount > 1))
				throw new SQLException("Too many rows returned by result set.");
			
			if (isMap && metaData.getColumnCount() < 2)
				throw new SQLException("Too few fields returned by result set.");
			
			if ((isAtomic && metaData.getColumnCount() > 1) || (isMap && metaData.getColumnCount() > 2))
				throw new SQLException("Too many fields returned by result set.");

			try {
				// Initialize the row object.
				Object obj = isSingular ? result
					       : isAtomic ? null
					       : Data.newInstance(objClass); 
						
				if (isAtomic)
					obj = getDataSourceColumn(objClass, resultSet, 1, suppressBinary);
				else if (isMap) {
					mapKey = resultSet.getString(1);
					mapValue = resultSet.getString(2);
				}
				else {
					if (binder instanceof ReflectionBinder)
						((ReflectionBinder) binder).setRecordCount(recordCount);
					binder.bindObject(obj, resultSet);
				}
				
				// Process the row object for the result.
				if (isArray || isCollection)
					((Collection)result).add(obj);
				else if (isMap)
					((Map)result).put(mapKey, mapValue);
				else if (isAtomic)
					result = obj;
			}
			catch (Exception e) {
				Log.logError("Unexpected error during resultset processing.", null, e);
				throw e;
			}
			catch (Throwable t) {
				throw new SQLException("Unexpected error during resultset processing.  " + t.getCause().getMessage());
			}
		}
		
		// If the result type is an array, then the contents of the vector need to be 
		// moved to an array.
		if (isArray) {
			// For some reason, the Vector doesn't want to dump the array using
			// the .toArray() method, so we have to do this the old-fashioned way.
			// Perhaps I will revisit this later...
			Object temp = Array.newInstance(resultClass.getComponentType(), ((ArrayList)result).size());
			
			for (int i = 0; i < ((ArrayList)result).size(); i++)
				Array.set(temp, i, ((ArrayList)result).get(i));
			
			result = temp;
		}
		
		// Type-cast the result so that no warnings appear in descendant classes.
		return (recordCount < 1) ? null : (T)result;
	}
	
	/**
	 * Executes a DML (INSERT, UPDATE, or DELETE) SQL statement.
	 * 
	 * @param statement The statement to execute.
	 * @return The number of rows effected, if applicable.
	 * @throws SQLException
	 */
	protected int execUpdate(String statement) throws SQLException {
		openConnection();
		int result = -1;
		try { 
			result = _execUpdate(statement, true);
			closeConnection();
		}
		finally { 
			closeConnection();
		}
		return result;
	}
	
	/**
	 * Executes a DML (INSERT, UPDATE, or DELETE) SQL statement.
	 * 
	 * @param statement The statement to execute.
	 * @return The number of rows effected, if applicable.
	 * @throws SQLException
	 */
	protected int execUpdate(String statement, boolean autoCommit) throws SQLException {
		return _execUpdate(statement, autoCommit);
	}
	
	/**
	 * Executes a DML (INSERT, UPDATE, or DELETE) SQL statement.
	 * 
	 * @param statement The statement to execute.
	 * @return The number of rows effected, if applicable.
	 * @throws SQLException
	 */
	protected int execUpdate(JdbcStatement statement) throws SQLException {
		openConnection();
		int result = -1;
		try { 
			result = _execUpdate(statement, true);
			closeConnection();
		}
		finally { 
			closeConnection();
		}
		return result;
	}
	
	/**
	 * Executes a DML (INSERT, UPDATE, or DELETE) SQL statement.
	 * 
	 * @param statement The statement to execute.
	 * @return The number of rows effected, if applicable.
	 * @throws SQLException
	 */
	protected int execUpdate(JdbcStatement statement, boolean autoCommit) throws SQLException {
		return _execUpdate(statement, autoCommit);
	}
	
	/**
	 * Executes a DML (INSERT, UPDATE, or DELETE) SQL statement.
	 * 
	 * @param statement The statement to execute.
	 * @return The number of rows effected, if applicable.
	 * @throws SQLException
	 */
	protected int _execUpdate(Object statement, boolean autoCommit) throws SQLException {
		if (_conn == null) throw new SQLException("No connection exists.");
		PreparedStatement s = null;
		//Statement s = null;
		int result = -1;
		
		try {
			//s = _conn.prepareStatement(statement);
			//result = s.executeUpdate();
			
			//s = _conn.createStatement();
			useSchema(_conn);
			if (statement instanceof String)
				s = _conn.prepareStatement((String)statement);
			else if (statement instanceof JdbcStatement)
				s = ((JdbcStatement)statement).prepare(_conn);
			else
				throw new SQLException("Invalid statement type.");
			
			if (getSettings() != null && getSettings().getQueryTimeout() != null)
				s.setQueryTimeout(settings.getQueryTimeout());
			
			//result = s.executeUpdate(statement);
			result = s.executeUpdate();
			if (testMode) 
				_conn.rollback();
			else if (autoCommit && !isManaged()) 
				_conn.commit();
			s.close(); s = null;
		}
		catch (SQLException e) {
			if (autoCommit && !isManaged()) _conn.rollback();
			throw e;
		}
		finally {
			restoreSchema(_conn);
			if ((s != null) && (!s.isClosed())) s.close();
		}
		
		return result;
	}
	
	/**
	 * Executes a batch of DML (INSERT, UPDATE, or DELETE) SQL statements.
	 * 
	 * @param statements The statements to batch execute.
	 * @return The number of rows effected by each statement, if applicable.
	 * @throws SQLException
	 */
	protected int[] execUpdate(String[] statements) throws SQLException {
		openConnection();
		int[] result = null;
		try { 
			result = _execUpdate(statements, true);
			closeConnection();
		}
		finally { 
			closeConnection(); 
		}
		return result;
	}
	
	/**
	 * Executes a batch of DML (INSERT, UPDATE, or DELETE) SQL statements.
	 * 
	 * @param statements The statements to batch execute.
	 * @return The number of rows effected by each statement, if applicable.
	 * @throws SQLException
	 */
	protected int[] execUpdate(JdbcStatement[] statements, boolean autoCommit) throws SQLException {
		return _execUpdate(statements, autoCommit);
	}
	
	/**
	 * Executes a batch of DML (INSERT, UPDATE, or DELETE) SQL statements.
	 * 
	 * @param statements The statements to batch execute.
	 * @return The number of rows effected by each statement, if applicable.
	 * @throws SQLException
	 */
	protected int[] execUpdate(JdbcStatement[] statements) throws SQLException {
		openConnection();
		int[] result = null;
		try { 
			result = _execUpdate(statements, true);
			closeConnection();
		}
		finally { 
			closeConnection(); 
		}
		return result;
	}
	
	/**
	 * Executes a batch of DML (INSERT, UPDATE, or DELETE) SQL statements.
	 * 
	 * @param statements The statements to batch execute.
	 * @return The number of rows effected by each statement, if applicable.
	 * @throws SQLException
	 */
	protected int[] execUpdate(String[] statements, boolean autoCommit) throws SQLException {
		return _execUpdate(statements, autoCommit);
	}	
	
	/**
	 * Executes a batch of DML (INSERT, UPDATE, or DELETE) SQL statements.
	 * 
	 * @param statements The statements to batch execute.
	 * @return The number of rows effected by each statement, if applicable.
	 * @throws SQLException
	 */
	protected int[] _execUpdate(String[] statements, boolean autoCommit) throws SQLException {
		if (_conn == null) throw new SQLException("No connection exists.");
		//PreparedStatement s = null;
		Statement s = null;
		int[] result = null;
		
		try {
			useSchema(_conn);
			s = _conn.createStatement();
			
			if (getSettings() != null && getSettings().getQueryTimeout() != null)
				s.setQueryTimeout(settings.getQueryTimeout());
			
			for (int i = 0; i < statements.length; i++) s.addBatch(statements[i]);
			result = s.executeBatch();
			if (testMode)
				_conn.rollback();
			else if (autoCommit && !isManaged()) 
				_conn.commit();
			s.close(); s = null;
		}
		catch (SQLException e) {
			if (autoCommit && !isManaged()) _conn.rollback();
			throw e;
		}
		finally {
			restoreSchema(_conn);
			if ((s != null) && (!s.isClosed())) s.close();
		}
		
		return result;
	}
	
	/**
	 * Executes a batch of DML (INSERT, UPDATE, or DELETE) SQL statements.
	 * 
	 * @param statements The statements to batch execute.
	 * @return The number of rows effected by each statement, if applicable.
	 * @throws SQLException
	 */
	protected int[] _execUpdate(JdbcStatement[] statements, boolean autoCommit) throws SQLException {
		if (_conn == null) throw new SQLException("No connection exists.");
		PreparedStatement s = null;
		//Statement s = null;
		int[] result = new int[statements.length];
		
		try {
			useSchema(_conn);
			for (int i = 0; i < statements.length; i++) {
				s = statements[i].prepare(_conn);
				
				if (getSettings() != null && getSettings().getQueryTimeout() != null)
					s.setQueryTimeout(settings.getQueryTimeout());
				
				result[i] = s.executeUpdate();
				s.close(); s = null;
			}
			if (testMode)
				_conn.rollback();
			else if (autoCommit && !isManaged()) 
				_conn.commit();
		}
		catch (SQLException e) {
			if (autoCommit && !isManaged()) _conn.rollback();
			throw e;
		}
		finally {
			restoreSchema(_conn);
			if ((s != null) && (!s.isClosed())) s.close();
		}
		
		return result;
	}
	
	/**
	 * Executes a stored procedure with no return value.
	 * This function will auto-commit after execution.
	 * <i>Use with the</i> <code>createCall</code> <i>function.</i>
	 * @param statement The statement to execute.
	 * @throws SQLException
	 */
	protected void execProcedure(String statement) throws SQLException {
		execProcedure(statement, null);
	}
	
	/**
	 * Executes a stored procedure with no return value.
	 * <i>Use with the</i> <code style="color: blue;">createCall</code> <i>function.</i>
	 * @param statement The statement to execute.
	 * @param autoCommit Whether to auto-commit after execution.
	 * @throws SQLException
	 */
	protected void execProcedure(String statement, boolean autoCommit) throws SQLException {
		execProcedure(statement, null, autoCommit);
	}
	
	/**
	 * Executes a stored procedure that returns a value.
	 * This function will auto-commit after execution.
	 * <i>Use with the</i> <code>createCall</code> <i>function.</i>
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class of the expected return value.
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	protected <T> T execProcedure(String statement, Class resultClass) throws SQLException {
		openConnection();
		try { return execProcedure(statement, resultClass, true); }
		finally {
			closeConnection(); 
		}
	}
	
	/**
	 * Executes a stored procedure that returns a value.
	 * <i>Use with the</i> <code>createCall</code> <i>function.</i>
	 * @param <T>
	 * @param statement The statement to execute.
	 * @param resultClass The class of the expected return value.
	 * @param autoCommit Whether to auto-commit after execution.
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> T execProcedure(String statement, Class resultClass, boolean autoCommit) throws SQLException {
		if (_conn == null) throw new SQLException("No connection exists.");
		CallableStatement s = null;
		Object result = null;
		
		try {
			useSchema(_conn);
			s = _conn.prepareCall(statement);
			
			if (getSettings() != null && getSettings().getQueryTimeout() != null)
				s.setQueryTimeout(settings.getQueryTimeout());
			
			boolean returns = (statement.indexOf(" ? = ") > -1) ||
			                  (statement.indexOf(" ? := ") > -1); 
			if (returns) s.registerOutParameter(1, getReturnType(resultClass));  
			s.execute();
			if (returns) result = getReturnValue(resultClass, s);
			if (testMode)
				_conn.rollback();
			else if (autoCommit && !isManaged()) 
				_conn.commit();
			s.close(); s = null;
			return (T)result;
		}
		catch (SQLException e) {
			if (autoCommit && !isManaged()) _conn.rollback();
			throw e;
		}
		finally {
			restoreSchema(_conn);
			if ((s != null) && (!s.isClosed())) s.close();
		}
	}
	
	/**
	 * Executes a batch of stored procedures.
	 * This function will auto-commit after execution.
	 * <i>Use with the</i> <code>createCall</code> <i>function.</i>
	 * @param <T>
	 * @param statements The statements to execute.
	 * @return
	 * @throws SQLException
	 */
	protected void execProcedure(String[] statements) throws SQLException {
		openConnection();
		try { execProcedure(statements, true); }
		finally { 
			closeConnection(); 
		}
	}
	
	/**
	 * Executes a batch of stored procedures.
	 * <i>Use with the</i> <code>createCall</code> <i>function.</i>
	 * @param <T>
	 * @param statements The statements to execute.
	 * @param autoCommit Whether to auto-commit after execution.
	 * @return
	 * @throws SQLException
	 */
	protected void execProcedure(String[] statements, boolean autoCommit) throws SQLException {
		if (_conn == null) throw new SQLException("No connection exists.");
		CallableStatement s = null;
		
		try {
			useSchema(_conn);
			s = _conn.prepareCall(statements[0]);
			
			if (getSettings() != null && getSettings().getQueryTimeout() != null)
				s.setQueryTimeout(settings.getQueryTimeout());
			
			for (int i = 0; i < statements.length; i++) s.addBatch(statements[i]);
			s.executeBatch();			
			if (testMode)
				_conn.rollback();
			else if (autoCommit && !isManaged()) 
				_conn.commit();
			s.close(); s = null;
		}
		catch (SQLException e) {
			if (autoCommit && !isManaged()) _conn.rollback();
			throw e;
		}
		finally {
			restoreSchema(_conn);
			if ((s != null) && (!s.isClosed())) s.close();
		}
	}
	
	/**
	 * Get the value of the managed attribute of the ConnectionDataSource annotation.
	 * @return the value of the managed attribute
	 */
	public boolean isManaged() {
		ConnectionDataSource cds = getClass().getAnnotation(ConnectionDataSource.class);
		return (cds != null) ? cds.managed() : (_managed != null) ? _managed : false;
	}
	
	private Connection _conn = null;
	/**
	 * @return The connection object that is tied to the current instance of the class.
	 */
	public Connection getConnection() { return _conn; }
	
	private String _jndiName;
	/**
	 * Overrides the jndiName as set in the annotation.
	 * @param jndiName
	 */
	public void setJndiName(String jndiName) { _jndiName = jndiName; }
	/**
	 * Get the jndiName.  This will be either the overridden value (setJndiName), if set, 
	 * or that specified by the @ConnectionDataSource annotation. 
	 * @return
	 */
	public String getJndiName() {
		if (_jndiName != null)
			return _jndiName;
		ConnectionDataSource source = this.getClass().getAnnotation(ConnectionDataSource.class);
		if (source != null)
			return source.jndiName();
		ConnectionParams params = this.getClass().getAnnotation(ConnectionParams.class);
		if (params != null)
			return "[MANUAL]";
		return "[NO DATA SOURCE]";
	}
	
	private String _username;
	/**
	 * Overrides the user name as set in the annotation.
	 * @param username
	 */
	public void setUsername(String username) { _username = username; }
	private String _password;
	/**
	 * Overrides the password as set in the annotation.
	 * @param password
	 */
	public void setPassword(String password) { _password = password; }
	
	
	/**
	 * Opens a connection, and ties it to the current instance of the class.
	 * @return The connection object.
	 * @throws SQLException
	 */
	protected Connection openConnection() throws SQLException {
		if (testMode)
			return openManualConnection();
		
		// If a JNDI data source is not being used, then try opening a URL connection.
		if ((_jndiName == null) && (this.getClass().getAnnotation(ConnectionDataSource.class) == null)) 
			return openManualConnection();
		
		String jndiName = (_jndiName != null) ? _jndiName : this.getClass().getAnnotation(ConnectionDataSource.class).jndiName();
		//String name = jndiName;
		
		//if (_conn != null) throw new SQLException("A connection is already established.  Please close the connection first.");
		if (_conn != null) { 
			try { if (!_conn.isClosed()) _conn.close(); } 
			catch (Exception e) { Log.logError("Unable to close connection.", null, e); } 
			finally { _conn = null; }
		}
		
		try {
			DataSource src = getDataSource(jndiName);
			/*
			InitialContext ctx = new InitialContext();
			DataSource src = null;
			// WebLogic & GlassFish don't require the prefix, so we'll try that first.
			try {
				src = (DataSource)ctx.lookup(name); 
			}
			// Tomcat and JBoss do require the prefix, so we'll do that next.
			catch (NamingException e) {
				try {
					name = "java:/" + jndiName;
					src = (DataSource)ctx.lookup(name); 
				}
				catch (NamingException e2) {
					name = "java:/comp/env/" + jndiName;
					src = (DataSource)ctx.lookup(name); 
				} 
			}
			*/
	
			_conn = (Connection)src.getConnection();
			if (!_conn.isValid(2)) {
				_conn.close();
				_conn = (Connection)src.getConnection();
			}
			if (!isManaged()) _conn.setAutoCommit(false);
		}
		catch (NamingException e) {
			_conn = null;
			throw new SQLException("Unable to establish connection to " + jndiName + "."); 
		}

		if (!suppressOpenCallback)
			openCallback(_conn);
		return _conn;
	}
	
	static DataSource getDataSource(String jndiName) throws NamingException {
		InitialContext ctx = new InitialContext();
		// WebLogic & GlassFish don't require the prefix, so we'll try that first.
		try {
			return (DataSource)ctx.lookup(jndiName); 
		}
		// Tomcat and JBoss do require the prefix, so we'll do that next.
		catch (NamingException e) {
			try {
				return (DataSource)ctx.lookup("java:/" + jndiName); 
			}
			catch (NamingException e2) {
				return (DataSource)ctx.lookup("java:/comp/env/" + jndiName); 
			} 
		}
	}
	
	protected Connection openManualConnection() throws SQLException {
		ConnectionParams params = testMode ? testParams : this.getClass().getAnnotation(ConnectionParams.class);
		
		//if (this.getClass().getAnnotation(ConnectionParams.class) == null)
		if (params == null)
			throw new SQLException("No connection params have been supplied.");
		
		/*
		String driver = this.getClass().getAnnotation(ConnectionParams.class).driver();
		String url = this.getClass().getAnnotation(ConnectionParams.class).url();
		String username = (_username != null) ? _username : this.getClass().getAnnotation(ConnectionParams.class).username();
		String password = (_password != null) ? _password : this.getClass().getAnnotation(ConnectionParams.class).password();
		*/
		
		String driver = params.driver();
		String url = params.url();
		String username = (_username != null && !testMode) ? _username : params.username();
		String password = (_password != null && !testMode) ? _password : params.password();
		
		if (_conn != null) throw new SQLException("A connection is already established.  Please close the connection first.");
		
		try {
			Class.forName(driver);
			_conn = (Data.isEmpty(username)) 
			      ? DriverManager.getConnection(url)
				  : DriverManager.getConnection(url, username, password);
			_conn.setAutoCommit(false);
		}
		catch (SQLException e) { throw new SQLException("Unable to establish connection to '" + url + "'.\n" + e.getMessage()); }
		catch (ClassNotFoundException e) { throw new SQLException("Unable to instantiate driver class '" + driver + "'.\n" + e.getMessage()); }

		if (!suppressOpenCallback)
			openCallback(_conn);
		return _conn;
	}
	
	/**
	 * Closes the connection that is tied to the current instance of the class.
	 * @throws SQLException
	 */
	protected void closeConnection() throws SQLException {
		if (_conn != null) {
			try { if (!_conn.isClosed()) _conn.close(); }
			catch (SQLException e) { _conn.close(); throw e; }
			finally { _conn = null; }
		}
	}

	/**
	 * Set the autoCommit setting on the connection that is tied to the current instance of the class.
	 * @param autoCommit
	 * @throws SQLException
	 */
	protected void setAutoCommit(boolean autoCommit) throws SQLException { if (_conn != null) _conn.setAutoCommit(autoCommit); }
	
	/**
	 * Commits the current transaction on the connection that is tied to the current instance of the class.
	 * @throws SQLException
	 */
	protected void commit() throws SQLException { if (_conn != null) _conn.commit(); }
	
	/**
	 * Rolls back the current transaction on the connection that is tied to the current instance of the class.
	 * @throws SQLException
	 */
	protected void rollback() throws SQLException { if (_conn != null) _conn.rollback(); }
	
	
	// Is Atomic
	private static boolean isAtomic(Class<?> clazz) {
		return clazz.isPrimitive() ||
		   	   clazz.getCanonicalName().equals("java.lang.Boolean") ||
	           clazz.getCanonicalName().equals("java.lang.Byte") ||
	           clazz.getCanonicalName().equals("java.lang.Double") ||
	           clazz.getCanonicalName().equals("java.lang.Float") ||
	           clazz.getCanonicalName().equals("java.lang.Integer") ||
	           clazz.getCanonicalName().equals("java.lang.Long") ||
	           clazz.getCanonicalName().equals("java.lang.Short") ||
		       clazz.getCanonicalName().equals("java.lang.String") ||
		       clazz.getCanonicalName().equals("java.math.BigDecimal") ||
		       clazz.getCanonicalName().equals("java.sql.Date") ||
		       clazz.getCanonicalName().equals("java.sql.Time") ||
		       clazz.getCanonicalName().equals("java.sql.Timestamp") ||
		       clazz.getCanonicalName().equals("java.util.Date") ||
		       clazz.getCanonicalName().equals("java.time.LocalDate") ||
		       clazz.getCanonicalName().equals("java.time.LocalDateTime") ||
		       clazz.getCanonicalName().equals("java.time.LocalTime") ||
		       clazz.getCanonicalName().equals("java.time.ZonedDateTime") ||
		       clazz.getCanonicalName().equals("java.time.OffsetDateTime") ||
		       clazz.getCanonicalName().equals("byte[]");
	}
	
	// Get Data Source Column
	private static Object getDataSourceColumn(Class<?> clazz, ResultSet rs, int columnIndex, boolean suppressBinary) throws SQLException {
		String cName = clazz.getCanonicalName().replaceAll("\\[\\]", "");
		return (cName.equals("boolean")) ? rs.getBoolean(columnIndex)
			 : (!clazz.isArray() && cName.equals("byte")) ? rs.getByte(columnIndex)
			 : (cName.equals("double")) ? rs.getDouble(columnIndex)
			 : (cName.equals("float")) ? rs.getFloat(columnIndex)
			 : (cName.equals("int")) ? rs.getInt(columnIndex)
			 : (cName.equals("long")) ? rs.getLong(columnIndex)
			 : (cName.equals("short")) ? rs.getShort(columnIndex)
			 : (rs.getObject(columnIndex) == null) ? null
			 : (cName.equals("java.lang.Boolean")) ? Boolean.valueOf(rs.getBoolean(columnIndex))
			 : (cName.equals("java.lang.Byte")) ? Byte.valueOf(rs.getByte(columnIndex))
			 : (cName.equals("java.lang.Double")) ? Double.valueOf(rs.getDouble(columnIndex))
			 : (cName.equals("java.lang.Float")) ? Float.valueOf(rs.getFloat(columnIndex))
			 : (cName.equals("java.lang.Integer")) ? Integer.valueOf(rs.getInt(columnIndex))
			 : (cName.equals("java.lang.Long")) ? Long.valueOf(rs.getLong(columnIndex))
			 : (cName.equals("java.lang.Short")) ? Short.valueOf(rs.getShort(columnIndex))
			 : (cName.equals("java.lang.String")) ? rs.getString(columnIndex)
			 : (cName.equals("java.math.BigDecimal")) ? rs.getBigDecimal(columnIndex)
			 : (cName.equals("java.sql.Date")) ? rs.getDate(columnIndex)
			 : (cName.equals("java.sql.Time")) ? rs.getTime(columnIndex)
		     : (cName.equals("java.sql.Timestamp")) ? rs.getTimestamp(columnIndex)
		     : (cName.equals("java.util.Date")) ? new java.util.Date(rs.getTimestamp(columnIndex).getTime())
    		 : (cName.equals("java.time.LocalDate")) ? rs.getObject(columnIndex, java.time.LocalDate.class) // rs.getTimestamp(columnIndex).toInstant().atZone(ZoneId.of("UTC")).toLocalDate()
		     : (cName.equals("java.time.LocalDateTime")) ? rs.getTimestamp(columnIndex).toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()
		     : (cName.equals("java.time.LocalTime")) ? rs.getObject(columnIndex, java.time.LocalTime.class) // rs.getTimestamp(columnIndex).toInstant().atZone(ZoneId.of("UTC")).toLocalTime()
		     : (cName.equals("java.time.ZonedDateTime")) ? rs.getTimestamp(columnIndex).toInstant().atZone(ZoneId.systemDefault())
		     : (cName.equals("java.time.OffsetDateTime")) ? rs.getTimestamp(columnIndex).toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime() 
		     : (clazz.isArray() && cName.equals("byte")) ? (suppressBinary ? null : rs.getBytes(columnIndex))
		     : rs.getObject(columnIndex);
	}
	
	// Get Return Type
	private static int getReturnType(Class<?> clazz) {
		if (clazz == null) return -1;
		String cName = clazz.getCanonicalName();
		
		return (cName.equals("boolean")) ? Types.BOOLEAN
			 : (cName.equals("byte")) ? Types.TINYINT
			 : (cName.equals("double")) ? Types.DOUBLE
			 : (cName.equals("float")) ? Types.FLOAT
			 : (cName.equals("int")) ? Types.INTEGER
			 : (cName.equals("long")) ? Types.BIGINT
			 : (cName.equals("short")) ? Types.SMALLINT
			 : (cName.equals("java.lang.Boolean")) ? Types.BOOLEAN
			 : (cName.equals("java.lang.Byte")) ? Types.TINYINT
			 : (cName.equals("java.lang.Double")) ? Types.DOUBLE
			 : (cName.equals("java.lang.Float")) ? Types.FLOAT
			 : (cName.equals("java.lang.Integer")) ? Types.INTEGER
			 : (cName.equals("java.lang.Long")) ? Types.BIGINT
			 : (cName.equals("java.lang.Short")) ? Types.SMALLINT
			 : (cName.equals("java.lang.String")) ? Types.VARCHAR
			 : (cName.equals("java.math.BigDecimal")) ? Types.DECIMAL
			 : (cName.equals("java.sql.Date")) ? Types.DATE
			 : (cName.equals("java.sql.Time")) ? Types.TIME
		     : (cName.equals("java.sql.Timestamp")) ? Types.TIMESTAMP
		     : (cName.equals("java.util.Date")) ? Types.TIMESTAMP
		     : (cName.equals("java.time.LocalDate")) ? Types.DATE
		     : (cName.equals("java.time.LocalDateTime")) ? Types.TIMESTAMP
		     : (cName.equals("java.time.LocalTime")) ? Types.TIME
		     : (cName.equals("java.time.ZonedDateTime")) ? Types.TIMESTAMP
		     : (cName.equals("java.time.OffsetDateTime")) ? Types.TIMESTAMP
		     : (cName.equals("byte[]")) ? Types.BLOB
		     : Types.OTHER;
	}

	// Get Return Value
	private static Object getReturnValue(Class<?> clazz, CallableStatement s) throws SQLException {
		if (clazz == null) return -1;
		String cName = clazz.getCanonicalName();
		
		return (cName.equals("boolean")) ? s.getBoolean(1)
			 : (cName.equals("byte")) ? s.getByte(1)
			 : (cName.equals("double")) ? s.getDouble(1)
			 : (cName.equals("float")) ? s.getFloat(1)
			 : (cName.equals("int")) ? s.getInt(1)
			 : (cName.equals("long")) ? s.getLong(1)
			 : (cName.equals("short")) ? s.getShort(1)
			 : (cName.equals("java.lang.Boolean")) ? Boolean.valueOf(s.getBoolean(1))
			 : (cName.equals("java.lang.Byte")) ? Byte.valueOf(s.getByte(1))
			 : (cName.equals("java.lang.Double")) ? Double.valueOf(s.getDouble(1))
			 : (cName.equals("java.lang.Float")) ? Float.valueOf(s.getFloat(1))
			 : (cName.equals("java.lang.Integer")) ? Integer.valueOf(s.getInt(1))
			 : (cName.equals("java.lang.Long")) ? Long.valueOf(s.getLong(1))
			 : (cName.equals("java.lang.Short")) ? Short.valueOf(s.getShort(1))
			 : (cName.equals("java.lang.String")) ? s.getString(1)
			 : (cName.equals("java.math.BigDecimal")) ? s.getBigDecimal(1)
			 : (cName.equals("java.sql.Date")) ? s.getDate(1)
			 : (cName.equals("java.sql.Time")) ? s.getTime(1)
		     : (cName.equals("java.sql.Timestamp")) ? s.getTimestamp(1)
		     : (cName.equals("java.util.Date")) ?  new java.util.Date(s.getTimestamp(1).getTime())
		     : (cName.equals("java.time.LocalDate")) ? s.getTimestamp(1).toLocalDateTime().toLocalDate()
		     : (cName.equals("java.time.LocalDateTime")) ? s.getTimestamp(1).toLocalDateTime()
		     : (cName.equals("java.time.LocalTime")) ? s.getTimestamp(1).toLocalDateTime().toLocalTime()
		     : (cName.equals("java.time.ZonedDateTime")) ? s.getTimestamp(1).toLocalDateTime().atZone(ZoneId.systemDefault())
		     : (cName.equals("java.time.OffsetDateTime")) ? s.getTimestamp(1).toLocalDateTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()
		     : Types.OTHER;
	}
	
	/**
	 * Replaces each parameter index notation with each given parameter.  Each parameter index
	 * notation is in the form <b><code>{x}</code></b> or <b><code>{sql: x}</code></b> where 
	 * <b><code>x</code></b> is the parameter's index (ordinal position, starting with 
	 * <b><code>1</code></b>).  If the <b><code>{x}</code></b> form is used, the parameter is
	 * formatted using <code>String com.flyingj.common.util.Util.obj2SQLStr(java.lang.Object Source)</code>,
	 * otherwise, if the <b><code>{sql: x}</code></b> form is used, then the string form of
	 * the parameter is used as-is, with no additional formatting.<br/>
	 * <br/>
	 * IMPORTANT NOTE: While a collection of values may be passed as a parameer, an array cannot.
	 * To resolve this, either convert the array to a collection or use applyParameterBean.
	 * 
	 * @param statement A SQL statement with parameter index notations.
	 * @param params Parameters to be used in preparing statement.
	 * @return statement with all parameter notations replaced by actual parameter values.
	 */
	public String applyParameters(String statement, Object... params) {
		String result = statement;
	
		for (int i = 1; i <= params.length; i++) {
			result = result.replaceAll("\\{" + i + "\\}", Matcher.quoteReplacement(evalParamObject(params[i - 1])))
			               .replaceAll("\\{sql: " + i + "\\}", Matcher.quoteReplacement(evalParamObject(params[i - 1], true)));
		}

		String callingMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
		Log.logDebug("\n" + result + "\n", null, callingMethod);
		
		return result;
	}

	/**
	 * Applies indexed parameters to a JdbcStatement.  Each parameter index
	 * notation is in the form <b><code>{x}</code></b> or <b><code>{sql: x}</code></b> where 
	 * <b><code>x</code></b> is the parameter's index (ordinal position, starting with 
	 * <b><code>1</code></b>).  If the <b><code>{x}</code></b> form is used, the parameter is
	 * formatted using <code>String com.flyingj.common.util.Util.obj2SQLStr(java.lang.Object Source)</code>,
	 * otherwise, if the <b><code>{sql: x}</code></b> form is used, then the string form of
	 * the parameter is used as-is, with no additional formatting.<br/>
	 * <br/>
	 * IMPORTANT NOTE: While a collection of values may be passed as a parameer, an array cannot.
	 * To resolve this, either convert the array to a collection or use applyParameterBean.
	 * 
	 * @param statement
	 * @param params
	 */
	public void applyParameters(JdbcStatement statement, Object... params) {
		for (Object param : params)
			statement.addIndexedParameter(param);
		
		String callingMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
		statement.debug(callingMethod);
	}
	
	/**
	 * <b>upcaseFirst</b><br><br>
	 * Returns source with the first letter upper-cased.
	 * @param source - The string to upper-case the first letter of.
	 * @return the string with the first letter upper-cased.
	 */
	protected static String upcaseFirst(String source) {
		return source.isEmpty() ? source : source.substring(0, 1).toUpperCase() + source.substring(1);
	}
	
	/**
	 * Replaces each parameter name notation with the value from the named field's
	 * getter in the bean.  Each parameter name notation is in the form
	 * <b><code>{name}</code></b> or <b><code>{sql: name}</code></b> where 
	 * <b><code>name</code></b> is a field name in the bean.  Note that the field
	 * itself is not accessed, but the getter for that field is accessed.  So, 
	 * for example, if <b><code>{favoriteColor}</code></b> is found, the getter
	 * named <b><code>getFavoriteColor</code></b> will be searched for.  If no
	 * getter matches that name, then a NoSuchMethod exception will be thrown.<br>
	 * <br>
	 * Please see the notes for <b><code>applyParameters</code></b> for usage of
	 * the <b><code>sql:</code></b> parameter notation prefix.<br/>
	 * <br/>
	 * IMPORTANT NOTE: Both collections of values and arrays may be referenced as parameters.
	 * 
	 * @param statement a SQL statement with parameter name notations.
	 * @param bean the bean containing the parameters to be used in preparing statement.
	 * @return statement with all parameter notations replaced by actual parameter values.
	 */
	public String applyParameterBean(String statement, Object bean) throws SQLException {
		String result = statement;
		int i = result.indexOf("{");
		boolean found = false;

		while (i > -1) {
			int x = result.indexOf("}", i + 1);
			String name = result.substring(i + 1, x);
			boolean sql = name.indexOf("sql:") == 0;
			if (!sql && Pattern.compile("\\W").matcher(name).lookingAt()) {
				// If the pattern matches, then this isn't a valid column/field name; it's most likely an encoded string.
				i = result.indexOf("{", i + 1);
				continue;
			}
			Object value = null;
			if (bean instanceof Map map) {
				value = map.get(name);
				result = result.replace("{" + name + "}", evalParamObject(value, sql));
			}
			else {
				Log.logDebug("Parsing getter name for: " + name, null, "applyParameterBean");
				String getterName = "get" + upcaseFirst((sql) ? name.substring(4).trim() : name.trim());
				value = null;
				try {
					Method method = Data.getDeclaredMethod(bean.getClass(), getterName);
					found = true;
					value = method.invoke(bean);
				}
				catch (NoSuchMethodException e) {
					Log.logWarning("The getter name '" + getterName + "' for object (" + bean.getClass().getCanonicalName() + ") was not found.", null);
					found = false;
				}
				catch (Exception e) {
					throw new SQLException(e.getMessage() + " '" + getterName + "'"); 
				}
				if (found) 
					result = result.replace("{" + name + "}", evalParamObject(value, sql));
			}
            i = result.indexOf("{", i + 1);
		}
		
		return result;
	}
	
	/**
	 * Applies named parameters from a bean to a JdbcStatement.  Each parameter name notation is in the form
	 * <b><code>{name}</code></b> or <b><code>{sql: name}</code></b> where 
	 * <b><code>name</code></b> is a field name in the bean.  Note that the field
	 * itself is not accessed, but the getter for that field is accessed.  So, 
	 * for example, if <b><code>{favoriteColor}</code></b> is found, the getter
	 * named <b><code>getFavoriteColor</code></b> will be searched for.  If no
	 * getter matches that name, then a NoSuchMethod exception will be thrown.<br>
	 * <br>
	 * Please see the notes for <b><code>applyParameters</code></b> for usage of
	 * the <b><code>sql:</code></b> parameter notation prefix.<br/>
	 * <br/>
	 * IMPORTANT NOTE: Both collections of values and arrays may be referenced as parameters.
	 * 
	 * @param statement a SQL statement with parameter name notations.
	 * @param bean the bean containing the parameters to be used in preparing statement.
	 * @return statement with all parameter notations replaced by actual parameter values.
	 */
	public void applyParameterBean(JdbcStatement statement, Object bean) throws SQLException {
		String[] names = statement.getParamNames();

		for (String name : names) {
			String getterName = "get" + upcaseFirst(name);
			try {
				Method method = Data.getDeclaredMethod(bean.getClass(), getterName);
				statement.addNamedParamter(name, method.invoke(bean));
			}
			catch (NoSuchMethodException e) {
				Log.logWarning("The getter name '" + getterName + "' for object (" + bean.getClass().getCanonicalName() + ") was not found.", null);
			}
			catch (Exception e) {
				throw new SQLException(e.getMessage() + " '" + getterName + "'"); 
			}
		}
	}
	
	/**
	 * Creates a call statement for a stored procedure or function.
	 * 
	 * @param storedFunctionName The stored function name to use.
	 * @param returns Whether the stored function/procedure returns a value.
	 * @param params Parameters to be used in preparing statement. 
	 * @return statement prepared for execution.
	 */
	public String createCall(String storedFunctionName, boolean returns, Object... params) {
		StringBuilder result = new StringBuilder("");
		for (Object param : params)
			result.append(formatParam(null, param, result.isEmpty()));
		return formatCall(storedFunctionName, returns, result.toString());
	}
	
	/**
	 * Creates a call statement for a stored procedure or function using named parameters.
	 * <b>NOTE: This can only be used with Oracle or MS SQL Server.  
	 * MySQL and PostgreSQL don't support named parameter referencing.</b>
	 * 
	 * @param storedFunctionName The stored function name to use.
	 * @param returns Whether the stored function/procedure returns a value.
	 * @param params A parameter map of name-value pairs to be used in preparing statement. 
	 * @return statement prepared for execution.
	 */
	public String createCallNamed(String storedFunctionName, boolean returns, Map<String,Object> params) {
		if (!Data.in(dbName, new DbmsType[] { DbmsType.ORACLE, DbmsType.SQL_SERVER }))
			throw new IllegalStateException("A named parameter stored procedure call may not be used with the selected database.");
		StringBuilder result = new StringBuilder("");
		for (Entry<String,Object> entry : params.entrySet())
			result.append(formatParam(entry.getKey(), entry.getValue(), result.isEmpty()));
		return formatCall(storedFunctionName, returns, result.toString());
	}
	
	/**
	 * Format a parameter for the createCall and createCallNamed methods.
	 * Only applicable to Oracle and Microsoft SQL Server.
	 * @param name
	 * @param value
	 * @param first
	 * @return
	 */
	private String formatParam(String name, Object value, boolean first) {
		String delimiter = first ? "" : ", ";
		String refformat = dbName == DbmsType.ORACLE ? "%s => " : "@%s = ";
		String nameref = name == null ? "" : String.format(refformat, name);
		return delimiter + nameref + evalParamObject(value);
	}
	
	/**
	 * Format a stored procedure call for the createCall and createCallNamed methods.
	 * @param storedFunctionName
	 * @param returns
	 * @param params
	 * @return
	 */
	private String formatCall(String storedFunctionName, boolean returns, String params) {
		String returnref = returns ? "? = " : "";
		return String.format("{ %scall %s(%s) }", returnref, storedFunctionName, params);
	}
	
	public static Object mEscape(Object source) {
		if ((source == null) || !(source instanceof String))
			return source;
		String result = (String)source;
		for (int i = result.length() - 1; i > -1; i--)
		    if ((result.charAt(i) == '$') || (result.charAt(i) == '\\'))
		    	result = result.substring(0, i) + "\\" + result.substring(i);
		return result;
	}
	
	/**
	 * Evaluates a parameter object for inclusion into a statement.
	 * @param param
	 * @return
	 */
	public String evalParamObject(Object param) {
		return Data.obj2SQLStr(dbName, dbName == DbmsType.MYSQL ? mEscape(param) : param);
	}
	
	/**
	 * Evaluates a parameter object for inclusion into a statement.
	 * @param param
	 * @param sql If true, use as-is, otherwise use default behavior.
	 * @return
	 */
	public String evalParamObject(Object param, boolean sql) {
		return (!sql) ? evalParamObject(param) :
			   (param == null) ? "" : param.toString();
	}
	
	/**
	 * Gets the contents of a text SQL file and returns it as a string.
	 * If the file doesn't exist, then a null is returned.
	 * The name of the file is relative to the path specified in the
	 * <b>SQLFileContext</b> annotation's <b>path</b> attribute.  If
	 * this annotation is not present on the class, then the name must 
	 * contain a full path.  If the annotation is present, but the file 
	 * is not at that path, use <b>getSQLFile(String name, String path)</b> 
	 * instead.
	 * @param name the name of the file
	 * @return the contents of the specified file
	 */
	public String getSQLFileNull(String name) {
		SQLFileContext c = getClass().getAnnotation(SQLFileContext.class);
		String path = (c == null) ? null : c.path();
		try {
			return getSQLFile(name, path);
		}
		catch (NullPointerException e) {
			Log.logException(e, null);
			return null;
		}
	}
	
	/**
	 * Gets the contents of a text SQL file and returns it as a string.
	 * The name of the file is relative to the path specified in the
	 * <b>SQLFileContext</b> annotation's <b>path</b> attribute.  If
	 * this annotation is not present on the class, then the name must 
	 * contain a full path.  If the annotation is present, but the file 
	 * is not at that path, use <b>getSQLFile(String name, String path)</b> 
	 * instead.
	 * @param name the name of the file
	 * @return the contents of the specified file
	 */
	public String getSQLFile(String name) {
		SQLFileContext c = getClass().getAnnotation(SQLFileContext.class);
		String path = (c == null) ? null : c.path();
		return getSQLFile(name, path);
	}
	
	/**
	 * Gets the contents of a text SQL file and returns it as a string.
	 * The name of the file is relative to the path specified in the
	 * <b>path</b> parameter.  This overrides the <b>path</b> attribute
	 * of the <b>SQLFileContext</b> annotation, if present on the class.
	 * @param name the name of the file
	 * @return the contents of the specified file
	 */
	public String getSQLFile(String name, String path) {
		String _path = (path == null) ? "" : Data.enforceEnd(path, "/");
		return Data.readTextFile(getClass(), _path + name);
	}

	private boolean suppressOpenCallback;

	/**
	 * Performs an operation after opening a connection.  An example 
	 * use case is for setting VPD data after opening a connection to
	 * an Oracle Enterprise database.<br/><br/>
	 * <i>Do not call execQuery in this method.  If needing to run 
	 * execUpdate or execProcedure, then only use those methods that 
	 * take the autoCommit parameter.</i>
	 * <b>WARNING: Failure to adhere to this precaution will cause an 
	 * infinite recursion loop.</b>
	 * @throws SQLException
	 */
	protected void openCallback(Connection conn) throws SQLException {
		// Stub.
	}
	
	private String schema;
	
	public final String getSchema() { return schema; }
	/**
	 * Set the schema to use for statement execution.  Set to null to use the default schema for the connection. 
	 * @return
	 */
	public final void setSchema(String schema) { this.schema = schema; }
	
	private String defaultSchema;
	
	protected final void useSchema(Connection conn) throws SQLException {
		if (schema != null) {
			if (dbName == DbmsType.MYSQL) {
				defaultSchema = conn.getCatalog();
				conn.setCatalog(schema);
			} else {
				defaultSchema = conn.getSchema();
				conn.setSchema(schema);
			}
		}
	}
	
	protected final void restoreSchema(Connection conn) throws SQLException {
		if (defaultSchema == null)
			return;
		if (conn != null && !conn.isClosed()) {
			if (dbName == DbmsType.MYSQL)
				conn.setCatalog(defaultSchema);
			else
				conn.setSchema(defaultSchema);
		}
		defaultSchema = null;
	}
}
