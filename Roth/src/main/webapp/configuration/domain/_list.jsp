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

<r:dataGrid id="domainListTable" dataSource="requestScope.domains" dialogCaption="Domain|Domain &amp;quot;{domainName}&amp;quot;"
            containerId="domainlist" action="/Domain/load?_method=AJAX" 
            columnMoving="true" columnSizing="true" sorting="true" searching="true"
            rowSelect="true" height="calc(100% - ${rf:mobile(pageContext) ? 25 : 41}px)" width="100%" exporting="true">
    <r:column caption="Domain ID" dataSource="domainId" width="40" key="true" visible="false"/>
    <r:column caption="Name" dataSource="domainName" width="100" key="true"/>
    <r:column caption="Description" dataSource="description" width="300"/>
    <r:column caption="Updated By" dataSource="updatedBy" width="160"/>
    <r:column caption="Last Updated" dataSource="updatedDts" width="180"/>
    
    <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
        <r:button type="add" action="/Domain/edit"/>
    </c:if>
    <r:button type="edit" action="/Domain/edit"/>
    <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
        <r:button type="delete" action="/Domain/delete?_method=AJAX"/>
    </c:if>
</r:dataGrid>
<div class="jbreak"></div>
