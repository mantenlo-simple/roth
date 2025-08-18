<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.roth.base.util.Data"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<%
	try {
		pageContext.setAttribute("status", response.getStatus());
		String uri = Data.sanitize((String)request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI), true);
		pageContext.setAttribute("uri", uri);
		String query = Data.sanitize((String)request.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING), false);
		query = query == null ? "" : "?" + query;
		pageContext.setAttribute("query", query);
	}
	catch (java.net.MalformedURLException e) {
		if (response.getStatus() != 400)
			response.sendError(400);
	}
	catch (java.io.FileNotFoundException e) {
		if (response.getStatus() != 404)
			response.sendError(404);
	}
%>

<r:page themeId="${param['themeId']}">
	<style type="text/css">
        .errorJsp { text-align: center; }
    </style>
	<div class="errorJsp">
	    <c:if test="${status eq 401}">
		    <script type="text/javascript">
		        addEvent(window, 'load', function() { doAuthentication('${uri}${query}'); });
		    </script>
	    </c:if>
	    <br/><br/>
	    <span style="font-size: 20px; font-weight: bold;">
	        HTTP Status ${status} ${status eq 400 ? 'Malformed Request' :
	                                status eq 401 ? 'Unauthorized' :
	                                status eq 403 ? 'Forbidden' :
	                                status eq 404 ? 'Not Found' :
	                                status eq 405 ? 'Method Not Allowed' :
	                                status eq 500 ? 'Internal Server Error' :
	                                status eq 599 ? 'Database Not Found' : ''}<br/>
	    </span>
	    <br/>
	    <span style="font-size: 16px;">
	        Description: ${status eq 400 ? 'The request was malformed.' :
	                       status eq 401 ? 'The requested resource requires authentication to access.' :
	                       status eq 403 ? 'Access to the requested resource is denied.' :
	                       status eq 404 ? 'The requested resource cannot be found.' :
	                       status eq 405 ? 'The method used is not allowed for the requested resource.' :
	                       status eq 500 ? 'An unspecified error has occurred.' :
	                       status eq 599 ? 'The configuration database cannot be found.' : ''}
	    </span><br/>
	    <br/><br/>
	</div>
</r:page>