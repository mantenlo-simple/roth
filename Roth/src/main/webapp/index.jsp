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
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<!-- ${rf:setResourcePath(pageContext, 'com/roth/resource/portal')} -->

<r:portlet servletPath="/Home">
    <script type="text/javascript" src="${portalRoot}/quill/quill.js"></script>
    
    <script type="text/javascript" src="${portalRoot}/js/rothdom.js"></script>
    <script type="text/javascript">
    	addEvent(window, "load", () => {
			setTimeout(initCodeMirror);
		});
    </script>

    <c:choose>
        <c:when test="${empty rf:getUserName(pageContext)}">
            <c:if test="${empty theme.homeUri}">
                <div style="width: 100%; text-align: right;">${rf:value(pageContext, 'notLoggedIn')}</div><br/>
            </c:if>
            <script type="text/javascript">
	            function doLogin() {
	                var callback = function() {
	                    if (this.readyState == 4) {
	                        if (this.status == 200) {
	                        	// php = Protocol Host Port
	                            var php = location.protocol + '//' + location.host;
	                            document.location = php + portalRoot + '/Home';
	                        }
	                        else
	                            Roth.getDialog('error').error("${rf:value(pageContext, 'loginInvalid')}");
	                    }
	                };
	                executeAjax({callback: callback, url: 'secure_redirect.html'});
	                //Roth.execDialog('login', '/Roth/login.jsp', '', 'Login', 'login', 'username');
	            }
            </script>
            <div id="homeContent" style="height: ${empty theme.homeUri ? '474px' : 'auto;'}; scroll: auto;">
                <c:if test="${!empty theme.homeUri and theme.homeUriViewType eq 'I'}">
                    <script type="text/javascript">
                        Roth.ajax.htmlAction('homeContent', '${theme.homeUri}');
                    </script>
                </c:if>
                <c:if test="${!empty theme.homeUri and theme.homeUriViewType eq 'F'}">
                    <iframe id="homeframe" src="${theme.homeUri}" style="border: none; width: 100%;" title="Home"
                            onload="var frame = _$('homeframe');
                                    var homeContent = _$('homeContent');
                                    //frame.style.width = lesserOf(homeContent.scrollWidth, frame.contentWindow.document.body.scrollWidth) + 'px';
                                    frame.style.height = frame.contentWindow.document.body.scrollHeight + 'px';"></iframe>
                </c:if>
            </div>
        </c:when>
        <c:when test="${empty requestScope.homeInit}">
            <script type="text/javascript">
                // php = Protocol Host Port
                var php = location.protocol + '//' + location.host;
                document.location = php + portalRoot + '/Home';
            </script>
        </c:when>
        <c:otherwise>
      		<div style="height: 400px;">
      			Welcome Home.
      			<%--
      			<r:button caption="Create a button" iconName="check" onClick="this.parentNode.appendChild(Elements.newButton({text: 'Try Me', iconName: 'ban', props: {href: '#', onclick: event => { alert('hello'); return false; }}}));"/>
      			
      			<r:button caption="Create a text box" iconName="check" onClick="this.parentNode.appendChild(Elements.newTextBox({text: 'A Label', width: '250px', props: {name: 'requestScope.object.field', value: 'someValue', onchange: (event) => { alert('I changed.'); }}}));"/>
      			 --%>
            </div>
        </c:otherwise>
    </c:choose>

    
</r:portlet>