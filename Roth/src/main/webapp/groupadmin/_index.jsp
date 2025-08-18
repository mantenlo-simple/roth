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
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:jspSecurity rolesAllowed="GroupAdmin"/>
<c:set var="mobi" value="${rf:mobile(pageContext)}"/>

<r:tabset>
    <r:option caption="Users" iconName="user" value="users" pageId="userlist" selected="true"/>
    <%-- <r:option caption="Groups" iconName="group" value="groups" pageId="grouplist"/> --%>
</r:tabset>

<c:if test="${!rf:mobile(pageContext)}">
	<r:break height="8"/>
</c:if>

<div id="dauserlist" style="${mobi ? 'height: calc(100% - 5.4em);' : ''}"><jsp:include page="_users.jsp"/></div>
<div id="dagrouplist" style="display: none;"><jsp:include page="_groups.jsp"/></div>

<r:break/>