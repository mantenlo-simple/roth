package com.roth.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.base.util.Data.Pad;
import com.roth.jdbc.annotation.ConnectionDataSource;
import com.roth.jdbc.model.EnhancedBean;
import com.roth.jdbc.model.Insertable;
import com.roth.jdbc.model.StateBean;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.model.BookBean;
import com.roth.portal.model.DesktopBean;
import com.roth.portal.model.PageBean;
import com.roth.portal.model.PortletBean;
import com.roth.portal.model.PortletRoleBean;
import com.roth.portal.model.RoleBean;
import com.roth.portal.model.ThemeBean;
import com.roth.portal.model.ThemeLinkBean;
import com.roth.portal.model.ThemeOverrideBean;

import jakarta.servlet.ServletContext;

@ConnectionDataSource(jndiName = "roth")
final class RothContextUtil extends TableUtil {
	private static final long serialVersionUID = -6147188822968162293L;
	
	private static final String MESSAGE = "MESSAGE: ";
	private static final String ORIGIN = "com.roth.servlet.RothContextUtil";
	private static final String SYSTEM = "[SYSTEM]";
	
	RothContextUtil() throws SQLException { }

	private static final String[] PROTECTED_ROLES = 
		{"Developer", "DomainAdmin", "ExternalUser", "GroupAdmin", "InternalUser", 
		 "manager-gui", "NewsAdmin", "PortalAdmin", "SecurityAdmin", "SystemAdmin"};
	
	
	void logDelete(String reference) {
		Log.log(MESSAGE, String.format("    Removing %s", reference), ORIGIN, SYSTEM, false, null);
	}
	
	void logInsert(String reference) {
		Log.log(MESSAGE, String.format("      Adding %s", reference), ORIGIN, SYSTEM, false, null);
	}
	
	void logUpdate(String reference) {
		Log.log(MESSAGE, String.format("    Updating %s", reference), ORIGIN, SYSTEM, false, null);
	}
	
	private static Map<Class<? extends EnhancedBean>, String[]> classMap;
	static {
		classMap = new HashMap<>();
		classMap.put(RoleBean.class, new String[] { "role_name" });
		classMap.put(PortletBean.class, new String[] { "portlet_name" });
		classMap.put(BookBean.class, new String[] { "book_name" });
		classMap.put(PageBean.class, new String[] { "book_id", "portlet_id" });
		classMap.put(ThemeBean.class, new String[] { "theme_name" });
		classMap.put(DesktopBean.class, new String[] { "desktop_name" });
	}
	
	private static final String ROLENAME = "roleName";
	private static final String PORTLETNAME = "portletName";
	private static final String BOOKNAME = "bookName";
	private static final String PAGETITLE = "pageTitle";
	private static final String DESKTOPNAME = "desktopName";
	
