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
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<c:set var="mobi" value="${rf:mobile(pageContext)}"/>

<r:page title="Portal Login" onLoad="_$('jsc_username').focus();">
    <c:choose>
	    <c:when test="${mobi}">
	        <meta name="viewport" content="width=device-width, initial-scale=1.0">
	        <style type="text/css">
	            body { font-size: ${param['fontSize']}px; width: 100%; height: auto; }
	            .loginForm { width: 100%; height: 100%; }
	        </style>
	    </c:when>
	    <c:otherwise>
	    	<style type="text/css">
		        body { margin: 0; padding: 0; width: 20em; height: auto; background: transparent; }
		        .loginForm { width: 100%; height: 100%; }
		    </style>
	    </c:otherwise>
    </c:choose>
    <!-- ${rf:setResourcePath(pageContext, 'com/roth/portal/resource/portal')} -->
    <script type="text/javascript">
    if ("${param['error']}" == 'true') {
        var callback = function() {
            if ((this.readyState == 4) && (this.status == 200)) {
                window.top.Roth.getDialog('wait').hide();
                var canReset = this.responseText == 'canReset';
                // days to expire
                var dte = !canReset ? this.responseText : '';
                var isnull = dte.trim() == '';
                dte = parseInt(dte);
                var error = (canReset) ? 'The username or password was invalid.<br/>Click <a href="javascript:Roth.changePassword(false, true);">here</a> if you have forgotten your password.'
                    : (isnull || dte > 0) ? '${rf:value(pageContext, 'loginInvalid')}'
                    : 'Your password is expired.<br/>Click <a href="javascript:Roth.changePassword(true);">here</a> to change password.';
                window.top.Roth.getDialog('error').error(error);
            }
        }
        executeAjax({callback: callback, method: 'GET', url: portalRoot + '/AuthenticationServlet/getDaysToExpire', parameters: "userid=${param['j_username']}&password=${param['j_password']}"}); 
    }
    </script>
    <r:form action="/j_security_check" method="POST" cssClass="loginForm">
	    <r:textBox id="jsc_username" label="${rf:value(pageContext, 'username')}" dataSource="j_username" width="${mobi ? 'calc(100% - 0.6em)' : '19em'}" onKeyDown="authKeyDown(this, event);"/>
	    <r:break/>
	    <r:textBox id="jsc_password" label="${rf:value(pageContext, 'password')}" dataSource="j_password" width="${mobi ? 'calc(100% - 0.6em)' : '19em'}" onKeyDown="authKeyDown(this, event);" password="true"/>
	    <r:break height="8"/>
	    <r:button type="login" formSubmit="true"/>
	    <r:button type="cancel" onClick="window.top.Roth.getParentDialog(window.top._$('loginframe')).hide(); return false;"/>
	    <r:break/>
	</r:form>
</r:page>