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
var prepType;
var prepParams;
// Prepare Dialog
function prepDialog(type, caption, params) {
	prepParams = { "type": type, "caption": caption, "params": params };
	prepType = type.toLowerCase();
	var url = ((type == 'Theme') || (type == 'Desktop') || (type == 'Book')) 
	        ? contextRoot + '/' + type + '/edit' 
	        : contextRoot + '/configuration/' + type.toLowerCase() + '/_edit.jsp';
    var dlg = Roth.getDialog('dialog', url, params);
    dlg.caption = //Roth.createImage(caption.toLowerCase()) + ' ' + caption + ' ' + type;
    	          Roth.createImageCaption(caption.toLowerCase(), caption + ' ' + type);
    dlg.onshow = function() { 
    	var input = document.getElementById('userid');
    	if (input) {
    		if (input.getAttribute('readonly')) 
    			input = document.getElementById('name');
    		input.focus();
    		input.select();
    	}
    };
    dlg.onhide = function() { /*enableList();*/ };
    dlg.modal = true;
    dlg.show();
}
// Get Parameters
function getParams(type) {
    var tableName = type.toLowerCase() + 'ListTable';
    var selectedRow = getSelectedRow(tableName);
    if (!selectedRow) return null;
    function value(name) { return getSelectedCellData(tableName, name).trim(); }
    if (type == 'User')
        return 'userid=' + value('userid') + '&name=' + value('name') + '&updBy=' + value('updBy') + '&updDts=' + value('updDts');
    else if (type == 'Role')
        return 'roleName=' + value('roleName') + '&description=' + value('description') + '&updBy=' + value('updBy') + '&updDts=' + value('updDts');
    else if (type == 'Portlet')
        return 'portletId=' + value('portletId') + '&portletName=' + value('portletName') + '&portletUri=' + value('portletUri') + '&description=' + value('description') + '&updBy=' + value('updBy') + '&updDts=' + value('updDts');
    else if (type == 'Book')
        return 'bookId=' + value('bookId');
    else if (type == 'Desktop')
        return 'desktopId=' + value('desktopId') + '&desktopName=' + value('desktopName');
    else if (type == 'Theme')
        return 'themeId=' + value('themeId');
    else
        return null;
}
// Add Record
function addRecord(type) {
    prepDialog(type, 'Add', '');
}
// Edit Record
function editRecord(type) {
    var params = getParams(type);
    if (params) prepDialog(type, 'Edit', params);
    else Roth.getDialog('alert').alert('Please first select a ' + type.toLowerCase() + ', then click "Edit".');
}
// Confirm Delete
function confirmDelete(type, which, confirmed) {
    var selectedRow = getSelectedRow(type.toLowerCase() + 'ListTable');
    if (!selectedRow) {
        Roth.getDialog('alert').alert('Please first select a ' + type.toLowerCase() + ', then click "Delete".');
        return;
    }
    if (!confirmed) {
        var dlg = Roth.getDialog('deleteConfirm');
        dlg.button = which;
        dlg.callback = function() { confirmDelete(type, this.button, true); }; 
        dlg.confirm('Are you sure you wish to delete this ' + type.toLowerCase() + '?');
        return;
    }
    document.getElementById('delete' + type + 'Id').value = getChild(selectedRow.cells[0], 0).innerHTML;
    prepType = type.toLowerCase();
    submitForm(which);
}
// Close Dialog
function closeDialog() {
	setTimeout("Roth.getDialog('dialog').hide();");
}
// Valid Password
function validPassword() {
	var p1 = document.getElementById('password');
	var p2 = document.getElementById('_password');
	var result = p1.value == p2.value;
	if (!result) Roth.getDialog('passworderror').alert('The passwords do not match.');
	return result;
} 
// Valid User
function validUser(userid) {
	var result = _$v('userid').trim() != ''; 
	if (!result) {
		Roth.getDialog('error').error('The userid is required.');
		return false;
	}
	result = validPassword();
	if ((userid == '') && result) {
		result = _$v('password').trim() != '';
		if (!result)
			Roth.getDialog('error').error('The password is required when creating a new user.');
	}
	return result;
}
// Get List
function getList(page) {
	prepType = page;
	page = page.nor('list');
	page = page.charAt(0).toUpperCase() + page.slice(1);
	var action = contextRoot + '/' + page + '/load'; 
	document.ajaxobj = new Object();
	var which = document.ajaxobj;
	which['onajaxresponse'] = listAjaxResponse;
	which['onajaxerror'] = function(response) { alert(response.status); };
	which.ajaxCallback = function() {
		if (this.readyState == 4) {
			var state = (this.status == 200) ? 'response' : 'error';
			var handler = this.data['onajax' + state];
			var error = "No " + state + " event handler was specified for this callback."; 
			var f = (!handler) ? null : handler;// new Function('request', handler);
			if (f) f(this); else throw error;
		}
	};
	executeAjax({callback: which.ajaxCallback, method: 'GET', url: action, parameters: '_method=AJAX', data: which});
}
// Get Page
function getPage(page, key, other) {
	document.ajaxobj = new Object();
	var which = document.ajaxobj;
	var action;
	var params;
	if (page == 'userroles') {
		which.page = document.getElementById(page);
		action = contextRoot + '/User/loadRoles';
		params = '_userid=' + key;
	}
	else if (page == 'roleusers') {
		which.page = document.getElementById(page);
		action = contextRoot + '/Role/loadUsers';
		params = '_roleName=' + key;
	}
	else if (page == 'roleportlets') {
		which.page = document.getElementById(page);
		action = contextRoot + '/Role/loadPortlets';
		params = '_roleName=' + key;
	}
	else if (page == 'portletroles') {
		which.page = document.getElementById(page);
		action = contextRoot + '/Portlet/loadRoles';
		params = '_portletId=' + key +
		         "&_portletName=" + other;
	}
	else if (page == 'desktoplinks') {
		which.page = document.getElementById(page);
		action = contextRoot + '/Desktop/loadLinks';
		params = '_desktopId=' + key +
		         "&_desktopName=" + other;
	}
	else return;
	prepType = page;
	which.ajaxCallback = function() {
		if (this.readyState == 4) {
			if (this.status == 200) {
				if (this.responseText.contains('<!--LO' + 'GIN-->')) { login(); return; }
				document.ajaxobj.page.innerHTML = this.responseText;
			}
			else 
				Roth.getDialog('alert').alert(this.status);
		}
	};
	executeAjax({callback: which.ajaxCallback, url: action, parameters: params, data: which});
}
// Toggle Theme Input Focus
function toggleThemeInputFocus(which) {
	var h = document.getElementById('customHeaderHtml');
	var f = document.getElementById('customFooterHtml');
	var c = document.getElementById('copyrightName');
	
	h.style.height = (h == which) ? '250px' : '34px';
	f.style.height = (f == which) ? '250px' : '34px';
	c.style.height = (c == which) ? '250px' : '34px';
}
// ------------ AJAX -------------
var loginLocation = contextRoot + '/configuration/index.jsp';
function login(alerted) {
    if (!alerted) {
        var dlg = Roth.getDialog('loginAlert');
        dlg.callback = function() { login(true); }; 
        dlg.alert('The session has timed out. Please login again.');
        return;
    }
    document.location = loginLocation;
}
function editAjaxResponse(request) {
	if (request.responseText.contains('<!--LO' + 'GIN-->')) { login(); return; }
	listAjaxResponse(request);
	if (prepParams.caption == 'Add') {
		var type = prepParams.type;
		var tableId = type.toLowerCase() + 'ListTable';
		var id;

		switch (type) {
			case "User": id = 'userid'; break;
			case "Role": id = 'roleName'; break;
			case "Portlet": id = 'portletName'; break;
			default: return;
		}
		
		var value = document.getElementById(id).value;
		var grid = document.getElementById(tableId);
		var tbody = getChild(grid, '1.1.1.0.0.1');
		
		for (var i = 0; i < tbody.rows.length; i++) {
			var div = getChild(tbody.rows[i], (type == 'Portlet') ? '1.0' : '0.0');

			if (div.innerHTML == value) {
				rowSelect(tbody.rows[i], null);
				break;
			}
		}
		
		prepDialog(type, 'Edit', getParams(type));
	}
}
function editAjaxError(request) {
	Roth.getDialog('alert').alert(request.status);
}
function listAjaxResponse(request) {
	if (request.responseText.contains('<!--LO' + 'GIN-->')) { login(); return; }
	var div = document.getElementById(prepType + 'list');
	if (!div) div = document.getElementById(prepType);
	if (div) div.innerHTML = request.responseText;
	else Roth.getDialog('alert').alert('Container could not be found for response.');
    Roth.getDialog('wait').hide();
}
function previewTheme() {
	var selectedRow = getSelectedRow('themeListTable');
    if (!selectedRow) {
        Roth.getDialog('alert').alert('Please first select a theme, then click "Preview".');
        return;
    }
    var themeId = getChild(selectedRow.cells[0], 0).innerHTML;
    document.location = contextRoot + '/Theme/preview?themeId=' + themeId; 
}

function canManageContent() {
	var selectedRow = getSelectedRow('contentTable');
    if (!selectedRow) return true;
    else if (selectedRow.getAttribute("key").contains("tableName=book")) { 
    	Roth.getDialog('alert').alert('Books may not be edited or deleted from this list.'); 
    	return false; 
    }
    return true;
}
function refreshThemes() {
	Roth.ajax.htmlAction('themelist', contextRoot + '/Theme/load', '_method=AJAX'); 
	Roth.getDialog('import').hide();
}