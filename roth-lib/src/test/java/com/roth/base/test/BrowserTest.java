package com.roth.base.test;

public class BrowserTest {
	
	private static final String[] AGENT_STRINGS = {
		"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0",     // Firefox
		"Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0",  // Firefox
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36",  // Chrome
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36 OPR/38.0.2220.41", // Opera (new)
		"Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.00", // Opera (old)
		"Opera/9.60 (Windows NT 6.0; U; en) Presto/2.1.1", // Opera (old)
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59", // Edge New
		"Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1", // Safari
		"Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)", // IE Mobile
		"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)", // Google Bot
		"Mozilla/5.0 (compatible; YandexAccessibilityBot/3.0; +http://yandex.com/bots)", // Yandex Accebility Bot
		
		
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Brave Chrome/89.0.4389.72 Safari/537.36", // Brave
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.84 Safari/537.36", // Chrome
		"Mozilla/5.0 (X11; Linux armv7l) AppleWebKit/537.36 (KHTML, like Gecko) Raspbian Chromium/74.0.3729.157 Chrome/74.0.3729.157 Safari/537.36", // Chromium
		"Mozilla/5.0 (Windows NT 6.2) AppleWebKit/535.7 (KHTML, like Gecko) Comodo_Dragon/16.1.1.0 Chrome/16.0.912.63 Safari/535.7", // Comodo_Dragon
		"Mozilla/5.0 (iPhone; CPU iPhone OS 15_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.3 Mobile/15E148 DuckDuckGo/7 Safari/605.1.15", // DuckDuckGo
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:101.0) Gecko/20100101 Firefox/101.0", // Firefox
		"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko", // IE 11
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.19582", // Edge New
		"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.13) Gecko/20100916 Iceape/2.0.8", // Iceape
		"Mozilla/5.0 (X11; Linux x86_64; rv:17.0) Gecko/20121202 Firefox/17.0 Iceweasel/17.0.1", // Iceweasel
		"Opera/9.80 (X11; Linux i686; Ubuntu/14.10) Presto/2.12.388 Version/12.16.2", // Opera Old
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A", // Safari
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.99 Safari/537.36 Vivaldi/2.9.1705.41", // Vivaldi
	};
	
	// Pattern:
	//  Crap/5.0 (OS Info) Engine/Version [(Engine Info)] Browser/Version [Browser/Version] [Browser/Version]
}
