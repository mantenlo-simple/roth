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

<style type="text/css">
    code { color: blue; }
</style>

<c:set var="functions">
    toggleConnection = function(value) {
        _$('jndi').style.display = (value == 'jndi') ? 'block' : 'none';
        _$('manual').style.display = (value == 'manual') ? 'block' : 'none';
    }
    toggleSource = function(value)  {
        _$('table').style.display = (value == 'table') ? 'block' : 'none';
        _$('sql').style.display = (value == 'sql') ? 'block' : 'none';
    }
</c:set>

<div onmouseover="${functions}">
    <r:form action="/Developer/genModel" blob="true" method="AJAX" onAjax="Roth.ajax.fileCallback(request);">
        <r:hidden dataSource="requestScope.sqlPost.jndiName"/>
        <r:hidden dataSource="requestScope.sqlPost.schemaName"/>
        <r:hidden dataSource="requestScope.sqlPost.tableName"/>
        <r:hidden dataSource="requestScope.sqlPost.statement"/>
        <r:hidden dataSource="requestScope.sqlPost.primaryKey"/>

        <r:break/>
        
        <fieldset style="float: left; margin: 0 8px 8px 0; padding: 8px 2px 2px 8px; width: ${rf:mobile(pageContext) ? '100%' : '39.4em'};">
            <c:set var="wholeSchema">
            	<c:if test="${!empty param['tableId']}">
	            	<r:checkBox label="Process Whole Schema" boolValues="false|true" dataSource="requestScope.sqlPost.wholeSchema"
	            				title="Process all tables in the selected table's schema.  The value of Class Name will be interpreted as a template, using '*' as a wildcard representing the camel-case version of each table name."
	            	            onClick="var className = _$('className');
	            	            		 if (!className.oldValue) className.oldValue = className.value; 
	            	                     className.value = this.checked == true ? '*Bean' : className.oldValue;"/>
            	</c:if>
            </c:set>
            <legend>Output Class ${wholeSchema}</legend>
            <r:textBox id="packageName" label="Package Name" dataSource="requestScope.sqlPost.packageName" width="${rf:mobile(pageContext) ? 'calc(100% - 6px)' : '280px'}" required="true"/>
            <c:if test="${rf:mobile(pageContext)}">
            	<r:break/>
            </c:if>
            <r:textBox id="className" label="Class Name" dataSource="requestScope.sqlPost.className" width="${rf:mobile(pageContext) ? 'calc(100% - 6px)' : '200px'}" required="true"/>
        </fieldset>
        
        <r:break/>
        
        <fieldset style="float: left; margin: 0 8px 8px 0; padding: 8px 2px 2px 8px; width: ${rf:mobile(pageContext) ? '100%' : '39.4em'};">
            <legend>Options</legend>
            <r:wrap>
                <r:checkBox label="Use <code>@PermissiveBinding</code>" dataSource="usePermissiveBinding" boolValues="false|true" value="true" title="Include the @PermissiveBinding annotation to allow table changes to be gracefully handled."/><br/>
                <%-- <r:checkBox label="Use <code>java.util.Date</code>" dataSource="useDate" boolValues="false|true" value="true" title="Use java.util.Date instead of java.sql.Timestamp."/><br/> --%>
                <r:checkBox label="Use <code>java.math.BigDecimal</code>" dataSource="useBigDecimal" boolValues="false|true" title="Use java.math.BigDecimal insteal of all other number types."/><br/>
            </r:wrap>
            <c:if test="${rf:mobile(pageContext)}">
            	<r:break/>
            </c:if>
            <r:wrap style="${rf:mobile(pageContext) ? '' : 'margin-left: 20px;'}">
                <r:checkBox label="Sort column names" dataSource="sortColumns" boolValues="false|true" value="true" title="Sort columns in alphabetical order."/><br/>
                <r:checkBox label="Trim <code>CHAR</code> columns" dataSource="trimChar" boolValues="false|true" value="true" title="Trim all CHAR fields in setters."/><br/>
                <c:if test="${empty param['statement']}">
	                <r:checkBox label="Extend <code>LoggerBean</code>" dataSource="logChanges" boolValues="false|true" title="Extend LoggerBean class to embed logging of field changes."/><br/>
	                <r:checkBox label="Implement <code>assign</code>" dataSource="implementAssign" boolValues="false|true" title="Implement assign function for object cloning."/><br/>
                </c:if>
            </r:wrap>
            <c:if test="${empty param['statement']}">
	            <r:break/>
	            <r:textBox label="Self-Logging <code>INSERT</code> Statement" dataSource="requestScope.sqlPost.selfLogInsert" width="${rf:mobile(pageContext) ? 'calc(100% - 6px)' : '38.2em'}"/>
            </c:if>
        </fieldset>
            
        <r:break height="4"/>
        <r:button caption="Submit" iconName="save" formSubmit="true"/>
        <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
        <r:break/>
    </r:form>
    <r:break/>
    
    <c:if test="${!empty requestScope.classcode}">
        <div class="jbreak" style="height: 8px;"></div>
        Output:<br/>
        <textarea wrap="${'off'}" cols="117" rows="30">${requestScope.classcode}</textarea>
    </c:if>
    
</div>