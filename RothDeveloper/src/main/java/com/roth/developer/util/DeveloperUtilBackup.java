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
package com.roth.developer.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.base.util.LengthComparator;
import com.roth.developer.model.ColumnInfoBean;
import com.roth.developer.model.IndexInfoBean;
import com.roth.developer.model.JndiSettingBean;
import com.roth.developer.model.TableInfoBean;
import com.roth.developer.model.TriggerInfoBean;
import com.roth.jdbc.util.DbmsType;
import com.roth.jdbc.util.JdbcUtil;
import com.roth.jdbc.util.JndiUtil;
import com.roth.portal.model.RoleProperty;

public class DeveloperUtilBackup extends JdbcUtil {
	private static final long serialVersionUID = 2182944833141062013L;

	public DeveloperUtilBackup(String jndiname) throws SQLException {
		super(jndiname);
	}
	
	protected int digits(int number) {
		int d = 1;
		int n = number;
		while (n >= 10) {
			n /= 10;
			d++;
		}
		return d;
	}
	
	private static final Integer[] DATE_TYPES = {Types.DATE, Types.TIME, Types.TIMESTAMP};
	
	public void execQuery(String statement, Integer limit, Integer maxStrLen, PrintWriter writer) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement s = null;
        ResultSet rs = null;
        //String result = "";
        String header = "";
        int lim = (limit == null) ? 500 : limit;
        int maxlen = (maxStrLen == null) ? 50 : maxStrLen;
        int riSize = digits(lim); 
        if (riSize < 3) riSize = 3;
        if (lim == 0) riSize = 10;
        Date start = null;
        Date result = null;
        Date end = null;
        boolean explainPlan = false;
        boolean oracle = getDbName().equals("Oracle");
        boolean mysql = getDbName().equals("MySQL");
                
