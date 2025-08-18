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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.JdbcLookup;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.LoggerParams;
import com.roth.jdbc.annotation.NoTest;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.annotation.Table;
import com.roth.jdbc.annotation.Tables;
import com.roth.jdbc.model.EnhancedBean;
import com.roth.jdbc.model.LoggerBean;
import com.roth.jdbc.model.LoggerBean.LogValue;
import com.roth.jdbc.model.StateBean;

@NoTest
public class TableUtil extends JdbcUtil {
	private static final long serialVersionUID = 8518147379787359673L;
	
	protected class HashTable {
		private String schema;
		private String name;
		private String[] columns;
		private String[] pkColumns;
		
		public HashTable(String schema, String name, String[] pkColumns) {
			this.schema = (Data.isEmpty(schema)) ? null : schema;
			this.name = name;
			this.pkColumns = pkColumns;
		}
		
		public String getSchema() { return schema; }
		public String getName() { return name; }
		public String[] getColumns() { return columns; }
		public void setColumns(String[] columns) { this.columns = columns; }
		public String[] getPkColumns() { return pkColumns; }
		public void setPkColumns(String[] pkColumns) { this.pkColumns = pkColumns; }
		
		public String getQName() { return ((Data.isEmpty(schema)) ? "" : schema + ".") + name; }
	}

	
	private transient HashMap<Class<?>, HashTable> _tables;
	
	
	/**
	 * Initializes an instance of TableUtil.  This can be used without annotations.
	 * @param jndiName the JNDI data source name
	 * @param tableName the table to use
	 * @param beanClass the bean class to use for data selection
	 */
	public TableUtil(String jndiName, String schemaName, String tableName, Class<?> beanClass) throws SQLException {
		super(jndiName);
		init(schemaName, tableName, null, beanClass);
	}
	
	/**
	 * Initializes an instance of TableUtil.  This requires the
	 * use of the @ConnectionDataSource annotation.
	 * @param tableName the table to use
	 * @param beanClass the bean class to use for data selection
	 */
	public TableUtil(String schemaName, String tableName, Class<?> beanClass) throws SQLException {
		super();
		init(schemaName, tableName, null, beanClass);
	}

	/**
	 * 
	 * @param jndiName
	 */
	public TableUtil(String jndiName) throws SQLException {
		super(jndiName);
		_tables = new HashMap<>();
	}
	
	/**
	 * Initializes an instance of TableUtil.  This requires the
	 * use of both the @ConnectionDataSource and @Table annotations.
	 */
	public TableUtil() throws SQLException {
		super();
		Tables tables = this.getClass().getAnnotation(Tables.class);
		if (tables != null)
			for (int i = 0; i < tables.tables().length; i++) {
				Table table = tables.tables()[i];
				init(table.schema(), table.name(), table.primaryKeyColumns(), table.beanClass());
			}
		else if (_tables == null)
			_tables = new HashMap<>();
	}
	
	private void init(String schemaName, String tableName, String[] pkColumns, Class<?> beanClass) {
		if (_tables == null) _tables = new HashMap<>();
		_tables.put(beanClass, new HashTable(schemaName, tableName, pkColumns));
		getTableDefinition(_tables.get(beanClass), beanClass);
	}
	
	private String[] getColumnNames(Connection conn, String schemaName, String tableName) throws SQLException {
		String[] columnNames = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try {
			String table = (schemaName == null) ? tableName : schemaName + "." + tableName;
			s = conn.prepareStatement("SELECT * FROM " + table + " WHERE 1 = 0");
			rs = s.executeQuery();
			ResultSetMetaData m = rs.getMetaData();
			int count = m.getColumnCount();
			columnNames = new String[count];
			for (int i = 0; i < count; i++)
				columnNames[i] = m.getColumnName(i + 1).toLowerCase();
			rs.close();
			s.close();
		}
		catch (SQLException e) {
			Log.logError("Error during statement preparation or execution while acquiring metadata.", null, e);
			throw e;
		}
		catch (Exception e) { Log.logError("Unexpected Error.", null, e); }
		finally {
			try { if (rs != null) rs.close(); }
			catch (SQLException e) { Log.logError("Unable to close resultset.", null, e); }
			try { if (s != null) s.close(); }
			catch (SQLException e) { Log.logError("Unable to close statement.", null, e); }
			closeConnection();
		}
		return columnNames;
	}
	
