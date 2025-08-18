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

<r:jspSecurity rolesAllowed="SystemAdmin"/>
<r:form action="/Logging/set" method="AJAX" onAjax="Roth.ajax.messageCallback(request, null, null, null, 1500);">
	<r:codeMirror label="Log Settings" dataSource="logSettings" value="${requestScope.logSettings}" width="300px" height="150px"/>

	<%--
    <r:select label="Log Level" dataSource="logLevel" value="${requestScope.logLevel}" width="${rf:mobile(pageContext) ? '100%' : '110px'}">
        <r:option caption="Exception" value="0"/>
        <r:option caption="Error" value="1"/>
        <r:option caption="Warning" value="2"/>
        <r:option caption="Info" value="3"/>
        <r:option caption="Debug" value="4"/>
    </r:select>
    
    <c:if test="${rf:mobile(pageContext)}">
		<r:break height="8px"/>
	</c:if>
    
    <r:wrap label="" style="${rf:mobile(pageContext) ? 'margin-left: 90px;' : ''}">
   	    <r:checkBox label="Log stacktrace" dataSource="logStackTrace" boolValues="N|Y" value="${requestScope.logStackTrace}"/>
   	</r:wrap>
   	
   	<r:break/>
   	
   	<r:textBox label="Log Codes" dataSource="logCodes" value="${requestScope.logCodes}" width="${rf:mobile(pageContext) ? '100%' : '20em'};"
               onKeyDown="var e = event || window.event; if (e.keyCode == 13) { submitForm(this); return false;}"
               title="Log codes are a comma-delimited list of codes that identify which log entries are to be reported in the logs.  If left empty, all level-qualifying log entries will be reported."/>
     --%>
    <r:break height="${mobi ? '1em' : '0.5em'}"/>

    <r:button type="ok" formSubmit="true" onClick="Roth.getDialog('wait').wait();"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>
<r:break/>