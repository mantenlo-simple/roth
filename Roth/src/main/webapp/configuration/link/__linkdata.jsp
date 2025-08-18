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

<r:form action="/Link/save" method="AJAX" onAjax="Roth.getParentDialog(this).callback(request);">
    <div style="width: 484px;">
        <r:textBox label="Link Name" dataSource="requestScope.link.linkName" style="width: 229px;" required="true"/>
        <r:textBox label="Link Title" dataSource="requestScope.link.linkTitle" style="width: 229px;" required="true"/>
        <div class="jbreak"></div>
        <div style="float: left;">
            <r:textBox label="Link URI" dataSource="requestScope.link.linkUri" style="width: 370px;" required="true"/>
            <div class="jbreak"></div>
            <r:textBox label="Link Icon URI" dataSource="requestScope.link.linkIcon" style="width: 370px;" onKeyUp="document.getElementById('iconPreview').src = this.value"/>
        </div>
        <div style="float: left;">
            <div class="jedt"><div class="jlbl">Icon Preview:</div>
                <div style="float: left; border: 1px solid silver; padding: 3px 12px 0 12px; height: 67px; width: 67px; text-align: center;"><img id="iconPreview" src="${requestScope.link.linkIcon}" alt="404" style="margin: 0px;"/></div>
            </div>
        </div>
    </div>
    <r:hidden dataSource="requestScope.link.linkId"/>
    
    <div class="jbreak" style="height: 8px;"></div>
    <r:button type="save" formSubmit="true"/>
    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    <div class="jbreak"></div>
</r:form>