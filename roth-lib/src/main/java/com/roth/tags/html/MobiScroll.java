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

import com.roth.base.util.Data;

public class MobiScroll extends HtmlTag {
	private static final long serialVersionUID = 3031346893919120977L;
	
	public int doStartTag() throws JspException {
		String onScroll = "if ((this.offsetHeight + this.scrollTop) >= this.scrollHeight) " + 
							  "this.className = 'mobi-scroll bottom'; " + 
						  "else if (this.scrollTop == 0) " + 
							  "this.className = 'mobi-scroll top'; " + 
						  "else " + 
							  "this.className = 'mobi-scroll middle';";
		print(tagStart("div", attr("class", "mobi-scroll top") + attr("style", "height: " + Data.nvl((String)getValue("height"), "100%") + ";") + attr("onscroll", onScroll)));
		return EVAL_BODY_INCLUDE; 
	}
	
	public int doEndTag() throws JspException {
		println(tagEnd("div"));
		release();
		return EVAL_PAGE;
	}
	
	// Attribute Setters
	public void setHeight(String height) { setValue("height", height); }

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
