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
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "page",
	       primaryKeyColumns = {"book_id", "portlet_id"})
public class PageBean implements Serializable, StateBean {
	private static final long serialVersionUID = -2210728133010715092L;

	private Long bookId;
	private Long portletId;
	private String pageTitle;
	private Long sequence;
	private String createdBy;
	private LocalDateTime createdDts;
	private String updatedBy;
	private LocalDateTime updatedDts;
	
	private String bookName;
	private String portletName;
	
	public Long getBookId() { return bookId; }
	public void setBookId(Long bookId) { this.bookId = bookId; }

	public Long getPortletId() { return portletId; }
	public void setPortletId(Long portletId) { this.portletId = portletId; }

	public String getPageTitle() { return pageTitle; }
	public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

	public Long getSequence() { return sequence; }
	public void setSequence(Long sequence) { this.sequence = sequence; }

	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	public LocalDateTime getCreatedDts() { return createdDts; }
	public void setCreatedDts(LocalDateTime createdDts) { this.createdDts = createdDts; }
	
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

	public LocalDateTime getUpdatedDts() { return updatedDts; }
	public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

	public String getBookName() { return bookName; }
	public void setBookName(String bookName) { this.bookName = bookName; }
	
	public String getPortletName() { return portletName; }
	public void setPortletName(String portletName) { this.portletName = portletName; }
	
	@Override
	public boolean isNew() { boolean result = updatedDts == null; updatedDts = LocalDateTime.now(); return result; }

}
