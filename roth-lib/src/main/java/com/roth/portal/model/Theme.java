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
package com.roth.portal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.portal.util.Portal;

public class Theme implements Serializable {
	private static final long serialVersionUID = 4187802184618263062L;

	private BigDecimal _themeId;
	private String _themeName;
	private String _customCssUri;
	private String _customJsUri;
	private String _copyrightName;
	private String _customHeaderHtml;
	private String _customFooterHtml;
	private BigDecimal _mobileThemeId;
	private String _passwordUri;
	private String _homeUri;
	private String _homeUriViewType;
    private String _logoutUri;
	
	public BigDecimal getThemeId() { return _themeId; }
	public void setThemeId(BigDecimal themeId) { _themeId = themeId; }
	
	public String getThemeName() { return _themeName; }
	public void setThemeName(String themeName) { _themeName = themeName; }
	
	public String getCustomCssUri() { return _customCssUri; }
	public void setCustomCssUri(String customCssUri) { _customCssUri = customCssUri; }
	
	public String getCustomJsUri() { return _customJsUri; }
	public void setCustomJsUri(String customJsUri) { _customJsUri = customJsUri; }
	
	public String getCopyrightName() { return _copyrightName; }
	public void setCopyrightName(String copyrightName) { _copyrightName = copyrightName; }
	
	public String getCustomHeaderHtml() { return _customHeaderHtml; }
	public void setCustomHeaderHtml(String customHeaderHtml) { _customHeaderHtml = customHeaderHtml; }
	
	public String getCustomFooterHtml() { return _customFooterHtml; }
	public void setCustomFooterHtml(String customFooterHtml) { _customFooterHtml = customFooterHtml; }
	
	public BigDecimal getMobileThemeId() { return _mobileThemeId; }
	public void setMobileThemeId(BigDecimal mobileThemeId) { _mobileThemeId = mobileThemeId; }
	
	public String getPasswordUri() { return _passwordUri; }
	public void setPasswordUri(String passwordUri) { _passwordUri = passwordUri; }
	
	public String getHomeUri() { return _homeUri; }
	public void setHomeUri(String homeUri) { _homeUri = homeUri; }
	
	public String getHomeUriViewType() { return _homeUriViewType; }
	public void setHomeUriViewType(String homeUriViewType) { _homeUriViewType = homeUriViewType; }
	
	public String getLogoutUri() { return _logoutUri; }
	public void setLogoutUri(String logoutUri) { _logoutUri = logoutUri; }
	
	private Link[] _links;
	private boolean _linksLoaded;
	
	public Link[] getLinks() {
		if (!_linksLoaded) {
			try {
				Portal p = new Portal();
				_links = p.getLinks(_themeId); 
			}
			catch (SQLException e) { Log.logException(e, null); }
			_linksLoaded = true;
		}
		
		return _links; 
	}
	
	public void parseStaticTheme(String staticTheme) {
		String[] c = staticTheme.replaceAll("\r\n", "\n").split("\\{BREAK\\}");
		String[] details = c[0].trim().split("\n");
		for (String det : details) {
			String[] ds = det.split("=");
			if (ds[0].equals("customCssUri")) {
				_customCssUri = ds.length > 1 ? ds[1] : null;
				if (_customCssUri != null)
					_customCssUri = _customCssUri.replaceAll("\\|", "\n");
			}
			else if (ds[0].equals("customJsUri")) {
				_customJsUri = ds.length > 1 ? ds[1] : null;
				if (_customJsUri != null)
					_customJsUri = _customJsUri.replaceAll("\\|", "\n");
			}
			else if (ds[0].equals("homeUri"))
				_homeUri = ds.length > 1 ? ds[1] : null;
			else if (ds[0].equals("homeUriViewType"))
				_homeUriViewType = ds.length > 1 ? ds[1] : null;
		}
		
		_copyrightName = c[1].trim();
		
		if (c[2] != null && !Data.isEmpty(c[2].trim())) {
			String customHtml = c[2].trim();
			int p = customHtml.indexOf("<portletcontent/>");
	    	if (p < 0) {
	    		_customHeaderHtml = customHtml;
	    		_customFooterHtml = null;
	    	}
	    	else {
	    		_customHeaderHtml = customHtml.substring(0, p);
	    		_customFooterHtml = customHtml.substring(p + 17);
	    	}
		}
		
		if (c[3] != null && !Data.isEmpty(c[3].trim())) {
			String[] links = c[3].trim().split("\n");
			_links = new Link[links.length];
			for (int i = 0; i < links.length; i++) {
				String[] l = links[i].split("=");
				String[] ld = l[1].split("\\|");
				_links[i] = new Link();
				_links[i].setTitle(l[0]);
				_links[i].setLinkUri(ld[0]);
				_links[i].setTarget("_blank");
			}
		}
		_linksLoaded = true;
	}
}
