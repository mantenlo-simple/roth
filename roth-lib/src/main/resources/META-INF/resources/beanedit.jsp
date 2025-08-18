<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:jspSecurity rolesAllowed="Authenticated"/>

<c:set var="paramString" value=""/>
<c:set var="exclusion">${requestScope.beanName}Id=</c:set>
<c:forEach var="item" items="${fn:split(queryString, '&')}">
	<c:if test="${not fn:startsWith(item, exclusion)}">
		<c:set var="paramString">${paramString}${empty paramString ? '' : '&'}${item}</c:set>
	</c:if>
</c:forEach>
<c:set var="paramString" value="${empty paramString ? '' : '?'}${paramString}"/>

<r:form action="${requestScope.servletPath}/save${paramString}" method="AJAX" 
        dataGridId="${requestScope.beanName}List" containerId="${requestScope.gridSettings.containerId}">
	<c:forEach var="input" items="${requestScope.formSettings.inputs}">
		<c:set var="readonly" value="${input.readonly or (input.worm and not empty requestScope[requestScope.beanName].id)}"/>
		<c:choose>
			<c:when test="${input.type eq 'break'}">
				<r:break height="${input.height}"/>
			</c:when>
			<c:when test="${input.type eq 'spacer'}">
				<r:wrap style="width: ${input.width};"/>
			</c:when>
			<c:when test="${input.type eq 'hidden'}">
				<r:hidden dataSource="requestScope.${requestScope.beanName}.${input.dataSource}"/>
			</c:when>
			<c:when test="${input.type eq 'textBox'}">
				<r:textBox label="${input.label}" dataSource="requestScope.${requestScope.beanName}.${input.dataSource}" 
				           required="${input.required}" readOnly="${readonly}" 
				           width="${input.width}" maxLength="${not empty input.maxLength ? input.maxLength : 9999999}" 
				           onChange="${input.onChange}"/>
			</c:when>
			<c:when test="${input.type eq 'textArea'}">
				<r:textArea label="${input.label}" dataSource="requestScope.${requestScope.beanName}.${input.dataSource}" 
				            required="${input.required}" readOnly="${readonly}"
				            width="${input.width}" height="${input.height}" maxLength="${not empty input.maxLength ? input.maxLength : 9999999}"/>
			</c:when>
			<c:when test="${input.type eq 'select'}">
				<c:set var="optionsDataSource" value="${input.optionsDataSource}"/>
				<r:select label="${input.label}" dataSource="requestScope.${requestScope.beanName}.${input.dataSource}" 
				          optionsDataSource="${requestScope[optionsDataSource]}" 
				          nullable="${input.nullable}" required="${input.required}" readOnly="${readonly}" 
				          width="${input.width}"/>
			</c:when>
			<c:when test="${input.type eq 'calendarSelect'}">
				<r:calendarSelect label="${input.label}" dataSource="requestScope.${requestScope.beanName}.${input.dataSource}" 
				                  required="${input.required}" readOnly="${readonly}" 
				                  width="${input.width}" onChange="${input.onChange}"/>
			</c:when>
			<c:when test="${input.type eq 'checkBox'}">
				<r:checkBox label="${input.label}" dataSource="requestScope.${requestScope.beanName}.${input.dataSource}" 
				            boolValues="${input.boolValues}" readOnly="${readonly}"/>
			</c:when>
			<c:when test="${input.type eq 'indicator'}">
				<c:set var="indicator" value="${input.indicator}"/>
				<r:indicator id="${indicator.id}" cssClass="${indicator.cssClass}" iconName="${requestScope[beanName][indicator.iconName]}" color="${requestScope[beanName][indicator.color]}" background="${requestScope[beanName][indicator.background]}"/>
			</c:when>
		</c:choose>
	</c:forEach>

	<r:break height="8px"/>

	<%-- <r:button type="save" formSubmit="true" onClick="window.grid${requestScope.beanName}Liststate = Roth.grid.getState('${requestScope.beanName}List');"/> --%>
	<r:button type="save" formSubmit="true"/>
	<r:button type="cancel" onClick="Roth.getParentDialog(this).hide();"/>
</r:form>