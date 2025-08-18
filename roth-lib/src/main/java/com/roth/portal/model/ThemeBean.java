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
import java.util.List;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.EnhancedBean;
import com.roth.jdbc.model.Insertable;
import com.roth.jdbc.model.StateBean;
import com.roth.jdbc.util.TableUtil;

@JdbcTable(name = "theme",
           primaryKeyColumns = {"theme_id"})
public class ThemeBean implements Serializable, StateBean, EnhancedBean, Insertable {
    private static final long serialVersionUID = 1L;

    private Long themeId;
    private String themeName;
    private String customCssUri;
    private String customJsUri;
    private String customHeaderHtml;
    private String customFooterHtml;
    private String copyrightName;
    private Long mobileThemeId;
    private String passwordUri;
    private String homeUri;
    private String homeUriViewType;
    private String logoutUri;
    private String createdBy;
	private LocalDateTime createdDts;
    private String updatedBy;
	private LocalDateTime updatedDts;

    public Long getThemeId() { return themeId; }
    public void setThemeId(Long themeId) { this.themeId = themeId; }

    public String getThemeName() { return themeName; }
    public void setThemeName(String themeName) { this.themeName = themeName; }

    public String getCustomCssUri() { return customCssUri; }
    public void setCustomCssUri(String customCssUri) { this.customCssUri = customCssUri; }
    
    public String getCustomJsUri() { return customJsUri; }
    public void setCustomJsUri(String customJsUri) { this.customJsUri = customJsUri; }

    public String getCustomHeaderHtml() { return customHeaderHtml; }
    public void setCustomHeaderHtml(String customHeaderHtml) { this.customHeaderHtml = customHeaderHtml; }

    public String getCustomFooterHtml() { return customFooterHtml; }
    public void setCustomFooterHtml(String customFooterHtml) { this.customFooterHtml = customFooterHtml; }

    public String getCustomHtml() {
    	if ((customHeaderHtml == null) && (customFooterHtml == null))
    		return null;
    	return Data.nvl(customHeaderHtml) + "<portletcontent/>" + Data.nvl(customFooterHtml); 
    }
    public void setCustomHtml(String customHtml) {
    	if (customHtml == null) {
    		customHeaderHtml = null;
    		customFooterHtml = null;
    		return;
    	}
    	int p = customHtml.indexOf("<portletcontent/>");
    	if (p < 0) {
    		customHeaderHtml = customHtml;
    		customFooterHtml = null;
    	}
    	else {
    		customHeaderHtml = customHtml.substring(0, p);
    		customFooterHtml = customHtml.substring(p + 17);
    	}
    }
    
    public String getCopyrightName() { return copyrightName; }
    public void setCopyrightName(String copyrightName) { this.copyrightName = copyrightName; }
    
    public Long getMobileThemeId() { return mobileThemeId; }
    public void setMobileThemeId(Long mobileThemeId) { this.mobileThemeId = mobileThemeId; }
    
    public String getPasswordUri() { return passwordUri; }
	public void setPasswordUri(String passwordUri) { this.passwordUri = passwordUri; }
	
	public String getHomeUri() { return homeUri; }
	public void setHomeUri(String homeUri) { this.homeUri = homeUri; }
	
	public String getHomeUriViewType() { return homeUriViewType; }
	public void setHomeUriViewType(String homeUriViewType) { this.homeUriViewType = homeUriViewType; }
	
	public String getLogoutUri() { return logoutUri; }
	public void setLogoutUri(String logoutUri) { this.logoutUri = logoutUri; }
	
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	public LocalDateTime getCreatedDts() { return createdDts; }
	public void setCreatedDts(LocalDateTime createdDts) { this.createdDts = createdDts; }
	
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
	 
	public LocalDateTime getUpdatedDts() { return updatedDts; }
	public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

	@Override
	public void prepare() {
		themeId = -1L;
	}
	
    @Override
    public boolean isNew() { 
    	boolean result = themeId == null; 
    	if (result) themeId = Long.valueOf(-1);
    	return result;
    }
    
    @Override
	public boolean equals(Object other) {
		if (other != null && other instanceof ThemeBean otherTheme)
			return Data.eq(themeId, otherTheme.themeId) &&
				   Data.eq(themeName, otherTheme.themeName) &&
				   Data.eq(customCssUri, otherTheme.customCssUri) &&
				   Data.eq(customJsUri, otherTheme.customJsUri) &&
				   Data.eq(customHeaderHtml, otherTheme.customHeaderHtml) &&
				   Data.eq(customFooterHtml, otherTheme.customFooterHtml) &&
				   Data.eq(copyrightName, otherTheme.copyrightName) &&
				   Data.eq(mobileThemeId, otherTheme.mobileThemeId) &&
				   Data.eq(passwordUri, otherTheme.passwordUri) &&
				   Data.eq(homeUri, otherTheme.homeUri) &&
				   Data.eq(homeUriViewType, otherTheme.homeUriViewType) &&
				   Data.eq(logoutUri, otherTheme.logoutUri) &&
				   Data.eq(updatedBy, otherTheme.updatedBy) &&
				   Data.eq(updatedDts, otherTheme.updatedDts);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Data.complexHashCode(themeId, themeName, customCssUri, customJsUri, customHeaderHtml, customFooterHtml, copyrightName, 
									mobileThemeId, passwordUri, homeUri, homeUriViewType, logoutUri, updatedBy, updatedDts);
	}
	

