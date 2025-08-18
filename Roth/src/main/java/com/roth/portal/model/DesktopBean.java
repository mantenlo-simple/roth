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

import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "desktop",
           primaryKeyColumns = {"desktop_id"})
@PermissiveBinding
public class DesktopBean implements Serializable, StateBean {
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
    public void setThemeId(Long themeId) { this.themeId = themeId; }

    public String getBookName() { return bookName; }
	public void setBookName(String bookName) { this.bookName = bookName; }
	
	public String getThemeName() { return themeName; }
	public void setThemeName(String themeName) { this.themeName = themeName; }
	
	@Override
    public boolean isNew() { 
    	boolean result = desktopId == null; 
    	if (result) desktopId = Long.valueOf(-1);
    	updatedDts = LocalDateTime.now();
    	return result; 
    }
}