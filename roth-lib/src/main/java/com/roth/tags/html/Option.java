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

import jakarta.servlet.jsp.JspException;

import com.roth.tags.html.util.OptionData;

public class Option extends ActionTag {
	private static final long serialVersionUID = 2381015720171288002L;

	// Attribute Setters
	public void setCaption(String caption) { setOptionDataValue("caption", caption); }
	public void setIconName(String iconName) { setOptionDataValue("iconName", iconName.toLowerCase()); }
	public void setOverlayName(String overlayName) { setOptionDataValue("overlayName", overlayName.toLowerCase()); }
	public void setPageId(String page) { setOptionDataValue("page", page); }
	public void setSelected(boolean selected) { setOptionDataValue("selected", selected ? "true" : "false"); }
	public void setValue(String value) { setOptionDataValue("value", value); }
	public void setOnClick(String onClick) { setOptionDataValue("onclick", onClick); }
	
	public OptionData getOptionData() { return (OptionData)getValue("optionData"); }
	public void setOptionDataValue(String name, String value) {
		if (getValue("optionData") == null) setValue("optionData", new OptionData());
		if (name.equals("action"))
			((OptionData)getValue("optionData")).setAction(value);
		else if (name.equals("caption"))
			((OptionData)getValue("optionData")).setCaption(value);
		else if (name.equals("iconName"))
			((OptionData)getValue("optionData")).setIconName(value);
		else if (name.equals("overlayName"))
			((OptionData)getValue("optionData")).setOverlayName(value);
		else if (name.equals("page"))
			((OptionData)getValue("optionData")).setPageId(value);
		else if (name.equals("selected"))
			((OptionData)getValue("optionData")).setSelected(value.equals("true"));
		else if (name.equals("value"))
			((OptionData)getValue("optionData")).setValue(value);
	}
	
	public int doEndTag() throws JspException {
		if (getValue("action") != null) setOptionDataValue("action", getActionUrl()); 
		OptionTag ancestor = (OptionTag)findAncestorWithClass(this, OptionTag.class);
		ancestor.addOption((OptionData)getValue("optionData"));
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
