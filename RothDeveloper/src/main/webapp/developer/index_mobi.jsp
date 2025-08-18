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
            height: calc(100% - 1.7em);
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
    </style>
    <script type="text/javascript">
	    let sysAdmin = ${rf:isUserInRole(pageContext, 'SystemAdmin')};
	    let editOrientation = "${empty param['orientation'] or param['orientation'] eq 'horizontal' ? 'vertical' : 'horizontal'}";
	    let restore = ${param['restore'] eq 'true'};
		<jsp:include page="index.js"/>
    </script>
    
    <r:tabset id="dsiTabs" style="padding-left: 30em; width: 100%; box-sizing: border-box;"
              onSelect="let pageId = Roth.tabset.getSelected(this); 
                        if ((pageId == 'objCont') && (_$('objCont').innerHTML == ''))
                           Roth.ajax.htmlAction('objCont', '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=S&jndiname=' + _$v('jndiname'));
                        if (pageId === 'settingsCont')
                        	_$('scrTog').classList.add('disabled');
                        else
                        	_$('scrTog').classList.remove('disabled');">
        <r:option caption="Editor" iconName="edit" value="" pageId="dsnCont" selected="true"/>
        <r:option caption="Tables" iconName="table" value="" pageId="objCont"/>
        <%--
        <r:option caption="Views" iconName="sql" value="" pageId="objCont"/>
        <r:option caption="Stored Procedures" iconName="function" value="" pageId="objCont"/>
         --%>
        <r:option caption="Settings" iconName="cog" value="" pageId="settingsCont"/>
    </r:tabset>
    
     <div style="position: absolute; right: 10px; top: 50px; text-align: right; font-size: 0.6rem;">
     	Data Source: <span id="jndinameLabel" style="color: navy;">${requestScope.firstJndiName}</span><br/>
     	Schema: <span id="schemaLabel" style="color: navy;"></span><br/>
     	<r:break height="6px"/>
    	<r:button id="scrTog" iconName="arrows-alt-v" 
    	          onClick="let id = undefined;
    	                   let block = 'end';
    	                   if (_$('dsnCont').style.display !== 'none')
    	                   	   id = getChild(_$('dsnCont'), '2').scrollTop === 0 ? 'resultCont' : 'queryCont';
    	                   else if (_$('objCont').style.display !== 'none')
    	                   	   id = getChild(_$('objCont'), '1').scrollTop === 0 ? 'metaCont' : 'schemaCont';
    	                   if (id === 'queryCont' || id === 'schemaCont')
    	                   	   block = 'start';
    	                   if (id) {
    	                       let obj = _$(id);
    	                       if (obj)
    	                           obj.scrollIntoView({behavior: 'smooth', block: block});
    	                   }"/>
    </div>
   
    
    <r:break/>
    
    <div id="dsnCont" style="width: 100%; height: calc(100% - 24px);">
        <r:form id="execForm" action="/Developer/exec" method="AJAX" target="result" onAjax="Roth.ajax.htmlCallback(request, 'result', function() { setTimeout(function() { _$('resultCont').scrollIntoView({behavior: 'smooth', inline: 'center'}); }, 200); });">
            <r:hidden id="jndiname" dataSource="jndiname" value="${requestScope.firstJndiName}"/>
		    <r:hidden id="limit" dataSource="limit" value="500"/>
		    <r:hidden id="maxlen" dataSource="maxlen" value="50"/>
		    <r:hidden id="schema" dataSource="schema" value=""/>
		    <r:hidden id="adhocStatementSend" dataSource="statement"/>
		    <r:hidden id="params" dataSource="params"/>
            <r:hidden id="password" dataSource="password"/>
		</r:form>    
		
	    <r:break/>
	    
	    <div style="width: 100%; height: calc(100% - 40px); scroll-snap-type: y mandatory; overflow-y: scroll; overflow-x: hidden;">
	    	<div id="queryCont" style="width: 100%; height: 100%; scroll-snap-align: start;">
	    		<r:textArea id="adhocStatement" dataSource="__statement" width="100%" height="100%" style="background: rgba(255,255,255,0.6);" inputStyle="background: transparent;"/>
	    	</div>
	    	<div id="resultCont" style="width: 100%; height: 100%; scroll-snap-align: start;">
	    		<div class="roth-input" style="height: 100%; width: 100%; position: relative;">
	    			<div id="result" class="roth-output" style="height: 100%; box-sizing: border-box; background: rgba(255,255,255,0.6); overflow: auto; padding: 4em 1em 1em 1em; margin: 0;"></div>
                       <div style="position: absolute; left: 0.5em; top: 0.5em; padding: 0.2em 0.5em; background: #fffd; border-radius: 0.2em; border: 0.1em solid silver; font-size: 1.5em;">
                       	<r:icon iconName="copy" title="Copy to clipboard." style="color: rgba(0,0,0,0.3);"
                       	        onClick="var range = document.createRange();  
							    		  range.selectNode(_$('result'));  
							    		  window.getSelection().addRange(range);  
							    		  try { var successful = document.execCommand('copy'); } catch(err) { console.log('Copy to clipboard failed.'); }  
							    		  window.getSelection().removeAllRanges();
							    		  Roth.getDialog('alert').alert('Results copied to clipboard.');"/>
					    <r:icon iconName="save" title="Save to file." style="color: rgba(0,0,0,0.3); margin-left: 10px;"
                       	         onClick="Roth.file.save(getChild(_$('result'), 0).innerHTML, 'result.txt', 'text/sql');"/>
				        <r:icon iconName="trash" title="Clear results." style="color: rgba(0,0,0,0.3); margin-left: 10px;"
                       	         onClick="_$('result').innerHTML = '';"/>
                     </div>
                 </div>
	    	</div>
	    </div>
	    
	    <r:break height="8px"/>
    
	    <r:button caption="Run" iconName="play" 
	              onClick="execStatement(event || window.event);"
	              title="(Ctrl-Enter); Hold the Shift key down while clicking to run selected text (Ctrl-Shift-Enter)."/>
        <c:if test="${rf:isUserInRole(pageContext, 'Developer')}">
            <r:button iconName="coffee" 
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
    </div>
    
    <div id="objCont" style="width: 100%; height: calc(100% - 24px); display: none;"></div>
    
    <div id="settingsCont" style="width: 100%; height: calc(100% - 24px); display: none;">
    	<r:select label="Data Source" title="The JNDI data source to execute statements with" 
                  id="jndiNameSelect" dataSource="_na" optionsDataSource="${requestScope.jndiNames}" width="100%"
                  onChange="jndiChange(getValue(this));"/>
		<r:break/>
        <r:textBox label="Limit" title="Limit number of rows in result set" dataSource="limit" number="true" width="100%" value="500"
                   onChange="_$('limit').value = getValue(this);"/>
        <r:break/>
		<r:textBox label="Max" title="Trim to maximum column width" dataSource="maxlen" number="true" width="100%" value="50"
                   onChange="_$('maxlen').value = getValue(this);"/>
        <r:break/>
		<%--
        <r:textBox label="Schema" title="The selected schema to run statements against" dataSource="schema" style="width: 9em;" value=""
                   onChange="_$('schema').value = getValue(this);"/>
        <r:break/>
          --%>
        <r:select label="Schema" title="The selected schema to run statements against" dataSource="schema" width="100%" 
                  optionsDataSource="${requestScope.schemas}" value="" nullable="true"
                  onChange="schemaChange(getValue(this));"/>
    </div>
</r:portlet>