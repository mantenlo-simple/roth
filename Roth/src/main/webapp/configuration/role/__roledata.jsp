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

<c:set var="params">fRoleName=${param['fRoleName']}</c:set>
<r:form action="/Role/save?${params}" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'rolelist', function() { Roth.ajax.htmlAction('editroleListTable_content', contextRoot + '/Role/edit', 'roleName=' + _$v('roleName')); });" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:textBox label="Role Name" id="roleName" dataSource="requestScope.role.roleName" width="${rf:mobile(pageContext) ? '100%' : '467px'}" readOnly="${!empty requestScope.role.updatedDts}" required="true"/>
    <br/>
    <r:textBox label="Description" id="description" dataSource="requestScope.role.description" width="${rf:mobile(pageContext) ? '100%' : '467px'}"/>
    
    <r:hidden dataSource="requestScope.role.createdBy"/>
    <r:hidden dataSource="requestScope.role.createdDts"/>
    <r:hidden dataSource="requestScope.role.updatedBy"/>
    <r:hidden dataSource="requestScope.role.updatedDts"/>
    <input type="hidden" name="_method" value="ajax"/>
    
    <div class="jbreak" style="height: 8px;"></div>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>