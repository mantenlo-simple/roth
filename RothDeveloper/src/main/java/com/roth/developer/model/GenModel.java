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
package com.roth.developer.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.roth.base.util.Data;
import com.roth.base.util.GenericComparator;


public class GenModel {

	public static class TypeDef {
		private String _typeImport;
		private String _typeName;
		private boolean _trim;
		
		public TypeDef(String typeImport, String typeName) {
			_typeImport = typeImport;
			_typeName = typeName;
			_trim = false;
		}
		
		public TypeDef(String typeImport, String typeName, boolean trim) {
			_typeImport = typeImport;
			_typeName = typeName;
			_trim = trim;
		}
		
		public String getTypeImport() { return _typeImport; }
		public String getTypeName() { return _typeName; }
		public boolean getTrim() { return _trim; }
	}
	
	public static class ColMetaData {
		private String _label;
		private String _name;
		private TypeDef _type;
		
		public ColMetaData(String label, String name, TypeDef type) {
			_label = label;
			_name = name;
			_type = type;
		}
		
		public String getLabel() { return _label; }
		public void setLabel(String label) { _label = label; }
		
		public String getName() { return _name; }
		public void setName(String name) { _name = name; }
		
		public TypeDef getType() { return _type; }
		public void setType(TypeDef type) { _type = type; }
	}
	
	@SuppressWarnings("unchecked")
	public static String ParsePost(SqlPost post) throws Exception {
		String schema = (post.getSchemaName() == null)
		              ? "" : "schema = \"" + post.getSchemaName() + "\", ";
		String primaryKey = (post.getPrimaryKey() == null)
		                  ? "" : ",\n           primaryKeyColumns = {" + post.getPKCs() + "}";
		
		String imports = (post.getLogChanges()) ? "import com.roth.jdbc.model.LoggerBean;\n\n" 
				       : "import java.io.Serializable;\n\n";
		String annotations = (post.getTableName() == null) 
		                   ? ""
		                   : "@JdbcTable(" + schema + "name = \"" + post.getTableName() + "\"" + primaryKey + ")\n";
		String fields = "";
		String methods = "";
		String assign = "";
		
		String stateBean = "";
		
		if (post.getUsePermissiveBinding()) {
			imports += "import com.roth.jdbc.annotation.PermissiveBinding;\n";
			annotations += "@PermissiveBinding()\n";
		}
		if (post.getSelfLogInsert() != null) {
			imports += "import com.roth.jdbc.annotation.LoggerParams;\n";
			annotations += "@LoggerParams(insert = \"" + post.getSelfLogInsert() + "\")\n";
		}
		
		if ((post.getTableName() != null) && (post.getPrimaryKey() != null)) {
			imports += "import com.roth.jdbc.annotation.JdbcTable;\n" +
					   "import com.roth.jdbc.model.StateBean;\n\n";
			stateBean = (post.getLogChanges()) ? " implements StateBean" : ", StateBean";
		}
		else if (post.getTableName() != null)
			imports += "import com.roth.jdbc.annotation.JdbcTable;\n\n";
		
		if (post.getDriver() != null) Class.forName(post.getDriver());
		Connection conn = null;
		String jndiName = post.getJndiName();
		
		if (!Data.isEmpty(post.getJndiName())) {
			String name = jndiName;
			try {
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
		
				conn = (Connection)src.getConnection();
				conn.setAutoCommit(false);
			}
			catch (NamingException e) {
				throw new SQLException("Unable to establish connection to " + jndiName + ". [" + name + "]"); 
			}
		}
		else
			conn = (Data.isEmpty(post.getUsername())) 
		         ? DriverManager.getConnection(post.getUrl())
			     : DriverManager.getConnection(post.getUrl(), post.getUsername(), post.getPassword());
		conn.setAutoCommit(false);
		
		PreparedStatement s = null;
		ResultSet rs = null;
		boolean hasTrim = false;
		
		try {
			String table = post.getTableName();
			if ((table != null) && (post.getSchemaName() != null))
				table = post.getSchemaName() + "." + table;
			String statement = (table != null)
			                 ? "SELECT * FROM " + table + " WHERE 1 = 0"
			                 : post.getStatement();
			s = conn.prepareStatement(statement);
			rs = s.executeQuery();
			ResultSetMetaData m = s.getMetaData();
			ArrayList<ColMetaData> cols = new ArrayList<ColMetaData>();
			
			for (int i = 1; i <= m.getColumnCount(); i++)
				cols.add(new ColMetaData(m.getColumnLabel(i), camelcase(m.getColumnLabel(i).toLowerCase()), getType(m.getColumnType(i), post)));
			
			if (post.getSortColumns()) {
				GenericComparator comp = new GenericComparator("name", 1);
				Collections.sort(cols, comp);
			}
			
			for (int i = 0; i < cols.size(); i++) {
				TypeDef type = cols.get(i).getType(); //getType(m.getColumnType(i), post);
				if (type.getTrim())
					hasTrim = true;
				String name = cols.get(i).getName(); //camelcase(m.getColumnLabel(i).toLowerCase());
				String trim = type.getTrim() ? "Data.trim(" + name + ")" : name;
				String logger = post.getLogChanges() ? " if (different(\"" + cols.get(i).getLabel() + "\", this." + name + ", " + trim + "))" : "";
				imports += ((type.getTypeImport() == null) || (imports.indexOf(type.getTypeImport()) > -1)) 
						? "" : "import " + type.getTypeImport() + ";\n";
				fields += "    private " + type.getTypeName() + " " + name + ";\n";
				if (post.getSelfLogInsert() == null) {
					methods += "    public " + type.getTypeName() + " get" + upperFirst(name) + "() { return " + name + "; }\n"
					         + "    public void set" + upperFirst(name) + "(" + type.getTypeName() + " " + name + ") {" + logger + " this." + name + " = " + trim + "; }\n"
					         + "\n";
				}
				else {
					methods += "    public " + type.getTypeName() + " get" + upperFirst(name) + "() { return (" + type.getTypeName() + ")getObject(\"" + cols.get(i).getLabel() + "\"); }\n"
					         + "    public void set" + upperFirst(name) + "(" + type.getTypeName() + " " + name + ") { " + (post.getLogChanges() ? "logS" : "s") + "etObject(\"" + cols.get(i).getLabel() + "\", " + trim + "); }\n"
					         + "\n";
				}
				assign += "        set" + upperFirst(name) + "(source.get" + upperFirst(name) + "());\n";
			}
			
			if (hasTrim)
				imports = "import com.roth.base.util.Data;\n\n" + imports;
		}
		catch (SQLException e) {
			e.printStackTrace();
			conn.rollback();
			throw e;
		}
		catch (Exception e) { e.printStackTrace(); }
		finally {
			try { if (rs != null) rs.close(); if (s != null) s.close(); }
			catch (SQLException e) { e.printStackTrace(); }
		
			try { if (!conn.isClosed()) conn.close(); }
			catch (SQLException e) { conn.close(); throw e; }
		}
		
		if (!stateBean.isEmpty()) {
			methods += "    @Override\n" +
	                   "    public boolean isNew() {\n" +
	                   "        boolean result = " + post.getPKnull() + ";\n" +
	                   "        // TODO: Add update code to run every time.\n" +
	                   "        // Example:\n" +
	                   "        //   updatedDts = LocalDateTime.now();\n" +
	                   "        if (result) {\n" +
	                   "            // TODO: Add initialization code to run when new.\n" +
	                   "            // Example:\n" +
	                   "            //   createdBy = updatedBy;\n" +
	                   "            //   createdDts = updatedDts;\n" +
	                   "        }\n" +
	                   "        return result;\n" +
	                   "    }\n";
		}
		
		String logger = (post.getLogChanges()) ? " extends LoggerBean" : " implements Serializable";
		
		return "package " + post.getPackageName() + ";\n" +
		       "\n" +
		       imports + 
		       "\n" +
		       annotations +
		       "public class " + post.getClassName() + logger  + stateBean + " {\n" +
		       "    private static final long serialVersionUID = 1L;\n" +
		       (post.getSelfLogInsert() != null ? "" : "\n" + fields) + 
		       "\n" + 
		       methods +
		       (!post.getImplementAssign() ? "" : "\n    public void assign(" + post.getClassName() + " source) {\n" + assign + "    }\n") +
		       "}";
	}
	
