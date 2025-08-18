package com.roth.portal.util;

import com.roth.base.util.AbstractSortComparator;

public class LengthComparator extends AbstractSortComparator {
	
	public LengthComparator(String sortColumn, int sortOrder) {
		super(sortColumn, sortOrder);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 2814022845202869155L;

	@Override
	public int compare(Object o1, Object o2) {
		int result = ((String)o1).length() > ((String)o2).length() ? 1 :
			         ((String)o1).length() < ((String)o2).length() ? -1 :	0;
		return result * getSortOrder();
	}
}
