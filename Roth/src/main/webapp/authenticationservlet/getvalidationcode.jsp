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

<r:form id="pinform" action="/AuthenticationServlet/getValidationCode" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'changePassword_content');">
    <r:textBox id="userid" dataSource="userid" label="Username"/>
    <r:break/>
    <r:textBox id="pin" label="PIN" dataSource="pin" password="true"
               onFocus="_$('pinform').onkeypress = function(event) {
                            var e = event ? event : window.event; 
                            if (e.keyCode == 13) return false;
                        };"
               onKeyDown="var e = event ? event : window.event; 
                          if (e.keyCode == 13) submitForm(_$('pinform'));
                          else if (!Roth.mask.numberMask(event, true)) return false;"/>
    <r:break height="8"/>

    <r:button id="_pinok" type="ok" formSubmit="true"/>
    <r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>
<r:break/>
