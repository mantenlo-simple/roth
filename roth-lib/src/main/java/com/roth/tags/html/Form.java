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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

public class Form extends ActionTag {
	private static final long serialVersionUID = 8389120730985844536L;

	public int doStartTag() throws JspException {
		setValue("action", getActionUrl());
		if (getValue("method") == null) setValue("method", "POST");
		else if (getValue("method").toString().equalsIgnoreCase("AJAX")) {
			setValue("method", "POST");
			setValue("ajax", "AJAX");
		}
		if (getBooleanValue("_blob", false))
			setValue("responsetype", "blob");
		String onAjax = genOnAjax();
		if (onAjax != null)
			setValue("onajax", onAjax + getStringValue("onajax", ""));
		println(tagStart("form", getHTMLAttributes()));
		println(getXsrfInput(pageContext));
		return EVAL_BODY_INCLUDE;
	}
	
	public int doEndTag() throws JspException {
		println(tagEnd("form"));
		release();
		return EVAL_PAGE;
	}
	
	public static String getXsrfInput(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String responseXsrf = (String)request.getAttribute("_csrf-token");
		return responseXsrf == null ? "" : "<input type=\"hidden\" name=\"_csrf-token\" value=\"" + responseXsrf.replace("\"", "&quot;") + "\">";
	}
	
	private String genOnAjax() throws JspException {
		String dataGridId = getStringValue("_dataGridId");
		String containerId = getStringValue("_containerId");
		if (dataGridId == null && containerId == null)
			return null;
		if (dataGridId != null && containerId == null)
			throw new JspException("When using default grid refresh, both dataGridId and containerId must supplied.");
		boolean autoClose = getBooleanValue("_autoClose", true);
		boolean preserveState = dataGridId == null ? false : getBooleanValue("_preserveState", true);
		boolean notifySave = dataGridId == null ? false : getBooleanValue("_nofitySave", true);
		
		String close = autoClose ? ", null, this" : "";
		String stateStore = preserveState ? "let state = Roth.grid.getState('{GRID_ID}');".replace("{GRID_ID}", dataGridId) : "";
		String stateRestore = preserveState ? "if (state) Roth.grid.setState('{GRID_ID}', state, 'key');".replace("{GRID_ID}", dataGridId) : "";
		String notify = notifySave ? "Roth.getDialog('flash').flash('Saved successfully.');" : "";
		String callback = getStringValue("_callback", "");
		return """
			{STATE_STORE}
			Roth.ajax.htmlCallback(request, '{CONTAINER_ID}', () => {
					{STATE_RESTORE}
					{NOTIFY_SAVE}
					{CALLBACK}
				}{CLOSE});
			"""
			.replace("{CONTAINER_ID}", containerId)
			.replace("{STATE_STORE}", stateStore)
			.replace("{STATE_RESTORE}", stateRestore)
			.replace("{NOTIFY_SAVE}", notify)
			.replace("{CLOSE}", close)
			.replace("{CALLBACK}", callback);
	}
	
	public boolean getReadOnly() { return getBooleanValue("_readonly", false); }
	
	// Attribute Setters
	public void setAccept(String accept) { setValue("accept", accept); }
	public void setAcceptCharset(String acceptCharset) { setValue("accept-charset", acceptCharset); }
	public void setAutoComplete(String autoComplete) { setValue("autocomplete", autoComplete); }
	public void setBlob(boolean blob) { setValue("_blob", blob); }
	
	public void setAutoClose(Boolean autoClose) { setValue("_autoClose", autoClose); }
	public void setCallback(String callback) { setValue("_callback", callback); }
	public void setContainerId(String containerId) { setValue("_containerId", containerId); }
	public void setDataGridId(String dataGridId) { setValue("_dataGridId", dataGridId); }
	public void setNotifySave(Boolean notifySave) { setValue("_notifySave", notifySave); }
	public void setPreserveState(Boolean preserveState) { setValue("_preserveState", preserveState); }
	
	public void setEncType(String encType) { setValue("enctype", encType); }
	public void setMethod(String method) { setValue("method", method); }
	public void setName(String name) { setValue("name", name); }
	public void setReadOnly(boolean readOnly) { setValue("_readonly", readOnly); }
    public void setTarget(String target) { setValue("target", target); }
	// Event Attribute Setters
	public void setOnSubmit(String onSubmit) { setValue("onsubmit", onSubmit); }
	public void setOnAjax(String onAjax) { setValue("onajax", onAjax); }
	public void setOnAjaxError(String onAjaxError) { setValue("onajaxerror", onAjaxError); }
	public void setOnAjaxResponse(String onAjaxResponse) { setValue("onajaxresponse", onAjaxResponse); }

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
