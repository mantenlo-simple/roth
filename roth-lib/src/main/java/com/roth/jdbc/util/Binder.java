package com.roth.jdbc.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Binder {
	public void bindObject(Object object, ResultSet resultSet) throws SQLException; 
}
