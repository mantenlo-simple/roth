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

import java.util.ArrayList;

import jakarta.servlet.jsp.JspException;

import com.roth.tags.html.util.OptionData;

public class Tabset extends ActionTag implements OptionTag {
	private static final long serialVersionUID = 6670478499501738656L;

	// Attributes
	public void setIconAlign(String iconAlign) { setValue("iconAlign", iconAlign.toLowerCase()); }
	// Event Handlers
	public void setOnSelect(String onSelect) { setValue("onselect", onSelect); }
	
	@SuppressWarnings("unchecked")
	@Override
	public void addOption(OptionData option) {
		if (getValue("_options") == null) setValue("_options", new ArrayList<OptionData>());
		((ArrayList<OptionData>)getValue("_options")).add(option);
	}
		
	public int doStartTag() throws JspException {
		return EVAL_BODY_INCLUDE;
	}
	
	@SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {
		ArrayList<OptionData> options = (ArrayList<OptionData>)getValue("_options");
		
		if (options != null) {
			String tabs = "";
			
			for (int i = 0; i < options.size(); i++)
				tabs += (tabs.isEmpty() ? "" : " ") + getTab(options.get(i), i, options.size());
			
			println(getTabset(getId(), tabs, (String)getValue("onselect"), true));
			
			//String id = getId() == null ? "" : attr("id", getId());
			//String style = getValue("style") == null ? "" : attr("style", (String)getValue("style"));
			//println(tag("div", attr("class", "jtab") + id + style, tag("ul", attr("onclick", (String)getValue("onselect")), body)));
		}
		
		release();
		return EVAL_PAGE;
	}
	
	protected String getTab(OptionData option, int index, int count) {
		//boolean left = Data.nvl((String)getValue("iconAlign"), "left").equalsIgnoreCase("left");
		//String icon = (Data.isEmpty(option.getIconName())) ? "" : Icon.getIcon(option.getIconName(), option.getOverlayName(), !left, left);
		//String caption = tag("span", "", option.getCaption());
		//String body = left ? icon + caption : caption + icon;
		//return tag("li", attr("onmouseup", "rtmu(this)") + 
		//		         attr("page", option.getPageId()) +
		//		         ((option.getAction() != null) ? attr("action", option.getAction()) : "") +
		//		         attr("class", ((index == 0) ? "left" : "") +
		//		         		       (((index == 0) && option.getSelected()) ? " " : "") +
		//		           		       ((option.getSelected()) ? "selected" : "")), 
		//		   tag("div", attr("class", ((index == (count - 1)) ? "right" : "")), body));

		//String onclick = option.getAction() == null ? String.format("Roth.tabset.setPage('%s', '%s');", getId(), option.getPageId())
		//		       : String.format("Roth.tabset.setPage('%s', '%s', '%s');", getId(), option.getPageId(), option.getAction());
		
		String caption = String.format("<span>%s</span>", option.getCaption());
		String icon = getIcon(option.getIconName());
		return getTab(index, getId(), option.getPageId(), caption, icon, option.getAction(), null, option.getSelected());
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
