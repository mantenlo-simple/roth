package com.roth.jdbc.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import com.roth.base.util.Data;

public class MapBinder implements Binder {
	@SuppressWarnings("unchecked")
	@Override
	public void bindObject(Object object, ResultSet resultSet) throws SQLException {
		if (object instanceof Map map) {
			ResultSetMetaData meta = resultSet.getMetaData();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				Object value = resultSet.getObject(i);
				if (!resultSet.wasNull())
					map.put(Data.camelcase(meta.getColumnLabel(i).toLowerCase()), value);
			}
		}
	}
}