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

public class BookContentBean implements Serializable {
	private static final long serialVersionUID = -4858209748453972590L;

	private Long bookId;
	private String tableName;
	private Long tableId;
	private String name;
	private String title;
	private Long sequence;
	private String updatedBy;
    private LocalDateTime updatedDts;
	
	public Long getBookId() { return bookId; }
	public void setBookId(Long bookId) { this.bookId = bookId; }
	
	public String getTableName() { return tableName; }
	public void setTableName(String tableName) { this.tableName = tableName; }
	
	public Long getTableId() { return tableId; }
	public void setTableId(Long tableId) { this.tableId = tableId; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	
	public Long getSequence() { return sequence; }
	public void setSequence(Long sequence) { this.sequence = sequence; }
	
	public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }
}
