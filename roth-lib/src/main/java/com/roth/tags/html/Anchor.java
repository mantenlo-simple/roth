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

public class Anchor extends ActionTag {
	private static final long serialVersionUID = -415852944833909100L;
	
	// doStartTag
	public int doStartTag() throws JspException {
		print(getAnchorStart());
		return EVAL_BODY_INCLUDE;
	}
	
	// doEndTag
	public int doEndTag() throws JspException {
        print(getAnchorEnd());
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
