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

<c:if test="${!rf:mobile(pageContext)}">
	<r:form action="/Group/load" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'grouplist');">
	    <r:wrap label=""><r:break height="4"/>Filter:</r:wrap>
	    <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
	        <r:select label="Domain" dataSource="fDomainId" optionsDataSource="${requestScope.domains}" nullable="true" value="${param['fDomainId']}"
	                  title="Limit list to groups in the selected domain.  If no domain is selected, all available domains are shown."/>
	    </c:if>
	    <r:textBox label="Group Name" dataSource="fGroupName" style="width: 100px;" value="${param['fGroupName']}" 
	               title="Show all groups with a group name containing this value."
	               onKeyDown="var e = event || window.event; if (e.keyCode == 13) { submitForm(this); return false;}"/>
	    <%--
	    <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
	        <r:select label="Role" dataSource="fRoleName" optionsDataSource="${requestScope.roles}" nullable="true" value="${param['fRoleName']}" 
	                  title="Show all groups that have this role"/>
	    </c:if>
	     --%>
	    <r:button type="ok" formSubmit="true" style="margin-bottom: 8px;"/>
	</r:form>
	
	<r:break/>
</c:if>

<c:set var="params">fDomainId=${param['fDomainId']}</c:set>
<c:set var="params">${params}&fGroupName=${param['fGroupName']}</c:set>
<c:set var="params">${params}&fRoleName=${param['fRoleName']}</c:set>
<r:dataGrid id="groupListTable" dataSource="requestScope.groups" dialogCaption="Group|Group &amp;quot;{groupName}&amp;quot;"
            containerId="grouplist" action="/Group/load?${params}" 
            columnMoving="true" columnSizing="true" searching="true"
            rowSelect="true" height="calc(100% - ${rf:mobile(pageContext) ? 25 : 101}px)" width="100%" exporting="true">
    <r:column caption="Domain" dataSource="domainId" width="100" key="true">
        <c:set var="domainId">${rowData.domainId}</c:set>
        ${requestScope.domains[domainId]}
    </r:column>
    <r:column caption="Group ID" dataSource="groupId" width="40" key="true" visible="false"/>        
    <r:column caption="Group Name" dataSource="groupName" width="270" key="true">
        ${rowData.lineageIcons}${rowData.groupName}
    </r:column>
    <r:column caption="Description" dataSource="description" width="270"/>
    <r:column caption="Updated By" dataSource="updatedBy" width="180"/>
    <r:column caption="Last Updated" dataSource="updatedDts" width="180"/>
    
    <r:button type="add" action="/Group/edit?${params}"/>
    <r:button type="edit" action="/Group/edit?${params}"/>
    <r:button type="delete" action="/Group/delete?_method=AJAX&${params}"/>
</r:dataGrid>
<div class="jbreak"></div>