	@SuppressWarnings("unused")
	private void getTableDefinition(HashTable table, Class<?> beanClass) {
		try {
			PermissiveBinding pb = this.getClass().getAnnotation(PermissiveBinding.class); 
			if (pb == null) pb = beanClass.getAnnotation(PermissiveBinding.class);
			String tableName = table.getName().replaceAll("`", "");
		    boolean newConnection = getConnection() == null;
			Connection conn = newConnection ? openConnection() : getConnection();
			ResultSet columns = conn.getMetaData().getColumns(getDbName() == DbmsType.MYSQL ? table.getSchema() : null, table.getSchema(), tableName, null);
			String columnNames = "";
			// Process meta data columns.
			while (columns.next())
				columnNames += (Data.isEmpty(columnNames) ? "" : "|") + columns.getString("COLUMN_NAME").toLowerCase();
			// If no meta data columns, then get the columns from an empty result set.
			String[] cn = (columnNames.isEmpty()) ? getColumnNames(conn, table.getSchema(), tableName) : columnNames.split("\\|");
			// Clear the string.
			columnNames = "";
			for (String n : cn) {
				// If the @PermissiveBinding annotation is found, then output a warning to the log if 
				// a getter is not found, but do not include the column in the list.
				if (pb != null) {
					String methodName = "get" + n.substring(0, 1).toUpperCase() + n.substring(1);
					try { Method method = Data.getDeclaredMethod(beanClass, methodName); }
					catch (NoSuchMethodException e) {
						String ccLabel = n.replaceAll("_", "");
						methodName = "get" + ccLabel.substring(0, 1).toUpperCase() + ccLabel.substring(1);
						try { Method method = Data.getDeclaredMethod(beanClass, methodName); }
						catch (NoSuchMethodException e2) {
							if (!pb.suppressWarnings()) {
								String warning = "No field getter was found for column label '" + n + "' in class '" + beanClass.getCanonicalName() + "'.";
							    Log.logWarning(warning, "<unavailable>");
							}
							continue;
						}
					}
				}
				columnNames += (Data.isEmpty(columnNames) ? "" : "|") + n;
			}
			table.setColumns(columnNames.split("\\|"));
//			ResultSet keys = conn.getMetaData().getPrimaryKeys(null, table.getSchema(), tableName);
//			String keyNames = "";
//			while (keys.next()) keyNames += (Data.isEmpty(keyNames) ? "" : "|") + keys.getString("COLUMN_NAME");
			//PrimaryKey pk = getClass().getAnnotation(PrimaryKey.class);
//			if (table.getPkColumns() == null) 
//				table.setPkColumns(keyNames.split("\\|"));
			//_keyNames = (pk != null) ? pk.columnNames() : keyNames.split("\\|");
			if (newConnection) closeConnection();
		}
		catch (Exception e) { Log.logException(e, "<unavailable>"); }
	}
	
	private void validate(Class<?> beanClass) throws SQLException {
		HashTable t = _tables.get(beanClass);
		if (t == null) {
			JdbcTable jt = beanClass.getAnnotation(JdbcTable.class);
			if (jt == null) throw new SQLException("No @JdbcTable annotation was found in " + beanClass.getCanonicalName() + ".");
			init(jt.schema(), jt.name(), jt.primaryKeyColumns(), beanClass);
			t = _tables.get(beanClass);
		}
		if ((t == null) && _tables.isEmpty()) throw new SQLException("No @Tables annotation was found, and no table was manually defined.");
		if (t == null) throw new SQLException("No @Table annotation was found, and no table was manually defined.");
		//if (_beanClass == null) throw new SQLException("No @Table annotation was found, and no beanClass was manually defined.");
		if (t.getColumns() == null) throw new SQLException("No table definition was found.");
		if (t.getPkColumns() == null) throw new SQLException("No primary key definition was found.");
	}
	
	/**
	 * Get the record that matches the where clause in the defined table.
	 * @param <T>
	 * @param beanClass the bean class that defines the table
	 * @param where the where clause to filter the data
	 * @return a bean or null, if no record matches
	 * @throws SQLException
	 */
	public <T> T get(Class<?> beanClass, String where) throws SQLException {
		return get(beanClass, where, null);
	}
	
