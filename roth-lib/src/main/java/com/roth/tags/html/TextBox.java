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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;

public class TextBox extends InputTag {
	private static final long serialVersionUID = 7961706972930243089L;

	public void setAutoComplete(String autoComplete) { setValue("autocomplete", autoComplete); }
	public void setGroupThousands(boolean groupThousands) { setValue("_groupThousands", Boolean.toString(groupThousands)); }
	public void setMaxLength(int maxLength) { setValue("maxlength", Integer.toString(maxLength)); }
	public void setNumber(boolean number) { setValue("_number", number); }
	public void setPassword(boolean password) { setValue("password", Boolean.toString(password)); }
	public void setPrecision(int precision) { setValue("_precision", Integer.toString(precision)); }
	public void setSigned(boolean signed) { setValue("_signed", signed); }
	public void setPlaceholder(String placeholder) { setValue("placeholder", placeholder); }
	
	static final String TEMPLATE = """
			<div class="roth-input" ${size}>
				<input type="${type}" id="${id}" name="${name}" value="${value}" ${attributes}>
			</div>
			""";
	
	@SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {
		MobiScroll mobiScroll = (MobiScroll)findAncestorWithClass(this, MobiScroll.class);
		if (mobiScroll != null)
			setValue("onfocus", "var mobiScroll = getAncestorWithClass(this, 'mobi-scroll'); mobiScroll.scrollTop = this.parentNode.parentNode.offsetTop;");
		String value = (String)getRemoveValue("value");
		boolean password = Data.nvl((String)getRemoveValue("password")).equals("true");
		boolean groupThousands = Data.nvl((String)getValue("_groupThousands")).equals("true");
		if (value == null) {
			Object dsValue = getDataSourceValue();
			HashMap<String,String> formats = (HashMap<String,String>)pageContext.getSession().getAttribute("formats");
			value = (dsValue == null) 
			      ? "" 
			      : (dsValue instanceof java.sql.Date)
			      ? Data.dateToStr((Date)dsValue, formats.get(Data.FMT_DATE))
	    		  : (dsValue instanceof java.sql.Time)
			      ? Data.dateToStr((Date)dsValue, formats.get(Data.FMT_TIME))
	    		  : (dsValue instanceof java.sql.Timestamp)
			      ? Data.dateToStr((Date)dsValue, formats.get(Data.FMT_DATETIME))
	    		  : (dsValue instanceof java.util.Date)
			      ? Data.dateToStr((Date)dsValue, formats.get(Data.FMT_DATETIME))
			      : (dsValue instanceof LocalDate)
			      ? Data.dateToStr((LocalDate)dsValue, formats.get(Data.FMT_DATETIME))
	    		  : (dsValue instanceof LocalDateTime)
			      ? Data.dateToStr((LocalDateTime)dsValue, formats.get(Data.FMT_DATETIME))
	    		  : (dsValue instanceof LocalTime)
			      ? Data.dateToStr((LocalTime)dsValue, formats.get(Data.FMT_DATETIME))
			      : (groupThousands && ((dsValue instanceof java.lang.Integer) ||
						                (dsValue instanceof java.lang.Long) ||
						                (dsValue instanceof java.lang.Float) ||
						                (dsValue instanceof java.lang.Double) ||
						                (dsValue instanceof java.math.BigDecimal)))
				  ? groupThou(dsValue.toString())
				  : dsValue.toString();
		    if (dsValue instanceof Number) 
		    	setValue("_number", true);
		    if ((dsValue instanceof Integer) || (dsValue instanceof Long) || (dsValue instanceof Short)) 
		    	setValue("_precision", "0");
		}
		if (getBooleanValue("_number", false)) {
			// If the precision isn't set, then it's assumed that it's a floating point number 
			// (as an integer's precision is already set to '0'); default to 30.
			Integer precision = Data.strToInteger(Data.nvl((String)getValue("_precision"), "30"));
			setValue("onkeydown", Data.nvl((String)getValue("onkeydown")) + "return Roth.mask.numberMask(event, false, null, " + precision + ", " +  getBooleanValue("_signed", true) + ");");
			setValue("onkeyup",  Data.nvl((String)getValue("onkeyup")) + "Roth.validate.validateNumber(event, " + precision + ", " + groupThousands + ");");
		}
		if (password) 
			setValue("autocomplete", "off");
		boolean ro = isReadOnly(); removeValue("readonly");
		if (ro) 
			setValue("readonly", "readonly");
		println(getTextBox(getId(), (String)getValue("_datasource"), escapeValue(value), (String)getValue("_label"), null, (String)getValue("_width"), this.getHTMLAttributes(), password));
		release();
		return EVAL_PAGE;
	}
	
	public static String getTextBox(String id, String name, String value, String label, String title, String width, String attributes, boolean password) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String size = getSize(width, null); // Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("type", password ? "password" : "text");
		params.put("name", name);
		params.put("value", value);
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;\"");
		String result = applyTemplate2(TEMPLATE, params);
		return Data.isEmpty(label) ? result : InputTag.getLabledInput(id, label, title, result, size, attributes.contains("required"), attributes.contains("noRender"));
	}
	
	protected String groupThou(String source) {
		boolean neg = source.indexOf("-") > -1;
		String result = source.replaceAll("-", "");
		int p = result.indexOf(".");
		if (p < 0) p = result.length();
		while (p > 3) {
			p -= 3;
			if (p > 0) result = result.substring(0, p) + "," + result.substring(p);
		}
		return neg ? "-" + result : result;
	}
}
