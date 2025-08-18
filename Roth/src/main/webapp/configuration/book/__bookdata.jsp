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

<r:form action="/Book/save" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'booklist', function() { Roth.ajax.htmlAction('editbookListTable_content', contextRoot + '/Book/edit', 'bookName=' + _$v('bookName')); });" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
	<r:textBox label="Book Name" id="bookName" dataSource="requestScope.book.bookName" width="${rf:mobile(pageContext) ? '100%' : '180px'}" readOnly="${!empty requestScope.book.updatedDts}" required="true"/>
    <c:if test="${rf:mobile(pageContext)}">
    	<r:break/>
    </c:if>
    <r:select label="Parent Book" dataSource="requestScope.book.parentBookId" width="${rf:mobile(pageContext) ? '100%' : '184px'}" nullable="true"
              optionsDataSource="${requestScope.books}" keyDataSource="bookId" valueDataSource="bookName"/>
    <r:break/>
    <r:textBox label="Book Title" id="bookTitle" dataSource="requestScope.book.bookTitle" width="${rf:mobile(pageContext) ? '100%' : '375px'}" required="true"/>
    <r:break/>
    <r:textBox label="Description" id="description" dataSource="requestScope.book.description" width="${rf:mobile(pageContext) ? '100%' : '375px'}"/>
    
    <r:hidden dataSource="requestScope.book.bookId"/>
    <r:hidden dataSource="requestScope.book.sequence"/>
    <r:hidden dataSource="requestScope.book.lineage"/>
    <r:hidden dataSource="requestScope.book.updatedBy"/>
    <r:hidden dataSource="requestScope.book.updatedDts"/>
    <input type="hidden" name="_method" value="ajax"/>
    
    <div class="jbreak" style="height: 8px;"></div>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>