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

import java.util.HashMap;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;
import com.roth.tags.el.Resource;

public class InputTag extends HtmlTag {
	private static final long serialVersionUID = 1737424149164762928L;

	private static final String HUND_PERC = "width: 100%;";
	
	// Attribute Setters
	public void setChecked(boolean checked) { setValue("checked", checked); }
	public void setDataSource(Object dataSource) { setValue("_datasource", dataSource); }
	public void setHeight(String height) { setValue("_height", height); }
	public void setInnerStyle(String innerStyle) { setValue("_innerStyle", innerStyle); }
	public void setInputStyle(String style) { setStyle(style); }
	public void setLabel(String label) { setValue("_label", localize(label)); }
	public void setLabelStyle(String labelStyle) { setValue("_labelStyle", labelStyle); }
	public void setMaxLength(int maxLength) { setValue("maxlength", maxLength); }
	public void setOuterStyle(String outerStyle) { setValue("_outerStyle", outerStyle); }
	public void setOverride(boolean override) { setValue("_override", override); }
	public void setReadOnly(boolean readOnly) { setValue("_readonly", readOnly); }
	public void setNoRender(boolean noRender) { setValue("_noRender", noRender); }
	public void setRequired(boolean required) { setValue("_required", required); }
	public void setSize(int size) { setValue("size", size); }
	public void setValue(String value) { setValue("value", value); }
	public void setWidth(String width) { setValue("_width", width); }
	
	// Common Event Attribute Setters
	public void setOnBlur(String onBlur) { setValue("onblur", onBlur); }
	public void setOnChange(String onChange) { setValue("onchange", onChange); }
	public void setOnDropDown(String onDropDown) { setValue("ondropdown", onDropDown); }
	public void setOnFocus(String onFocus) { setValue("onfocus", onFocus); }
	public void setOnSelect(String onSelect) { setValue("onselect", onSelect); }
	
	protected boolean isReadOnly() {
		Form form = (Form)findAncestorWithClass(this, Form.class);
		Wrap wrap = (Wrap)findAncestorWithClass(this, Wrap.class);
        Boolean ancestorReadonly = null;
        if ((wrap != null) && (wrap.getForm() == form)) 
        	ancestorReadonly = wrap.getReadOnly();
        else if (form != null) 
        	ancestorReadonly = form.getReadOnly();
        boolean readonly = false;
        if (!getBooleanValue("_override", false) && (ancestorReadonly != null) && (ancestorReadonly)) 
            readonly = true;
        else
            readonly = getBooleanValue("_readonly", false); 
        return readonly;
    }
	
	protected String escapeValue(String value) {
		return value.replaceAll("\"", "&quot;");
	}
	
	protected String getEscapedValue() {
		return escapeValue((String)getValue("value"));
	}
	
	protected String getInput(String type) {
		return getInput(type, false);
	}
	
	protected boolean isRequired() {
		return getBooleanValue("_required", false); 
	}
	
	protected String getRequiredAttr() {
		return isRequired() ? " required=\"true\"" : "";
	}
	
	protected String getInput(String type, boolean altId) {
	    boolean ro = isReadOnly();
		String readonly = ro ? " readonly" : "";
		if (ro) 
			setValue("class", Data.nvl((String)getValue("class")) + " jreadonly");
		if (type.equals("checkbox") && ro) readonly = " disabled=\"disabled\"";
		String n = getName();
		String name = (type.equals("checkbox")) ? "__" + n : n;
		if (altId) 
			setValue("id", getValue("id") + "_alt");
		if (getValue("_width") != null)
			setValue("style", Data.nvl((String)getValue("style")) + HUND_PERC);
		String input = tag("input", attr("type", type) + attr("name", name) + readonly + getRequiredAttr() + getHTMLAttributes(), null);
		if (type.equals("hidden")) return input;
		String result = input;
		if (type.equals("checkbox")) {
			String label = (String)getRemoveValue("_label");
			if ((label != null) && label.startsWith("##"))
				label = Resource.getString(pageContext, label.substring(2));
			String title = (String)getValue("title");
			title = title == null ? "" : attr("title", title);
			result += tag("label", attr("for", (String)getValue("id")) + attr("class", "cb-toggle fa-stack"), 
							//Image.getStackedImage("square", "s", "square", "r") + 
							//Image.getStackedImage("square", "s", "check-square", "r")
							Image.getStackedImage("square", "s", "square", "r", "check-square", "r")
						 );
			if (label != null) result += tag("label", attr("for", (String)getValue("id")) + title, label);
			if (getBooleanValue("_wrap", false)) setValue("_label", "&nbsp;");
			String hid = (String)getValue("_hid");
			result += tag("input", attr("type", "hidden") + attr("id", hid) + attr("name", n) + attr("value", getEscapedValue()), null);
		}
		else
			result = tag("div", attr("class", "jib") + (getValue("_width") == null ? "" : attr("style", HUND_PERC)), input);
		
		return getWrap(result);
	}
	
