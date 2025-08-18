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

<c:set var="names" value="${fn:split(param['params'], ',')}"/>

<style type="text/css">
	.paramgrid {
		display: grid;
		grid-template-columns: 25% 25% 50%;
		gap: 6px;
		width: calc(100% - 6px);
	}
	.paramgrid > div {
		width: 100%;
	}
	.paramgrid > div:nth-child(3n+1) {
	}
	.paramname {
		text-align: right; 
		padding-top: 10px;
	}
</style>

<div class="paramgrid">
	<div></div>
	<div>Type</div>
	<div>Value</div>

	<c:forEach var="name" varStatus="stat" items="${names}">
		<div class="paramname">${name}</div>
		<r:hidden id="name${stat.index}" dataSource="_na" value="${name}"/>
		<div>
		    <r:select id="type${stat.index}" dataSource="_na"
		              onChange="var d = getValue(this) == 'DATE';
		                        var dt = getValue(this) == 'DATETIME';
		                        _$('vn${stat.index}').style.display = !d && !dt ? 'block' : 'none';
		                        _$('vd${stat.index}').style.display = d ? 'block' : 'none';
		                        _$('vdt${stat.index}').style.display = dt ? 'block' : 'none';">
		        <r:option caption="String" value="VARCHAR"/>
		        <r:option caption="Number" value="NUMBER"/>
		        <r:option caption="Date" value="DATE"/>
		        <r:option caption="Date-Time" value="DATETIME"/>
		    </r:select>
	    </div>
	    <div>
		    <div id="vn${stat.index}">
		        <r:textBox id="valuesn${stat.index}" dataSource="_na" width="100%"/>
		    </div>
		    <div id="vd${stat.index}" style="display: none;">
		        <r:calendarSelect id="valued${stat.index}" dataSource="_na" showClear="true" width="100%"/>
		    </div>
		    <div id="vdt${stat.index}" style="display: none;">
		        <r:calendarSelect id="valuedt${stat.index}" dataSource="_na" showClear="true" showTime="true" width="100%"/>
		    </div>
	    </div>
	    <r:break/>
	</c:forEach>
</div>

<r:break height="8"/>

<r:button id="_paramok" type="ok" 
          onClick="var p = '';
                   for (var i = 0; i < ${fn:length(names)}; i++) {
                       var name = _$v('name' + i);
                       var type = _$v('type' + i);
                       var value = type == 'DATE' ? _$v('valued' + i) :
                                   type == 'DATETIME' ? _$v('valuedt' + i) :
                                   _$v('valuesn' + i);
                       if (!paramCache[name]) paramCache[name] = new Object();
                       paramCache[name].type = type;
                       paramCache[name].value = value; 
                       p += (p == '' ? '' : ',') + name + '|' + type + '|' + value; 
                   }
                   _$('params').value = p; 
                   execStatement(null, true); 
                   Roth.getParentDialog(this).hide();"/>
<r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
<r:break/>