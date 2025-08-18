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
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<c:set var="params">fDomainId=${param['fDomainId']}</c:set>
<c:set var="params">${params}&fUserid=${param['fUserid']}</c:set>
<c:set var="params">${params}&fName=${param['fName']}</c:set>
<c:set var="params">${params}&fRoleName=${param['fRoleName']}</c:set>
<r:form action="/User/save?${params}" method="AJAX" autoComplete="off" 
        onAjax="Roth.ajax.htmlCallback(request, 'userlist', function() { 
                    Roth.ajax.htmlAction('edituserListTable_content', contextRoot + '/User/edit', 'userid=' + _$v('userid') + '&domainId=' + _$v('domainId')); 
                });"
        onSubmit="Roth.getDialog('wait').wait('Please wait while your changes are saved...');">

	<style type="text/css">
		<jsp:include page="_user.css"/>
	</style>

	<div class="user-form-grid">
		<div>
			<c:choose>
		        <c:when test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
		            <r:select id="domainId" label="Domain" dataSource="requestScope.user.domainId" optionsDataSource="${requestScope.domains}"
		                      nullable="true" width="${rf:mobile(pageContext) ? '100%' : '9em'}" required="true" readOnly="${!empty requestScope.user.updatedDts}"/>
		            <r:wrap label="&nbsp;" style="width: 12.2em; margin-left: ${rf:mobile(pageContext) ? '70px' : '0.5em'};">
		                <div style="float: left;" title="Protecting a user account prevents it from being seen in the Group Administrator dialog.">
		                    <r:checkBox label="Protect" dataSource="requestScope.user.protect" boolValues="N|Y"/>
		                </div>
		                <c:if test="${true}">
			                <div style="float: left; margin-left: 1em;" title="${empty requestScope.user.lockedDts ? '' : rf:formatDate(requestScope.user.lockedDts, sessionScope.formats['datetime'])}">
			                    <r:checkBox label="Locked" dataSource="requestScope.user.locked" boolValues="N|Y"/>
			                </div>
		                </c:if>
		            </r:wrap>
		        </c:when>
		        <c:otherwise>
		            <c:set var="domainIdStr">${requestScope.domainId}</c:set>
		            <r:textBox label="Domain" dataSource="_na" value="${requestScope.domains[domainIdStr]}" width="${rf:mobile(pageContext) ? '100%' : '10.8em'}" readOnly="true"/>
		            <r:hidden id="domainId" dataSource="requestScope.user.domainId" value="${requestScope.domainId}"/>
		            <r:hidden dataSource="requestScope.user.protect"/>
		        </c:otherwise>
		    </c:choose>
		    
		    <r:break/>
		    
		    <r:textBox label="User ID" id="userid" dataSource="requestScope.user.userid" width="${rf:mobile(pageContext) ? '100%' : '10.8em'}"
		               required="true" readOnly="${!empty requestScope.user.updatedDts}"/>
		    <c:if test="${rf:mobile(pageContext)}">
				<r:break/>
			</c:if>
		    <r:textBox label="Name" id="name" dataSource="requestScope.user.name" width="${rf:mobile(pageContext) ? '100%' : '10.8em'}" autoComplete="off" readOnly="true" style="color: black !important;" 
		               onFocus="this.removeAttribute('readonly');" onBlur="this.setAttribute('readonly','');"/>
		    
		    <r:break/>
		    
		    <r:textBox label="Password" id="password" dataSource="passwordNew" password="true" width="${rf:mobile(pageContext) ? '100%' : '10.8em'}" autoComplete="off"
		               required="${empty requestScope.user.userid}" onKeyUp="Roth.compValidate('password', '_password');"
		               title="Leave password fields empty to keep existing password."/>
		    <c:if test="${rf:mobile(pageContext)}">
				<r:break/>
			</c:if>
		    <r:textBox label="Verify Password" id="_password" dataSource="_password" password="true" width="${rf:mobile(pageContext) ? '100%' : '10.8em'}" autoComplete="off"
		               required="${empty requestScope.user.userid}" onKeyUp="Roth.compValidate('password', '_password');"
		               title="Leave password fields empty to keep existing password."/>
		</div>
		<div style="background: #6003;">
			<r:wrap label="Password Expiration" style="margin-right: 0;">
				<r:calendarSelect id="expireDts" dataSource="requestScope.user.expireDts" showTime="true" required="${not rf:isUserInRole(pageContext, 'SecurityAdmin')}"
				                  onChange="let sel = _$('expireSelector');
				                  			if (this.value !== '') {	
				                  				let first = sel.parentNode.getElementsByClassName('roth-radio')[0];
				                  				first.children[0].value = this.value;
				                  			}
				                  			window.stopdthide = true;
				                  			setValue(sel, this.value);"/>
		        <c:set var="oldvalue">${rf:formatDate(requestScope.user.expireDts, sessionScope.formats['datetime'])}</c:set>
		        <c:set var="now">${rf:formatDate(rf:now('LocalDateTime'), sessionScope.formats['datetime'])}</c:set>
		        <c:set var="nextperiod">${rf:formatDate(rf:dateAdd(rf:now('LocalDateTime'), 'day', requestScope.pwdShelfLife), sessionScope.formats['datetime'])}</c:set>
		        <r:radioGroup id="expireSelector" dataSource="_na" vertical="true" value="${oldvalue}"
		                      onChange="_$('expireDts').value = this.value;">
		            <c:if test="${!empty oldvalue}">
		                <r:option caption="Current Value" value="${oldvalue}"/>
		            </c:if>
		            <r:option caption="Expire Now" value="${now}"/>
		            <c:if test="${!empty requestScope.pwdShelfLife}">
		                <r:option caption="Renew Expiration" value="${nextperiod}"/>
		            </c:if>
		            <c:if test="${rf:isUserInRole(pageContext, 'SecurityAdmin')}">
		                <r:option caption="Never Expire" value=""/>
		            </c:if>
		        </r:radioGroup>
		    </r:wrap>
		</div>
		<div style="background: #0603;">
			<r:wrap label="Profile" style="margin-right: 0; width: 100%;">
				<r:hidden dataSource="requestScope.profile.userid"/>
		        <r:hidden dataSource="requestScope.profile.domainId"/>
		        <r:hidden dataSource="requestScope.profile.updatedBy"/>
		        <r:hidden dataSource="requestScope.profile.updatedDts"/>
		        <r:hidden dataSource="requestScope.profile.userImage"/>
			
				<div class="user-profile-grid">
					<div>
					<%--
						<r:textArea label="Address" dataSource="requestScope.profile.address" width="${rf:mobile(pageContext) ? '100%' : '356px'}" style="height: 4em; resize: none;"/>
			       
				        <r:break/>
				        
				        <r:textBox label="City" dataSource="requestScope.profile.city" width="${rf:mobile(pageContext) ? '100%' : '106px'}"/>
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
				        <r:break/>
				         --%>
				        <r:textBox label="Work Phone" dataSource="requestScope.profile.phoneWork" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
				        <r:break/>
				        <r:textBox label="Cell Phone" dataSource="requestScope.profile.phoneCell" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
				        <%--
				        <r:break/>
						<r:textBox label="Fax" dataSource="requestScope.profile.phoneFax" width="${rf:mobile(pageContext) ? '100%' : '11em'}"/>
						 --%>
					</div>
				</div>
		       
		        <r:hidden dataSource="requestScope.user.createdBy"/>
		        <r:hidden dataSource="requestScope.user.createdDts"/>
		        <r:hidden dataSource="requestScope.user.updatedBy"/>
		        <r:hidden dataSource="requestScope.user.updatedDts"/>
		        <input type="hidden" name="_method" value="ajax"/>
		    </r:wrap>
		</div>
	</div>
    
    <r:break height="8px"/>
    
    <r:button type="save" formSubmit="true" onClick="if (!validUser('${requestScope.user.userid}')) return false;"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>