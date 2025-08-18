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
<%@ page import="java.util.Properties"%>
<%@ page import="jakarta.servlet.ServletContext"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<c:set var="mobi" value="${rf:mobile(pageContext)}"/>
<r:context/>

<c:set var="resourcePath" value="com/roth/servlet/resource/about"/>
<!-- ${rf:setResourcePath(pageContext, resourcePath)} -->

<style type="text/css">
    .aboutPage {
        background: rgba(256,256,256,0.6);
        box-shadow: 0 0 0.3em dimgray inset;
        padding: 0.5em; 
        border-radius: 0.2em;
        border-right: 0.1em solid silver;
        overflow-y: scroll;
        margin: 0.2em; 
        width: ${mobi ? '18em' : '33.5em'};
        height: ${mobi ? '9em' : '18em'};
    }
    .aboutPageInset { 
        padding: 0.5em;
        font-size: ${mobi ? '0.6' : '1'}em;
        /*height: 200px;
        overflow: auto;
        margin-bottom: 8px;*/
    }
    .ackTitle {
        font-weight: bold; 
        border-bottom: 1px solid silver; 
        clear: both; 
        margin-bottom: 0.3em; 
        padding-bottom: 0.2em;
    }
    .ackImage {
        float: left;
        width: 4em;
        height: 4em;
    }
    .ackInfo {
        float: left; 
        margin-left: 1.2em; 
        font-weight: normal;
        padding-top: 0.5em;
    }
    .rothBack {
    	background: url(${portalRoot}/images/roth-logo.svg) ${mobi ? '1' : '2'}em no-repeat; 
    	background-size: ${mobi ? '85% 85%' : '29em 29em'};
    	width: ${mobi ? '20em' : '34em'};
    }
    .rothBack>div {
    	background: rgba(256,256,256,0.9);
    }
</style>

