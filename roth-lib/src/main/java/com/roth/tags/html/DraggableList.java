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

import java.util.List;

import com.roth.base.util.Data;

import jakarta.servlet.jsp.JspException;

public class DraggableList extends HtmlTag {
	private static final long serialVersionUID = 8079015280152129696L;

	private static final String DATA_SOURCE = "dataSource";
	private static final String ORIENTATION = "orientation";
	private static final String SEQUENCE_ID = "sequenceId";
	
	public void setDataSource(Object dataSource) { setValue(DATA_SOURCE, dataSource); }
	public void setOrientation(String orientation) { setValue(ORIENTATION, orientation); }
	public void setSequenceId(String sequenceId) { setValue(SEQUENCE_ID, sequenceId); }

	private String[] ORIENTATIONS = {"h", "v", "horz", "vert", "horizontal", "vertical"};
	
	private String UL_START = """
			<ul class="draggable-list %s" %s style="%s">
			""";
	private String UL_END = "</ul>";
	private String LI_START = """
			<li class="item" draggable="true">
				<div class="details">
			""";
	private String LI_END = """
		        </div>
			    <i class="fa-solid fa-grip-vertical"></i>
			</li>
			""";
	
	@Override
	public int doStartTag() throws JspException {
		String orientation = getStringValue(ORIENTATION, "vertical").toLowerCase();
		if (!Data.in(orientation, ORIENTATIONS))
			throw new JspException("Orientation must be one of 'h|horz|horizontal|v|vert|vertical'.");
		String orientationClass = orientation.startsWith("h") ? "horizontal" : "vertical";
		String sequenceId = getStringValue("sequenceId", "");
		if (!sequenceId.isEmpty())
			sequenceId = String.format("""
					data-sequence-id="%s"
					""", sequenceId);
		print(String.format(UL_START, orientationClass, sequenceId, gridTemplate(sizeOf(getValue(DATA_SOURCE)), orientation.startsWith("h"))));
		boolean hasNext = nextItem();
		if (hasNext)
			print(LI_START);
		setValue("hasItems", hasNext);
		return hasNext ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
	
	@Override
	public int doAfterBody() throws JspException {
		println(LI_END);
		boolean hasNext = nextItem();
		if (hasNext)
			print(LI_START);
		return hasNext ? EVAL_BODY_AGAIN : SKIP_BODY;
	}
	
	@Override
	public int doEndTag() throws JspException {
		//boolean hasItems = getBooleanValue("hasItems", false);
		//if (hasItems)
		//	println(LI_END);
		println(UL_END);
		release();
		return EVAL_PAGE;
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
	
	private boolean nextItem() {
		Object dataSource = getValue(DATA_SOURCE);
		Integer itemIndex = getIntegerValue("itemIndex", 0);
		if (itemIndex >= sizeOf(dataSource))
			return false;
		@SuppressWarnings("rawtypes")
		Object item = dataSource instanceof List ? ((List)dataSource).get(itemIndex) : ((Object[])dataSource)[itemIndex];
		pageContext.setAttribute("itemData", item);
		pageContext.setAttribute("itemIndex", itemIndex);
		itemIndex++;
		setValue("itemIndex", itemIndex);
		return true;
	}
	
	private String gridTemplate(int size, boolean horizontal) {
		String template = horizontal ? "grid-template-columns: repeat(%d, 1fr);" : "grid-template-rows: repeat(%d, 1fr);";
		return String.format(template, size);
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
