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
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:dataGrid id="domainPropertyTable" dataSource="requestScope.domainProperties" 
            height="300" containerId="domainproperties" rowSelect="true"
            columnSizing="true">
    <r:column caption="Domain ID" dataSource="domainId" width="40" key="true" visible="false"/>
    <r:column caption="Property Name" dataSource="propertyName" width="16em" key="true"/>
    <r:column caption="Updated By" dataSource="updatedBy" width="11em"/>
    <r:column caption="Updated On" dataSource="updatedDts" width="11em"/>
    
    <r:button type="add" action="/Domain/editProperty?domainId=${param['domainId']}"/>
    <r:button type="edit" action="/Domain/editProperty"/>
    <r:button type="delete" action="/Domain/deleteProperty"/>
</r:dataGrid>
${rf:mobile(pageContext) ? '<span>' : ''}
<r:button type="close" onClick="Roth.getParentDialog(this).hide();" style="${rf:mobile(pageContext) ? 'margin-top: 8px' : 'margin-left: 16px'};"/>
${rf:mobile(pageContext) ? '</span>' : ''}
<div class="jbreak"></div>