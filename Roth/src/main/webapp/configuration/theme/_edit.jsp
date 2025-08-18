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
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:form action="/Theme/save" method="AJAX" autoComplete="off" 
        onAjax="Roth.ajax.htmlCallback(request, 'themelist', function() { 
                    Roth.ajax.htmlAction('editthemeListTable_content', contextRoot + '/Theme/edit', 'themeName=' + _$v('themeName'), null, null, function() {
	                    var mixedMode = {
	                        name: 'htmlmixed',
	                        scriptTypes: [{matches: /\/x-handlebars-template|\/x-mustache/i, mode: null},
	                                      {matches: /(text|application)\/(x-)?vb(a|script)/i, mode: 'vbscript'}]
	                    };
	                    editorc = CodeMirror.fromTextArea(_$('copyrightName'), {mode: mixedMode, tabMode: 'indent', lineNumbers: true});
	                    editorc.setSize('100%', '4em');
	                    editorc.refresh();
	                    editorh = CodeMirror.fromTextArea(_$('customHtml'), {mode: mixedMode, tabMode: 'indent', lineNumbers: true});
	                    //editorh.setSize(966, 304); 
                    }); 
                })" 
        onSubmit="editorc.save(); 
                  editorh.save();
                  Roth.getDialog('wait').wait('Please wait while your changes are saved...');">
    <c:set var="params">themeId=${requestScope.theme.themeId}&themeName=${requestScope.theme.themeName}</c:set>
    <r:tabset onSelect="editorc.refresh(); editorh.refresh();">
        <r:option caption="Theme" value="" pageId="themedata" iconName="paint-brush" selected="true"/>
        <r:option caption="HTML" value="" pageId="headhtml" iconName="code"/>
        <%-- <r:option caption="Footer" value="" pageId="foothtml" iconName="new" overlayName="down"/> --%>
        <c:if test="${!empty requestScope.theme.themeName}">
	        <r:option caption="Links" value="" pageId="themelinks" iconName="link" action="/Theme/loadLinks?${params}"/>
	        <r:option caption="Overrides" value="" pageId="themeoverrides" iconName="globe" action="/Theme/loadOverrides?${params}"/>
        </c:if>
    </r:tabset>
    
    <c:if test="${!rf:mobile(pageContext)}">
    	<div class="jbreak" style="height: 6px;"></div>
    </c:if>
    
    <div id="themedata">
	    <r:textBox label="Theme Name" id="themeName" dataSource="requestScope.theme.themeName" required="true" width="${rf:mobile(pageContext) ? '100%' : '13em'}"/>
	    <c:if test="${rf:mobile(pageContext)}">
			<r:break/>
		</c:if>
	    <r:select label="Mobile Theme" id="mobileThemeId" dataSource="requestScope.theme.mobileThemeId" optionsDataSource="${requestScope.themes}" nullable="true" width="${rf:mobile(pageContext) ? '100%' : '13em'}"
	              title="A mobile theme is an alternate theme that will be used when rendering to mobile browsers.  Preferably a theme that has been designed using Responsive Web Design principles should be used here."/>
	    <r:break/>
	    <r:textArea label="CSS Includes (one URI per line)" title="Optional.  One or more URIs (one per line).  May be absolute or relative paths." 
	                id="customCssUri" dataSource="requestScope.theme.customCssUri" width="${rf:mobile(pageContext) ? '100%' : '36.5em'}" style="height: 8.5em; white-space: pre;" maxLength="1024"/>
	    <c:if test="${rf:mobile(pageContext)}">
			<r:break/>
		</c:if>
	    <r:textArea label="JavaScript Includes (one URI per line)" title="Optional.  One or more URIs (one per line).  May be absolute or relative paths." 
	                id="customJsUri" dataSource="requestScope.theme.customJsUri" width="${rf:mobile(pageContext) ? '100%' : '36.5em'}" style="height: 8.5em; white-space: pre;" maxLength="1024"/>
	    <r:break/>
        <r:textBox label="Home URI" id="homeUri" dataSource="requestScope.theme.homeUri" width="${rf:mobile(pageContext) ? '100%' : '26.5em'}"
                   title="This describes the URI to content that will show in the Home page when a user has not yet logged in."/>
	    <c:if test="${rf:mobile(pageContext)}">
			<r:break/>
		</c:if>
	    <r:select label="View Type" dataSource="requestScope.theme.homeUriViewType" width="${rf:mobile(pageContext) ? '100%' : '6em'}" 
	              title="Inline loads the contents of the URI into the 'main_content' DIV tag; embedded script blocks are not automatically evaluated, but the content and host page share the same JavaScript scope.  Iframe loads the content of the URI in an Iframe; embedded script blocks are evaluated, but content and host page do not share the same scope.  Also, if the content of an Iframe is from a different host, most browsers will block JavaScript between content and host page as cross-site scripting.">
	        <r:option caption="Inline" value="I"/>
	        <r:option caption="Iframe" value="F"/>
	    </r:select>
        <r:break/>
        <r:textArea id="copyrightName" label="Copyright Name" dataSource="requestScope.theme.copyrightName" width="100%" style="height: 4em;"/>
        <%--
        <div class="jedt" style="margin-right: 0;">
            <div class="jlbl">Copyright Name</div>
            <textarea id="copyrightName" name="requestScope.theme.copyrightName" wrap="${'off'}" style="width: 74em; height: 4em;">${fn:escapeXml(requestScope.theme.copyrightName)}</textarea>
        </div>
         --%>
        
        <div class="jbreak" style="height: 8px;"></div>
        <c:set var="onSave">
            editorh.save();
            if ((_$v('customHtml').trim() != '') && !_$v('customHtml').contains('<' + 'portletcontent/>')) {
                Roth.getDialog('error').error('Custom HTML must contain &' + 'lt;portletcontent/> when not empty.');
                return false;
            }
        </c:set>
	    <r:button type="save" formSubmit="true" onClick="${onSave}"/>
	    <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    </div>
    
    <div id="headhtml" style="display: none;">
    	<r:textArea id="customHtml" dataSource="requestScope.theme.customHtml" width="${rf:mobile(pageContext) ? '100%' : '73.5em'}" style="height: 20em;"/>
    	<%--
	    <div class="jedt" style="margin-right: 0;">
	        <div class="jlbl">Custom HTML</div>
	        <textarea id="customHtml" name="requestScope.theme.customHtml" wrap="${'off'}" style="width: 54em; height: 20em;">${fn:escapeXml(requestScope.theme.customHtml)}</textarea>
	    </div>
	     --%>
	    
	    <div class="jbreak" style="height: 8px;"></div>
    
        <r:button type="save" formSubmit="true" onClick="${onSave}"/>
        <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    </div>
    
    <%--
    <div id="foothtml" style="display: none;">
	    <div class="jedt" style="margin-right: 0;">
	        <div class="jlbl">Custom Footer HTML</div>
	        <textarea id="customFooterHtml" name="requestScope.theme.customFooterHtml" wrap="${'off'}" style="width: 706px; height: 304px;" >${fn:escapeXml(requestScope.theme.customFooterHtml)}</textarea>
	    </div>
	    
	    <div class="jbreak" style="height: 8px;"></div>
    
        <r:button type="save" formSubmit="true" onClick="${onSave}"/>
        <r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
    </div>
    --%>
    
    <div id="themelinks" style="display: none; width: ${rf:mobile(pageContext) ? '100%' : '800px'};">
        Loading links...
    </div>
    
    <div id="themeoverrides" style="display: none; width: ${rf:mobile(pageContext) ? '100%' : '800px'};">
        Loading overrides...
    </div>
     
    <r:hidden dataSource="requestScope.theme.themeId"/>
    <r:hidden dataSource="requestScope.theme.updatedBy"/>
    <r:hidden dataSource="requestScope.theme.updatedDts"/>
    <input type="hidden" name="_method" value="ajax"/>
</r:form>

<div class="jbreak"></div>