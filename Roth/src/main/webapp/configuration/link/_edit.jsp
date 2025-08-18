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

<r:tabset onSelect="Roth.tabset.getPage(this, 'linkId=${requestScope.link.linkId}&linkTitle=${requestScope.link.linkTitle}')">
    <r:option caption="Link Data" value="links" pageId="linkdata" iconName="link" selected="true" />
    <c:if test="${rf:isUserInRole(pageContext, 'PortalAdmin')}">
        <r:option caption="Link Roles" value="roles" pageId="linkroles" iconName="role" action="/Link/loadRoles"/>
    </c:if>
</r:tabset> 

<div class="jbreak" style="height: 6px;"></div>

<div id="linkdata" style="width: 475px;">
    <jsp:include page="__linkdata.jsp"/>
</div>

<div id="linkroles" style="width: 523px; display: none;">
    Loading link roles...
</div>

<div class="jbreak"></div>