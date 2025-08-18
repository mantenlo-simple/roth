package com.roth.tags.html;

import java.util.HashMap;
import java.util.Map;

import com.roth.base.util.Data;

import jakarta.servlet.jsp.JspException;

public class HtmlBox extends InputTag {
	private static final long serialVersionUID = -7887895921374555160L;
	
	private static String TEMPLATE = """
			<div class="roth-input html-box" ${size}>
				<textarea id="${id}" name="${name}" ${attributes}>${value}</textarea>
			</div>
			""";
	
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
		//println(getTinyMce(getId(), (String)getValue("_datasource"), escapeValue(value), (String)getValue("_label"), null, (String)getValue("_width"), (String)getValue("_height"), this.getHTMLAttributes()));
		println(getInput(getAttrMap()));
		release();
		return EVAL_PAGE;
	}
	
	//@Override //<-- This is a placeholder for a redesign of the InputTag
	private String getInputValue() throws JspException {
		String value = (String)getRemoveValue("value");
		if (value == null) {
			Object dsValue = getDataSourceValue();
			value = (dsValue == null) ? "" : dsValue.toString();
		}
		return value;
	}
	
	//@Override //<-- This is a placeholder for a redesign of the InputTag
	private Map<String,String> getAttrMap() throws JspException {
		String label = getStringValue("_label", "");
		String size = getSize(getStringValue("_width"), getStringValue("_height"));
		Map<String,String> params = new HashMap<>();
		params.put("id", getId());
		params.put("label", label);
		params.put("name", getStringValue("_datasource"));
		params.put("value", getInputValue());
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(getHTMLAttributes()) + " style=\"width:100%;height:100%;\"");
		return null;
	}
	
	public static String getInput(Map<String,String> attrMap) {
		if (attrMap.get("id") == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String result = applyTemplate(TEMPLATE, attrMap);
		attrMap.put("input", result);
		return Data.isEmpty(attrMap.get("label")) ? result : getLabeledInput(attrMap);
	}
}
