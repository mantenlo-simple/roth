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
<%@taglib uri="roth" prefix="r"%>

<r:jspSecurity rolesAllowed="SystemAdmin,Developer"/>

<%--
<r:portlet servletPath="/PojoGen">
    <script type="text/javascript">
        function toggleConnection(value) {
        	_$('jndi').style.display = (value == 'jndi') ? 'block' : 'none';
        	_$('manual').style.display = (value == 'manual') ? 'block' : 'none';
        }
        function toggleSource(value) {
            _$('table').style.display = (value == 'table') ? 'block' : 'none';
            _$('sql').style.display = (value == 'sql') ? 'block' : 'none';
        }
    </script>
 --%>
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
    <r:form action="/Test/actionOne" target="_blank">
        <fieldset style="float: left; margin: 0 8px 8px 0; height: 230px; width: 312px;">
            <legend>Connection</legend>
            <input type="radio" id="connjndi" name="_connection" value="jndi" checked onclick="toggleConnection(this.value)"/><label for="connjndi">JNDI</label>
            &nbsp;&nbsp;&nbsp;
            <input type="radio" id="connman" name="_connection" value="manual" onclick="toggleConnection(this.value)"/><label for="connman">Manual</label>
            <r:break height="12"/>
            <div id="jndi">
                <%-- <r:textBox label="JNDI Name" dataSource="requestScope.sqlPost.jndiName" style="width: 200px;"/> --%>
                <r:select label="JNDI Name" dataSource="requestScope.sqlPost.jndiName" optionsDataSource="${requestScope.jndiNames}" required="true" style="width: 206px;"/>
            </div>
            <div id="manual" style="display: none;">
	            <r:textBox label="Driver" dataSource="requestScope.sqlPost.driver" style="width: 200px;"/><br/>
	            <r:textBox label="URL" dataSource="requestScope.sqlPost.url" style="width: 300px;"/><br/>
	            <r:textBox label="User Name" dataSource="requestScope.sqlPost.username" style="width: 200px;"/><br/>
	            <r:textBox label="Password" dataSource="requestScope.sqlPost.password" style="width: 200px;"/><br/>
            </div>
        </fieldset>
        
        <fieldset style="float: left; margin: 0 8px 8px 0; height: 230px; width: 374px; padding: 8px 4px 8px 8px;">
            <legend>Source</legend>
            <input type="radio" id="srctbl" name="_source" value="table" checked onclick="toggleSource(this.value)"/><label for="srctbl">Table</label>
            &nbsp;&nbsp;&nbsp;
            <input type="radio" id="srcsql" name="_source" value="sql" onclick="toggleSource(this.value)"/><label for="srcsql">SQL Statement</label>
            <r:break height="12"/>
            <div id="table">
                <r:textBox label="Schema Name (optional)" dataSource="requestScope.sqlPost.schemaName" style="width: 200px;"/><br/>
                <r:textBox label="Table Name" dataSource="requestScope.sqlPost.tableName" style="width: 200px;" onBlur="if (_$v('className') == '') _$v('className', camelcase(this.value) + 'Bean');"/><br/>
                <r:textBox label="Primary Key Columns (space delimited -- optional)" dataSource="requestScope.sqlPost.primaryKey" style="width: 300px;"/><br/>
                <r:textBox label="Self-Logging <code>INSERT</code> Statement" dataSource="requestScope.sqlPost.selfLogInsert" style="width: 300px;"/><br/>
            </div>
            <div id="sql" style="display: none;">
                <r:textArea label="Statement" dataSource="requestScope.sqlPost.statement" style="width: 362px; height: 158px;"/>
            </div>
        </fieldset>
        
        <r:break/>
        
        <fieldset style="float: left; margin: 0 8px 8px 0; height: 116px;">
            <legend>Output Class</legend>
            <r:textBox label="Package Name" dataSource="requestScope.sqlPost.packageName" style="width: 280px;" required="true"/><br/>
            <r:textBox id="className" label="Class Name" dataSource="requestScope.sqlPost.className" style="width: 200px;" required="true"/><br/>
        </fieldset>
        
        <fieldset style="float: left; margin: 0 8px 8px 0; height: 116px; width: 390px;">
            <legend>Options</legend>
            <r:wrap>
	            <r:checkBox label="Use <code>@PermissiveBinding</code>" dataSource="usePermissiveBinding" boolValues="false|true" value="true" title="Include the @PermissiveBinding annotation to allow table changes to be gracefully handled."/><br/>
	            <%-- <r:checkBox label="Use <code>java.util.Date</code>" dataSource="useDate" boolValues="false|true" value="true" title="Use java.util.Date instead of java.sql.Timestamp."/><br/> --%>
	            <r:checkBox label="Use <code>java.math.BigDecimal</code>" dataSource="useBigDecimal" boolValues="false|true" title="Use java.math.BigDecimal insteal of all other number types."/><br/>
            </r:wrap>
            <r:wrap style="margin-left: 20px;">
	            <r:checkBox label="Sort column names" dataSource="sortColumns" boolValues="false|true" value="true" title="Sort columns in alphabetical order."/><br/>
	            <r:checkBox label="Trim <code>CHAR</code> columns" dataSource="trimChar" boolValues="false|true" value="true" title="Trim all CHAR fields in setters."/><br/>
	            <r:checkBox label="Extend <code>LoggerBean</code>" dataSource="logChanges" boolValues="false|true" title="Extend LoggerBean class to embed logging of field changes."/><br/>
	            <r:checkBox label="Implement <code>assign</code>" dataSource="implementAssign" boolValues="false|true" title="Implement assign function for object cloning."/><br/>
            </r:wrap>
            
            <%--
            <input type="checkbox" id="timestamp" name="useDate" value="true" checked onchange="this.value = this.checked;"/><label for="timestamp">Use <code>java.util.Date</code> in place of <code>java.sql.Timestamp</code>.</label><br/>
            <input type="checkbox" id="bigdec" name="useBigDecimal" value="false" onchange="this.value = this.checked;"/><label for="bigdec">Use <code>java.math.BigDecimal</code> for all numbers.</label><br/>
            <input type="checkbox" id="sortcols" name="sortColumns" value="true" checked onchange="this.value = this.checked;"/><label for="sortcols">Sort column names in alphabetical order.</label><br/>
            <input type="checkbox" id="trimchar" name="trimChar" value="true" checked onchange="this.value = this.checked;"/><label for="trimchar">Trim <code>CHAR</code> columns in setters.</label><br/>
            <input type="checkbox" id="logchg" name="logChanges" value="false" onchange="this.value = this.checked;"/><label for="logchg">Embed logger to track changes to columns.</label><br/>
            <input type="checkbox" id="permissive" name="usePermissiveBinding" value="true" checked onchange="this.value = this.checked;"/><label for="permissive">Include @PermissiveBinding annotation.</label><br/>
             --%>
        </fieldset>
	        
        <r:break height="4"/>
        <r:button caption="Submit" iconName="save" action="/PojoGen/genModel" formSubmit="true"/>
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
<%--
</r:portlet>
 --%>