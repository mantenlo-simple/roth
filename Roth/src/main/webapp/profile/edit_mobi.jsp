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
	.mobi-form {
		height: calc(100% - 4em);
	}
</style>

<div style="height: 100%;">
	<r:wrap type="input" style="font-weight: bold; padding: 0.5em; width: calc(100% - 1em);">${requestScope.userFullName}</r:wrap>
	<r:break/>
	<r:form action="/Profile/save" method="AJAX" onAjax="Roth.ajax.messageCallback(request, function() { Roth.getDialog('editProfile').hide(); })" cssClass="mobi-form">
	    <r:hidden dataSource="requestScope.profile.userid"/>
	    <r:hidden dataSource="requestScope.profile.domainId"/>
	    <r:hidden dataSource="requestScope.profile.updatedBy"/>
	    <r:hidden dataSource="requestScope.profile.updatedDts"/>
	    <r:hidden dataSource="requestScope.profile.userImage"/>
	    
	    <r:mobiScroll>
		    <r:textArea id="profileAddress" label="Address" dataSource="requestScope.profile.address" style="height: 3.8em;"/>
		    <r:textBox label="City" dataSource="requestScope.profile.city" width="calc(100% - 0.6em)"/>
		    <r:textBox label="State" dataSource="requestScope.profile.state" maxLength="2" width="calc(100% - 0.6em)"/>
		    <r:textBox label="Country" dataSource="requestScope.profile.country" maxLength="3" width="calc(100% - 0.6em)"/>
		    <r:textBox label="Post Code" dataSource="requestScope.profile.postalCode" width="calc(100% - 0.6em)"/>
		    <r:textBox label="Home Email" dataSource="requestScope.profile.emailHome" width="calc(100% - 0.6em)"/>
		    <r:textBox label="Work Email" dataSource="requestScope.profile.emailWork" width="calc(100% - 0.6em)"/>
		    <r:textBox label="Home Phone" dataSource="requestScope.profile.phoneHome" width="calc(100% - 0.6em)"/>
		    <r:textBox label="Work Phone" dataSource="requestScope.profile.phoneWork" width="calc(100% - 0.6em)"/>
		    <r:textBox label="Cell" dataSource="requestScope.profile.phoneCell" width="calc(100% - 0.6em)"/>
		    <r:textBox label="Fax" dataSource="requestScope.profile.phoneFax" width="calc(100% - 0.6em)"/>
		    <r:break/>
	    </r:mobiScroll>
	     
	    <r:break height="0.5em"/>
	    
	    <r:button type="save" formSubmit="true" onClick="Roth.getDialog('wait').wait('${rf:valuex(pageContext, 'com/jp/html/resource/common', 'pleaseWaitMessage')}');"/>
	    <r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
	    
	    <c:if test="${requestScope.domain.pwdAllowReset eq 'Y'}">
	    	<r:break height="0.5em"/>
	        <r:button caption="Password Reset" iconName="key"
	                  title="Configure password reset information." 
	                  onClick="Roth.execDialog('resetInfo', contextRoot + '/Profile/getResetInfo', null, 'Password Reset Info', 'login', 'pin')"/>
	    </c:if>
	</r:form>
	<r:break/>
</div>