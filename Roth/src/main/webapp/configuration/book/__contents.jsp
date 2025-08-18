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
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:form action="/Book/saveContents" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'bookcontent')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:dataGrid id="contentTable" dataSource="requestScope.bookContents"
                containerId="bookcontent" 
                height="250" rowSelect="true">
        <r:column caption="" dataSource="bookId" width="40" key="true" visible="false"/>
        <r:column caption="" dataSource="name" width="40" key="true" visible="false"/>
        <r:column caption="" dataSource="tableId" width="40" key="true" visible="false"/>
        <r:column caption="" dataSource="sequence" width="40" key="true" visible="false"/>
        <r:column caption="" dataSource="tableName" width="2em" key="true">
        	<r:icon iconName="${rowData.tableName eq 'book' ? 'book' : 'plug'}"/>
        </r:column>
        <r:column caption="Title" dataSource="title" width="175"/>
        <r:column caption="Updated By" dataSource="updatedBy" width="180"/>
        <r:column caption="Last Updated" dataSource="updatedDts" width="180"/>
        
        <r:button type="add" action="/Book/editPage">
               <r:parameter name="bookId" value="${param['bookId']}"/>
               <r:parameter name="bookName" value="${param['bookName']}"/>
               <r:parameter name="sequence" value="${fn:length(requestScope.bookContents)}"/>
        </r:button>
        <r:button type="edit" action="/Book/editPage?bookName=${param['bookName']}" sendParams="true" onValidate="if (!canManageContent()) return false;"/>
        <r:button type="delete" action="/Book/deletePage?bookName=${param['bookName']}" onValidate="if (!canManageContent()) return false;"/>
    </r:dataGrid>

	<c:if test="${rf:mobile(pageContext)}">
		<r:break/>
	</c:if>
	<r:button iconName="caret-up" title="Move Up" style="${rf:mobile(pageContext) ? 'margin-top: 8px' : 'margin-left: 16px'};" action="/Book/movePage?direction=up&bookName=${param['bookName']}" onClick="Roth.table.htmlAction('contentTable', this.href, true); return false;" sendParams="true"/>
    <r:button iconName="caret-down" title="Move Down" action="/Book/movePage?direction=down&bookName=${param['bookName']}" onClick="Roth.table.htmlAction('contentTable', this.href, true); return false;" sendParams="true"/>    	
	
    <r:button type="close" style="margin-left: 16px;" onClick="Roth.getParentDialog(this).hide();"/>
    <input type="hidden" name="bookId" id="bookId" value="${param['bookId']}"/>
</r:form>

<%--
<r:form action="/Book/save" method="AJAX" onAjaxResponse="editAjaxResponse(request)" onAjaxError="editAjaxError(request)" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <div style="float: left; width: 330px;">
        Hello
    </div>
    <div style="float: left;">
        <r:button caption="" iconName="add"/><div class="jbreak"></div>
        <r:button caption="" iconName="remove"/><div class="jbreak"></div>
        <r:button caption="" iconName="up"/><div class="jbreak"></div>
        <r:button caption="" iconName="down"/><div class="jbreak"></div>
    </div>
    
    <input type="hidden" name="bookId" id="bookId" value="${param['bookId']}"/>
    <input type="hidden" name="updatedBy" id="updatedBy" value="${param['updatedBy']}"/>
    <input type="hidden" name="updatedDts" id="updatedDts" value="${param['updatedDts']}"/>
    <input type="hidden" name="_method" value="ajax"/>
    
    <div class="jbreak" style="height: 8px;"></div>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>
--%>