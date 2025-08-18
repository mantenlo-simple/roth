<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:jspSecurity rolesAllowed="Authenticated"/>

<% com.roth.servlet.ActionServlet.processXsrf("GET", request, response); %>

<r:form action="${param['action']}" method="AJAX" encType="multipart/form-data" onAjax="Roth.ajax.messageCallback(request); refreshThemes();">
	<%--
	<r:file id="importFile" dataSource="${param['dataSource']}" filenameDataSource="" accept="${param['accept']}"/>
	 --%>
	<r:wrap label="${param['inputLabel']}">
		<div class="roth-input">
			<input id="importFile" type="file" name="${param['dataSource']}" size="30" accept="${param['accept']}" multiple onclick="if (!this.onchange) this.onchange = Roth.file.onChange;">
			<span style="position: absolute; left: 8px; top: 9px;" onclick="this.previousElementSibling.click();"></span>
			<div style="position: absolute; right: 8px; top: 9px;">
				<em class="fa fa-folder-open" onclick="this.parentNode.previousElementSibling.previousElementSibling.click();"></em>
			</div>
		</div>
	</r:wrap>
	<r:break height="8px"/>
	<r:button caption="${param['button']}" iconName="${param['icon']}" formSubmit="true"/>
	<r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>