	/**
	 * Get the record that matches the where clause in the defined table.
	 * @param <T>
	 * @param beanClass the bean class that defines the table
	 * @param where the where clause to filter the data
	 * @param bean the bean to apply to the where clause (see the "See Also" notation below)
	 * @return a bean or null, if no record matches
	 * @throws SQLException
	 * @see {@link JdbcUtil#applyParameterBean(String, Object)}
	 */
	public <T> T get(Class<?> beanClass, String where, Object bean) throws SQLException {
		validate(beanClass);
		HashTable t = _tables.get(beanClass);
		String statement = "SELECT " + Data.join(t.getColumns(), ", ") + " " +
		                     "FROM " + t.getQName();
		if (where != null) statement += " WHERE " + where;
		if (bean != null) statement = applyParameterBean(statement, bean);
		Log.logDebug(statement, null, "get");
		return execQuery(statement, beanClass);
	}
	
	/**
	 * Gets a list of all records in the defined table.
	 * @param <T>
	 * @return a Vector of beans
	 * @throws SQLException
	 */
	public <T> T getList(Class<?> beanClass) throws SQLException { return getList(beanClass, null, null, null); }
	
	/**
	 * Gets a list of all records that match the where clause in the defined table.
	 * @param <T>
	 * @param where the where clause to filter the data
	 * @return a Vector of beans
	 * @throws SQLException
	 */
	public <T> T getList(Class<?> beanClass, String where) throws SQLException { return getList(beanClass, where, null, null); }
	public <T> T getList(Class<?> beanClass, String where, Object bean) throws SQLException { return getList(beanClass, where, null, bean); }
	
	/**
	 * Gets a list of all records that match the where clause in the defined table.
	 * @param <T>
	 * @param beanClass the bean class that defines the table
	 * @param where the where clause to filter the data
	 * @param order the order clause to order the data
	 * @param bean the bean to apply to the where clause (see the "See Also" notation below)
	 * @return a Vector of beans
	 * @throws SQLException
	 * @see {@link JdbcUtil#applyParameterBean(String, Object)}
	 */
	public <T> T getList(Class<?> beanClass, String where, String order, Object bean) throws SQLException {
		validate(beanClass);
		HashTable t = _tables.get(beanClass);
		String statement = "SELECT " + Data.join(t.getColumns(), ", ") + " " +
		                     "FROM " + t.getQName();
		if (where != null) statement += " WHERE " + where;
		if (order != null) statement += " ORDER BY " + order;
		if (bean != null) statement = applyParameterBean(statement, bean);
		return execQuery(statement, ArrayList.class, beanClass);
	}
	
	protected String getMapStatement(String table, String key, String value, String order, String where) {
		String result = "SELECT " + key + " AS keycol, " + value + " AS valcol FROM " + table;
		if (where != null) result += " WHERE " + where;
		if (order != null) result += " ORDER BY " + order;
		return result;
	}
	
	protected Map<String,String> getMap(String statement) throws SQLException {
		return execQuery(statement, LinkedHashMap.class);
	}
	/**
	 * Return a map of key-value pairs from a table for lookup purposes.
	 * @param table the table name
	 * @param key the column name representing the key
	 * @param value the column name representing the value
	 * @return
	 * @throws SQLException
	 */
	public Map<String,String> getMap(String table, String key, String value) throws SQLException {
		return getMap(getMapStatement(table, key, value, null, null));
	}
	/**
	 * Return a map of key-value pairs from a table for lookup purposes.
	 * @param table the table name
	 * @param key the column name representing the key
	 * @param value the column name representing the value
	 * @param order the order by clause (not including 'ORDER BY')
	 * @return
	 * @throws SQLException
	 */
	public Map<String,String> getMap(String table, String key, String value, String order) throws SQLException {
		return getMap(getMapStatement(table, key, value, order, null));
	}
	/**
	 * Return a map of key-value pairs from a table for lookup purposes.
	 * @param table the table name
	 * @param key the column name representing the key
	 * @param value the column name representing the value
	 * @param order the order by clause (not including 'ORDER BY')
	 * @param where the where clause (not including 'WHERE')
	 * @return
	 * @throws SQLException
	 */
	public Map<String,String> getMap(String table, String key, String value, String order, String where) throws SQLException {
		return getMap(getMapStatement(table, key, value, order, where));
	}
	
	/**
	 * Convert a <String,String> map to a <Integer,String> map.  This requires that the key value can be converted to a Integer.
	 * @param source
	 * @return
	 */
	public Map<Integer,String> toIntMap(Map<String,String> source) {
		Map<Integer,String> result = new LinkedHashMap<>();
		source.entrySet().forEach(e -> result.put(Data.strToInteger(e.getKey()), e.getValue())); 
		return result;
	}
	
