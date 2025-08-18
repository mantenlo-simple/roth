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
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:jspSecurity rolesAllowed="PortalAdmin,SecurityAdmin,DomainAdmin"/>

<r:portlet servletPath="/configuration/index.jsp" themeId="${requestScope.previewThemeId}">
<%-- 
	<link rel="stylesheet" href="${contextRoot}/codemirror/codemirror.css">
	<script src="${contextRoot}/codemirror/codemirror.js"></script>
	<script src="${contextRoot}/codemirror/xml.js"></script>
	<script src="${contextRoot}/codemirror/javascript.js"></script>
	<script src="${contextRoot}/codemirror/css.js"></script>
	<script src="${contextRoot}/codemirror/vbscript.js"></script>
	<script src="${contextRoot}/codemirror/htmlmixed.js"></script>
--%>
    <c:set var="tabpage" value="${!empty requestScope.tabpage ? requestScope.tabpage : rf:isUserInRole(pageContext, 'SecurityAdmin') or rf:isUserInRole(pageContext, 'DomainAdmin') ? 'user' : 'portlet'}"/>
    <script type="text/javascript">
        <jsp:include page="_index.js"/>
        addEvent(window, 'load', function() { getList('${tabpage}list'); document.getElementById('${tabpage}list').style.display = 'block'; });
        setTimeout(initCodeMirror);
    </script>

    <r:tabset onSelect="Roth.getDialog('wait').wait(); Roth.tabset.getPage(this, '_method=AJAX')">
        <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin') or rf:isUserInRole(pageContext, 'DomainAdmin')}">
            <r:option caption="Domains" value="domains" pageId="domainlist" iconName="globe" action="/Domain/load" selected="${requestScope.tabpage eq 'domain'}"/>
            <r:option caption="Users" value="users" pageId="userlist" iconName="user" action="/User/load" selected="${requestScope.tabpage eq 'user' or empty requestScope.tabpage}"/>
            <r:option caption="Groups" value="groups" pageId="grouplist" iconName="users" action="/Group/load" selected="${requestScope.tabpage eq 'group'}"/>
        </c:if>
        <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
            <r:option caption="Roles" value="roles" pageId="rolelist" iconName="shield-alt" action="/Role/load" selected="${requestScope.tabpage eq 'role'}"/>
        </c:if>
        <c:if test="${rf:isUserInRole(pageContext, 'PortalAdmin')}">
	        <r:option caption="Portlets" value="portlets" pageId="portletlist" iconName="plug" action="/Portlet/load" selected="${requestScope.tabpage eq 'portlet'}"/>
	        <r:option caption="Books" value="books" pageId="booklist" iconName="book" action="/Book/load" selected="${requestScope.tabpage eq 'book'}"/>
	        <r:option caption="Desktops" value="desktops" pageId="desktoplist" iconName="desktop" action="/Desktop/load" selected="${requestScope.tabpage eq 'desktop'}"/>
	        <r:option caption="Themes" value="themes" pageId="themelist" iconName="paint-brush" action="/Theme/load" selected="${requestScope.tabpage eq 'theme'}"/>
	        <r:option caption="Settings" value="settings" pageId="settings" iconName="cog" action="/Setting/load"/>
        </c:if>
    </r:tabset>
    
    <c:if test="${!rf:mobile(pageContext)}">
    	<div class="jbreak" style="height: 1em;"></div>
    </c:if>
    
    <div id="domainlist" style="height: calc(100% - 20px); ${requestScope.tabpage == 'domain' ? '' : 'display: none;'}">
        Loading domains...
    </div>
    <div id="userlist" style="height: calc(100% - 20px); ${requestScope.tabpage == 'user' ? '' : 'display: none;'}">
        Loading users...
    </div>
    <div id="grouplist" style="height: calc(100% - 20px); ${requestScope.tabpage == 'group' ? '' : 'display: none;'}">
        Loading groups...
    </div>
    <div id="rolelist" style="height: calc(100% - 20px); ${requestScope.tabpage == 'role' ? '' : 'display: none;'}">
        Loading roles...
    </div>
    <div id="portletlist" style="height: calc(100% - 20px); ${requestScope.tabpage == 'portlet' ? '' : 'display: none;'}">
        Loading portlets...
    </div>
    <div id="booklist" style="height: calc(100% - 20px); ${requestScope.tabpage == 'book' ? '' : 'display: none;'}">
        Loading books...
    </div>
    <div id="desktoplist" style="height: calc(100% - 20px); ${requestScope.tabpage == 'desktop' ? '' : 'display: none;'}">
        Loading desktops...
    </div>
    <div id="themelist" style="height: calc(100% - 20px); ${requestScope.tabpage == 'theme' ? '' : 'display: none;'}">
        Loading themes...
    </div>
    <div id="settings" style="height: calc(100% - 20px); display: none;">
        Loading settings...
    </div>
    
    <div class="jbreak"></div>
</r:portlet>