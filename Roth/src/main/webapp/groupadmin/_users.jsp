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
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:jspSecurity rolesAllowed="GroupAdmin"/>
<c:set var="mobi" value="${rf:mobile(pageContext)}"/>

<r:dataGrid id="daUserTable" dataSource="requestScope.users" containerId="deptadmin_content"
			action="/GroupAdmin" dialogCaption="User|User &amp;quot;{userid}&amp;quot;"
            height="340px" width="${mobi ? '100%' : '904px'}" 
            rowSelect="true" sorting="true">
    <r:column caption="User ID" dataSource="userid" width="80" key="true"/>
    <r:column caption="Domain" dataSource="domainId" width="80" key="true">
        <c:set var="domainId">${rowData.domainId}</c:set>
        ${requestScope.domains[domainId]}
    </r:column>
    <r:column caption="Name" dataSource="name" width="180"/>
    <r:column caption="Created By" dataSource="createdBy" width="180"/>
    <r:column caption="Updated By" dataSource="updatedBy" width="180"/>
    <r:column caption="Updated On" dataSource="updatedDts" width="180"/>
    
    <r:button type="add" action="/GroupAdmin/edit"/>
    <r:button type="edit" action="/GroupAdmin/edit"/>
    <%-- // Probably don't want to let them delete users... 
    <r:button type="delete" action="/DeptAdmin/delete"/>
    --%>
</r:dataGrid>
<r:wrap style="width: 16px;"/>
<r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
<r:break/>