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

<r:jspSecurity rolesAllowed="Authenticated"/>

<style type="text/css">
	<jsp:include page="../configuration/user/_user.css"/>
</style>

<r:wrap type="input" style="font-weight: bold; padding: 8px;">${requestScope.userFullName}</r:wrap>
<r:break height="8"/>
<r:form action="/Profile/save" method="AJAX" onAjax="Roth.ajax.messageCallback(request, function() { Roth.getDialog('editProfile').hide(); })">
    <r:hidden dataSource="requestScope.profile.userid"/>
    <r:hidden dataSource="requestScope.profile.domainId"/>
    <r:hidden dataSource="requestScope.profile.updatedBy"/>
    <r:hidden dataSource="requestScope.profile.updatedDts"/>
    <r:hidden dataSource="requestScope.profile.userImage"/>
    
    <%--
    <div class="user-profile-grid">
		<div>
			
		    <r:textArea id="profileAddress" label="Address" dataSource="requestScope.profile.address" width="${rf:mobile(pageContext) ? '100%' : '357px'}" style="height: 4em; resize: none;"/>
		    
		    <r:break/>
		    
		    <r:textBox label="City" dataSource="requestScope.profile.city" width="${rf:mobile(pageContext) ? '100%' : '106px'}"/>
		    <c:if test="${rf:mobile(pageContext)}">
				<r:break/>
			</c:if>
		    <r:textBox label="State" dataSource="requestScope.profile.state" width="${rf:mobile(pageContext) ? '100%' : '48px'}" maxLength="2"/>
		    <c:if test="${rf:mobile(pageContext)}">
				<r:break/>
			</c:if>
		    <r:textBox label="Country" dataSource="requestScope.profile.country" width="${rf:mobile(pageContext) ? '100%' : '64px'}" maxLength="3"/>
		    <c:if test="${rf:mobile(pageContext)}">
				<r:break/>
			</c:if>
		    <r:textBox label="Postal Code" dataSource="requestScope.profile.postalCode" width="${rf:mobile(pageContext) ? '100%' : '106px'}"/>
		    
		    <r:break/>
		    
		    <r:textBox label="Home Email" dataSource="requestScope.profile.emailHome" width="${rf:mobile(pageContext) ? '100%' : '356px'}"/>
		    
		    <r:break/>
		     --%>
		    <r:textBox label="Email" dataSource="requestScope.profile.emailWork" placeholder="example@domain.com" width="${rf:mobile(pageContext) ? '100%' : '319px'}"/>
		    <r:break/>
		    <r:textBox label="Work Phone" dataSource="requestScope.profile.phoneWork" placeholder="000-000-0000" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		    <r:textBox label="Cell Phone" dataSource="requestScope.profile.phoneCell" placeholder="000-000-0000" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		    <%--
		</div>
		<div>
		    <r:textBox label="Home Phone" dataSource="requestScope.profile.phoneHome" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		    <r:break/>
		    <r:textBox label="Work Phone" dataSource="requestScope.profile.phoneWork" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		    <r:break/>
		    <r:textBox label="Cell" dataSource="requestScope.profile.phoneCell" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		    <r:break/>
		    <r:textBox label="Fax" dataSource="requestScope.profile.phoneFax" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
		</div>
	</div> --%>
    
    <r:break height="8"/>
    
    <r:button type="save" formSubmit="true" onClick="Roth.getDialog('wait').wait('${rf:valuex(pageContext, 'com/jp/html/resource/common', 'pleaseWaitMessage')}');"/>
    <r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
    
    <c:if test="${requestScope.domain.pwdAllowReset eq 'Y'}">
        <r:wrap> &nbsp; &nbsp; </r:wrap>
        <r:button caption="Password Reset" iconName="key"
                  title="Configure password reset information." 
                  onClick="Roth.execDialog('resetInfo', contextRoot + '/Profile/getResetInfo', null, 'Password Reset Info', 'key', 'pin')"/>
    </c:if>
</r:form>
<r:break/>