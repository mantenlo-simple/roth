package com.roth.tags.html;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;

public class MenuItem extends HtmlTag {
	private static final long serialVersionUID = -8093977000074481049L;

	public int doEndTag() throws JspException {
		
		
		release();
		return EVAL_PAGE;
	}
	
	/**
	 * 
	 * @param id
	 * @param caption
	 * @param iconName
	 * @return
	 */
	public static String getMenuItem(String id, String caption, String iconName, String href, String onClick) {
		return getMenuItem(id, caption, iconName, null, null, href, onClick);
	}
	
	/**
	 * 
	 * @param id
	 * @param caption
	 * @param iconName
	 * @return
	 */
	public static String getMenuItemToggle(String id, String caption) {
		return getMenuItem(id, caption, null, null, null, null, "Roth.menu.toggle('" + id + "');");
	}
	
	/**
	 * 
	 * @param id
	 * @param caption
	 * @param iconName
	 * @param dropMenu
	 * @param sideDrop null or 0: 
	 * @return
	 */
	public static String getMenuItem(String id, String caption, String iconName, String dropMenu, Integer sideDrop, String href, String onClick) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (caption == null)
			throw new IllegalArgumentException("The caption argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("caption", caption);
		params.put("itemicon", iconName == null ? "" : Icon.getIcon(iconName));
		params.put("dropmenu", Data.nvl(dropMenu));
		params.put("dropicon", dropMenu == null ? "" : Icon.getIcon(sideDrop < 0 ? "caret-left" : sideDrop > 0 ? "caret-right" : "caret-down"));
		params.put("href", Data.nvl(href, "#"));
		params.put("onclick", Data.nvl(onClick) + (href == null ? " return false;" : ""));
		return applyTemplate("menuitem", params); 
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
	
	/*
	
	<li>
		<a id="${id}" href="#" onclick="tabSelect(this); return false;">
			<span>
				${itemicon}<span>${caption}</span>${dropicon}
			</span>
		</a>
		${dropmenu}
	</li>
	
	*/
}