	private boolean isRemove(EnhancedBean bean) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String className = bean.getClass().getName().replace("com.roth.portal.model.", "");
		return switch(className) {
		case "RoleBean" -> getValue(ROLENAME, bean).startsWith("-");
		case "PortletBean" -> getValue(PORTLETNAME, bean).startsWith("-");
		case "BookBean" -> getValue(BOOKNAME, bean).startsWith("-");
		case "PageBean" -> getValue(BOOKNAME, bean).startsWith("-");
		case "DesktopBean" -> getValue(DESKTOPNAME, bean).startsWith("-");
		default -> false;
		};
	}
	
	private void cleanRemove(EnhancedBean bean) throws IllegalArgumentException {
		if (bean instanceof RoleBean role)
			role.setRoleName(absValue(role.getRoleName()));
		else if (bean instanceof PortletBean portlet)
			portlet.setPortletName(absValue(portlet.getPortletName()));
		else if (bean instanceof BookBean book)
			book.setBookName(absValue(book.getBookName()));
		else if (bean instanceof PageBean page) {
			page.setBookName(absValue(page.getBookName()));
			page.setPortletName(absValue(page.getPortletName()));
		}
		else if (bean instanceof DesktopBean desktop)
			desktop.setDesktopName(absValue(desktop.getDesktopName()));
	}
	
	private static final String FORMAT1 = "%s ( %s )";
	private static final String FORMAT2 = "%s ( %s, %s, %s )";
	
	private String getReference(EnhancedBean bean) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String className = bean.getClass().getName().replace("com.roth.portal.model.", "");
		return switch(className) {
		case "RoleBean" -> String.format(FORMAT1, "role", getValue(ROLENAME, bean));
		case "PortletBean" -> String.format(FORMAT1, "portlet", getValue(PORTLETNAME, bean));
		case "BookBean" -> String.format(FORMAT1, "book", getValue(BOOKNAME, bean));
		case "PageBean" -> String.format(FORMAT2, "page", getValue(BOOKNAME, bean), getValue(PORTLETNAME, bean), getValue(PAGETITLE, bean));
		case "DesktopBean" -> String.format(FORMAT1, "desktop", getValue(DESKTOPNAME, bean));
		default -> null;
		};
	}
	
	private boolean isRemove(String value) {
		return value.startsWith("-");
	}
	
	private String absValue(String value) {
		return isRemove(value) ? value.substring(1) : value;
	}
	
	private String getValue(String name, EnhancedBean bean) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method getter = Data.getDeclaredMethod(bean.getClass(), Data.getGetterName(name));
		return (String) getter.invoke(bean);
	}
	
	private Long getLongValue(String name, EnhancedBean bean) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method getter = Data.getDeclaredMethod(bean.getClass(), Data.getGetterName(name));
		return (Long) getter.invoke(bean);
	}
	
	private String getFilter(String name, EnhancedBean bean) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return applyParameters(String.format("%s = {1}", name), getValue(name, bean));
	}
	
	private String getLongFilter(String name, EnhancedBean bean) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return applyParameters(String.format("%s = {1}", name), getLongValue(name, bean));
	}
	
	private boolean isProtected(EnhancedBean bean) {
		if (bean instanceof RoleBean role && role.getRoleName().startsWith("-"))
			return Data.in(role.getRoleName().substring(1), PROTECTED_ROLES);
		return false;
	}
	
	void processBean(EnhancedBean bean) throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		boolean remove = isRemove(bean);
		cleanRemove(bean);
		fullLookup(bean);
		String[] refs = classMap.get(bean.getClass());
		EnhancedBean existing = null;
		String filter = null;
		if (refs.length == 1)
			filter = getFilter(refs[0], bean);
		else {
			StringBuilder filterBuilder = new StringBuilder("");
			for (String ref : refs)
				filterBuilder.append(filterBuilder.isEmpty() ? "" : " AND ").append(getLongFilter(ref, bean));
			filter = filterBuilder.toString();
		}
		existing = get(bean.getClass(), filter);
		if (remove) {
			doRemove(existing);
			return;
		}
		existing = doSave(bean, existing, filter);
		processBeanRelationships(existing);
	}
	
	private void doRemove(EnhancedBean bean) throws SQLException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (bean == null || isProtected(bean))
			return;
		logDelete(getReference(bean));
		delete(bean);
	}
	
	private EnhancedBean doSave(EnhancedBean bean, EnhancedBean existing, String filter) throws SQLException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String reference = getReference(bean);
		if (existing == null) {
			logInsert(reference);
			if (bean instanceof Insertable ibean)
				ibean.prepare();
			insert(bean);
			return get(bean.getClass(), filter);
		}
		EnhancedBean temp = existing.copy();
		existing.merge(bean);
		if (!existing.equals(temp)) {
			logUpdate(reference);
			update(existing);
			return existing;
		}
		return existing;
	}
	
	private void processBeanRelationships(EnhancedBean bean) throws SQLException {
		if (bean instanceof PortletBean portlet) {
			execUpdate(applyParameters("DELETE FROM portlet_role WHERE portlet_id = {1}", portlet.getPortletId()));
			List<StateBean> list = new ArrayList<>();
			if (portlet.getRoles() != null)
				for (String roleName : portlet.getRoles()) {
					PortletRoleBean rbean = new PortletRoleBean();
					rbean.setPortletId(portlet.getPortletId());
					rbean.setRoleName(roleName);
					rbean.setCreatedBy(portlet.getUpdatedBy());
					rbean.setCreatedDts(portlet.getUpdatedDts());
					list.add(rbean);
				}
			insert(list);
		}
	}
	
	void processTheme(String filename, ServletContext servletContext) throws SQLException {
		if (filename.startsWith("-")) {
			ThemeBean existing = get(ThemeBean.class, applyParameters("theme_name = {1}", absValue(filename)));
			if (existing != null) {
				logDelete(String.format("theme ( %s )", Data.pad(existing.getThemeName(), ' ', 20, Pad.RIGHT)));
				delete(existing);
			}
			return;
		}
		String staticTheme = Data.readTextFile(servletContext, "/WEB-INF/" + filename, StandardCharsets.UTF_8);
		ArrayList<ThemeLinkBean> links = new ArrayList<>();
		ArrayList<ThemeOverrideBean> overrides = new ArrayList<>();
		ThemeBean theme = new ThemeBean();
		try {
			theme.setStaticTheme(staticTheme, links, overrides, this);
		} catch (Exception e) {
			Log.logException(e, null);
		}
		ArrayList<Object> toInsert = new ArrayList<>();
		toInsert.add(theme);
		toInsert.addAll(links);
		toInsert.addAll(overrides);
		delete(theme); // If it already exists, delete it to be overwritten.
		logInsert(String.format("theme ( %s )", Data.pad(theme.getThemeName(), ' ', 20, Pad.RIGHT)));
		insert(toInsert);
	}
}