	/**
	 * Convert a <String,String> map to a <Long,String> map.  This requires that the key value can be converted to a Long.
	 * @param source
	 * @return
	 */
	public Map<Long,String> toLongMap(Map<String,String> source) {
		Map<Long,String> result = new LinkedHashMap<>();
		source.entrySet().forEach(e -> result.put(Data.strToLong(e.getKey()), e.getValue())); 
		return result;
	}
	
	/**
	 * Get a typed map (a map who's key is the primary key of the table, and the value is the bean representing the corresponding record).
	 * The key must be an atomic value (i.e., made up of a single column); the reason for this is that the key must be a type that is both
	 * easily referenced and corresponds to a single getter return value.
	 * @param <K>
	 * @param <T>
	 * @param keyClass
	 * @param valueClass
	 * @return
	 * @throws SQLException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public <K,T> Map<K, T> getTypedMap(Class<K> keyClass, Class<T> valueClass) throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return getTypedMap(keyClass, valueClass, null);
	}
	
	/**
	 * Get a typed map (a map who's key is the primary key of the table, and the value is the bean representing the corresponding record).
	 * The key must be an atomic value (i.e., made up of a single column); the reason for this is that the key must be a type that is both
	 * easily referenced and corresponds to a single getter return value.
	 * @param <K>
	 * @param <T>
	 * @param keyClass
	 * @param valueClass
	 * @param filter
	 * @return
	 * @throws SQLException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public <K,T> Map<K, T> getTypedMap(Class<K> keyClass, Class<T> valueClass, String filter) throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		JdbcTable table = valueClass.getAnnotation(JdbcTable.class);
		if (table == null)
			throw new IllegalArgumentException("The specified class must have a JdbcTable annotation.");
		if (table.primaryKeyColumns().length != 1)
			throw new IllegalArgumentException("The specified class's JdbcTable annotation must define an atomic primary key.");
		String getter = Data.getGetterName(table.primaryKeyColumns()[0]);
		Method method = valueClass.getMethod(getter);
		if (!method.getReturnType().getTypeName().equals(keyClass.getCanonicalName()))
			throw new IllegalArgumentException("The specified class's primary key should be a " + keyClass.getCanonicalName() + ".");
		List<T> list = getList(valueClass, filter);
		Map<K, T> map = new HashMap<>();
		if (list != null)
			for (T item : list)
				map.put((K)method.invoke(item), item);
		return map;
	}
	
	protected static boolean arrayHasLob(Object[] beans) {
		for (Object bean : beans) {
			if (hasLob(bean))
				return true;
		}
		return false;
	}
	
	protected static boolean collectionHasLob(Collection<?> beans) {
		for (Object bean : beans) {
			if (hasLob(bean))
				return true;
		}
		return false;
	}	
	
	protected static boolean hasLob(Object bean) {
		JdbcTable table = bean.getClass().getAnnotation(JdbcTable.class);
		if (table == null)
			throw new IllegalArgumentException("The object specified in the argument must have a JdbcTable annotation.");
		return table.hasLob();
	}
	
	/**
	 * Get the primary key filter template for a StateBean that has a single-column integer-based primary key.
	 * @param bean
	 * @return
	 */
	public static String getPrimaryFilter(StateBean bean, boolean numeric) {
		JdbcTable table = bean.getClass().getAnnotation(JdbcTable.class);
		return Data.exJoin(table.primaryKeyColumns(), " AND ", numeric);
	}
	
