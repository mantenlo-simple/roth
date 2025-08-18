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

<r:form action="/Portlet/saveRoles" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'portletroles')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid dataSource="requestScope.portletRoles" height="20em">
        <r:row onClick="clickGridCheckbox(event, this);"/>
        <r:column caption="" dataSource="portletId" width="43px">
            <r:checkBox dataSource="requestScope.portletRoles[${rowIndex}].portletId"
             			value="${!empty requestScope.portletRoles[rowIndex].portletId ? param['portletId'] : ''}"
             			boolValues="|${param['portletId']}"/>
            <r:hidden dataSource="requestScope.portletRoles[${rowIndex}].roleName"/>
            <r:hidden dataSource="requestScope.portletRoles[${rowIndex}].createdBy"/>
            <r:hidden dataSource="requestScope.portletRoles[${rowIndex}].createdDts"/>
        </r:column>
        <r:column caption="Role Name" dataSource="roleName" width="14em"/>
        <r:column caption="Assigned By" dataSource="createdBy" width="11em"/>
        <r:column caption="Assigned On" dataSource="createdDts" width="11em"/>
        
        <r:button type="save" formSubmit="true"/>
        <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    </r:dataGrid>
    <input type="hidden" name="portletId" id="portletId" value="${param['portletId']}"/>
</r:form>
<div class="jbreak"></div>