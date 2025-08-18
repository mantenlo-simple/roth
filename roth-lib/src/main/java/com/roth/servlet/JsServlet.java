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
package com.roth.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.base.util.ResourceUtil;
import com.roth.servlet.util.Browser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String JS_SERVLET_SET = "_jsServlet.set";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		Browser browser = new Browser(request.getHeader("User-Agent"));
		String path = request.getRequestURI().replace(request.getContextPath(), "")
		                                     .replace("JS", "js");
		URL myURL = request.getSession().getServletContext().getResource(path);
		if (myURL == null) {
			String userid = request.getUserPrincipal() == null ? "<unavailable>" : request.getUserPrincipal().getName();
			Log.logWarning("[JsServlet.doPost] ERROR: Invalid path: '" + path + "'.", userid);
			response.sendError(404);
			return;
		}
	    
	    InputStream in = myURL.openStream();
	    try (InputStreamReader isr = new InputStreamReader(in); BufferedReader reader = new BufferedReader(isr)) {
		    response.setContentType("text/javascript");
		    
		    while (reader.ready()) {
		    	String line = processLine(reader.readLine(), browser, request);
		    	if (line != null) response.getWriter().println(line);
		    }
	    }
	}
	
	/**
	 * Get a string value for the specified key from a resource bundle.
	 * The resource bundle is defined by the path parameter and the page
	 * context's locale.  It will first attempt to location the resource
	 * bundle in <b>jplib's</b> class loader.  If not found, then the 
	 * page context's class loader will be searched.  If still not found, 
	 * then a null is returned. 
	 * @param pageContext the JSTL <i>'pageContext'</i> implicit object
	 * @param path the path to the resource bundle
	 * @param key the key to the value within the resource bundle
	 * @return the value specified by key, otherwise null
	 */
	public static String getString(HttpServletRequest request, String path, String key) {
		Locale locale = request.getLocale();
		
		if (path == null)
			path = "com/jp/html/resource/common";
		
		String result = ResourceUtil.getResource(path, key, locale);
		
		if (result == null) {
			ClassLoader loader = request.getServletContext().getClassLoader();
			result = ResourceUtil.getResource(path, key, locale, loader);
		}
		
		return result;
	}
	
	protected String combineFromOne(String[] source) {
		StringBuilder expression = new StringBuilder("");
		for (int i = 1; i < source.length; i++)
			expression.append(source[i]);
		return expression.toString();
	}
	
	@SuppressWarnings("unchecked")
	protected String doExpressionSet(String expression, HttpServletRequest request) {
		String[] ex = expression.split("=");
		if (request.getAttribute(JS_SERVLET_SET) == null)
			request.setAttribute(JS_SERVLET_SET, new HashMap<String,String>());
		HashMap<String,String> set = (HashMap<String,String>)request.getAttribute(JS_SERVLET_SET);
		set.put(ex[0], ex[1]);
		return "";
	}
	
	@SuppressWarnings("unchecked")
	protected String doExpressionGet(String expression, HttpServletRequest request) {
		if (request.getAttribute(JS_SERVLET_SET) == null)
			return "";
		HashMap<String,String> set = (HashMap<String,String>)request.getAttribute(JS_SERVLET_SET);
		return set.get(expression);
	}
	
	@SuppressWarnings("unchecked")
	protected String doExpressionResource(String expression, HttpServletRequest request) throws ServletException {
		String[] ex = expression.split("\\.");
		if (ex.length > 2)
			throw new ServletException("Invalid resource expression '" + expression + "'.");
		String path = (ex.length == 2) ? ex[0] : null;
		String key = (ex.length == 2) ? ex[1] : ex[0];
		HashMap<String,String> set = (HashMap<String,String>)request.getAttribute(JS_SERVLET_SET);
		if ((path == null) && (set != null))
			path = set.get("path");
		return Data.nvl(getString(request, path, key));
	}
	
	protected String doExpressionSql(String expression, HttpServletRequest request) throws ServletException {
		return null;
	}
	
	protected String doExpressionCache(String expression, HttpServletRequest request) throws ServletException {
		return null;
	}
	
	protected String processLine(String source, Browser browser, HttpServletRequest request) throws ServletException {
		String result = source;
		int pos = result.indexOf("#[");
		while (pos > -1) {
			int p2 = result.indexOf("]", pos);
			String expression = result.substring(pos, p2 + 1);
			String[] exc = result.substring(pos + 2, p2).split(" ");
			// #[set resourcePath=com/jp/html/resource/common]  <-- Sets the default path for a resource lookup
			// #[set sqlDataSource=dsn.table:locale.key.value]  <-- Sets the default location for a sql dataSource lookup
			// #[set cacheLocation=requestScope.attribute] <-- how should the cache be formatted?
			String command = exc[0];
			String value = "";
			switch (command) {
				case "set" : value = doExpressionSet(combineFromOne(exc), request); break; 
				case "get" : value = doExpressionGet(combineFromOne(exc), request); break;
				case "resource" : value = doExpressionResource(combineFromOne(exc), request); break;
				case "contextRoot" : value = request.getContextPath(); break;
				case "sql" : break;
				case "cache" : break;
				default : throw new ServletException("Invalid expression command '" + command + "'.");
			}
			// If not one of ('set', 'get', 'resource', 'sql', 'cache')
			result = result.replaceAll(Pattern.quote(expression), value);
			pos = result.indexOf("#[");
		}
		return result;
	}
}
