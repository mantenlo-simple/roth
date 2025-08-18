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

<r:form action="/User/saveProperty" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'userproperties', function() { Roth.getDialog('edituserPropertyTable').hide(); });">
    <r:hidden dataSource="userid" value="${requestScope.userProperty.userid}"/>
    <r:hidden dataSource="domainId" value="${requestScope.userProperty.domainId}"/>
    <r:hidden dataSource="requestScope.userProperty.userid"/>
    <r:hidden dataSource="requestScope.userProperty.domainId"/>
    <r:hidden dataSource="requestScope.userProperty.createdBy"/>
    <r:hidden dataSource="requestScope.userProperty.createdDts"/>
    <r:hidden dataSource="requestScope.userProperty.updatedBy"/>
    <r:hidden dataSource="requestScope.userProperty.updatedDts"/>

    <r:textBox label="Property Name" dataSource="requestScope.userProperty.propertyName" width="${rf:mobile(pageContext) ? '100%' : '400px'}" required="true" readOnly="${!empty requestScope.userProperty.propertyName}"/>
    <r:break/>
    <r:textBox label="Property Value" dataSource="requestScope.userProperty.propertyValue" width="${rf:mobile(pageContext) ? '100%' : '400px'}"/>

    <r:break height="6"/>

    <r:button type="save" formSubmit="true"/>
    <r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>
<r:break/>