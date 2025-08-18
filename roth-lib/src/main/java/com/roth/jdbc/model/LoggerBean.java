/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.roth.jdbc.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class LoggerBean extends ResultBean {
	private static final long serialVersionUID = -8776360726229506660L;

	private HashMap<String,LogValue> log;
	private boolean suppressLogging;
	
	public HashMap<String,LogValue> getLog() { return log; }
	
	public boolean getSuppressLogging() { return suppressLogging; }
	public void setSuppressLogging(boolean suppressLogging) { this.suppressLogging = suppressLogging; }
	
	public class LogValue implements Serializable {
		private static final long serialVersionUID = -242818588695460098L;

		private String fieldName;
		private Object oldValue;
		private Object newValue;

		public LogValue(String fieldName, Object oldValue, Object newValue) {
			this.fieldName = fieldName;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
		public String getFieldName() { return fieldName; }
		
		public Object getOldValue() { return oldValue; }
		
		public Object getNewValue() { return newValue; }
		public void setNewValue(Object newValue) { this.newValue = newValue; }
	}
	
	protected boolean equals(Object oldValue, Object newValue) {
		return ((oldValue == null) && (newValue == null)) ||
			   ((oldValue != null) && (newValue != null) && (oldValue.equals(newValue)));
	}
	
	protected boolean different(String name, Object oldValue, Object newValue) {
		boolean isLob = oldValue instanceof byte[] || newValue instanceof byte[];
		// If objects are LOBs (byte arrays), then use a hash instead of the actual value.
		Object o = isLob ? Arrays.hashCode((byte[])oldValue) : oldValue;
		Object n = isLob ? Arrays.hashCode((byte[])newValue) : newValue; 
		boolean result = !equals(o, n);
		if (result && !suppressLogging) {
			if (log == null) log = new HashMap<String,LogValue>();
			LogValue v = log.get(name);
			if (v == null) log.put(name, new LogValue(name, o, n));
			else v.setNewValue(newValue);
		}
		return result;
	}
	
	/**
	 * Provides a change-logging interface to the ResultBean.setObject method.
	 * @param key The field name.
	 * @param value The field value.
	 */
	protected void logSetObject(String key, Object value) { if (different(key, getObject(key), value)) setObject(key, value); }
}
