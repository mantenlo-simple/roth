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
import java.util.HashMap;

public class ResultBean implements Serializable {
	private static final long serialVersionUID = -2674661642475862139L;

	private HashMap<String, Object> data;
	
	protected void reset() { data.clear(); }
	
	protected Object getObject(String key) {
		if (data == null) 
			data = new HashMap<String, Object>();
		return data.get(key); 
	}
	
	protected void setObject(String key, Object value) { 
		if (data == null) 
			data = new HashMap<String, Object>();
		data.put(key, value); 
	}
}
