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
package com.roth.base.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceUtil {
	private static ResourceUtil _instance;
	private HashMap<String, ResourceBundle> map;
	
	private ResourceUtil() {
		map = new HashMap<String, ResourceBundle>();
	}
	
	public static synchronized ResourceUtil getInstance() {
		if (_instance == null) _instance = new ResourceUtil();
		return _instance;
	}
	
	/**
	 * This will attempt to locate the properties file defined by the path, 
	 * and locale within <b>jplib's</b> class loader.  If one is not found, then 
	 * it will attempt to locate the properties file for the ENGLISH locale.
	 * If still not found, then a null is returned.
	 * @param path the path to the resource bundle
	 * @param key the key to the value within the resource bundle
	 * @param locale the locale (i.e. language)
	 * @return the value specified by key, otherwise null
	 */
	public static String getResource(String path, String key, Locale locale) {
		return getResource(path, key, locale, null);
	}
	
	/**
	 * This will attempt to locate the properties file defined by the path, 
	 * locale, and class loader.  If one is not found, then it will attempt
	 * to locate the properties file for the ENGLISH locale.  If still not
	 * found, then a null is returned.
	 * @param path the path to the resource bundle
	 * @param key the key to the value within the resource bundle
	 * @param locale the locale (i.e. language)
	 * @param loader the class loader (if applicable)
	 * @return the value specified by key, otherwise null
	 */
	public static String getResource(String path, String key, Locale locale, ClassLoader loader) {
		ResourceUtil u = getInstance();
		ResourceBundle r = u.map.get(path + "_" + locale);
		
		if (r == null) 
			try { r = getBundle(path, locale, loader);
				  if (r == null) r = getBundle(path, Locale.ENGLISH, loader);
				  if (r != null)_instance.map.put(path + "_" + locale, r);
			}
		    catch (Exception e) { }
		    
		return (r == null) ? null : r.getString(key);
	}
	
	private static ResourceBundle getBundle(String path, Locale locale, ClassLoader loader) {
		ResourceBundle result = null;
		
		try {
			result = (loader == null)
			       ? ResourceBundle.getBundle(path, locale)
				   : ResourceBundle.getBundle(path, locale, loader);
		}
		catch (Exception e) { }
		
		return result;
	}
}
