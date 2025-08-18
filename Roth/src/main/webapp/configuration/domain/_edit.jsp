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

<c:set var="params" value="domainId=${requestScope.domain.domainId}&domainName=${requestScope.domain.domainName}"/>
<r:tabset>
    <r:option caption="Domain" value="domains" pageId="domaindata" iconName="globe" selected="true"/>
    <c:if test="${!empty requestScope.domain.domainId}">
        <%-- <r:option caption="Users" value="users" pageId="domainusers" iconName="user" action="/Domain/loadUsers"/> --%>
        <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
            <r:option caption="Roles" value="roles" pageId="domainroles" iconName="shield-alt" action="/Domain/loadRoles?${params}"/>
            <r:option caption="Properties" value="properties" pageId="domainproperties" iconName="list" action="/Domain/loadProperties?${params}"/>
        </c:if>
    </c:if>
</r:tabset> 

<c:if test="${!rf:mobile(pageContext)}">
	<r:break height="0.5em"/>
</c:if>

<div id="domaindata" style="width: ${rf:mobile(pageContext) ? '100%' : '606px'};">
    <jsp:include page="__domaindata.jsp"/>
</div>

<div id="domainusers" style="width:  ${rf:mobile(pageContext) ? '100%' : '650px'}; display: none;">
    Domain Users
</div>

<div id="domainroles" style="width:  ${rf:mobile(pageContext) ? '100%' : '650px'}; display: none;">
    Domain Roles
</div>

<div id="domainproperties" style="width:  ${rf:mobile(pageContext) ? '100%' : '650px'}; display: none;">
    Domain Properties
</div>

<div class="jbreak"></div>