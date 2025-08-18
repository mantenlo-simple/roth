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
package com.roth.tags.el;

import java.util.Locale;

import jakarta.servlet.jsp.PageContext;

import com.roth.base.util.Data;
import com.roth.base.util.ResourceUtil;

/**
 * 
 * @author James M. Payne
 *
 */
public class Resource {
	public static final String DEFAULT_PATH = "com/roth/tags/html/resource/common";
	
	private static final String RESOURCE_PATH_KEY = "__roth_resource_path";
	
	private static String getPathFromContext(PageContext pageContext) {
		String path = (String)pageContext.getAttribute(RESOURCE_PATH_KEY);
		if (path == null)
			path = (String)pageContext.getRequest().getAttribute(RESOURCE_PATH_KEY);
		if (path == null)
			path = (String)pageContext.getSession().getAttribute(RESOURCE_PATH_KEY);
		return path;
	}
	
	/**
	 * Get a string value for the specified key from a resource bundle.
	 * The resource bundle is defined by the page context's locale and 
	 * <i>'__roth_resource_path'</i> attibute.  If the attribute is
	 * not defined, then it will default to <b>roth-lib's</b> common resource
	 * bundle.  It will first attempt to location the resource
	 * bundle in <b>roth-lib's</b> class loader.  If not found, then the 
	 * page context's class loader will be searched.  If still not found, 
	 * then a null is returned.
	 * @param pageContext the JSTL <i>'pageContext'</i> implicit object
	 * @param key the key to the value within the resource bundle
	 * @return the value specified by key, otherwise null
	 */
	public static String getString(PageContext pageContext, String key) {
		String path = getPathFromContext(pageContext);
		if (path == null)
			path = DEFAULT_PATH;
		return getString(pageContext, path, key);
	}
	
	/**
	 * Get a string value for the specified key from a resource bundle.
	 * The resource bundle is defined by the path parameter and the page
	 * context's locale.  It will first attempt to location the resource
	 * bundle in <b>roth-lib's</b> class loader.  If not found, then the 
	 * page context's class loader will be searched.  If still not found, 
	 * then a null is returned. 
	 * @param pageContext the JSTL <i>'pageContext'</i> implicit object
	 * @param path the path to the resource bundle
	 * @param key the key to the value within the resource bundle
	 * @return the value specified by key, otherwise null
	 */
	public static String getString(PageContext pageContext, String path, String key) {
		Locale locale = pageContext.getRequest().getLocale();
		String result = ResourceUtil.getResource(path, key, locale);
		
		if (result == null) {
			ClassLoader loader = pageContext.getServletContext().getClassLoader();
			result = ResourceUtil.getResource(path, key, locale, loader);
		}
		
		return result;
	}
	
	public static Integer getInteger(PageContext pageContext, String key) {
		return Data.strToInteger(getString(pageContext, key));
	}
	
	public static Integer getInteger(PageContext pageContext, String path, String key) {
		return Data.strToInteger(getString(pageContext, path, key));
	}
	
	/**
	 * Get a string value for the specified key from a resource bundle.
	 * The resource bundle is defined by the path parameter and the page
	 * context's locale.  It will first attempt to location the resource
	 * bundle in <b>roth-lib's</b> class loader.  If not found, then the 
	 * page context's class loader will be searched.  If still not found, 
	 * then a null is returned. 
	 * @param pageContext the JSTL <i>'pageContext'</i> implicit object
	 * @param path the path to the resource bundle
	 * @param key the key to the value within the resource bundle
	 * @return the value specified by key, otherwise null
	 */
	public static String getStringSansLocale(PageContext pageContext, String path, String key) {
		String result = ResourceUtil.getResource(path, key, Locale.ROOT);
		if (result == null) {
			ClassLoader loader = pageContext.getServletContext().getClassLoader();
			result = ResourceUtil.getResource(path, key, Locale.ROOT, loader);
		}
		
		return result;
	}
	
	/**
	 * Set resource path on the page context.
	 * @param pageContext
	 * @param resourcePath
	 */
	public static void setResourcePath(PageContext pageContext, String resourcePath) {
		setResourcePath(pageContext, resourcePath, "page");
	}
	
	/**
	 * Set resource path on the page context.
	 * @param pageContext
	 * @param resourcePath
	 */
	public static void setResourcePath(PageContext pageContext, String resourcePath, String scope) {
		switch (scope) {
		case "page": pageContext.setAttribute(RESOURCE_PATH_KEY, resourcePath); break;
		case "request": pageContext.getRequest().setAttribute(RESOURCE_PATH_KEY, resourcePath); break;
		case "session": pageContext.getSession().setAttribute(RESOURCE_PATH_KEY, resourcePath); break;
		default: {}
		}
	}
}

