package com.roth.tags.html.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.roth.base.util.Data;

public class HtmlGenerator {
	/**
	 *  Prevent instantiation.
	 */
	private HtmlGenerator() {}
	
	private static Map<String,String> templates;
	
	static {
		templates = new HashMap<>();
		String source = Data.readTextFile(HtmlGenerator.class, "/com/roth/tags/html/resource/templates.html");
		int t = 0;
		do {
			t = source.indexOf("<template ", t);
			if (t > -1) {
				int i = source.indexOf(" id=", t);
				int ei = source.indexOf("\"", i + 5);
				String id = source.substring(i + 5, ei); 
				int et = source.indexOf("</template>", t);
				String template = source.substring(ei + 2, et).trim();
				templates.put(id, template);
				t = et;
			}
		} while(t > -1);
	}
	
	private static String applyTemplate(String id, Map<String,String> parameters) {
		String template = templates.get(id);
		for (Entry<String,String> e : parameters.entrySet())
			template = template.replace(String.format("${%s}", e.getKey()), e.getValue());
		return template;
	}
	
	/**
	 * Creates the HTML code for a Font Awesome icon.
	 * The name parameter is the icon name.  The default style is 'solid'.  
	 * If a different style is desired, the name should be prefixed with a
	 * single character identifying the style, followed by a period.<br/>  
	 * <br/>
	 * Examples:<br/>
	 * name = "plus" -- This translates to "fas fa-plus".<br/>
	 * name = 'r.plus" -- This translates to "far fa-plus".<br/>
	 * <br/>
	 * Styles: 's' - solid, 'r' - regular, 'l' - light, 't' - thin (new in 6.0), and 'd' - duotone<br/>
	 * <br/>
	 * Note: This framework only includes the free icon set.  
	 * The pro set may be used by overriding the JavaScript inclusion.
	 * @param name
	 * @return
	 */
	public static String getIcon(String name) {
		if (name == null)
			throw new IllegalArgumentException("The name argument may not be null.");
		String[] n = name.split("\\.");
		String _style = n.length > 1 ? n[0] : "s";
		String _name = n.length > 1 ? n[1] : n[0];
		//return String.format(
		//	"""
		//	<i class="fa%s fa-%s"></i>
		//	""", _style, _name);
		return String.format("<i class=\"fa%s fa-%s\"></i>\n", _style, _name);
	}
	
	/**
	 * Creates the HTML code for a Font Awesome stacked icon.
	 * The parameters are the icon names.
	 * @param name
	 * @return
	 * @see #getIcon
	 */
	public static String getStackedIcon(String ... names) {
		if (names == null)
			throw new IllegalArgumentException("The names argument may not be null.");
		//return String.format(
		//	"""
		//	<span class="fa-stack">
		//		%s
		//	</span>
		//	""", Arrays.asList(names).stream().map(name -> getIcon(name)).reduce("", String::concat));
		return String.format("<span class=\"fa-stack\">\n%s\n</span>\n", Arrays.asList(names).stream().map(name -> getIcon(name)).reduce("", String::concat));
	}
	