	private static String upperFirst(String source) {
		return source.length() == 0 ? source : source.substring(0, 1).toUpperCase() + source.substring(1);
	}
	
	//private static String lowerFirst(String source) {
	//	return source.substring(0, 1).toLowerCase() + source.substring(1);
	//}
	
	public static String camelcase(String source) {
		String result = "";
		int last = 0;
		String seg;
		
		for (int i = 0; i < source.length(); i++)
			if (source.charAt(i) == '_') {
				seg = source.substring(last, i);
				result += (result.length() == 0) ? seg : upperFirst(seg);
				last = i + 1;
			}
			else if (source.charAt(i) == '$') {
				seg = source.substring(last, i);
				result += ((result.length() == 0) ? seg : upperFirst(seg)) + '$';
				last = i + 1;
			}
		
		seg = source.substring(last);
		result += (result.length() == 0) ? seg : upperFirst(seg);
		return result;
	}
	
	private static TypeDef getType(int sqlType, SqlPost post) {
		return (sqlType == Types.BOOLEAN ||
		        sqlType == Types.BIT) ? new TypeDef(null, "Boolean")
		     : (sqlType == Types.BLOB ||
		        sqlType == Types.BINARY ||
		        sqlType == Types.VARBINARY ||
		        sqlType == Types.LONGVARBINARY) ? new TypeDef(null, "byte[]")		 
		     : (sqlType == Types.TINYINT) ? new TypeDef(null, "Short")
		     : (sqlType == Types.DOUBLE) ? new TypeDef(null, "Double")
		     : (sqlType == Types.FLOAT) ? new TypeDef(null, "Double")
		     : (sqlType == Types.REAL) ? new TypeDef(null, "Float")
		     : (sqlType == Types.INTEGER) ? new TypeDef(null, "Integer")
		     : (sqlType == Types.BIGINT) ? new TypeDef(null, "Long")
		     : (sqlType == Types.SMALLINT) ? new TypeDef(null, "Short")
		     : (sqlType == Types.VARCHAR) ? new TypeDef(null, "String")
		     : (sqlType == Types.CHAR) ? new TypeDef(null, "String", post.getTrimChar())
		     : (sqlType == Types.DECIMAL) ? new TypeDef("java.math.BigDecimal", "BigDecimal")
		     : (sqlType == Types.NUMERIC) ? new TypeDef("java.math.BigDecimal", "BigDecimal")
		     : (sqlType == Types.DATE) ? new TypeDef("java.time.LocalDate", "LocalDate")
		     : (sqlType == Types.TIME) ? new TypeDef("java.time.LocalTime", "LocalTime")
		     : (sqlType == Types.TIME_WITH_TIMEZONE) ? new TypeDef("java.time.ZonedDateTime", "ZonedDateTime")
		     : (sqlType == Types.TIMESTAMP) ? new TypeDef("java.time.LocalDateTime", "LocalDateTime")
    		 : (sqlType == Types.TIMESTAMP_WITH_TIMEZONE) ? new TypeDef("java.time.ZonedDateTime", "ZonedDateTime")
		     : new TypeDef("java.lang.String", "String");
	}
}
