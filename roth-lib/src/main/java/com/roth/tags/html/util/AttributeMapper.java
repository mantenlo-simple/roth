package com.roth.tags.html.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeMapper {
	private final static String[] AUDIO_VIDEO = {
		"autoplay", "controls", "loop", "muted", "oncanplaythrough", "ondurationchange", "onemptied", "onended",
		"onloadeddata", "onloadedmetadata", "onloadstart", "onpause", "onplay", "onplaying", "onprogress",
		"onratechange", "onseeked", "onseeking", "onstalled", "onsuspend", "ontimeupdate", "onvolumechange",
		"onwaiting", "preload"
	};
	private final static String[] FORM = {
		"accept-charset", "action", "enctype", "method", "novalidate", "onreset", "onsubmit", "autocomplete"
	};
	
	// video only: "poster"
	// track: "default", "kind", "oncuechange", "srclang", "label" (this one is also for option/optgroup
	
	private final static String[] GLOBAL = {
		"accesskey", "class", "contenteditable", "data-*", "dir", "draggable", "enterkeyhint", "hidden",
		"id", "inert", "inputmode", "lang", "popover", "spellcheck", "style", "tabindex", "title", "translate"
	};
	private final static String[] INPUT = {
		"accept", "checked", "list", "onsearch", "pattern", "step", "min", "max", "multiple", "size", "required",
		"dirname", "maxlength", "placeholder", "readonly"
	};
	private final static String[] OL = {
		"reversed", "start"
	};
	private final static String[] OPTION = { "selected" };
	private final static String[] TEXT_AREA = {
		"cols", "rows", "wrap"
	};
	private final static String[] TH = { "scope" };
	private final static String[] VISIBLE_ELEMENTS = {
		"onblur", "onchange", "onclick", "oncontextmenu", "oncopy", "oncut", "ondblclick", "ondrag", "ondragend",
		"ondragenter", "ondragleave", "ondragover", "ondragstart", "ondrop", "onfocus", "oninput", "oninvalid",
		"onkeydown", "onkeypress", "onkeyup", "onmousedown", "onmousemove", "onmouseout", "onmouseover",
		"onmouseup", "onmousewheel", "onpaste", "onscroll", "onselect", "onwheel"
	};
	
	
	private final static Map<String,List<String>> TAG_MAP;
	static {
		TAG_MAP = new HashMap<>();
		//TAG_MAP.put("form", null);
	}
	
	private AttributeMapper() {}
	
	public String mapAttr(String tagName, Map<String,String> attrMap) {
		
		
		
		/*
		
		Global
		
		Visible Elements
		
		
		
		 */
		
		
		
		
		return null;
	}
	
}
