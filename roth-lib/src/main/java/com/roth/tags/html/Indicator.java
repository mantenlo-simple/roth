package com.roth.tags.html;

import jakarta.servlet.jsp.JspException;

public class Indicator extends HtmlTag {
	private static final long serialVersionUID = 5874789158880548208L;

	public void setBackground(String background) { setValue("background", background.toLowerCase()); }
	public void setColor(String color) { setValue("color", color.toLowerCase()); }
	public void setIconName(String iconName) { setValue("iconName", iconName.toLowerCase()); }
	
	@Override
	public int doEndTag() throws JspException {
		if (getValue("iconName") == null)
			throw new JspException("The iconName attribute is required.");
		// Background
		String background = getStringValue("background", "");
		if (!background.isEmpty())
			background = String.format("background: %s;", background);
		// Color
		String color = getStringValue("color", "");
		if (!color.isEmpty())
			color = String.format("color: %s;", color);
		// Icon (required)
		String icon = getIcon(getStringValue("iconName", "badicon"));
		// Class
		String cssClass = getStringValue("class", "");
		if (!cssClass.isEmpty())
			cssClass = String.format(" class=\"%s\"", cssClass);
		// ID
		String id = String.format(" id=\"%s\"", getId());
		String attr = attr("style", background + color);
		print(tag("DIV", id + cssClass + attr, icon));
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
