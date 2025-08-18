package com.roth.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.EnhancedBean;
import com.roth.jdbc.model.StateBean;
import com.roth.jdbc.util.TableUtil;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;
import com.roth.servlet.util.BeanEntry;
import com.roth.servlet.util.FormEntry;
import com.roth.servlet.util.GridEntry;
import com.roth.servlet.util.RoleEntry;

import jakarta.servlet.jsp.JspException;

@Navigation(simpleActions = { @SimpleAction(name = "listApi", action = "list"),
		                      @SimpleAction(name = "viewApi", action = "view"),
		                      @SimpleAction(name = "editApi", action = "edit"),
		                      @SimpleAction(name = "saveApi", action = "save"),
		                      @SimpleAction(name = "deleteApi", action = "delete") })
public class BeanServlet extends ActionServlet {
	private static final long serialVersionUID = -2590772201735107523L;

	private static final String API = "api";
	private static final String CODE_OK = "200";
	private static final String CODE_UNAUTHORIZED = "401";
	private static final String CODE_FORBIDDEN = "403";
	
	private transient Map<String, BeanEntry> beanRegistry;
	private transient Map<String, RoleEntry> roleRegistry;
	private transient Map<String, GridEntry> gridRegistry;
	private transient Map<String, String>    viewRegistry;
	private transient Map<String, FormEntry> formRegistry;
	
	protected BeanServlet() {
		super();
		beanRegistry = new HashMap<>();
		roleRegistry = new HashMap<>();
		gridRegistry = new HashMap<>();
		viewRegistry = new HashMap<>();
		formRegistry = new HashMap<>();
	}
	
	protected BeanEntry getBeanEntry(String servletPath) { return beanRegistry.get(servletPath); }
	
	/**
	 * Register a bean class, bean name, and JNDI resource name for a given servletPath.  This is 
	 * required to make the BeanServlet work.
	 * @param servletPath - the path of the servlet mapping (not including the "/*" at the end.
	 * @param jndiName - the name of the JNDI resource used to perform crud operations.
	 * @param beanName - the name of the bean for reference in .jsp files; the list will append the suffix "List".
	 * @param beanClass - a model class that implements the @StateBean interface.
	 */
	protected void registerBean(String servletPath, String jndiName, String beanName, Class<? extends StateBean> beanClass) {
		if (beanRegistry.get(servletPath) != null)
			return;
		JdbcTable table = beanClass.getAnnotation(JdbcTable.class);
		if (table == null)
			throw new IllegalArgumentException("The beanClass must contain a @JdbcTable annotation.");
		if (table.primaryKeyColumns().length != 1)
			throw new IllegalArgumentException("The beanClass' @JdbcTable annotation must contain a primaryKeyColumns value with one and only one column name.");
		String primaryKey = table.primaryKeyColumns()[0];
		String parentKey = Data.envl(table.parentKeyColumn());
		String primaryFilter = primaryKey + " = {1}";
		String parentFilter = parentKey + " = {1}";
		String primarySetter = "set" + Data.upcaseFirst(Data.camelcase(primaryKey));
		beanRegistry.put(servletPath, new BeanEntry(
			beanClass,
			beanName,
			jndiName,
			primaryKey,
			parentKey,
			primaryFilter,
			parentFilter,
			primarySetter
		));
	}
	
	/**
	 * Register CRUD roles for a given servletPath.  This is optional, as roles may also be applied 
	 * using the @ActionServletSecurity annotation, or manually by overriding the doList, doView, 
	 * doEdit, doSave, or doDelete methods.
	 * @param servletPath - the path of the servlet mapping (not including the "/*" at the end.
	 * @param selectRole - the role needed to execute the list or view methods.
	 * @param insertRole - the role needed to open an edit form or save when adding. 
	 * @param updateRole - the role needed to open an edit form or save when updating.
	 * @param deleteRole - the role needed to delete.
	 */
	protected void registerRoles(String servletPath, String selectRole, String insertRole, String updateRole, String deleteRole) {
		roleRegistry.put(servletPath, new RoleEntry(
			selectRole,
			insertRole,
			updateRole,
			deleteRole
		));
	}
	
	/**
	 * Register a grid definition for a given servletPath.
	 * @param servletPath - the path of the servlet mapping (not including the "/*" at the end.
	 * @param grid
	 */
	protected void registerGrid(String servletPath, GridEntry grid) {
		if (gridRegistry.get(servletPath) != null)
			return;
		gridRegistry.put(servletPath, grid);
	}
	
