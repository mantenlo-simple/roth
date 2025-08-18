package com.roth.portal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "news",
           primaryKeyColumns = {"news_id"})
@PermissiveBinding()
public class NewsBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private String content;
    private Long domainId;
    private Long groupId;
    private String headline;
    private String languageCode;
    private Long newsId;
    private LocalDateTime postDts;
    private String sticky;
    private String updatedBy;
    private LocalDateTime updatedDts;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    
    public String getLanguageCode() { return languageCode; }
	public void setLanguageCode(String languageCode) { this.languageCode = languageCode == null ? null : languageCode.toLowerCase(); }
	
	public Long getNewsId() { return newsId; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }

    public LocalDateTime getPostDts() { return postDts; }
	public void setPostDts(LocalDateTime postDts) { this.postDts = postDts; }
	
	public String getSticky() { return sticky; }
	public void setSticky(String sticky) { this.sticky = sticky; }
	
	public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    @Override
    public boolean isNew() {
    	setUpdatedDts(LocalDateTime.now()); 
    	boolean result = newsId == null; 
    	if (result) {
    		newsId = Long.valueOf(-1);
    		postDts = updatedDts;
    	}
    	return result;  
    }
}