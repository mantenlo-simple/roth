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
	<r:form action="/Role/load" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'rolelist', function() { _$('rfltrn').focus(); });">
	    <r:wrap label=""><r:break height="4"/>Filter:</r:wrap>
	    <r:textBox id="rfltrn" label="Role Name" dataSource="fRoleName" style="width: 100px;" value="${param['fRoleName']}" 
	               title="Show all roles with a role name containing the value." 
	               onKeyDown="var e = event || window.event; if (e.keyCode == 13) { submitForm(this); return false;}"/>
	    <r:button type="ok" formSubmit="true" style="margin-bottom: 8px;"/>
	</r:form>
	
	<r:break/>
</c:if>

<c:set var="params">fRoleName=${param['fRoleName']}</c:set>
<r:dataGrid id="roleListTable" dataSource="requestScope.roles" dialogCaption="Role|Role &amp;quot;{roleName}&amp;quot;"
            containerId="rolelist" action="/Role/load?${params}" 
            columnMoving="true" columnSizing="true" sorting="true" searching="true"
            rowSelect="true" height="calc(100% - ${rf:mobile(pageContext) ? 25 : 101}px)" width="100%" exporting="true" wrapping="true">
    <r:column caption="Role Name" dataSource="roleName" width="12em" key="true"/>
    <r:column caption="Description" dataSource="description" width="40em"/>
    <r:column caption="Updated By" dataSource="updatedBy" width="180"/>
    <r:column caption="Last Updated" dataSource="updatedDts" width="180"/>
    
    <r:button type="add" action="/Role/edit?${params}"/>
    <r:button type="edit" action="/Role/edit?${params}"/>
    <r:button type="delete" action="/Role/delete?_method=AJAX&${params}"/>
</r:dataGrid>
<div class="jbreak"></div>