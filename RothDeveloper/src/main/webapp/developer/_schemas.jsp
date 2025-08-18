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

<c:set var="prevSchema"/>

<style type="text/css">
    .unselectable {
        white-space: nowrap;
        -webkit-user-select: none; /* webkit (safari, chrome) browsers */
        -moz-user-select: none; /* mozilla browsers */
        -khtml-user-select: none; /* webkit (konqueror) browsers */
        -ms-user-select: none; /* IE10+ */
        padding: 3px 0 4px 0;
    }
    .unselectable a {
        height: 16px;
    }
    .jtab .fa { color: darkgray; } 
    .jtab .selected .fa { color: black; }
    #tblList .fa { color: darkgray; }
    .fa-caret-down  { width: 0.6em; color: rgba(0,0,0,0.75); } 
    .fa-caret-right { width: 0.4em; color: rgba(0,0,0,0.75); margin-left: 0.2em; }
</style>


<r:splitPanes height="100%" width="100%" variablePane="0" orientation="vertical">
   	<r:pane size="calc(100% - 44px)">
 		<r:splitPanes height="100%" width="100%" variablePane="1" orientation="horizontal">
	    	<r:pane minSize="15em" size="20em">
	    		<div id="tblList" style="height: 100%; width: 100%; overflow: auto; border: 1px solid silver; padding: 0.8em; box-sizing: border-box; background: rgba(248,248,248,0.5);">
			        <c:forEach var="schema" items="${requestScope.schemas}">
				        <div class="unselectable" style="clear: both; cursor: pointer;" 
				             onDblClick="var c = _$('${schema}_tables'); 
				                         c.style.display = c.style.display == 'block' ? 'none' : 'block'; 
				                         var i = getChild(this, '0.0.0'); 
				                         i.setAttribute('data-icon', c.style.display == 'block' ? 'caret-down' : 'caret-right');
				                         if ((c.style.display == 'block') && (c.innerHTML == ''))
			                                Roth.ajax.htmlAction(c.id, '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=L&jndiname=' + _$v('jndiname') + '&schema=${schema}'); 
				                         return false;">
				            <div style="float: left;">
					            <r:icon iconName="caret-right" 
					                       onClick="var c = _$('${schema}_tables'); 
	                                                c.style.display = c.style.display == 'block' ? 'none' : 'block';
	                                                var i = getChild(this, 0);
	                                                i.setAttribute('data-icon', c.style.display == 'block' ? 'caret-down' : 'caret-right');
	                                                if ((c.style.display == 'block') && (c.innerHTML == ''))
                                                        Roth.ajax.htmlAction(c.id, '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=L&jndiname=' + _$v('jndiname') + '&schema=${schema}');"/>
				                ${schema}
			                </div>
			                <div style="float: right;">
				                <r:icon iconName="sync-alt" title="Refresh Table List" style="color: rgba(0,0,0,0.75);" 
				                         onClick="var c = _$('${schema}_tables'); 
				                                  if (c.style.display == 'block')
				                                      Roth.ajax.htmlAction(c.id, '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=L&jndiname=' + _$v('jndiname') + '&schema=${schema}');"/>
			                </div>
			                <r:break/>
				            <div id="${schema}_tables" style="display: none;"></div>
				        </div>
			        </c:forEach>
				</div>
	    	</r:pane>
	    	<r:pane minSize="25em" size="calc(100% - 20.7em)">
	    		<r:splitPanes height="100%" width="100%" variablePane="1" orientation="vertical">
	    			<r:pane fixed="true" size="3.2em">
		    			<r:hidden id="tableParams" dataSource="_na"/>
			    		<r:tabset id="objtabs" style="margin: 0.2em 0 0.2em 0.4em;"
					              onSelect="let {pageId} = Roth.tabset.getSelected(this);
					                        if (tableId && (pageId == 'indexes') && (_$('indexes').innerHTML == ''))
					                            Roth.ajax.htmlAction('indexes', '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=I&jndiname=' + _$v('jndiname') + '&tableId=' + tableId);
					                        else if (tableId && (pageId == 'data') && (_$('data').innerHTML == ''))
					                            Roth.ajax.htmlAction('data', '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=D&jndiname=' + _$v('jndiname') + '&tableName=' + tableName);
					                        else if (tableId && (pageId == 'foreignkeys') && (_$('foreignkeys').innerHTML == ''))
					                            Roth.ajax.htmlAction('foreignkeys', '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=F&jndiname=' + _$v('jndiname') + '&tableId=' + tableId);
					                        else if (tableId && (pageId == 'triggers') && (_$('triggers').innerHTML == ''))
					                            Roth.ajax.htmlAction('triggers', '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=T&jndiname=' + _$v('jndiname') + '&tableId=' + tableId);">
					        <r:option caption="Columns" value="" iconName="columns" pageId="columns"/>
					        <r:option caption="Indexes" value="" iconName="search" pageId="indexes"/>
					        <%-- <r:option caption="Data" value="" iconName="database" pageId="data"/> --%>
					        <r:option caption="Foreign Keys" value="" iconName="shield-alt" pageId="foreignkeys"/>
					        <r:option caption="Triggers" value="" iconName="code" pageId="triggers"/>
					    </r:tabset>
					    <r:break height="1em"/>
	    			</r:pane>
	    			<r:pane size="calc(100% - 3.2em)">
			    		<div id="columns" style="width: 100%; height: 100%; padding-left: 0.4em; box-sizing: border-box;"></div>
					    <div id="indexes" style="width: 100%; height: 100%; padding-left: 0.4em; box-sizing: border-box; display: none;"></div>
					    <div id="data" style="width: 100%; height: 100%; padding-left: 0.4em; box-sizing: border-box; display: none;"></div>
					    <div id="foreignkeys" style="width: 100%; height: 100%; padding-left: 0.4em; box-sizing: border-box; display: none;"></div>
					    <div id="triggers" style="width: 100%; height: 100%; padding-left: 0.4em; box-sizing: border-box; display: none;"></div>
	    			</r:pane> 
	    		</r:splitPanes>
	    	</r:pane>
 		</r:splitPanes>
   	</r:pane>
   	<r:pane fixed="true" size="44px">
    	<r:break height="0.8em"/>
  
	    <r:button caption="Script" iconName="code" 
		          title="Generate a creation script for the selected table."
		          onClick="Roth.getDialog('wait').wait();
		                   Roth.ajax.messageAction('/RothDeveloper/Developer/getScript', _$v('tableParams'), null, 'SQL Script', 'code');"/>
		<c:if test="${rf:isUserInRole(pageContext, 'Developer')}">
		    <r:button caption="Java" iconName="coffee" 
		              title="Create a Java Object from the selected table definition."
		              onClick="Roth.getDialog('wait').wait();
		                       Roth.execDialog('pojogen', '/RothDeveloper/Developer/abbreviated', _$v('tableParams'), 'Create Java Object', 'coffee', 'packageName', null, null, null, null, function() { _$v('className', camelcase(tableName) + 'Bean'); });"/>
		</c:if>
		<r:break/>
   	</r:pane>
</r:splitPanes>
<r:break height="6"/>