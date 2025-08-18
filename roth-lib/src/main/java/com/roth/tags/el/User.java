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

import java.sql.SQLException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;

import com.roth.base.log.Log;
import com.roth.portal.util.Portal;

/**
 * 
 * @author James M. Payne
 *
 */
public class User {
	/**
	 * Gets the domain name from the user principal.
	 * @param pageContext the JSTL <i>'pageContext'</i> implicit object
	 * @return the domain name if logged in, null otherwise
	 */
	public static String getDomainName(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		if (request.getUserPrincipal() == null) return null;
		String userid = request.getUserPrincipal().getName();
		int dPos = userid.indexOf("@");
        String _domainName = dPos < 0 ? "default" : userid.substring(dPos + 1);
		return _domainName;
	}
	/**
	 * Gets the user id from the user principal.
	 * @param pageContext the JSTL <i>'pageContext'</i> implicit object
	 * @return the user id if logged in, null otherwise
	 */
	public static String getUserId(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		if (request.getUserPrincipal() == null) return null;
		String userid = request.getUserPrincipal().getName();
		int dPos = userid.indexOf("@");
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        return _userid;
	}
	/**
	 * Gets the user name from the user principal.
	 * @param pageContext the JSTL <i>'pageContext'</i> implicit object
	 * @return the user name if logged in, null otherwise
	 */
	public static String getUserName(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		return (request.getUserPrincipal() == null) ? null : request.getUserPrincipal().getName();
	}
	public static String getUserFullName(PageContext pageContext) {
		try { return new Portal().getUserFullName(getUserName(pageContext)); }
		catch (SQLException e) {
			Log.logException(e, getUserName(pageContext));
			return "";
		}
	}
	/**
	 * Checks to see if the currently logged in user has the specified role.
	 * @param pageContext the JSTL <i>'pageContext'</i> implicit object 
	 * @param roleName the name of the role to check for
	 * @return true if the user has the role, false otherwise
	 */
	public static boolean isUserInRole(PageContext pageContext, String roleName) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		try { return request.isUserInRole(roleName); }
		catch (Exception e) {
			Log.logError("[com.roth.tags.el.User.isUserInRole] ERROR: Unable to call 'request.isUserInRole'.", request.getUserPrincipal().getName(), e);
			return false;
		}
	}
}