	/**
	 * Get the primary key for a StateBean that has a single-column Long-based primary key.
	 * @param bean
	 * @return
	 */
	public static Object[] getPrimaryKey(StateBean bean) {
		JdbcTable table = bean.getClass().getAnnotation(JdbcTable.class);
		Object[] result = new Object[table.primaryKeyColumns().length];
		try {
			for (int i = 0; i < result.length; i++) {
				Method getter = bean.getClass().getMethod(Data.getGetterName(table.primaryKeyColumns()[i]));
				result[i] = getter.invoke(bean);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.logException(e, null);
		}
		return result;
	}
	
	protected static final int SQL_INSERT = 0;
	protected static final int SQL_UPDATE = 1;
	protected static final int SQL_DELETE = 2;
	
	protected static final String SQL_INSERT_TEMPLATE = "INSERT INTO [qname] ([columns]) VALUES ([values])";
	protected static final String SQL_UPDATE_TEMPLATE = "UPDATE [qname] SET [sets] WHERE [conditions]";
	protected static final String SQL_DELETE_TEMPLATE = "DELETE FROM [qname] WHERE [conditions]";
	
	protected static final String QNAME = "[qname]";
	protected static final String COLUMNS = "[columns]";
	protected static final String VALUES = "[values]";
	protected static final String SETS = "[sets]";
	protected static final String CONDITIONS = "[conditions]";
	
	protected String getTemplate(Object bean, int type) throws SQLException, IllegalArgumentException {
		validate(bean.getClass());
		HashTable t = _tables.get(bean.getClass());
		String result;
		switch(type) {
			case SQL_INSERT: 
				result = SQL_INSERT_TEMPLATE
						.replace(QNAME, t.getQName())
						.replace(COLUMNS, Data.join(t.getColumns(), ", "))
						.replace(VALUES, Data.join(t.getColumns(), ", ", true));
				break;
			case SQL_UPDATE: 
				result = SQL_UPDATE_TEMPLATE
						.replace(QNAME, t.getQName())
						.replace(SETS, Data.exJoin(Data.minus(t.getColumns(), t.getPkColumns()), ", "))
						.replace(CONDITIONS, Data.exJoin(t.getPkColumns(), " AND "));
				break;
			case SQL_DELETE: 
				result = SQL_DELETE_TEMPLATE
						.replace(QNAME, t.getQName())
						.replace(CONDITIONS, Data.exJoin(t.getPkColumns(), " AND "));
				break;
			default: throw new IllegalArgumentException("Invalid statement type specified.");
		}
		Log.logDebug("Generated Template: %s".formatted(result), null, "getTemplate");
		return result;
	}
	
	/**
	 * Gets an early-bound, applied INSERT, UPDATE, or DELETE statement for the given bean.
	 * @param bean
	 * @param type SQL_INSERT, SQL_UPDATE, or SQL_DELETE
	 * @return
	 * @throws SQLException
	 */
	protected String getStatement(Object bean, int type) throws SQLException {
		String statement = applyParameterBean(getTemplate(bean, type), bean);
		Log.logDebug(statement, null, "getStatement");
		return statement;
	}
	
	/**
	 * Gets a late-bound, applied INSERT, UPDATE, or DELETE statement for the given bean.
	 * @param bean
	 * @param type SQL_INSERT, SQL_UPDATE, or SQL_DELETE
	 * @return
	 * @throws SQLException
	 */
	protected JdbcStatement getStatementEx(Object bean, int type) throws SQLException {
		JdbcStatement statement = new JdbcStatement(getTemplate(bean, type));
		applyParameterBean(statement, bean);
		return statement;
	}
	
	/**
	 * Get an INSERT statement for bean.
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public String getInsert(Object bean) throws SQLException { return getStatement(bean, SQL_INSERT); }
	/**
	 * Get an INSERT statement for bean.
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public JdbcStatement getInsertEx(Object bean) throws SQLException { return getStatementEx(bean, SQL_INSERT); }
	/**
	 * Get an UPDATE statement for bean.
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public String getUpdate(Object bean) throws SQLException { return getStatement(bean, SQL_UPDATE); }
	/**
	 * Get an UPDATE statement for bean.
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public JdbcStatement getUpdateEx(Object bean) throws SQLException { return getStatementEx(bean, SQL_UPDATE); }
	/**
	 * Get a DELETE statement for bean.
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public String getDelete(Object bean) throws SQLException { return getStatement(bean, SQL_DELETE); }
	/**
	 * Get a DELETE statement for bean.
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public JdbcStatement getDeleteEx(Object bean) throws SQLException { return getStatementEx(bean, SQL_DELETE); }
	/**
	 * Get an INSERT or UPDATE statement for bean.  The bean object must be a descendant of StateBean.  
	 * The isNew function determines whether to generate an INSERT or UPDATE statement.   
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public String getSave(Object bean) throws SQLException { return getStatement(bean, checkStateBean(bean).isNew() ? SQL_INSERT : SQL_UPDATE); }
	/**
	 * Get an INSERT or UPDATE statement for bean.  The bean object must be a descendant of StateBean.  
	 * The isNew function determines whether to generate an INSERT or UPDATE statement.   
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public JdbcStatement getSaveEx(Object bean) throws SQLException { return getStatementEx(bean, checkStateBean(bean).isNew() ? SQL_INSERT : SQL_UPDATE); }
	
	protected StateBean checkStateBean(Object bean) throws SQLException {
		if (!(bean instanceof StateBean)) 
			throw new SQLException("The object in 'bean' is not an instance of StateBean.");
		return (StateBean)bean;
	}
	
	/**
	 * Inserts an array of records into their defined tables.
	 * @param beans the records to insert
	 * @return an array of the number of rows effected per statement, if applicable
	 * @throws SQLException
	 */
	public int[] insert(Object[] beans) throws SQLException {
		return execBeanStatementArray(beans, SQL_INSERT);
	}
	
	/**
	 * Inserts a collection of records into their defined tables.
	 * @param beans the records to insert
	 * @return an array of the number of rows effected per statement, if applicable
	 * @throws SQLException
	 */
	public int[] insert(Collection<?> beans) throws SQLException {
		return execBeanStatementArray(beans.toArray(), SQL_INSERT);
	}
	
	/**
	 * Inserts a record into the defined table.
	 * @param bean the record to insert
	 * @return the number of rows effected, if applicable
	 * @throws SQLException
	 */
	public int insert(Object bean) throws SQLException {
		return execBeanStatement(bean, SQL_INSERT);
	}
	
	protected void getLogStatements(LoggerBean bean, ArrayList<String> statements) throws SQLException {
		LoggerParams lp = bean.getClass().getAnnotation(LoggerParams.class);
		if ((lp != null) && (bean.getLog() != null)) {
			HashMap<String,LogValue> log = bean.getLog();
			Iterator<String> iterator = log.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String insert = lp.insert();
				insert = applyParameterBean(insert, bean);
				insert = applyParameterBean(insert, log.get(key));
				statements.add(insert);
			}
		}
	}
	
