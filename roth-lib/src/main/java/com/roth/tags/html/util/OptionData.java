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
package com.roth.tags.html.util;

import java.io.Serializable;

public class OptionData implements Serializable {
	private static final long serialVersionUID = -6381612949022603422L;

	private String action;
	private String caption;
	private String iconName;
	private String overlayName;
	private String pageId;
	private boolean selected;
	private String value;
	
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
	
	public String getCaption() { return caption; }
	public void setCaption(String caption) { this.caption = caption; }

	public String getIconName() { return iconName; }
	public void setIconName(String iconName) { this.iconName = iconName; }
	
	public String getOverlayName() { return overlayName; }
	public void setOverlayName(String overlayName) { this.overlayName = overlayName; }
	
	public String getPageId() { return pageId; }
	public void setPageId(String pageId) { this.pageId = pageId; }
	
	public boolean getSelected() { return selected; }
	public void setSelected(boolean selected) { this.selected = selected; }
	
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
}
