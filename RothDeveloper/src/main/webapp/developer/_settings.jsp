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

<r:jspSecurity rolesAllowed="SystemAdmin,Developer"/>

These settings indicate whether a user with the Developer role may use a data source (Available
-- not checked or false by<br/>default), and if so, whether that data source will be readonly to them
(Readonly -- checked or true by default).

<r:break height="1em"/> 

<r:form id="jndiSettingForm" action="/Developer/saveSettings" method="AJAX" onAjax="Roth.ajax.messageCallback(request, function() { Roth.getParentDialog(_$('jndiSettingForm')).hide(); }, null, null, 2500);">
	<r:dataGrid dataSource="requestScope.settings" height="240px">
		<r:column caption="JNDI Name" dataSource="jndiName" width="10em"/>
		<r:column caption="DBMS" dataSource="databaseName" width="10em"/>
		<r:column caption="Available" dataSource="available" width="7em">
			<r:hidden dataSource="requestScope.settings[${rowIndex}].jndiSettingId"/>
			<r:checkBox dataSource="requestScope.settings[${rowIndex}].available" boolValues="N|Y"/>
		</r:column>
		<r:column caption="Readonly" dataSource="readonly" width="7em">
			<r:checkBox dataSource="requestScope.settings[${rowIndex}].readonly" boolValues="N|Y"/>
		</r:column>
		<r:column caption="Updated By" dataSource="updatedBy" width="10em"/>
		<r:column caption="Updated Dts" dataSource="updatedDts" width="12em"/>
		
		<r:button type="save" formSubmit="true"/>
		<r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
	</r:dataGrid>

</r:form>
<r:break/>