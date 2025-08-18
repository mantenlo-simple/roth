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
	<jsp:include page="../../configuration/user/_user.css"/>
</style>

<r:form action="/GroupAdmin/save" method="AJAX" 
        onAjax="let state = Roth.grid.getState('daUserTable');
        		Roth.ajax.htmlCallback(request, 'dauserlist', function() { 
        			if (${empty requestScope.user.updatedDts}) 
        				Roth.execDialog({
        				    id: 'editdaUserTable',
        				    url: '/Roth/GroupAdmin/edit',
        				    params: 'userid=' + _$v('dauserid') + '&domainId=' + _$v('dadomainId'),
        				    caption: 'Edit',
        				    img: 'edit'
        				}); 
        			});
        		Roth.grid.setState('daUserTable', state, 'key');" 
        autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:hidden id="dadomainId" dataSource="requestScope.user.domainId" value="${requestScope.domainId}"/>
    <r:textBox label="Domain" dataSource="_na" value="${rf:getDomainName(pageContext)}" readOnly="true" width="${rf:mobile(pageContext) ? '100%' : '100px'}"/>

    <r:break/>
    
    <r:textBox label="User ID" id="dauserid" dataSource="requestScope.user.userid" required="${empty requestScope.user.updatedDts}"  
               readOnly="${!empty requestScope.user.updatedDts}" width="${rf:mobile(pageContext) ? '100%' : '141px'}"/>
    <c:if test="${rf:mobile(pageContext)}">
		<r:break/>
	</c:if>
    <r:textBox label="Name" id="name" dataSource="requestScope.user.name" width="${rf:mobile(pageContext) ? '100%' : '141px'}" 
             autoComplete="off" readOnly="true" style="color: black !important;" 
		               onFocus="this.removeAttribute('readonly');" onBlur="this.setAttribute('readonly','');"/>
    
    <r:break/>
    
    <c:if test="${empty requestScope.user.updatedDts}">
	    <r:textBox label="Password" id="password" dataSource="passwordNew" password="true" width="${rf:mobile(pageContext) ? '100%' : '141px'}"
	               required="${empty requestScope.user.userid}" onKeyUp="Roth.compValidate('password', '_password');"/>
	    <c:if test="${rf:mobile(pageContext)}">
			<r:break/>
		</c:if>
	    <r:textBox label="Verify Password" id="_password" dataSource="_password" password="true" width="${rf:mobile(pageContext) ? '100%' : '141px'}"
	               required="${empty requestScope.user.userid}" onKeyUp="Roth.compValidate('password', '_password');"/>
    </c:if>
    
    <r:break/>
    
    <r:wrap type="input" label="Profile" style="width: 100%; margin-right: 0;">
        <r:hidden dataSource="requestScope.profile.userid"/>
        <r:hidden dataSource="requestScope.profile.domainId"/>
        <r:hidden dataSource="requestScope.profile.updatedBy"/>
        <r:hidden dataSource="requestScope.profile.updatedDts"/>
        <r:hidden dataSource="requestScope.profile.userImage"/>
        
        <div class="user-profile-grid">
			<div> <%--
				<r:textArea label="Address" dataSource="requestScope.profile.address" width="${rf:mobile(pageContext) ? '100%' : '356px'}" style="height: 4em; resize: none;"/>
	       
		        <r:break/>
		        
		        <r:textBox label="City" dataSource="requestScope.profile.city" width="${rf:mobile(pageContext) ? '100%' : '103px'}"/>
		        <c:if test="${rf:mobile(pageContext)}">
					<r:break/>
				</c:if>
			    <r:textBox label="State" dataSource="requestScope.profile.state" width="${rf:mobile(pageContext) ? '100%' : '48px'}"/>
		        <c:if test="${rf:mobile(pageContext)}">
					<r:break/>
				</c:if>
			    <r:textBox label="Country" dataSource="requestScope.profile.country" width="${rf:mobile(pageContext) ? '100%' : '64px'}"/>
		        <c:if test="${rf:mobile(pageContext)}">
					<r:break/>
				</c:if>
			    <r:textBox label="Postal Code" dataSource="requestScope.profile.postalCode" width="${rf:mobile(pageContext) ? '100%' : '106px'}"/>
		        
		        <r:break/>
		        
		        <r:textBox label="Home Email" dataSource="requestScope.profile.emailHome" width="${rf:mobile(pageContext) ? '100%' : '356px'}"/>
		        
		        <r:break/>
		         --%>
		        <r:textBox label="Work Email" dataSource="requestScope.profile.emailWork" width="${rf:mobile(pageContext) ? '100%' : '356px'}"/>
			</div>
			<div>
			<%--
		        <r:textBox label="Home Phone" dataSource="requestScope.profile.phoneHome" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		        <r:break/> --%>
		        <r:textBox label="Work Phone" dataSource="requestScope.profile.phoneWork" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		        <r:break/>
		        <r:textBox label="Cell Phone" dataSource="requestScope.profile.phoneCell" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		        <%--
		        <r:break/>
				<r:textBox label="Fax" dataSource="requestScope.profile.phoneFax" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
				 --%>
			</div>
		</div>
        
        <r:break/>
    </r:wrap>
    
    <r:hidden dataSource="requestScope.user.createdBy"/>
    <r:hidden dataSource="requestScope.user.updatedBy"/>
    <r:hidden dataSource="requestScope.user.updatedDts"/>
    
    <r:break/>
    
    <r:button type="save" formSubmit="true" onClick="if (_$v('userid') == '') { Roth.getDialog('pwderror').error('User ID cannot be empty.'); return false; } if (_$('password') && ${empty param['userid']}) { if (!Roth.compValidate('password', '_password')) { Roth.getDialog('pwderror').error('Passwords do not match.'); return false; } if (_$v('password') == '') { Roth.getDialog('pwderror').error('Password cannot be empty.'); return false; } }"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>
<r:break/>