	protected String getDropdown(String imageName, String imageAttr) {
	    boolean ro = isReadOnly();
	    String readonly = (ro) ? " readonly" : "";
	    if (ro) 
	    	setValue("class", Data.nvl((String)getValue("class")) + " jreadonly");
        String image = Image.getImage("calendar-alt jedticon", null, Data.nvl(imageAttr));
		String input = tag("input", attr("type", "text") + attr("name", getName()) + readonly + getRequiredAttr() + getHTMLAttributes(), null);
		input = tag("span", null, input);
		String result = tag("div", attr("class", "jib"), input) + image;
		return getWrap(result);
	}
	
	protected String getTextArea(String value) {
	    boolean ro = isReadOnly();
	    String readonly = (ro) ? " readonly" : "";
	    if (ro) 
	    	setValue("class", Data.nvl((String)getValue("class")) + " jreadonly");
        String input = tag("textarea", attr("name", getName()) + readonly + getRequiredAttr() + getHTMLAttributes(), value);
        String innerStyle = Data.nvl((String)getValue("_innerStyle"));
		if (!innerStyle.isEmpty())
			innerStyle = attr("style", innerStyle);
		String result = tag("div", attr("class", "jib") + innerStyle, input);
		return getWrap(result);
	}
	
	protected String getName() {
		String dataSource = (String)getValue("_datasource");
		return dataSource.replace("requestScope", "request")
		          		 .replace("sessionScope", "session")
		          		 .replace("applicationScope", "application");
	}
	
	protected Object getDataSourceValue() throws JspException {
		return getDataSourceValue(null);
	}
	
	protected Object getDataSourceValue(String sourceKey) throws JspException {
		ELContext c = pageContext.getELContext();
		String[] objs = getValue(Data.nvl(sourceKey, "_datasource")).toString().split("\\.");
		Object result = null;
		for (int i = 0; i < objs.length; i++) {
			String name = getDsvSegName(objs[i]);
			Integer idx = getDsvSegIndex(objs[i]);
			result = c.getELResolver().getValue(c, result, name);
			if (idx != null) result = Data.itemOf(result, idx);
		}
		return result;
	}
	
	protected String getDsvSegName(String source) {
		int i = source.indexOf("[");
		return (i < 0) ? source : source.substring(0, i);
	}
	
	protected Integer getDsvSegIndex(String source) throws JspException {
		int i = source.indexOf("[");
		if (i < 0) return null;
		int x = source.indexOf("]", i);
		if (x < 0) throw new JspException("Malformed EL expression index notation.");
		return (i < 0) ? null : Integer.valueOf(source.substring(i + 1, x));
	}
	
	protected String getWrap(String body) {
		String label = (String)getValue("_label");
		if ((label != null) && label.startsWith("##"))
			label = Resource.getString(pageContext, label.substring(2));
		if (label == null) return body;
		String lblClass = !isRequired() ? "jlbl" : "jlbl jrequired";
		String title = (String)getValue("title");
		title = title == null ? "" : attr("title", title);
		String labelStyle = Data.nvl((String)getValue("_labelStyle"));
		if (!labelStyle.isEmpty())
			labelStyle = attr("style", labelStyle);
		String tagbody = tag("div", attr("class", lblClass) + labelStyle + title, label) + body;
		String outerStyle = Data.nvl((String)getValue("_outerStyle"));
		if (getValue("_width") != null)
			outerStyle += "width:" + getValue("_width") + ";";
		if (!outerStyle.isEmpty())
			outerStyle = attr("style", outerStyle);
		return tag("div", attr("class", "jedt") + outerStyle, tagbody);
	}
	
	// NEW CODE
	
	public static String getSize(String width, String height) {
		String w = Data.isEmpty(width) ? "" : "width:" + width + ";";
		String h = Data.isEmpty(height) ? "" : "height:" + height + ";";
		return !Data.isEmpty(w) || !Data.isEmpty(h) ? " style=\"" + w + h + "\"" : ""; 
	}
	
