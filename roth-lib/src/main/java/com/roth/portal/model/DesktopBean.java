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
import java.time.LocalDateTime;

import com.roth.base.util.Data;
import com.roth.jdbc.annotation.JdbcLookup;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.EnhancedBean;
import com.roth.jdbc.model.Insertable;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "desktop",
           primaryKeyColumns = {"desktop_id"})
@PermissiveBinding
public class DesktopBean implements Serializable, StateBean, EnhancedBean, Insertable {
    private static final long serialVersionUID = 1L;

    private Long desktopId;
    private String desktopName;
    private String desktopTitle;
    private String desktopUri;
    private String desktopIcon;
    private String description;
    private Long bookId;
    private String updatedBy;
    private LocalDateTime updatedDts;
    private String passwordUri;
    private String homeUri;
    private String logoutUri;
    private Long themeId;

    private String bookName;
    private String themeName;

    public Long getDesktopId() { return desktopId; }
    public void setDesktopId(Long desktopId) { this.desktopId = desktopId; }

    public String getDesktopName() { return desktopName; }
    public void setDesktopName(String desktopName) { this.desktopName = desktopName; }

    public String getDesktopTitle() { return desktopTitle; }
    public void setDesktopTitle(String desktopTitle) { this.desktopTitle = desktopTitle; }

    public String getDesktopUri() { return desktopUri; }
    public void setDesktopUri(String desktopUri) { this.desktopUri = desktopUri; }
    
    public String getDesktopIcon() { return desktopIcon; }
    public void setDesktopIcon(String desktopIcon) { this.desktopIcon = desktopIcon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getBookId() { return bookId; }
    @JdbcLookup(table = "book", key = "book_id", value = "book_name", valueField = "bookName")
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    public String getPasswordUri() { return passwordUri; }
    public void setPasswordUri(String passwordUri) { this.passwordUri = passwordUri; }

    public String getHomeUri() { return homeUri; }
    public void setHomeUri(String homeUri) { this.homeUri = homeUri; }

    public String getLogoutUri() { return logoutUri; }
    public void setLogoutUri(String logoutUri) { this.logoutUri = logoutUri; }

    public Long getThemeId() { return themeId; }
    @JdbcLookup(table = "theme", key = "theme_id", value = "theme_name", valueField = "themeName")
    public void setThemeId(Long themeId) { this.themeId = themeId; }

    public String getBookName() { return bookName; }
	public void setBookName(String bookName) { this.bookName = bookName; }
	
	public String getThemeName() { return themeName; }
	public void setThemeName(String themeName) { this.themeName = themeName; }
	
	@Override
	public void prepare() {
		desktopId = -1L;
	}
	
	@Override
    public boolean isNew() { 
    	boolean result = desktopId == null; 
    	if (result) desktopId = Long.valueOf(-1);
    	return result; 
    }
	
	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof DesktopBean otherDesktop)
			return Data.eq(desktopId, otherDesktop.desktopId) &&
				   Data.eq(desktopName, otherDesktop.desktopName) &&
				   Data.eq(desktopTitle, otherDesktop.desktopTitle) &&
				   Data.eq(desktopUri, otherDesktop.desktopUri) &&
				   Data.eq(desktopIcon, otherDesktop.desktopIcon) &&
				   Data.eq(description, otherDesktop.description) &&
				   Data.eq(bookId, otherDesktop.bookId) &&
				   Data.eq(passwordUri, otherDesktop.passwordUri) &&
				   Data.eq(homeUri, otherDesktop.homeUri) &&
				   Data.eq(logoutUri, otherDesktop.logoutUri) &&
				   Data.eq(themeId, otherDesktop.themeId) &&
				   Data.eq(updatedBy, otherDesktop.updatedBy) &&
				   Data.eq(updatedDts, otherDesktop.updatedDts);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Data.complexHashCode(desktopId, desktopName, desktopTitle, desktopUri, desktopIcon, description, bookId, passwordUri, homeUri, logoutUri, themeId, updatedBy, updatedDts);
	}
	
	@Override
	public EnhancedBean copy() {
		DesktopBean dest = new DesktopBean();
    	dest.desktopId = desktopId;
    	dest.desktopName = desktopName;
    	dest.desktopTitle = desktopTitle;
    	dest.desktopUri = desktopUri;
    	dest.desktopIcon = desktopIcon;
    	dest.description = description;
    	dest.bookId = bookId;
    	dest.passwordUri = passwordUri;
    	dest.homeUri = homeUri;
    	dest.logoutUri = logoutUri;
    	dest.themeId = themeId;
    	dest.updatedBy = updatedBy;
    	dest.updatedDts = updatedDts;
    	return dest;
    }

	@Override
	public void merge(EnhancedBean source) {
		if (source instanceof DesktopBean sourceDesktop) {
			desktopName = sourceDesktop.desktopName;
			desktopTitle = sourceDesktop.desktopTitle;
			desktopUri = sourceDesktop.desktopUri;
			desktopIcon = sourceDesktop.desktopIcon;
			description = sourceDesktop.description;
			bookId = sourceDesktop.bookId;
			passwordUri = sourceDesktop.passwordUri;
			homeUri = sourceDesktop.homeUri;
			logoutUri = sourceDesktop.logoutUri;
			themeId = sourceDesktop.themeId;
			updatedBy = sourceDesktop.updatedBy;
			updatedDts = sourceDesktop.updatedDts;
		}
	}
}