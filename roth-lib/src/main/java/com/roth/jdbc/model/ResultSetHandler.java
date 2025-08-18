package com.roth.jdbc.model;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public interface ResultSetHandler {
	public void onStart();
	public void onEnd();
	public void onRow(ResultSet resultset, ResultSetMetaData metadata); // callback for handling a dataset row.
}
