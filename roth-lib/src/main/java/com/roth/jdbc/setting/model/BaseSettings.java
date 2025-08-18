package com.roth.jdbc.setting.model;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;

import com.roth.portal.util.Portal;
import com.roth.base.log.Log;
import com.roth.base.util.Data;

public abstract class BaseSettings implements Serializable {
	private static final long serialVersionUID = -3512508570232011659L;

	private String category;
	private HashMap<String,String> values;
	private HashMap<String,Boolean> changed;
	
	protected void setCategory(String category) { this.category = category; }
	
	protected HashMap<String,String> _getValues() { return values; }
	
	protected String getValue(String name) {
		if (values == null)
			values = new HashMap<>();
		if (changed == null)
			changed = new HashMap<>();
		return values.get(name); 
	}
	protected void setValue(String name, String value) {
		if (values == null)
			values = new HashMap<>();
		if (changed == null)
			changed = new HashMap<>();
		if (!Data.nvl(values.get(name)).equals(Data.nvl(value)))
			changed.put(name, true);
		values.put(name, value); 
	}
	
	public void load() {
		try {
			values = new Portal().getSettings(category);
		}
		catch (SQLException e) {
			Log.logWarning("Unable to gather data from 'roth.setting'.", null);
		}
	}
	
	public void save(String userid) {
		try {
			new Portal().saveSettings(category, values, changed, userid);
		}
		catch (SQLException e) {
			Log.logException("Unable to save data to 'roth.setting'.", null, e);
		}
	}
}
