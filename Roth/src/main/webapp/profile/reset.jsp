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
<r:form action="/Profile/saveResetInfo" method="AJAX" onAjax="Roth.ajax.htmlCallback(request, 'resetInfo_content', function() { Roth.getDialog('alert').alert('Info settings saved successfully.'); }, null, _$('resetInfo_content'));">
    <r:textBox id="pin" label="PIN" dataSource="pin" style="width: 90px;" value="${requestScope.pin}" password="true" required="true"
               onKeyDown="if (!Roth.mask.numberMask(event, true)) return false;"
               onKeyUp="Roth.compValidate('pin', 'confirmpin');"
               onChange="_$('confirmpin').value = '';"/>
    <r:textBox id="confirmpin" label="Confirm PIN" dataSource="_na" style="width: 90px;" value="${requestScope.pin}" password="true" required="true"
               onKeyDown="if (!Roth.mask.numberMask(event, true)) return false;"
               onKeyUp="Roth.compValidate('pin', 'confirmpin');"/>
    
    <r:break/>
    
    <r:textBox label="Validation Email Address" dataSource="email" style="width: 192px;" value="${requestScope.email}" required="true"/>

    <r:break height="8"/>

    <r:button type="save" formSubmit="true" onClick="if (!Roth.compValidate('pin', 'confirmpin')) {
                                                         Roth.getDialog('error').error('PIN and Confirm PIN must match.');
                                                         return false;
                                                     }
                                                     var pin = _$v('pin');
                                                     if (pin.length < 4) {
                                                         Roth.getDialog('error').error('PIN must be 4 or more digits.');
                                                         return false;
                                                     }
                                                     var badpins = '1111,2222,3333,4444,5555,6666,7777,8888,9999,1234';
                                                     if (badpins.contains(pin)) {
                                                         Roth.getDialog('error').error('Please do not use an easy to guess PIN.');
                                                         return false;
                                                     }"/>
    <r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>
<r:break/>
