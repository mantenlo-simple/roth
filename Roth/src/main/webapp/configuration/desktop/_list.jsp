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

<r:dataGrid id="desktopListTable" dataSource="requestScope.desktops" dialogCaption="Desktop|Desktop &amp;quot;{desktopName}&amp;quot;"
            containerId="desktoplist" action="/Desktop/load?_method=AJAX"
            columnMoving="true" columnSizing="true" sorting="true" searching="true" 
            rowSelect="true" height="calc(100% - ${rf:mobile(pageContext) ? 25 : 41}px)" width="100%" exporting="true">
    <r:column caption="Desktop ID" dataSource="desktopId" width="40" key="true" visible="false"/>
    <r:column caption="Desktop Name" dataSource="desktopName" width="150" key="true"/>
    <r:column caption="Desktop Title" dataSource="desktopTitle" width="240"/>
    <r:column caption="Description" dataSource="description" width="240"/>
    <r:column caption="Updated By" dataSource="updatedBy" width="180"/>
    <r:column caption="Last Updated" dataSource="updatedDts" width="180"/>
    
    <r:button type="add" action="/Desktop/edit"/>
    <r:button type="edit" action="/Desktop/edit"/>
    <r:button type="delete" action="/Desktop/delete?_method=AJAX"/>
</r:dataGrid>
<div class="jbreak"></div>
