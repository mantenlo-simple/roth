package com.roth.servlet.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.base.util.Data.Pad;

import jakarta.servlet.http.HttpServletRequest;

public class Browser {
	/*
	// Engines
	private static final String ENGINE_WEB_KIT = "AppleWebKit";
	private static final String ENGINE_GECKO = "Gecko";
	private static final String ENGINE_IE_MOBILE = "IEMobile";
	private static final String ENGINE_KHTML = "KHTML";
	private static final String ENGINE_PRESTO = "Presto";
	private static final String ENGINE_TRIDENT = "Trident";
	
	private static final String[] ENGINES = { ENGINE_WEB_KIT, ENGINE_GECKO, ENGINE_IE_MOBILE, ENGINE_KHTML, ENGINE_PRESTO, ENGINE_TRIDENT };
	
	// Browsers
	private static final String BROWSER_CHROME = "Chrome";
	private static final String BROWSER_NEWEDG = "Edg";
	private static final String BROWSER_NEW_EDGE = "New Edge";
	private static final String BROWSER_EDGE = "Edge";
	private static final String BROWSER_FIREFOX = "Firefox";
	private static final String BROWSER_MSIE = "MSIE";
	private static final String BROWSER_KONQUERER = "Konqueror";
	private static final String BROWSER_OPERA = "Opera";
	private static final String BROWSER_SAFARI = "Safari";
	
	private static final String[] BROWSERS = { BROWSER_CHROME, BROWSER_NEW_EDGE, BROWSER_EDGE, BROWSER_FIREFOX, BROWSER_MSIE, BROWSER_KONQUERER, BROWSER_OPERA, BROWSER_SAFARI };
	
	private static final String DONT_KNOW = "Don't Know";
	private static final String MOBI = "Mobi";
	*/
	
	String agent;
	String browserName;
	String browserVersion;
	String engineName;
	String engineVersion;
	boolean isMobile;
	String osName;
	String osVersion;
	
	public String getAgent() { return agent; }
	public String getBrowserName() { return browserName; }
	public String getBrowserVersion() { return browserVersion; } 
	public String getEngineName() { return engineName; }
	public String getEngineVersion() { return engineVersion; }
	public boolean isMobile() { return isMobile; }
	public String getOsName() { return osName; }
	public String getOsVersion() { return osVersion; }
	
	public Browser(HttpServletRequest request) {
		this(request.getHeader("User-Agent"));
	}
	
	public Browser(String agent) {
		setAgent(agent);
	}
	
	public static boolean isEngine(String identifier) {
		return Data.in(identifier, new String[] {"AppleWebKit", "Gecko", "Presto", "Trident"});
	}
	
	public static boolean isBrowser(String identifier) {
		return BROWSER_ORDER.get(identifier) != null;
	}
	
	public static boolean isOS(String identifier) {
		return Data.in(identifier, new String[] {"Windows", "Mac OS X", "Linux", "Android", "iPhone", "iPad"});
	}
	
	private static String compString(String source) {
		int pos = source.indexOf("/");
		return pos < 0 ? source : source.substring(0, pos);
	}
	private static final String UNKNOWN = "Unknown";
	private static final Map<String,Integer> BROWSER_ORDER;
	static {
		BROWSER_ORDER = new HashMap<>();
		BROWSER_ORDER.put("Iceweasel", 0);
		BROWSER_ORDER.put("Iceape", 1);
		BROWSER_ORDER.put("Firefox", 2);
		BROWSER_ORDER.put("Brave", 3);
		BROWSER_ORDER.put("Vivaldi", 4);
		BROWSER_ORDER.put("DuckDuckGo", 5);
		BROWSER_ORDER.put("Comodo_Dragon", 6);
		BROWSER_ORDER.put("Edg", 7);
		BROWSER_ORDER.put("Edge", 8);
		BROWSER_ORDER.put("Chromium", 9);
		BROWSER_ORDER.put("Chrome", 10);
		BROWSER_ORDER.put("Safari", 11);
	}
	private static void sortAgentValues(List<String> values) {
		Collections.sort(values, (a,b) -> {
			Integer av = Data.nvl(BROWSER_ORDER.get(compString(a)), BROWSER_ORDER.size());
			Integer bv = Data.nvl(BROWSER_ORDER.get(compString(b)), BROWSER_ORDER.size());
			return av.compareTo(bv);
		});
	}
	private static final String[] OS_LIST = { "Windows", "Android", "Linux", "Mac OS X", "iPhone", "iPad", "CrOS" };
	private static final String getOsName(String source) {
		return source.equals("CrOS") ? "Chrome OS" : source;
	}
	private String setAgent(String agent) {
		this.agent = agent;
		try {
			List<String> parts = splitAgentString(agent);
			getEngine(parts);
			getBrowser(parts);
			getOs(parts.get(1));
			isMobile = agent.contains("Mobi");
		}
		catch (Exception e) {
			Log.logException(e, null);
		}
		return null;
	}
	
