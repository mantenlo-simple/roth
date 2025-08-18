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
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:form action="/Theme/saveLinks" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'themeLinks')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid dataSource="requestScope.themeLinks"
                containerId="themelinks" id="themeLinksTable" dialogCaption="Link"
                width="100%" height="314" rowSelect="true">
        <r:column caption="" dataSource="themeId" width="40" key="true" visible="false"/>
        <r:column caption="" dataSource="sequence" width="40" key="true" visible="false"/>
        <r:column caption="Link Title" dataSource="linkTitle" width="115"/>
        <r:column caption="Link URI" dataSource="linkUri" width="225"/>
        <r:column caption="Created By" dataSource="updatedBy" width="180"/>
        <r:column caption="Created On" dataSource="updatedDts" width="180"/>
        
        <r:button type="add" action="/Theme/editLink">
               <r:parameter name="themeId" value="${param['themeId']}"/>
               <r:parameter name="themeName" value="${param['themeName']}"/>
               <r:parameter name="sequence" value="${fn:length(requestScope.themeLinks)}"/>
        </r:button>
        <r:button type="edit" action="/Theme/editLink?themeName=${param['themeName']}"/>
        <r:button type="delete" action="/Theme/deleteLink?themeName=${param['themeName']}"/>
    </r:dataGrid>
    <c:if test="${rf:mobile(pageContext)}">
		<r:break/>
	</c:if>
    <r:button iconName="caret-up" title="Move Up" style="${rf:mobile(pageContext) ? 'margin-top: 8px' : 'margin-left: 16px'};" action="/Theme/moveLink?direction=up&themeName=${param['themeName']}" htmlCallback="true" onClick="Roth.table.htmlAction('contentTable', this.href, true); return false;"/>
    <r:button iconName="caret-down" title="Move Down" action="/Theme/moveLink?direction=down&themeName=${param['themeName']}" htmlCallback="true" onClick="Roth.table.htmlAction('contentTable', this.href, true); return false;"/>
    
    <r:button type="close" style="margin-left: 16px;" onClick="Roth.getParentDialog(this).hide();"/>
    <input type="hidden" name="themeId" id="themeId" value="${param['themeId']}"/>
</r:form>
