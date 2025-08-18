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

import com.roth.base.log.Log;
import com.roth.base.util.Data;

/**
 * For internal use by JdbcStatement.
 * @author james
 *
 */
public class ParamNotation {
	private boolean applied;
	private Integer end;
	private Integer index;
	private boolean sql;
	private String name;
	private String notation;
	private Integer start;
	
	public ParamNotation(Integer start) { this.start = start; }

	public boolean isApplied() { return applied; }
	public void setApplied(boolean applied) { this.applied = applied; }

	public Integer getEnd() { return end; }
	public void setEnd(Integer end) { this.end = end; }

	public Integer getIndex() { return index; }
	public void setIndex(Integer index) { this.index = index; }

	public boolean isSql() { return sql; }
	public void setSql(boolean sql) { this.sql = sql; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getNotation() { return notation; }
	public void setNotation(String notation) { 
		this.notation = notation;
		name = notation.substring(1, notation.length() - 1).replaceAll(" ", "");
		if (name.startsWith("sql:")) {
			sql = true;
			name = name.substring(4);
		}
		if (Data.isNumeric(name)) {
			index = Data.strToInteger(name);
			name = null;
		}
		Log.logDebug("   notation: " + notation, null, "mapNotations");
		Log.logDebug("   name: " + name, null, "mapNotations");
		Log.logDebug("   index: " + index, null, "mapNotations");
		Log.logDebug("   sql: " + sql, null, "mapNotations");
	}

	public Integer getStart() { return start; }
	public void setStart(Integer start) { this.start = start; }
}
