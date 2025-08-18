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
package com.roth.tags.html.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import com.roth.base.util.AbstractSortComparator;
import com.roth.base.util.Data;
import com.roth.base.log.Log;

public class DataGridParams implements Serializable {
	private static final long serialVersionUID = 3832347236212353852L;

	// Paging
	private boolean defaultPaging;
	private boolean paging;
	private int recordCount;
	private int pageCount;
	private int pageIndex;
	private int pageSize;
	
	// Sorting
	private String sortColumn;
	private int sortOrder; // -1 (desc) / 1 (asc)
	private boolean doSort;
	private int hashCode;
	
	// Searching
	private String searchColumn;
	private String searchValue;
	private String searchFlag;
	private int searchRowIndex;
	
	// Visible Columns
	private String[] visibleColumns;
	private String[] columnOrder;
	private String[] columnSizes;
	
	// Metrics
	private Integer height;
	private Integer width;
	
	// User Properties
	private int save;
	private boolean doSave;
	private boolean doDelete;

	// Data Grid Column Data
	private ArrayList<ColumnData> columnData;
	
	public DataGridParams() {
		pageSize = 20;
		pageIndex = 1;
	}
	
	protected void reset() {
		save = 0;
		height = null;
		width = null;
		paging = defaultPaging;
		visibleColumns = null;
		columnOrder = null;
		columnSizes = null;
		searchColumn = null;
		searchValue = null;
		searchFlag = null;
		searchRowIndex = 0;
		sortColumn = null;
	}

	public void parseParams(String params) throws Exception {
		if (Data.isEmpty(params)) return;
		int priorSave = save;
		String[] ps = params.split("&");
		for (int i = 0; i < ps.length; i++) {
			String[] p = ps[i].split("=");
			String k = p[0];
			String v = p[1];
			// Stored Parameters
			if (k.equals("height")) height = Integer.valueOf(v);
			else if (k.equals("width")) width = Integer.valueOf(v);
			else if (k.equals("rows")) {
				pageSize = Integer.parseInt(v);
				setPaging(pageSize > 0);
			}
			else if (k.equals("sort")) {
				if ((sortColumn == null) || !sortColumn.equals(v.substring(1))) sortOrder = 0;
				if (v.charAt(0) == 'A') sortOrder = 1;
				else if (v.charAt(0) == 'D') sortOrder = -1;
				else if (sortOrder == 0) sortOrder = 1;
				else sortOrder = sortOrder * -1;
				sortColumn = v.substring(1);
				doSort = true;
			}
			else if (k.equals("show")) visibleColumns = v.split(",");
			else if (k.equals("order")) columnOrder = v.split(",");
			else if (k.equals("size")) columnSizes = v.split(",");
			// Transitive Parameters
			else if (k.equals("save")) { save = Integer.parseInt(v); doSave = save > 0; }
			else if (k.equals("search")) {
				if (v.contains("|")) {
					String[] s = v.split("\\|");
					searchColumn = s[0];
					searchValue = s[1];
					searchFlag = "new";
				}
				else if (v.equals("clear")) {
					searchColumn = null;
					searchValue = null;
					searchFlag = null;
					searchRowIndex = 0;
				}
				else {
					String[] s = v.split("\\.");
					searchFlag = s[0];
					searchRowIndex = Integer.parseInt(s[1]);
				}
			}
			else if (k.equals("page")) {
				try { pageIndex = Integer.parseInt(v); }
				catch (Exception e) {
					if (v.equals("first")) pageIndex = 1;
					else if (v.equals("previous")) { if (pageIndex > 1) pageIndex--; }
					else if (v.equals("next")) { if (pageIndex < pageCount) pageIndex++; }
					else if (v.equals("last")) pageIndex = pageCount;
					else throw new Exception("[DataGridParams.parseParams] Unknown page designation '" + v + "'.");
				}
			}
		}
		
		if (save == 3) {
			reset();
			if (priorSave > 0) doDelete = true;
		}
	}
	
