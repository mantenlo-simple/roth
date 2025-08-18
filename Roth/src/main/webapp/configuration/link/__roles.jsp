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

Roles assigned to link "${param['linkTitle']}":
<r:form action="/Link/saveRoles" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'linkroles')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid dataSource="requestScope.linkRoles" height="250">
        <r:column caption="" dataSource="linkId" width="30">
            <c:set var="checkvalue">checkvalue="${param['linkId']}"</c:set>
            <input type="checkbox" 
                   name="requestScope.linkRoles[${rowIndex}].linkId"
                   value="${!empty requestScope.linkRoles[rowIndex].linkId ? param['linkId'] : ''}"
                   ${checkvalue}
                   onclick="this.value = (this.checked) ? this.getAttribute('checkvalue') : ''" 
                   ${!empty requestScope.linkRoles[rowIndex].linkId ? 'checked' : ''}
             />
             <r:hidden dataSource="requestScope.linkRoles[${rowIndex}].roleName"/>
             <r:hidden dataSource="requestScope.linkRoles[${rowIndex}].updatedBy"/>
             <r:hidden dataSource="requestScope.linkRoles[${rowIndex}].updatedDts"/>
        </r:column>
        <r:column caption="Role Name" dataSource="roleName" width="175"/>
        <r:column caption="Assigned By" dataSource="updatedBy" width="140"/>
        <r:column caption="Assigned On" dataSource="updatedDts" width="140"/>
    </r:dataGrid>
    <input type="hidden" name="linkId" id="linkId" value="${param['linkId']}"/>
    
    <div class="jbreak" style="height: 8px;"></div>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>