	/**
	 * Register a view definition for a given servletPath.  This is simply a path to a JSP file.<br/>
	 * Example: "/myfolder/myjsp.jsp"
	 * @param servletPath
	 * @param manualJsp
	 */
	protected void registerView(String servletPath, String manualJsp) {
		if (viewRegistry.get(servletPath) != null)
			return;
		viewRegistry.put(servletPath, manualJsp);
	}
	
	/**
	 * Register a form definition for a given servletPath.
	 * @param servletPath - the path of the servlet mapping (not including the "/*" at the end.
	 * @param form
	 */
	protected void registerForm(String servletPath, FormEntry form) {
		if (formRegistry.get(servletPath) != null)
			return;
		formRegistry.put(servletPath, form);
	}
	
	/**
	 * Check to see if the user has the given role.
	 * @param roleName
	 * @param conn
	 * @return
	 */
	private String checkRole(String roleName, ActionConnection conn) {
		boolean authenticated = conn.getRequest().getUserPrincipal() != null;
		boolean hasRole = roleName == null || (authenticated && conn.hasRole(roleName)) || (!authenticated && roleName.equalsIgnoreCase("Anonymous"));
		return hasRole ? CODE_OK : authenticated ? CODE_FORBIDDEN : CODE_UNAUTHORIZED;
	}
	
	/**
	 * Get the servlet path from the request.  This may provide different results depending upon how 
	 * many maps are used. 
	 * @param conn
	 * @return
	 */
	protected String getServletPath(ActionConnection conn) {
		String result = conn.getRequest().getRequestURI().replace(conn.getRequest().getContextPath(), "");
		return result.substring(0, result.lastIndexOf('/'));
	}
	
	/**
	 * Get the query string.  Override this method to manipulate the query string referenced
	 * by the default pages.  This is used to populate the request attribute "queryString".
	 * @param conn
	 * @return
	 */
	protected String getQueryString(ActionConnection conn) {
		return conn.getRequest().getQueryString();
	}
		
	/**
	 * Gather a list of beans to present to a list view.  This method expects the existence of "list.jsp".
	 * @param conn
	 * @return
	 * @throws SQLException
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */
	@Action(forwards = { @Forward(name = SUCCESS, path = "/beanlist.jsp") },
			responses = { @Response(name = API),
					      @Response(name = CODE_UNAUTHORIZED, httpStatusCode = 401),
					      @Response(name = CODE_FORBIDDEN, httpStatusCode = 403) })
	@MethodSecurity(methods = {GET, POST})
	public String list(ActionConnection conn) throws SQLException {
		putBean(getQueryString(conn), "queryString", conn);
		// Get the servlet path and any registry entries related to it.
		String servletPath = getServletPath(conn);
		BeanEntry entry = beanRegistry.get(servletPath);
		RoleEntry roles = roleRegistry.get(servletPath);
		GridEntry grid = gridRegistry.get(servletPath);
		// Check the select role, if it exists.
		String code = checkRole(roles == null ? null : roles.selectRole(), conn);
		if (!CODE_OK.equals(code))
			return code;
		// Look for a parentId, if present in the parameters.
		Long parentId = entry.parentKey() == null ? null : conn.getLong(Data.camelcase(entry.parentKey()));
		// Put the serletPath, beanName, and gridSettings on the request for access if needed later.
		putBean(servletPath, "servletPath", conn);
		putBean(entry.beanName(), "beanName", conn);
		putBean(grid, "gridSettings", conn);
		// Call doList to actually perform the selection of data.
		List<? extends StateBean> list = doList(conn, parentId);
		boolean api = isCallingActionName("listApi", conn);
		if (api)
			conn.printJson(list);
		else
			putBean(list, entry.beanName() + "List", conn);
		return api ? API : grid.getManualJsp() != null ? String.format("forward:%s", grid.getManualJsp()) : SUCCESS;
	}
	
	/**
	 * Perform the gathering of the list of beans.  This may be overridden to customize behavior.
	 * @param conn
	 * @param parentId
	 * @return
	 * @throws SQLException
	 */
	protected List<StateBean> doList(ActionConnection conn, Long parentId) throws SQLException {
		BeanEntry entry = beanRegistry.get(getServletPath(conn));
		TableUtil util = new TableUtil(entry.jndiName());
		String filter = parentId == null ? null : util.applyParameters(entry.parentFilter(), parentId);
		Log.logDebug(filter, null, "doList");
		return util.getList(entry.beanClass(), filter);
	}
	
