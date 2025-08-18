package com.roth.jdbc.setting.model;

import java.sql.SQLException;

import com.roth.base.util.Data;

public class MiscSettings extends BaseSettings {
	private static final long serialVersionUID = -109702891231985543L;

	public MiscSettings() throws SQLException {
		setCategory("misc");
		load();
	}
	
    private String valueList;
    
    public String getValueList() { return valueList; }
    public void setValueList(String valueList) { this.valueList = valueList == null ? null : valueList.trim(); }
    
    @Override
    public void load() {
		super.load();
		
		valueList = "";
		if (_getValues() != null)
			for (String key : _getValues().keySet())
				valueList += (valueList.isEmpty() ? "" : "\n") + key + "=" + getValue(key);
	}
	
    @Override
    public void save(String userid) {
    	if (_getValues() != null)
	    	for (String key : _getValues().keySet())
	    		setValue(key, null);    	
    	
    	if (valueList != null)
	    	for (String value : Data.splitLF(valueList)) {
	    		String[] kv = value.split("=");
				setValue(kv[0], kv.length > 1 ? kv[1] : "");
	    	}
    	
		super.save(userid);
	}
    
	public String getValue(String name) { return super.getValue(name); }
	public void setValue(String name, String value) { super.setValue(name, value); }
	
	public Boolean getBoolean(String name) { 
		String value = super.getValue(name);
		return value == null ? null : value.equalsIgnoreCase("true") || value.equalsIgnoreCase("Y"); 
	}
	public void setBoolean(String name, Boolean value) {
		super.setValue(name, value.toString());
	}
	
}
