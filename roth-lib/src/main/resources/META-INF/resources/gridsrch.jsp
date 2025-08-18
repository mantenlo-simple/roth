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

<r:select label="Search Column" id="_j_dataGrid_search_column" dataSource="_na" width="${rf:mobile(pageContext) ? '100%' : '130px'}" nullable="true" required="true">
	<c:set var="fields" value="${fn:split(param['fields'], ',')}"/>
	<%
		String[] fields = (String[])pageContext.getAttribute("fields");
		java.util.Arrays.sort(fields, (a, b) -> { return a.split("\\|")[1].toLowerCase().compareTo(b.split("\\|")[1].toLowerCase()); });
	%>
	<c:forEach var="field" items="${fields}" varStatus="stat">
		<c:set var="fieldDef" value="${fn:split(field, '\\\\|')}"/>
		<r:option caption="${fieldDef[1]}" value="${fieldDef[0]}"/>
	</c:forEach>
</r:select>
<c:if test="${rf:mobile(pageContext)}">
	<r:break/>
</c:if>
<r:textBox label="Search Value" id="_j_dataGrid_search_value" dataSource="_na" width="${rf:mobile(pageContext) ? '100%' : '210px'}" required="true"/>

<r:break height="8px"/>

<c:set var="xsrfToken" value="${cookie['xsrf-token'].value}"/>
<c:set var="csrfToken">${fn:escapeXml(rf:encrypt(xsrfToken, xsrfToken.hashCode()))}</c:set>
<input type="hidden" id="gcToken" name="_na" value="${csrfToken}"/>
<r:button type="ok" onClick="let col = getValue(_$('_j_dataGrid_search_column'));
                             let val = getValue(_$('_j_dataGrid_search_value'));
                             if (!col || col === '' || !val || val === '') {
                             	Roth.getDialog('alert').alert('One or more required fields do not have values.');
                             	return false;
                             }
                             Roth.table.search.submit(_$v('gcToken'));
                             setTimeout(() => { Roth.getParentDialog(this).hide(); });"/>
<r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>