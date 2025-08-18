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
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<c:set var="params" value="roleName=${param['roleName']}"/>
<r:tabset>
    <r:option caption="Role" value="roles" pageId="roledata" iconName="shield-alt" selected="true"/>
    <c:if test="${!empty param['roleName']}">
        <r:option caption="Domains" value="domains" pageId="roledomains" iconName="globe" action="/Role/loadDomains?${params}"/>
        <r:option caption="Users" value="users" pageId="roleusers" iconName="user" action="/Role/loadUsers?${params}"/>
        <r:option caption="Groups" value="groups" pageId="rolegroups" iconName="users" action="/Role/loadGroups?${params}"/>
        <c:if test="${rf:isUserInRole(pageContext, 'PortalAdminRole')}">
	        <r:option caption="Portlets" value="portlets" pageId="roleportlets" iconName="plug" action="/Role/loadPortlets?${params}"/>
	    </c:if>
	    <r:option caption="Properties" value="properties" pageId="roleproperties" iconName="list" action="/Role/loadProperties?${params}"/>
    </c:if>
</r:tabset> 

<c:if test="${!rf:mobile(pageContext)}">
	<div class="jbreak" style="height: 6px;"></div>
</c:if>

<div id="roledata" style="width: ${rf:mobile(pageContext) ? '100%' : '41em'};">
    <jsp:include page="__roledata.jsp"/>
</div>

<div id="roledomains" style="width: ${rf:mobile(pageContext) ? '100%' : '41em'}; display: none;">
    Role Domains
</div>

<div id="roleusers" style="width: ${rf:mobile(pageContext) ? '100%' : '45em'}; display: none;">
    Role Users
</div>

<div id="rolegroups" style="width: ${rf:mobile(pageContext) ? '100%' : '45em'}; display: none;">
    Role Groups
</div>

<div id="roleportlets" style="width: ${rf:mobile(pageContext) ? '100%' : '41em'}; display: none;">
    Role Portlets
</div>

<div id="roleproperties" style="width: ${rf:mobile(pageContext) ? '100%' : '41em'}; display: none;">
    Role Properties
</div>

<div class="jbreak"></div>