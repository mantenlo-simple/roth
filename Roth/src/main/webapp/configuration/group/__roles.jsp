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
<%@taglib uri="roth" prefix="r"%>

<r:form action="/Group/saveRoles" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'grouproles')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid dataSource="requestScope.groupRoles" height="250" containerId="grouproles">
        <r:row onClick="clickGridCheckbox(event, this);"/>
        <r:column caption="" dataSource="groupId" width="43px">
            <r:checkBox dataSource="requestScope.groupRoles[${rowIndex}].groupId"
             			value="${!empty requestScope.groupRoles[rowIndex].groupId ? param['groupId'] : ''}"
             			boolValues="|${param['groupId']}"/>
            <r:hidden dataSource="requestScope.groupRoles[${rowIndex}].groupName"/>
            <r:hidden dataSource="requestScope.groupRoles[${rowIndex}].roleName"/>
            <r:hidden dataSource="requestScope.groupRoles[${rowIndex}].createdBy"/>
            <r:hidden dataSource="requestScope.groupRoles[${rowIndex}].createdDts"/>
        </r:column>
        <r:column caption="Role Name" dataSource="roleName" width="13em"/>
        <r:column caption="Assigned By" dataSource="createdBy" width="11em"/>
        <r:column caption="Assigned On" dataSource="createdDts" width="11em"/>
        
        <r:button type="save" formSubmit="true"/>
        <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    </r:dataGrid>
    <input type="hidden" name="groupId" id="groupId" value="${param['groupId']}"/>
</r:form>
<div class="jbreak"></div>