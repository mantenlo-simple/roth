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

Users assigned to domain "${param['domainName']}"
<r:form action="/Domain/saveUsers" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'domainusers')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid dataSource="requestScope.domainUsers" height="250" containerId="domainusers">
        <r:column caption="" dataSource="domainId" width="30">
            <c:set var="checkvalue">checkvalue="${param['domainId']}"</c:set>
            <input type="checkbox" 
                   name="requestScope.domainUsers[${rowIndex}].domainId"
                   value="${!empty requestScope.domainUsers[rowIndex].domainId ? param['domainId'] : ''}"
                   ${checkvalue}
                   onclick="this.value = (this.checked) ? this.getAttribute('checkvalue') : ''" 
                   ${!empty requestScope.domainUsers[rowIndex].domainId ? 'checked' : ''}
             />
             <r:hidden dataSource="requestScope.domainUsers[${rowIndex}].userid"/>
             <r:hidden dataSource="requestScope.domainUsers[${rowIndex}].updatedBy"/>
             <r:hidden dataSource="requestScope.domainUsers[${rowIndex}].updatedDts"/>
        </r:column>
        <r:column caption="User ID" dataSource="userid" width="175"/>
        <r:column caption="Assigned By" dataSource="updatedBy" width="140"/>
        <r:column caption="Assigned On" dataSource="updatedDts" width="140"/>
        
        <r:button type="save" formSubmit="true"/>
        <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    </r:dataGrid>
    <input type="hidden" name="domainId" id="domainId" value="${param['domainId']}"/>
</r:form>
<div class="jbreak"></div>