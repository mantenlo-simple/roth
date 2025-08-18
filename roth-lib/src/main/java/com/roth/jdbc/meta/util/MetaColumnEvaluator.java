package com.roth.jdbc.meta.util;

import com.roth.jdbc.meta.model.Column;

import jdk.jfr.Experimental;

@Experimental
public interface MetaColumnEvaluator {
	/**
	 * Format a MetaColumn to a SQL type definition.
	 * @param fieldClass
	 * @return
	 */
	String format(Column meta);
	
	/**
	 * Parse a SQL type definition to a MetaColumn. 
	 * @param fieldDefinition
	 * @return
	 */
	Column parse(String fieldDefinition);
}
