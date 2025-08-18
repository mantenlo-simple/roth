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

<r:jspSecurity rolesAllowed="GroupAdmin"/>

<style type="text/css">
	.gadmin-form {
		height: 100%;
	}
</style>

<r:form action="/GroupAdmin/save" method="AJAX" cssClass="gadmin-form" 
        onAjax="Roth.ajax.htmlCallback(request, 'dauserlist', function() { 
                    if (${empty requestScope.user.updatedDts}) 
                        Roth.execDialog('editdaUserTable', '/JpPortal/DeptAdmin/edit', 'userid=' + _$v('dauserid') + '&domainId=' + _$v('dadomainId'), 'Edit', 'edit'); 
                })" 
        autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:hidden id="dadomainId" dataSource="requestScope.user.domainId" value="${requestScope.domainId}"/>
    
    <r:mobiScroll>
	    <r:textBox label="Domain" dataSource="_na" value="${rf:getDomainName(pageContext)}" readOnly="true"/>
	
	    <r:textBox label="User ID" id="dauserid" dataSource="requestScope.user.userid" required="${empty requestScope.user.updatedDts}"  
	               readOnly="${!empty requestScope.user.updatedDts}"/>
	    <r:textBox label="Name" id="name" dataSource="requestScope.user.name"/>
	    
	    <c:if test="${empty requestScope.user.updatedDts}">
		    <r:textBox label="Password" id="password" dataSource="passwordNew" password="true"
		               required="${empty requestScope.user.userid}" onKeyUp="Roth.compValidate('password', '_password');"/>
		    <r:textBox label="Verify Password" id="_password" dataSource="_password" password="true"
		               required="${empty requestScope.user.userid}" onKeyUp="Roth.compValidate('password', '_password');"/>
	    </c:if>
	    
	    <r:wrap type="input" label="Profile">
	        <r:hidden dataSource="requestScope.profile.userid"/>
	        <r:hidden dataSource="requestScope.profile.domainId"/>
	        <r:hidden dataSource="requestScope.profile.updatedBy"/>
	        <r:hidden dataSource="requestScope.profile.updatedDts"/>
	        <r:hidden dataSource="requestScope.profile.userImage"/>
	        
	        <r:textArea label="Address" dataSource="requestScope.profile.address" style="height: 5em;"/>
	        <r:textBox label="City" dataSource="requestScope.profile.city"/>
	        <r:textBox label="State" dataSource="requestScope.profile.state"/>
	        <r:textBox label="Country" dataSource="requestScope.profile.country"/>
	        <r:textBox label="Postal Code" dataSource="requestScope.profile.postalCode"/>
	
	        <r:textBox label="Home Phone" dataSource="requestScope.profile.phoneHome"/>
	        <r:textBox label="Cell" dataSource="requestScope.profile.phoneCell"/>
	        <r:textBox label="Work Phone" dataSource="requestScope.profile.phoneWork"/>
	        <r:textBox label="Fax" dataSource="requestScope.profile.phoneFax"/>
	        
	        <r:textBox label="Home Email" dataSource="requestScope.profile.emailHome"/>
	        <r:textBox label="Work Email" dataSource="requestScope.profile.emailWork"/>
	    </r:wrap>
	    
	    <r:hidden dataSource="requestScope.user.createdBy"/>
	    <r:hidden dataSource="requestScope.user.updatedBy"/>
	    <r:hidden dataSource="requestScope.user.updatedDts"/>
    </r:mobiScroll>
    
    <div class="jbreak" style="height: 0.5em;"></div>
    
    <r:button type="save" formSubmit="true" onClick="if (_$v('userid') == '') { Roth.getDialog('pwderror').error('User ID cannot be empty.'); return false; } if (_$('password') && ${empty param['userid']}) { if (!Roth.compValidate('password', '_password')) { Roth.getDialog('pwderror').error('Passwords do not match.'); return false; } if (_$v('password') == '') { Roth.getDialog('pwderror').error('Password cannot be empty.'); return false; } }"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>
<r:break/>