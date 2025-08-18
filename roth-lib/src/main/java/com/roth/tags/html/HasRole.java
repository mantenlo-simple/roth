package com.roth.tags.html;

import com.roth.base.util.Data;
import com.roth.tags.el.User;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

public class HasRole extends TagSupport {
	private static final long serialVersionUID = -7888455346299216555L;

	public void setVar(String varName) { setValue("var", varName); }
	public void setRole(String role) { setValue("role", role); }
	
	public int doEndTag() throws JspException {
		String varName = getValue("var").toString();
		String role = getValue("role").toString();
		String[] roles = Data.nvl(role).split(",");
		boolean hasRole = false;
		for (String roleName : roles)
			if (User.isUserInRole(pageContext, roleName))
				hasRole = true;
		pageContext.setAttribute(varName, hasRole);
		return super.doEndTag();
	}
}