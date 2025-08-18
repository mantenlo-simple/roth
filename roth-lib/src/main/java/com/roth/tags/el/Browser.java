package com.roth.tags.el;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;

public class Browser implements Serializable {
	private static final long serialVersionUID = 3037319062286530944L;

	public static boolean mobile(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		return request.getHeader("User-Agent").indexOf("Mobi") != -1;
	}
	
	public static String browserName(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String[] browsers = {"ABrowse", "Acoo", "America Online Browser", "AmigaVoyager", "AOL", "Arora",
				             "Avant Browser", "Beonex", "BonEcho", "Browzar", "Camino", "Charon", "Cheshire",
				             "Chimera", "Edge", "ChromePlus", "Chrome", "Classilla", "CometBird", "Comodo_Dragon",
				             "Conkeror", "Crazy Browser", "Cyberdog", "Deepnet Explorer", "DeskBrowse",
				             "Dillo", "Dooble", "Element Browser", "ELinks", "Enigma Browser",
				             "EnigmaFox", "Epiphany", "Firebird", "Firefox", "Fireweb Navigator", "Flock",
				             "Fluid", "Galaxy", "Galeon", "GranParadiso", "GreenBrowser", "Hana", "HotJava",
				             "IBM WebExplorer", "IBrowse", "iCab", "Iceape", "IceCat", "Iceweasel", "iNet Browser",
				             "MSIE", "iRider", "Iron", "K-Meleon", "K-Ninja", "Kapiko", "Kazehakase", "KKman",
				             "KMLite", "Konqueror", "LeechCraft", "Links", "Lobo", "lolifox", "Lorentz",
				             "Lunascape", "Lynx", "Madfox", "Maxthon", "Midori", "Minefield", "myibrow", 
				             "MyIE2", "Namoroka", "Navscape", "NCSA_Mosaic", "NetNewsWire", "NetPositive", 
				             "NetSurf", "OmniWeb", "Opera", "Orca", "Oregano", "osb-browser", "Palemoon", 
				             "Phoenix", "Pogo", "Prism", "QtWeb", "Rekonq", "retawq", "RockMelt", "SeaMonkey", 
				             "Shiira", "Shiretoko", "Sleipnir", "SlimBrowser", "Stainless", "Sundance", 
				             "Sunrise", "surf", "Sylera", "Tencent Traveler", "TenFourFox", "TheWorld", 
				             "Uzbl", "Vimprobable", "Vonkeror", "w3m", "WeltweitimnetzBrowser", "WorldWideWeb", 
				             "Wyzo", "Safari", "Netscape", "Mozilla"};
		for (String browser : browsers)
		    if (request.getHeader("User-Agent").toLowerCase().indexOf(browser.toLowerCase()) != -1)
		        return browser;
		return "Unknown";
	}
	
	public static String browserVersion(PageContext pageContext) {
		String browser = browserName(pageContext);
		if (browser.equals("Unknown"))
			return null;
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String agent = request.getHeader("User-Agent").toLowerCase();
		int s = agent.indexOf(browser.toLowerCase()) + browser.length();
		int e = agent.indexOf(";", s);
		if (e < 0)
			e = agent.length();
		return agent.substring(s, e).replaceAll("[^a-zA-Z0-9.\\s]", "");
	}
}
