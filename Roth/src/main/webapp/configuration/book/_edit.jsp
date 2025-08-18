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

<r:tabset onSelect="Roth.tabset.getPage(this, 'bookId=${requestScope.book.bookId}&bookName=${requestScope.book.bookName}')">
    <r:option caption="Book Data" value="books" pageId="bookdata" iconName="book" selected="true" />
    <c:if test="${!empty requestScope.book.bookId}">
        <r:option caption="Contents" value="content" pageId="bookcontent" iconName="file" action="/Book/getContents?bookId=${requestScope.book.bookId}&bookName=${requestScope.book.bookName}"/>
    </c:if>
</r:tabset> 

<div class="jbreak" style="height: 6px;"></div>

<div id="bookdata" style="width: ${rf:mobile(pageContext) ? '100%' : '380px'};">
    <jsp:include page="__bookdata.jsp"/>
</div>

<div id="bookcontent" style="width: ${rf:mobile(pageContext) ? '100%' : '600px'}; display: none;">
    Loading book contents...
</div>

<div class="jbreak"></div>