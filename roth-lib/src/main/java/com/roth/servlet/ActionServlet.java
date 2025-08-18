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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.security.Principal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import com.roth.base.annotation.ReadOnly;
import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.export.util.JsonUtil;
import com.roth.portal.util.Portal;
import com.roth.realm.RothPrincipal;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Multipart;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;
import com.roth.servlet.util.ParameterParser;
import com.roth.servlet.util.SsoXsrfMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public abstract class ActionServlet extends HttpServlet {
	private static final long serialVersionUID = -3681841395411597362L;

	/** Size constant. */
	protected static final int KILOBYTE = 1_024;
	/** Size constant. */
	protected static final int MEGABYTE = 1_048_576;
	/** Size constant. */
	protected static final int GIGABYTE = 1_073_741_824;
	/** Size constant. */
	protected static final long TERABYTE = 1_099_511_627_776L;
	/** Size constant. */
	protected static final long PETABYTE = 1_125_899_906_842_624L;
	/** Size constant. */
	protected static final long EXABYTE = 1_152_921_504_606_846_976L;
	
	/** Scope constant. */
	public static final String REQUEST = "request";
	/** Scope constant. */
	public static final String SESSION = "session";
	/** Scope constant. */
	public static final String APPLICATION = "application";
	/** Scope constant. */
	public static final String CONTEXT = "context";
	
	/** Name constant for @SimpleAction. */
	public static final String BEGIN = "begin";
	/** Name constant for @Forward or @Request. */
	public static final String FAILURE = "failure";
	/** Name constant for @Forward or @Request. */
	public static final String SUCCESS = "success";
	
	/** Method constant. */
	public static final String GET = "GET";
	/** Method constant. */
	public static final String POST = "POST";
	/** Method constant. */
	public static final String PUT = "PUT";
	/** Method constant. */
	public static final String PATCH = "PATCH";
	/** Method constant. */
	public static final String DELETE = "DELETE";
	
	/** Role constant. */
	public static final String ANONYMOUS = "Anonymous";
	/** Role constant. */
	public static final String AUTHENTICATED = "Authenticated";
	
	public static final String ACTION_SERVLET_FILES = "__ActionServlet.files";
	
	private static final String FORMATS = "formats";
	private static final String CSRF_TOKEN = "_csrf-token";
	private static final String XSRF_TOKEN = "xsrf-token";
	private static final String PROCESS_XSRF = "processXsrf";

//	private static final String BEAN_CLASS_REQUIRED = "SimpleAction named '%s' is malformed.  A beanClass is required when calling a sub-action.";
//	private static final String ACTION_REQUIRED = "SimpleAction named '%s' failed.  An action is required when not supplying a sub-action in the URL.";
	private static final String PATH_REQUIRED = "SimpleAction named '%s' is malformed.  A path or action is required.";
	
	private static final String CALLING_ACTION_NAME = "_ActionServlet.callingActionName";
	
	private static final String FORWARD_RESPONSE_NOT_FOUND = "A forward or response named '%s' was not found.";
	private static final String CLASS_DOES_NOT_MATCH = "Class found at the annotated reference '%s' does not match the defined class.";
	
	// Annotations =================================================================================
	/**
	 * Defines a "next stop" in the request cycle.
	 * "name"   (required) defines the name of the forward.
	 *          This corresponds with the result value of an action.   
	 * "action" (optional) defines the action to forward to, if desired.
	 *          This is mutually exclusive with "path".
	 * "path"   (optional) defines the JSP to forward to, if desired.
	 * @author jpayne
	 *
	 */
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface Forward {
		String action() default "";
		String name();
		String path() default "";
		String pathMobi() default "";
	}
	
	/**
	 * Defines a direct response to a request.
	 * "name"    (required) defines the name of the response.
	 *           This corresponds with the result value of an action.   
	 * "message" (optional) defines a message (if desired) to return to the client.
	 * "httpStatusCode" (optional) defines the HTTP status code to return.
	 *                  The default is 200 (success).
	 * @author jpayne
	 *
	 */
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface Response {
		String name();
		String message() default "";
		int httpStatusCode() default 200;
	}
	
	/**
	 * Designates a servlet method as an Action.<br>
	 * An action must be defined as follows:<br>
	 * <code>public String methodName(ActionConnection conn) { }</code><br>
	 * The ActionConnection parameter contains the request and response
	 * objects.
	 * @author jpayne
	 *
	 */
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface Action {
		Forward[] forwards() default {};
		Response[] responses() default {};
	}
	
	/**
	 * Defines a bean destination for posted data.  This is used with
	 * the @Post annotation.  Beans that are instantiated are added to
	 * the attribute list of the specified scope.<br>
	 * <b>name</b> (required) defines the name of the object.<br>
	 * <b>scope</b>     (required) defines the scope of the object.<br>
	 * <b>beanClass</b> (required) defines the class of the object.
	 *             If used with "typeClass", then it defines the
	 *             element class.<br>
	 * <b>typeClass</b> (optional) defines the class of object when<br>
	 *             defining an array or collection.  If this attribute
	 *             is not used, then the @Bean annotation refers to
	 *             an atomic value.  Valid values
	 *             may be an Array ?[].class (example: MyBean[].class),
	 *             a List (? instanceof List).class (example: ArrayList.class),
	 *             or a Map (? instanceof Map).class (example: HashMap.class).
	 * @author jpayne
	 *
	 */
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface Bean {
		String name();
		Class<?> beanClass();
		Class<?> typeClass() default Object.class; 
		String scope() default REQUEST; // "request", "session", or "application"
	}
	
	/**
	 * Designates an Action as a POST data processor. i.e. data posted
	 * through an HTML form, and any GET parameters will be processed 
	 * by the servlet and placed in the defined beans, just prior to 
	 * execution of the action.<br>
	 * <b>beans</b> (required) defines an array of @Bean annotations.
	 * @author jpayne
	 * @see {@link @Bean}
	 */
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface Post {
		Bean[] beans();
	}
	
	/**
	 * Specifies that an Action can only be executed if the user has
	 * one or more of the roles specified.  Otherwise a 401 or 403 will result.<br>
	 * <b>roles</b> (required) defines an array of role names.
	 * @author jpayne
	 */
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface MethodSecurity {
		String[] roles() default {};
		String[] methods() default {};
	}

	public class SubPath extends ParameterParser<Integer> {
		private static String[] getSegments(HttpServletRequest request) {
			String subPath = Data.envl((String)request.getAttribute("__subpath"));
			return subPath == null ? null : subPath.split("/");
		}
		
		@SuppressWarnings("unchecked")
		SubPath(HttpServletRequest request) {
			super(getSegments(request), (HashMap<String,String>)request.getSession().getAttribute("formats"), request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName());
		}
	}
	
	// ActionConnection ============================================================================
	
	/**
	 * The ActionConnection class encapsulates the current connection's Request and Response
	 * objects, as well as provide some convenience functions to shortcut interactions with
	 * the Request and Reponse objects.
	 * @author James M. Payne
	 *
	 */
	public class ActionConnection extends ParameterParser<String> {
		//private static final String ArrayList = null;  // <-- What was this?
		HttpServletRequest request; 
		HttpServletResponse response;
		String userName;
		String userid;
		String domainName;
		Long domainId;
		SubPath subPath;
		
		public ActionConnection(HttpServletRequest request, HttpServletResponse response) {
			super(request);
			this.request = request;
			this.response = response;
			
			Principal p = request.getUserPrincipal(); 
			if (p != null) {
				userName = p.getName();
				if (p instanceof RothPrincipal cp) {
					userid = cp.getUserid();
					domainName = cp.getDomainName();
					domainId = cp.getDomainId();
				}
				else {
					userid = null;
					domainName = null;
					domainId = null;
				}
			}
			else {
				userName = null;
				userid = null;
				domainName = null;
				domainId = null;
			}
			
			subPath = new SubPath(request);
		}
		
		public HttpServletRequest getRequest() { return request; }
		public HttpServletResponse getResponse() { return response; }
		public HttpSession getSession() { return request.getSession(); }
		public ServletContext getContext() { return request.getServletContext(); }
		public SubPath getSubPath() { return subPath; }
		/**
		 * This is a shortcut for getRequest.getUserPrincipal().getName();  This is the userid and domainName in the form: "userid@domainName".
		 * @return
		 */
		public String getUserName() { return userName; }
		/**
		 * This returns the userid of the user;
		 * @return
		 */
		public String getUserid() { return userid; }
		/**
		 * This returns the domain of the user.  If the user logged in without a domain, then the domain is implied to be "default".
		 * @return
		 */
		public String getDomainName() { return domainName; }
		/**
		 * This returns the domainId of the domain of the user.
		 * @return
		 */
		public Long getDomainId() { return domainId; }
		/**
		 * This is a shortcut for getRequest().isUserInRole(String name);
		 * Returns true if the user has any of the roles.
		 * @param roleName
		 * @return
		 */
		public boolean hasRole(String ... roleName) {
			for (String role : roleName)
			    if (request.isUserInRole(role))
			    	return true;
			return false;
		}
		/**
		 * This returns true only if the user has all roles named.
		 * @param roleName
		 * @return
		 */
		public boolean hasAllRoles(String ... roleName) {
			for (String role : roleName)
			    if (!request.isUserInRole(role))
			    	return false;
			return true;
		}
		/**
		 * This returns the string representation of the request's locale.
		 * @return
		 */
		public String getLocale() { return request.getLocale().toString(); }
		/**
		 * This returns a list of parameter keys, including multi-part form names if applicable.  
		 * @return
		 */
		public String[] getParamKeys() {
			ArrayList<String> paramKeys = new ArrayList<>();
			paramKeys.addAll(request.getParameterMap().keySet());
			if (isMultipart())
				try {
					for (FileItem item : getCachedMultiPartForm(request))
						paramKeys.add(item.getFieldName());
				}
				catch (Exception e) { Log.logException(e, getUserName()); }
			return paramKeys.toArray(new String[paramKeys.size()]);
		}
		/**
		 * This returns whether the request contains multi-part form data.
		 * @return
		 */
		public boolean isMultipart() { return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data"); }
		/**
		 * This returns a posted parameter from a multi-part form.
		 * @param name
		 * @return
		 */
		private String getMultiPartParam(String name) {
			List<FileItem> formItems = getCachedMultiPartForm(request);
	        for (FileItem item : formItems)
	        	if (item.isFormField() && item.getFieldName().equals(name))
	        		return item.getString();
	        return null;
		}
		/**
		 * This is a shortcut for getRequest().getParameter(String name);
		 * @param name
		 * @return
		 */
		@Override
		public String getParameter(String name) {
			return isMultipart() ? Data.envl(Data.nvl(getMultiPartParam(name), Data.nvl(request.getParameter(name)))) : Data.envl(request.getParameter(name));
		}
		/**
		 * This returns whether a posted parameter is a file parameter.
		 * @param name
		 * @return
		 */
		public boolean isFileParam(String name) {
			List<FileItem> formItems = getCachedMultiPartForm(request);
	        for (FileItem item : formItems)
	        	if (!item.isFormField() && item.getFieldName().equals(name))
	        		return true;
	        return false;
		}
		/**
		 * This returns a posted file parameter from a multi-part form.
		 * @param name
		 * @return
		 */
		public byte[] getFileParam(String name) {
			List<FileItem> formItems = getCachedMultiPartForm(request);
	        for (FileItem item : formItems)
	        	if (!item.isFormField() && item.getFieldName().equals(name))
	        		try { return item.getInputStream().readAllBytes(); }
	        		catch (IOException e) { Log.logException(e, this.getUserName()); }
	        return new byte[0];
		}
		/**
		 * This returns a posted parameter value list from a multi-part form.
		 * @param name
		 * @return
		 */
		private String[] getMultiPartParamValues(String name) {
			List<String> list = new ArrayList<>();
			List<FileItem> formItems = getCachedMultiPartForm(request);
	        for (FileItem item : formItems)
	        	if (item.isFormField() && item.getFieldName().equals(name)) {
	        		String value = item.getString();
	        		if (value != null)
	        			list.add(value);
	        	}
	        return list.isEmpty() ? null : list.toArray(new String[list.size()]);
		}
		/**
		 * This is a shortcut for getRequest.getParameterValues(String name);
		 * @param name
		 * @return
		 */
		public String[] getParameterValues(String name) {
			return isMultipart() ? getMultiPartParamValues(name) : request.getParameterValues(name);
		}
		/**
		 * Returns a list of uploaded files, if any.  Ensure the form or j:form has encType="multipart/form-data" or an exception will be thrown.
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public List<FileItem> getFiles(int maxSize) {
			Log.logDebug("" + maxSize, userid, "getFiles"); // Should do something with this someday.
			return (List<FileItem>)request.getAttribute(ACTION_SERVLET_FILES);
		}
		
		/**
		 * Returns a list of uploaded files, if any.  This will cache the results in the request scope.
		 * Subsequent calls within the same scope will return the cached results.  
		 * Ensure the form or j:form has encType="multipart/form-data" or an exception will be thrown.
		 * @param request
		 * @param maxSize
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public List<FileItem> getCachedFiles() {
			return (List<FileItem>)request.getAttribute(ACTION_SERVLET_FILES);
		}
		
		/**
		 * Returns a cached uploaded file by field name, if it exists.  Returns null otherwise.
		 * @param name
		 * @param request
		 * @param maxSize
		 * @return
		 */
		public FileItem findCachedFile(String name) {
			try {
				List<FileItem> list = getCachedFiles();
				for (FileItem item : list)
					if (item.getFieldName().equals(name))
						return item;
			}
			catch (Exception e) {
				Log.logException(e, null);
			}
			return null;
		}
		
		/**
		 * The request will checked for one of the following scenarios:<br/>
		 * 1. JSON data posted as the request body with contentType="application/json"<br/>
		 * 2. JSON data posted as a value/parameter named "json"<br/>
		 * 3. JSON data posted as a value/parameter named "data"<br/>
		 * If no JSON data is found, an IOException is thrown.
		 * @return
		 * @throws IOException
		 */
		public String getJson() throws IOException {
			if ("application/json".equals(request.getContentType()))
				return request.getReader().lines().collect(Collectors.joining()); //IOUtils.toString(request.getReader());
			String result = getString("json");
			if (result != null)
				return result;
			result = getString("data");
			if (result == null)
				throw new IOException("JSON data not found.");
			return result;
		}
		
		/**
		 * Translate JSON to an atomic class (e.g. String, Long, etc.), POJO, or array. 
		 * @param <T>
		 * @param beanClass
		 * @return
		 * @throws IOException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 * @throws NoSuchMethodException
		 */
		public <T> T getJson(Class<T> beanClass) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			String json = getJson();
			return JsonUtil.jsonToObj(json, beanClass);
		}
		
		/**
		 * Translate JSON to a collection (e.g. ArrayList, Vector, etc.).
		 * @param <T>
		 * @param beanClass
		 * @param elementClass
		 * @return
		 * @throws IOException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 * @throws NoSuchMethodException
		 */
		public <T> T getJson(Class<T> beanClass, Class<?> elementClass) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			String json = getJson();
			return JsonUtil.jsonToObj(json, beanClass, elementClass);
		}
		
		/**
		 * Translate JSON to a map (e.g. HashMap, LinkedHashMap, etc.).
		 * @param <T>
		 * @param beanClass
		 * @param keyClass
		 * @param valueClass
		 * @return
		 * @throws IOException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 * @throws NoSuchMethodException
		 */
		public <T> T getJson(Class<T> beanClass, Class<?> keyClass, Class<?> valueClass) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			String json = getJson();
			return JsonUtil.jsonToObj(json, beanClass, keyClass, valueClass);
		}
		
		/**
		 * This is a shortcut for getResponse().getWriter().print(String arg0); 
		 * @param source
		 */
		void print(String source, boolean flush) {
			try { 
				response.getWriter().print(source);
				if (flush)
					response.getWriter().flush();
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		/**
		 * This is a shortcut for getResponse().getWriter().print(String arg0); 
		 * @param source
		 */
		public void print(String source) {
			print(source, false);
		}
		
		/**
		 * This is a shortcut for getResponse().getWriter().println(String arg0); 
		 * @param source
		 */
		void println(String source, boolean flush) {
			try { 
				response.getWriter().println(source);
				if (flush)
					response.getWriter().flush();
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		/**
		 * This is a shortcut for getResponse().getWriter().println(String arg0); 
		 * @param source
		 */
		public void println(String source) {
			println(source, false);
		}
		
		
		/**
		 * Converts an object to JSON and prints it to output.  It also sets the content type to "application/json".
		 * @param object
		 */
		public void printJson(Object object) {
			try { printJson(JsonUtil.objToJson(object)); }
			catch (Exception e) {
				Log.logException(e, userid);
				print("'Invalid JSON conversion.  See logs.'"); 
			}
		}
		
		/**
		 * Prints json to output.  It also sets the content type to "application/json".
		 * Please note that this function does <b><i>not</i></b> validate that json is valid JSON notation.
		 * @param json
		 */
		public void printJson(String json) {
			response.setContentType("application/json");
			print(json);
		}
				
		/**
		 * This adds an error message to the error message stack that will be processed by
		 * the Portlet or Message tags.
		 * @param message
		 */
		public void putError(String message) {
			String error = (String)request.getAttribute("_error");
			error = (error == null) ? message : error + "\n" + message;
			request.setAttribute("_error", error);
		}
		/**
		 * This adds an warning message to the warning message stack that will be processed by
		 * the Portlet or Message tags.
		 * @param message
		 */
		public void putWarning(String message) {
			String warning = (String)request.getAttribute("_warning");
			warning = (warning == null) ? message : warning + "\n" + message;
			request.setAttribute("_warning", warning);
		}
		/**
		 * This adds an info message to the info message stack that will be processed by
		 * the Portlet or Message tags.
		 * @param message
		 */
		public void putInfo(String message) {
			String info = (String)request.getAttribute("_info");
			info = (info == null) ? message : info + "\n" + message;
			request.setAttribute("_info", info);
		}
		
		public int getPostErrorCount() {
			Integer postErrorCount = (Integer)request.getAttribute("_ActionServlet.PostErrorCount");
			return postErrorCount == null ? 0 : postErrorCount;
		}
		
		public boolean isMobile() { return request.getHeader("User-Agent").indexOf("Mobi") != -1; }
		
		public String getCsrfToken() { return request.getAttribute(CSRF_TOKEN).toString(); }
		
		public void printCsrfToken() {
			response.setContentType("text/plain");
			print(getCsrfToken());
		}
	}
	
	// ParameterKey ================================================================================
	
	protected class ParameterKey {
		String scope;
		String bean;
		String name;
		String index;
		
		public ParameterKey(String key, String beanName) {
			int p = key.indexOf(".");
			String[] scopes = {REQUEST, SESSION, APPLICATION, CONTEXT,
					           "requestScope", "sessionScope", "applicationScope"};
			String segment = (p < 0) ? key : key.substring(0, p);
			if (Data.in(segment, scopes)) {
				scope = segment.replace("Scope", "");
				key = (p < 0) ? "" : key.substring(p + 1);
				p = key.indexOf(".");
				segment = (p < 0) ? key : key.substring(0, p);
			}
			if (segment.equals(beanName)) {
				bean = segment;
				key = (p < 0) ? "" : key.substring(p + 1);
			}
			else if (segment.startsWith(beanName + "[")) {
				int i = segment.indexOf("[");
				int e = segment.indexOf("]", i);
				bean = segment.substring(0, i);
				index = segment.substring(i + 1, e);
				key = (p < 0) ? "" : key.substring(p + 1);
			}
			else if (scope != null) bean = "";
			name = key;
		}
		
		public String getScope() { return scope; }
		public String getBean() { return bean; }
		public String getName() { return name; }
		public String getIndex() { return index; }
	}

	// ParamKeyComparator ==========================================================================
	
	protected class ParamKeyComparator implements Comparator<String> {
		private String procIndex(String source) {
			StringBuilder result = new StringBuilder("");
			int c = 0;
			int p = source.indexOf("[");
			while (p > -1) {
				int e = source.indexOf("]", p + 1);
				StringBuilder i = new StringBuilder(source.substring(p + 1, e));
				boolean isNumeric = false;
				try { 
					Integer.parseInt(i.toString()); 
					isNumeric = true; 
				} catch (Exception ex) {
					// Eat it.
				} 
				if (isNumeric) 
					while (i.length() < 9) 
						i.append("0" + i);
				result.append(source.substring(c, p) + "[" + i + "]");
				c = e + 1;
				p = source.indexOf("[", c);
			}
			return result.toString();
		}
		
		public ParamKeyComparator() {
			// I don't remember why this is empty.
		}
		
		@Override
		public int compare(String arg0, String arg1) {
			String new0 = procIndex(arg0);
			String new1 = procIndex(arg1);
			return new1.compareTo(new0);
		}
	}
	
	// ActionServlet ===============================================================================
	
	protected ActionServlet() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String,String> checkFormats(HttpServletRequest request) {
		Map<String,String> formats = (HashMap<String,String>)request.getSession().getAttribute(FORMATS);
		if (formats == null) {
			formats = new HashMap<>();
			formats.put(Data.FMT_DATE, Data.ISO_DATE);
			formats.put(Data.FMT_TIME, Data.ISO_TIME);
			formats.put(Data.FMT_DATETIME, Data.ISO_DATETIME);
			request.getSession().setAttribute(FORMATS, formats);
		}
		return formats;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(GET, request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(POST, request, response);
	}
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(PUT, request, response);
	}
	
	protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(PATCH, request, response);
	}
	
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(DELETE, request, response);
	}
	
	protected boolean authorized(String[] roles, String[] methods, String httpMethod, HttpServletRequest request, HttpServletResponse response, String action) throws IOException {
		boolean dbFound = true;
		try { 
			new Portal(); 
		}
		catch (SQLException e) {
			Log.logException(e, null);
			dbFound = false; 
		}
		boolean loggedIn = request.getUserPrincipal() != null;
		boolean userAllowed = roles.length == 0 || userHasRole(roles, request);
		boolean methodAllowed = methods.length == 0 || Data.in(httpMethod, methods);
		if (dbFound && userAllowed && methodAllowed)
			return true;
		String userid = loggedIn ? request.getUserPrincipal().getName() : null;
		int[] responseCodes = {401, 403, 405, 599};
		String target = (action != null) ? "action named \"" + action + "\"" : "servlet";
		String[] warningMessages = {"[ActionServlet.handleRequest] WARNING: An anonymous user tried to access the " + target + ", which is role-protected.  Responded with 401.",
                                    "[ActionServlet.handleRequest] WARNING: The user does not have a valid role for the " + target + "'.  Responded with 403.",
                                    "[ActionServlet.handleRequest] WARNING: A request method for the " + target + " is not allowed'.  Responded with 405.",
                                    "[ActionServlet.handleRequest] WARNING: The configuration database cound not be found when accessing the " + target + "'.  Responded with 599."};
		int responseIndex = !dbFound ? 3 : !methodAllowed ? 2 : loggedIn ? 1 : 0;
		Log.logWarning(warningMessages[responseIndex], userid);
		response.sendError(responseCodes[responseIndex]);
		return false;
	}
	
	private static Cookie findCookie(String name, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
			for (Cookie cookie : cookies)
				if (cookie.getName().equals(name))
					return cookie;
		return null;
	}
	
	private static Cookie genXsrfCookie() {
		String token = UUID.randomUUID().toString();
		Cookie result = new Cookie(XSRF_TOKEN, token);
		result.setHttpOnly(true);
		result.setSecure(true);
		result.setMaxAge(-1);
		result.setPath("/");
		return result;
	}
	
	protected static void expireXsrfCookie(ActionConnection conn) {
		Cookie cookie = findCookie(XSRF_TOKEN, conn.getRequest());
		if (cookie != null)
			cookie.setMaxAge(1);
	}
	
	private static String getSsoSessionId(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
			for (Cookie cookie : cookies)
				if ("JSESSIONIDSSO".equals(cookie.getName()))
					return cookie.getValue();
		return null;
	}
	
	private static Cookie getSessionXsrfCookie(HttpServletRequest request) {
		String ssoSessionId = getSsoSessionId(request);
		Cookie result = SsoXsrfMap.getCookie(ssoSessionId);
		if (result == null) {
			result = genXsrfCookie();
			SsoXsrfMap.setCookie(ssoSessionId, result);
		}
		return result;
	}
	
	private static String encodeCsrfToken(String token) {
		return token == null ? null : Data.encrypt(token, token.hashCode());
	}
	
	private static String decodeCsrfToken(String token, int seed) {
		try {
			return token == null ? null : Data.decrypt(token, seed);
		} catch (Exception e) {
			Log.logDebug("Invalid seed used for token decode.", null, "decodeCsrfToken");
			return null;
		}
	}
	
	private static boolean compareXsrfTokens(Cookie sessionToken, Cookie requestToken, String paramToken) {
		Log.logDebug("compareXsrfTokens\n" +
				     "  sessionToken: " + sessionToken.getValue() + "\n" +
	                 "  requestToken: " + requestToken.getValue() + "\n" +
				     "    paramToken: " + decodeCsrfToken(paramToken, sessionToken.getValue().hashCode()) + "\n", null, "compareXsrfTokens");
		if (!sessionToken.getValue().equals(requestToken.getValue()))
			return false;
		return sessionToken.getValue().equals(decodeCsrfToken(paramToken, sessionToken.getValue().hashCode()));
	}

	private static void cacheMultiPartForm(HttpServletRequest request) {
		if (request.getAttribute(ACTION_SERVLET_FILES) != null)
			return;
		DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(16 * KILOBYTE);
        FileUpload upload = new FileUpload();
        upload.setFileItemFactory(factory);
        try {
			request.setAttribute(ACTION_SERVLET_FILES, upload.parseRequest(new ServletRequestContext(request)));
		} catch (FileUploadException e) {
			Log.logException(e, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<FileItem> getCachedMultiPartForm(HttpServletRequest request) {
		return (List<FileItem>)request.getAttribute(ACTION_SERVLET_FILES);
	}
	
	private static String getMultiPartParam(String name, HttpServletRequest request) {
		List<FileItem> formItems = getCachedMultiPartForm(request);
        for (FileItem item : formItems)
        	if (item.isFormField() && item.getFieldName().equals(name))
        		return item.getString();
        return null;
	}
	
	private static String getCsrfParam(HttpServletRequest request) {
		return request.getContentType().startsWith(FileUpload.MULTIPART) 
			   ? getMultiPartParam(CSRF_TOKEN, request) 
			   : request.getParameter(CSRF_TOKEN);
	}
	
	public static boolean processXsrf(String httpMethod, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// XSRF/CSRF token
		Log.logDebug("Session Context Creation Time: " + request.getSession().getCreationTime(), null, PROCESS_XSRF);
		Cookie sessionXsrf = getSessionXsrfCookie(request);
		Log.logDebug("Session XSRF: " + sessionXsrf.getValue(), null, PROCESS_XSRF);
		
		// Evaluate incoming XSRF/CSRF token
		Cookie requestXsrf = request.getUserPrincipal() == null ? null : findCookie(XSRF_TOKEN, request);
		Log.logDebug("Request XSRF: " + (requestXsrf == null ? "null" : requestXsrf.getValue()), null, PROCESS_XSRF);
		if (Data.in(httpMethod, new String[] {POST, PUT, PATCH, DELETE}) && requestXsrf != null 
				&& !compareXsrfTokens(sessionXsrf, requestXsrf, getCsrfParam(request))) { 
				Log.logError("[ActionServlet.handleRequest] ERROR: XSRF-TOKEN failure.\n    Request URI: " + request.getRequestURI() + "\n    Request Method: " + httpMethod + "\n    Responded with 400.", null, new Exception("XSRF-TOKEN failure."));
				response.sendError(400);
				return false;
		}
		
		// Create outgoing XSRF/CSRF token
		Cookie responseXsrf = sessionXsrf;
		String encToken = encodeCsrfToken(responseXsrf.getValue());
		response.addCookie(responseXsrf);
		request.setAttribute(CSRF_TOKEN, encToken);
		return true;
	}
	
	private boolean isMobile(HttpServletRequest request) {
		return request.getHeader("User-Agent").indexOf("Mobi") != -1;
	}
	
	private String withContext(String path, HttpServletRequest request) {
		return path.startsWith("/") ? path : addContextPath(request, path);
	}
	
	private String withContext(String path, HttpServletRequest request, String format) {
		return path.startsWith("/") ? path : String.format(format, request.getServletPath(), path);
	}
	
	/**
	 * Get the sub-action path.
	 * @param actionName
	 * @param simple
	 * @param request
	 * @return
	 * @throws ServletException
	 */
	private String getSubActionPath(String actionName, SimpleAction simple, HttpServletRequest request) throws ServletException {
		String path = null;
		if (isMobile(request) && !simple.pathMobi().isEmpty())
			path = withContext(simple.pathMobi(), request);
		else if (!simple.path().isEmpty())
			path = withContext(simple.path(), request);
		else if (!simple.action().isEmpty()) {
			path = withContext(simple.action(), request, "%s/%s");
	    	request.setAttribute(CALLING_ACTION_NAME, actionName);
	    }
    	else
    		throw new ServletException(String.format(PATH_REQUIRED, simple.name()));
		return path;
	}
	
	/**
	 * Check to see if a @Navigation annotation exist.  If so see if any @SimpleAction annotations match the action.<br/>
	 * Returns true if a path was found, false otherwise. 
	 * @param actionName
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private boolean processNavigation(String actionName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Class<? extends ActionServlet> cls = getClass();
		while (cls != ActionServlet.class) {
			Navigation nav = cls.getAnnotation(Navigation.class);
			if (nav != null)
				for (SimpleAction simple : nav.simpleActions())
					/* If a matching SimpleAction is found, then do the forward and skip the rest of 
					   this function. -- Unless it defines a bean sub-action call. */
					if (simple.name().equals(actionName)) {
						String path = getSubActionPath(actionName, simple, request);
						if (path != null) {
			            	getServletContext().getRequestDispatcher(path).forward(request, response);
			            	return true;
			            }
					}
			cls = (Class<? extends ActionServlet>)cls.getSuperclass();
		}
		return false;
	}
	
	/**
	 * Get the method from the invoking object that corresponds to methodName (this was derived from actionName; the post-context path).
	 * @param methodName
	 * @param response
	 * @param userid
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	private Method getActionMethod(String methodName, HttpServletResponse response, String userid) throws IOException, ServletException {
		try {
			return this.getClass().getMethod(methodName, ActionConnection.class);
		}
		catch (Exception e) {
			if (methodName.equals(BEGIN))
				Log.logError("[ActionServlet.handleRequest] WARNING: 'begin' action called but not found.  Responded with 404.", userid, e);
			else
				Log.logError(String.format("[ActionServlet.handleRequest] WARNING: No method found named '%s'.  Responded with 404.", methodName), userid, e);
				// throw new ServletException("No method found named '" + methodName + "'.")
			response.sendError(404);
			return null;
		}
	}
	
	/**
	 * 
	 * @param sourcePath
	 * @param request
	 * @return
	 * @throws ServletException
	 */
	private String getActionPath(String sourcePath, HttpServletRequest request) throws ServletException {
		final String PARAM = "{param:";
		final String ATTR = "{attr:";
		// If the path doesn't start with "/", assume the current context (see function).
		return sourcePath.startsWith(PARAM) ? extractParam(request, sourcePath) :
			   sourcePath.startsWith(ATTR) ? extractAttr(request, sourcePath) :
			   !sourcePath.startsWith("/") ? addContextPath(request, sourcePath) :
			   sourcePath;
	}
	
	/**
	 * Interpret the path to forward to.  The @Forward annotation should have a path and/or pathMobi, or an action.
	 * Exceptions are thrown if none are found or if action and either path or pathMobi are found together.
	 * @param f
	 * @param methodName
	 * @param forwardName
	 * @param request
	 * @return
	 * @throws ServletException
	 */
	private String getForwardPath(Forward f, String methodName, String forwardName, HttpServletRequest request) throws ServletException {
		if ((!f.pathMobi().isEmpty() || !f.path().isEmpty()) && !f.action().isEmpty())
			throw new ServletException("Forward named '" + forwardName + "' is malformed.  Either path and/or pathMobi or action may be set, not both.");
		// Else, if mobile and pathMobi is defined, then forward to it.
		else if (isMobile(request) && !f.pathMobi().isEmpty())
			return getActionPath(f.pathMobi(), request);
		// Else, if only a path is defined, then forward to it.
		else if (!f.path().isEmpty())
			// If the path doesn't start with "/", assume the current context (see function).
			return getActionPath(f.path(), request);
		// Otherwise, if only an action is defined, then forward to it.
		else if (!f.action().isEmpty()) {
			request.setAttribute(CALLING_ACTION_NAME, methodName);
			return withContext(f.action(), request, "%s/%s");
		}
		// However, if neither path nor action is defined, then something is wrong... 
		else
			throw new ServletException("Forward named '" + forwardName + "' is malformed.  A path or action is required.");
	}	
	
	/**
	 * Handle the request.  This is called from all HTTP method handlers and handles user role and HTTP method security. 
	 * @param httpMethod
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void handleRequest(String httpMethod, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setHeader("X-Frame-Options", "DENY");
		checkFormats(request);
		Object sslId = request.getAttribute("jakarta.servlet.request.ssl_session_id");
		RothPrincipal user = (RothPrincipal)request.getUserPrincipal();
		if (user != null && user.getSslId() == null)
			user.setSslId(Data.obj2Str(sslId));
		
		// Evaluate whether the servlet should be accessed in the first place.
		ActionServletSecurity sec = getClass().getAnnotation(ActionServletSecurity.class);
		
		if (Data.nvl(request.getContentType()).startsWith(FileUpload.MULTIPART))
			cacheMultiPartForm(request);
		if (!processXsrf(httpMethod, request, response) ||
			((sec != null) && !authorized(sec.roles(), new String[] {}, httpMethod, request, response, null))) {
			return;
		}
		
		/* -- GET ACTION NAME -- */
		// Look for the actionName on the request URI or as a parameter; if neither then default to "begin".
		String actionName = Data.nvl(servletSubPath(request), Data.nvl(request.getParameter("_action"), BEGIN));
		// Check for a sub-path (this may be data added to the path).
	    if ((actionName != null) && actionName.contains("/")) {
	    	int slash = actionName.indexOf("/");
	    	request.setAttribute("__subpath", actionName.substring(slash + 1));
	    	actionName = actionName.substring(0, slash);
	    }
	    /* If a @Navigation annotation exists, and a sub-action path found in the @SimpleAction list,
	       then it will be forwarded to, and this method should return immediately. */
	    if (processNavigation(actionName, request, response))
	    	return;
	    
		/* -- GET METHOD -- */
		// Find the method for actionName.
		String userid = new ActionConnection(request, response).getUserName();
		Object inv = this;
		String methodName = actionName;
		Method method = getActionMethod(methodName, response, userid);
		
		/* -- GET ACTION ANNOTATION -- */
		// If a method is found, find the Action annotation for it.
		Action action = null;
		if (method != null) action = method.getAnnotation(Action.class);
		else return; // If method is null, then this is returning a 404.
		if (action == null) throw new ServletException("Method named '" + methodName + "' is not an action method.  Please provide an Action annotation.");
		
		// See if there is any security on this method.
		MethodSecurity security = method.getAnnotation(MethodSecurity.class);
		if ((request.getUserPrincipal() == null) && (request.getParameter("a_action") != null) && request.getParameter("a_action").equals("login")) {
			try { request.login(request.getParameter("a_username"), request.getParameter("a_password")); }
			catch (Exception e ) { Log.logError("[ActionServlet.handleRequest] WARNING: Authorization failed for username '" + request.getParameter("a_username") + "' and password '" + request.getParameter("a_password") + "'.", userid, e); }
		}
		// If security exists, evaluate it.
		if ((security != null) && !authorized(security.roles(), security.methods(), httpMethod, request, response, methodName)) 
			return;

		ActionConnection conn = new ActionConnection(request, response); 
		
		/* -- GET POST ANNOTATION -- */
		// If an Action annotation is found, find a Post annotation for the method, if one exists.
		Post post = method.getAnnotation(Post.class);
		// If a Post annotation is found, bind post data to the specified bean(s).
		if (post != null) 
			try { doBind(post, conn); }
			catch (Exception e) { Log.logException(e, userid); }
		
		/* -- EXECUTE ACTION & FORWARD -- */
		try { 
			String result = method.invoke(inv, conn).toString();
			/* exportOverride is used when a dataGrid has requested a data export.  This allows any servlet
			 * context to be referenced for an export, as this will override the current servlet and forward
			 * to the export servlet.  This requires com.roth.servlet.ExportServlet to be registered in the 
			 * host servlet's context application web.xml (each context root that host data grids must register this).
			 */
			boolean exportOverride = Data.nvl(request.getParameter("__j_dataGrid_exportOverride")).equals("true");
			boolean forwardOverride = result.startsWith("forward:"); 
			String path = exportOverride ? "/Export" : forwardOverride ? result.substring(8) : getForwardResponsePath(action, result, methodName, conn);
			if (path == null)
				return;
			setForward(path, conn);			
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t instanceof Exception ct)
				handleException(actionName, ct, conn);
			else
				Log.log("THROWN: " + t.getClass().getCanonicalName(), "Unhandled throwable: " + t.getMessage(), "ActionServlet.handleRequest", null, true, t);
		}
		catch (Exception e) { 
			handleException(actionName, e, conn);
		}
	}
	
	/**
	 * Set a forward on the request dispatcher for the path given.
	 * @param path
	 * @param conn
	 * @throws ServletException
	 * @throws IOException
	 */
	private void setForward(String path, ActionConnection conn) throws ServletException, IOException {
		getServletContext().getRequestDispatcher(path).forward(conn.getRequest(), conn.getResponse());
	}
	
	/**
	 * Get the path associated with name.  This may be a @Forward or a @Response.  If a @Forward is named,
	 * then the associated path is returned, otherwise if a @Response is named, then null is returned.  If
	 * neither a @Forward nor a @Response are found for that name, then an exception is thrown. 
	 * @param action
	 * @param name
	 * @param methodName - used to set the calling action name
	 * @param conn
	 * @return
	 * @throws ServletException
	 */
	private String getForwardResponsePath(Action action, String name, String methodName, ActionConnection conn) throws ServletException {
		Forward f = findForward(action.forwards(), name);
		if (f == null) {
			Response r = findResponse(action.responses(), name);
			if (r == null) 
				throw new ServletException(String.format(FORWARD_RESPONSE_NOT_FOUND, name));
			else {
				conn.getResponse().setStatus(r.httpStatusCode());
				if (!r.message().isEmpty())
					conn.print(r.message(), true);
				return null;
			}
		}
		return getForwardPath(f, methodName, name, conn.getRequest());
	}
	
	/**
	 * Checks a list of roles against the roles associated with the users.  An unauthenticated user 
	 * has the pseudo-role "Anonymous".  If any roles present in the roles parameter are also present 
	 * in the user's roles then true is returned, otherwise false is returned. 
	 * @param roles
	 * @param request
	 * @return
	 */
	private boolean userHasRole(String[] roles, HttpServletRequest request) {
		for (String role : roles) {
			boolean isAnonymous = role.equalsIgnoreCase(ANONYMOUS) && request.getUserPrincipal() == null;
			if (isAnonymous || request.isUserInRole(role)) 
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the bean from scope attributes using the name and scope 
	 * specified by the postToBean annotation object.  If the bean
	 * is not found, then one is instantiated. 
	 * @param bean the annotation object that defines the name, class, and scope of the bean
	 * @param request the request object through which the scope attributes are accessed
	 * @return the bean
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	private static Object getBean(Bean bean, HttpServletRequest request) throws IllegalAccessException, InstantiationException, ServletException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Object obj = null;
		
		// Get the bean from the specified scope attributes.
		if (bean.scope().equalsIgnoreCase(REQUEST))
			obj = request.getAttribute(bean.name());
		else if (bean.scope().equalsIgnoreCase(SESSION))
			obj = request.getSession().getAttribute(bean.name());
		else if (bean.scope().equalsIgnoreCase(APPLICATION) ||
				bean.scope().equalsIgnoreCase(CONTEXT))
			obj = request.getSession().getServletContext().getAttribute(bean.name());
		
		// If not found, instantiate the bean.
		if ((obj == null) && (bean.typeClass() == Object.class))
			obj = bean.beanClass().getConstructor().newInstance();
		else if (obj == null)
			obj = bean.typeClass().getConstructor().newInstance();
		else if ((bean.typeClass() == Object.class) && (obj.getClass() != bean.beanClass()))
			throw new ServletException(String.format(CLASS_DOES_NOT_MATCH, bean.name()));
		
		return obj;
	}
	
	/**
	 * Adds the bean to scope attributes using the name and scope 
	 * specified by the postToBean annotation object.   
	 * @param obj the bean
	 * @param bean the annotation object that defines the name and scope of the bean
	 * @param request the request object through which the scope attributes are accessed
	 */
	private static void putBean(Object obj, Bean bean, HttpServletRequest request) {
		if (bean.scope().equalsIgnoreCase(REQUEST))
			request.setAttribute(bean.name(), obj);
		else if (bean.scope().equalsIgnoreCase(SESSION))
			request.getSession().setAttribute(bean.name(), obj);
		else if (bean.scope().equalsIgnoreCase(APPLICATION) ||
				bean.scope().equalsIgnoreCase(CONTEXT))
			request.getSession().getServletContext().setAttribute(bean.name(), obj);
	}
	
	/**
	 * Compares two strings to see if they are equal, or if a is null.
	 * @param a
	 * @param b
	 * @return true if the two strings are equal or a is null.
	 */
	private boolean nncomp(String a, String b) {
		return (a == null) || a.equals(b);
	}
	
	/**
	 * Bind any applicable request parameter values to the specified beans.
	 * @param post the post object that defines the bean(s) to bind the parameters to 
	 * @param request the request from which to read the parameter values
	 * @throws ParseException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doBind(Post post, ActionConnection conn) throws ParseException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ServletException {
		String[] paramKeys = conn.getParamKeys();
		// Sort parameter keys in reverse order.
		// The ParamKeyComparator left-zero-pads numeric indexes for proper comparison.
		Arrays.sort(paramKeys, new ParamKeyComparator());
		
		// Loop through beans defined in Post annotation.
		for (int b = 0; b < post.beans().length; b++) {
			Object obj = getBean(post.beans()[b], conn.getRequest());
			bindObject(obj, post.beans()[b].beanClass(), post.beans()[b].name(), post.beans()[b].scope(), conn, paramKeys);
			putBean(obj, post.beans()[b], conn.getRequest());
		}
	}
	
	/**
	 * Sort the pKeys (parameter keys).  If the pKeys parameter is null, then the request's parameter
	 * map keys are used.
	 * @param pKeys
	 * @param request
	 * @return
	 */
	private String[] getSortedParamKeys(String[] pKeys, HttpServletRequest request) {
		String[] paramKeys = pKeys != null ? pKeys.clone() : request.getParameterMap().keySet().toArray(new String[] {});
		Arrays.sort(paramKeys, new ParamKeyComparator());
		return paramKeys;
	}
	
	/**
	 * Determines whether the binder method should skip the parameter.<br/>
	 * Reasons to skip:<br/>
	 * - The parameter name starts with an underscore.<br/>
	 * - The scopes don't match<br/>
	 * - The names don't match.<br/>
	 * - The object is a List, and the scope is null or the index is null.<br/>
	 * @param param
	 * @param pk
	 * @param name
	 * @param scope
	 * @param obj
	 * @return
	 */
	private boolean shouldSkip(String param, ParameterKey pk, String name, String scope, Object obj) {
		return param.startsWith("_") || 
				!nncomp(pk.getScope(), scope) || 
				!nncomp(pk.getBean(), name) || 
				((obj instanceof java.util.List) && (pk.getScope() == null) && (pk.getIndex() == null));
	}
	
	/**
	 * Prepare an indexed object for binding.<br/><br/>
	 * If the parameter key is indexed, then it should be an array, list, or map.  Make sure that the object is
	 * valid.  If a list or map, and the index doesn't exist (i.e. the list or map doesn't already contain that
	 * index), then the list or map will be updated with a new instance of the bean at the specified index.  
	 * In the case of a list, and the index is beyond the list size, then the list will be padded with nulls 
	 * until the index is valid.  If an array, and the index is out of bounds, then an exception will occur 
	 * (this is why parameter names are sorted backwards for processing).
	 * @param obj
	 * @param index
	 * @param beanClass
	 * @param pk
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@SuppressWarnings({ "unchecked" })
	private Object prepareIndexedObject(Object obj, Class<?> beanClass, ParameterKey pk) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String index = pk.getIndex();
		// Array : integer index
		if (obj.getClass().isArray())
			return ((Object[])obj)[Integer.parseInt(index)];
		// List : integer index
		if (obj instanceof java.util.List ciobj) {
			int x = Integer.parseInt(index);
			while (ciobj.size() < (x + 1)) 
				ciobj.add(null);
			if (ciobj.get(x) == null) 
				ciobj.set(x, beanClass.getConstructor().newInstance());
			return ciobj.get(x);
		}
		// Map : String key ("index")
		if (obj instanceof java.util.Map ciobj) {
			ciobj.put(index, beanClass.getConstructor().newInstance());
			return ciobj.get(pk.getIndex());
		}
		// None of the above : return as-is
		return obj;
	}
	
	/**
	 * Bind relevant parameters to the supplied object.
	 * @param obj
	 * @param beanClass
	 * @param name
	 * @param scope
	 * @param conn
	 * @param pKeys
	 * @throws ParseException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected void bindObject(Object obj, Class<?> beanClass, String name, String scope, ActionConnection conn, String[] pKeys) throws ParseException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		StringBuilder errors = new StringBuilder("Errors were encountered during servlet parameter bind to object class (" + beanClass.getCanonicalName() + "):\n");
		int errorCount = 0;
		// Loop through the parameters.
		for (String param : getSortedParamKeys(pKeys, conn.getRequest())) {
			
			ParameterKey pk = new ParameterKey(param, name);
			if (shouldSkip(param, pk, name, scope, obj))
				continue;
			Object iobj = obj;
			if (pk.getIndex() != null)
				iobj = prepareIndexedObject(iobj, beanClass, pk);

			try {
				//if (conn.isFileParam(param))
				//	setRefObject(iobj, param, pk.getName(), conn.getFileParam(param), null, checkFormats(conn.getRequest()), conn);
				//else
				//	setRefObject(iobj, param, pk.getName(), conn.getParameter(param), conn.getParameterValues(param), checkFormats(conn.getRequest()), conn);
				setRefObject(iobj, param, pk.getName(), checkFormats(conn.getRequest()), conn);
			}
			catch (NumberFormatException e) {
				errors.append("        NumberFormatException - " + param + "=" + conn.getParameter(param) + "\n");
				errorCount++;
			}
			catch (ParseException e) {
				errors.append("        ParseException - " + param + "=" + conn.getParameter(param) + "\n");
				errorCount++;
			}
		}
		conn.getRequest().setAttribute("_ActionServlet.PostErrorCount", errorCount);
		if (errorCount > 0) 
			Log.logError(errors.toString() + "    ", Data.nvl(getUserName(conn), ANONYMOUS), null);
	}
	
	/**
	 * Recursively traverses a parameter reference chain, and sets the value
	 * at the end.  This will instantiate any null references in the chain to 
	 * ensure that the object at the end can be reached.
	 * @param obj the parent object
	 * @param origref the original reference (for error reporting)
	 * @param reference the current reference to evaluate
	 * @param value the value to set at the end of the reference chain
	 * @throws NullPointerException
	 * @throws IndexOutOfBoundsException
	 * @throws ParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	// This assumes that the reference does not contain scope or bean name.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setRefObject(Object object, String origref, String reference, Map<String,String> formats, ActionConnection conn) throws NullPointerException, IndexOutOfBoundsException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Object obj = object;
		// Split the reference chain into individual object references.
		String[] refs = reference.split("\\.");
		// Split out the first reference into name and index
		int p = refs[0].indexOf("[");
		String name = (p < 0) ? refs[0] : refs[0].substring(0, p);
		String index = (p < 0) ? null : refs[0].substring(p + 1, refs[0].indexOf("]")).replace("\"", "").replace("'", "");
		// Get the declared methods, so that we can search for getters and/or setters later.
		Method[] methods = obj.getClass().getDeclaredMethods();
		// Prepare evaluation variables.
		boolean isArray = false;
		boolean isList = false;
		boolean isMap = false;
		Class<?> keyType = null;
		Class<?> compType = null;
		
		// If this is an array, list, or map...
		if (index != null) {
			// Get the referenced object from 'object'.
			String getterName = "get" + Data.upcaseFirst(name);
			Method method = findGetterMethod(methods, getterName);
			obj = method.invoke(obj);
			
			// If the referenced object is null, then get the object's type
			// and initialized it.
			if (obj == null) {
				Class c = method.getReturnType();

				if (c.getComponentType() != null)
					obj = Array.newInstance(c.getComponentType(), Integer.parseInt(index) + 1);
				else
					obj = c.getConstructor().newInstance();
				
				method = findSetterMethod(methods, "set" + Data.upcaseFirst(name));
				method.invoke(object, obj);
			}
			
			// Check to see if it is an array, list, or map.
			isArray = obj.getClass().isArray();
			isList = obj instanceof java.util.List;
			isMap = obj instanceof java.util.Map;
			
			// Get the component type of the object.
			if (isArray) 
				compType = obj.getClass().getComponentType();
			else if (isList)
				compType = getListCompType(method.getGenericReturnType());
			else if (isMap) {
				keyType = getMapKeyType(method.getGenericReturnType());
				compType = getMapCompType(method.getGenericReturnType());
			}
			else 
				compType = null;
			
			// If no component type was found, then throw an exception,
			// because and index was referenced inappropriately.
			if (compType == null)
				throw new ParseException("Member identified by \"" + name + "\" in reference \"" + origref + "\" cannot be processed.", 0);
			
			// Get the methods from the new object.
			methods = obj.getClass().getDeclaredMethods();
		}
		
		// If this is the end of the object chain, then set the object...
		if (refs.length == 1) {
			// If the object is an array, list, or map, then set the value
			if (index != null) {
				Object o = Data.getParamValue(compType, conn.getParameter(origref), formats);
				Object k = Data.getParamValue(keyType, index, formats);
				
				if (isArray) 
					((Object[])obj)[Integer.parseInt(index)] = compType.cast(o);
				else if (isList) {
					java.util.List l = (java.util.List)obj;
					int x = Integer.parseInt(index);
					while (l.size() < (x + 1)) l.add(null);
					l.set(x, o);
				}
				else if (isMap)
					((java.util.Map)obj).put(k, o);
			}
			// Otherwise set the atomic value.
			else {
				String setterName = "set" + Data.upcaseFirst(name);
				Method method = findSetterMethod(methods, setterName);
				if ((method != null) && (!method.isAnnotationPresent(ReadOnly.class))) {
					compType = method.getParameterTypes()[0];
					if (compType.isArray() && !compType.getCanonicalName().equals("byte[]"))
						method.invoke(obj, (Object)conn.getParameterValues(origref));
					else if (compType.getCanonicalName().equals("byte[]")) {
						Multipart multipart = method.getAnnotation(Multipart.class);
						if (multipart != null) {
							FileItem file = conn.findCachedFile(origref);
							Log.logDebug("File size: " + file.getSize(), null, "multipart");
							if (file != null && file.getSize() > 0) {
								Log.logDebug("Setting file.", null, "multipart");
								method.invoke(obj, file.get());
								if (multipart.filename() != null) {
									String fileSetter = "set" + Data.upcaseFirst(multipart.filename());
									Method fileMethod = findSetterMethod(methods, fileSetter);
									if (fileMethod != null)
										fileMethod.invoke(obj, file.getName());
								}
								if (multipart.mimeType() != null) {
									String fileSetter = "set" + Data.upcaseFirst(multipart.mimeType());
									Method fileMethod = findSetterMethod(methods, fileSetter);
									String mimeType = Data.getMimeType(file.get());
									boolean allowed = false;
									for (String a : multipart.allowed()) {
										allowed = a.equals("*") ||
												  a.startsWith("*") && a.endsWith("*") ? mimeType.contains(a.substring(1, a.length() - 1))
												: a.startsWith("*") ? mimeType.endsWith(a.substring(1))
												: a.endsWith("*") ? mimeType.startsWith(a.substring(0, a.length() - 1))
												: mimeType.equals(a);
										if (allowed)
											break;
									}
									if (!allowed)
										throw new IllegalArgumentException("The uploaded file does not have an allowed mime type.");
									if (fileMethod != null)
										fileMethod.invoke(obj, mimeType); 
								}
							}
						}
					}
					else {
						Object o = Data.getParamValue(compType, conn.getParameter(origref), formats);
						method.invoke(obj, o);
					}
				}
			}
		}
		// Otherwise traverse further down the chain.
		else {
			// Get the new reference, which is the portion of the reference 
			// chain that is downstream from the current reference.
			String newreference = reference.substring(reference.indexOf(".") + 1);
			Object k = Data.getParamValue(keyType, index, formats);
			// If the object is an array, list or map, then get the indexed value.
			Object o = (isArray) ? ((Object[])obj)[Integer.parseInt(index)]
			         : (isList) ? ((java.util.List)obj).get(Integer.parseInt(index))
					 : (isMap) ? ((java.util.Map)obj).get(k)
					 : null;
					 
			// If the object is not an array, list, or map, then use the getter
			// to retrieve the object.
			if ((o == null) && (index == null)) {
				String getterName = "get" + Data.upcaseFirst(name);
				Method method = findGetterMethod(methods, getterName);
				o = method.invoke(obj);
				
				// If the getter returned null, then instantiate the object defined
				// by the return type of the getter, and then use the setter to 
				// apply it to the parent object.
				if (o == null) {
					try { o = method.getReturnType().getConstructor().newInstance(); }
					catch (Exception e) { Log.logException(e, null); }
					Method m = findSetterMethod(methods, "set" + Data.upcaseFirst(name));
					m.invoke(obj, o);
				}
			}
			
			// Recurse into the new reference.
			setRefObject(o, origref, newreference, formats, conn);
		}
	}
	
	/**
	 * Get the type(s) specified in the parameter of a parameterized type.
	 * @param source the parameterized type
	 * @return the type(s) specified in the parameter
	 */
	private String getBracketedType(String source) {
		String c = source;
		int s = c.indexOf('<');
		int e = c.indexOf('>');
		// Return everything between the "<" and ">".
		return (s < 0) ? null : c.substring(s + 1, e).replace("?", "java.lang.String");
	}
	
	/**
	 * Get a List's component type.
	 * @param t the declared type of the List
	 * @return the component type of the List
	 */
	private Class<?> getListCompType(Type t) {
		String c = getBracketedType(t.toString());
		try { return Class.forName(Data.nvl(c, "java.lang.String")); }
		catch (ClassNotFoundException ex) { return java.lang.String.class; }
	}
		
	/**
	 * Get a Map's key type.
	 * @param t the declared type of the Map
	 * @return the key type of the Map
	 */
	private Class<?> getMapKeyType(Type t) {
		String c = Data.nvl(getBracketedType(t.toString()), "java.lang.String,java.lang.String").split(",")[0].trim();
		try { return Class.forName(c); }
		catch (ClassNotFoundException ex) { return java.lang.String.class; }
	}
	
	/**
	 * Get a Map's component type.
	 * @param t the declared type of the Map
	 * @return the component type of the Map
	 */
	private Class<?> getMapCompType(Type t) {
		String c = Data.nvl(getBracketedType(t.toString()), "java.lang.String,java.lang.String").split(",")[1].trim();
		try { return Class.forName(c); }
		catch (ClassNotFoundException ex) { return java.lang.String.class; }
	}
	
	/**
	 * Find a method that matches the getterName, and no parameters.
	 * @param methods
	 * @param setterName
	 * @return
	 */
	private Method findGetterMethod(Method[] methods, String getterName) {
		for (int i = 0; i < methods.length; i++)
			if ((methods[i].getName().equals(getterName)) &&
				(methods[i].getParameterTypes().length == 0))
				return methods[i];
		
		return null;
	}
	
	/**
	 * Find a method that matches the setterName, and receives one and only one parameter.
	 * @param methods
	 * @param setterName
	 * @return
	 */
	private Method findSetterMethod(Method[] methods, String setterName) {
		for (int i = 0; i < methods.length; i++)
			if ((methods[i].getName().equals(setterName)) &&
				(methods[i].getParameterTypes().length == 1) &&
				(methods[i].getReturnType().equals(Void.TYPE)))
				return methods[i];
		
		return null;
	}
	
	/**
	 * <b>findForward</b><br><br>
	 * Searches through an array of Forward annotations for one with the specified <i>name</i>. 
	 * @param forwards - The array of Forward annotations to search.
	 * @param name - The name of the Forward annotation to search for.
	 * @return the <i>path</i> of the Forward annotation whose name is specified, or <b>null</b> if not found.
	 */
	private Forward findForward(Forward[] forwards, String name) {
		if (forwards != null)
			for (int i = 0; i < forwards.length; i++)
				if (forwards[i].name().equals(name))
					return forwards[i];
		
		return null;
	}
	
	/**
	 * <b>findResponse</b><br><br>
	 * Searches through an array of Response annotations for one with the specified <i>name</i>. 
	 * @param responses - The array of Response annotations to search.
	 * @param name - The name of the Response annotation to search for.
	 * @return the <i>path</i> of the Response annotation whose name is specified, or <b>null</b> if not found.
	 */
	private Response findResponse(Response[] responses, String name) {
		if (responses != null)
			for (int i = 0; i < responses.length; i++)
				if (responses[i].name().equals(name))
					return responses[i];
		
		return null;
	}
	
	/**
	 * <b>addContextPath</b><br><br>
	 * Prefixes the servlet's context path to a path.<br><b>Note:</b> the servlet's path is altered to lower-case the first letter.
	 * @param request - The request to derive servlet's the context path from.
	 * @param path - The path to prefix.
	 * @return the prefixed path.
	 */
	private String addContextPath(HttpServletRequest request, String path) {
		// The current context is the servlet path with a lower-cased first letter.
		// If the @Navigation.contextPath is specified, then it is prepended.
		Navigation nav = this.getClass().getAnnotation(Navigation.class);
		String navContext = nav != null ? nav.contextPath() : "";
		boolean override = navContext.endsWith("/");
		if (override) navContext = navContext.substring(0, navContext.length() - 1);
		String servletPath = override ? "" : request.getServletPath().toLowerCase();
		return Data.enforceStart(String.format("%s%s/%s", navContext, servletPath, path), "/");
	}
	
	private static String extractParam(HttpServletRequest request, String param) throws ServletException {
		int b = param.indexOf("{param:");
		int i = param.indexOf('}');
		if (i < 0) throw new ServletException("Improperly encoded parameter encoding encountered.  Cannot extract parameter.");
		String name = param.substring(b + 7, i).trim();
		return request.getParameter(name);
	}
	
	private static String extractAttr(HttpServletRequest request, String attr) throws ServletException {
		int b = attr.indexOf("{attr:");
		int i = attr.indexOf('}');
		if (i < 0) throw new ServletException("Improperly encoded attribute encoding encountered.  Cannot extract attribute.");
		String name = attr.substring(b + 7, i).trim();
		return (String)request.getAttribute(name);
	}
	
	/**
	 * <b>servletSubPath</b><br><br>
	 * Removes the servlet's context path from the request URI.
	 * @param request - The request from which to retrieve the URI.
	 * @return any sub-path left after removing the servlet's context path.
	 */
	private String servletSubPath(HttpServletRequest request) {
		String path = request.getContextPath() + request.getServletPath();
		if (request.getRequestURI().equals(path)) return null;
		String result = request.getRequestURI().replaceAll(path + "/", "").trim();
		if (result.endsWith("/")) result = result.substring(0, result.length() - 1).trim();
		return (result.length() > 0) ? result : null;
	}
	
	protected String getSubpath(ActionConnection conn) {
		return (String)conn.getRequest().getAttribute("__subpath");
	}
	
	/**
	 * Gets the bean defined at <i>index</i> in the @Post annotation. 
	 * @param <T>
	 * @param index the index of the bean in the @Post annotation (starting at 0)
	 * @param conn the ActionConnection passed to the action
	 * @return the bean that was defined by the indexed @Bean annotation.
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getBean(int index, ActionConnection conn) {
		try {
			StackTraceElement[] e = Thread.currentThread().getStackTrace();
			String callingMethodName = e[2].getMethodName();
			Method callingMethod = getClass().getMethod(callingMethodName, ActionConnection.class);
			Post post = callingMethod.getAnnotation(Post.class);
			Bean bean = post.beans()[index];
			return (T)getBean(bean, conn.getRequest());
		}
		catch (Exception e) { return null; }
	}
	
	public static <T> T getBean(String name, ActionConnection conn) {
		return getBean(name, REQUEST, conn);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name, String scope, ActionConnection conn) {
		if (scope.equalsIgnoreCase(REQUEST))
			return (T)conn.getRequest().getAttribute(name);
		else if (scope.equalsIgnoreCase(SESSION))
			return (T)conn.getRequest().getSession().getAttribute(name);
		else if (scope.equalsIgnoreCase(APPLICATION) || scope.equalsIgnoreCase(CONTEXT))
			return (T)conn.getRequest().getSession().getServletContext().getAttribute(name);
		else
			return null;
	}
	
	/**
	 * Places an object on the request scope.<br/>
	 * This is equivalent to <code style="color: blue;">conn.getRequest().setAttribute(name, bean);</code>
	 * @param bean
	 * @param name
	 * @param conn
	 */
	public static void putBean(Object bean, String name, ActionConnection conn) {
		putBean(bean, name, REQUEST, conn);
	}
	
	/**
	 * Places an object on the specified scope.<br/>
	 * This is equivalent to:<br/>
	 * - if scope = REQUEST then <code style="color: blue;">conn.getRequest().setAttribute(name, bean);</code><br/>
	 * - if scope = SESSION then <code style="color: blue;">conn.getRequest().getSession().setAttribute(name, bean);</code><br/>
	 * - if scope = APPLICATION or CONTEXT then<br/>
	 * &nbsp; <code style="color: blue;">conn.getRequest().getServletContext().setAttribute(name, bean);</code>
	 * @param bean
	 * @param name
	 * @param scope
	 * @param conn
	 */
	@SuppressWarnings("all")
	public static void putBean(Object bean, String name, String scope, ActionConnection conn) {
		class BeanClass implements Bean {
			private String _name;
			private String _scope;
			public BeanClass(String name, String scope) { _name = name; _scope = scope; }
			@Override
			public String name() { return _name; }
			@Override
			public String scope() { return _scope; }
			@Override
			public Class<? extends Annotation> annotationType() { return null; }
			@Override
			public Class<?> beanClass() { return null; }
			@Override
			public Class<?> typeClass() { return Object.class; }
		}
		putBean(bean, new BeanClass(name, scope), conn.getRequest());
	}
	
	protected static String getCookie(ActionConnection conn, String name, String defaultValue) {
		Cookie[] cookies = conn.getRequest().getCookies();
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			if (name.equals(cookie.getName())) return cookie.getValue();
		}
		return defaultValue;
	}
	
	protected static void setCookie(ActionConnection conn, String name, String value) {
		conn.getResponse().addCookie(new Cookie(name, value));
	}
	
	protected static String getUserName(ActionConnection conn) {
		return getUserName(conn.getRequest());
	}
	
	private static String getUserName(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) return null;
        return request.getUserPrincipal().getName();
    }
	
	/** 
	 * Deprecated
	 * @param conn
	 * @param method
	 * @return
	 */
	protected String getLogLoc(ActionConnection conn, String method) {
	    return getLogLoc(conn.getRequest(), method);
	}
	
	/**
	 * Deprecated
	 * @param request
	 * @param method
	 * @return
	 */
	private String getLogLoc(HttpServletRequest request, String method) {
        return getUserName(request) + " @ " +
               request.getContextPath().replace("/", "") + "/" + 
               this.getClass().getCanonicalName() + "." + method;
    }
	
	/**
	 * A calling action is an action which forwards to another action.  If the action in which
	 * this method is called was forwarded to by another action, then that other action name
	 * will be returned.  Otherwise null will be returned.
	 * @param conn
	 * @return the calling action name, if applicable.
	 */
	protected static String getCallingActionName(ActionConnection conn) {
	    return (String)conn.getRequest().getAttribute(CALLING_ACTION_NAME);
	}
	
	/**
	 * If the calling action name is equal to <i>action</i>, then true is returned; false otherwise.
	 * @param action
	 * @param conn
	 * @return
	 */
	protected static boolean isCallingActionName(String action, ActionConnection conn) {
		return Data.nvl(getCallingActionName(conn)).equals(action);
	}
	
	/**
	 * This method logs the exception, sends the exception message on the response, and returns <i>returnValue</i>.
	 * @param e
	 * @param conn
	 * @param returnValue
	 * @return
	 */
	protected static String returnLogException(Exception e, ActionConnection conn, String returnValue) {
		return returnLogException(e, conn, returnValue, null, false);
	}
	
	/**
	 * This method logs the exception, sends <i>returnMessage</i> on the response, and returns <i>returnValue</i>.
	 * @param e
	 * @param conn
	 * @param returnValue
	 * @param returnMessage
	 * @return
	 */
	protected static String returnLogException(Exception e, ActionConnection conn, String returnValue, String returnMessage) {
		return returnLogException(e, conn, returnValue, returnMessage, false);
	}
	
	/**
	 * This method logs the exception, sends <i>returnMessage</i> on the response, and returns <i>returnValue</i>.
	 * @param e
	 * @param conn
	 * @param returnValue
	 * @param returnMessage
	 * @param includeException
	 * @return
	 */
	protected static String returnLogException(Exception e, ActionConnection conn, String returnValue, String returnMessage, boolean includeException) {
		Log.logException(e, conn.getUserName());
		conn.println(returnMessage == null ? e.getMessage() : returnMessage + (includeException ? e.getMessage() : ""));
		return returnValue;
	}
	
	/**
	 * This method handles exceptions thrown from or not handled by actions.
	 * This method logs the exception, sets the response httpStatusCode to 500, and sends the exception message on the response.
	 * To customize, override this function.
	 * @param actionName
	 * @param exception
	 * @param conn
	 */
	protected void handleException(String actionName, Exception exception, ActionConnection conn) {
		if (!conn.getResponse().isCommitted())
			conn.getResponse().reset();
		conn.getResponse().setStatus(exception instanceof MalformedURLException ? 400 : 500);
		Log.logException(exception, conn.getUserName(), actionName);
		conn.println(exception.getMessage());
	}
}
