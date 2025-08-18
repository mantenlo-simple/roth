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

<r:form action="/Role/saveGroups" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'rolegroups')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid dataSource="requestScope.roleGroups" height="250" containerId="rolegroups">
        <r:row onClick="clickGridCheckbox(event, this);"/>
        <r:column caption="" dataSource="groupId" width="43px">
            <r:checkBox dataSource="requestScope.roleGroups[${rowIndex}].roleName"
             			value="${!empty requestScope.roleGroups[rowIndex].roleName ? param['roleName'] : ''}"
             			boolValues="|${param['roleName']}"/>
            <r:hidden dataSource="requestScope.roleGroups[${rowIndex}].groupId"/>
            <r:hidden dataSource="requestScope.roleGroups[${rowIndex}].createdBy"/>
            <r:hidden dataSource="requestScope.roleGroups[${rowIndex}].createdDts"/>
        </r:column>
        <r:column caption="Group Name" dataSource="groupName" width="17.5em">
            ${rowData.lineageIcons}${rowData.groupName}
        </r:column>
        <r:column caption="Assigned By" dataSource="createdBy" width="11em"/>
        <r:column caption="Assigned On" dataSource="createdDts" width="11em"/>
        
        <r:button type="save" formSubmit="true"/>
        <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    </r:dataGrid>
    <input type="hidden" name="roleName" id="roleName" value="${param['roleName']}"/>
</r:form>
<div class="jbreak"></div>