	/**
	 * Gather a bean to present in a view format.  This method expects the existence of "view.jsp".
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	@Action(responses = { @Response(name = API),
				          @Response(name = CODE_UNAUTHORIZED, httpStatusCode = 401),
				      	  @Response(name = CODE_FORBIDDEN, httpStatusCode = 403) })
	@MethodSecurity(methods = GET)
	public String view(ActionConnection conn) throws SQLException, JspException {
		putBean(getQueryString(conn), "queryString", conn);
		String servletPath = getServletPath(conn);
		BeanEntry entry = beanRegistry.get(servletPath);
		RoleEntry roles = roleRegistry.get(servletPath);
		String code = checkRole(roles == null ? null : roles.selectRole(), conn);
		if (!CODE_OK.equals(code))
			return code;
		Long id = conn.getLong(Data.camelcase(entry.primaryKey()));
		StateBean bean = doView(conn, id);
		boolean api = isCallingActionName("viewApi", conn);
		if (api)
			conn.printJson(bean);
		else
			putBean(bean, entry.beanName(), conn);
		if (api)
			return API;
		String manualJsp = viewRegistry.get(servletPath);
		if (manualJsp == null)
			throw new JspException(String.format("No view is registered for the sevlet path: %s", servletPath));
		return String.format("forward:%s", manualJsp);
	}
	
	/**
	 * Perform the gathering of the bean for view.  This may be overridden to customize behavior.
	 * @param conn
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	protected StateBean doView(ActionConnection conn, Long id) throws SQLException {
		return doEdit(conn, id, null);
	}
	
	/**
	 * Gather a bean for editing.  This method expects the existence of "edit.jsp".
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	@Action(forwards = { @Forward(name = SUCCESS, path = "/beanedit.jsp") },
			responses = { @Response(name = API),
				          @Response(name = CODE_UNAUTHORIZED, httpStatusCode = 401),
						  @Response(name = CODE_FORBIDDEN, httpStatusCode = 403) })
	@MethodSecurity(methods = GET)
	public String edit(ActionConnection conn) throws SQLException {
		putBean(getQueryString(conn), "queryString", conn);
		String servletPath = getServletPath(conn);
		BeanEntry entry = beanRegistry.get(servletPath);
		RoleEntry roles = roleRegistry.get(servletPath);
		FormEntry form = formRegistry.get(servletPath);
		GridEntry grid = gridRegistry.get(servletPath);
		Long id = conn.getLong(Data.camelcase(entry.primaryKey()));
		Long parentId = entry.parentKey() == null ? null : conn.getLong(Data.camelcase(entry.parentKey()));
		String role = roles == null ? null : id == null ? roles.insertRole() : roles.updateRole();
		String code = checkRole(role, conn);
		putBean(servletPath, "servletPath", conn);
		putBean(entry.beanName(), "beanName", conn);
		putBean(form, "formSettings", conn);
		putBean(grid, "gridSettings", conn);
		if (!CODE_OK.equals(code))
			return code;
		StateBean bean = doEdit(conn, id, parentId);
		boolean api = isCallingActionName("editApi", conn);
		if (api)
			conn.printJson(bean);
		else
			putBean(bean, entry.beanName(), conn);
		return api ? API : form.getManualJsp() != null ? String.format("forward:%s", form.getManualJsp()) : SUCCESS;
	}
	
	/**
	 * Perform the gathering of the bean for add/edit.  This may be overridden to customize behavior.
	 * @param conn
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	protected StateBean doEdit(ActionConnection conn, Long id, Long parentId) throws SQLException {
		BeanEntry entry = beanRegistry.get(getServletPath(conn));
		TableUtil util = new TableUtil(entry.jndiName());
		StateBean result = util.get(entry.beanClass(), util.applyParameters(entry.primaryFilter(), id));
		if (result == null && parentId != null) {
			try {
				result = Data.newInstance(entry.beanClass());
				String setterName = Data.getSetterName(Data.camelcase(entry.parentKey()));
				Method setParentId = entry.beanClass().getDeclaredMethod(setterName, Long.class);
				setParentId.invoke(result, parentId);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				Log.logException(e, null);
			}
		}
		return result;
	}
	
	/**
	 * Save a posted bean.  This method will forward to the list method when finished, unless an exception is thrown.
	 * @param conn
	 * @return
	 * @throws SQLException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParseException
	 * @throws IOException 
	 */
	@Action(forwards = { @Forward(name = SUCCESS, action = "list"),
			             @Forward(name = API, action = "listApi") },
			responses = { @Response(name = CODE_UNAUTHORIZED, httpStatusCode = 401),
						  @Response(name = CODE_FORBIDDEN, httpStatusCode = 403) })
	@MethodSecurity(methods = POST)
	public String save(ActionConnection conn) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ParseException, IOException {
		boolean isApi = isCallingActionName("saveApi", conn);
		String servletPath = getServletPath(conn);
		BeanEntry entry = beanRegistry.get(servletPath);
		RoleEntry roles = roleRegistry.get(servletPath);
		Long id = conn.getLong(String.format("requestScope.%s.%s", entry.beanName(), Data.camelcase(entry.primaryKey())));
		String role = roles == null ? null : id == null ? roles.insertRole() : roles.updateRole();
		String code = checkRole(role, conn);
		if (!CODE_OK.equals(code))
			return code;
		Object obj = null;
		if (isApi)
			obj = conn.getJson(entry.beanClass());
		else {
			String[] paramKeys = conn.getParamKeys();
			// Sort parameter keys in reverse order.
			// The ParamKeyComparator left-zero-pads numeric indexes for proper comparison.
			Arrays.sort(paramKeys, new ParamKeyComparator());
			obj = getBean(entry.beanName(), conn);
			if (obj == null)
				obj = Data.newInstance(entry.beanClass());
			bindObject(obj, entry.beanClass(), entry.beanName(), REQUEST, conn, paramKeys);
		}
		try {
			Method setUpdatedBy = entry.beanClass().getDeclaredMethod("setUpdatedBy", String.class);
			setUpdatedBy.invoke(obj, conn.getUserName());
		}
		catch (NoSuchMethodException e) {
			Log.logError(e.getMessage(), conn.getUserName(), e);
		}
		putBean(obj, entry.beanName(), conn);
		doSave(conn);
		return isApi ? API : SUCCESS;
	}
	
