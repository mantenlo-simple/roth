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
<%@taglib uri="roth" prefix="j"%>
<%@taglib uri="roth-functions" prefix="jf"%>

<j:jspSecurity rolesAllowed="SystemAdmin,Developer"/>

<j:textBox id="_password" label="Enter Password" dataSource="password" password="true" 
           onKeyDown="var e = event ? event : window.event; if (e.keyCode == 13) _$('_pwdok').click();"/>
<j:break height="8"/>
<j:button id="_pwdok" type="ok" onClick="_$('password').value = _$v('_password'); 
                                         //_$('execForm').setAttribute('ajax', 'AJAX'); 
                                         //submitForm(_$('execForm'));
                                         execStatement(null, true);  
                                         //_$('execForm').removeAttribute('ajax'); 
                                         _$('password').value = ''; 
                                         Roth.getParentDialog(this).hide();"/>
<j:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
<j:break/>