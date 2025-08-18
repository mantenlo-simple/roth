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
import java.math.BigDecimal;
import java.sql.SQLException;

import com.roth.base.log.Log;
import com.roth.portal.util.Portal;

public class MenuItem implements Serializable {
	private static final long serialVersionUID = 5310939424173003714L;

	private String _itemType;
	private BigDecimal _bookId;
	private String _title;
	private String _portletUri;
	private BigDecimal _sequence;
	private MenuItem[] _menuItems;
	private MenuItem _parent;
	
	private boolean _menuItemsLoaded;

	public String getItemType() { return _itemType; }
	public void setItemType(String itemType) { _itemType = itemType; }
	
	public BigDecimal getBookId() { return _bookId; }
	public void setBookId(BigDecimal bookId) { _bookId = bookId; }
	
	public String getTitle() { return _title; }
	public void setTitle(String title) { _title = title; }
	
	public String getPortletUri() { return _portletUri; }
	public void setPortletUri(String portletUri) { _portletUri = portletUri; }
	
	public BigDecimal getSequence() { return _sequence; }
	public void setSequence(BigDecimal sequence) { _sequence = sequence; }	
	
	public MenuItem[] getMenuItems(String userid) {
		if (_bookId == null) return null;
		
		if (!_menuItemsLoaded) {
			try {
				Portal p = new Portal();
				_menuItems = p.getMenuItems(_bookId, userid); 
			}
			catch (SQLException e) { Log.logException(e, null); }
			_menuItemsLoaded = true;
		}
		
		if (_menuItems != null)
			for (int i = 0; i < _menuItems.length; i++)
				_menuItems[i].setParent(this);
		
		return _menuItems; 
	}
	
	public MenuItem getParent() { return _parent; }
	public void setParent(MenuItem parent) { _parent = parent; }
}