<div class="rothBack">
	<div>
		<div style="text-align: center; font-size: ${mobi ? '0.7' : '1'}em;">
			<span style="margin: 0.2em 0; display: inline-block; font-family: kells2; font-size: 4em; color: black; text-shadow: 0 0 0.1em darkgray;">${rf:value(pageContext, 'name')}<span class="version"> ${rf:value(pageContext, 'version')}</span></span><br/><span style="font-size:1.2em; font-weight: bold;">Copyright &copy; 2010-<%= com.roth.base.util.Data.dateToStr(new java.util.Date(), "yyyy") %> ${rf:value(pageContext, 'copyrightLabel')}</span><br/>
			${rf:value(pageContext, 'license')}<br/>
			<span style="font-size: 1em;">See the project on <a href="https://sourceforge.net/projects/roth/" target="_blank">SourceForge</a>.</span><br/>
		</div>
		<br/>
		<div id="aboutVersion" class="aboutPage">
		<div class="aboutPageInset">
			<% 
			    Properties prop = new Properties();
			    prop.load(pageContext.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
			    pageContext.setAttribute("portalVersion", pageContext.getServletContext().getServletContextName() + " " + prop.getProperty("Implementation-Version", "0.0.0"));
			%>
			<div style="margin: ${mobi ? '5.15' : '6'}em 2em;">
			    <img src="${portalRoot}/images/roth-logo.svg" style="float: left; width: 4em; height: 4em;"/>
			    <div style="float: left; padding-top: 0.7em;">
					<div style="float: left; text-align: right; width: 6em;">Library -&nbsp;</div>
					<div style="float: left;">${param['libVersion']}</div>
					<r:break height="0.9em"/>
					<div style="float: left; text-align: right; width: 6em;">Portal -&nbsp;</div>
					<div style="float: left;">${portalVersion}</div>
				</div>
				<r:break/>
			</div>
			<c:if test="${param['appVersion'] ne portalVersion}">
		 	    <r:break height="0.9em"/>
		 	    <div style="border-top: 0.1em solid silver; padding-top: 0.9em;">
				    <div style="float: left; text-align: right; width: 10em;">Application -&nbsp;</div>
				    <div style="float: left;">${param['appVersion']}</div>
				    <c:if test="${!empty param['appCopyright']}">
				        <r:break/>
				        <div style="float: left; margin-left: 10em;">Copyright &copy; ${param['appCopyright']}</div>
				    </c:if>
			    </div>
			</c:if>
			<r:break height="0.5em"/>
		</div>

		<div class="aboutPageInset">
			<div style="font-weight: bold; border-bottom: 1px solid silver;">Consultation</div>
			&nbsp;&nbsp;&nbsp;${rf:value(pageContext, 'consultation')}<br/>
			<br/>
			<div style="font-weight: bold; border-bottom: 1px solid silver;">Fonts</div>
			
			<c:forEach begin="${0}" end="${rf:intValue(pageContext, 'fontCount') - 1}" step="${1}" varStatus="stat">
				<c:set var="font">font${stat.index}</c:set>
				<c:set var="fontCopyrightYear">${font}CopyrightYear</c:set>
				<c:set var="fontCopyrightLink">${font}CopyrightLink</c:set>
				<c:set var="fontCopyrightLabel">${font}CopyrightLabel</c:set>
				<c:set var="fontAuthor">${font}Author</c:set>
				<c:set var="fontLicense">${font}License</c:set>
				&nbsp;&nbsp;&nbsp;<b>${rf:value(pageContext, font)}</b> - Copyright &copy; ${rf:value(pageContext, fontCopyrightYear)} 
				<a href="${rf:value(pageContext, fontCopyrightLink)}" target="_blank">${rf:value(pageContext, fontCopyrightLabel)}</a><br/>
				<c:if test="${!empty rf:value(pageContext, fontAuthor)}">
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;designed by: ${rf:value(pageContext, fontAuthor)}<br/>
				</c:if>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;license: ${rf:value(pageContext, fontLicense)}<br/>
				<r:break height="0.5em"/>
			</c:forEach>
			
			<r:break height="0.5em"/>

			<c:forEach begin="${0}" end="${rf:intValue(pageContext, 'widgetCount') - 1}" step="${1}" varStatus="stat">
				<div>
					<c:set var="widget">widget${stat.index}</c:set>
					<c:set var="widgetName">${widget}Name</c:set>
					<c:set var="widgetVersion">${widget}Version</c:set>
					<c:set var="widgetCopyrightYear">${widget}CopyrightYear</c:set>
					<c:set var="widgetCopyrightLink">${widget}CopyrightLink</c:set>
					<c:set var="widgetCopyrightLabel">${widget}CopyrightLabel</c:set>
					<c:set var="widgetCopyrightSuffix">${widget}CopyrightSuffix</c:set>
					<c:set var="widgetLicense">${widget}License</c:set>
					<c:set var="widgetImageType">${widget}ImageType</c:set>
					<c:set var="widgetImageSrc">${widget}ImageSrc</c:set>
					<c:set var="widgetImageColor">${widget}ImageColor</c:set>
					
			        <div class="ackTitle">${rf:value(pageContext, widget)}</div>
			        <c:choose>
			        	<c:when test="${rf:value(pageContext, widgetImageType) eq 'icon'}">
			        		<div class="ackImage" style="font-size: 4em; color: ${rf:value(pageContext, widgetImageColor)}; width: 1em; height: 1em;"><i class="${rf:value(pageContext, widgetImageSrc)}"></i></div>
			        	</c:when>
			        	<c:otherwise>
			        		<img class="ackImage" src="${portalRoot}${rf:value(pageContext, widgetImageSrc)}"/>
			        	</c:otherwise>
			        </c:choose>
			        <div class="ackInfo" style="padding-top: 0.4em;">
			            ${rf:value(pageContext, widgetName)} ${rf:value(pageContext, widgetVersion)}<br/> 
			            Copyright &copy; ${rf:value(pageContext, widgetCopyrightYear)} 
			            <a href="${rf:value(pageContext, widgetCopyrightLink)}" target="_blank">
			            	${rf:value(pageContext, widgetCopyrightLabel)}
			            </a>
			            ${rf:value(pageContext, widgetCopyrightSuffix)}<br/>
			            license: ${rf:value(pageContext, widgetLicense)}<br/>
			        </div>
			        <r:break/>
			    </div>
			    <r:break height="1em"/>
			</c:forEach>
			
			<div style="font-weight: bold; border-bottom: 1px solid silver;">Other Instructive Sources</div>
			&nbsp;&nbsp;&nbsp;
			<c:forEach begin="${0}" end="${rf:intValue(pageContext, 'sourceCount') - 1}" step="${1}" varStatus="stat">
				<c:set var="sourceLink">source${stat.index}Link</c:set>
				<c:set var="sourceLabel">source${stat.index}Label</c:set>
				<a href="${rf:value(pageContext, sourceLink)}" target="_blank">${rf:value(pageContext, sourceLabel)}</a>,
			</c:forEach>
			<r:break height="12"/>
		</div>
		</div>
		
		<r:break height="0.8em"/>
		
		<r:button type="close" onClick="Roth.getParentDialog(this).hide();"/>
		
		<r:break/>
	</div>
</div>