        try {
        	s = conn.prepareStatement(statement);
        	start = new Date();
        	if (statement.startsWith("EXPLAIN")) {
        		explainPlan = true;
        		if (oracle) {
        			s.execute();
        			rs = s.executeQuery("SELECT plan_table_output FROM TABLE(dbms_xplan.display())");
        		}
        		else if (mysql)
        			rs = s.executeQuery();
        		else
        			throw new SQLException("Explain plans can only be run on an Oracle or MySQL database.");
        	}
        	else
        		rs = s.executeQuery();
            result = new Date();
            ResultSetMetaData m = s.getMetaData();
            if (m == null) m = rs.getMetaData();
            int columnCount = m.getColumnCount();
            int rowCount = 0;
            int columnSizes[] = new int[columnCount];
            // Header
            header = String.format("%1$" + riSize + "s", "ROW") + " | ";
            for (int i = 0; i < columnCount; i++) {
            	boolean dateField = Data.in(m.getColumnType(i + 1), DATE_TYPES);
            	columnSizes[i] = dateField ? 25 : m.getColumnDisplaySize(i + 1);
            	if (!(explainPlan && oracle) && !dateField && columnSizes[i] > maxlen) columnSizes[i] = maxlen;
            	String label = m.getColumnLabel(i + 1);
            	if (columnSizes[i] < label.length()) columnSizes[i] = label.length();
                header += String.format("%1$-" + columnSizes[i] + "s", label) + "  ";
            }
            header += "\n";
            for (int x = 0; x < riSize; x++) header += "-";
            header += " + ";
            for (int i = 0; i < columnCount; i++) {
            	for (int x = 0; x < columnSizes[i]; x++) header += "-"; 
            	header += "  ";
            }
            header += "\n";
            
            writer.println(statement + "\n");
            if (!(explainPlan && oracle))
            	writer.print(header);
            
            // Rows
            while (rs.next()) {
            	if ((rowCount > 0) && (rowCount % 40 == 0) && !(explainPlan && oracle))
            		writer.print("\n" + header);
            	rowCount++;
            	String row = "";
            	if (!(explainPlan && oracle))
            		row +=String.format("%1$" + riSize + "s", rowCount) + " | ";
                // If a limit exists, and has been reached, then stop processing the result set
                if ((lim > 0) && (rowCount > lim))
                    break;
                for (int i = 0; i < columnCount; i++) {
                	String value = rs.getString(i + 1);
                	int lf = (value == null) ? -1 : value.indexOf("\n");
                	if (lf > -1) value = value.substring(0, lf) + "...";
                	if ((value != null) && (value.length() > columnSizes[i])) value = value.substring(0, columnSizes[i]);
                	row += String.format("%1$-" + columnSizes[i] + "s", value) + "  ";
                }
                row += "\n";
                writer.print(row);
            }
            
            end = new Date();
            if ((lim > 0) && (rowCount > lim)) rowCount = lim;
            
            writer.print(explainPlan ? "\n" : "\n" + rowCount + " rows fetched.  ");
            if ((lim > 0) && (rowCount == lim)) writer.print("Fetch terminated by limit.  ");
            writer.print("Exec time: " + (result.getTime() - start.getTime()) + " ms, Proc time: " + (end.getTime() - result.getTime()) + " ms");
        }
        catch (SQLException e) {
        	//Log.logException(e, null);
            if (!isManaged()) conn.rollback();
            throw e;
        }
        catch (Exception e) { 
        	//Log.logException(e, null);
        	throw e;
        }
        finally {
            try { if (rs != null) rs.close(); }
            catch (SQLException e) { Log.logException(e,null); }
            try { if (s != null) s.close(); }
            catch (SQLException e) { Log.logException(e,null); }
            closeConnection();
        }
    }
	
	public int execUpdate(String statement) throws SQLException {
		return super.execUpdate(statement);
	}
	
	public String getDSIStatement(DbmsType dbmsName, String infoType) throws SQLException {
		String statement = "SELECT info_statement " +
				             "FROM datasource_info " +
				            "WHERE dbms_name = {1} " +
				              "AND info_type = {2}";
		statement = applyParameters(statement, dbmsName, infoType);
		Log.logDebug(statement, null);
		return execQuery(statement, String.class);
	}
	
	public ArrayList<String> getSchemas() throws SQLException {
		String statement = new DeveloperUtilBackup("roth").getDSIStatement(getDbName(), "S");
		if (statement == null)
			throw new SQLException("No schema info statement is defined for this database name (" + getDbName() + ").");
		Log.logDebug(statement, null, "getSchemas");
		return execQuery(statement, ArrayList.class, String.class);
	}
	
	public ArrayList<TableInfoBean> getTables(String schema) throws SQLException {
		String statement = new DeveloperUtilBackup("roth").getDSIStatement(getDbName(), "L");
		if (statement == null)
			throw new SQLException("No table info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, schema);
		Log.logDebug(statement, null, "getTables");
		return execQuery(statement, ArrayList.class, TableInfoBean.class);
	}
	
	public ArrayList<ColumnInfoBean> getColumns(String tableId) throws SQLException {
		String statement = new DeveloperUtilBackup("roth").getDSIStatement(getDbName(), "C");
		if (statement == null)
			throw new SQLException("No column info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, tableId);
		Log.logDebug(statement, null, "getColumns");
		return execQuery(statement, ArrayList.class, ColumnInfoBean.class);
	}
	
	public ArrayList<IndexInfoBean> getIndexes(String tableId) throws SQLException {
		String statement = new DeveloperUtilBackup("roth").getDSIStatement(getDbName(), "I");
		if (statement == null)
			throw new SQLException("No index info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, tableId);
		Log.logDebug(statement, null, "getIndexes");
		return execQuery(statement, ArrayList.class, IndexInfoBean.class);
	}
	
	public ArrayList<TriggerInfoBean> getTriggers(String tableId) throws SQLException {
		String statement = new DeveloperUtilBackup("roth").getDSIStatement(getDbName(), "T");
		if (statement == null)
			throw new SQLException("No trigger info statement is defined for this database name (" + getDbName() + ").");
		statement = applyParameters(statement, tableId);
		Log.logDebug(statement, null, "getTriggers");
		return execQuery(statement, ArrayList.class, TriggerInfoBean.class);
	}
	
	public String evalEnv(String source) {
		String result = source;
		int p = result.indexOf("[env:");
		while (p > -1) {
			String name = result.substring(p + 5, result.indexOf("]", p));
			result = result.replaceAll("\\[env:" + name + "\\]", System.getenv(name.trim()));
			p = result.indexOf("[env:");
		}
		return result;
	}
	
	public String evalRpv(String source, String userid) throws SQLException {
		String result = source;
		int p = result.indexOf("[rpv:");
		while (p > -1) {
			String name = result.substring(p + 5, result.indexOf("]", p));
			result = result.replaceAll("\\[rpv:" + name + "\\]", RoleProperty.getAggregateRoleProperty(userid, name.trim()));
			p = result.indexOf("[rpv:");
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public String evalParams(String source, String params) throws SQLException {
		if (Data.isEmpty(params))
			return source;
		String result = source;
		String[] ps = params.split(",");
		ArrayList<String> pal = new ArrayList<String>();
		LinkedHashMap<String,String> pam = new LinkedHashMap<String,String>();
		for (String pi : ps) {
			String[] p = pi.split("\\|");
			pal.add(p[0]);
			pam.put(p[0], p[1] + "," + (p.length > 2 ? p[2] : ""));
		}
		pal.sort(new LengthComparator(null, -1));
		for (String p : pal) {
			String[] pv = pam.get(p).split(",");
			String replacement = pv[0].equals("DATE") ? Data.obj2SQLStr(getDbName(), Data.strToDate(pv[1])) :
				                 pv[0].equals("DATETIME") ? Data.obj2SQLStr(getDbName(), Data.strToDts(pv[1])) :
				                 pv[0].equals("NUMBER") ? Data.obj2SQLStr(getDbName(), Data.strToDouble(pv[1])) :
				                 Data.obj2SQLStr(getDbName(), pv.length > 1 ? pv[1] : null);
			result = result.replaceAll("\\:" + p, replacement);
		}
		Log.logDebug("AdhocUtil.evalParams result: " + result, null);
		return result;
	}
	
	public ArrayList<JndiSettingBean> getJndiNameSettings() throws SQLException {
		// Get list of jndi names
		ArrayList<String> jndiNames = JndiUtil.getJndiNames();
		// Convert to SQL list
		String[] names = jndiNames.toArray(new String[jndiNames.size()]);
		String list = "'" + Data.join(names, "', '") + "'";
		// Remove jndi_setting entries that no longer apply, and insert any new jndi names with default settings
		String[] statements = {
			applyParameters("DELETE FROM jndi_setting WHERE jndi_name NOT IN ({sql: 1})", list), 
			applyParameters("INSERT INTO jndi_setting (jndi_setting_id, jndi_name, updated_by, updated_dts) " +
				            "SELECT 0, jndi_name, {1}, SYSDATE() FROM ({sql: 2}) x " +
				            "WHERE NOT EXISTS (SELECT 1 FROM jndi_setting WHERE jndi_name = x.jndi_name)", 
				            "SYSTEM", "SELECT " + list.replaceAll(", ", " AS jndi_name UNION ALL SELECT ") + " AS jndi_name")
		};
		Log.logDebug(statements[0], null, "getJndiNameSettings");
		Log.logDebug(statements[1], null, "getJndiNameSettings");
		execUpdate(statements);
		// Select all settings and order by jndi_name
		String statement = "SELECT * FROM jndi_setting WHERE jndi_name IN ({sql: 1}) ORDER BY jndi_name";
		statement = applyParameters(statement, list);
		Log.logDebug(statement, null, "getJndiNameSettings");
		return execQuery(statement, ArrayList.class, JndiSettingBean.class);
	}
}
