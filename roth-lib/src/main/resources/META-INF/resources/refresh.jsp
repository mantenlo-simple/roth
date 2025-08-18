<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<% com.roth.servlet.ActionServlet.processXsrf("GET", request, response); %>
{
	"csrfToken": "<%= org.apache.commons.text.StringEscapeUtils.escapeJson(request.getAttribute("_csrf-token").toString()) %>"
}
