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

<!-- ${rf:setResourcePath(pageContext, 'com/jp/portal/resource/portal')} -->

<r:form action="/News/save" method="AJAX" onAjax="Roth.getParentDialog(this).callback(request);">
    <r:hidden dataSource="requestScope.news.newsId"/>
    <r:hidden dataSource="requestScope.news.postDts"/>
    <r:hidden dataSource="requestScope.news.updatedBy"/>
    <r:hidden dataSource="requestScope.news.updatedDts"/>

    <c:choose>
        <c:when test="${rf:isUserInRole(pageContext, 'NewsAdmin')}">
            <r:select label="##domain" dataSource="requestScope.news.domainId" optionsDataSource="${requestScope.domains}" nullable="true"/>
        </c:when>
        <c:otherwise>
            <r:hidden dataSource="requestScope.news.domainId"/>
        </c:otherwise>
    </c:choose>
    
    <r:select label="##group" dataSource="requestScope.news.groupId" optionsDataSource="${requestScope.groups}" nullable="true"/>
    
    <r:textBox label="##languageCode" dataSource="requestScope.news.languageCode" style="width: 90px;" required="true"/>
    <r:break/>
    <r:textBox label="##headline" dataSource="requestScope.news.headline" style="width: 500px;" required="true"/>
    <r:break/>
    <r:textArea id="newsContent" label="##content" dataSource="requestScope.news.content" style="width: 700px; height: 300px;" required="true"/>
    <r:break/>
    <r:checkBox label="##sticky" dataSource="requestScope.news.sticky" boolValues="N|Y"/>
    
    <r:break height="8"/>
    <r:button type="save" formSubmit="true" onClick="tinyMCE.triggerSave();"/>
    <r:button type="close" onClick="tinymce.remove('#newsContent'); Roth.getParentDialog(this).hide();"/>
    <r:break/>
</r:form>