	private List<String> splitAgentString(String source) {
		boolean paren = false;
		StringBuilder segment = new StringBuilder("");
		List<String> parts = new ArrayList<>();
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == '(')
				paren = true;
			else if (c == ')')
				paren = false;
			else if (c == ' ' && !paren) {
				parts.add(segment.toString());
				segment = new StringBuilder("");
			}
			else
				segment.append(c);
		}
		parts.add(segment.toString());
		return parts;
	}
	
	private static final String TRIDENT = "Trident";
	
	private void getEngine(List<String> source) {
		if (source.get(1).contains(TRIDENT)) {
			String s = source.get(1);
			int pos = s.indexOf(TRIDENT);
			engineName = TRIDENT;
			engineVersion = s.substring(pos + 8, s.indexOf(" ", pos + 8) - 1);
		}
		else {
			String[] engineParts = source.get(2).split("/");
			engineName = engineParts[0];
			engineVersion = engineParts[1];
		}
	}
	
	private void getBrowser(List<String> source) {
		String[] browserParts;
		if (engineName.equals(TRIDENT)) {
			String s = source.get(1);
			browserParts = new String[] {"MSIE", s.substring(s.indexOf("rv:") + 3)};
		}
		else if (engineName.equals("Presto"))
			browserParts = source.get(0).split("/");
		else {
			List<String> copy = IntStream.range(0, source.size()).filter(i -> i >= 3).mapToObj(source::get).collect(Collectors.toList());
			sortAgentValues(copy);
			String s = copy.get(0);
			browserParts = s.split("/");
		}
		browserName = getBrowserName(browserParts[0]);
		browserVersion = browserParts[1];
	}
	
	private String getBrowserName(String source) {
		return source.equals("Edg") ? "Edge" : source;
	}
	
	private static final Map<String,String> WINDOWS_VERSIONS;
	static {
		WINDOWS_VERSIONS = new HashMap<>();
		WINDOWS_VERSIONS.put("NT 5.1", "XP");
		WINDOWS_VERSIONS.put("NT 5.2", "XP");
		WINDOWS_VERSIONS.put("NT 6.0", "Vista");
		WINDOWS_VERSIONS.put("NT 6.1", "7");
		WINDOWS_VERSIONS.put("NT 6.2", "8");
		WINDOWS_VERSIONS.put("NT 6.3", "8.1");
		WINDOWS_VERSIONS.put("NT 10.0", "10/11");
	}
	
	private String getOsVersion(String os, String source) {
		String[] osParts = source.split(";");
		String[] mcParts = osParts.length > 1 ? osParts[1].split(" ") : new String[] {null};
		return switch (os) {
		case "Windows" -> Data.nvl(WINDOWS_VERSIONS.get(osParts[0].replace("Windows", "").trim()), UNKNOWN);
		case "Mac OS X" -> Data.nvl(mcParts[mcParts.length - 1].replace("_", ".").trim(), UNKNOWN);
		default -> UNKNOWN;
		};
	}
	
	private void getOs(String source) {
		osName = UNKNOWN;
		for (String os : OS_LIST)
			if (source.contains(os))
				osName = getOsName(os);
		osVersion = getOsVersion(osName, source);
	}
	
	public static String formatVersion(String version) {
		String[] parts = version.split("\\.");
		StringBuilder result = new StringBuilder("");
		for (String part : parts)
			result.append(Data.pad(part, '0', 4, Pad.LEFT));
		return Data.pad(result.toString(), '0', 20, Pad.RIGHT);
	}
	
	public String matchIdentifier(String identifier) {
		if (identifier.equalsIgnoreCase("mobile") && isMobile)
			return "mobile";
		/*
		if (identifier.equalsIgnoreCase(browserName))
			return formatVersion(browserVersion);
		if (identifier.equalsIgnoreCase(engineName))
			return formatVersion(engineVersion);
		*/
		if (Data.inIgnoreCase(browserName, identifier.split(",")))
			return formatVersion(browserVersion);
		if (Data.inIgnoreCase(engineName, identifier.split(",")))
			return formatVersion(engineVersion);
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	
	
	private boolean agentContainsSlash(String source) {
		return agent.contains(String.format("%s/", source));
	}
	
	private boolean agentContainsSpace(String source) {
		return agent.contains(String.format("%s ", source));
	}
	
	private void processEngine() {
		engine = agentContainsSlash(ENGINE_WEB_KIT) ? ENGINE_WEB_KIT
			   : agentContainsSlash(ENGINE_GECKO) ? ENGINE_GECKO
			   : agentContainsSpace(ENGINE_IE_MOBILE) ? ENGINE_IE_MOBILE
			   : agentContainsSlash(ENGINE_KHTML) ? ENGINE_KHTML
			   : agentContainsSlash(ENGINE_PRESTO) ? ENGINE_PRESTO
			   : agentContainsSlash(ENGINE_TRIDENT) ? ENGINE_TRIDENT
			   : DONT_KNOW;
	}
	
	private void processBrowserName() {
		browserName = agentContainsSlash(BROWSER_NEWEDG) ? BROWSER_NEW_EDGE
		    		: agentContainsSlash(BROWSER_EDGE) ? BROWSER_EDGE
				    : agentContainsSlash(BROWSER_CHROME) ? BROWSER_CHROME
			        : agentContainsSlash(BROWSER_FIREFOX) ? BROWSER_FIREFOX
			        : agentContainsSpace(BROWSER_MSIE) || ENGINE_TRIDENT.equals(engine) ? BROWSER_MSIE
				    : agentContainsSlash(BROWSER_KONQUERER) ? BROWSER_KONQUERER
				    : agentContainsSlash(BROWSER_OPERA) ? BROWSER_OPERA
				    : agentContainsSlash(BROWSER_SAFARI) ? BROWSER_SAFARI
				    : DONT_KNOW;
	}
	
	public Browser(String agent) {
		this.agent = agent;
		processEngine();
		processBrowserName();
		
		/*
		// Engines
		boolean webkit = agentContainsSlash(ENGINE_WEB_KIT);
		boolean gecko = !webkit && agentContainsSlash(ENGINE_GECKO); 
		boolean iemobile = agentContainsSpace(ENGINE_IE_MOBILE);
		boolean khtml = agentContainsSlash(ENGINE_KHTML);
		boolean presto = agentContainsSlash(ENGINE_PRESTO);
		boolean trident = agentContainsSlash(ENGINE_TRIDENT);
		// Browsers
		boolean chrome = agentContainsSlash(BROWSER_CHROME);
		boolean newedge = agentContainsSlash(BROWSER_NEWEDG);
		boolean edge = agentContainsSlash(BROWSER_EDGE);
		boolean firefox = agentContainsSlash(BROWSER_FIREFOX);
		boolean ie = agentContainsSpace(BROWSER_MSIE) || trident;
		boolean konqueror = agentContainsSlash(BROWSER_KONQUERER);
		boolean opera = agentContainsSlash(BROWSER_OPERA);
		boolean safari = agentContainsSlash(BROWSER_SAFARI);
		
		/*
		engine = (webkit) ? ENGINE_WEB_KIT
			   : (gecko) ? ENGINE_GECKO
			   : (iemobile) ? ENGINE_IE_MOBILE
			   : (khtml) ? ENGINE_KHTML
			   : (presto) ? ENGINE_PRESTO
			   : (trident) ? ENGINE_TRIDENT
			   : DONT_KNOW;
		*/
	//    engineVersion = getVersion(agent, engine);
	
	    /*
	    browserName = (newedge) ? BROWSER_NEW_EDGE
		    		: (edge) ? BROWSER_EDGE
				    : (chrome) ? BROWSER_CHROME
			        : (firefox) ? BROWSER_FIREFOX
			        : (ie) ? BROWSER_MSIE
				    : (konqueror) ? BROWSER_KONQUERER
				    : (opera) ? BROWSER_OPERA
				    : (safari) ? BROWSER_SAFARI
				    : DONT_KNOW;
				    */ /*
	    browserVersion = getVersion(agent, browserName);
	    isMobile = agent.indexOf(MOBI) != -1;
	}
	
	protected float getVersion(String userAgent, String identifier) {
		String id = BROWSER_NEW_EDGE.equals(identifier) ? String.format("%s/", BROWSER_NEWEDG) 
				  : BROWSER_MSIE.equals(identifier) && !agentContainsSpace(BROWSER_MSIE) ? "rv:"
				  : identifier + ((identifier.equals("MSIE") || identifier.equals(ENGINE_IE_MOBILE)) ? " " : "/");
		if (identifier.equals(BROWSER_OPERA)) id = "Version/";
		char end = "rv:".equals(id) || identifier.equals(ENGINE_IE_MOBILE) ? ')' 
				 : (identifier.equals(BROWSER_MSIE) || identifier.equals(ENGINE_TRIDENT)) ? ';' 
				 : ' ';
		int i = userAgent.indexOf(id) + id.length();
		StringBuilder result = new StringBuilder("");
		boolean dot = false;
		
		while ((i < userAgent.length()) && (userAgent.charAt(i) != end)) {
			if (userAgent.charAt(i) == '.') { if (dot) break; else dot = true; }
			result.append(userAgent.charAt(i)); 
			i++;
		}
		
		try { return Float.parseFloat(result.toString()); }
		catch (Exception e) { return 0; }
	}
	
	public boolean isEngine(String identifier) {
		return Data.in(identifier, ENGINES);
	}
	
	public boolean isBrowser(String identifier) {
		return Data.in(identifier, BROWSERS);
	}
	
	public String toString() {
		return "Browser: " + browserName + " (" + browserVersion + ") /  Engine: " + engine + " (" + engineVersion + ") / Mobile: " + isMobile;
	} */
}