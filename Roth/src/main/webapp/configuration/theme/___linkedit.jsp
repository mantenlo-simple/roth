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
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:form action="/Theme/saveLink" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'themelinks', function() { Roth.getDialog('editthemeLinksTable').hide(); })" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:textBox label="Link Title" dataSource="requestScope.themeLink.linkTitle" width="${rf:mobile(pageContext) ? '100%' : '190px'}"/>
    <c:if test="${rf:mobile(pageContext)}">
		<r:break/>
	</c:if>
    <r:textBox label="Link Target" dataSource="requestScope.themeLink.target" width="${rf:mobile(pageContext) ? '100%' : '169px'}"/>
    
    <r:break/>
    
    <r:textBox label="Link URI" dataSource="requestScope.themeLink.linkUri" width="${rf:mobile(pageContext) ? '100%' : '379px'}"/>
    
    <r:hidden dataSource="requestScope.themeLink.themeId" value="${param['themeId']}"/>
    <r:hidden dataSource="requestScope.themeLink.sequence" value="${param['sequence']}"/>
    <r:hidden dataSource="requestScope.themeLink.updatedBy"/>
    <r:hidden dataSource="requestScope.themeLink.updatedDts"/>
    <r:hidden dataSource="themeId" value="${param['themeId']}"/>
    <r:hidden dataSource="themeName" value="${param['themeName']}"/>
    
    <r:break height="8"/>
    <r:button type="save" formSubmit="true"/>
    <r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
    <r:break/>
</r:form>