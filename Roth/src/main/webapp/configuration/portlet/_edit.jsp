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

<c:set var="params" value="portletId=${requestScope.portlet.portletId}&portletName=${requestScope.portlet.portletName}"/>
<r:tabset>
    <r:option caption="Portlet" value="portlets" pageId="portletdata" iconName="plug" selected="true" />
    <c:if test="${!empty requestScope.portlet.portletId}">
        <r:option caption="Roles" value="roles" pageId="portletroles" iconName="shield-alt" action="/Portlet/loadRoles?${params}"/>
    </c:if>
</r:tabset> 

<div class="jbreak" style="height: 6px;"></div>

<div id="portletdata" style="width: ${rf:mobile(pageContext) ? '100%' : '840px'};">
    <jsp:include page="__portletdata.jsp"/>
</div>

<div id="portletroles" style="width: ${rf:mobile(pageContext) ? '100%' : '41em'}; display: none;">
    Loading portlet roles...
</div>

<div class="jbreak"></div>