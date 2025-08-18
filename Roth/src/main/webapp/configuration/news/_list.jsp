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

<!-- ${rf:setResourcePath(pageContext, 'com/jp/portal/resource/portal')} -->

<r:dataGrid id="newsList"
            dataSource="requestScope.news" containerId="newsDlg_content" rowSelect="true" height="250" 
            onOpen="Roth.getDialog('wait').hide();
                    tinymce.init({
					    selector: '#newsContent',
					    theme: 'modern',
					    plugins: [
					        'advlist autolink lists link image charmap print preview hr anchor pagebreak',
					        'searchreplace wordcount visualblocks visualchars code fullscreen',
					        'insertdatetime nonbreaking save table contextmenu directionality',
					        'template paste textcolor colorpicker textpattern imagetools'
					    ],
					    toolbar1: 'bold italic | forecolor backcolor | alignleft aligncenter alignright alignjustify | ' +
					              'bullist numlist outdent indent | link image | print preview',
					    style_formats: [
					        {title: 'Bold text', inline: 'b'},
					        {title: 'Red text', inline: 'span', styles: {color: '#ff0000'}},
					        {title: 'Red header', block: 'h2', styles: {color: '#ff0000'}},
					        {title: 'Example 1', inline: 'span', classes: 'example1'},
					        {title: 'Example 2', inline: 'span', classes: 'example2'},
					        {title: 'Table styles'},
					        {title: 'Table row 1', selector: 'tr', classes: 'tablerow1'}
					    ],
					    width: 700,
					    height: 220
					});
					var tr = getChild(_$('editnewsList'), '0.0.0');
					tr.style.cursor = 'default';
                    tr.onmousedown = function() {};
					var c = Roth.getDialog('editnewsList').callback;
					Roth.getDialog('editnewsList').callback = function (request) {
					    tinymce.remove('#newsContent');
					    c(request);
					};">
    <r:column caption="" dataSource="newsId" visible="false" key="true" width="0"/>
    <r:column caption="##domain" dataSource="domainId" width="70">
        <c:set var="domainId">${rowData.domainId}</c:set><%-- convert to string --%>
        ${requestScope.domains[domainId]}
    </r:column>
    <r:column caption="##group" dataSource="groupId" width="90">
        <c:set var="groupId">${rowData.groupId}</c:set><%-- convert to string --%>
        ${requestScope.groups[groupId]}
    </r:column>
    <r:column caption="Language" dataSource="languageCode" width="70"/>
    <r:column caption="##headline" dataSource="headline" width="220"/>
    <r:column caption="Posted" dataSource="postDts" width="140"/>
    <r:column caption="##sticky" dataSource="sticky" width="46">
        <c:if test="${rowData.sticky eq 'Y'}"><r:icon iconName="accept"/></c:if>
    </r:column>
    <r:column caption="Updated By" dataSource="updatedBy" width="140"/>
    <r:column caption="Last Updated" dataSource="updatedDts" width="140"/>
    
    <r:button type="add" action="/News/edit"/>
    <r:button type="edit" action="/News/edit"/>
    <r:button type="delete" action="/News/delete?_method=AJAX"/>
    <r:button type="close" style="margin-left: 16px;" 
              onClick="Roth.getParentDialog(this).hide(); 
                       document.location = document.location; 
                       return false;"/>
</r:dataGrid>
<r:break/>