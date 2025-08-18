package com.roth.servlet.util;

import java.io.Serializable;

public class IndicatorEntry implements Serializable {
	private static final long serialVersionUID = -4919014348352543126L;

	private String id;
	private String cssClass;
	private String iconName;
	private String color;
	private String background;
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getCssClass() { return cssClass; }
	public void setCssClass(String cssClass) { this.cssClass = cssClass; }
	
	public String getIconName() { return iconName; }
	public void setIconName(String iconName) { this.iconName = iconName; }
	
	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }
	
	public String getBackground() { return background; }
	public void setBackground(String background) { this.background = background; }
}
