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

<r:dataGrid dataSource="requestScope.links" containerId="linkDlg_content" 
            rowSelect="true" height="250">
    <r:column caption="" dataSource="linkId" visible="false" key="true" width="0"/>
    <r:column caption="Name" dataSource="linkName" width="150"/>
    <r:column caption="Title" dataSource="linkTitle" width="250"/>
    <r:column caption="Updated By" dataSource="updatedBy" width="140"/>
    <r:column caption="Last Updated" dataSource="updatedDts" width="140"/>
    
    <r:button type="add" action="/Link/edit"/>
    <r:button type="edit" action="/Link/edit"/>
    <r:button type="delete" action="/Link/delete?_method=AJAX"/>
    <r:button type="close" style="margin-left: 16px;" onClick="Roth.getParentDialog(this).hide(); document.location = document.location; return false;"/>
</r:dataGrid>
<c:if test="${rf:isUserInRole(pageContext, 'PortalAdmin')}">
	<r:form action="/Link/load" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'linkDlg_content')">
	    <r:button caption="View ${param['allLinks'] eq 'true' ? 'Only My' : 'All Users'} Links" iconName="view" style="float: right;" formSubmit="true" action="/Link/load">
	        <r:parameter name="allLinks" value="${param['allLinks'] eq 'true' ? 'false' : 'true'}"/>
	    </r:button>
	</r:form>
</c:if>
<div class="jbreak"></div>