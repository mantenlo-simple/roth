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
<%@ page import="com.roth.tags.html.HtmlTag"%>

<c:set var="templates" value="${HtmlTag.templates}"/>

Roth.controls = {
	"getLabeledInput": {
		
	},
	"getTextbox": (params = {}) => {
		if (!params.id)
			throw "Parameter id is required.";
		const { name = "", value = "", label = "", title = "", width - "", attributes = "", password = false } = params;
		const input = `${HtmlTag.getTemplate('textbox')}`;
		return label === "" ? input : `${HtmlTag.getTemplate('labeledinput')}`;
	}
};



<!-- 

public static String getLabledInput(String id, String label, String title, String input, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (label == null)
			throw new IllegalArgumentException("The label argument may not be null.");
		if (input == null)
			throw new IllegalArgumentException("The input argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", label);
		params.put("title", Data.nvl(title));
		params.put("input", input);
		params.put("attributes", Data.nvl(attributes));
		return applyTemplate("labeledinput", params);
	}
	
	public static String getTextBox(String id, String name, String value, String label, String title, String width, String attributes, boolean password) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String size = getSize(width, null); // Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("type", password ? "password" : "text");
		params.put("name", name);
		params.put("value", value);
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("textbox", params);
		else
			return getLabledInput(id, label, title, applyTemplate("textbox", params), size);
	}


 -->


<c:forEach var="item" items="${templates}">
	${item.key} <br/>
	
	${item.value}
</c:forEach>