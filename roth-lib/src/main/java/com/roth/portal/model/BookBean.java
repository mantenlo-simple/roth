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
import com.roth.jdbc.model.EnhancedBean;
import com.roth.jdbc.model.Insertable;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "book",
           primaryKeyColumns = {"book_id"})
public class BookBean implements Serializable, StateBean, EnhancedBean, Insertable {
    private static final long serialVersionUID = 1L;

    private Long bookId;
    private Long parentBookId;
    private String bookName;
    private String bookTitle;
    private String description;
    private String createdBy;
    private LocalDateTime createdDts;
    private String updatedBy;
    private LocalDateTime updatedDts;
    private String lineage;
    private Long sequence;

    private String parentName;

    public BookBean() {
    	sequence = 0L;
    }
    
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getParentBookId() { return parentBookId; }
    @JdbcLookup(table = "book", key = "book_id", value = "book_name", valueField = "parentName")
    public void setParentBookId(Long parentBookId) { this.parentBookId = parentBookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

	public LocalDateTime getCreatedDts() { return createdDts; }
	public void setCreatedDts(LocalDateTime createdDts) { this.createdDts = createdDts; }

	public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    public String getLineage() { return lineage; }
    public void setLineage(String lineage) { this.lineage = lineage; }

    public Long getSequence() { return sequence; }
    public void setSequence(Long sequence) { this.sequence = sequence; }

    public String getParentName() { return parentName; }
	public void setParentName(String parentName) { this.parentName = parentName; }
	
	@Override
	public void prepare() {
		bookId = -1L;
	}
	
	@Override
    public boolean isNew() {
    	boolean result = bookId == null; 
    	if (result) { bookId = Long.valueOf(-1); lineage = ""; }
    	return result; 
    }
	
	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof BookBean otherBook)
			return Data.eq(bookId, otherBook.bookId) &&
				   Data.eq(bookName, otherBook.bookName) &&
				   Data.eq(bookTitle, otherBook.bookTitle) &&
				   Data.eq(parentBookId, otherBook.parentBookId) &&
				   Data.eq(description, otherBook.description) &&
				  // Data.eq(lineage, otherBook.lineage) &&
				   Data.eq(sequence, otherBook.sequence) &&
				   Data.eq(updatedBy, otherBook.updatedBy) &&
				   Data.eq(updatedDts, otherBook.updatedDts);
		else 
			return false;
	}
	
	@Override
	public int hashCode() {
		return Data.complexHashCode(bookId, bookName, bookTitle, parentBookId, description, lineage, sequence, updatedBy, updatedDts);
	}
	
	@Override
	public EnhancedBean copy() {
		BookBean dest = new BookBean();
    	dest.bookId = bookId;
    	dest.bookName = bookName;
    	dest.bookTitle = bookTitle;
    	dest.parentBookId = parentBookId;
    	dest.description = description;
    	dest.lineage = lineage;
    	dest.sequence = sequence;
    	dest.updatedBy = updatedBy;
    	dest.updatedDts = updatedDts;
    	return dest;
    }

	@Override
	public void merge(EnhancedBean source) {
		if (source != null && source instanceof BookBean sourceBook) {
			bookName = sourceBook.bookName;
			bookTitle = sourceBook.bookTitle;
			parentBookId = sourceBook.parentBookId;
			description = sourceBook.description;
			lineage = sourceBook.lineage;
			sequence = sourceBook.sequence;
			updatedBy = sourceBook.updatedBy;
			updatedDts = sourceBook.updatedDts;
		}
	}
}