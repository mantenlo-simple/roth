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
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<c:set var="params" value="userid=${param['userid']}&domainId=${param['domainId']}"/>
<%-- <r:tabset onSelect="Roth.tabset.getPage(this, 'userid=${param['userid']}&domainId=${param['domainId']}')"> --%>
<r:tabset>
    <r:option caption="User" value="users" pageId="userdata" iconName="user" selected="true" />
    <c:if test="${!empty param['userid']}">
        <%-- <r:option caption="Domains" value="domains" pageId="userdomains" iconName="globe" action="/User/loadDomains"/> --%>
        <r:option caption="Groups" value="groups" pageId="usergroups" iconName="users" action="/User/loadGroups?${params}"/>
        <r:option caption="Roles" value="roles" pageId="userroles" iconName="shield-alt" action="/User/loadRoles?${params}"/>
        <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
            <r:option caption="Properties" value="properties" pageId="userproperties" iconName="list" action="/User/loadProperties?${params}"/>
        </c:if>
    </c:if>
</r:tabset> 

<c:if test="${!rf:mobile(pageContext)}">
	<r:break height="0.5em"/>
</c:if>

<div id="userdata" style="width: ${rf:mobile(pageContext) ? '100%' : '536px'};">
    <jsp:include page="__userdata.jsp"/>
</div>

<div id="userdomains" style="width: ${rf:mobile(pageContext) ? '100%' : '40.5em'}; display: none;">
    Loading user domains...
</div>

<div id="usergroups" style="width: ${rf:mobile(pageContext) ? '100%' : '40.5em'}; display: none;">
    Loading user groups...
</div>

<div id="userroles" style="width: ${rf:mobile(pageContext) ? '100%' : '40.5em'}; display: none;">
    Loading user roles...
</div>

<div id="userproperties" style="width: ${rf:mobile(pageContext) ? '100%' : '40.5em'}; display: none;">
    Loading user properties...
</div>

<div class="jbreak"></div>