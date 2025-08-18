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
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<c:set var="params">fDomainId=${param['fDomainId']}</c:set>
<c:set var="params">${params}&fGroupName=${param['fGroupName']}</c:set>
<c:set var="params">${params}&fRoleName=${param['fRoleName']}</c:set>
<r:form action="/Group/save?${params}" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'grouplist', function() { Roth.ajax.htmlAction('editgroupListTable_content', contextRoot + '/Group/edit', 'groupName=' + _$v('groupName')); });" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:textBox label="Group Name" id="groupName" dataSource="requestScope.group.groupName" width="${rf:mobile(pageContext) ? '100%' : '180px'}" required="true"/>
    <c:if test="${rf:mobile(pageContext)}">
    	<r:break/>
    </c:if>
    <r:select label="Parent Group" dataSource="requestScope.group.parentGroupId" width="${rf:mobile(pageContext) ? '100%' : '184px'}" nullable="true"
              optionsDataSource="${requestScope.groups}" keyDataSource="groupId" valueDataSource="groupName"/>
    <r:break/>
    <c:choose>
	    <c:when test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
		    <r:select label="Domain" dataSource="requestScope.group.domainId" optionsDataSource="${requestScope.domains}"
		              nullable="true" width="${rf:mobile(pageContext) ? '100%' : '100px'}" required="true"/>
	    </c:when>
	    <c:otherwise>
	        <r:textBox label="Domain" dataSource="_na" value="${rf:getDomainName(pageContext)}" width="${rf:mobile(pageContext) ? '100%' : '94px'}" readOnly="true"/>
	    </c:otherwise>
    </c:choose>
    <c:if test="${rf:mobile(pageContext)}">
    	<r:break/>
    </c:if>
    <r:textBox label="Description" id="description" dataSource="requestScope.group.description" width="${rf:mobile(pageContext) ? '100%' : '264px'}"/>
    
    <r:hidden dataSource="requestScope.group.groupId"/>
    <r:hidden dataSource="requestScope.group.lineage"/>
    <r:hidden dataSource="requestScope.group.updatedBy"/>
    <r:hidden dataSource="requestScope.group.updatedDts"/>
    <input type="hidden" name="_method" value="ajax"/>
    
    <div class="jbreak" style="height: 8px;"></div>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>