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
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.roth.base.annotation.Ignore;
import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.StateBean;
import com.roth.portal.db.PortalUtil;

@JdbcTable(name = "theme",
           primaryKeyColumns = {"theme_id"})
public class ThemeBean implements Serializable, StateBean {
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
	
	List<ThemeLinkBean> links;
	List<ThemeOverrideBean> overrides;

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

	public List<ThemeLinkBean> getLinks() { return links; }
	public void setLinks(List<ThemeLinkBean> links) { this.links = links; }
	
	public List<ThemeOverrideBean> getOverrides() { return overrides; }
	public void setOverrides(List<ThemeOverrideBean> overrides) { this.overrides = overrides; }
	
    @Override
    public boolean isNew() { 
    	boolean result = themeId == null; 
    	if (result) themeId = Long.valueOf(-1);
    	updatedDts = LocalDateTime.now();
    	return result;
    }
    
    @Ignore
    public String getStaticTheme(ArrayList<ThemeLinkBean> links, ArrayList<ThemeOverrideBean> overrides) {
    	String _links = "";
    	if (links != null)
	    	for (ThemeLinkBean l : links) {
	    		_links += (_links.isEmpty() ? "" : "\n") +
	    				  l.getLinkTitle() + "=" + l.getLinkUri() + "|" + l.getTarget() + "|" + 
	    	              l.getSequence() + "|" + l.getUpdatedBy() + "|" + Data.dtsToStr(l.getUpdatedDts());
	    	}
    	String _overrides = "";
    	try {
    		if (overrides != null)
		    	for (ThemeOverrideBean o : overrides) {
		    		_overrides += (_overrides.isEmpty() ? "" : "\n") +
		    				      o.getHostName() + "=" + new PortalUtil().getThemeName(o.getAltThemeId()) + "|" + 
		    				      o.getUpdatedBy() + "|" + Data.dtsToStr(o.getUpdatedDts());
		    	}
    	}
    	catch (SQLException e) { Log.logException(e, null); }
    	return "themeId=" + Data.nvl(Data.longToStr(themeId)) + "\n" +
               "themeName=" + Data.nvl(themeName) + "\n" +
               "mobileThemeId=" + Data.nvl(Data.longToStr(mobileThemeId)) + "\n" +
               "customCssUri=" + Data.join(Data.splitLF(Data.nvl(customCssUri)), "|") + "\n" +
               "customJsUri=" + Data.join(Data.splitLF(Data.nvl(customJsUri)), "|") + "\n" +
               "homeUri=" + Data.nvl(homeUri) + "\n" +
               "homeUriViewType=" + Data.nvl(homeUriViewType) + "\n" +
               "updatedBy=" + Data.nvl(updatedBy) + "\n" +
               "updatedDts=" + Data.nvl(Data.dtsToStr(updatedDts)) + "\n{BREAK}\n" +
               Data.nvl(copyrightName) + "\n{BREAK}\n" +
               Data.nvl(getCustomHtml()) + "\n{BREAK}\n" +
               _links + "\n{BREAK}\n" +
               _overrides;
    }
    
    public void setStaticTheme(String staticTheme, ArrayList<ThemeLinkBean> links, ArrayList<ThemeOverrideBean> overrides) throws Exception {
    	if (links == null)
    		throw new Exception("The links parameter cannot be null.");
    	if (overrides == null)
    		throw new Exception("The overrides parameter cannot be null.");
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
					override.setAltThemeId(new PortalUtil().getThemeId(Data.get(odt, 0)));
					override.setUpdatedBy(Data.get(odt, 1));
					override.setUpdatedDts(Data.strToLocalDateTime(Data.get(odt, 2)));
					overrides.add(override);
				}
    	}
    	catch (Exception e) { Log.logException(e, null); }
    }
}