	public static String getButton(String type, String id, String href, String onclick, String label, String icon, String title) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("type", Data.nvl(type));
		params.put("href", Data.nvl(href, "#"));
		params.put("onclick", (Data.isEmpty(onclick) ? "" : onclick) + (Data.isEmpty(href) ? " return false;" :  ""));
		params.put("caption", label == null ? "" : String.format("<span>%s</span>", label));
		params.put("icon", Data.nvl(icon));
		params.put("title", Data.nvl(title));
		return applyTemplate("button", params);
	}

	
	public static String getTabset(String id, String tabs, String onselect, boolean fullWidth) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (tabs == null)
			throw new IllegalArgumentException("The tabs argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("fullwidth", fullWidth ? "full-width" : "");
		params.put("onselect", Data.nvl(onselect));
		params.put("tabs", tabs);
		return applyTemplate("tabset", params);
	}
	
	public static String getTab(int index, String id, String pageid, String label, String icon, String href, String title, boolean selected) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("index", Integer.toString(index));
		params.put("pageid", pageid);
		params.put("caption", Data.nvl(label));
		params.put("icon", Data.nvl(icon));
		params.put("href", Data.nvl(href, "#"));
		params.put("title", Data.nvl(title));
		params.put("checked", selected ? "checked" : "");
		return applyTemplate("tab", params);
	}
	

	public static String getLabledInput(String id, String label, String title, String input, String attributes) {
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
		return applyTemplate("labeledinput", params);
	}
	
	public static String getTextBox(String id, String name, String value, String label, String title, String width, String attributes, boolean password) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String w = Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", label);
		params.put("type", password ? "password" : "text");
		params.put("name", name);
		params.put("value", value);
		params.put("attributes", Data.isEmpty(label) ? Data.nvl(attributes) + w : Data.nvl(attributes) + " style=\"width: 100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("textbox", params);
		else
			return getLabledInput(id, label, title, applyTemplate("textbox", params), w);
	}
	
	public static String getSelect(String id, String name, String value, String label, String title, String options, String width, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		String w = Data.isEmpty(width) ? "" : String.format(" style=\"width: %s;\"", width);
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", label);
		params.put("options", Data.nvl(options));
		params.put("name", name);
		params.put("value", value);
		params.put("attributes", Data.isEmpty(label) ? Data.nvl(attributes) + w : Data.nvl(attributes) + " style=\"width: 100%;\"");
		if (Data.isEmpty(label))
			return applyTemplate("select", params);
		else
			return getLabledInput(id, label, title, applyTemplate("select", params), w);
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
		params.put("label", label);
		params.put("name", name);
		params.put("value", value);
		params.put("onclick", Data.nvl(onclick));
		params.put("attributes", Data.nvl(attributes));
		return applyTemplate("radio", params);
	}
	
	public static String getRadioGroup(String id, String name, String value, String label, String title, String options, String onclick, String attributes) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("label", Data.nvl(label));
		params.put("name", name);
		params.put("value", value);
		params.put("options", options);
		params.put("onclick", Data.nvl(onclick));
		params.put("attributes", Data.nvl(attributes));
		return applyTemplate("radiogroup", params);
	}
	
	public static String getToggleRadio(String id, String name, String value, String label, boolean checked) {
		String _checked = checked ? "checked=\"checked\"" : "";
		//return String.format(
		//	"""
		//	<input id="%s" type="radio" name="%s" value="%s" %s/><label for="%s"><span>%s</span></label>
		//	""", id, name, value, _checked, id, label);
		return String.format("<input id=\"%s\" type=\"radio\" name=\"%s\" value=\"%s\" %s/><label for=\"%s\"><span>%s</span></label>\n", id, name, value, _checked, id, label);
	}
	
	public static String getToggle(String body) {
		//return String.format(
		//	"""
		//	<div class="roth-toggle">
		//		%s
		//	</div>	
		//	""", body);
		return String.format("<div class=\"roth-toggle\">\n%s\n</div>", body);
	}
	
	//public static String getRadio(String id, String index, String name, String value, String label, String title, String onclick, String attributes) {
	//	
	//}
	
	public static String input(String type, String id, String name, String value, String title, String style, String events, String options) {
		if (Data.in(type, new String[] { "text", "password", "checkbox", "radio" }))
			return String.format("<input type=\"%s\" id=\"%s\" name=\"%s\" value=\"%s\" title=\"%s\" style=\"%s\" %s/>", type, id, name, value, title, style, events);
		else if (type == "textarea")
			return String.format("<textarea id=\"%s\" name=\"%s\" title=\"%s\" style=\"%s\" %s>%s</textarea>", id, name, title, style, events, value);
		else if (type == "select")
			return String.format("<select id=\"%s\" name=\"%s\" value=\"%s\" title=\"%s\" style=\"%s\" %s>%s</select>", id, name, value, title, style, events, options);
		else
			return "";
	}
	
	//<input id="__${id}${index}" type="radio" name="__${name}" value="${value}" onclick="_$('${id}').value = 'value'; ${onclick}"/>
	
	
	public static String label(String forId, String label) {
		return String.format("<label for=\"%s\">%s</label>", forId, label);
	}
	
	public static String iconLabel(String forId, String name) {
		return label(forId, div(icon(name)));
	}
	
	public static String div(String content) {
		return String.format("<div>%s</div>", content);
	}
	
	public static String div(String className, String content) {
		return String.format("<div class=\"$s\">%s</div>", className, content);
	}
	
	public static String span(String content) {
		return String.format("<span>%s</span>", content);
	}
	
	public static String icon(String name) {
		String[] sn = name.split(":");
		String s = sn.length == 2 ? sn[0] : "s";
		String n = sn.length == 2 ? sn[1] : sn[0];
		return String.format("<i class=\"fa%s fa-%s\"></i>", s, n);
	}
	
	public static String layeredIcons(String icons) {
		return String.format("<span class=\"fa-layers fa-fw\">%s</span>", icons);
	}
	
	public static String layerCounter(int value) {
		return String.format("<span class=\"fa-layers-counter\">%s</span>", value);
	}
	
	public static String br() { 
		return "<br/>"; 
	}
}
