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
import com.roth.portal.util.Portal;

public class Desktop implements Serializable {
	private static final long serialVersionUID = 6795377816801883796L;

	private BigDecimal _desktopId;
	private String _desktopTitle;
	private BigDecimal _bookId;
	private MenuItem _book;
	//private Link[] _links;
	//private String _passwordUri;
	//private String _homeUri;
	//private String _logoutUri;
	private BigDecimal _themeId;
	private Theme _theme;	
	
	private boolean _bookLoaded;
	//private boolean _linksLoaded;
	private boolean _themeLoaded;
	
	private String _hostname;
	public void setHostname(String hostname) { _hostname = hostname; }
	
	private boolean _mobile = false;
	public void setMobile(boolean mobile) { _mobile = mobile; }
	public boolean getMobile() { return _mobile; }
	
	public BigDecimal getDesktopId() { return _desktopId; }
	public void setDesktopId(BigDecimal desktopId) { _desktopId = desktopId; }
	
	public String getDesktopTitle() { return _desktopTitle; }
	public void setDesktopTitle(String desktopTitle) { _desktopTitle = desktopTitle; }
	
	public BigDecimal getBookId() { return _bookId; }
	public void setBookId(BigDecimal bookId) { _bookId = bookId; }
	
	public MenuItem getBook() {
		if (_bookId == null) return null;
		
		if (!_bookLoaded) {
			try {
				Portal p = new Portal();
				_book = p.getBook(_bookId); 
			}
			catch (SQLException e) { Log.logException(e, null); }
			_bookLoaded = true;
		}
		
		return _book; 
	}
	
	//public Link[] getLinks() {
	//	if (!_linksLoaded) {
	//		Portal p = new Portal();
	//		try { _links = p.getLinks(_desktopId); }
	//		catch (SQLException e) { Logger.logException(this.getClass().getCanonicalName() + ".getLinks", e); }
	//		_linksLoaded = true;
	//	}
	//	
	//	return _links; 
	//}
	
	//public String getPasswordUri() {  return (_passwordUri != null) ? _passwordUri : getTheme().getPasswordUri(); }
	//public void setPasswordUri(String passwordUri) { _passwordUri = passwordUri; }
	
	//public String getHomeUri() {  return (_homeUri != null) ? _homeUri : getTheme().getHomeUri(); }
	//public void setHomeUri(String homeUri) { _homeUri = homeUri; }
	
	//public String getLogoutUri() { return (_logoutUri != null) ? _logoutUri : getTheme().getLogoutUri(); }
	//public void setLogoutUri(String logoutUri) { _logoutUri = logoutUri; }
	
	public BigDecimal getThemeId() { return _themeId; }
	public void setThemeId(BigDecimal themeId) { _themeId = themeId; _themeLoaded = false; }
	
	public Theme getTheme() {
		if (!_themeLoaded) {
			try {
				Portal p = new Portal();
				if (_themeId != null) _theme = p.getTheme(_themeId, _hostname, _mobile); 
			}
			catch (SQLException e) { Log.logException(e, null); }
			if (_theme == null) _theme = new Theme();
			_themeLoaded = true;
		}
		
		return _theme;
	}
}
