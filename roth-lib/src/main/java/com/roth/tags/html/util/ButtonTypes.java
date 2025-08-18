package com.roth.tags.html.util;

import java.util.HashMap;
import java.util.Map;

public class ButtonTypes {
	private static final Map<String,String> IMAGE_NAMES;
	private static final Map<String,String> CAPTIONS;
	static {
		Map<String,String> imageNames = new HashMap<>();
		imageNames.put("ok", "");
		imageNames.put("add", "");
		imageNames.put("edit", "");
		imageNames.put("delete", "");
		imageNames.put("open", "");
		imageNames.put("load", "");
		imageNames.put("view", "");
		imageNames.put("preview", "");
		imageNames.put("refresh", "");
		imageNames.put("close", "");
		imageNames.put("save", "");
		imageNames.put("cancel", "");
		imageNames.put("submit", "");
		imageNames.put("accept", "");
		imageNames.put("yes", "");
		imageNames.put("no", "");
		imageNames.put("search", "");
		imageNames.put("import", "");
		imageNames.put("export", "");
		imageNames.put("login", "");
		imageNames.put("logout", "");
		IMAGE_NAMES = imageNames;
		
		Map<String,String> captions = new HashMap<>();
		captions.put("ok", "");
		captions.put("add", "");
		captions.put("edit", "");
		captions.put("delete", "");
		captions.put("open", "");
		captions.put("load", "");
		captions.put("view", "");
		captions.put("preview", "");
		captions.put("refresh", "");
		captions.put("close", "");
		captions.put("save", "");
		captions.put("cancel", "");
		captions.put("submit", "");
		captions.put("accept", "");
		captions.put("yes", "");
		captions.put("no", "");
		captions.put("search", "");
		captions.put("import", "");
		captions.put("export", "");
		captions.put("login", "");
		captions.put("logout", "");
		CAPTIONS = captions;
	}
	
	
	public static String getImageName(String type) {
		return IMAGE_NAMES.get(type);
	}
	
	public static String getCaption(String type) {
		return CAPTIONS.get(type);
	}

}
