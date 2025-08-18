package com.roth.tags.html.util;

import java.io.Serializable;

public class MenuItemData implements Serializable {
	private static final long serialVersionUID = 2311324968165742033L;

	private String id;
	private String iconName;
	private String caption;
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getIconName() { return iconName; }
	public void setIconName(String iconName) { this.iconName = iconName; }
	
	public String getCaption() { return caption; }
	public void setCaption(String caption) { this.caption = caption; }
}
