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

<r:dataGrid id="themeListTable" dataSource="requestScope.themes"
            containerId="themelist" action="/Theme/load?_method=AJAX" dialogCaption="Theme|Theme &amp;quot;{themeName}&amp;quot;"
            onOpen="var mixedMode = {
				        name: 'htmlmixed',
				        scriptTypes: [{matches: /\/x-handlebars-template|\/x-mustache/i, mode: null},
				                      {matches: /(text|application)\/(x-)?vb(a|script)/i, mode: 'vbscript'}]
				    };
				    setTimeout(() => {
					    editorc = CodeMirror.fromTextArea(_$('copyrightName'), {mode: mixedMode, tabMode: 'indent', lineNumbers: true});
	                    editorc.setSize('100%', '4em');
	                    editorc.refresh();
	                    editorh = CodeMirror.fromTextArea(_$('customHtml'), {mode: mixedMode, tabMode: 'indent', lineNumbers: true});
                    	editorh.setSize('100%', 304);
				    }, 100);
                    Roth.getDialog('wait').hide();"
            columnMoving="true" columnSizing="true" sorting="true" searching="true"
            rowSelect="true" height="calc(100% - ${rf:mobile(pageContext) ? 68 : 41}px)" width="100%" exporting="true">
    <r:column caption="ID" dataSource="themeId" width="60" key="true"/>
    <r:column caption="Theme Name" dataSource="themeName" width="250" key="true"/>
    
    <r:button type="add" action="/Theme/edit"/>
    <r:button type="edit" action="/Theme/edit"/>
    <r:button type="delete" action="/Theme/delete?_method=AJAX"/>
</r:dataGrid>
${rf:mobile(pageContext) ? '<span>' : ''}
<r:button id="themePreview" caption="Preview" iconName="eye" style="${rf:mobile(pageContext) ? 'margin-top: 8px' : 'margin-left: 16px'};" onClick="previewTheme()"/>
<r:button type="export" onClick="var row = getSelectedRow('themeListTable');
                                 if (!row) {
                                     Roth.getDialog('alert').alert('Please select a row, then try again.');
                                     return false;
                                 }
                                 window.open(contextRoot + '/Theme/exportTheme?' + row.getAttribute('key'), '_blank');" style="margin-left: 16px;"/>
<r:button type="import" onClick="Roth.getDialog('import').file('Import Theme', 'Import', 'upload', '/Theme/importTheme', 'File', 'source', '.thm', 'import', 'Roth.ajax.messageCallback(request); refreshThemes();');"/>
${rf:mobile(pageContext) ? '</span>' : ''}
<div class="jbreak"></div>