	/**
	 * Perform a save of the bean.  This may be overridden to customize behavior.
	 * Use the name options of getBean.
	 * Example of getBean: getBean(beanName, conn);
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	protected void doSave(ActionConnection conn) throws SQLException {
		BeanEntry entry = beanRegistry.get(getServletPath(conn));
		StateBean bean = getBean(entry.beanName(), conn);
		TableUtil util = new TableUtil(entry.jndiName());
		if (bean instanceof EnhancedBean enhanced)
			util.saveWithMerge(enhanced);
		else
			util.save(bean);
	}
	
	/**
	 * Delete the bean associated with the incoming ID (primary key value).
	 * This method will forward to the list method when finished, unless an exception is thrown.
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	@Action(forwards = { @Forward(name = SUCCESS, action = "list"),
            			 @Forward(name = API, action = "listApi") },
			responses = { @Response(name = API),
					      @Response(name = CODE_UNAUTHORIZED, httpStatusCode = 401),
					      @Response(name = CODE_FORBIDDEN, httpStatusCode = 403) })
	@MethodSecurity(methods = GET)
	public String delete(ActionConnection conn) throws SQLException {
		String servletPath = getServletPath(conn);
		BeanEntry entry = beanRegistry.get(servletPath);
		RoleEntry roles = roleRegistry.get(servletPath);
		String code = checkRole(roles == null ? null : roles.deleteRole(), conn);
		if (!CODE_OK.equals(code))
			return code;
		Long id = conn.getLong(Data.camelcase(entry.primaryKey()));
		doDelete(conn, id);
		return isCallingActionName("deleteApi", conn) ? API : SUCCESS;
	}
	
	/**
	 * Perform the delete of the bean associated with the incoming ID (primary key value).
	 * This method may be overridden to customize behavior.
	 * @param conn
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	protected void doDelete(ActionConnection conn, Long id) throws SQLException {
		BeanEntry entry = beanRegistry.get(getServletPath(conn));
		StateBean bean;
		try {
			bean = Data.newInstance(entry.beanClass());
			Method setter = bean.getClass().getDeclaredMethod(entry.primarySetter(), Long.class);
			setter.invoke(bean, id);
			new TableUtil(entry.jndiName()).delete(bean);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			Log.logException(e, conn.getUserName());
		}
	}
}
