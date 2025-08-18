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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.roth.base.log.Log;

/**
 * 
 * @author james
 *
 */
public class JdbcStatement implements Serializable {
	private static final long serialVersionUID = -6832401278854772855L;

	private ArrayList<Object> indexedParameters;      // Parameters added through applyParameters
	private HashMap<String,Object> namedParameters;   // Parameters added through applyParameterBean
	private String mappedStatement;
	private ArrayList<ParamNotation> mappedNotations; // Indexes or Names in order of appearance in statement
	private boolean hasSqlNotations;
	private String statement;
	private HashMap<String, Boolean> names;
	
	/**
	 * Creates and initializes a JdbcStatement object, which wraps a parameterized statement
	 * using indexed notation (i.e. "{1}", "{2}", etc.) and/or named notation (i.e. "{tableId}",
	 * "{firstName}", etc.), converts it to a traditional prepared statement, and applies
	 * any supplied parameters to it, relieving the developer from having to keep track of 
	 * which "?" refers to what.
	 * 
	 * Note: Batch JdbcStatement operations in JdbcUtil support including BLOB objects, but do not 
	 * perform as well as batch String operations in JdbcUtil.  If not involving BLOB objects in 
	 * the statements, it is recommended to use batch String operations instead to improve performance.
	 * 
	 * @param statement
	 */
	public JdbcStatement(String statement) {
		this.statement = statement;
		indexedParameters = new ArrayList<>();
		namedParameters = new HashMap<>();
		mappedNotations = new ArrayList<>();
		hasSqlNotations = false;
		mapNotations();
	}
	
	public void addIndexedParameter(Object parameter) {
		indexedParameters.add(parameter);
	}
	
	public void addNamedParamter(String name, Object parameter) {
		namedParameters.put(name, parameter);
	}
	
	public void clearParameters() {
		indexedParameters.clear();
		namedParameters.clear();
	}
	
	public String[] getParamNames() {
		if (names == null) {
			names = new HashMap<>();
			for (ParamNotation note : mappedNotations)
				if (note.getName() != null)
					names.put(note.getName(), true);
		}
		return names.keySet().toArray(new String[names.keySet().size()]);
	}
	
	public boolean hasSqlNotations() { return hasSqlNotations; } 
	
	public PreparedStatement prepare(Connection conn) throws SQLException {
		// Apply any "sql:" parameters
		for (ParamNotation notation : mappedNotations) {
			if (!notation.isSql())
				continue;
			mappedStatement = mappedStatement.replaceAll(notation.getNotation(), getStringParam(notation));
		}
		
		// Prepare the statement
		PreparedStatement ps = conn.prepareStatement(mappedStatement);

		// Apply any non-"sql:" parameters
		int index = 1;
		for (ParamNotation notation : mappedNotations) {
			if (notation.isSql())
				continue;
			Log.logDebug(notation.getNotation(), null, "JdbcStatement.prepare");
			applyParameter(ps, index, getObjectParam(notation));
			notation.setApplied(true);
			index++;
		}
		return ps;
	}
	
	public void debug(String debugCode) {
		StringBuilder message = new StringBuilder("\nPrepared Statement:\n" + statement + "\nUsing Parameters:\n");
		for (ParamNotation notation : mappedNotations)
			message.append("[" + getObjectParam(notation).toString() + "]\n");
		Log.logDebug(message.toString() + "\n", null, debugCode);
	}
	
