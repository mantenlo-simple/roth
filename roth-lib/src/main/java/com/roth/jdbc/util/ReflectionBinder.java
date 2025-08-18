package com.roth.jdbc.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.LoggerBean;

public class ReflectionBinder implements Binder{

	private class MethodCache implements Serializable {
		private static final long serialVersionUID = 4994552497590892403L;
		
		private Method _method;
		private Class<?> _clazz;
		
		public Method getMethod() { return _method; }
		public void setMethod(Method method) { _method = method; }
		
		public Class<?> getClazz() { return _clazz; }
		public void setClazz(Class<?> clazz) { _clazz = clazz; }
	}
	
	private static HashMap<Class<?>, ArrayList<MethodCache>> mc;
	
	private ResultSetMetaData metaData;
	private int recordCount;
	private boolean suppressBinary;
	
	public ReflectionBinder(ResultSetMetaData metaData) {
		if (mc == null)
			mc = new HashMap<>();
		this.metaData = metaData;
		recordCount = 0;
	}
	
	public boolean getSuppressBinary() { return suppressBinary; }
	public void setSuppressBinary(boolean suppressBinary) { this.suppressBinary = suppressBinary; }
	
	public void setRecordCount(int recordCount) { this.recordCount = recordCount; }
	
	@Override
	public void bindObject(Object object, ResultSet resultSet) throws SQLException {
		String canonicalName = object.getClass().getCanonicalName();
		ArrayList<MethodCache> amc = mc.get(object.getClass());
		
		if (amc == null) {
			amc = new ArrayList<>();
			mc.put(object.getClass(), amc);
		}			
		
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			// If the element object is not atomic (i.e. it's a POJO),
			// process the row using reflection.
			String columnLabel = metaData.getColumnLabel(i).toLowerCase();
			String methodName = "set" + columnLabel.substring(0, 1).toUpperCase() + columnLabel.substring(1);
			MethodCache c = null;
			
			// If a method cache already exists for this column, then use it.
			// Otherwise, set up a method cache for the column.
			if (amc.size() >= i)
				c = amc.get(i - 1); 
			else {
				c = new MethodCache();
				amc.add(c);
				
				try {
					// First check for a setter based on the column name 
					// returned by the database.  This would generally be
					// an underscored name like "site_code".  You can optimize
					// the return of data slightly by specifying a camel-case
					// alias.
					c.setMethod(Data.getDeclaredMethod(object.getClass(), methodName));
					c.getMethod().setAccessible(true);
					c.setClazz(c.getMethod().getParameterTypes()[0]);
				}
				catch (NoSuchMethodException e) {
					try { 
						// If the first check failed, we assume that the column
						// is underscored, while the setter is camel-cased.  Change
						// the label to camel-case and try again.
						String ccLabel = columnLabel.replaceAll("_", "");
						methodName = "set" + ccLabel.substring(0, 1).toUpperCase() + ccLabel.substring(1);
						
						c.setMethod(Data.getDeclaredMethod(object.getClass(), methodName));
						c.getMethod().setAccessible(true);
						c.setClazz(c.getMethod().getParameterTypes()[0]);
					}
					// If no setter is found, then throw an exception.
					catch (NoSuchMethodException e2) {
						String message = "No field setter was found for column label '" + columnLabel + "' in class '" + canonicalName + "'.";
						PermissiveBinding pb = this.getClass().getAnnotation(PermissiveBinding.class);
						if (pb == null)
							pb = (PermissiveBinding) object.getClass().getAnnotation(PermissiveBinding.class);
						if (pb != null) {
							c.setMethod(null);
							if (!pb.suppressWarnings() && (recordCount < 2)) 
								Log.logWarning(message, null);
						}
						else {
							throw new SQLException(message);
						}
					}
				}
			}
			
			// This checks the existence only for good practice.
			// In reality, this should never be null.  However, since
			// "should" isn't a guarantee...
			// Update: Things change.  This can be hit if @PermissiveBinding is used.
			if (c.getMethod() != null) {
				try {
					Object[] o = new Object[1];
					o[0] = getDataSourceColumn(c.getClazz(), resultSet, i, suppressBinary);
					c.getMethod().setAccessible(true);
					if (object instanceof LoggerBean) ((LoggerBean)object).setSuppressLogging(true);
					c.getMethod().invoke(object, o);
					if (object instanceof LoggerBean) ((LoggerBean)object).setSuppressLogging(false);
				}
				catch (InvocationTargetException e) {
					throw new SQLException(e.getMessage());
				}
				catch (IllegalAccessException e) {
					throw new SQLException(e.getMessage());
				}
				catch (IllegalArgumentException e) {
					throw new SQLException(e.getMessage());
				}
			}
		}
	}
	
	private static Object getDataSourceColumn(Class<?> clazz, ResultSet rs, int columnIndex, boolean suppressBinary) throws SQLException {
		String cName = clazz.getCanonicalName().replaceAll("\\[\\]", "");
		try {
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
			     : (clazz.isArray() && cName.equals("byte")) ? (suppressBinary ? null : rs.getBytes(columnIndex))
			     : (clazz.isEnum()) ? clazz.getMethod("valueOf", String.class).invoke(null, rs.getString(columnIndex))
			     : rs.getObject(columnIndex, clazz);
		}
		catch (SQLException e) {
			Log.logError(clazz.getCanonicalName() + " | " + columnIndex + " | " + suppressBinary, null, e);
			throw e;
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			Log.logError(clazz.getCanonicalName() + " | " + columnIndex + " | " + suppressBinary, null, e);
			throw new SQLException(e.getMessage());
		}
	}
}
