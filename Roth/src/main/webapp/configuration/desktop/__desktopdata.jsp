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

<r:form action="/Desktop/save" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'desktoplist', function() { Roth.ajax.htmlAction('editdesktopListTable_content', contextRoot + '/Desktop/edit', 'desktopName=' + _$v('desktopName')); });" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:textBox label="Desktop Name" id="desktopName" dataSource="requestScope.desktop.desktopName" width="${rf:mobile(pageContext) ? '100%' : '179px'}" required="true"/>
    <c:if test="${rf:mobile(pageContext)}">
    	<r:break/>
    </c:if>
    <r:textBox label="Desktop Title" id="desktopTitle" dataSource="requestScope.desktop.desktopTitle" width="${rf:mobile(pageContext) ? '100%' : '280px'}" required="true"/>
    
    <r:break/>
    
    <r:select label="Context Book" dataSource="requestScope.desktop.bookId" width="${rf:mobile(pageContext) ? '100%' : '184px'}" required="true">
        <r:option caption="" value=""/>
        <c:forEach var="book" items="${requestScope.bookList}">
            <r:option caption="${book.bookName}" value="${book.bookId}"/>
        </c:forEach>
    </r:select>
    <c:if test="${rf:mobile(pageContext)}">
    	<r:break/>
    </c:if>
    <r:select label="Theme" dataSource="requestScope.desktop.themeId" width="${rf:mobile(pageContext) ? '100%' : '164px'}" required="true">
        <r:option caption="" value=""/>
        <c:forEach var="theme" items="${requestScope.themeList}">
            <r:option caption="${theme.themeName}" value="${theme.themeId}"/>
        </c:forEach>
    </r:select>
    
    <r:break/>
    
    <div style="${rf:mobile(pageContext) ? '' : 'float: left;'}">
        <r:hidden dataSource="requestScope.desktop.desktopUri"/>
	    <%-- <r:textBox label="Desktop URI" id="desktopUri" dataSource="requestScope.desktop.desktopUri" style="width: 370px;" required="true"/>
	    <r:break/> --%>
	    <r:textBox label="Desktop Icon URI" id="desktopIcon" dataSource="requestScope.desktop.desktopIcon" width="${rf:mobile(pageContext) ? '100%' : '360px'}" onKeyUp="document.getElementById('iconPreview').src = this.value"/>
        <r:break/>
        <r:textBox label="Description" id="description" dataSource="requestScope.desktop.description" width="${rf:mobile(pageContext) ? '100%' : '360px'}"/>
    </div>
    <div style="${rf:mobile(pageContext) ? '' : 'float: left; margin-left: 8px;'}">
        <div class="jedt"><div class="jlbl">Icon Preview</div>
            <div style="float: left; margin-left: 10px; border: 1px solid silver; border-radius: 3px; padding: 3px; min-height: 70px; min-width: 70px; "><img id="iconPreview" src="${requestScope.desktop.desktopIcon}" alt="404" style="margin: 0px; width: 64px; height: 64px;"/></div>
        </div>
    </div>
    
    
    <r:hidden dataSource="requestScope.desktop.desktopId" id="desktopId"/>
    <r:hidden dataSource="requestScope.desktop.updatedBy" id="updatedBy"/>
    <r:hidden dataSource="requestScope.desktop.updatedDts" id="updatedDts"/>
    <input type="hidden" name="_method" value="ajax"/>
    
    <r:break height="8px"/>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>
<div class="jbreak"></div>