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

public class Slide extends ActionTag {
	private static final long serialVersionUID = 5751705640152922123L;

	public void setIconUri(String iconUri) { setOptionDataValue("iconName", iconUri); }
	public void setInTransition(String inTransition) { setOptionDataValue("page", inTransition); }
	public void setOutTransition(String outTransition) { setOptionDataValue("value", outTransition); }
	public void setTitle(String title) { setOptionDataValue("caption", title); }
	public void setTransition(String transition) { setValue("inTransition", transition); setValue("outTransition", transition); }
	
	public OptionData getOptionData() { return (OptionData)getValue("optionData"); }
	public void setOptionDataValue(String name, String value) {
		if (getValue("optionData") == null) setValue("optionData", new OptionData());
		if (name.equals("action"))
			((OptionData)getValue("optionData")).setAction(value);
		else if (name.equals("caption")) // title
			((OptionData)getValue("optionData")).setCaption(value);
		else if (name.equals("iconName")) // iconUri
			((OptionData)getValue("optionData")).setIconName(value);
		else if (name.equals("page")) // inTransition
			((OptionData)getValue("optionData")).setPageId(value);
		else if (name.equals("value")) // outTransition
			((OptionData)getValue("optionData")).setValue(value);
	}
	
	// doEndTag
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
