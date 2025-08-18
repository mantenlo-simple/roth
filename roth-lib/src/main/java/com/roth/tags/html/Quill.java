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

public class Quill extends InputTag {
	private static final long serialVersionUID = 3751795049799452050L;

	public void setMaxLength(int maxLength) { setValue("maxlength", Integer.toString(maxLength)); }
			
	public int doEndTag() throws JspException {
		MobiScroll mobiScroll = (MobiScroll)findAncestorWithClass(this, MobiScroll.class);
		if (mobiScroll != null)
			setValue("onfocus", "var mobiScroll = getAncestorWithClass(this, 'mobi-scroll'); mobiScroll.scrollTop = this.parentNode.parentNode.offsetTop;");
		String value = (String)getRemoveValue("value");
		if (value == null) {
			Object dsValue = getDataSourceValue();
			value = (dsValue == null) ? "" : dsValue.toString();
		}
		//setValue("value", value);
		println(getQuill(getId(), (String)getValue("_datasource"), escapeValue(value), (String)getValue("_label"), null, (String)getValue("_width"), (String)getValue("_height"), this.getHTMLAttributes()));
		
		//println(getTextArea(value));
		release();
		return EVAL_PAGE;
	}
}