	public String composeParams(boolean transitive) {
		String[] ord = {"D", "",  "A"};
		ArrayList<String> result = new ArrayList<String>();
		// Stored Parameters
		if (height != null) result.add(param("height", height.toString()));
		if (width != null) result.add(param("width", width.toString()));
		if (save > 0) result.add(param("rows", Integer.toString(pageSize)));
		if (sortColumn != null) result.add(param("sort", ord[sortOrder + 1] + sortColumn));
		if (visibleColumns != null) result.add(param("show", Data.join(visibleColumns, ",")));
		if (columnOrder != null) result.add(param("order", Data.join(columnOrder, ",")));
		if (columnSizes != null) result.add(param("size", Data.join(columnSizes, ",")));
		result.add(param("save", Integer.toString(save)));
		// Transitive Parameters
		if (transitive) {
			// Search?
			result.add(param("page", Integer.toString(pageIndex)));
		}
		String[] r = new String[result.size()];
		r = result.toArray(r);
		return Data.join(r, "&");
	}
	
	protected static String param(String name, String value) {
		return name + "=" + value;
	}
	
	public void setDefaultPaging(boolean defaultPaging) { this.defaultPaging = defaultPaging; }
	
	public boolean getPaging() { return paging; }
	public void setPaging(boolean paging) { 
		this.paging = paging;
		if (paging) {
			if (pageSize == 0) pageSize = 20;
		}
		else {
			pageSize = 0;
			pageIndex = 1;
		}
	}
	
	public int getRecordCount() { return recordCount; }
	public void setRecordCount(int recordCount) { 
		if (recordCount < 1) paging = false;
		this.recordCount = recordCount;
		if (paging && (pageSize == 0)) pageSize = 20;
		pageCount = (pageSize == 0) ? 1 : recordCount / pageSize;
		if ((pageSize > 0) && ((pageCount * pageSize) < recordCount)) pageCount++;
		if (pageCount < 1) pageCount = 1; // Added 2011-08-18 JMP
		if (pageIndex > pageCount) pageIndex = pageCount;
	}
	
	public int getPageCount() { return pageCount; }
	
	public int getPageIndex() { return pageIndex; }
	public void setPageIndex(int pageIndex) { this.pageIndex = pageIndex; }

	public void setPageForRow(int rowIndex) { pageIndex = (pageSize == 0) ? 1 : rowIndex / pageSize + 1; }
	
	public int getPageSize() { return pageSize; }
	public void setPageSize(int pageSize) { this.pageSize = pageSize; }

	public int getPageStart() { return pageSize * (pageIndex - 1); }
	public int getPageEnd() {
		int result = (pageSize * pageIndex) - 1;
		return ((recordCount - 1) > result) ? result : recordCount - 1;  
	}
	
	public String getSortColumn() { return sortColumn; }
	public void setSortColumn(String sortColumn) { this.sortColumn = sortColumn; }

	public int getSortOrder() { return sortOrder; }
	public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
	
	public boolean getDoSort(int hashCode) { 
		boolean result = doSort || ((sortColumn != null) && (hashCode != this.hashCode));
		this.hashCode = hashCode;
		doSort = false; 
		return result; 
	} 

	public void setHashCode(int hashCode) { this.hashCode = hashCode; }

	public String getSearchColumn() { return searchColumn; }
	public void setSearchColumn(String searchColumn) { this.searchColumn = searchColumn; }

	public String getSearchValue() { return searchValue; }
	public void setSearchValue(String searchValue) { this.searchValue = searchValue; }
	
	public String getSearchFlag() { return searchFlag; }
	public void setSearchFlag(String searchFlag) { this.searchFlag = searchFlag; }
	
	public int getSearchRowIndex() { return searchRowIndex; }
	public void setSearchRowIndex(int searchRowIndex) { this.searchRowIndex = searchRowIndex; }

	public String[] getVisibleColumns() { return visibleColumns; }
	public void setVisibleColumns(String[] visibleColumns) { this.visibleColumns = visibleColumns; }
	
