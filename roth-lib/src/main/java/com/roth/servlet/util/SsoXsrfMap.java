package com.roth.servlet.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.Cookie;

public class SsoXsrfMap {
	private SsoXsrfMap() {}
	
	private static class SsoSession {
		Cookie cookie;
		LocalDateTime lastAccess;
		
		SsoSession(Cookie cookie) {
			this.cookie = cookie;
			this.lastAccess = LocalDateTime.now();
		}
	}
	
	private static Map<String,SsoSession> map;
	
	static {
		map = new ConcurrentHashMap<>();
	}
	
	public static Cookie getCookie(String ssoSessionId) {
		SsoSession session = ssoSessionId == null ? null : map.get(ssoSessionId);
		if (session != null)
			session.lastAccess = LocalDateTime.now();
		cleanSessions();
		return session == null ? null : session.cookie;
	}
	
	public static void setCookie(String ssoSessionId, Cookie cookie) {
		if (ssoSessionId == null || cookie == null)
			return;
		map.put(ssoSessionId, new SsoSession(cookie));
		cleanSessions();
	}
	
	private static void cleanSessions() {
		for (String key : map.keySet()) {
			if (Duration.between(map.get(key).lastAccess, LocalDateTime.now()).getSeconds() > 3600)
				map.remove(key);
		}
	}
}
