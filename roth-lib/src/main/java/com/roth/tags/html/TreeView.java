/*
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
 */
package com.roth.tags.html;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

import jakarta.servlet.jsp.JspException;

public class TreeView extends HtmlTag {
	private static final long serialVersionUID = 8079015280152129696L;

	private static final String DATA_SOURCE = "dataSource";
	private static final String CHILD_SOURCE = "childSource";
	
	// A Collection of objects representing nodes.
	public void setDataSource(Object dataSource) { setValue(DATA_SOURCE, dataSource); }
	// A member name referencing a Collection of objects representing "child" nodes.
	// It is assumed that if different object types are used that the same field name
	// will be used throughout.
	public void setChildSource(String childSource) { setValue(CHILD_SOURCE, childSource); }

	private static final String NODE_DATA = "nodeData";
	private static final String NODE_INDEX = "nodeIndex";
	private static final String NODE_LEVEL = "nodeLevel";
	private static final String NODE_SIZE = "nodeSize";
	
	@Override
	public int doStartTag() throws JspException {
		Object dataSource = getValue(DATA_SOURCE);
		String childSource = getStringValue(CHILD_SOURCE);
		
		TreeNodeStack stack = new TreeNodeStack();
		stack.push(dataSource);

		println("<div class=\"root\">");
		setValue("stack", stack);
		Object data = stack.next();
		if (data != null) {
			Object children = getChildren(data, childSource);
			println(String.format(sizeOf(children) == 0 ? NODE_START_BEFORE_EMPTY : NODE_START_BEFORE, "even"));
		}
		pageContext.setAttribute(NODE_DATA, data);
		pageContext.setAttribute(NODE_INDEX, stack.getLevelIndex());
		pageContext.setAttribute(NODE_LEVEL, stack.getLevel());
		pageContext.setAttribute(NODE_SIZE, stack.getLevelSize());
		return data == null ? SKIP_BODY : EVAL_BODY_INCLUDE; 
	}
	
	private Object getChildren(Object parent, String childSource) {
		if (parent == null)
			return null;
		try {
			Method csmeth = Data.getDeclaredMethod(parent.getClass(), Data.getGetterName(childSource));
			return csmeth != null ? csmeth.invoke(parent) : null;
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			Log.logException(e, null);
			return null;
		}
	}

	// Usage: String.format(NODE_START, oddEven)
	private static final String NODE_START_BEFORE = """
			<div class="node %s">
			    <div><div class="toggle" onclick="toggle(this.parentNode.parentNode); event.stopPropagation();"><i class="fas fa-caret-right"></i><i class="fas fa-caret-down"></i></div></div>
			    <div>
			""";
	private static final String NODE_START_BEFORE_EMPTY = """
			<div class="node %s">
			    <div><div class="toggle empty">&nbsp;&nbsp;</div></div>
			    <div>
			""";
	private static final String NODE_START_AFTER = """
			    </div>
			    <div></div>
			    <div class="children">
			""";
	private static final String NODE_END = """
			    </div>
			</div>
			""";
	
	@Override
	public int doAfterBody() throws JspException {
		TreeNodeStack stack = (TreeNodeStack)getValue("stack");
		String childSource = getStringValue(CHILD_SOURCE);
		
		println(NODE_START_AFTER);
		int childCount = 0;
		
		Object data = pageContext.getAttribute(NODE_DATA);
		Object children = getChildren(data, childSource);
		childCount = sizeOf(children);
		if (childCount > 0)
			stack.push(children);
		else
			println(NODE_END);
		data = null;
		while (stack.getLevel() > 1 && !stack.hasNext()) {
			stack.pop();
			println(NODE_END);
		}
		if (stack.getLevel() > 0 && stack.hasNext())
			data = stack.next();

		if (data != null)
			println(String.format(sizeOf(getChildren(data, childSource)) == 0 ? NODE_START_BEFORE_EMPTY : NODE_START_BEFORE, (stack.getLevelIndex() -1) % 2 == 0 ? "even" : "odd"));
		
		pageContext.setAttribute(NODE_DATA, data);
		pageContext.setAttribute(NODE_INDEX, stack.getLevelIndex());
		pageContext.setAttribute(NODE_LEVEL, stack.getLevel());
		pageContext.setAttribute(NODE_SIZE, stack.getLevelSize());
		return data == null ? SKIP_BODY : EVAL_BODY_AGAIN;
	}
	
	private int sizeOf(Object object) {
		if (object == null)
			return 0;
		if (object instanceof List<?> temp)
			return temp.size();
		if (object.getClass().isArray())
			return ((Object[])object).length;
		return 0;
	}
	
	@Override
	public int doEndTag() throws JspException {
		println("</div>");
		release();
		return EVAL_PAGE;
	}
	@Override
	public String[][] getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String[] getEntities() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getTemplate() {
		// TODO Auto-generated method stub
		return null;
	}
}