	/**
	 * Inserts an array of records into their defined tables.
	 * @param beans the records to insert
	 * @return an array of the number of rows effected per statement, if applicable
	 * @throws SQLException
	 */
	public int[] update(Object[] beans) throws SQLException {
		return execBeanStatementArray(beans, SQL_UPDATE);
	}
	
	public int[] updateWithMerge(EnhancedBean[] beans) throws SQLException {
		return updateWithMerge(Arrays.asList(beans));
	}
	
	/**
	 * Inserts a collection of records into their defined tables.
	 * @param beans the records to insert
	 * @return an array of the number of rows effected per statement, if applicable
	 * @throws SQLException
	 */
	public int[] update(Collection<?> beans) throws SQLException {
		return execBeanStatementArray(beans.toArray(), SQL_UPDATE);
	}
	
	public int[] updateWithMerge(Collection<EnhancedBean> beans) throws SQLException {
		beans = mergeList(beans.stream().toList());
		return update(beans);
	}
	
	/**
	 * Updates a record in the defined table.
	 * @param bean the record to insert
	 * @return the number of rows effected, if applicable
	 * @throws SQLException
	 */
	public int update(StateBean bean) throws SQLException {
		return execBeanStatement(bean, SQL_UPDATE);
	}
	
	/**
	 * Updates a record in the defined table after performing a merge with the original record.
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public int updateWithMerge(EnhancedBean bean) throws SQLException {
		bean = merge(bean);
		return update(bean);
	}
	
	/**
	 * Merge the bean to be updated with the original record before saving.
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	protected EnhancedBean merge(EnhancedBean bean) throws SQLException {
		String filter = applyParameterBean(generateFilter(bean), bean);
		EnhancedBean original = get(bean.getClass(), filter);
		if (original != null)
			original.merge(bean);
		return original != null ? original : bean;
	}
	
	/**
	 * Generate a filter for use by the merge method.
	 * @param bean
	 * @return
	 */
	protected String generateFilter(EnhancedBean bean) {
		JdbcTable table = bean.getClass().getAnnotation(JdbcTable.class);
		if (table == null)
			throw new IllegalArgumentException(String.format("The supplied class %s does not have the required @JdbcTable annotation.", bean.getClass().getCanonicalName()));
		StringBuilder filter = new StringBuilder("");
		for (String keyColumn : table.primaryKeyColumns())
			filter.append(String.format("%s%s = {%s}", filter.isEmpty() ? "" : " AND ", keyColumn, Data.camelcase(keyColumn)));
		return filter.toString();
	}
	
