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

<b>${requestScope.gridSettings.title}</b> <c:if test="${not empty param['headerLabel']}">(${param['headerLabel']})</c:if>
<r:break height="8px"/>
<r:dataGrid id="${requestScope.beanName}List" dataSource="requestScope.${requestScope.beanName}List" 
            containerId="${requestScope.gridSettings.containerId}"
            action="${requestScope.servletPath}/list${paramString}" 
            dialogCaption="${requestScope.gridSettings.dialogCaption}" 
            columnMoving="true" columnSizing="true" exporting="true" sorting="true" searching="true" rowSelect="true" wrapping="true" 
            height="calc(100% - 60px)">
	<r:row onClick="${requestScope.gridSettings.onRowClick}" onDblClick="${requestScope.gridSettings.onRowDblClick}"/>
	<c:forEach var="column" items="${requestScope.gridSettings.columns}">
		<c:choose>
			<c:when test="${not empty column.dataSourceMap}">
				<r:column dataSource="${column.dataSource}" caption="${column.caption}" key="${column.key}" visible="${column.visible}" width="${column.width}">
					<c:set var="mapName" value="${column.dataSourceMap}"/>
					<c:set var="map" value="${requestScope[mapName]}"/>
					<c:set var="dataSource" value="${column.dataSource}"/>
					<c:set var="valueKey">${rowData[dataSource]}</c:set>
					${map[valueKey]}<br/>
				</r:column>
			</c:when>
			<c:when test="${not empty column.boolValues}">
				<r:column dataSource="${column.dataSource}" caption="${column.caption}" key="${column.key}" visible="${column.visible}" width="${column.width}">
					<c:set var="boolValues" value="${column.boolValues.split('\\\\|')}"/>
					<c:set var="dataSource" value="${column.dataSource}"/>
					<c:set var="valueKey">${rowData[dataSource]}</c:set>
					${valueKey eq boolValues[1] ? 'Yes' : 'No'}
				</r:column>
			</c:when>
			<c:when test="${not empty column.indicator}">
				<r:column dataSource="${column.dataSource}" caption="${column.caption}" key="${column.key}" visible="${column.visible}" width="${column.width}">
					<c:set var="indicator" value="${column.indicator}"/>
					<r:indicator cssClass="${indicator.cssClass}" iconName="${rowData[indicator.iconName]}" color="${rowData[indicator.color]}" background="${rowData[indicator.background]}"/>
				</r:column>
			</c:when>
			<c:otherwise>
				<r:column dataSource="${column.dataSource}" caption="${column.caption}" key="${column.key}" visible="${column.visible}" width="${column.width}"/>
			</c:otherwise>
		</c:choose>
	</c:forEach>
	
	<r:button type="add" action="${requestScope.servletPath}/edit${paramString}"/>
	<r:button type="edit" action="${requestScope.servletPath}/edit${paramString}"/>
	<r:button type="delete" action="${requestScope.servletPath}/delete${paramString}"/>
</r:dataGrid>