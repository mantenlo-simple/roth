package com.roth.jdbc.setting.model;

import com.roth.base.util.Data;

public class JdbcSettings extends BaseSettings {
	private static final long serialVersionUID = -109702891231985543L;

	public JdbcSettings() {
		setCategory("jdbc");
		load();
	}
	
	public Integer getQueryTimeout() { return Data.strToInteger(getValue("queryTimeout")); }
	public void setQueryTimeout(Integer queryTimeout) { setValue("queryTimeout", Data.integerToStr(queryTimeout)); }
}