	@Override
	public EnhancedBean copy() {
		ThemeBean dest = new ThemeBean();
    	dest.themeId = themeId;
    	dest.themeName = themeName;
    	dest.customCssUri = customCssUri;
    	dest.customJsUri = customJsUri;
    	dest.customHeaderHtml = customHeaderHtml;
    	dest.customFooterHtml = customFooterHtml;
    	dest.copyrightName = copyrightName;
    	dest.mobileThemeId = mobileThemeId;
    	dest.passwordUri = passwordUri;
    	dest.homeUri = homeUri;
    	dest.homeUriViewType = homeUriViewType;
    	dest.logoutUri = logoutUri;
    	dest.updatedBy = updatedBy;
    	dest.updatedDts = updatedDts;
    	return dest;
    }

	@Override
	public void merge(EnhancedBean source) {
		if (source instanceof ThemeBean sourceTheme) {
			themeName = sourceTheme.themeName;
			customCssUri = sourceTheme.customCssUri;
			customJsUri = sourceTheme.customJsUri;
			customHeaderHtml = sourceTheme.customHeaderHtml;
			customFooterHtml = sourceTheme.customFooterHtml;
			copyrightName = sourceTheme.copyrightName;
			mobileThemeId = sourceTheme.mobileThemeId;
			passwordUri = sourceTheme.passwordUri;
			homeUri = sourceTheme.homeUri;
			homeUriViewType = sourceTheme.homeUriViewType;
			logoutUri = sourceTheme.logoutUri;
			updatedBy = sourceTheme.updatedBy;
			updatedDts = sourceTheme.updatedDts;
		}
	}
    
    public void setStaticTheme(String staticTheme, List<ThemeLinkBean> links, List<ThemeOverrideBean> overrides, TableUtil util) throws Exception {
    	if (links == null)
    		throw new IllegalArgumentException("The links parameter cannot be null.");
    	if (overrides == null)
    		throw new IllegalArgumentException("The overrides parameter cannot be null.");
    	try {
    		String[] theme = staticTheme.split("\\{BREAK\\}");
    		String[] detail = Data.splitLF(Data.get(theme, 0));
    		if (detail != null) {
    			for (String d : detail) {
    				String[] dkv = d.split("=");
    				switch (dkv[0]) {
    				    case "themeId": themeId = Data.strToLong(Data.get(dkv, 1)); break;
    				    case "themeName": themeName = Data.get(dkv, 1); break;
    				    case "mobileThemeId": mobileThemeId = Data.strToLong(Data.get(dkv, 1)); break;
    				    case "customCssUri": customCssUri = Data.get(dkv, 1); break;
    				    case "customJsUri": customJsUri = Data.get(dkv, 1); break;
    				    case "homeUri": homeUri = Data.get(dkv, 1); break;
    				    case "homeUriViewType": homeUriViewType = Data.get(dkv,  1); break;
    				    case "updatedBy": updatedBy = Data.get(dkv, 1); break;
    				    case "updatedDts": updatedDts = Data.strToLocalDateTime(Data.get(dkv, 1)); break;
    				    default: { /* Do nothing; this is to satisfy lint. */ }
    				}
    			}
    		}
    		if (customCssUri != null)
    			customCssUri = customCssUri.replaceAll("\\|", "\n");
    		if (customJsUri != null)
    			customJsUri = customJsUri.replaceAll("\\|", "\n");
   			copyrightName = Data.get(theme, 1);
   			setCustomHtml(Data.get(theme, 2));
			String[] l = Data.splitLF(Data.get(theme, 3));
			if (l != null)
				for (String lnk : l) {
					String[] lkv = lnk.split("=");
					String[] ldt = Data.nvl(Data.get(lkv, 1)).split("\\|");
					ThemeLinkBean link = new ThemeLinkBean();
					link.setThemeId(themeId);
					link.setLinkTitle(Data.get(lkv, 0));
					link.setLinkUri(Data.get(ldt, 0));
					link.setTarget(Data.get(ldt, 1));
					link.setSequence(Data.strToLong(Data.get(ldt, 2)));
					link.setUpdatedBy(Data.get(ldt, 3));
					link.setUpdatedDts(Data.strToLocalDateTime(Data.get(ldt, 4)));
					links.add(link);
				}
			String[] o = Data.splitLF(Data.get(theme, 4));
			if (o != null)
				for (String ovr : o) {
					String[] okv = ovr.split("=");
					String[] odt = Data.nvl(Data.get(okv, 1)).split("\\|");
					ThemeOverrideBean override = new ThemeOverrideBean();
					override.setThemeId(themeId);
					override.setHostName(Data.get(okv, 0));
					ThemeBean altTheme = util.get(ThemeBean.class, util.applyParameters("theme_name = {1}", Data.get(odt, 0)));
					override.setAltThemeId(altTheme.getThemeId());
					override.setUpdatedBy(Data.get(odt, 1));
					override.setUpdatedDts(Data.strToLocalDateTime(Data.get(odt, 2)));
					overrides.add(override);
				}
    	}
    	catch (Exception e) { Log.logException(e, null); }
    }
}