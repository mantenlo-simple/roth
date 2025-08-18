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

<r:portlet servletPath="/Developer">
    <style type="text/css">
        .sqltoolcontent {
            height: calc(100% - 12px);
            border: 1px solid rgba(0,128,0,0.8); 
            border-top: none; 
            border-bottom-left-radius: 0.5em; 
            border-bottom-right-radius: 0.5em; 
            padding: 1em;
        }
        .dsiTblSel {
	        background: gray;
	        color: white;
	    }
	    pre { margin: 0; }
	    .CodeMirror { background: transparent; }
	    #dsnOpts > .roth-wrap:first-child, #dsnOpts > .roth-wrap:nth-child(4) {
	    	min-width: 150px;
	    }
    </style>
    <script type="text/javascript">
	    let sysAdmin = ${rf:isUserInRole(pageContext, 'SystemAdmin')};
	    let editOrientation = "${empty param['orientation'] or param['orientation'] eq 'horizontal' ? 'vertical' : 'horizontal'}";
	    let restore = ${param['restore'] eq 'true'};
		<jsp:include page="index.js"/>
    </script>
    
    <%--
    <r:button type="open" onClick="Roth.execDialog('openDsn', '/RothDeveloper/Developer/openDsn', null, 'Open Data Source', 'open');"/>
    <div id="datasources" class="jtab">
        <ul onclick="var e = event ? event : window.event;
        	         var src = e.target ? e.target : e.srcElement;
        	         src = getAncestorNode(src, 'LI');
                     if (src && src.nodeName == 'LI') {
        	             window.stm[window.stmidx].save();
        	             window.stmidx = src.getAttribute('page');
        	             window.stm[window.stmidx].refresh();
                     }">
        </ul>
    </div>
     --%>
    
    <div id="dsnTabs" style="width: 100%;">
	    <r:tabset id="dsiTabs" style="padding-left: 30em; width: 100%; box-sizing: border-box;"
	              onSelect="let {pageId} = Roth.tabset.getSelected(this);
	              console.log(pageId); 
	                        if ((pageId == 'objCont') && (_$('objCont').innerHTML == '')) {
	                      console.log('Inside');
	                           Roth.ajax.htmlAction('objCont', '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=S&jndiname=' + _$v('jndiname'));
	                        }">
	        <r:option caption="Editor" iconName="edit" value="" pageId="dsnCont" selected="true"/>
	        <r:option caption="Tables" iconName="table" value="" pageId="objCont"/>
	        <%--
	        <r:option caption="Views" iconName="sql" value="" pageId="objCont"/>
	        <r:option caption="Stored Procedures" iconName="function" value="" pageId="objCont"/>
	         --%>
	    </r:tabset>
    </div>
    <r:break/>
    <div id="dsnOpts" style="position: absolute; right: 33px; margin: -44px 0 0 0; height: 3.5em; background: white; border: 1px solid silver; border-top: none; border-bottom: none; padding: 0 0.5em;">
        <r:select label="Data Source" title="The JNDI data source to execute statements with" 
                  id="jndiNameSelect" dataSource="_na" optionsDataSource="${requestScope.jndiNames}" value="${not empty param['jndiName'] ? param['jndiName'] : requestScope.firstJndiName}"
                  onChange="jndiChange(getValue(this));"/>
        <r:textBox id="rowLimit" label="Limit" title="Limit number of rows in result set" dataSource="limit" width="75px" value="500" number="true"
                   onChange="_$('limit').value = getValue(this); if (!window.loading && !window.updating) updateLocalStorage();"/>
		<r:textBox id="maxLength" label="Max" title="Trim to maximum column width" dataSource="maxlen" width="50px" value="50" number="true"
                   onChange="_$('maxlen').value = getValue(this); if (!window.loading && !window.updating) updateLocalStorage();"/>
		<%--
        <r:textBox label="Schema" title="The selected schema to run statements against" dataSource="schema" style="width: 9em;" value=""
                   onChange="_$('schema').value = getValue(this);"/>
          --%>
        <r:select id="schemaSel" label="Schema" title="The selected schema to run statements against" dataSource="schema" 
                  optionsDataSource="${requestScope.schemas}" value="" nullable="true"
                  onChange="schemaChange(getValue(this));"/>
    </div>
    <r:break/>
    <div id="dsnCont" class="sqltoolcontent">
        <r:form id="execForm" action="/Developer/exec" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'result', () => { if (!window.loading) updateLocalStorage(); });">
            <r:hidden id="jndiname" dataSource="jndiname" value="${not empty param['jndiName'] ? param['jndiName'] : requestScope.firstJndiName}"/>
		    <r:hidden id="limit" dataSource="limit" value="500"/>
		    <r:hidden id="maxlen" dataSource="maxlen" value="50"/>
		    <r:hidden id="schema" dataSource="schema" value=""/>
		    <r:hidden id="adhocStatementSend" dataSource="statement"/>
		    <r:hidden id="params" dataSource="params"/>
            <r:hidden id="password" dataSource="password"/>
		</r:form>    
		
	    <%-- <r:textBox id="desc" label="Description" dataSource="name" style="width: 200px;"/> --%>
	    <r:break/>
	    
	    <r:splitPanes height="100%" width="100%" variablePane="0" orientation="vertical">
	    	<r:pane size="calc(100% - 44px)">
   				<r:splitPanes id="testpanes" height="100%" width="100%" variablePane="0" orientation="${empty param['orientation'] ? 'horizontal' : param['orientation'] }">
			    	<r:pane minSize="15em" size="calc(100% - ${empty param['orientation'] or param['orientation'] eq 'horizontal' ? '50%' : '15.7em'})">
			    		<r:textArea id="adhocStatement" dataSource="__statement" width="100%" height="100%" 
			    		            style="background: rgba(255,255,255,0.6);" inputStyle="background: transparent;"/>
			    	</r:pane>
			    	<r:pane minSize="15em" size="${empty param['orientation'] or param['orientation'] eq 'horizontal' ? 'calc(50% - 0.7em)' : '15em'}">
			    	    <div class="roth-input" style="height: 100%; width: 100%; position: relative;">
				    		<div id="result" class="roth-output" style="height: 100%; box-sizing: border-box; background: rgba(255,255,255,0.6); overflow: auto; padding: ${param['orientation'] eq 'vertical' ? '1em 1em 1em 2.4em' : '2.4em 1em 1em 1em'}; margin: 0;"></div>
	                        <div style="position: absolute; left: 0.5em; top: 0.5em; padding: ${empty param['orientation'] or param['orientation'] eq 'horizontal' ? '0.2em 0.5em' : '0.3em'}; background: #fffd; border-radius: 0.2em; border: 0.1em solid silver;">
	                            <r:icon iconName="${empty param['orientation'] or param['orientation'] eq 'horizontal' ? 'arrows-alt-v' : 'arrows-alt-h'}" 
	                                     title="${empty param['orientation'] or param['orientation'] eq 'horizontal' ? 'Switch to vertical panes.' : 'Switch to horizontal panes.'}" 
	                                     style="color: rgba(0,0,0,0.3);"
	                                     onClick="setOrientation();"/>
	                            <c:if test="${param['orientation'] eq 'vertical'}"><r:break/></c:if>
	                        	<r:icon iconName="copy" title="Copy to clipboard." style="color: rgba(0,0,0,0.3); margin-${param['orientation'] eq 'vertical' ? 'top' : 'left'}: 0.2em;"
	                        	         onClick="var range = document.createRange();  
									    		  range.selectNode(_$('result'));  
									    		  window.getSelection().addRange(range);  
									    		  try { var successful = document.execCommand('copy'); } catch(err) { console.log('Copy to clipboard failed.'); }  
									    		  window.getSelection().removeAllRanges();
									    		  Roth.getDialog('alert').alert('Results copied to clipboard.');"/>
							    <c:if test="${param['orientation'] eq 'vertical'}"><r:break/></c:if>
							    <r:icon iconName="save" title="Save to file." style="color: rgba(0,0,0,0.3); margin-${param['orientation'] eq 'vertical' ? 'top' : 'left'}: 0.2em;"
	                        	         onClick="Roth.file.save(getChild(_$('result'), 0).innerHTML, 'result.txt', 'text/sql');"/>
							    <c:if test="${param['orientation'] eq 'vertical'}"><r:break/></c:if>
						        <r:icon iconName="trash" title="Clear results." style="color: rgba(0,0,0,0.3); margin-${param['orientation'] eq 'vertical' ? 'top' : 'left'}: 0.2em;"
	                        	         onClick="_$('result').innerHTML = ''; updateLocalStorage();"/>
	                        </div>
                        </div>
			    	</r:pane>
   				</r:splitPanes>
	    	</r:pane>
	    	<r:pane fixed="true" size="44px">
		    	<r:break height="0.8em"/>
    
			    <r:button caption="Run Statement" iconName="play" 
			              onClick="execStatement(event || window.event);"
			              title="(Ctrl-Enter); Hold the Shift key down while clicking to run selected text (Ctrl-Shift-Enter)."/>
		           <%--
			    <r:button caption="Run Script" iconName="play" overlayName="new" formSubmit="true" 
		                     onClick="if (!validateAdhoc(event)) return false; Roth.getDialog('wait').wait();"
		                     title="(Alt-Enter)"/>
		                      --%>
		        <c:if test="${rf:isUserInRole(pageContext, 'Developer')}">
		            <r:button caption="Java" iconName="coffee" 
					          title="Create a Java Object from the current query."
					          onClick="var stmt = getStatement(event); 
					                   if (!stmt) 
					                       return false; 
					                   Roth.getDialog('wait').wait();
					                   Roth.execDialog('pojogen', '/RothDeveloper/Developer/abbreviated', 'jndiname=' + _$v('jndiname') + '&statement=' + stmt, 'Create Java Object', 'coffee', 'packageName');"/>
				</c:if>
				
				
				<r:button type="save" style="margin-left: 1em;" 
			              onClick="var stmt = getAdhocStmt(true); 
				                   if (!stmt) 
				                       return false;
			                       Roth.file.save(getAdhocStmt(true), 'file.sql', 'text/sql');"/>
			    <r:button type="open" 
			              onClick="Roth.file.open(function(result) {
			                  _$v('adhocStatement', result);
				              window.stm[window.stmidx].setValue(result);
				              window.stm[window.stmidx].refresh();  
			              });"/>
				<r:break/>
	    	</r:pane>
	    </r:splitPanes>
	    
	    
	    <%--
	    <r:button type="save" 
	              onClick="if ((_$v('desc') == '') || (_$v('adhocStatement') == '')) { 
	                           Roth.getDialog('error').error('A description and statement are required.'); 
	                           return false; 
	                       } 
	                       _$('execForm').setAttribute('ajax', 'AJAX'); 
	                       submitForm(_$('execForm'), '/DashboardWeb/Developer/save'); 
	                       _$('execForm').removeAttribute('ajax'); 
	                       _$('password').value = '';"/>
	    
	    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
	      --%>
		<r:break/>
    </div>
    
    <div id="objCont" class="sqltoolcontent" style="display: none;"></div>
</r:portlet>