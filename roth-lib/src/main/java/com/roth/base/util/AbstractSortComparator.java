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
package com.roth.base.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 
 * @author James M. Payne
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractSortComparator implements Comparator, Serializable {
	private static final long serialVersionUID = 3046099788465637095L;
	
	private String _sortColumn;
	private int _sortOrder;
	
	public AbstractSortComparator(String sortColumn, int sortOrder) {
		_sortColumn = sortColumn;
		_sortOrder = sortOrder;
	}
	
	// sortColumn
	public String getSortColumn() { return _sortColumn; }
	public void setSortColumn(String sortColumn) { _sortColumn = sortColumn; }
	// sortOrder
	public int getSortOrder() { return _sortOrder; }    
	public void setSortOrder(int sortOrder) { _sortOrder = sortOrder; }
	
	public abstract int compare(Object o1, Object o2);
	/*
	Example:
	    {
		    MyClass a = (MyClass)o1;
		    MyClass b = (MyClass)o2;
		    int result = 0;
		    
		    // Primitives
		    if (getSortColumn().equals("someColumn")) {
    		    result = (a.getSomeColumn() > b.getSomeColumn()) ? 1 : (a.getSomeColumn() < b.getSomeColumn()) ? -1 : 0;
    		// Strings
    		else if (getSortColumn().equals("anotherCol"))
    		    result = a.getAnotherCol().compareToIgnoreCase(b.getAnotherCol());
    		// Other comparables (Integer, BigDecimal, Date, etc.);
    		else if (getSortColumn().equals("yetAnother"))
    		    result = a.getYetAnother().compareTo(b.getYetAnother());
    		    
    		return result * getSortOrder();
        }
	 */
}