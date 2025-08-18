package com.roth.schedule;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.util.DbmsType;
import com.roth.jdbc.util.JdbcUtil;

public class SqlCallable implements ScheduleCallable {
	public static final int QUERY = 0;
	public static final int UPDATE = 1;
	public static final int PROCEDURE = 2;
	
	private static final Integer[] DATE_TYPES = {Types.DATE, Types.TIME, Types.TIMESTAMP};
	private static final Integer[] LOB_TYPES = {Types.BLOB, Types.CLOB, Types.NCLOB};
	
	private String jndiName;
	private String fileName;
	private String statement;
	private String format;
	private int type; 
	
	private class CallableUtil extends JdbcUtil {
		private static final long serialVersionUID = 1L;
		public CallableUtil(String jndiName) throws SQLException { super(jndiName); }
		
		public void execUpdate(String[] statements, PrintWriter writer) throws SQLException {
			int[] result = super.execUpdate(statements);
			String output = Arrays.stream(result).mapToObj(Integer::toString).collect(Collectors.joining("\n"));
			writer.write(output);
		}
		
		public void execProcedure(String statement, PrintWriter writer) throws SQLException {
			execProcedure(statement);
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
		
		public void execQuery(String statement, PrintWriter writer, String format) throws SQLException {
	        Connection conn = openConnection();
	        PreparedStatement s = null;
	        ResultSet rs = null;
	        String header = "";
	        int lim = 1_000_000;
	        int maxlen = 4_000;
	        int riSize = digits(lim); 
	        if (riSize < 3) riSize = 3;
	        if (lim == 0) riSize = 10;
	        LocalDateTime start = null;
	        LocalDateTime result = null;
	        LocalDateTime end = null;
	        boolean explainPlan = false;
	        boolean oracle = getDbName() == DbmsType.ORACLE;
	        boolean mysql = getDbName() == DbmsType.MYSQL;
	                
	        try {
	        	useSchema(conn);
	        	s = conn.prepareStatement(statement);
	        	start = LocalDateTime.now();
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
	            result = LocalDateTime.now();
	            ResultSetMetaData m = s.getMetaData();
	            if (m == null) m = rs.getMetaData();
	            int columnCount = m.getColumnCount();
	            int rowCount = 0;
	            int columnSizes[] = new int[columnCount];
	            // Header
	            header = String.format("%1$" + riSize + "s", "ROW") + " | ";
	            for (int i = 0; i < columnCount; i++) {
	            	boolean dateField = Data.in(m.getColumnType(i + 1), DATE_TYPES);
	            	boolean lobField = Data.in(m.getColumnType(i + 1), LOB_TYPES);
	            	columnSizes[i] = dateField ? 25 : lobField ? 7 : m.getColumnDisplaySize(i + 1);
	            	if (!(explainPlan && oracle) && !dateField && columnSizes[i] > maxlen) columnSizes[i] = maxlen;
	            	String label = m.getColumnLabel(i + 1);
	            	if (columnSizes[i] < label.length()) columnSizes[i] = label.length();
	            	String fmt = "%1$-" + columnSizes[i] + "s";
	                header += String.format(fmt, label) + "  ";
	            }
	            header += "\n";
	            for (int x = 0; x < riSize; x++) header += "-";
	            header += " + ";
	            for (int i = 0; i < columnCount; i++) {
	            	for (int x = 0; x < columnSizes[i]; x++) header += "-"; 
	            	header += "  ";
	            }
	            header += "\n";
	            
	            writer.println(Data.htmlEscape(statement) + "\n");
	            if (!(explainPlan && oracle))
	            	writer.print(Data.htmlEscape(header));
	            
	            // Rows
	            while (rs.next()) {
	            	if ((rowCount > 0) && (rowCount % 40 == 0) && !(explainPlan && oracle))
	            		writer.print("\n" + Data.htmlEscape(header));
	            	rowCount++;
	            	String row = "";
	            	if (!(explainPlan && oracle)) {
	            		String fmt = "%1$" + riSize + "s";
	            		row += String.format(fmt, rowCount) + " | ";
	            	}
	                // If a limit exists, and has been reached, then stop processing the result set
	                if ((lim > 0) && (rowCount > lim))
	                    break;
	                for (int i = 0; i < columnCount; i++) {
	                	String typeName = m.getColumnTypeName(i + 1);
	                	String value = typeName.toLowerCase().contains("lob") ? "[" + typeName + "]" : rs.getString(i + 1);
	                	int lf = (value == null) ? -1 : value.indexOf('\n');
	                	if (lf > -1) value = value.substring(0, lf) + "...";
	                	if ((value != null) && (value.length() > columnSizes[i])) value = value.substring(0, columnSizes[i]);
	                	row += String.format("%1$-" + columnSizes[i] + "s", value) + "  ";
	                }
	                row += "\n";
	                writer.print(Data.htmlEscape(row));
	            }
	            
	            end = LocalDateTime.now();
	            if ((lim > 0) && (rowCount > lim)) rowCount = lim;
	            
	            writer.print(explainPlan ? "\n" : "\n" + rowCount + " rows fetched.  ");
	            if ((lim > 0) && (rowCount == lim)) writer.print("Fetch terminated by limit.  ");
	            writer.print("Exec time: " +  Duration.between(start, result).toMillis() + " ms, Proc time: " +  Duration.between(result, end).toMillis() + " ms");
	        }
	        catch (SQLException e) {
	            if (!isManaged()) conn.rollback();
	            throw e;
	        }
	        catch (Exception e) { 
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
	}
	
	@Override
	public ScheduleCallableResult call() throws Exception {
		CallableUtil util = new CallableUtil(jndiName);
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(fileName))) {
			if (type == QUERY)
				util.execQuery(statement, writer, format);
			else if (type == UPDATE)
				util.execUpdate(statement.split("---"), writer);
			else
				util.execProcedure(statement, writer);
		}
		return null;
	}

	@Override
	public void setParams(String... args) {
		jndiName = args[0];
		statement = args[1];
		fileName = args[2];
		format = args[3];  // Format Options: CSV, JSON
		type = statement.toUpperCase().startsWith("SELECT") ? QUERY
			 : statement.toUpperCase().startsWith("CALL") ? PROCEDURE
			 : UPDATE;
	}
}
