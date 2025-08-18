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

<style type="text/css">
	<jsp:include page="_domain.css"/>
</style>

<r:form action="/Domain/save" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'domainlist', function() { Roth.ajax.htmlAction('editdomainListTable_content', contextRoot + '/Domain/edit', 'domainName=' + _$v('domainName')); });" autoComplete="off" onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <r:hidden id="domainId" dataSource="requestScope.domain.domainId"/>
    
    <div class="domain-form-grid">
		<div>
			<r:textBox label="Domain Name" id="domainName" dataSource="requestScope.domain.domainName" width="${rf:mobile(pageContext) ? '100%' : '130px'}" readOnly="${!empty requestScope.domain.updatedDts}" required="true"/>
	    	${rf:mobile(pageContext) ? '<span>' : ''}
	    	<r:textBox label="Description" id="description" dataSource="requestScope.domain.description" width="${rf:mobile(pageContext) ? '100%' : '450px'}"/>
	    	${rf:mobile(pageContext) ? '</span>' : ''}
		</div>    	
    	<div style="background: #6003;">
			Password Requirements
			<r:break height="16px"/>
			<div class="domain-pass-req">
				<div>
					<r:textBox label="Length" dataSource="requestScope.domain.pwdMinLength" width="50px"
			                   title="Minimum length of passwords." 
			                   onKeyDown="return Roth.mask.numberMask(event, true, 3);"/>
			        <r:textBox label="Life" dataSource="requestScope.domain.pwdShelfLife" width="50px"
			                   title="How many days a password is valid before it expires." 
			                   onKeyDown="return Roth.mask.numberMask(event, true, 3);"/>
			        <r:textBox label="Count" dataSource="requestScope.domain.pwdRememberCount" width="50px"
			                   title="How many passwords to remember to prevent reuse." 
			                   onKeyDown="return Roth.mask.numberMask(event, true, 2);"/>
				</div>
				<div>
				    <r:checkBox label="Mixed-case Required" dataSource="requestScope.domain.pwdRequireMixed" boolValues="N|Y"
			                    title="Require at least one lower-case and one upper-case character."/>
			        <r:break/>
			        <r:checkBox label="Number Required" dataSource="requestScope.domain.pwdRequireNumber" boolValues="N|Y"
			                    title="Require at least one numeric digit."/>
			        <r:break/>
			        <r:checkBox label="Special Character Required" dataSource="requestScope.domain.pwdRequireSpecial" boolValues="N|Y"
			                    title="Require at least one special character (punctuation, etc.)."/>
		        </div>
			</div>
		</div>
		<div style="background: #0603;">
			Password Options
			<r:break height="16px"/>
			<r:textBox label="Grace" dataSource="requestScope.domain.pwdMaxExpiredAge" width="40px"
	                       title="Maximum number of days past expiration that a password can be changed by the user." 
	                       onKeyDown="return Roth.mask.numberMask(event, true, 3);"/>
	        <c:if test="${false}">
	            <r:textBox label="Test" dataSource="requestScope.domain.pwdChallengeMin" width="40px"
				           title="How many test or challenge questions the user is required to answer correctly for password reset." 
				           onKeyDown="return Roth.mask.numberMask(event, true, 2);"/>
	        </c:if>
	        <r:wrap style="vertical-align: bottom; margin-bottom: 7px;">
		        <r:checkBox label="Allow Reset" dataSource="requestScope.domain.pwdAllowReset" boolValues="N|Y"
		                    title="Allow users to reset forgotten passwords.  The prerequisite is a PIN and email address configured in the user's profile."/>
	        </r:wrap>
		</div>
		<div>
			<c:set var="sourceExplanation"><span style="font-size: 0.9em;">(used when validating against an alternate source)</span></c:set>
            <r:textBox label="Source ${sourceExplanation}" dataSource="requestScope.domain.pwdSource" width="100%"
                       title="Source - If supplied, describes an alternate, external source, through which passwords will be validated."/>
		</div>
    </div>
    
    <%--
    
    <r:break height="16px"/>
    
    
    
    
    <r:wrap type="input" style="margin-right: 0; width: 37.5em;">
	    <r:textBox label="Domain Name" id="domainName" dataSource="requestScope.domain.domainName" style="width: 8.2em;" readOnly="${!empty requestScope.domain.updatedDts}" required="true"/>
	    <r:textBox label="Description" id="description" dataSource="requestScope.domain.description" style="width: 26.2em;"/>
    </r:wrap>
    
    <r:break/>
    
    <div style="float: left;">
	    <r:wrap type="input" label="Password Requirements" color="#dccaca" style="width: 28em;">
	    	<br/>
	        <r:wrap style="margin-bottom: 0;">
		        <r:break height="8"/>
		        <r:textBox label="Length" dataSource="requestScope.domain.pwdMinLength" style="width: 2.8em;"
		                   title="Minimum length of passwords." 
		                   onKeyDown="return Roth.mask.numberMask(event, true, 3);"/>
		        <r:textBox label="Life" dataSource="requestScope.domain.pwdShelfLife" style="width: 2.8em;"
		                   title="How many days a password is valid before it expires." 
		                   onKeyDown="return Roth.mask.numberMask(event, true, 3);"/>
		        <r:textBox label="Count" dataSource="requestScope.domain.pwdRememberCount" style="width: 2.8em;"
		                   title="How many passwords to remember to prevent reuse." 
		                   onKeyDown="return Roth.mask.numberMask(event, true, 2);"/>
	        </r:wrap>
	        <r:wrap style="margin-top: -0.5em;">
		        <r:checkBox label="Mixed-case Required" dataSource="requestScope.domain.pwdRequireMixed" boolValues="N|Y"
		                    title="Require at least one lower-case and one upper-case character."/>
		        <r:break/>
		        <r:checkBox label="Number Required" dataSource="requestScope.domain.pwdRequireNumber" boolValues="N|Y"
		                    title="Require at least one numeric digit."/>
		        <r:break/>
		        <r:checkBox label="Special Character Required" dataSource="requestScope.domain.pwdRequireSpecial" boolValues="N|Y"
		                    title="Require at least one special character (punctuation, etc.)."/>
	        </r:wrap>
	    </r:wrap>
	   
	    <r:break/>
	    
	    <r:wrap type="input" label="Password Options" color="#cadcca" style="width: 17em;">
	        <r:break height="8"/>
	        <r:textBox label="Grace" dataSource="requestScope.domain.pwdMaxExpiredAge" style="width: 40px;"
	                       title="Maximum number of days past expiration that a password can be changed by the user." 
	                       onKeyDown="return Roth.mask.numberMask(event, true, 3);"/>
	        <c:if test="${false}">
	            <r:textBox label="Test" dataSource="requestScope.domain.pwdChallengeMin" style="width: 40px;"
				           title="How many test or challenge questions the user is required to answer correctly for password reset." 
				           onKeyDown="return Roth.mask.numberMask(event, true, 2);"/>
	        </c:if>
	        <r:wrap style="margin-top: 1.3em;">
		        <r:checkBox label="Allow Reset" dataSource="requestScope.domain.pwdAllowReset" boolValues="N|Y"
		                    title="Allow users to reset forgotten passwords.  The prerequisite is a PIN and email address configured in the user's profile."/>
	        </r:wrap>
	    </r:wrap>
	    
	    <c:if test="${false}">
		    <r:wrap type="input" label="Lockout Options" color="#cacadc" style="width: 8.5em; margin-right: 0;">
		        <r:break height="8"/>
		        <r:textBox label="Max" dataSource="requestScope.domain.lockoutCount" style="width: 40px;"
		                       title="Maximum number of failed authentication attempts are allowed before lockout." 
		                       onKeyDown="return Roth.mask.numberMask(event, true, 3);"/>
	            <r:textBox label="Timeout" dataSource="requestScope.domain.lockoutTime" style="width: 40px;"
				           title="How long before a locked account will be automatically unlocked.  ." 
				           onKeyDown="return Roth.mask.numberMask(event, true, 2);"/>
		    </r:wrap>
	    </c:if>
	    
	    <r:break/>
	    
	    <r:wrap type="input" label="Alternate Pasword Authentication" style="width: 28em;" 
	            title="If used, then 'Password Requirements' and 'Password Options' are not applicable.">
            <r:break height="6"/>
            <c:set var="sourceExplanation"><span style="font-size: 0.9em;">(used when validating against an alternate source)</span></c:set>
            <r:textBox label="Source ${sourceExplanation}" dataSource="requestScope.domain.pwdSource" style="width: 26.5em;"
                       title="Source - If supplied, describes an alternate, external source, through which passwords will be validated."/>
        </r:wrap>
    </div>
     --%>
    
    
    
    <c:if test="${false}">
	    <r:wrap type="input" label="Session">
	        <div style="float: left; padding-top: 4px;" title="Timeout - Maximum number of minutes to allow a session to remain idle before automatically logging out.">
	            <r:textBox label="Timeout" dataSource="requestScope.domain.sessionTimeout" style="width: 50px;" onKeyDown="return Roth.mask.numberMask(event, true, 3);"/>
	        </div>
	    </r:wrap>
    </c:if>
    
    <r:hidden dataSource="requestScope.domain.updatedBy"/>
    <r:hidden dataSource="requestScope.domain.updatedDts"/>
    <input type="hidden" name="_method" value="ajax"/>
    
    <r:break height="8px"/>
    
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>