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

<r:dataGrid id="dsiForeignkeys" dataSource="requestScope.foreignkeys" width="100%" height="100%" columnSizing="true">
    <r:column caption="Table ID" dataSource="tableId" width="50" visible="false"/>
    <r:column caption="Constraint Name" dataSource="constraintName" width="180"/>
    <r:column caption="Column(s)" dataSource="columnNames" width="270"/>
    <r:column caption="Ref. Constraint Name" dataSource="referencedConstraintName" width="190"/>
    <r:column caption="Ref. Schema" dataSource="referencedSchema" width="150"/>
    <r:column caption="Ref. Table" dataSource="referencedTableName" width="150"/>
    <r:column caption="Ref. Columns" dataSource="referencedColumnNames" width="270"/>
    <r:column caption="Delete Rule" dataSource="deleteRule" width="150"/>
</r:dataGrid>
<r:break/>
