package com.roth.expression;

import java.io.Serializable;

public interface IdentProvider extends Serializable {
	/*
	
	IdentProvider should handle the following scenarios:
	- Provides the value associated with the identifier name: getValue(identifier)
	  (this allows the Expression to do full evaluation for a list of POJOs)
	- Provides the SQL field name that is associated with the identifier name: getColumnName(identifier)
	  (this allows the Expression to generate a SQL filter for use in a query)
	*/
	String getColumnName(String identifier);
	String getValue(String identifier);
}
