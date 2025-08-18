package com.roth.tags.html;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

public class Context extends TagSupport {
	private static final long serialVersionUID = -7888455346299216555L;

	public int doEndTag() throws JspException {
		pageContext.setAttribute("contextRoot", pageContext.getServletContext().getContextPath());
		pageContext.setAttribute("portalRoot", Page.getPortalRoot());
		return super.doEndTag();
	}
}