package com.roth.jdbc.setting.model;

import java.sql.SQLException;

import com.roth.base.util.Data;

public class MailSettings extends BaseSettings {
	private static final long serialVersionUID = -109702891231985543L;

	public MailSettings() throws SQLException {
		setCategory("mail");
		load();
	}
	
	public String getHost() { return getValue("host"); }
	public void setHost(String host) { setValue("host", host); }
	
	public Integer getPort() { return Data.strToInteger(getValue("port")); }
	public void setPort(Integer port) { setValue("port", Data.integerToStr(port)); }
	
	public String getUser() { return getValue("user"); }
	public void setUser(String user) { setValue("user", user); }
	
	public String getPassword() { return getValue("password"); }
	public void setPassword(String password) { setValue("password", password); }
	
	public Integer getMode() { return Data.strToInteger(getValue("mode")); }
	public void setMode(Integer mode) { setValue("mode", Data.integerToStr(mode)); }
	
	public String getProtocol() { return getValue("protocol"); }
	public void setProtocol(String protocol) { setValue("protocol", protocol); }
	
	/*
	public String getKeyStoreInstance() { return getValue("keyStoreInstance"); }
	public void setKeyStoreInstance(String keyStoreInstance) { setValue("keyStoreInstance", keyStoreInstance); }
	
	public String getKeyStoreFileName() { return getValue("keyStoreFileName"); }
	public void setKeyStoreFileName(String keyStoreFileName) { setValue("keyStoreFileName", keyStoreFileName); }
	
	public String getKeyStorePassword() { return getValue("keyStorePassword"); }
	public void setKeyStorePassword(String keyStorePassword) { setValue("keyStorePassword", keyStorePassword); }
	*/
}