	private void mapNotations() {
		mappedStatement = statement.trim();
		// parse through statement to generate mapped statement, and fill mappedNotations list.
		
		boolean quoted = false;
		boolean found = false;
		for (int i = 0; i < mappedStatement.length(); i++) {
			if (mappedStatement.charAt(i) == '\'' && mappedStatement.charAt(i-1) != '\\')
				quoted = !quoted;
			if (!quoted && mappedStatement.charAt(i) == '{') {
				found = true;
				mappedNotations.add(new ParamNotation(i));
			}
			if (!quoted && found && mappedStatement.charAt(i) == '}') {
				found = false;
				ParamNotation notation = mappedNotations.get(mappedNotations.size() - 1);
				notation.setEnd(i);
				notation.setNotation(mappedStatement.substring(notation.getStart(), notation.getEnd() + 1));
				Log.logDebug("notation: " + notation.getNotation(), null, "mapNotations");
				if (notation.isSql())
					hasSqlNotations = true;
			}
		}
		for (int i = mappedNotations.size() - 1; i > -1; i--) {
			// Replace all notations with "?"...
			ParamNotation notation = mappedNotations.get(i);
			// ...unless it's a "sql:" notation.
			if (notation.isSql())
				continue;
			mappedStatement = mappedStatement.substring(0, notation.getStart()) + "?" + mappedStatement.substring(notation.getEnd() + 1);
			notation.setApplied(true);
			// ^ is this where I was intending to use this, or was it when I use the applyParameters and applyParameterBean functions?
		}
		
		Log.logDebug("statement:\n" + statement, null, "mapNotations");
		Log.logDebug("mappedStatement:\n" + mappedStatement, null, "mapNotations");
	}
	
	private String getStringParam(ParamNotation notation) {
		return (String)getObjectParam(notation);
	}
	
	private Object getObjectParam(ParamNotation notation) {
		if (notation.getIndex() != null) 
			return indexedParameters.get(notation.getIndex() - 1);
		else
			return namedParameters.get(notation.getName());
	}
	
	private void applyParameter(PreparedStatement ps, int index, Object parameter) throws SQLException {
		if (parameter == null)
			ps.setNull(index, 0);
		else if (parameter.getClass().isEnum())
			ps.setString(index, parameter.toString());
		/* // Confirm that this isn't needed before removing.  It looks like auto-boxing occurs and this section is never reached. 
		else if (parameter.getClass().equals(boolean.class))
			ps.setBoolean(index, (boolean)parameter);
		else if (parameter.getClass().equals(byte.class))
			ps.setByte(index, (byte)parameter);
		else if (parameter.getClass().equals(double.class))
			ps.setDouble(index, (double)parameter);
		else if (parameter.getClass().equals(float.class))
			ps.setFloat(index, (float)parameter);
		else if (parameter.getClass().equals(int.class))
			ps.setInt(index, (int)parameter);
		else if (parameter.getClass().equals(long.class))
			ps.setFloat(index, (long)parameter);
		else if (parameter.getClass().equals(short.class))
			ps.setFloat(index, (short)parameter);
		*/
		else if (parameter instanceof Boolean param)
			ps.setBoolean(index, param);
		else if (parameter instanceof Byte param)
			ps.setByte(index, param);
		else if (parameter instanceof Double param)
			ps.setDouble(index, param);
		else if (parameter instanceof Float param)
			ps.setFloat(index, param);
		else if (parameter instanceof Integer param)
			ps.setInt(index, param);
		else if (parameter instanceof Long param)
			ps.setLong(index, param);
		else if (parameter instanceof Short param)
			ps.setShort(index, param);
		else if (parameter instanceof BigDecimal param)
			ps.setBigDecimal(index, param);
		else if (parameter instanceof java.sql.Date param)
			ps.setDate(index, param);
		else if (parameter instanceof java.sql.Time param)
			ps.setTime(index, param);
		else if (parameter instanceof java.sql.Timestamp param)
			ps.setTimestamp(index, param);
		else if (parameter instanceof java.util.Date param)
			ps.setTimestamp(index, new java.sql.Timestamp(param.getTime()));
		else if (parameter instanceof java.time.LocalDate || parameter instanceof java.time.LocalDateTime ||
				 parameter instanceof java.time.LocalTime || parameter instanceof java.time.ZonedDateTime ||
				 parameter instanceof java.time.OffsetDateTime)
			ps.setObject(index, parameter);
		else if (parameter instanceof String param)
			ps.setString(index, param);
		else if (parameter instanceof byte[] param)
			ps.setBytes(index, param);
		else
			throw new SQLException("Unexpected parameter type.");
	}
}
