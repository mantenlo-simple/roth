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

public class File extends InputTag {
	private static final long serialVersionUID = 7961706972930243089L;

	public void setAccept(String accept) { setValue("_accept", accept); }
	public void setFilenameDataSource(String filenameDataSource) { setValue("_filenamedatasource", filenameDataSource); }
	
	public int doEndTag() throws JspException {
		MobiScroll mobiScroll = (MobiScroll)findAncestorWithClass(this, MobiScroll.class);
		if (mobiScroll != null)
			setValue("onfocus", "var mobiScroll = getAncestorWithClass(this, 'mobi-scroll'); mobiScroll.scrollTop = this.parentNode.parentNode.offsetTop;");
		String valueFilename = (String)getDataSourceValue("_filenamedatasource");
		boolean ro = isReadOnly(); removeValue("readonly");
		if (ro) setValue("readonly", "readonly");
		println(getFile(getId(), (String)getValue("_datasource"), (String)getValue("_accept"), (String)getValue("_filenamedatasource"), valueFilename, (String)getValue("_label"), null, (String)getValue("_width"), this.getHTMLAttributes()));
		release();
		return EVAL_PAGE;
	}
}
