package com.roth.tags.html;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;

public class Menu extends HtmlTag {
	private static final long serialVersionUID = -8093977000074481049L;

	public int doEndTag() throws JspException {
		
		
		release();
		return EVAL_PAGE;
	}
	
	public static String getMenu(String id, String items, boolean right) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (items == null)
			throw new IllegalArgumentException("The items argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("items", items);
		params.put("right", right ? "right" : "");
		return applyTemplate("menu", params); 
	}
	
	public static String getDropMenu(String items, boolean icons) {
		if (items == null)
			throw new IllegalArgumentException("The items argument may not be null.");
		Map<String,String> params = new HashMap<>();
		params.put("items", items);
		params.put("icons", icons ? "icons" : "");
		return applyTemplate("dropmenu", params); 
	}
	
	public static String getIconMenu(String id, String menuIcon, String caption, String dropIcon, String items, boolean right, boolean icons) {
		if (id == null)
			throw new IllegalArgumentException("The id argument may not be null.");
		if (items == null)
			throw new IllegalArgumentException("The items argument may not be null.");
		if (menuIcon == null && caption == null)
			throw new IllegalArgumentException("The menuIcon and caption arguments may not both be null.");
		Map<String,String> params = new HashMap<>();
		params.put("id", id);
		params.put("menuicon", menuIcon == null ? "" : getIcon(menuIcon));
		params.put("caption", Data.nvl(caption));
		params.put("dropicon", dropIcon == null ? "" : getIcon(dropIcon));
		params.put("items", items);
		params.put("right", right ? "right" : "");
		params.put("icons", icons ? "icons" : "");
		return applyTemplate("iconmenu", params);
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
	
	<template id="dropmenu">
		<div class="roth-dropmenu">
			<ul>
				${items}
			</ul>
		</div>
	</template>
	
	<template id="menu">
		<div class="roth-menu full-width" id="${id}">
			<ul>
				${items}
			</ul>
		</div>
	</template>
	
	<template id="iconmenu">
		<div class="roth-iconmenu">
			<a id="${id}" href="#" onclick="tabSelect(this); return false;">
				<span>
					<i class="fas fa-bars"></i>
				</span>
			</a>
			<div class="roth-dropmenu">
				<ul>
					${items}
				</ul>
			</div>
		</div>
	</template>
	
	*/
}
