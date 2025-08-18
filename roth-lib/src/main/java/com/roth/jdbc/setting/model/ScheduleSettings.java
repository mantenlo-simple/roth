package com.roth.jdbc.setting.model;

import java.sql.SQLException;

import com.roth.base.util.Data;

public class ScheduleSettings extends BaseSettings {
	private static final long serialVersionUID = -109702891231985543L;

	public ScheduleSettings() throws SQLException {
		setCategory("schedule");
		load();
	}
	
	public Integer getTimerInterval() { return Data.strToInteger(getValue("timerInterval")); }
	public void setTimerInterval(Integer timerInterval) { setValue("timerInterval", Data.integerToStr(timerInterval)); }
	
	public Integer getMaxThreads() { return Data.strToInteger(getValue("maxThreads")); }
	public void setMaxThreads(Integer maxThreads) { setValue("maxThreads", Data.integerToStr(maxThreads)); }
}
