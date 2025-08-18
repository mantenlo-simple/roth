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

<div class="grid-conf-container">
	<div>
		Visible Fields:
		
		<c:set var="fields" value="${fn:split(param['fields'], ',')}"/>
		<%
			String[] fields = (String[])pageContext.getAttribute("fields");
			java.util.Arrays.sort(fields, (a, b) -> { return a.split("\\|")[1].toLowerCase().compareTo(b.split("\\|")[1].toLowerCase()); });
		%>
		<div id="_j_dataGrid_fields" class="grid-conf-fields" style="width: ${rf:mobile(pageContext) ? '100%' : '230px'}">
			<c:forEach var="field" items="${fields}" varStatus="stat">
				<c:set var="fieldDef" value="${fn:split(field, '\\\\|')}"/>
				<div class="${stat.index % 2 == 0 ? 'even' : 'odd'}" fieldname="${fieldDef[0]}">
					<r:checkBox label="${fieldDef[1]}" dataSource="rGrd${fieldDef[0]}_na" boolValues="false|true" value="${fieldDef[2]}"/> <%-- domainId|Domain|true --%>
	            </div>
			</c:forEach>
		</div>
	</div>
	<div>
		<r:radioGroup label="Remember Settings" id="_j_dataGrid_mem" dataSource="_j_dataGrid_mem" value="${param['remember']}" vertical="true">
			<r:option caption="Only in current session" value="0"/>
			<r:option caption="In my profile manualy" value="1"/>
			<r:option caption="In my profile automatically" value="2"/>
			<r:option caption="Restore default settings" value="3"/>
		</r:radioGroup>
		
		<r:break height="8px"/>
		
		<r:wrap style="padding-top: 10px;" title="Setting to 0 will turn off paging.">Rows per Page:</r:wrap> <r:textBox id="_j_dataGrid_rpp" dataSource="_j_dataGrid_rpp" number="true" maxLength="3" precision="0" width="58px" value="${param['rows']}"/>
	</div>
</div>

<r:break height="16px"/>

<c:set var="xsrfToken" value="${cookie['xsrf-token'].value}"/>
<c:set var="csrfToken">${fn:escapeXml(rf:encrypt(xsrfToken, xsrfToken.hashCode()))}</c:set>
<input type="hidden" id="gcToken" name="_na" value="${csrfToken}"/>
<r:button type="ok" onClick="Roth.table.config.apply('${param['gridid']}', _$v('gcToken')); setTimeout(() => { Roth.getParentDialog(this).hide(); });"/>
<r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>