	/**
	 * Saves an array of beans to their defined tables.
	 * This function requires that the beans implement the StateBean interface.
	 * It checks the beans to see if each record is new.  If so, then it will
	 * execute an insert, if not, then it will execute an update.  
	 * @param beans the records to save
	 * @return an array of the number of rows effected per statement, if applicable
	 * @throws SQLException
	 */
	public int[] save(StateBean[] beans) throws SQLException {
		boolean lob = false;
		for (Object bean : beans)
			if (hasLob(bean)) {	lob = true; break; }
		
		
		Object[] statements = lob ? new JdbcStatement[beans.length] : new String[beans.length];
		for (int i = 0; i < beans.length; i++) {
			int type = beans[i].isNew() ? SQL_INSERT : SQL_UPDATE;
			statements[i] = lob ? getStatementEx(beans[i], type) : getStatement(beans[i], type);
		}
		if (lob)
			return execUpdate((JdbcStatement[])statements);
		else 			
			return execUpdate((String[])statements);
	}
	
	public int[] saveWithMerge(EnhancedBean[] beans) throws SQLException {
		return save(mergeList(Arrays.asList(beans)));
	}
	
	/**
	 * Saves a collection of beans to their defined tables.
	 * This function requires that the beans implement the StateBean interface.
	 * It checks the beans to see if each record is new.  If so, then it will
	 * execute an insert, if not, then it will execute an update.  
	 * @param beans the records to save
	 * @return an array of the number of rows effected per statement, if applicable
	 * @throws SQLException
	 */
	public int[] save(Collection<?> beans) throws SQLException {
		return save(beans.toArray(new StateBean[beans.size()]));
	}
	
	public int[] saveWithMerge(Collection<EnhancedBean> beans) throws SQLException {
		return save(mergeList(beans.stream().toList()));
	}
	
	/**
	 * This function requires that the bean implement the StateBean interface.
	 * It checks the bean to see if the record is new.  If so, then it will
	 * execute an insert, if not, then it will execute an update.  
	 * @param bean the record to save
	 * @return the number of rows effected, if applicable
	 * @throws SQLException
	 */
	public int save(StateBean bean) throws SQLException {
		return checkStateBean(bean).isNew() ? insert(bean) : update(bean);
	}
	
	public int saveWithMerge(EnhancedBean bean) throws SQLException {
		return save(merge(bean));
	}
	
	/**
	 * Deletes an array of records from their defined table(s).
	 * @param beans the records to delete
	 * @return an array of the number of rows effected per statement, if applicable
	 * @throws SQLException
	 */
	public int[] delete(Object[] beans) throws SQLException {
		return execBeanStatementArray(beans, SQL_DELETE);
	}
	
	/**
	 * Deletes a collection of records from their defined table(s).
	 * @param beans the records to delete
	 * @return an array of the number of rows effected per statement, if applicable
	 * @throws SQLException
	 */
	public int[] delete(Collection<?> beans) throws SQLException {
		return execBeanStatementArray(beans.toArray(), SQL_DELETE);
	}
	
	/**
	 * Deletes a record from its defined table.
	 * @param bean the record to delete
	 * @return the number of rows effected, if applicable
	 * @throws SQLException
	 */
	public int delete(Object bean) throws SQLException {
		return execBeanStatement(bean, SQL_DELETE);
	}
	
	private int[] execBeanStatementArray(Object[] beans, int statementType) throws SQLException {
		if (arrayHasLob(beans)) {
			JdbcStatement[] statements = new JdbcStatement[beans.length];
			
			for (int i = 0; i < beans.length; i++)
				statements[i] = getStatementEx(beans[i], statementType);
			
			return execUpdate(statements);
		}
		else {
			String[] statements = new String[beans.length];
			
			for (int i = 0; i < beans.length; i++)
				statements[i] = getStatement(beans[i], statementType);
			
			return execUpdate(statements);
		}
	}
	
	/**
	 * Get the original records for the listed beans.  If there is no original (i.e., it's a new record, 
	 * it will be returned as-is, otherwise the new values will be merged to the original and returned.
	 * The returned list will be ready for executing update or save.
	 * @param beans
	 * @return
	 * @throws SQLException
	 */
	private List<EnhancedBean> mergeList(List<EnhancedBean> beans) throws SQLException {
		String filter = generateListFilter(beans);
		List<EnhancedBean> list = getList(beans.get(0).getClass(), filter);
		Map<String,EnhancedBean> originals = new HashMap<>();
		String template = generateFilter(beans.get(0).getClass());
		if (!Data.isEmpty(list)) {
			for (EnhancedBean bean : list)
				originals.put(applyParameterBean(template, bean), bean);
		}
		List<EnhancedBean> result = new ArrayList<>();
		for (EnhancedBean bean : beans) {
			String check = applyParameterBean(template, bean);
			EnhancedBean original = originals.get(check);
			if (original != null) {
				original.merge(bean);
				result.add(original);
			}
			else
				result.add(bean);
		}
		return result;
	}
	
