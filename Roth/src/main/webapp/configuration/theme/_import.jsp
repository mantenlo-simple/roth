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

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <link type="text/css" rel="stylesheet" href="${contextRoot}/css/roth-base.css"/>
        
        <script type="text/javascript" src="${contextRoot}/js/rothutil.js"></script>
        <script type="text/javascript" src="${contextRoot}/js/roth.js"></script>
    </head>
    <body style="margin: 0; padding: 0; background: transparent;" onload="window.top._$('itframe').width = document.body.clientWidth;
                                                                          window.top._$('itframe').height = document.body.clientHeight;">
       <r:form action="/Theme/importTheme" encType="multipart/form-data">
            
            <r:wrap label="File">
                <input type="file" name="source" size="30" accept=".thm" multiple/>
            </r:wrap>
            
            <r:break height="8"/>
            <r:button type="import" formSubmit="true"/>
            <r:button type="cancel" onClick="window.top.Roth.getDialog('importtheme').hide();"/>
        </r:form>
        <r:break/>
    </body>
</html>