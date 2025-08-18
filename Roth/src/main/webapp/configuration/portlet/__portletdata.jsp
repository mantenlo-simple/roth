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

<r:form action="/Portlet/save" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'portletlist', function() { Roth.ajax.htmlAction('editportletListTable_content', contextRoot + '/Portlet/edit', 'portletName=' + _$v('portletName')); });" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:textBox label="Portlet Name" id="portletName" dataSource="requestScope.portlet.portletName" width="${rf:mobile(pageContext) ? '100%' : '413px'}" readOnly="${!empty requestScope.portlet.updatedDts}" required="true"/>
    <c:if test="${rf:mobile(pageContext)}">
    	<r:break/>
    </c:if>
    <r:textBox label="Description" id="description" dataSource="requestScope.portlet.description" width="${rf:mobile(pageContext) ? '100%' : '413px'}"/>
    <r:break/>
    <r:textBox label="Portlet URI &nbsp; (note: when using a 'javascript:' URI, use single quotes only, if necessary)" id="portletUri" dataSource="requestScope.portlet.portletUri" width="${rf:mobile(pageContext) ? '100%' : '838px'}" required="true"/>
    
    <r:hidden dataSource="requestScope.portlet.portletId"/>
    <r:hidden dataSource="requestScope.portlet.applicationId" value="0"/>
    <r:hidden dataSource="requestScope.portlet.updatedBy"/>
    <r:hidden dataSource="requestScope.portlet.updatedDts"/>
    <input type="hidden" name="_method" value="ajax"/>
    
    <r:break height="8px"/>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>