	/**
	 * Generate a populated filter for a list of beans.  Note that all beans in the list must be the same class.
	 * @param beans
	 * @return
	 * @throws SQLException
	 */
	public String generateListFilter(List<EnhancedBean> beans) throws SQLException {
		if (Data.isEmpty(beans))
			return "1 = 0";
		Class<? extends EnhancedBean> beanClass = beans.get(0).getClass();
		String partFilter = generateFilter(beanClass);
		StringBuilder filter = new StringBuilder("");
		for (EnhancedBean bean : beans) {
			if (bean.getClass() != beanClass)
				throw new SQLException("All beans in the list must be the same class.");
			filter.append(String.format("%s(%s)", filter.isEmpty() ? "" : " OR ", applyParameterBean(partFilter, bean)));
		}
		return filter.toString();
	}
	
	/**
	 * Generate a parameterized filter for use selecting an existing record from the annotated table.
	 * @param beanClass
	 * @return
	 * @throws SQLException
	 */
	public String generateFilter(Class<? extends EnhancedBean> beanClass) throws SQLException {
		JdbcTable table = beanClass.getAnnotation(JdbcTable.class);
		if (table == null)
			throw new SQLException("No @JdbcTable annotation was found in " + beanClass.getCanonicalName() + ".");
		if (Data.isEmpty(table.primaryKeyColumns()))
			throw new SQLException("No primaryKeyColumns wer found in the @JdbcTable annotation on " + beanClass.getCanonicalName() + ".");
		StringBuilder filter = new StringBuilder("");
		for (String column : table.primaryKeyColumns())
			filter.append(String.format("%s%s = {%s}", filter.isEmpty() ? "" : " AND ", column, Data.camelcase(column)));
		return filter.toString();
	}
	
	
	/*
	@SuppressWarnings("rawtypes")
	private int[] execBeanStatementCollection(Collection<?> beans, int statementType) throws SQLException {
		if (collectionHasLob(beans)) {
			JdbcStatement[] statements = new JdbcStatement[beans.size()];
			Iterator iterator = beans.iterator();
			int i = 0;
			
			while (iterator.hasNext())
				statements[i++] = getStatementEx(iterator.next(), statementType); 
			
			return execUpdate(statements);
		}
		else {
			String[] statements = new String[beans.size()];
			Iterator iterator = beans.iterator();
			int i = 0;
			
			while (iterator.hasNext())
				statements[i++] = getStatement(iterator.next(), statementType); 
			
			return execUpdate(statements);
		}
	}
	*/
	
	private int execBeanStatement(Object bean, int statementType) throws SQLException {
		if (hasLob(bean))
			return execUpdate(getStatementEx(bean, statementType));
		else
			return execUpdate(getStatement(bean, statementType));
	}
	
	public void fullLookup(StateBean source) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		Method[] methods = source.getClass().getDeclaredMethods();
		for (Method method : methods) {
			JdbcLookup def = method.getAnnotation(JdbcLookup.class);
			if (def == null)
				continue;
			// Use the method if it's a setter, otherwise infer the setter corresponding to the getter.
			try {
				Method setter = method.getName().startsWith("set") ? method 
							  : method.getName().startsWith("get") ? Data.getDeclaredMethod(source.getClass(), String.format("s%s", method.getName().substring(1)))
							  : null;
				if (setter != null)
					setter.invoke(source, lookup(def, source, setter.getParameterTypes()[0]));
			}
			catch (NoSuchMethodException e) {
				Log.logException(e, null);
			}
			// Use def.keyField(), or if null, the camel-case version of def.key() to reference the setter.
			// Use the setter's parameter type as the returnType parameter in the lookup method.
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T lookup(JdbcLookup def, StateBean source, Class<T> returnType) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		Method getter = Data.getDeclaredMethod(source.getClass(), Data.getGetterName(def.valueField()));
		String statement = applyParameters(String.format("SELECT %s FROM %s WHERE %s = {1}", def.key(), def.table(), def.value()), getter.invoke(source));
		return (T) execQuery(statement, returnType);
	}
}
