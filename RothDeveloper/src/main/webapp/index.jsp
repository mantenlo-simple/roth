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
<%@taglib uri="roth" prefix="j"%>
<%@taglib uri="roth-functions" prefix="jf"%>

<!-- ${jf:setResourcePath(pageContext, 'com/jp/portal/resource/portal')} -->

<j:portlet servletPath="/Developer">
    <c:choose>
        <c:when test="${empty jf:getUserName(pageContext)}">
            <c:if test="${empty theme.homeUri}">
                <div style="width: 100%; text-align: right;">${jf:value(pageContext, 'notLoggedIn')}</div><br/>
            </c:if>
            <%-- Please click <a href="secure_redirect.html">here</a> to login.<br> --%>
            <script type="text/javascript">
	            addEvent(window, 'load', doAuthentication('/RothDeveloper/Developer'));
            </script>
            <div id="homeContent" style="height: ${empty theme.homeUri ? '474px' : 'auto;'}; scroll: auto;">
                <c:if test="${!empty theme.homeUri and theme.homeUriViewType eq 'I'}">
                    <script type="text/javascript">
                        Roth.ajax.htmlAction('homeContent', '${theme.homeUri}');
                    </script>
                </c:if>
                <c:if test="${!empty theme.homeUri and theme.homeUriViewType eq 'F'}">
                    <iframe id="homeframe" src="${theme.homeUri}" style="border: none; width: 100%;" 
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
                document.location = php + '/Roth/Home';
            </script>
        </c:when>
        <c:otherwise>
            <style type="text/css">
                .dmi {
                    border: 1px solid #789;
                    padding: 6px 6px 0 6px;
                    margin: 0 6px 8px 0;
                    font-size: 24px;
                    vertical-align: center;
                    cursor: pointer;
                }
                .dmi:hover { 
                    background-color: #eaefff;
                    color: navy;
                    border: 1px solid #9ab 
                }
                #dtmenu { height: 460px; overflow-y: auto; }
                #dtmenu a { color: black; text-decoration: none; }
                #dtmenu a:visited { color: black; }
                #dtmenu a:hover { color: navy; }
                a img { border: none; }
            </style>
            <c:set var="welcome"><div style="float: right; font-weight: normal;">${jf:value(pageContext, 'hello')}, ${fn:split(jf:getUserFullName(pageContext), ' ')[0]}.  ${jf:value(pageContext, 'welcome')} Roth!</div></c:set>
            <script type="text/javascript">
	            function resizeMenus(event) {
	            	var contentWidth = parseInt(getProperty(_$c('main_content')[0], 'width')) - 34; //_$c('main_content')[0].clientWidth - 50;
	            	var linkWidth = 365;
	            	var newsWidth = contentWidth - linkWidth;
	            	// var width = (_$c('main_content')[0].clientWidth - 54) / 2;
	                _$('dtmenuc').style.width = linkWidth + 'px';
	                _$('lmenuc').style.width = linkWidth + 'px';
	                _$('nmenuc').style.width = newsWidth + 'px';
	                
	                //alert('height: ' + _$c('main_content')[0].clientHeight + ', ' + getProperty(_$c('main_content')[0], 'height'));
	                //alert('width: ' + _$c('main_content')[0].clientWidth + ', ' + getProperty(_$c('main_content')[0], 'width'));
	                
	                
	                
	                /*
	                if (_$c('fullwindow')[0]) {
	                	var offset = getAbsCoord(_$c('main')[0]).y
	                	           + _$c('footer')[0].clientHeight + 16;
	                	_$c('main')[0].style.height = (document.body.clientHeight - offset) + 'px';
	                    var height = _$c('main_content')[0].clientHeight - 26;
	                    var half = height / 2 - 12;
	                    _$('dtmenuc').style.height = half + 'px';
	                    getChild(_$('dtmenuc'), 2).style.height = (half - 34) + 'px';
	                    _$('lmenuc').style.height = half + 'px';
	                    getChild(_$('lmenuc'), 3).style.height = (half - 34) + 'px';
	                    _$('nmenuc').style.height = height + 'px';
                        getChild(_$('nmenuc'), 3).style.height = (height - 34) + 'px';
	                }
	                */
	            }
	            function welcome() {
	            	//_$c('portlet')[0].innerHTML += '${welcome}';
	            }
	            addEvent(window, 'load', function() { welcome(); });
	            addEvent(window, 'load', function() { resizeMenus(); });
	            addEvent(window, 'resize', function() { resizeMenus(); });
            </script>
            
            <%--
            <table style="width: 100%; height: 50.2em;">
                <colgroup>
                    <col style="width: 15em;"/>
                    <col style="width: auto;"/>
                </colgroup>
                <tr style="height: 25em;">
                    <td style="padding: 0 0.1em 0.1em 0; vertical-align: top;">
                        <%@include file="_desktops_compact.jsp" %>
                    </td>
                    <td rowspan="2" style="padding: 0 0 0 0.1em; height: 50em;">
                        <%@include file="_news.jsp" %>
                    </td>
                </tr>
                <tr style="height: 25em;">
                    <td style="padding: 0.1em 0.1em 0 0; vertical-align: bottom;">
                        <%@include file="_links_compact.jsp" %>
                    </td>
                </tr>
            </table>
             --%>
            <%--
            <div id="leftpnl" style="float: left;">
	            <%@include file="_desktops_compact.jsp" %>
	            <j:break height="12"/>
	            <%@include file="_links_compact.jsp" %>
            </div>
            <div id="rightpnl" style="float: right;">
                <%@include file="_news.jsp" %>
            </div>
            <j:break/>
             --%>
        </c:otherwise>
    </c:choose>

    
</j:portlet>