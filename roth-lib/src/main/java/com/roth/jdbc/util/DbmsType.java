package com.roth.jdbc.util;

import com.roth.base.util.Data;

public enum DbmsType {
	DB2 (new String[] { "DB2", "DB2/AIX64" }),
	INFORMIX (new String[] { "Informix", "Informix Dynamic Server", "IDS/UNIX32" }),
	MYSQL (new String[] { "MySQL", "MariaDB" }),
	ORACLE (new String[] { "Oracle" }),
	POSTGRESQL (new String[] { "PostgreSQL" }),
	SQL_SERVER (new String[] { "Microsoft SQL Server" });
	
	private String[] dbName;
	
	private DbmsType(String[] dbName) {
		this.dbName = dbName;
	}
	
	public String[] getDbmsName() { 
		return dbName; 
	}
	
	public static DbmsType valueOfDbName(String dbName) {
		for (DbmsType type : DbmsType.values())
			if (Data.in(dbName, type.dbName))
				return type;
		return null;
	}
}
