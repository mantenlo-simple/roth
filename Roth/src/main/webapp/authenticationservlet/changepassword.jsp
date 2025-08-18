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
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<c:set var="action" value="${requestScope.forgotten ? 'changeForgottenPassword' : 'changeExpiredPassword'}"/>
<c:set var="name" value="${requestScope.forgotten ? 'validationCode' : 'oldPassword'}"/>
<c:set var="label" value="${requestScope.forgotten ? 'Validation Code' : 'Current Password'}"/>
<c:set var="src">src="${pageContext.servletContext.contextPath}/profile/index.js"</c:set>
<div id="pwdscriptcontainer" ${src}></div>
<r:form action="/AuthenticationServlet/${action}" method="AJAX" onAjaxResponse="pwdAjaxResponse(request)" onAjaxError="pwdAjaxError(request, ${requestScope.forgotten})">
    <r:textBox id="userid" dataSource="userid" label="Username"/>
    <r:break/>
    <r:textBox id="${name}" dataSource="${name}" label="${label}" password="true"/>
    <r:break/>
    <r:textBox id="newPassword" dataSource="newPassword" label="New Password" password="true" onKeyUp="Roth.compValidate('newPassword', 'confPassword');"/>
    <r:break/>
    <r:textBox id="confPassword" dataSource="" label="Confirm Password" password="true" onKeyUp="Roth.compValidate('newPassword', 'confPassword');"/>
    <r:break height="0.5em"/>
    <r:button type="save" formSubmit="true" onClick="if (!Roth.compValidate('newPassword', 'confPassword')) { Roth.getDialog('pwderror').error('The <b>New Password</b> and <b>Confirm Password</b> do not match.'); return false; }"/>
    <r:button type="cancel" onClick="Roth.getDialog('changePassword').hide();"/>
</r:form>
<r:break/>