	public boolean isColumnVisible(String columnName, boolean defaultValue) {
		if ((columnName == null) || columnName.startsWith("_"))
			return true;
		else if (visibleColumns != null) {
			for (int i = 0; i < visibleColumns.length; i++)
				if (visibleColumns[i].equals(columnName))
					return true;
			return false;
		}
		else return defaultValue;
	}
	
	public String[] getColumnOrder() { return columnOrder; }
	public void setColumnOrder(String[] columnOrder) { this.columnOrder = columnOrder; }

	public String[] getColumnSizes() { return columnSizes; }
	public void setColumnSizes(String[] columnSizes) { this.columnSizes = columnSizes; }
	
	public String getColumnSize(String columnName, String defaultValue) {
		try {
			if (columnSizes != null)
				for (int i = 0; i < columnSizes.length; i++) {
					String[] s = columnSizes[i].split("\\.");
					if (s[0].equals(columnName)) return s[1];
				}
		}
		catch (Exception e) { Log.logException(e, "<unavailable>"); /* Eat it, but print error. */ }
		return defaultValue;
	}

	public Integer getHeight() { return height; }
	public void setHeight(Integer height) { this.height = height; }

	public Integer getWidth() { return width; }
	public void setWidth(Integer width) { this.width = width; }

	public int getSave() { return save; }
	public void setSave(int save) { this.save = save; }
	
	public boolean getDoSave() { return doSave; }
	public boolean getDoDelete() { return doDelete; }
	
	private class PComparator extends AbstractSortComparator {
		private static final long serialVersionUID = 6620799386729941769L;
		private LinkedHashMap<String, Integer> orderMap;

		public PComparator(String sortColumn, LinkedHashMap<String, Integer> orderMap) { super(sortColumn, 1); this.orderMap = orderMap; }
		
		private Method getDeclaredMethod(Object object, String name) throws NoSuchMethodException {
			Method method = null;
			Class<?> clazz = object.getClass();
			
			do { try { method = clazz.getDeclaredMethod(name); } catch (Exception e) { }
			} while (method == null & (clazz = clazz.getSuperclass()) != null);
		
			if (method == null) throw new NoSuchMethodException();
			return method;
		}
		
		private Object invokeMethod(Object object, String methodName, Object[] arguments) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			Method method = getDeclaredMethod(object, methodName);
			method.setAccessible(true);
			return method.invoke(object, arguments);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object arg0, Object arg1) {
			String method = "get" + getSortColumn().substring(0, 1).toUpperCase() + getSortColumn().substring(1);
			Object a = null;
			Object b = null;
			
			try { 
				a = orderMap.get(invokeMethod(arg0, method, null));
				b = orderMap.get(invokeMethod(arg1, method, null));
			} 
			catch (Exception e) { 
				Log.logError("--> DataGridParams Error: PComparator unable to invoke method \"" + method + "\" in class \"" + arg0.getClass().getSimpleName() + "\".", "<unavailable>", e); 
			}
			
			int result = 0;
	        if ((a != null) && (b != null)) result = ((Comparable)a).compareTo((Comparable)b);
	        else result = Integer.valueOf((a == null) ? 2 : 1).compareTo((b == null) ? 2 : 1);
			return result * getSortOrder();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setColumnData(ArrayList<ColumnData> columnData) { 
		this.columnData = columnData;
		if (columnOrder != null) {
			// Create hash map for sorting
			LinkedHashMap<String, Integer> m = new LinkedHashMap<String, Integer>();
			// Sort columns in order of appearance
			// This will place all the non-visible fields last (mostly)
			for (int i = 0; i < columnOrder.length; i++) m.put(columnOrder[i], i);
			Collections.sort(this.columnData, new PComparator("dataSource", m));
		}
		// Evaluate column widths and visibility
		for (int i = 0; i < columnData.size(); i++) {
			ColumnData d = columnData.get(i); 
			d.setWidth(getColumnSize(d.getDataSource(), d.getWidth()));
			d.setVisible(isColumnVisible(d.getDataSource(), d.getVisible()));
		}
	}
	public ArrayList<ColumnData> getColumnData() { return columnData; }
}
