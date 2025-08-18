package com.roth.servlet.util;

import java.io.Serializable;

public class GridOptionsEntry implements Serializable {
	private static final long serialVersionUID = -3935107951009810198L;
	
	private boolean columnMoving;
	private boolean columnSizing;
	private boolean paging;
	private int pageSize;
	private boolean rowSelect;
	private boolean searching;
	private boolean sorting;
	private boolean wrapping;
	
	public boolean isColumnMoving() { return columnMoving; }
	public void setColumnMoving(boolean columnMoving) { this.columnMoving = columnMoving; }
	
	public boolean isColumnSizing() { return columnSizing; }
	public void setColumnSizing(boolean columnSizing) { this.columnSizing = columnSizing; }
	
	public boolean isPaging() { return paging; }
	public void setPaging(boolean paging) { this.paging = paging; }
	
	public int getPageSize() { return pageSize; }
	public void setPageSize(int pageSize) { this.pageSize = pageSize; }
	
	public boolean isRowSelect() { return rowSelect; }
	public void setRowSelect(boolean rowSelect) { this.rowSelect = rowSelect; }
	
	public boolean isSearching() { return searching; }
	public void setSearching(boolean searching) { this.searching = searching; }
	
	public boolean isSorting() { return sorting; }
	public void setSorting(boolean sorting) { this.sorting = sorting; }
	
	public boolean isWrapping() { return wrapping; }
	public void setWrapping(boolean wrapping) { this.wrapping = wrapping; }
}