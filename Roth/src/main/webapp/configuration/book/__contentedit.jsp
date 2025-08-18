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
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:form action="/Book/savePage?bookName=${param['bookName']}" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'bookcontent')" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <c:choose>
        <c:when test="${empty requestScope.page.portletId}">
            <r:select label="Portlet" optionsDataSource="${requestScope.portlets}" dataSource="requestScope.page.portletId" width="${rf:mobile(pageContext) ? '100%' : '200px'}" required="true"/>
        </c:when>
        <c:otherwise>
            <r:hidden dataSource="requestScope.page.portletId"/>
            <r:textBox label="Portlet" dataSource="_null" value="${param['name']}" width="${rf:mobile(pageContext) ? '100%' : '200px'}" readOnly="true"/>
        </c:otherwise>
    </c:choose>
    <c:if test="${rf:mobile(pageContext)}">
    	<r:break/>
    </c:if>
    <r:textBox label="Page Title" id="pageTitle" dataSource="requestScope.page.pageTitle" width="${rf:mobile(pageContext) ? '100%' : '180px'}" required="true"/>
    
    <r:hidden dataSource="requestScope.page.bookId"/>
    <r:hidden dataSource="requestScope.page.sequence" value="${!empty requestScope.page.sequence ? requestScope.page.sequence : param['sequence']}"/>
    <r:hidden dataSource="requestScope.page.updatedBy"/>
    <r:hidden dataSource="requestScope.page.updatedDts"/>
    <r:hidden dataSource="bookId" value="${param['bookId']}"/>
    <r:hidden dataSource="bookName" value="${param['bookName']}"/>
    <input type="hidden" name="_method" value="ajax"/>
    
    <div class="jbreak" style="height: 8px;"></div>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    <div class="jbreak"></div>
</r:form>