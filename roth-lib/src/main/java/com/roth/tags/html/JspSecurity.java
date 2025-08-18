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
package com.roth.tags.html;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.roth.portal.util.Portal;

public class JspSecurity extends TagSupport {
	private static final long serialVersionUID = 1855079332367989479L;
	
	// Attributes
	public void setRolesAllowed(String rolesAllowed) { setValue("rolesAllowed", rolesAllowed); }
	
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
		String rolesAllowed = (String)getValue("rolesAllowed");
		int result = EVAL_PAGE;
		boolean dbFound = true;
		try { new Portal(); }
		catch (SQLException e) { dbFound = false; }
		if (!dbFound) {
			result = SKIP_PAGE;
			try { response.sendError(599, "The configuration database cannot be found"); }
			catch (IOException e) { throw new JspException(e.getMessage()); }
		}
		else if (request.getUserPrincipal() == null) {
			result = SKIP_PAGE;
			try { response.sendError(401, "Access to the requested resource has been denied"); }
			catch (IOException e) { throw new JspException(e.getMessage()); }
		}
		else if (rolesAllowed != null) {
			String[] roles = rolesAllowed.split(",");
			result = SKIP_PAGE;
			for (String role : roles)
				if (request.isUserInRole(role))
					result = EVAL_PAGE;
			if (result == SKIP_PAGE) {
				try { response.sendError(403, "Access to the requested resource has been denied"); }
				catch (IOException e) { throw new JspException(e.getMessage()); }
			}
		}
		release();
		return result;
	}
}
