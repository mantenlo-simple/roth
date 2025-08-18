<%--
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
 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.roth.base.util.Data"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>

<%
	try {
		String redirect = Data.sanitize(request.getParameter("redirect"), true);
		pageContext.setAttribute("redirect", redirect);
	}
	catch (java.net.MalformedURLException e) {
		response.sendError(400);
	}
	catch (java.io.FileNotFoundException e) {
		response.sendError(404);
	}
%>

<c:set var="redirectCommand">window.top.document.location = '${redirect}';</c:set>
<c:set var="closeCommand">window.top.Roth.getDialog('wait').hide(); 
			window.top.Roth.getDialog('info').flash('You are successfully logged in.<br/>Please resubmit your last request.');
			window.top.setTimeout(() => {
				window.top.updateCsrf();
				window.top.Roth.getDialog('login').hide(); 
			});</c:set>
<c:set var="command" value="${empty redirect ? closeCommand : redirectCommand}"/>
<!DOCTYPE html>
<html>
    <head>
    <meta charset="UTF-8">
    <meta http-equiv="Cache-Control" content="no-store,no-cache,must-revalidate"> 
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Expires" content="-1">
    <script type="text/javascript">
		function doload() {
			${command}
		}
    </script>
    </head>
    <body onload="doload();">
    </body>
</html>