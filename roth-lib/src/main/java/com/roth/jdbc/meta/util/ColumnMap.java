package com.roth.jdbc.meta.util;

import com.roth.jdbc.meta.model.Column;
import com.roth.jdbc.meta.model.ColumnInfoBean;
import com.roth.jdbc.util.DbmsType;

public interface ColumnMap {
	/**
	 * Identify the database name (type) that this processor is for.
	 * This is used to verify validity when paired with a JNDI data source.
	 * @return
	 */
	DbmsType databaseName();
	
	/**
	 * Translate a ColumnInfoBean to a Column.
	 * @param nativeType
	 * @return
	 */
	Column toColumn(ColumnInfoBean info);

	/**
	 * Translate a Column to a SQL string.
	 * @param type
	 * @param size
	 * @param precision
	 * @return
	 */
	String toSqlString(Column column);
	
	/**
	 * Compare two columns and produce an alter table clause
	 * if differences are detected.  If the columns are equal
	 * then a blank string should be returned.  If the old
	 * column name is less than the new column name, then
	 * a drop column clause is returned, if the old column
	 * name is greater than the new column name, then an add
	 * column clause is returned, and lastly, if the column
	 * names are the same, but the details are different, then
	 * a change/modify column clause is returned.
	 * @param o
	 * @param n
	 * @return
	 */
	String toSqlDiff(Column o, Column n);
}
