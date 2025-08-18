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

<r:splitPanes orientation="vertical" variablePane="1" width="100%" height="100%">
	<r:pane minSize="15em" size="20em">
		<r:dataGrid id="dsiTriggers" dataSource="requestScope.triggers" width="100%" height="100%" columnSizing="true" rowSelect="true">
			<r:row onClick="var e = event || window.event;
			                if (e.ctrlKey)
			                	_$('triggerbody').innerHTML = '';
			                else
			                	_$('triggerbody').innerHTML = _$('span_${rowData.triggerId}').innerHTML;"/>
		    <r:column caption="Table ID" dataSource="tableId" width="50" visible="false"/>
		    <r:column caption="Trigger ID" dataSource="triggerId" width="50" visible="false"/>
		    <r:column caption="Name" dataSource="triggerName" width="220"/>
		    <r:column caption="Type" dataSource="triggerType" width="200"/>
		    <r:column caption="Event" dataSource="triggeringEvent" width="200"/>
		</r:dataGrid>
		<r:break/>
		<div style="display: none;">
			<c:forEach var="item" items="${requestScope.triggers}">
				<span id="span_${item.triggerId}">${item.triggerBody}</span>
			</c:forEach>
		</div>
	</r:pane>
	<r:pane minSize="5em" size="calc(100% - 20em)">
		<pre id="triggerbody" style="width: 100%; height: 100%; border: 0.1em solid silver; padding: 0.5em; background: #aaa3; overflow: auto;">
		</pre>
	</r:pane>
</r:splitPanes>