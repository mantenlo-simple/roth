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

<r:form action="/Theme/saveOverrides" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'themeOverrides')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid dataSource="requestScope.themeOverrides"
                containerId="themeoverrides" id="themeOverridesTable" dialogCaption="Host Override"
                width="100%" height="314" rowSelect="true">
        <r:column caption="" dataSource="themeId" width="40" key="true" visible="false"/>
        <r:column caption="Host Name" dataSource="hostName" width="225" key="true"/>
        <r:column caption="Alternate Theme" dataSource="altThemeId" width="140">
            <c:set var="altThemeId">${rowData.altThemeId}</c:set>
            ${requestScope.themes[altThemeId]}
        </r:column>
        <r:column caption="Created By" dataSource="updatedBy" width="180"/>
        <r:column caption="Created On" dataSource="updatedDts" width="180"/>
        
        <r:button type="add" action="/Theme/editOverride">
               <r:parameter name="themeId" value="${param['themeId']}"/>
               <r:parameter name="themeName" value="${param['themeName']}"/>
        </r:button>
        <r:button type="edit" action="/Theme/editOverride?themeName=${param['themeName']}"/>
        <r:button type="delete" action="/Theme/deleteOverride?themeName=${param['themeName']}"/>
        
    </r:dataGrid>
    <c:if test="${rf:mobile(pageContext)}">
		<r:break/>
	</c:if>
    <r:button type="close" style="${rf:mobile(pageContext) ? 'margin-top: 8px' : 'margin-left: 16px'};" onClick="Roth.getParentDialog(this).hide();"/>
    <input type="hidden" name="themeId" id="themeId" value="${param['themeId']}"/>
</r:form>
