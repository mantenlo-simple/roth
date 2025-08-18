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
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:jspSecurity rolesAllowed="GroupAdmin"/>
<c:set var="mobi" value="${rf:mobile(pageContext)}"/>

<r:form action="/GroupAdmin/saveGroups" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'dausergroups')" autoComplete="off" 
        onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid dataSource="requestScope.userGroups"
    			height="340px" width="100%" containerId="usergroups">
    	<r:row onClick="clickGridCheckbox(event, this);"/>
        <r:column caption="" dataSource="userid" width="2.9em">
            <c:set var="groupId" value=" ${rowData.groupId}"/>
            <c:if test="${!empty requestScope.groups[fn:trim(groupId)]}">
            	<r:checkBox dataSource="requestScope.userGroups[${rowIndex}].userid"
             			value="${!empty requestScope.userGroups[rowIndex].userid ? param['userid'] : ''}"
             			boolValues="|${param['userid']}"/>
            	<r:hidden dataSource="requestScope.userGroups[${rowIndex}].domainId" value="${param['domainId']}"/>
	            <r:hidden dataSource="requestScope.userGroups[${rowIndex}].groupId"/>
	            <r:hidden dataSource="requestScope.userGroups[${rowIndex}].createdBy"/>
	            <r:hidden dataSource="requestScope.userGroups[${rowIndex}].createdDts"/>
             </c:if>
        </r:column>
        <r:column caption="Group Name" dataSource="groupName" width="225px">
            ${rowData.lineageIcons}${rowData.groupName}
        </r:column>
        <r:column caption="Assigned By" dataSource="createdBy" width="180px"/>
        <r:column caption="Assigned On" dataSource="createdDts" width="180px"/>
        
        <r:button type="save" formSubmit="true"/>
        <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    </r:dataGrid>
    <input type="hidden" name="domainId" id="domainId" value="${param['domainId']}"/>
    <input type="hidden" name="userid" id="userid" value="${param['userid']}"/>
</r:form>
<div class="jbreak"></div>