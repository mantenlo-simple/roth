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
	<r:form action="/User/load" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'userlist');">
	    <r:wrap label=""><r:break height="4"/>Filter:</r:wrap>
	    <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
		    <r:select label="Domain" dataSource="fDomainId" optionsDataSource="${requestScope.domains}" nullable="true" value="${param['fDomainId']}"
		              title="Limit list to users in the selected domain.  If no domain is selected, all available domains are shown."/>
	    </c:if>
	    <r:textBox label="User ID" dataSource="fUserid" style="width: 100px;" value="${param['fUserid']}" 
	               title="Show all users with a userid containing this value."
	               onKeyDown="var e = event || window.event; if (e.keyCode == 13) { submitForm(this); return false;}"/>
	    <r:textBox label="Name" dataSource="fName" style="width: 120px;" value="${param['fName']}"
	               title="Show all users with a name containing this value."
	               onKeyDown="var e = event || window.event; if (e.keyCode == 13) { submitForm(this); return false;}"/>
	    <%--
	    <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
	        <r:select label="Role" dataSource="fRoleName" optionsDataSource="${requestScope.roles}" nullable="true" value="${param['fRoleName']}" 
	                  title="Show all users that have this role"/>
	    </c:if>
	     --%>
	    <r:button type="ok" formSubmit="true" style="margin-bottom: 8px;"/>
	</r:form>
	
	<r:break/>
</c:if>

<style type="text/css">
	.expiredIcon {
		float: left; 
		margin: 0 0 -1px 6px; 
		background: #fffa; 
		padding: 2px 2px 0 2px; 
		border-radius: 0.6em;
		color: red;
	}
</style>

<c:set var="params">fDomainId=${param['fDomainId']}</c:set>
<c:set var="params">${params}&fUserid=${param['fUserid']}</c:set>
<c:set var="params">${params}&fName=${param['fName']}</c:set>
<c:set var="params">${params}&fRoleName=${param['fRoleName']}</c:set>
<r:dataGrid id="userListTable" dataSource="requestScope.users"
            containerId="userlist" action="/User/load?${params}" dialogCaption="User|User &amp;quot;{userid}&amp;quot;"
            columnMoving="true" columnSizing="true" sorting="true" searching="true"
            rowSelect="true" height="calc(100% - ${rf:mobile(pageContext) ? 25 : 101}px)" width="100%" exporting="true">
    <r:column caption="Domain" dataSource="domainId" width="100px" key="true">
        <c:set var="domainId">${rowData.domainId}</c:set>
        ${requestScope.domains[domainId]}
    </r:column>
    <r:column caption="User ID" dataSource="userid" width="10em" key="true"/>
    <r:column caption="Name" dataSource="name" width="11em"/>
    <r:column caption="Created By" dataSource="createdBy" width="14em"/>
    <r:column caption="Created" dataSource="createdDts" width="11em"/>
    <r:column caption="Updated By" dataSource="updatedBy" width="14em"/>
    <r:column caption="Last Updated" dataSource="updatedDts" width="11em" key="true"/>
    <r:column caption="Expires" dataSource="expireDts" width="13em" key="true">
        <div style="float: left;">${rf:formatDate(rowData.expireDts, sessionScope.formats['datetime'])}</div>
        <c:if test="${!empty rowData.expireDts && rowData.expireDts lt rf:now('LocalDateTime')}">
        	<div class="expiredIcon" title="Expired">
        		<c:set var="expired"><r:icon iconName="exclamation-circle"/></c:set>
        		${rowData.expireDts < rf:now('LocalDateTime') ? expired : ''}
           	</div>
        </c:if>
    </r:column>
    
    <r:button type="add" action="/User/edit?${params}"/>
    <r:button type="edit" action="/User/edit?${params}"/>
    <r:button type="delete" action="/User/delete?_method=AJAX&${params}"/>
</r:dataGrid>

<div class="jbreak"></div>
