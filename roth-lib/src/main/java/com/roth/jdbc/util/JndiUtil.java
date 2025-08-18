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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

/**
 * 
 * @author James M. Payne
 *
 */
public class JndiUtil {
	/**
	 * Gets a list of jndiNames that have been filtered to include only DataSource or ResourceLinkRef objects.
	 * @return
	 */
	public static ArrayList<String> getJndiNames() {
		try {
			ArrayList<String> jndiNames = new ArrayList<String>();
			InitialContext ctx = new InitialContext();
			
			// Get list of jndiNames
			NamingEnumeration<NameClassPair> list = ctx.list("");
			
			// If none found on path "", then try "java:/".
			if (!list.hasMore()) 
				list = ctx.list("java:/");
			
			// If the first item is "comp", or if none found, then try "java:/comp/env/".
			NameClassPair first = list.hasMore() ? list.next() : null;
			if (first == null || first.getName().equals("comp")) 
				list = ctx.list("java:/comp/env/");
			else
				addIfDataSource(first, jndiNames);
			
			while (list.hasMore()) {
				NameClassPair ncp = list.next();
				addIfDataSource(ncp, jndiNames);
			}
			
			Collections.sort(jndiNames);
			return jndiNames;
		}
		catch (Exception e) {
			Log.logException(e, null);
			return null;
		}
	}
	
	/**
	 * Gets a list of jndiNames in a map (key and value are the same).
	 * @return
	 * @see getJndiNames()
	 */
	public static Map<String,String> getJndiNameMap() {
		return Data.stringsToMap(getJndiNames());
	}
	
	protected static void addIfDataSource(NameClassPair jndiEntry, ArrayList<String> list) {
		// It will be javax.sql.DataSource if defined in context.xml, or org.apache.naming.ResourceLinkRef if defined in server.xml and linked in context.xml.
		if (jndiEntry.getClassName().contains("DataSource") || jndiEntry.getClassName().contains("ResourceLinkRef"))
			list.add(jndiEntry.getName());
	}
	
	/**
	 * Get the database name for a given jndiName.
	 * @param jndiName
	 * @return
	 */
	public static String getDbName(String jndiName) throws NamingException {
		Connection conn;
		String result = null;
		try {
			DataSource src = JdbcUtil.getDataSource(jndiName);
			conn = (Connection)src.getConnection();
			if (!conn.isValid(2)) {
				conn.close();
				conn = (Connection)src.getConnection();
			}
			result = conn.getMetaData().getDatabaseProductName();
			conn.close();
		}
		catch (NamingException | SQLException e) {
			Log.logError("Unable to establish connection to " + jndiName + ".", null, e);
		}
		return result;
	}
}
