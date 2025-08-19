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

<div style="height: calc(100% - 2px); overflow-y: auto;">
	<r:form action="/Setting/save" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'settings');">
	    <div>
		    <r:wrap type="input">
		        <span title="Used when emailing validation codes for changing forgotten passwords.  Also used as the default values for the Email class.">Outgoing Mail Settings (SMTP)</span>
		        <div style="height: 0px; border-bottom: 1px solid silver; margin: 6px 0;"></div>
		        <r:textBox label="Host" dataSource="requestScope.smtp.host" width="300px"/>
		        <r:textBox label="Port" dataSource="requestScope.smtp.port" width="62px"/>
		        <r:break/>
		        <r:textBox label="User" dataSource="requestScope.smtp.user" width="201px"
		                   title="If user is not supplied, then authentication will not be used."/>
		        <r:textBox label="Password" dataSource="requestScope.smtp.password" width="161px"/>
		        <r:break/>
		        <r:select label="Mode" dataSource="requestScope.smtp.mode" width="93px">
		            <r:option caption="Simple" value="0"/>
		            <r:option caption="SSL" value="1"/>
		            <r:option caption="TLS" value="2"/>
		        </r:select>
		        <r:textBox label="From" dataSource="requestScope.smtp.from" width="223px"
		                   title="The email address to use as the sender or from address."/>
		    </r:wrap>
		    
		    <r:wrap type="input">
		        <span title="Used when emailing validation codes for changing forgotten passwords.  Also used as the default values for the Email class.">Incoming Mail Settings (IMAP/POP3)</span>
		        <div style="height: 0px; border-bottom: 1px solid silver; margin: 6px 0;"></div>
		        <r:textBox label="Host" dataSource="requestScope.mail.host" width="300px"/>
		        <r:textBox label="Port" dataSource="requestScope.mail.port" width="62px"/>
		        <r:break/>
		        <r:textBox label="User" dataSource="requestScope.mail.user" width="201px"
		                   title="If user is not supplied, then authentication will not be used."/>
		        <r:textBox label="Password" dataSource="requestScope.mail.password" width="161px"/>
		        <r:break/>
		        <r:select label="Protocol" dataSource="requestScope.mail.protocol" width="93px">
		            <r:option caption="POP3" value="pop3"/>
		            <r:option caption="IMAP" value="imap"/>
		        </r:select>
		        <r:select label="Mode" dataSource="requestScope.mail.mode" width="97px">
		            <r:option caption="Simple" value="0"/>
		            <r:option caption="SSL" value="1"/>
		            <r:option caption="TLS" value="2"/>
		        </r:select>
		    </r:wrap>
		    
		    <r:wrap type="input">
		        <span title="Settings used by descendants of the JdbcUtil class.">JDBC Settings</span>
		        <div style="height: 0px; border-bottom: 1px solid silver; margin: 6px 0;"></div>
		        <r:textBox label="Query Timeout" dataSource="requestScope.jdbc.queryTimeout" width="110px"
		                   title="Seconds"/>
		    </r:wrap>
		    
		    <r:break/>
		    
		    <r:wrap type="input" style="width: 100%;">
		    	<r:textArea label="Miscellaneous Settings" dataSource="misc" width="100%" height="200px" value="${requestScope.misc}"/>
		    </r:wrap>
		    <r:break/>
	    </div>
	    <r:button type="save" formSubmit="true" onClick="Roth.getDialog('wait').wait();"/>
	    <r:button type="refresh" iconName="sync-alt" onClick="Roth.ajax.htmlAction('settings', contextRoot + '/Setting/load');"/>
	    <r:break/>
	</r:form>
	<r:break/>
</div>