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

import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class SqlPost implements Serializable {
	private static final long serialVersionUID = -1292745574221161440L;

	private String driver;
	private String url;
	private String username;
	private String password;
	
	private String jndiName;
	
	private String statement;
	
	private String schemaName;
	private String tableName;
	private String primaryKey;
	private String selfLogInsert;
	
	private String packageName;
	private String className;
	
	private boolean useDate;
	private boolean useBigDecimal;
	private boolean sortColumns;
	private boolean trimChar;
	private boolean logChanges;
	private boolean usePermissiveBinding;
	private boolean implementAssign;
	
	private boolean wholeSchema;
	
	public String getDriver() { return driver; }
	public void setDriver(String driver) { this.driver = driver; }
	
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }
	
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	
	public String getJndiName() { return jndiName; }
	public void setJndiName(String jndiName) { this.jndiName = jndiName; }
	
	public String getStatement() { return statement; }
	public void setStatement(String statement) { this.statement = statement == null ? null : URLDecoder.decode(statement, Charset.forName("UTF-8")); }
	
	public String getSchemaName() { return schemaName; }
	public void setSchemaName(String schemaName) { this.schemaName = schemaName == null ? null : schemaName.toLowerCase(); }
	
	public String getTableName() { return tableName; }
	public void setTableName(String tableName) { this.tableName = tableName == null ? null : tableName.toLowerCase(); }
	
	public String getPrimaryKey() { return primaryKey; }
	public void setPrimaryKey(String primaryKey) { this.primaryKey = primaryKey == null ? null : primaryKey.toLowerCase(); }
	public String getPKCs() {
		String[] pkc = primaryKey.split(" ");
		String result = "";
		for (int i = 0; i < pkc.length; i++)
			result += ((i > 0) ? ", " : "") + "\"" + pkc[i] + "\"";
	    return result;
	}
	public String getPKnull() {
		String[] pk = primaryKey.split(" ");
		String result = "";
		for (int i = 0; i < pk.length; i++)
			result += ((i > 0) ? " || " : "") + GenModel.camelcase(pk[i]) + " == null";
		return result;
	}
	
	public String getSelfLogInsert() { return selfLogInsert; }
	public void setSelfLogInsert(String selfLogInsert) { this.selfLogInsert = selfLogInsert; }
	
	public String getPackageName() { return packageName; }
	public void setPackageName(String packageName) { this.packageName = packageName; }
	
	public String getClassName() { return className; }
	public void setClassName(String className) { this.className = className; }
	
	public boolean getUseDate() { return useDate; }
	public void setUseDate(boolean useDate) { this.useDate = useDate; }
	
	public boolean getUseBigDecimal() { return useBigDecimal; }
	public void setUseBigDecimal(boolean useBigDecimal) { this.useBigDecimal = useBigDecimal; }
	
	public boolean getSortColumns() { return sortColumns; }
	public void setSortColumns(boolean sortColumns) { this.sortColumns = sortColumns; }
	
	public boolean getTrimChar() { return trimChar; }
	public void setTrimChar(boolean trimChar) { this.trimChar = trimChar; }
	
	public boolean getLogChanges() { return logChanges; }
	public void setLogChanges(boolean logChanges) { this.logChanges = logChanges; }
	
	public boolean getUsePermissiveBinding() { return usePermissiveBinding; }
	public void setUsePermissiveBinding(boolean usePermissiveBinding) { this.usePermissiveBinding = usePermissiveBinding; }
	
	public boolean getImplementAssign() { return implementAssign; }
	public void setImplementAssign(boolean implementAssign) { this.implementAssign = implementAssign; }

	public boolean getWholeSchema() { return wholeSchema; }
	public void setWholeSchema(boolean wholeSchema) { this.wholeSchema = wholeSchema; }
}
