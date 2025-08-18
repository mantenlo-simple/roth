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
package com.roth.servlet.util;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.servlet.ActionServlet.Bean;
import com.roth.servlet.ActionServlet.Post;

/**
 * ServletParameter processes incoming GET, POST, and multipart/form parameters based on a given @Post annotation.
 * There are two valid parameter name notation formats: single-segment and full-path.
 * A single-segment parameter consists of only a name (e.g. "description" or "name").
 * A full-path parameter is multi-segmented (segments are delimited by a dot '.'); the first segment being a reference to scope (i.e request, session, or application). 
 * @author jpayne
 *
 */
public class ParameterProcessor implements Serializable {
	private static final long serialVersionUID = -8274144068443789784L;

	private HttpServletRequest request;
	private Post post;
	
	public static final String REQUEST = "request";
	public static final String REQUEST_SCOPE = "requestScope";
	public static final String SESSION = "session";
	public static final String SESSION_SCOPE = "sessionScope";
	public static final String APPLICATION = "application";
	public static final String APPLICATION_SCOPE = "applicationScope";
	public static final String CONTEXT = "context";
	public static final String CONTEXT_SCOPE = "contextScope";
	
//	private static final String[] REQUEST_SCOPES = {REQUEST, REQUEST_SCOPE};
//	private static final String[] SESSION_SCOPES = {SESSION, SESSION_SCOPE};
//	private static final String[] APPLICATION_SCOPES = {APPLICATION, APPLICATION_SCOPE, CONTEXT, CONTEXT_SCOPE};
	private static final String[] SCOPES = {REQUEST, REQUEST_SCOPE, SESSION, SESSION_SCOPE, APPLICATION, APPLICATION_SCOPE, CONTEXT, CONTEXT_SCOPE};
	
	public ParameterProcessor(HttpServletRequest request, Post post) {
		this.request = request;
		this.post = post;
		processParameters();
	}
	
	protected Object getAttr(String scope, String name) throws Exception {
		switch (scope) {
			case REQUEST_SCOPE: return request.getAttribute(name); 
			case SESSION_SCOPE: return request.getSession().getAttribute(name); 
			case APPLICATION_SCOPE:
			case CONTEXT_SCOPE: return request.getSession().getServletContext().getAttribute(name); 
			default: throw new Exception("Invalid scope reference: " + scope);
		}
	}
	
	protected Object setAttr(String scope, String name, Object attr) throws Exception {
		switch (scope) {
			case REQUEST_SCOPE: request.setAttribute(name, attr); break; 
			case SESSION_SCOPE: request.getSession().setAttribute(name, attr); break; 
			case APPLICATION_SCOPE:
			case CONTEXT_SCOPE: request.getSession().getServletContext().setAttribute(name, attr); break; 
			default: throw new Exception("Invalid scope reference: " + scope);
		}
		return attr;
	}
	
	/**
	 * This returns whether the request contains multi-part form data.
	 * @return
	 */
	protected boolean isMultipart() { return request.getContentType() == null ? false : request.getContentType().startsWith("multipart/form-data"); }
	
	/**
	 * This returns a posted parameter from a multi-part form.
	 * @param name
	 * @return
	 */
	private String getPartParameter(String name) {
		try {
			Part part = request.getPart(name);
			if (part != null)
				return new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (ServletException e) { Log.logException(e, null); } // Is thrown if not multipart/form-data, however that was already checked for.
		catch (IOException e) { Log.logException(e, null); } // Is thrown if an I/O error occurs during retrieval.
		return null;
	}
	
	/**
	 * This is a shortcut for getRequest().getParameter(String name);
	 * @param name
	 * @return
	 */
	public String getParameter(String name) {
		return isMultipart() ? Data.nvl(getPartParameter(name), request.getParameter(name)) : request.getParameter(name); 
	}
	
	/**
	 * This returns the user principal's name, if one is present (i.e. a user is logged in).
	 * @return
	 */
	protected String getUserName() { 
		Principal p = request.getUserPrincipal(); 
		if (p != null)
			return p.getName();
		return null;
	}
	
	/**
	 * This returns a list of parameter keys, including multi-part form names if applicable.  
	 * @return
	 */
	protected String[] getParamKeys() {
		ArrayList<String> paramKeys = new ArrayList<>();
		paramKeys.addAll(request.getParameterMap().keySet());
		if (isMultipart())
			try {
				for (Part part : request.getParts())
					paramKeys.add(part.getName());
			}
			catch (Exception e) { Log.logException(e, getUserName()); }
		return paramKeys.toArray(new String[paramKeys.size()]);
	}
	
	protected Object initObject(Bean bean) throws IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (bean.typeClass() == Object.class)
			return bean.beanClass().getConstructor().newInstance();
		else
			return bean.typeClass().getConstructor().newInstance();
	}
	
	protected void processParameters() {
		// Don't process parameters if there is no Post annotation, or if the Post annotation is empty.
		if (post == null || post.beans().length == 0) return;
		// - Get list of parameter names from request.
		// - Sort names such that longest number of segments come first, with any indexes in descending order.
		// - Loop through parameter names and process

		// Process the parameter keys
		List<ParameterSegment> parameters = new ArrayList<>();
		List<String> singular = new ArrayList<>();
		
		String[] paramKeys = getParamKeys();
		for (String key : paramKeys) {
			String[] segments = key.split("\\.");
			// Separate singular parameter keys (i.e. parameters with only one segment).  These will 
			// be processed later.
			if (segments.length == 1)
				singular.add(key);
			else {
				// Make sure that multi-segment parameter keys begin with a scope.  If not, log an exception
				// and continue without processing the parameter.
				if (!Data.in(segments[0], SCOPES)) {
					Log.logException(new Exception("An invalid segmented parameter key was encountered: \"" + key + "\".  Segmented parameters must begin with a scope reference."), getUserName());
					continue;
				}
				// Find primary segment, if already in list.  If not, created it and add it to the list.
				ParameterSegment ps = ParameterSegment.findSegment(parameters, segments[0]);
				if (ps == null) {
					ps = new ParameterSegment(segments[0]);
					parameters.add(ps);
				}
				// Process subsequent segments until all descendants are prepared.
				if (segments.length > 1)
					for (int i = 1; i < segments.length; i++)
						ps = ps.getChild(segments[i]);
			}
		}

		// Sort segments (this is recursive through the segment tree).
		// The order of the sort ensures that high numeric indexes come first.  This ensures that when
		// an array is instantiated, it will have the correct length.
		ParameterSegment.sortSegments(parameters);
		
		// Process segmented parameters.
		Map<String, ParameterSegment> map = new LinkedHashMap<>();
		for (ParameterSegment p : parameters)
			p.getParameterStrings(map);
		
		for (String k : map.keySet()) {
			ParameterSegment ps = map.get(k);
			ps.process(getParameter(k), post, this);
		}
		
		// Process singular parameters (they may be dependent upon the processed segmented parameters.
	//	for (String s : singular) {
			
	//	}
	}
	
	
}