	public static String getLabledInput(String id, String label, String title, String input, String attributes, boolean required, boolean noRender) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (label == null)
			throw new IllegalArgumentException("The label argument may not be null.");
		if (input == null)
			throw new IllegalArgumentException("The input argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", label);
		params.put("title", Data.nvl(title));
		params.put("input", input);
		params.put("attributes", Data.nvl(attributes));
		params.put("required", required ? "required" : "");
		params.put("norender", noRender ? "norender" : "");
		return applyTemplate("labeledinput", params);
	}
	
	public static String getLabeledInput(Map<String,String> attrMap) {
		if (attrMap.get("id") == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (attrMap.get("label") == null)
			throw new IllegalArgumentException("The label argument may not be null.");
		if (attrMap.get("input") == null)
			throw new IllegalArgumentException("The input argument may not be null.");
		return applyTemplate("labeledinput", attrMap);
	}
	
	/*
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
		if (Data.isEmpty(label))
			return applyTemplate("textbox", params);
		else
			return getLabledInput(id, label, title, applyTemplate("textbox", params), size, attributes.contains("required"), attributes.contains("noRender"));
	}
	*/
	
	public static String getFile(String id, String name, String accept, String nameFilename, String valueFilename, String label, String title, String width, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String size = getSize(width, null); // Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("accept", accept);
		params.put("nameFilename", nameFilename);
		params.put("valueFilename", Data.nvl(valueFilename));
		params.put("valueFilenameLabel", Data.nvl(valueFilename, "<span style=\"color: gray;\">Choose a file...</span>"));
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("file", params);
		else
			return getLabledInput(id, label, title, applyTemplate("file", params), size, attributes.contains("required"), attributes.contains("noRender"));
	}
	
	public static String getLookup(String id, String icon, String name, String value, String label, String title, String width, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (icon == null)
			throw new IllegalArgumentException("The icon argument may not be null.");
		String size = getSize(width, null); // Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("icon", icon);
		params.put("name", name);
		params.put("value", value);
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("lookup", params);
		else
			return getLabledInput(id, label, title, applyTemplate("lookup", params), size, attributes.contains("required"), attributes.contains("noRender"));
	}
	
	public static String getTextArea(String id, String name, String value, String label, String title, String width, String height, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String size = getSize(width, height); // Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("value", value);
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;height:100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("textarea", params);
		else
			return getLabledInput(id, label, title, applyTemplate("textarea", params), size, attributes.contains("required"), attributes.contains("noRender"));
	}
	
	public static String getQuill(String id, String name, String value, String label, String title, String width, String height, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String size = getSize(width, height); // Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("value", value);
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;height:100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("quill", params);
		else
			return getLabledInput(id, label, title, applyTemplate("quill", params), size, attributes.contains("required"), attributes.contains("noRender"));
	}
	
	public static String getTinyMce(String id, String name, String value, String label, String title, String width, String height, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String size = getSize(width, height); // Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("value", value);
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;height:100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("tinymce", params);
		else
			return getLabledInput(id, label, title, applyTemplate("tinymce", params), size, attributes.contains("required"), attributes.contains("noRender"));
	}
	
	public static String getCodeMirror(String id, String name, String value, String label, String title, String width, String height, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String size = getSize(width, height);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("value", value);
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;height:100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("codemirror", params);
		else
			return getLabledInput(id, label, title, applyTemplate("codemirror", params), size, attributes.contains("required"), attributes.contains("noRender"));
	}
	
	public static String getSelect(String id, String name, String value, String label, String title, String options, String width, String onchange, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String size = getSize(width, null);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("options", Data.nvl(options));
		params.put("name", name);
		params.put("value", value);
		params.put("size", Data.isEmpty(label) ? size : "");
		params.put("onchange", Data.nvl(onchange));
		params.put("attributes", Data.nvl(attributes) + " style=\"width:100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("select", params);
		else
			return getLabledInput(id, label, title, applyTemplate("select", params), size, attributes.contains("required"), attributes.contains("noRender"));
	}
	
	public static String getCheckBox(String id, String name, String value, String label, String title, String falseVal, String trueVal, String onclick, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (name == null)
			throw new IllegalArgumentException("The name argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("value", Data.nvl(value));
		params.put("title", Data.nvl(title));
		params.put("false", Data.nvl(falseVal, "false"));
		params.put("true", Data.nvl(trueVal, "true"));
		params.put("onclick", Data.nvl(onclick));
		params.put("attributes", attributes);
		return applyTemplate("checkbox", params);
	}
	
	public static String getRadio(String id, String index, String name, String value, String label, String title, String onclick, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("index", index);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("value", value);
		params.put("onclick", Data.nvl(onclick));
		params.put("attributes", Data.nvl(attributes));
		return applyTemplate("radio", params);
	}
	
	public static String getRadioGroup(String id, String name, String value, String label, String title, String options, String onclick, String onchange, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("value", value);
		params.put("options", options);
		params.put("onclick", Data.nvl(onclick));
		params.put("onchange", Data.nvl(onchange));
		params.put("attributes", Data.nvl(attributes));
		return applyTemplate("radiogroup", params);
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
