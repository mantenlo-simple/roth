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
function login(alerted) {
    if (!alerted) {
        var dlg = Roth.getDialog('loginAlert');
        dlg.callback = function() { login(true); }; 
        dlg.alert('The session has timed out. Please login again.');
        return;
    }
    document.location = document.location;
}
function rtogpg(id, show) {
    if (!id) return;
    var page = document.getElementById(id);
    if (page) page.style.display = show ? 'block' : 'none';
}

function updateCsrf() {
	Roth.ajax.jsonAction(contextRoot + "/refresh.jsp", null, null, data => {
		document.getElementsByName("_csrf-token").forEach(e => {
			e.value = data.csrfToken;
		});
	});
}

function rtmu(which) {
    var p = which.parentNode;
    for (var i = 0; i < p.children.length; i++)
        if (p.children[i].tagName == 'LI') {
            p.children[i].className = p.children[i].className.nor('selected');
            rtogpg(p.children[i].getAttribute('page'), false);
        }
    which.className = which.className.or('selected');
    p.page = which.getAttribute('page');
    p.action = which.getAttribute('action');
    rtogpg(p.page, true);
}


function loadInputEvents() {
    var inp = document.getElementsByClassName('jinp');
    if (inp)
        for (var i = 0; i < inp.length; i++) {
            var c = getChild(inp[i], '0.0');
            addEvent(c, 'focus', function() { var a = this.parentNode.parentNode; a.className = a.className.or('jinp-focus'); });
            addEvent(c, 'blur', function() { var a = this.parentNode.parentNode; a.className = a.className.nor('jinp-focus'); });
        }
}
addEvent(window, 'load', function() { loadInputEvents(); });
/* Forms */
function submitForm(which, action) {
    var formId = which.getAttribute('formId');
    if (formId) which = document.getElementById(formId);
    while (which && (which.tagName != 'FORM')) which = which.parentNode;
    if (which && (which.tagName == 'FORM')) {
		let mceInputs = which.querySelectorAll(".roth-tinymce");
		if (mceInputs)
			mceInputs.forEach(e => {
				e.innerHTML = tinymce.get(e.id).getContent();
			});
	
    	var reqc = 0;
    	for (var i = 0; i < which.elements.length; i++) {
    		var value = nvl(getValue(which.elements[i]), '');
    		if (which.elements[i].hasAttribute('required') && (value == ''))
    			reqc++;
    	}
    	if (reqc > 0)
    		Roth.getDialog('error').error('One or more <span class="jrequired">required</span> values are missing.');
    	else if ((!which.onsubmit) || (which.onsubmit() != false)) {
            var oldAction = which.action;
            if (action) which.action = action;
            if (!which.getAttribute('ajax')) which.submit();
            else { 
                submitAjaxForm(which);
                which.action = oldAction;
            }
        }
    }
}
function resetForm(which) {
    while (which && (which.tagName != 'FORM')) which = which.parentNode;
    if (which && (which.tagName == 'FORM')) which.reset();
}
function submitAjaxForm(which) {
	var multipart = which.getAttribute('enctype') == 'multipart/form-data';
	var parameters = multipart ? new FormData(which) : '';
    if (!multipart)
	    for (var i = 0; i < which.elements.length; i++) {
	        if (which.elements[i].name) {
	            if (parameters != '') parameters += '&';
	            parameters += which.elements[i].name + '=' + encodeURIComponent(which.elements[i].value); //encodeURIComponent(which.elements[i].value).replace(/\+/g, '%2B');
	        }
	    }
	which.ajaxCallback = function() {
        // Note: "this" does not refer to the Form instance here.
        // Instead, it refers to the AJAX request object.  This function
        // references the Form instance through "this.data".
        if (this.readyState == 4) {
            // Check for generic handler.  If found, execute and return.
            var handler = this.data.getAttribute('onajax');
            var f = (!handler) ? null : new Function('request', handler);
            if (f) { f.call(this.data, this); return; }
            // If not found, check for response/error-specific hander.
            var state = (this.status == 200) ? 'response' : 'error';
            handler = this.data.getAttribute('onajax' + state);
            var error = "No " + state + " event handler was specified for this callback."; 
            f = (!handler) ? null : new Function('request', handler);
            if (f) f.call(this.data, this); else throw error;
        }
    }; 
    let responseType = which.getAttribute('responseType');
    executeAjax({callback: which.ajaxCallback, method: 'POST', url: which.action, parameters: parameters, data: which, responseType: responseType});
}
/*
function attachCalendar(which) {
    if (!which && window.event) which = window.event.srcElement;
    var c = which.getAttribute('calendar').split('|');
    Calendar.setup({
        trigger    : which,//[which,_$(which.getAttribute('keyid'))],
        inputField : _$(which.getAttribute('keyid')),
        fdow       : 0,
        dateFormat : c[0] == 'Y' ? '%Y-%m-%d %H:%M' : '%Y-%m-%d',
        align      : 'Br/ / /T/r',
        showTime   : c[0] == 'Y',
        onSelect   : function() { 
        				 this.hide(); 
        				 let input = _$(which.getAttribute('keyid'));
        				 if (input.onchange)
        					 input.onchange();
        			 }
    });
    which.onmouseover = function() {};
}
*/
function attachCalendar(which) {
    if (!which && window.event) which = window.event.srcElement;
    let c = which.getAttribute('calendar').split('|');
    let keyid = which.getAttribute('keyid');
    which.dtselInstance = new dtsel.DTS(`#${keyid}`,  {
    	trigger: which,
	    direction: 'BOTTOM',
	    dateFormat: "yyyy-mm-dd",
	    showTime: c[0] === 'Y',
	    timeFormat: "HH:MM:SS",
	    paddingX: 0,
	    paddingY: 0
	});
    which.onclick = (event) => {
    	let e = event || window.event;
    	_$(keyid).drop(e);
    	e.stopPropagation();
    };
    which.calendarattached = true;
    /*
    which.previousElementSibling.onmouseover = function() {};
    which.previousElementSibling.onkeydown = function() {};
    which.onmouseover = function() {};
    */
}
var logoutRedirect = portalRoot;
function logout(confirmed) {
	if (!confirmed) {
		var dlg = Roth.getDialog('confirmlogout');
		dlg.callback = function() { logout(true); };
		dlg.confirm("Do you wish to log out?");
		return;
	}
	document.logoutCallback = function() {
        if (this.readyState == 4) {
            if (this.status == 200)
                document.location = logoutRedirect;
            else if (this.status == 401)
            	document.location = logoutRedirect;
            else
                Roth.getDialog('error').error('An error occurred while trying to logout.');
        }
    };
	executeAjax({callback: document.logoutCallback, method: 'GET', url: portalRoot + '/Auth/logout', type: "text/plain", user: 'logout', password: 'logout'});
}
/* Generic */
/* BEGIN JpLib */
var Roth = function Roth() {};
/* BEGIN Document Mouse Move Event Lister */
Roth.mouse = new Object();
Roth.mouse.object = null;
Roth.mouse._$dmmel = function(e) {
    if (!e) e = event;
    Roth.mouse.x = (isIE) ? e.clientX + document.body.scrollLeft : e.pageX;
    Roth.mouse.y = (isIE) ? e.clientY + document.body.scrollTop : e.pageY;
  
    // catch possible negative values in NS4
    if (Roth.mouse.x < 0) Roth.mouse.x = 0;
    if (Roth.mouse.y < 0) Roth.mouse.y = 0;
  
    if (Roth.mouse.object && Roth.mouse.object.callback)
        Roth.mouse.object.callback(e);
    if (Roth.mouse.object && Roth.mouse.object.onmousemove)
        Roth.mouse.object.onmousemove(e);
  
    return true;
};
Roth.mouse._$dmuel = function(e) {
	if (Roth.mouse.object && Roth.mouse.object.onmouseup)
		Roth.mouse.object.onmouseup(e);
	setTimeout('Roth.mouse.object = null;'); return true; 
};
if (!isIE) document.addEventListener("mousemove", Roth.mouse._$dmmel, true);
document.onmousemove = Roth.mouse._$dmmel;
if (!isIE) document.addEventListener("mouseup", Roth.mouse._$dmuel, true);
document.onmouseup = Roth.mouse._$dmuel;
/* END Document Mouse Move Event Lister */

Roth.isRothObject = function(className) {
    return className.contains('roth-button') || className.contains('rtbl') || className.contains('rgrd') || className.contains('control-btn');
};
Roth.enable = function(id, negate) {
    var ids = (id.isArray && id.isArray()) ? id : new Array(id);
    for (var i = 0; i < ids.length; i++) {
        var obj = _$(ids[i]);
        if (!this.isRothObject(obj.className)) continue;
        obj.className = (negate) ? obj.className.or('disabled') : obj.className.nor('disabled');
        if (obj && (obj.className.contains('roth-button') || obj.className.contains('control-btn'))) {
        	if (!negate && (obj.whenclick || obj.getAttribute('whenclick'))) {
            	obj.onclick = (obj.whenclick) ? obj.whenclick : new Function('event', obj.getAttribute('whenclick'));
                obj.whenclick = null;
            }
            else if (negate && !obj.whenclick) {
                obj.whenclick = obj.onclick;
                obj.onclick = function() { return false; };
            }
        }
    }
};
Roth.disable = function(id) { this.enable(id, true); };
Roth.isEnabled = function(id) {
    var o = document.getElementById(id);
    if (o && o.className) return !o.className.contains('disabled');
    else throw "A valid object was not found.";
};
Roth.isDisabled = function(id) { return !this.isEnabled(id); };
Roth.createBreak = function(height) {
    height = (!height) ? '' : ' style="height: ' + height + ';"'; 
    return '<div class="rbreak"' + height + '></div>';
};
Roth.createStackedImage = function(baseImageName, baseStyle, topImageName, topStyle) {
	return '<span class="fa-stack">' +
			   '<i class="fa' + baseStyle + ' fa-' + baseImageName + ' fa-stack-1x"></i>' +
			   '<i class="fa' + topStyle + ' fa-' + topImageName + ' fa-stack-1x"></i>' +
		   '</span>';
};
Roth.createStackedImage2 = function(baseImageName, baseStyle, topImageName1, topStyle1, topImageName2, topStyle2) {
	return '<i class="fa' + baseStyle + ' fa-' + baseImageName + ' fa-stack-1x"></i>' +
		   '<i class="fa' + topStyle1 + ' fa-' + topImageName1 + ' fa-stack-1x"></i>' +
		   '<i class="fa' + topStyle2 + ' fa-' + topImageName2 + ' fa-stack-1x"></i>';
};
Roth.createLabel = function(forId, className, body) {
	return '<label for="' + forId + '" class="' + className + '">' + body + '</label>';
};
Roth.createCheckbox = function(id, name, boolValues, checked, label) {
	let bool = boolValues.split('|');
	let f = bool[0];
	let t = bool[1];
	return '<input type="checkbox" id="' + id + '" name="__' + name + '" onclick="_$(\'rcb' + id + '\').value = this.checked ? \'' + t + '\' : \'' + f + '\';" value="' + f + '" ' + (checked ? 'checked' : '') + '>' +
		   Roth.createLabel(id, "cb-toggle fa-stack", Roth.createStackedImage2('square', 's', 'square', 'r', 'check-square', 'r')) +
		   Roth.createLabel(id, '', label) +
	       '<input type="hidden" id="rcb' + id + '_val" name="' + name + '" value="' + (checked ? t : f) + '">';
};
Roth.createRadioItem = function(id, name, value, checked, label) {
	return '<input type="radio" id="' + id + '" name="__' + name + '" onclick="_$(\'rri' + id + '\').value = this.value;" value="' + value + '" ' + (checked ? 'checked' : '') + '>' +
		   Roth.createLabel(id, "cb-toggle fa-stack", Roth.createStackedImage2('circle', 's', 'circle', 'r', 'dot-circle', 'r')) +
		   Roth.createLabel(id, '', label) +
	       '<input type="hidden" id="rri' + id + '_val" name="' + name + '" value="' + (checked ? value : '') + '">';
};
Roth.createIcon = function(iconName) {
    if (!iconName) throw "No iconName was specified.";
    if (iconName == 'wait') iconName = 'spinner fa-pulse fa-fw';
    
    let style = 's';
    if (iconName.includes('.')) {
    	let idx = iconName.indexOf('.');
    	style = iconName.substring(0,idx);
    	iconName = iconName.substring(idx + 1);
    }
    return '<span class="fa' + style + ' fa-' + iconName + '"></span>'; 
};
Roth.createIconLink = function(iconName, href, onclick, style) {
    var icon = this.createIcon(iconName);
    return '<a' + href + onclick + '>' + icon + '</a>';
};
Roth.createIconCaption = function(iconName, caption) {
    caption = '&nbsp;&nbsp;<span>' + caption + "</span>";
    return '&nbsp;' + this.createIcon(iconName) + caption;
};
Roth.createButton = function(caption, iconName, href, onclick) {
    if (caption) caption = '<span>' + caption + "</span>";
    if (!href) href = '#';
    href = ' href="' + href + '"';
    onclick = (!onclick) ? '' : ' onclick="' + onclick + '"';
    var icon = (!iconName) ? '' : ' ' + this.createIcon(iconName);
    return '<a' + href + onclick + ' class="roth-button">' +
               '<div>' + caption + icon + '</div>' +
           '</a>';
};
Roth.changePassword = function(expired, forgotten) {
	Roth.getDialog('error').hide();
    var script = document.getElementById('changePasswordScript');
    if (!script) {
        script = document.createElement("script");
        script.type = 'text/javascript';
        script.src = portalRoot + '/profile/index.js';
        script.id = 'changePasswordScript';
        document.body.appendChild(script);
    }
    var dlg = Roth.getDialog('changePassword', expired ? portalRoot + '/authenticationservlet/changepassword.jsp' :
    		                                    forgotten ? portalRoot + '/authenticationservlet/getvalidationcode.jsp' : portalRoot + '/Profile');
    dlg.caption = this.createIconCaption('key', 'Change Password');
    dlg.modal = true;
    dlg.onshow = function() { 
    	if (!expired) _$('oldPassword').focus(); 
    	else _$('userid').focus(); 
    };
    dlg.show();
};
Roth.pojoGen = function() {
	Roth.getDialog('wait').wait();
	Roth.execDialog('pojogen', developerRoot + '/Developer/createPojo', null, 'POJO Generator', 'package');
};
Roth.groupAdmin = function() {
	Roth.getDialog('wait').wait();
	Roth.execDialog('deptadmin', portalRoot + '/GroupAdmin', null, 'Group Administrator', 'users');
};
Roth.logSettings = function() {
	Roth.getDialog('wait').wait();
	Roth.execDialog('logsettings', portalRoot + '/Logging', null, 'Log Settings', 'cog', null, null, null, true);
};
Roth.editProfile = function() {
	Roth.getDialog('wait').wait();
    Roth.execDialog('editProfile', portalRoot + '/Profile/edit', null, 'Edit Profile', 'user', 'profileAddress');
};
Roth.compValidate = function(ida, idb) {
    var diff = (_$v(ida) != _$v(idb));
    _$(ida).style.color = diff ? 'firebrick' : '';
    _$(idb).style.color = diff ? 'firebrick' : '';
    return !diff;
};
/* END JpLib */
/* Tables */
Roth.grid = {
	getState(id) {
		let table = _$(id);
		let scr = getChild(table, '1.0.0');
		let row = getSelectedRow(id);
		return {index:row?row.rowIndex:null,key:row?row.getAttribute('key'):null,scrollLeft:scr.scrollLeft,scrollTop:scr.scrollTop};
	},
	setState(id, state, selection = 'index', trigger = null) {
		let table = _$(id);
		let scr = getChild(table, '1.0');
		scr.scrollTop = state.scrollTop;
		scr.scrollLeft = state.scrollLeft;
		let tbody = getChild(table, '1.0.0.1');
		let row;
		if (selection === 'index' && _$0(state.index))
			row = tbody.rows[state.index];
		else if (selection === 'key' && state.key) 
			for (let i = 0; i < tbody.rows.length; i++)
				if (tbody.rows[i].getAttribute('key') === state.key) {
					row = tbody.rows[i];
					break;
				}
		if (!row) {
			clearSelect(id);
			return;
		}
		if (trigger !== 'click')
			rowSelect(row);
		if (trigger === 'click' || trigger === 'dblclick')
			triggerEvent(row, trigger);
	}
}
function getSelectedRow(id) {
    var table = document.getElementById(id);
    return (table && table.data) ? table.data['selectedRow'] : null;
}
function setSelectedRow(id, index) {
    var grid = document.getElementById(id);
    //var row = getChild(grid, '1.1.1.0.0.1.' + index);
    var row = getChild(grid, '1.1.0.0.0.1.' + index);
    rowSelect(row, null);
}
function getSelectedCellData(tableId, dataSource) {
    var table = document.getElementById(tableId);
    var row = (table && table.data) ? table.data['selectedRow'] : null;
    
    if (row) {
        var ci = null;
        //var header = getChild(table, '1.0.1.0.0.1.0');
        var header = getChild(table, '1.0.0.0.0.1.0');
        
        for (var i = 0; i < header.cells.length; i++) {
            if (header.cells[i].getAttribute('datasource') == dataSource)
                ci = i;
        }
        
        if (ci != null) return getChild(row.cells[ci], 0).innerHTML;
    }
    
    return null;
}
function rowSelect(which, e) {
    if (window.event) e = window.event;
    //var table = getAncestorNode(which, 'TABLE');
    //var grid = getAncestorNode(table, 'TABLE');
    var table = getAncestorWithClass(which, 'rtbl');
    var grid = getAncestorWithClass(table, 'rgrd');
    if (grid.className.contains('disabled')) return;
    if (!grid.data) grid.data = new Object();
    var selectedRow = grid.data['selectedRow'];
    if (selectedRow != null) selectedRow.className = selectedRow.className.nor('highlighted');
    if ((selectedRow == which) && e && e.ctrlKey)
        grid.data['selectedRow'] = null;
    else {
        grid.data['selectedRow'] = which;
        which.className = which.className.or('highlighted');
    }
}
function clearSelect(id) {
	let grid = _$(id);
	if (!grid.data) grid.data = new Object();
    var selectedRow = grid.data['selectedRow'];
    if (selectedRow != null) selectedRow.className = selectedRow.className.nor('highlighted');
    grid.data['selectedRow'] = null;
}

function findColGroup(tbl) {
    for (var i = 0; i < tbl.children.length; i++)
        if (tbl.children[i].tagName == 'COLGROUP')
            return tbl.children[i];
    return null;
}
function moveColumn(tbl, colidx, newidx) {
    var offset = 1;
    // If a move is not taking place, don't bother processing.
    if (colidx == newidx) return;
    // If the column is moving to the left, then reverse the offset, so that positioning will be correct.
    if (newidx < colidx) offset = 0; //-1;
    //var tbl = document.getElementById(tableid);
    var grp = findColGroup(tbl);
    // If there's a colgroup, then move the specified col element.
    if (grp) {
        if ((newidx > 0) && (newidx < grp.children.length - 1))
            grp.insertBefore(grp.children[colidx], grp.children[newidx + offset]);
        else if (offset > 0)
            grp.appendChild(grp.children[colidx]);
        else
            grp.insertBefore(grp.children[colidx], grp.children[0]);
    }
    // Loop through the rows and move the specified cell in each row.
    for (var i = 0; i < tbl.rows.length; i++) {
        if ((newidx > 0) && (newidx < tbl.rows[i].cells.length - 1))
            tbl.rows[i].insertBefore(tbl.rows[i].cells[colidx], tbl.rows[i].cells[newidx + offset]);
        else if (offset > 0)
            tbl.rows[i].appendChild(tbl.rows[i].cells[colidx]);
        else
            tbl.rows[i].insertBefore(tbl.rows[i].cells[colidx], tbl.rows[i].cells[0]);
    }
}
function toggleColumn(datagrid, index) {
    /*var hc = getChild(datagrid, '1.0.1.0.0.0.' + index);
    var h = getChild(datagrid, '1.0.1.0.0.1');
    var bc = getChild(datagrid, '1.1.1.0.0.0.' + index);
    var b = getChild(datagrid, '1.1.1.0.0.1'); */
    var hc = getChild(datagrid, '1.0.0.0.0.0.' + index);
    var h = getChild(datagrid, '1.0.0.0.0.1');
    var bc = getChild(datagrid, '1.1.0.0.0.0.' + index);
    var b = getChild(datagrid, '1.1.0.0.0.1');
    
    var display = (hc.style.display == 'none') ? 'table-column' : 'none';
    hc.style.display = display;
    bc.style.display = display;
    if (display == 'table-column') display = 'table-cell';
    
    for (var i = 0; i < h.rows.length; i++)
        h.rows[i].cells[index].style.display = display;
    
    for (var i = 0; i < b.rows.length; i++)
        b.rows[i].cells[index].style.display = display;
}
//createElementAppend
Roth.createElementAppend = function(name, className) {
    var element = document.createElement(name);
    element.className = className;
    document.body.appendChild(element);
    return element;
}; var _$jadd = Roth.createElementAppend;
// removeElement
Roth.removeElement = function(element) {
	document.body.removeChild(element);
}; var _$jdel = Roth.removeElement;
// TABLE
Roth.table = new Object();
// TABLE / TR
Roth.table.tr = new Object();
// TABLE / TR / (handler) onMouseDown
Roth.table.tr._onMouseDown = function(e) {
    if (!e) e = window.event;
    //var which = (e.target) ? e.target : e.srcElement;
    return false;
}; var _$rtrmdn = Roth.table.tr._onMouseDown;
// TABLE / TR / (handler) onMouseMove
Roth.table.tr._onMouseMove = function(e) {
    if (!e) e = window.event;
    var which = (e.target) ? e.target : e.srcElement;
    
    //var grid = getAncestorNode(getAncestorNode(which, 'TABLE'), 'TABLE');
    var grid = getAncestorWithClass(getAncestorWithClass(which, 'rtbl'), 'rgrd');
    if (grid.className.contains('disabled')) { 
        which.style.cursor = 'default'; 
        which.dragtype = null;
        return; 
    }
    
    if (which.innerHTML == '') return;
    var c = (which.nodeName == 'TH') ? which : which.parentNode;
//    var isresize = c.className.contains('resize') && Roth.mouse.inEdge(c);
    var ismove = c.className.contains('move');// && !isresize;
//    which.style.cursor = (isresize) ? 'col-resize' : 'default';
//    which.dragtype = (isresize) ? 'resize' : (ismove) ? 'move' : null;
    which.dragtype = (ismove) ? 'move' : null;
    //if ((which.edge == 'left') && (which.clientWidth == 3)) which.edge = 'right';
    //if (which.edge == 'left') c = c.parentNode.cells[c.cellIndex - 1];
    //if ((c.clientWidth == 3) && (which.dragtype == 'resize')) {
    if ((c.clientWidth == 5) && (which.dragtype == 'resize')) {
        which.title = c.innerHTML;
        which.onmouseout = function() { this.title = ''; };
    }
    //if (which.dragtype != 'resize') which.title = '';
    which.title = '';
}; var _$rtrmmv = Roth.table.tr._onMouseMove;


Roth.table.col = {
	"sizer": {
		"mousedown": function(event) {
			let e = event || window.event; 
			let t = (e.target) ? e.target : e.srcElement;
			let h = getAncestorNode(t, 'TH');
			Roth.mouse.object = { 
	            "sizer": t,
	            "width": h.clientWidth,
	            "mx": Roth.mouse.x,
	            "callback": function() {
	            	let xdiff = Roth.mouse.x - Roth.mouse.object.mx;
	            	let col = getChild(getAncestorWithClass(h, 'rtbl'), '0.' + h.cellIndex);
	            	let newWidth = Roth.mouse.object.width + xdiff; // + offset; 
	            	let min = getEmPixels(h) * 2.5;
	            	if (newWidth < min) newWidth = min;
	                _$rsed(col, newWidth - ((isIE) ? 2 : 0));
	                col = getChild(getAncestorWithClass(getAncestorWithClass(h, 'rtbl'), 'rgrd'), '1.0.0.0.' + h.cellIndex);
	                _$rsed(col, newWidth - ((isIE) ? 2 : 0));
	            }
	        };
			e.stopPropagation();
		},
		"mouseup": function(event) {
			let e = event || window.event; 
			Roth.mouse.object = null;
			e.stopPropagation();
		}
	}
};


// MOUSE / in
Roth.mouse.inElement = function(which) {
    var coord = getAbsCoord(which);
    coord.w = which.clientWidth;
    coord.h = which.clientHeight;
    return (Roth.mouse.x >= coord.x) && (Roth.mouse.x <= (coord.x + coord.w)) &&
           (Roth.mouse.y >= coord.y) && (Roth.mouse.y <= (coord.y + coord.h));
}; var _$rmin = Roth.mouse.inElement;
// MOUSE / inEdge
Roth.mouse.inEdge = function(which) {
    var coord = getAbsCoord(which);
    //coord.l = Roth.mouse.x - coord.x;
    coord.r = coord.x + which.clientWidth - Roth.mouse.x;
    //which.edge = (coord.l <= 3) ? 'left' : (coord.r <= 3) ? 'right' : null;
    which.edge = (coord.r <= 5) ? 'right' : null;
    //if ((which.cellIndex == 0) && (which.edge == 'left')) which.edge = null;
    return which.edge != null;
}; var _$rmine = Roth.mouse.inEdge;
// setElementDimensions
Roth.setElementDimensions = function(e, w, h, x, y) {
    if (w) e.style.width = w + 'px';
    if (h) e.style.height = h + 'px';
    if (x) e.style.left = x + 'px';
    if (y) e.style.top = y + 'px';
}; var _$rsed = Roth.setElementDimensions;
Roth.table.sizer = {
    "_onMouseDown": function(e) {
        if (!e) e = window.event;
        var which = (e.target) ? e.target : e.srcElement;
        var table = getAncestorWithClass(which, 'rtbl');
        /*
        //var body = getChild(table, '1.1.1.0'); // [table.]tbody.tr(2nd).td(2nd).div
        var body = getChild(table, '1.1.0.0'); // [table.]tbody.tr(2nd).td(2nd).div
        var column = getChild(table, '0.1');
        */
        Roth.mouse.object = { 
            //"column": column,
            "sizer": which,
            //"width": parseInt(column.style.width),
            "width": parseInt(table.style.width),
            //"body": body,
            //"height": parseInt(body.style.height),
            "height": parseInt(table.style.height),
            "mx": Roth.mouse.x,
            "my": Roth.mouse.y,
            "table": table,
            "callback": function() {
            	var xdiff = Roth.mouse.x - Roth.mouse.object.mx;
                var ydiff = Roth.mouse.y - Roth.mouse.object.my;
                //_$rsed(Roth.mouse.object.column, Roth.mouse.object.width + xdiff);
                //_$rsed(Roth.mouse.object.body, null, Roth.mouse.object.height + ydiff);
                _$rsed(Roth.mouse.object.table, Roth.mouse.object.width + xdiff);
                _$rsed(Roth.mouse.object.table, null, Roth.mouse.object.height + ydiff);
            }
        };
        which.onmouseup = function() {
            Roth.mouse.object = null;
        };
        return false;
    }
};
Roth.table.pager = {
    "_onKeyDown": function(e, which) {
        if (!e) e = window.event;
        if (e.keyCode != 13 || which.nodeName != 'INPUT' || e.button > 0) 
        	return;
        var max = parseInt(which.getAttribute('max'));
        var req = parseInt(which.value);
        if (isNaN(req) || (req != which.value) || (req < 1) || (req > max)) Roth.getDialog('error').error('The specified page index is not valid.');
        //else tableAction(getAncestorNode(which, 'TABLE').id, 'page=' + req);
        else tableAction(getAncestorWithClass(which, 'rgrd').id, getValue(_$('gcToken')), 'page=' + req);
        return false;
    }
};
Roth.table.search = {
    "_onClick": function(e, which, flag) {
        if (!e) e = window.event;
        //var grid = getAncestorNode(which, 'TABLE');
        var grid = getAncestorWithClass(which, 'rgrd');
        
        if (flag) {
            Roth.table.search.execute(grid.id, flag);
            return;
        }
        
        /*
        var content = '<div style="position: relative; width: 375px;">' +
                          '<div class="jedt">' +
                              'Search Column<br/>' +
                              '<select id="_j_dataGrid_search_column">' +
                                  Roth.table.search.getColumns(grid) +
                              '</select>' +
                          '</div>' +
                          '<div class="jedt">' +
                              'Search Value<br/>' +
                              '<input type="text" id="_j_dataGrid_search_value" onKeyDown="Roth.table.search._onKeyDown(event, this)"/>' +
                          '</div>' +
                          Roth.createBreak() +
                             '<div style="width: 240px;">' +
                             Roth.createButton('OK', 'check', null, "Roth.table.search.submit(); return false;") +
                             Roth.createButton('Cancel', 'ban', null, "Roth.getDialog('dataGridSearch').hide(); return false;") +
                             '</div>' +
                             Roth.createBreak() +
                      '</div>';
        var dlg = Roth.getDialog('dataGridSearch');
        dlg.caption = //Roth.createIcon('search') + ' Search Data Grid';
                      Roth.createIconCaption('search', 'Search Data Grid');
        dlg.message = content;
        dlg.callback = function() { Roth.table.search.execute(grid.id); };
        dlg.modal = true;
        dlg.show();
        if (grid.getAttribute('searchcolumn')) {
            document.getElementById('_j_dataGrid_search_column').value = grid.getAttribute('searchColumn');
            document.getElementById('_j_dataGrid_search_value').value = grid.getAttribute('searchValue');
        }
		*/

		let params = 'gridid=' + grid.id + '&fields=' + grid.getAttribute('fields');
		Roth.execDialog('dataGridSearch', contextRoot + '/gridsrch.jsp', params, 'Search Data Grid', 'search', null, null, function() { Roth.table.search.execute(grid.id); });
    },
    "_onKeyDown": function(e, which) {
        if (!e) e = window.event;
        if (e.keyCode == 13) Roth.table.search.submit();
    },
    "submit": function() {
        var dlg = Roth.getDialog('dataGridSearch'); 
      //  dlg.hide(); 
        dlg.callback();
    },
    "getColumns": function(grid) {
        var result = '';
        var fields = grid.getAttribute('fields').split(',');
        fields.sort(function(a, b) { return (a.split('|')[1].toLowerCase() < b.split('|')[1].toLowerCase()) ? -1 : 1; });
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i].split('|');
            if (field[2] == 'true')
                result += '<option value="' + field[0] + '">' + field[1] + '</option>';
        }
        return result;
    },
    "execute": function(id, flag) {
        var params;
        if (flag) {
            if (Roth.table.search.find(id, flag)) return;
            var grid = document.getElementById(id);
            params = 'search=' + flag;
            if (flag != 'clear') params += '.' + grid.getAttribute('searchrowindex');
        }
        else {
            var col = document.getElementById('_j_dataGrid_search_column');
            var val = document.getElementById('_j_dataGrid_search_value');
            params = 'search=' + getValue(col) + '|' + val.value;
        }
        tableAction(id, getValue(_$('gcToken')), params);
    },
    "getAttrObj": function(id) {
        var grid = document.getElementById(id);
        if (grid.attrObj) return grid.attrObj;
        var a = new Object();
        //a.tbody = getChild(grid, '1.1.1.0.0.1');
        //a.tbody = getChild(grid, '1.1.0.0.0.1');
        a.tbody = getChild(grid, '1.0.0.1');
        a.pageSize = getAttrInt(grid, 'pagesize', a.tbody.rows.length);
        a.pageIndex = getAttrInt(grid, 'pageindex', 1);
        a.searchRowIndex = getAttrInt(grid, 'searchrowindex');
        a.start = (a.pageIndex - 1) * a.pageSize;
        a.end = a.start + a.pageSize - 1;
        if ((a.end - a.start) > a.tbody.rows.length) a.end = a.tbody.rows.length + a.start - 2;
        a.rowIndex = a.searchRowIndex - a.start;
        grid.attrObj = a;
        return a;
    },
    "find": function(id, flag) {
        if (flag == 'clear') return false;
        var grid = document.getElementById(id);
        var a = Roth.table.search.getAttrObj(id);
        if ((a.rowIndex < 0) || (a.rowIndex > (a.tbody.rows.length - 1))) return false;
        var colIndex = undefined;
        var found = false;
        var dir = (flag == 'next') ? 1 : -1;
        
        for (var i = 0; i < a.tbody.rows[a.rowIndex].cells.length; i++)
            if (a.tbody.rows[a.rowIndex].cells[i].className.contains('rsrchi')) {
                colIndex = i;
                break;
            }
        
        if (colIndex) {
            a.tbody.rows[a.rowIndex].cells[colIndex].className = 
                a.tbody.rows[a.rowIndex].cells[colIndex].className.nor('rsrchi').or('rsrch');
        
            var i = a.rowIndex;
            while (!found) {
                i += dir;
                if ((i < 0) || (i > (a.end - a.start))) break;
                if (a.tbody.rows[i].cells[colIndex].className.contains('rsrch')) {
                    a.tbody.rows[i].cells[colIndex].className = 
                        a.tbody.rows[i].cells[colIndex].className.nor('rsrch').or('rsrchi');
                    grid.setAttribute('searchrowindex', a.start + i);
                    a.searchRowIndex = a.start + i;
                    a.rowIndex = a.searchRowIndex - a.start;
                    found = true;
                    Roth.table.search.adjustScroll(id, flag);
                    break;
                }
            }
        }
        
        return found;
    },
    "adjustScroll": function(id, flag, gridId) {
        let a = id ? Roth.table.search.getAttrObj(id) : undefined;
        let ix = id ? a.rowIndex : _$(gridId).getAttribute('searchrowindex');
        if (!id) {
        	let pageSize = parseInt(_$(gridId).getAttribute('pagesize'));
        	if (pageSize > 0) {
	        	let pageIndex = parseInt(_$(gridId).getAttribute('pageindex'));
	        	ix = ix - (pageSize * (pageIndex - 1));
        	}
        }
        let tb = id ? a.tbody : getChild(_$(gridId), '1.0.0.1')
        if (ix === undefined || ix === null) return;
        let t = 0;
        for (let i = 0; i < ix; i++)
        	t += tb.rows[i].offsetHeight;
        let r = tb.rows[ix].offsetHeight;
        let div = getAncestorNode(tb, 'DIV');
        let st = div.scrollTop;
        let sh = div.clientHeight;
        let sb = st + sh;
        if (t < st || (t + r) > sb)
        	div.scrollTop = flag === 'next' ? t + r - sh : t;
    }
};
Roth.table.config = {
    "_onClick": function(e, which) {
        if (!e) e = window.event;
        //var grid = getAncestorNode(which, 'TABLE');
        var grid = getAncestorWithClass(which, 'rgrd');
        var pageSize = grid.getAttribute('pagesize');
        var s = parseInt(grid.getAttribute('save'));
        if (!pageSize) pageSize = 0;
/*
        var content = '<div style="position: relative; width: ' + (isMobile ? 'auto' : '450px') + ';">' +
                          '<div style="float: left; width: 250px;">' +
                              'Visible Fields:' +
                              Roth.createBreak('0.5rem') +
                              '<div id="_j_dataGrid_fields" style="height: 155px; overflow-y: auto; border: 1px solid silver;">' +
                                  Roth.table.config.getColumns(grid) +
							      Roth.createBreak() +
                              '</div>' +
                          '</div>' +
                          (isMobile ? Roth.createBreak('0.5rem') : '') + 
                          '<div style="float: left; width: 192px; margin-left: 8px;">' +
                              '<span style="float: left;">Rows Per Page: ' +
                              '<input type="text" id="_j_dataGrid_rpp" style="width: 30px;" value="' + pageSize + '"/>' +
                              '&nbsp;</span>&nbsp;' +
							  '<span style="cursor: pointer; color: blue; font-size: 1.5em;" onclick="Roth.table.config.showHelp(\'rpp\');">' +
							  '<i class="fa fa-question-circle"></i>' +
							  '</span>' +
                              Roth.createBreak('1rem') +
                              '<span style="float: left;">Remember Settings:&nbsp;</span>&nbsp;' +
							  '<span style="cursor: pointer; color: blue; font-size: 1.5em; margin-top: -0.3em; display: inline-block;" onclick="Roth.table.config.showHelp(\'mem\');">' +
							  '<i class="fa fa-question-circle"></i>' +
							  '</span>' +
                              Roth.createBreak('0.5rem') +
							  Roth.createRadioItem('r_dataGrid_0', '_j_dataGrid_mem', '0', s == 0, 'Only in current session') + '<br/>' +
							  Roth.createRadioItem('r_dataGrid_1', '_j_dataGrid_mem', '1', s == 1, 'In my profile manually') + '<br/>' +
							  Roth.createRadioItem('r_dataGrid_2', '_j_dataGrid_mem', '2', s == 2, 'In my profile automatically') + '<br/>' +
							  Roth.createRadioItem('r_dataGrid_3', '_j_dataGrid_mem', '3', false, 'Restore default settings') + '<br/>' +
							  Roth.createBreak() +
                          '</div>' +
                          Roth.createBreak('0.5rem') +
                          '<div style="width: 240px;">' +
                             Roth.createButton('OK', 'check', null, "var dlg = Roth.getDialog('dataGridSettings'); dlg.hide(); dlg.callback(); return false;") +
                             Roth.createButton('Cancel', 'ban', null, "Roth.getDialog('dataGridSettings').hide(); return false;") +
                          '</div>' +
                             Roth.createBreak() +
                      '</div>';
                      
        var dlg = Roth.getDialog('dataGridSettings');
        dlg.caption = //Roth.createIcon('gear') + ' Data Grid Settings';
                      Roth.createIconCaption('cog', 'Data Grid Settings');
        dlg.message = content;
        dlg.callback = function() { Roth.table.config.apply(grid.id); };
        dlg.modal = true;
        dlg.show();
*/
		let params = 'gridid=' + grid.id + '&fields=' + grid.getAttribute('fields') + '&rows=' + pageSize + '&remember=' + s;
		Roth.execDialog('gridconf', contextRoot + '/gridconf.jsp', params, 'Data Grid Settings', 'cog');
    },
    "getColumns": function(grid) {
        var result = '';
        var fields = grid.getAttribute('fields').split(',');
        fields.sort(function(a, b) { return (a.split('|')[1].toLowerCase() < b.split('|')[1].toLowerCase()) ? -1 : 1; });
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i].split('|');
            var color = (i % 2 == 0) ? 'white' : 'whitesmoke';
            var checked = (field[2] == 'true') ? ' checked' : '';
            result += '<div style="background-color: ' + color + ';">' +
						  Roth.createCheckbox('rGrd' + field[0], '_na', field[0] + '|', field[2] == 'true', field[1]) +
				       // Roth.createCheckbox = function(id, name, boolValues, checked, label) {
                      '</div>';
        }
        return result;
    },
    "showHelp": function(msg) {
        var dlg = Roth.getDialog('configHelp');
        dlg.caption = //Roth.createIcon('info') + ' Data Grid Settings Information';
                      Roth.createIconCaption('info', 'Data Grid Settings Information');
        if (msg == 'rpp')
            dlg.message = "For best performance, the number of rows per page should be less than 100<br/>" +
                          "(optimal is 20-40 -- the default is 20). &nbsp;More rows per page may be selected,<br/>" +
                          "however performace will degrade increasingly with the number of rows per<br/>" +
                          "page.<br/><br/>Selecting 0 rows per page will turn off paging.";
        else if (msg == 'mem')
            dlg.message = "<b>Only in current session</b> will only remember the settings " +
                          "in the current session.<br/>The next time you log in, the " +
                          "default settings will be restored.<br/><br/><b>In my profile " +
                          "manually</b> will remember the settings in the user profile, " +
                          "only<br/>when applied from this dialog.<br/><br/><b>In my profile " +
                          "automatically</b> will remember the settings in the user " +
                          "profile<br/>each time a change is made, like sorting, searching, " +
                          "or paging.<br/><br/><b>Restore defaults</b> will " +
                          "restore the defaults, and will switch afterward to<br/><b>Only " +
                          "in current session</b>.";
        dlg.message +=    Roth.createBreak('8px') +
                          '<div style="width: 70px; margin: 0 auto;">' +
                          Roth.createButton('OK', 'check', null, "var dlg = Roth.getDialog('configHelp'); dlg.hide(); return false;") +
                          '</div>' +
                          Roth.createBreak();
        dlg.show();
    },
    "apply": function(id, csrfToken) {
        var params = 'rows=' + _$v('_j_dataGrid_rpp'); 
        var fields = _$('_j_dataGrid_fields').childNodes;
        params += '&show=';
        var v = false;
        for (let i = 0; i < fields.length; i++) {
            let c = getChild(fields[i], '0.0');
            if (checked = c && c.value === 'true') {
                if (v) params += ',';
                params += c.parentNode.parentNode.getAttribute('fieldname');
                v = true; 
            }
        }
        //var mem = document.getElementsByName('___j_dataGrid_mem');
        //for (var i = 0; i < mem.length; i++)
        //    if (mem[i].checked) params += "&save=" + mem[i].value;
		params += '&save=' + _$v('_j_dataGrid_mem');
        tableAction(id, csrfToken, params);
    }
};
Roth.table.exportData = function(id, dataSource, type) {
	var grid = document.getElementById(id);
    var action = grid.getAttribute('action');
    var onRequest = dataSource.contains('requestScope.');
    var hasParams = action.contains('?'); 
    if (onRequest) action += (hasParams ? '&' : '?') + '__j_dataGrid_exportOverride=true';
    var path = action.substring(0, action.indexOf('/', 2));
    var i = document.getElementById('__j_dataGrid_export');
    if (i) document.body.removeChild(i);
    i = document.createElement('iframe');
    i.id = '__j_dataGrid_export'; 
    i.src = ((onRequest) ? action + '&' : path + '/Export?') +
            'dataSource=' + dataSource +
            '&output=' + type +
            '&columns=' + encodeURIComponent(grid.getAttribute('fields'));
    i.style.display = 'none';
    document.body.appendChild(i);
};
Roth.table.column = {
    "_onMouseDown": function(e) {
        if (!e) e = window.event;
        var which = (e.target) ? e.target : e.srcElement;
        if (which.innerHTML == '') return false;
        // If a dragtype hasn't been set, then we are probably just clicking.
        if (!which.dragtype) while (which.nodeName != 'TH') which = which.parentNode;
        // If we're moving a column, it is probably because the div tag inside
        // the th is being dragged.  If so, replace which with the div's parent
        // node, which is the th. 
        if ((which.nodeName == 'DIV') && (which.dragtype == 'move')) {
            which.parentNode.dragtype = 'move';
            which = which.parentNode;
        }
        
        which.mx = Roth.mouse.x;
        which.my = Roth.mouse.y;
        Roth.mouse.object = which;
        which.onmouseup = function(event) {
        	let e = event || window.event;
        	if (e.button > 0)
        		return;
            if (Roth.mouse.object && Roth.mouse.object.className.includes('sort')) {
                //var tableid = getAncestorNode(getAncestorNode(Roth.mouse.object, 'TABLE'), 'TABLE').getAttribute('id');
            	var tableid = getAncestorWithClass(getAncestorWithClass(Roth.mouse.object, 'rtbl'), 'rgrd').getAttribute('id');
                var order = (e.ctrlKey) ? 'A' : (e.shiftKey) ? 'D' : 'T';
                var params = 'sort=' + order + Roth.mouse.object.getAttribute('dataSource');
                tableAction(tableid, getValue(_$('csrfToken')), params);
            }
            Roth.mouse.object = null; 
        };
        
        if (which.dragtype == 'resize') {
            //if (which.edge == 'left') which = which.parentNode.cells[which.cellIndex - 1];
            var coord = getAbsCoord(which);
            var div = _$jadd('DIV', 'rthz');
            //_$rsed(div, null, null, coord.x + which.clientWidth - 3, coord.y + 1);
            _$rsed(div, null, null, coord.x + which.clientWidth - 5, coord.y + 1);
            div.th = which;
            //div.x = coord.x + which.clientWidth - 3;
            div.x = coord.x + which.clientWidth - 5;
            div.y = coord.y + 1;
            div.w = which.clientWidth;
            div.mx = Roth.mouse.x;
            div.my = Roth.mouse.y;
            Roth.mouse.object = div;
            div.onmouseup = function() {
                if (e.shiftKey) {
                    var col = findColGroup(div.th.parentNode.parentNode.parentNode).children[div.th.cellIndex];
                    if (parseInt(col.style.width) > 5) {
                        _$rsed(col, 5);
                        //col = getChild(getAncestorNode(getAncestorNode(div.th, 'TABLE'), 'TABLE'), '1.1.0.0.0.0.' + div.th.cellIndex);
                        col = getChild(getAncestorWithClass(getAncestorWithClass(div.th, 'rtbl'), 'rgrd'), '1.1.0.0.0.0.' + div.th.cellIndex);
                        _$rsed(col, 5);
                    }
                    else {
                        //var tbl = getChild(getAncestorNode(getAncestorNode(div.th, 'TABLE'), 'TABLE'), '1.1.0.0.0');
                    	var tbl = getChild(getAncestorWithClass(getAncestorWithClass(div.th, 'rtbl'), 'rgrd'), '1.1.0.0.0');
                        var w = div.th.scrollWidth;
                        for (var i = 0; i < tbl.rows.length; i++)
                            if (tbl.rows[i].cells[div.th.cellIndex].scrollWidth > w) 
                                w = tbl.rows[i].cells[div.th.cellIndex].scrollWidth;
                        _$rsed(col, w + 3);
                        //col = getChild(getAncestorNode(getAncestorNode(div.th, 'TABLE'), 'TABLE'), '1.1.0.0.0.0.' + div.th.cellIndex);
                        col = getChild(getAncestorWithClass(getAncestorWithClass(div.th, 'rtbl'), 'rgrd'), '1.1.0.0.0.0.' + div.th.cellIndex);
                        _$rsed(col, w + 3);
                    }
                }
                _$jdel(this); 
                Roth.mouse.object = null;
            };
            div.callback = function() {
                var xdiff = Roth.mouse.x - div.mx;
                var ydiff = Roth.mouse.y - div.my;
                _$rsed(div, null, null, div.x + xdiff, div.y + ydiff);
                //var col = getChild(getAncestorNode(div.th, 'TABLE'), '0.' + div.th.cellIndex);
                var col = getChild(getAncestorWithClass(div.th, 'rtbl'), '0.' + div.th.cellIndex);
                //findColGroup(div.th.parentNode.parentNode.parentNode).children[div.th.cellIndex];
                var offset = (!isIE) ? 1 : -7;
                var min = (isIE) ? -3 : 5;
                var newWidth = div.w + xdiff + offset; if (newWidth < min) newWidth = min;
                _$rsed(col, newWidth - ((isIE) ? 2 : 0));
                //col = getChild(getAncestorNode(getAncestorNode(div.th, 'TABLE'), 'TABLE'), '1.1.0.0.0.0.' + div.th.cellIndex);
                col = getChild(getAncestorWithClass(getAncestorWithClass(div.th, 'rtbl'), 'rgrd'), '1.1.0.0.0.0.' + div.th.cellIndex);
                _$rsed(col, newWidth - ((isIE) ? 2 : 0));
            };
        }
        else if (which.dragtype == 'move')
            which.callback = function() {
                if ((Math.abs(which.mx - Roth.mouse.x) > 5) ||
                    (Math.abs(which.my - Roth.mouse.y) > 5)) {
                    // Get the coordinates of the column header.
                    var coord = getAbsCoord(which);
                    // Get the indicator.
                    var ind = _$jadd('DIV', 'rthi rthid');
                    // Get the shadow.
                    var div = _$jadd('DIV', 'rths');
                    // Set the shadow dimensions.
                    _$rsed(div, which.clientWidth, which.clientHeight, coord.x + (Roth.mouse.x - which.mx), coord.y + (Roth.mouse.y - which.my));
                    // Set the indicator location.
                    _$rsed(ind, null, null, coord.x + (which.clientWidth / 2) - 8 + (isIE ? 1 : isFirefox ? -1 : 0), coord.y - 1 + (isFirefox ? -1 : 0));
                    // Tie the column header to the shadow.  
                    // These two variables denote the column header that the shadow is currently hovering over.
                    div.th = which;
                    div.thx = which.cellIndex;
                    
                    Roth.mouse.object = div;
                    div.x = parseInt(div.style.left);
                    div.y = parseInt(div.style.top);
                    div.mx = Roth.mouse.x;
                    div.my = Roth.mouse.y;
                    div.onmouseup = function() {
                        if (div.th) {
                            //var tbl = getAncestorNode(which, 'TABLE');
                            var tbl = getAncestorWithClass(which, 'rtbl');
                            var oldIndex = which.cellIndex;
                            moveColumn(tbl, oldIndex, div.thx);
                            //tbl = getChild(getAncestorNode(tbl, 'TABLE'), '1.1.0.0.0');
                            tbl = getChild(getAncestorWithClass(tbl, 'rgrd'), '1.0.0');
                            moveColumn(tbl, oldIndex, div.thx);
                        }
                        _$jdel(ind);
                        _$jdel(this); 
                        Roth.mouse.object = null;
                    };
                    div.callback = function() {
                        var xdiff = Roth.mouse.x - div.mx;
                        var ydiff = Roth.mouse.y - div.my;
                        _$rsed(div, null, null, div.x + xdiff, div.y + ydiff);
                        
                        if (_$rmin(which.parentNode)) {
                            ind.style.display = 'block';
                            var th = null;
                            var thx = null;
                            for (var i = 0; i < which.parentNode.cells.length; i++)
                                if (_$rmin(which.parentNode.cells[i])) {
                                    th = which.parentNode.cells[i];
                                    thx = i;
                                }
                            if (th && ((th.innerHTML == '') || (!th.className.contains('move')))) {
                                ind.style.display = 'none';
                                div.th = null;
                                div.thx = null;
                                return;
                            }
                            div.th = th;
                            div.thx = thx;
                            if (div.th) {
                                var c = getAbsCoord(div.th);
                                var o = (div.thx <= which.cellIndex) ? 0 : div.th.clientWidth + 1;
                                if (div.thx == which.cellIndex) {
                                    o = (which.clientWidth / 2);
                                    ind.className = ind.className.or('rthid');
                                }
                                else ind.className = ind.className.nor('rthid');
                                _$rsed(ind, null, null, c.x + o - 8 + (isIE ? 1 : isFirefox ? -1 : 0), null);
                            }
                        }
                        else {
                            ind.style.display = 'none';
                            div.th = null;
                            div.thx = null;
                        }
                    };
                }
            };
        return false;
    }
};

/*

Dialog generation function...

There are few scenarios in which a dialog will be used:

    1. The dialog is generated through a JSP tag.
    
    (or the dialog is generated through JavaScript as follows)
    
    2. The dialog is a message dialog 
       (displays a message with an OK button).
    3. The dialog is a confirmation dialog 
       (displays a message with OK and Cancel or Yes and No buttons).
    4. The dialog displays contents of a local DIV tag.
    5. The dialog displays the results of an AJAX call. 

*/

Roth.dialogs = new Object();
Roth.getDialog = function (id, url, parameters) {
    var dlg = null;
    
    if (this.dialogs[id]) {
        dlg = this.dialogs[id];
        dlg.url = url;
        dlg.parameters = parameters;
    }
    else {
        dlg = new JpDialog(id, url, parameters);
        this.dialogs[id] = dlg;
    }
    
    return dlg;
};
Roth.execDialog = function (id, url, params, caption, img, pid, sid, callback, nonmodal, enableScript, execonshow) {
	let args = id instanceof Object ? id : { id, url, params, caption, img, pid, sid, callback, nonmodal, enableScript, execonshow };
    if (!args.params || args.params.length == 0) args.params = '_dialogId=' + id;
    else args.params += '&_dialogId=' + args.id; 
    var dlg = Roth.getDialog(args.id, args.url, args.params);
    dlg.caption = (args.img && args.caption) ? Roth.createIconCaption(args.img, args.caption) 
                : (args.img) ? Roth.createIcon(args.img) : args.caption;
    dlg.onshow = function() {
	    Roth.getDialog('wait').hide();
	    if (args.pid) {
	        var i = document.getElementById(args.pid);
	        if (i && i.getAttribute('readonly') && args.sid) i = document.getElementById(args.sid);
	        if (i) { i.focus(); i.select(); }
	    }
	    initQuill(args.id);
	    initTinyMCEeditor({containerId: args.id});
	    if (args.execonshow) args.execonshow();
    };
    //dlg.onhide = function() {};
    dlg.modal = !args.nonmodal;
    dlg.callback = args.callback;
    dlg.enableScript = args.enableScript;
    dlg.show();
    return dlg;
};
Roth.table.open = function(tableid, action, type, dialogCaption) {
	var sendparams = type != 'add';
    var params = null;
    var caption = type.substr(0, 1).toUpperCase() + type.substr(1);//(type == 'open') ? "Open" : (type == 'view') ? "View" : (type = 'edit') ? 'Edit' : 'Add';
	var img = type == 'add' ? 'plus' : type == 'open' ? 'folder-open' : type == 'view' ? 'eye' : type;
    //if (sendparams) {
        var selectedRow = getSelectedRow(tableid);
        if (sendparams && !selectedRow) {
            Roth.getDialog('alert').alert('Please first select a row, then click "' + caption + '".');
            return;
        }
        if (selectedRow) params = selectedRow.getAttribute('key');
		let diacap = dialogCaption && dialogCaption !== '' ? dialogCaption.split('|') : undefined;
		let suffix = !diacap ? '' : type === 'add' || diacap.length === 1 ? diacap[0] : diacap[1];
		if (params) {
			let ps = params.split('&');
			ps.forEach(e => {
				let es = e.split('=');
				suffix = suffix.replace('{' + es[0] + '}', type === 'add' ? '' : es[1]);
			});
		}
        // This next line is to encode the params so that an edit function can
        // still access them in the case of an add operation, for purposes of 
        // determining parentage in a heirarchical situation.
        if (!sendparams && params) params = '_params=' + encodeURIComponent(params);
    //}
	caption += ' ' + suffix;
    var table = document.getElementById(tableid);
    var callback = function(request) {
    	Roth.ajax.htmlCallback(request, table.getAttribute('containerId'));
        Roth.getDialog('edit' + tableid).hide();
    };
    Roth.getDialog('wait').wait();
    var dlg = Roth.execDialog('edit' + tableid, action, params, caption, img, null, null, callback);
    //dlg.onshow = function() { Roth.getDialog('wait').hide(); };
    dlg.type = type;
    var onOpen = table.getAttribute('onopen');
	var f = (!onOpen) ? null : new Function(onOpen);
	if (f) dlg.onshow = f; 
};
Roth.table.confirmDelete = function(tableid, action, confirmed) {
    var selectedRow = getSelectedRow(tableid);
    if (!selectedRow) {
        Roth.getDialog('alert').alert('Please first select a row, then click "Delete".');
        return;
    }
    if (!confirmed) {
        var dlg = Roth.getDialog('deleteConfirm');
        //dlg.button = which;
        dlg.callback = function() { Roth.table.confirmDelete(tableid, action, true); }; 
        dlg.confirm('Are you sure you wish to delete this row?');
        return;
    }
    // Execute an AJAX call here, passing the params.
    selectedRow.callback = function() {
        if (this.readyState == 4) {
            if (this.status == 200) {
                var containerId = document.getElementById(tableid).getAttribute('containerId');
                var container = document.getElementById(containerId);
                if (container) container.innerHTML = this.responseText;
                else Roth.getDialog('error').error('The request processed, but the response was unable to be displayed due to an invalid reference.');
                var onDelete = _$(tableid).getAttribute('ondelete');
            	var f = (!onDelete) ? null : new Function(onDelete);
            	if (f) f();
            }
            else if (this.status == 401)
            	doAuthentication(false);
            else if (this.status == 403)
            	Roth.getDialog('error').error("Access to the requested resource was denied.");
            else Roth.getDialog('error').error(this.responseText);
        }
    };
    executeAjax({callback: selectedRow.callback, method: 'GET', url: action, parameters: selectedRow.getAttribute('key'), data: selectedRow});
};
Roth.table.htmlAction = function(tableid, action, sendparams) {
    var params = null;
    var selectedRow = undefined;
    if (sendparams) {
        selectedRow = getSelectedRow(tableid);
        if (!selectedRow) {
            Roth.getDialog('alert').alert('Please first select a row, then try again.');
            return;
        }
        params = selectedRow.getAttribute('key');
    }
    // Execute an AJAX call here, passing the params.
    var callback = function() {
        var containerId = document.getElementById(tableid).getAttribute('containerId');
        Roth.ajax.htmlCallback(this, containerId);
    };
    executeAjax({callback: callback, method: 'GET', url: action, parameters: params, data: selectedRow});
};
/* Dialogs */

Roth._$dlg_md = function(which) {
    which.onmouseup = function() { Roth._$dlg_mu(this); };
    var dlg = which.parentNode;
    while (dlg.nodeName != 'DIV') dlg = dlg.parentNode;
    Roth.mouse.object = dlg;
    dlg.x = parseInt(dlg.style.left);
    dlg.y = parseInt(dlg.style.top);
    dlg.mx = Roth.mouse.x;
    dlg.my = Roth.mouse.y;
    
    dlg.callback = function() {
        var xdiff = Roth.mouse.x - dlg.mx;
        var ydiff = Roth.mouse.y - dlg.my;
        
        dlg.style.left = (dlg.x + xdiff) + 'px';
        dlg.style.top = (dlg.y + ydiff) + 'px';
    };
};
Roth._$dlg_mu = function(which) {
    Roth.mouse.object = null;
};

/* Dialog Stack */
Roth.dlgStack = new Object();
Roth.dlgStack.count = 0;
Roth.dlgStack.push = function(dlg) {
	if (this.top(dlg)) return; // If it's already in the stack, then bring to the top, but don't push.
	this[this.count] = dlg; 
	this.count++; 
};
Roth.dlgStack.pop = function(dlg) {
	if (this.top(dlg) == null) return;  // If it's not even in the stack, don't execute.
	this.count--; 
	var result = this[this.count];
	delete this[this.count];
	return result; 
};
Roth.dlgStack.find = function(dlg) {
	for (var i = 0; i < this.count; i++)
		if (this[i] == dlg)
			return i;
	return null;
};
Roth.dlgStack.top = function(dlg) {
	var index = this.find(dlg);
	if (index != null) {
		this[this.count] = this[index];
		for (var i =  index; i < this.count - 1; i++)
			this[i] = this[i + 1];
		delete this[this.count];
	}
	return index;
};
Roth.dlgStack.hide = function() {
	if (this.count > 0)
		this[this.count - 1].hide();
} 
addEvent(window, 'keydown', function(event) { 
	var e = event || window.event;
	if (e.keyCode == 27) { // Escape will close top dialog.
		if ((window == window.top) || (Roth.dlgStack.count > 0)) 
			Roth.dlgStack.hide();
		else
			window.top.Roth.dlgStack.hide();
	}
});

/* BEGIN JpDialog */
function JpDialog(id, url, parameters) {
    if (!id) throw 'An id must be specified.';
    this.element = document.getElementById(id);
    this.id = id;
    this.url = (url) ? url : (this.element) ? this.element.getAttribute('url') : null;
    this.parameters = (parameters) ? parameters : (this.element) ? this.element.getAttribute('parameters') : null;
    this.closable = true; // Whether a close button appears in the title bar.
    this.movable = true; // Whether the dialog is movable.
    this.infront = true; // Make sure this dialog is in front when displaying.
    this.modal = false;
}
JpDialog.prototype.createDialog = function() {
    /*var tf = !this.infront ? ''
            : "document.body.appendChild(Roth.getDialog('" + this.id + "').element); ";
    var md = !this.movable ? ' onmousedown="' + tf + 'return false;"'
           : ' onmousedown="' + tf + 'Roth._$dlg_md(this); return false;" style="cursor: move;"';*/
	var md = ' onmouseover="initDialogHandlers(this);"';
    var e = document.createElement('DIV');
    e.id = this.id;
    e.className = 'dialog';
    e.style.left = '0px';
    e.style.top = '0px';
    e.style.zIndex = JpDialog.maxIndex + 2;
    JpDialog.maxIndex += 2;
    e.style.display = 'none';
    
    /*
    e.innerHTML = '<table class="jdlg_tbl">' +
                      '<tr' + md + '><td class="jdlg_tl"><div id="' + this.id + '_caption" class="jdlg_tr">' + this.caption + '</div></td></tr>' +
                      '<tr><td class="jdlg_ml"><div class="jdlg_mr">' +
                          '<div id="' + this.id + '_content" class="main_content">' +
                          '</div>' +
                      '</div></td></tr>' +
                      '<tr><td class="jdlg_bl"><div class="jdlg_br"></div></td></tr>' +
                  '</table>';
    */
    e.innerHTML = '<div class="dialog-header" ' + md + '>' +
                      '<div id="' + this.id + '_caption" class="caption">' + this.caption + '</div>' +
                      '<div class="close"></div>' +
                      '<div class="rbreak"></div>' +
                  '</div>' +
                  '<div id="' + this.id + '_content" class="dialog-body">' +
                  '</div>' +
                  '<div class="dialog-footer">' +
                      '<div class="rbreak"></div>' +
                  '</div>';
                  
    //var t = document.createElement('TABLE');
    //t.className = 'jdlg_tbl';
    document.body.appendChild(e);
        
    return e;
};
JpDialog.prototype.ajaxCallback = function() {
    // Note: "this" does not refer to the JpDialog instance here.
    // Instead, it refers to the AJAX request object.  This function
    // references the JpDialog instance through "this.data".
    if (this.readyState == 4) {
    	Roth.getDialog('wait').hide();
        if (this.status == 200) {
            if (this.responseText.contains('<!--LOGIN-->')) login();
            else {
                this.data.content.innerHTML = this.responseText;
                this.data.display();
            }
        }
        else if (this.status == 401)
        	doAuthentication(false);
        else if (this.status == 403)
        	Roth.getDialog('error').error("Access to the requested resource was denied.");
        else Roth.getDialog('error').error(this.responseText);
    }
};
JpDialog.prototype.position = function(x, y) {
	this.x = x;
    this.y = y;
    if (!this.x || !this.y) {
        var winDim = getWindowDimensions();
        var w = this.element.clientWidth;
        var h = this.element.clientHeight;
        if (winDim.height > document.body.clientHeight) winDim.height = document.body.clientHeight;
        this.x = (winDim.width - w) / 2 + winDim.scrollX;
        this.y = (winDim.height - h) / 2 + winDim.scrollY;
        if (this.y < 0) this.y = 0;
    }
    this.element.style.left = this.x + 'px';
    this.element.style.top = this.y + 'px';
};
JpDialog.prototype.display = function() {
	if (this.element.style.zIndex < JpDialog.maxIndex) {
		JpDialog.maxIndex += 2;
		this.element.style.zIndex = JpDialog.maxIndex;
	}
    if (this.modal) {
        var mdiv = document.getElementById('__jdlg_modal_' + this.id); 
        if (!mdiv) {
            mdiv = document.createElement('DIV');
            mdiv.id = '__jdlg_modal_' + this.id;
            mdiv.className = 'jmodal';
            mdiv.style.zIndex = this.element.style.zIndex - 1;
        }
        document.body.appendChild(mdiv);
    }
    
    if (this.infront) document.body.appendChild(this.element);
    this.element.style.display = 'block';
    this.element.style.visibility = 'visible';
    if (isMobile) {
		var winDim = getWindowDimensions();
		this.element.style.left = '0px';
	    this.element.style.top = '0px';
	    
	    
		//this.element.style.width = '100%'; //winDim.width + 'px';
		//this.element.style.maxHeight = '100%'; //'calc(' + winDim.height + 'px - 0.4em)';
	}
    else {
	    if (!this.x || !this.y) {
	        var winDim = getWindowDimensions();
	        var bdim = dim(document.body);
	        var edim = dim(this.element);
	        var w = edim.width;
	        var h = edim.height;
	        //var w = this.element.clientWidth;
	        //var h = this.element.clientHeight;
	        if (winDim.height > bdim.height) winDim.height = bdim.height;
	        //if (winDim.height > document.body.clientHeight) winDim.height = document.body.clientHeight;
	        this.x = (winDim.width - w) / 2 + winDim.scrollX;
	        this.y = (winDim.height - h) / 2 + winDim.scrollY;
	        if (this.x < 0) this.x = 0;
	        if (this.y < 0) this.y = 0;
	    }
	    this.element.style.left = this.x + 'px';
	    this.element.style.top = this.y + 'px';
    }
    if (this.enableScript) enableScript(this.id + '_content', this.enableScript.split(','));
    if (this.onshow) this.onshow();
};
JpDialog.prototype.show = function(x, y, nostack) {
    var el = null;
    if (this.element && !this.element.className.contains('dialog'/*'jdlg'*/)) {
        this.element.id = 'j_' + this.element_id;
        el = this.element;
    }
    if (!this.element) this.element = this.createDialog();
    this.content = document.getElementById(this.element.id + '_content');
    if (el) this.content.appendChild(el);
    if (this.message) this.content.innerHTML = this.message;
    if (this.closable) this.caption += ' <div class="close" title="Close" onmousedown="if (isIE) window.event.cancelBubble = true;" onclick="Roth.getDialog(\'' + this.id + '\').hide();">&times;</div>';
    var cap = document.getElementById(this.id + '_caption');
    if (cap) cap.innerHTML = this.caption;
    this.x = x;
    this.y = y;
    if (this.url)
        executeAjax({callback: this.ajaxCallback, method: 'GET', url: this.url, parameters: this.parameters, data: this});
    else
        this.display();
    if (!nostack) 
    	Roth.dlgStack.push(this);
};
JpDialog.prototype.hide = function() {
    if (this.modal) {
        var mdiv = document.getElementById('__jdlg_modal_' + this.id);
        if (mdiv) document.body.removeChild(mdiv);
    }
    /* This next line was necessary to overcome a strange bug in seemingly
       all browsers that would prevent an AJAX tab change to work propery
       after a sub-dialog and it's parent were closed and then reopened. */
    // if (this.url && this.content) this.content.innerHTML = '';
    if (this.content) this.content.innerHTML = '';
    
	var tel = this.element;
	if (tel) {
		setTimeout(function() {
	    	tel.style.display = 'none';
			tel.style.animation = '';
		}, 100);
		tel.style.animation = 'scale-out 0.125s ease-in';
	}
    if (this.onhide) this.onhide();
    Roth.dlgStack.pop(this);
};
JpDialog.prototype.messageDialog = function(type, caption, iconName, message, x, y) {
    this.caption = Roth.createIconCaption(iconName, caption);
    var cb = (this.callback) ? "dlg.callback(); " : "";
	this.url = type == 1 ? "#[contextRoot]/alert.jsp" : "#[contextRoot]/confirm.jsp";
	this.onshow = function() {
	    getChild(_$(this.id + '_content'), 0).innerHTML = message;
    };
	this.show(x, y);
};
JpDialog.prototype.info = function(message, x, y) {
    this.messageDialog(1, 'INFORMATION', 'info', message, x, y);
};
JpDialog.prototype.alert = function(message, x, y) {
    this.messageDialog(1, 'ALERT', 'exclamation', message, x, y);
};
JpDialog.prototype.warning = function(message, x, y) {
    this.messageDialog(1, 'WARNING', 'exclamation-triangle', message, x, y);
};
JpDialog.prototype.error = function(message, x, y) {
    this.messageDialog(1, 'ERROR', 'exclamation-circle', message, x, y);
};
JpDialog.prototype.confirm = function(message, x, y) {
    this.messageDialog(2, 'CONFIRM', 'question', message, x, y);
};
JpDialog.prototype.flash = function(message, x, y, timeout) {
    this.caption = Roth.createIconCaption('info', 'INFORMATION');
    this.message = message;
    this.show(x, y);
    if (!timeout) timeout = 3000;
    if (timeout > 0) setTimeout("Roth.getDialog('" + this.id + "').hide();", timeout);
};
JpDialog.prototype.wait = function(message, x, y) {
    this.caption = Roth.createIconCaption('wait', 'PLEASE WAIT');
    this.message = message ? message : 'Please wait while your request is processed...';
    this.closable = false;
    this.show(x, y, true);
};
JpDialog.prototype.file = function(caption, button, icon, action, inputLabel, dataSource, accept, submitType, callback, x, y) {
	this.caption = Roth.createIconCaption(icon, caption);
	//var parameters = '?action=' + action + '&inputLabel=' + inputLabel + '&dataSource=' + dataSource + '&accept=' + accept + '&submitType=' + submitType;
	//this.message = '<iframe id="fileframe" src="/Roth/_file.jsp' + parameters + '" style="border: none;"></iframe>';
	var onclick = action.startsWith('javascript:') ? action.nor('javascript:') : 'submitForm(this)';
	let type = 'fi' + 'le';
	/*
	var acceptAttr = (accept) ? "accept=\"" + accept + "\"" : "";
	this.message = '<form action="' + action + '" method="POST" ajax="AJAX" enctype="multipart/form-data" onajax="' + callback + '">' +    
                       '<div>' + inputLabel + '<br/>' +
                       '<input id="' + this.id + 'File" type="' + type + '" name="' + dataSource + '" size="30" ' + acceptAttr + ' multiple/>' +
                       '</div>' +
                       '<div class="rbreak" style="height: 8px;"></div>' +
                       '<a href="#" onclick="' + onclick + '; return false;" class="roth-button"><div><span>' + button + '</span><span class="fa fa-' + icon + '" style="margin-left: 4px;"></span></div></a>' +
                       '<a href="#" onclick="Roth.getParentDialog(this).hide(); return false;" class="roth-button"><div><span>Cancel</span><span class="fa fa-ban" style="margin-left: 4px;"></span></div></a>' +
                   '</form>' +
                   '<div class="rbreak"></div>';
    */
    this.url = contextRoot + `/upload.jsp?button=${button}&icon=${icon}&action=${action}&inputLabel=${inputLabel}&dataSource=${dataSource}&accept=${accept}`; 
	this.modal = true;
	this.show(x, y, true);
}
JpDialog.maxIndex = 20;
function initDialogHandlers(which) {
	which.onmousedown = function() {
		var dlg = which.parentNode;
		while (dlg.nodeName != 'DIV') dlg = dlg.parentNode;
		if (dlg.style.zIndex < JpDialog.maxIndex) {
			JpDialog.maxIndex += 2;
			dlg.style.zIndex = JpDialog.maxIndex;
			var mdiv = _$('__jdlg_modal_' + dlg.id);
			if (mdiv) 
				mdiv.style.zIndex = dlg.style.zIndex - 1;
		}
		Roth.mouse.object = dlg;
		dlg.x = parseInt(dlg.style.left);
		dlg.y = parseInt(dlg.style.top);
		dlg.mx = Roth.mouse.x;
		dlg.my = Roth.mouse.y;
		dlg.callback = function() {
			var xdiff = Roth.mouse.x - dlg.mx;
			var ydiff = Roth.mouse.y - dlg.my;
			dlg.style.left = (dlg.x + xdiff) + 'px';
			dlg.style.top = (dlg.y + ydiff) + 'px';
		};
	};
	which.onmouseup = function() {
		Roth.mouse.object = null;
	};
	which.onmouseover = null;
}
/* END JpDialog */
function tableAction(tableId, csrfToken, params) {
    var table = document.getElementById(tableId);
    if (!table) return;
    
    var cols = getChild(table, '0.0.0.0');
    var hdrs = getChild(table, '0.0.0.1.0');
    var ordparam = '&order=';
    var sizeparam = '&size=';
   
    for (var i = 0; i < (cols.children.length - 1); i++) {
        if (i > 0) { ordparam += ','; sizeparam += ','; }
        ordparam += getChild(hdrs, i).getAttribute('datasource');
        sizeparam += getChild(hdrs, i).getAttribute('datasource') + '.'
                   + getChild(cols, i).style.width;
    }
    var action = table.getAttribute('action');
    var parameters = (!params) ? ''
                   : '_csrf-token=' + encodeURIComponent(csrfToken) +
                     '&__j:dataGrid.id=' + table.getAttribute('paramid') +
                     '&__j:dataGrid.params=' + encodeURIComponent(params + ordparam + sizeparam);
    var containerId = table.getAttribute('containerid');
    if (!containerId) {
        table.ajaxCallback = function() {
            if (this.readyState == 4) {
                var state = (this.status == 200) ? 'response' : 'error';
                var handler = this.data.getAttribute('onajax' + state);
                var error = "No " + state + " event hander was specified for this callback.";
                var f = (!handler) ? null : new Function('request', handler);
                if (f) f(this); else throw error;
            }
        };
    }
    else {
        Roth.getDialog('wait').wait();
        let callback = undefined;
        if (params.contains('search') && !params.contains('clear'))
        	callback = function() {
        		let flag = params.split('=')[1].split('.')[0];
        		Roth.table.search.adjustScroll(null, flag, tableId);
        	}
        table.ajaxCallback = function() { Roth.ajax.htmlCallback(this, containerId, callback); };
    }
    executeAjax({callback: table.ajaxCallback, method: 'GET', url: action, parameters: parameters, data: table});
}

Roth.getParentDialog = function(which) {
    //while (which && (which.className != 'jdlg')) which = which.parentNode;
    //if (which && (which.className == 'jdlg')) return Roth.getDialog(which.id);
	while (which && (which.className != 'dialog')) which = which.parentNode;
    if (which && (which.className == 'dialog')) return Roth.getDialog(which.id);
};

Roth.tabset = {
    "getPage": function(which, params, callback) {
        if (!which.action) return;
        //var callback = function() { Roth.ajax.htmlCallback(this, this.data.page, callback); };
        //executeAjax({callback: }callback, url: which.action, parameters: params, data: which});
        Roth.ajax.htmlAction(which.page, which.action, params, null, null, callback);
        //containerId, action, params, data, message, callback)
    },
    "newTab": function(tabsetId, pageId, caption, iconName, selected) {
    	var sel = selected ? ' selected' : '';
    	var tab = '<li class="left' + sel + '" page="' + pageId + '" onmouseup="rtmu(this)">' +
                      '<div class="right">' +
                          '<span class="fa fa-' + iconName + '" style="margin-right: 4px;"></span>' +
                          '<span style="float: left;">' + caption + '</span>' +
                      '</div>' +
                  '</li>';
    	var tabset = _$(tabsetId);
    	var ul = getChild(tabset, '0');
    	ul.innerHTML += tab;
    },
    "newPage": function(containerId, pageId, selected, content) {
    	var sel = selected ? '' : 'style="display: none;"';
    	if (!content) content = '';
    	var page = '<div id="' + pageId + '" ' + sel + '>' + content + '</div>';
    	_$(containerId).innerHTML += page;
    },
    "getTabCount": function(tabsetId) {
    	var tabset = _$(tabsetId);
    	var ul = getChild(tabset, '0');
    	return ul.children.length;
    },
	"doSelect": function(tabsetId, pageId, href) {
		let tabset = _$(tabsetId);
		//let page = _$(pageId);
		let tabs = getChild(tabset, 0).children;
		for (let i = 0; i < tabs.length; i++) {
			if (tabs[i].nodeName !== 'LI')
				continue;
			let pid = tabs[i].getAttribute('page');
			let page = _$(pid);
			page.style.display = pid === pageId ? 'block' : 'none'; 
		}
		if (!href.endsWith('#'))
			Roth.ajax.htmlAction(pageId, href, null, null, null, tabset.onselect);
		else if (tabset.onselect)
			tabset.onselect();
	},
	"getSelected": function(which) {
		let rads = document.getElementsByName('_' + which.id);
		let pageId = undefined;
		let href = undefined;
        for (let i = 0; i < rads.length; i++)
        	if (rads[i].checked) {
        		pageId = rads[i].nextSibling.getAttribute('page');
        		href = rads[i].nextSibling.children[0].getAttribute('href');
        	}
		return {pageId, href}; 
	},
	"setSelected": function(which, pageId) {
		let rads = document.getElementsByName('_' + which.id);
        for (let i = 0; i < rads.length; i++)
        	if (pageId === rads[i].nextSibling.getAttribute('page'))
        		getChild(rads[i].nextSibling, 0).click();
	}
};
function getTextFromBlob(blob) {
	let reader = new FileReader();
	reader.onload = function() { reader.textResult = reader.result; }
	reader.readAsText(blob);
	return reader.textResult;
}
Roth.ajax = {
	"baseCallback": function(request, errorMessage, blob) {
		let args = request instanceof XMLHttpRequest ? { request, errorMessage, blob } : request; 
		// baseCallback processes error states on AJAX calls.
		// Return true when readyState == 4 (done) and status == 200 (OK), false otherwise.  
    	if (args.request.readyState == 4) {
    		Roth.getDialog('wait').hide();
			if (args.request.status == 400) {
				updateCsrf();
	        	Roth.getDialog('info').flash("Session validation failed and has been updated.<br/>Please resubmit your last request."); /* Please reopen form and try again. */
	        }
	        else if (args.request.status == 401)
	        	doAuthentication(false);
	        else if (args.request.status == 403)
	        	Roth.getDialog('error').error("Access to the requested resource was denied.");
	        else if (args.request.status != 200) {
	        	if (!args.blob || args.errorMessage)
	        		Roth.getDialog('error').error(args.errorMessage ? args.errorMessage : args.request.responseText);
	        	else {
	        		let reader = new FileReader();
		        	reader.onload = function() { Roth.getDialog('error').error(reader.result); }
		        	reader.readAsText(args.request.response);
	        	}
	        }
	        else {
				return true;
			}
    	}
    	return false;
    },	
    "baseAction": function(action, params, data, callback, execCallback, responseType) {
		let args = action instanceof Object ? action : { action, params, data, callback, execCallback, responseType };
    	Roth.getDialog('wait').wait();
        executeAjax({
			callback: execCallback, 
			method: 'GET', 
			url: args.action, 
			parameters: args.params, 
			data: args.data, 
			responseType: args.responseType
		});
    },
    "htmlCallback": function(request, containerId, callback, errorMessage, closeParent) {
		let args = request instanceof XMLHttpRequest ? { request, containerId, callback, errorMessage, closeParent } : request;
        if (Roth.ajax.baseCallback(args.request, args.errorMessage)) {
            var container = document.getElementById(args.containerId);
            if (args.request.responseText.contains('<!--LOGIN-->')) login();
            else container.innerHTML = args.request.responseText;
            if (args.callback) args.callback();
            if (args.closeParent) Roth.getParentDialog(args.closeParent)?.hide();
            //fontawesome.dom.i2svg();
        }
    },
    "htmlAction": function(containerId, action, params, data, message, callback) {
		let args = containerId instanceof Object ? containerId : { containerId, action, params, data, message, callback };
        var execCallback = function() { 
            Roth.ajax.htmlCallback(this, args.containerId, args.callback);
            if ((this.status == 200) && message) Roth.getDialog('notify').flash(message);
        };
    	Roth.ajax.baseAction(args.action, args.params, args.data, args.callback, execCallback);
    },
    "jsonCallback": function(request, callback, errorMessage) {
		let args = request instanceof XMLHttpRequest ? { request, callback, errorMessage } : request;
        if (Roth.ajax.baseCallback(args.request, args.errorMessage)) {
        	if (!args.callback)
        		throw new Exception('No callback was supplied.');
        	else
        		args.callback(JSON.parse(request.responseText));
        }
    },
    "jsonAction": function(action, params, data, callback) {
		let args = action instanceof Object ? action : { action, params, data, callback };
    	var execCallback = function() { 
            Roth.ajax.jsonCallback(this, args.callback);
        };
        Roth.ajax.baseAction(args.action, args.params, args.data, args.callback, execCallback);
    },
    "fileCallback": function(request) {
        if (Roth.ajax.baseCallback(request, null, true)) {
        	let filename = request.getResponseHeader('Content-Disposition').replace('attachment; filename=', '').replace(/"/g, '');
        	let mimetype = request.getResponseHeader('Content-Type');
        	Roth.file.save(request.response, filename, mimetype);
        }
    },
    "fileAction": function(action, params, data) {
    	var execCallback = function() { 
            Roth.ajax.fileCallback(this);
        };
    	Roth.ajax.baseAction(action, params, data, null, null, 'blob');
    },
    "messageCallback": function(request, callback, caption, iconName, timeout) {
        if (Roth.ajax.baseCallback(request, request.responseText)) {
        	if (caption)
           		Roth.getDialog('msg').messageDialog(1, caption, iconName, request.responseText);
           	else if (timeout)
           		Roth.getDialog('message').flash(request.responseText, null, null, timeout);
           	else 
           		Roth.getDialog('message').alert(request.responseText);
        	if (callback) callback();
        }
    },
    "messageAction": function(action, params, data, caption, iconName) {
        var execCallback = function() { 
            Roth.ajax.messageCallback(this, null, caption, iconName);
        };
        Roth.ajax.baseAction(action, params, data, null, execCallback);
    },
    "selectUpdate": (args) => {
		let {action, params, selectId, callback} = args
		Roth.ajax.jsonAction(action, params, null, (result) => {
			let select = _$(selectId);
			if (select) {
				let newOptions = '<option selected="selected"></option>';
				for (var key in result)
					newOptions += '<option value="' + key + '">' + result[key] + "</option>";
				select.innerHTML = newOptions;
			}
			if (callback)
				callback();
		});
	}
};
Roth.mask = {
	"isDigit": function(keyCode, shiftKey) {
		// 48..57 => '0'..'9' (number row, when !shiftKey)
		// 96..105 => '0'..'9' (numeric keypad)
		return (!shiftKey && (keyCode >= 48) && (keyCode <= 57)) || ((keyCode >= 96) && (keyCode <= 105));
	},
	"isAlpha": function(keyCode, valid) {
		// 65..90 => 'A'..'Z' (not case sensitive)
		return (keyCode >= 65) && (keyCode <= 90) && (!valid || (valid.indexOf(keyCode) > -1));
	},
	"isPunct": function(keyCode, shiftKey, valid) {
		var punct = {  '48': { 'true': ')' },
				       '49': { 'true': '!' },
				       '50': { 'true': '@' },
				       '51': { 'true': '#' },
				       '52': { 'true': '$' },
				       '53': { 'true': '%' },
				       '54': { 'true': '^' },
				       '55': { 'true': '&' },
				       '56': { 'true': '*' },
				       '57': { 'true': '(' },
				       '59': { 'true': '"', 'false': "'"},
				      '106': { 'true': '*', 'false': '*'},
				      '107': { 'true': '+', 'false': '='},
				      '109': { 'true': '_', 'false': '-'},
				      '110': { 'false': '.'},
				      '111': { 'true': '/', 'false': '/'},
				      '173': { 'true': '_', 'false': '-'}, // Firefox
				      '188': { 'true': '<', 'false': ','},
				      '189': { 'true': '_', 'false': '-'}, // Chrome
				      '190': { 'true': '>', 'false': '.'},
				      '191': { 'true': '?', 'false': '/'},
				      '192': { 'true': '~', 'false': '`'},
				      '219': { 'true': '{', 'false': '['},
				      '220': { 'true': '|', 'false': '\\'},
				      '221': { 'true': '}', 'false': ']'},
				      '222': { 'true': '"', 'false': "'"}
				      };
		return ((shiftKey && (keyCode >= 48) && (keyCode <= 57)) ||
				(keyCode == 59) || 
		        ((keyCode >= 106) && (keyCode <= 107)) ||
		        ((keyCode >= 109) && (keyCode <= 111)) ||
                (keyCode == 173) ||
		        ((keyCode >= 188) && (keyCode <= 192)) ||
		        ((keyCode >= 219) && (keyCode <= 221))) &&
		       (!valid || (valid.indexOf(punct[keyCode][shiftKey]) > -1));
	},
	"isControl": function(keyCode) { return keyCode < 32; }, // 8 (Bksp), 9 (Tab), 13 (Enter)
	"isCursor": function(keyCode) {
		// 45 (Ins), 46 (Del), 
		// 33 (PgUp), 34 (PgDn), 35 (End), 36 (Home), 37 (Left), 38 (Up), 39 (Right), 40 (Dn)
		return ((keyCode >= 33) && (keyCode <= 40)) || ((keyCode >= 45) && (keyCode <= 46));
	},
	"isFKey": function(keyCode) { return (keyCode >= 112) && (keyCode <= 123); },
	"isSelected": function(input) { var s = _getSelection(input); return s.selectionStart < s.selectionEnd; },
	"numberMask": function(e, numericOnly, maxLength, precision, signed) {
		if (window.event) e = window.event;
		if (e.ctrlKey || e.altKey) return true;
	    var src = (e.target) ? e.target : e.srcElement;
	    if (maxLength && (src.value.length >= maxLength) && (e.keyCode > 46)) 
	    	return false;
	    var cp = getCaratPos(src);
	    var minus = this.isPunct(e.keyCode, e.shiftKey, ['-']);
	    var mcont = src.value.contains('-');
	    if (minus && mcont || (cp > 0))
	    	minus = false;
	    var point = this.isPunct(e.keyCode, e.shiftKey, ['.']);
	    var pcont = src.value.contains('.');
	    if (point && pcont)
	    	point = false;
	    if ((point && precision < 1) || (minus && !signed) || ((point || minus) && numericOnly))
	    	return false;
	    var result = this.isControl(e.keyCode) ||
				     this.isCursor(e.keyCode) ||
				     this.isDigit(e.keyCode, e.shiftKey) || 
				     this.isSelected(src) ||
				     (minus && signed) ||
				     (point && precision > 0);
	    if (!this.isControl(e.keyCode) && !this.isCursor(e.keyCode) && result && precision && pcont && (cp > src.value.indexOf('.'))) {
	    	if (src.value.indexOf('.') < src.value.length - precision)
	    		result = false;
	    }
	    return result;
	},
	"alphaNumberMask": function(e) {
		if (window.event) e = window.event;
		if (e.ctrlKey || e.altKey) return true;
	    var src = (e.target) ? e.target : e.srcElement;
	    var digit = this.isDigit(e.keyCode, e.shiftKey);
	    var alpha = this.isAlpha(e.keyCode, [66, 75, 77, 84]); // ['B', 'K', 'M', 'T']
	    var dot = isPunct(e.keyCode, e.shiftKey, ['.']);
	    var minus = isPunct(e.keyCode, e.shiftKey, ['-']);
	    var cont = src.value.containsAny(['B', 'K', 'M', 'T']);
	    if (alpha && cont)
	    	alpha = false; // Don't allow a second alpha character.
		if (digit && cont && (getCaratPos(src) == src.value.length))
			digit = false;
		if (dot && src.value.containsAny(['.'])) dot = false;
		if (minus && src.value.containsAny(['-'])) minus = false;
	    return this.isControl(e.keyCode) ||
	    	   this.isCursor(e.keyCode) ||
	           digit ||
	           alpha ||
	           dot || minus;
	},
	"interpAlphaNum": function(source, dest) {
		if (source.value.contains('K')) dest.value = parseFloat(source.value.nor('K')) * 1000;
		else if (source.value.contains('k')) dest.value = parseFloat(source.value.nor('k')) * 1000;
		else if (source.value.contains('M')) dest.value = parseFloat(source.value.nor('M')) * 1000000;
		else if (source.value.contains('m')) dest.value = parseFloat(source.value.nor('m')) * 1000000;
		else if (source.value.contains('B')) dest.value = parseFloat(source.value.nor('B')) * 1000000000;
		else if (source.value.contains('b')) dest.value = parseFloat(source.value.nor('b')) * 1000000000;
		else if (source.value.contains('T')) dest.value = parseFloat(source.value.nor('T')) * 1000000000000;
		else if (source.value.contains('t')) dest.value = parseFloat(source.value.nor('t')) * 1000000000000;
		else dest.value = source.value;
	},
	"phoneMask": function(e, numericOnly) {
		var result = this.numberMask(e, numericOnly);
	    if (result) {
	    	if (window.event) e = window.event;
	    	if (e.ctrlKey || e.altKey) return true;
	    	var src = (e.target) ? e.target : e.srcElement;
	    	if ((src.value.length == 12) && (e.keyCode > 46)) return false;
	    	if (((src.value.length == 3) || (src.value.length == 7)) && (e.keyCode != 109) && (e.keyCode >= 32))
	    		src.value += '-';
	    }
	    return result;
	},
	"ssnMask": function(e, numericOnly) {
		var result = this.numberMask(e, numericOnly);
	    if (result) {
	        if (window.event) e = window.event;
	        if (e.ctrlKey || e.altKey) return true;
	        var src = (e.target) ? e.target : e.srcElement;
	        if ((src.value.length == 11) && (e.keyCode > 46)) return false;
	        if (((src.value.length == 3) || (src.value.length == 6)) && (e.keyCode != 109) && (e.keyCode >= 32))
	            src.value += '-';
	    }
	    return result;
	},
	"dateMask": function(e, ymOnly) {
		if (window.event) e = window.event;
		if (e.ctrlKey || e.altKey) return true;
		var src = (e.target) ? e.target : e.srcElement;
	    var cp = getCaratPos(src);
	    var slash = this.isPunct(e.keyCode, e.shiftKey, ['/']);
	    if (slash && ([2,5,8].indexOf(cp) < 0))
	    	slash = false;
	    if (slash) src.value += '-';
	    var minus = this.isPunct(e.keyCode, e.shiftKey, ['-']);
		if (minus && ([2,5,8].indexOf(cp) < 0))
			minus = false;
		if ((minus || slash) && (cp == 2))
			src.value = '20' + src.value;
		var digit = this.isDigit(e.keyCode, e.shiftKey);
		var maxLength = ymOnly ? 7 : 10;
		if (isAllSel(src)) maxLength++;
		if (src.value.length >= maxLength)
			digit = false;
		if (digit && ([4,7].indexOf(cp) > -1))
			src.value += '-';
		return this.isControl(e.keyCode) ||
			   this.isCursor(e.keyCode) ||
			   digit || 
			   minus;
	},
	"caPostMask": function(e, which) {
		var length = isAllSel(which) ? 0 : which.value.length;
		var l = length;
		var alpha = (l == 0) || (l == 2) || (l == 5);
		var result = (alpha && (e.keyCode >= 65) && (e.keyCode <= 90)) ||
		             (!alpha && (((e.keyCode >= 48) && (e.keyCode <= 57))) || ((e.keyCode >= 96) && (e.keyCode <= 105))) ||
		             ((e.keyCode != 32) && (e.keyCode <= 46));
		return result;
	},
	"zipMask": function(e, country) {
		if (window.event) e = window.event;
		if (e.ctrlKey || e.altKey) return true;
	    var src = (e.target) ? e.target : e.srcElement;
	    var result = (country == 'US') ? this.numberMask(e, true) : 
	                 (country == 'CA') ? this.caPostMask(e, src) : 
	                 (e.keyCode < 46) && (e.keyCode != 32) ;
	    var length = isAllSel(src) ? 0 : src.value.length;
	    if (result) {
	        if ((country == 'US') && (length >= 10) && (e.keyCode > 46)) return false;
	        if ((country == 'CA') && (length >= 7) && (e.keyCode > 46)) return false;
	        if ((country == 'US') && (length == 5) && (e.keyCode != 109) && (e.keyCode >= 32))
	            src.value += '-';
	        else if ((country == 'CA') && (length == 3) && (e.keyCode > 32))
	            src.value += ' ';
	    }
	    return result;
	}
};
Roth.validate = {
	"validateNumber": function(e, length, groups, nocarat) {
		if (window.event) e = window.event;
		if ((e.keyCode == 16) || (Roth.mask.isCursor(e.keyCode) && (e.keyCode != 46))) return true;
	    var src = (e.target) ? e.target : e.srcElement;
	    var cp = getCaratPos(src);
	    //var sel = getSelection(src);
		if (groups) {
			var offset = src.value.substr(0, cp).split(',').length - 1;
			src.value = src.value.replace(/,/g, '');
			var p = src.value.indexOf('.');
			if (p < 0) p = src.value.length;
			while (p > 3) {
				p -= 3;
				src.value = src.value.substr(0, p) + ',' + src.value.substr(p);
				if (p <= cp) cp++;
				//if (p <= sel.selectionStart) sel.selectionStart++;
				//if (p <= sel.selectionEnd) sel.selectionEnd++;
			}
			if (!nocarat) setCaratPos(src, cp - offset);
			//setSelection(src, sel.selectionStart, sel.selectionEnd);
		}
		return true;
	},
	"validateZip": function(which, country) {
		var result = (which.value.length == 0) ||
			        ((country == 'US') && ((which.value.length == 5) || (which.value.length == 10))) ||
			        ((country == 'CA') && (which.value.length == 7));
		which.style.color = result ? 'black' : 'red';
		return result;
	},
	"validatePhone": function(which) {
		var result = (which.value.length == 0) || (which.value.length == 12);
	    which.style.color = result ? 'black' : 'red';
	    return result;
	},
	"validateDate": function(which) {
		var result = (which.value.length == 0) || (which.value.length == 10);
	    which.style.color = result ? 'black' : 'red';
	    return result;
	},
	"validateSsn": function(which) {
		var result = (which.value.length == 0) || (which.value.length == 11);
	    which.style.color = result ? 'black' : 'red';
	    return result;
	},
	"validateEmail": function(which) {
		var apos = which.value.indexOf('@');
		var dpos = which.value.lastIndexOf('.');
	    var result = (which.value.length == 0) || ((apos > 0) && (apos < dpos) && (dpos < (which.value.length - 2)));
	    // '@' is not the first character, '@' comes before '.', '.' has at least two characters after it.
	    which.style.color = result ? 'black' : 'red';
	    return result;
	},
	"finishDate": function(source) {
		var eom = {
	        0: [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31],
	        1: [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
		};
		var y = parseInt(source.substring(0, 4));
		var m = parseInt(source.substring(5, 7));
		var leap = (y % 4) == 0;
		if (leap && ((y % 100) == 0) && ((y % 400) != 0))
			leap = false;
		leap = leap ? 1 : 0;
		return source + '-' + eom[leap][m - 1];
	}
};
Roth.sizeIframe = function(iframeId, dialogId) {
	setTimeout(function() { 
    	var i = window.top._$(iframeId); 
    	// Most browsers need an offset of 1 pixel to prevent scroll bars from appearing; IE needs an offset of 2 pixels.
    	if (isMobile) {
    		i.style.height = i.contentWindow.document.body.clientHeight + 'px';// '10.3rem';
    		i.style.width = '100%';
    	}
    	else {
			i.height = i.contentWindow.document.body.clientHeight + 2 + 'px';
			i.width = i.contentWindow.document.body.clientWidth + 2 + 'px';
			if (dialogId)
				window.top.Roth.getDialog(dialogId).position(); 
    	}
	});
};

function getCompHeight(element) { return parseFloat(getComputedStyle(element).height); }
function getCompWidth(element) { return parseFloat(getComputedStyle(element).width); }
function getIntAttr(element, attr) { return Length.toPx(element, element.getAttribute(attr)); }
function getIntVal(input) { return parseInt(input.value); }
Roth.split = {
	sizingLimit(which, id, sizeid) {
		var table = _$(id);
		var vertical = which.className == 'splitSizerV';
		var result = 0;
		var variable = getIntVal(_$(id + "_variable")) * 2;
		if (vertical)
			for (var i = 0; i < table.children.length; i++) {
				var row = table.children[i];
				if (row.id != sizeid)
					result += i == variable ? getIntAttr(row, 'ms') : getCompHeight(row) || 0;
			}
		else
			for (var i = 0; i < table.children[0].children.length; i++) {
				var cell = table.children[0].children[i];
				if (cell.id != sizeid)
					result += i == variable ? getIntAttr(cell, 'ms') : getCompWidth(cell) || 0;
			}
		return result;
	},
	nonVariableSize(which, id) {
		var table = _$(id);
		var vertical = which.className == 'splitSizerV';
		var result = 0;
		var variable = parseInt(_$(id + "_variable").value) * 2;
		if (vertical)
			for (var i = 0; i < table.children.length; i++) {
				var row = table.children[i];
				if (i != variable && row.nodeName == 'DIV')
					result += getCompHeight(row) || 0;
			}
		else
			var row = table.children[0];
			for (var i = 0; i < row.children.length; i++) {
				var cell = row.children[i];
				if (i != variable)
					result += getCompWidth(cell) || 0;
			}
		return result;
	},
	onsize(which, id, sizeid, direction) {
		var vertical = which.className == 'splitSizerV';
		var mainSize = vertical ? _$(id).clientHeight : _$(id).clientWidth;
		var variable = parseInt(_$(id + "_variable").value) * 2;
		if (!which.cs) which.cs = vertical ? _$(sizeid).clientHeight : _$(sizeid).clientWidth;//parseInt(_$(sizeid).getAttribute('ms'));
		Roth.mouse.object = {
			'sizer': which,
			'sizeid': sizeid,
			'mx': Roth.mouse.x,
			'my': Roth.mouse.y,
			'callback': function() {
				var diff = vertical ? (Roth.mouse.y - Roth.mouse.object.my) : (Roth.mouse.x - Roth.mouse.object.mx);
				var sizer = Roth.mouse.object.sizer;
				var size = sizer.cs - (diff * direction);
				var ms = getIntAttr(_$(sizeid), 'ms');
				if (size < ms) size = ms;
				var limit = Roth.split.sizingLimit(which, id, sizeid);
				if (size > (mainSize - limit)) size = (mainSize - limit);
				sizer.ncs = size;
				var nonVar = Roth.split.nonVariableSize(which, id);
				//var newVariable = mainSize - Roth.split.nonVariableSize(which, id);
				if (vertical) {
					_$(sizeid).style.height = size + 'px';
					_$(sizeid).children[0].style.height = size + 'px';
					_$(id).children[variable].style.height = 'calc(100% - ' + nonVar +  'px)';//newVariable + 'px';
				}
				else {
					_$(sizeid).style.width = size + 'px';
					_$(id).children[0].children[variable].style.width = 'calc(100% - ' + nonVar +  'px)';//newVariable + 'px';
				}
				//window.stm[window.stmidx].setSize('calc(100% - 0.4em)', 'calc(100% - 0.4em)');
			},
			'onmouseup': function () {
				var sizer = Roth.mouse.object.sizer;
				sizer.cs = sizer.ncs;
			}
		}
	},
	getState(id) {
		let split = _$(id);
		let horizontal = true;
		let sizer = getChild(split, '0.1.0');
		if (sizer.className === 'splitPanesTd') {
			sizer = getChild(split, '1.0');
			horizontal = false;
		}
		if (!sizer.className.contains('splitSizerV'))
			return null;
		let pane = getChild(split, horizontal ? '0.0' : '0');
		let sizeone = horizontal ? pane.style.width : pane.style.height;
		pane = getChild(split, horizontal ? '0.2' : '2');
		let sizetwo = horizontal ? pane.style.width : pane.style.height;
		return {horizontal: horizontal, one: sizeone, two: sizetwo, cs: sizer.cs};
	},
	setState(id, state) {
		if (!state)
			return; 
		let split = _$(id);
		let sizer = getChild(split, state.horizontal ? '0.1.0' : '1.0');
		let first = state.one.contains('calc');
		let pane = getChild(split, state.horizontal ? '0.0' : '0');
		if (state.horizontal)
			pane.style.width = state.one;
		else
			pane.style.height = state.one;
		if (!state.horizontal && !first)
			pane.children[0].style.height = state.one;
		pane = getChild(split, state.horizontal ? '0.2' : '2');
		if (state.horizontal)
			pane.style.width = state.two;
		else
			pane.style.height = state.two;
		if (!state.horizontal && first)
			pane.children[0].style.height = state.two;
		sizer.cs = state.cs;
	}
};
Roth.file = new Object();
Roth.file.save = function(content, filename, mimetype) {
	if (!mimetype) mimetype = 'text/plain';
	var blob = new Blob([content], {type:mimetype});
	var url = window.URL.createObjectURL(blob);
	var link = document.createElement("a");
	link.download = filename;
	link.innerHTML = 'Download File';
	link.href = url;
	link.onclick = Roth.file.removeLink;
	link.style.display = 'none';
	document.body.appendChild(link);
	link.click();
};
Roth.file.open = function(callback) {
	var dlg = Roth.getDialog('fileOpen');
	dlg.fileCallback = function(result) { callback(result); Roth.getDialog('fileOpen').hide(); };
	dlg.file('Open File', 'Open', 'folder-open', "javascript:Roth.file.doOpen(_$('fileOpenFile').files[0], Roth.getDialog('fileOpen').fileCallback)", 'File', '_na', '.sql');
}
Roth.file.doOpen = function(file, callback) {
	var reader = new FileReader();
    reader.onload = function(loadEvent) 
    {
        var content = loadEvent.target.result;
        callback(content);
    };
    reader.readAsText(file, "UTF-8");
}
Roth.file.removeLink = function(event) {
	var e = event || window.event;
	var t = (e.target) ? e.target : e.srcElement;
	document.body.removeChild(t);
}
Roth.file.onChange = function (event) {
	let target = event.target;
	let fullPath = target.value;
	if (fullPath) {
		let startIndex = (fullPath.indexOf('\\') >= 0 ? fullPath.lastIndexOf('\\') : fullPath.lastIndexOf('/'));
		let filename = fullPath.substring(startIndex);
		if (filename.indexOf('\\') === 0 || filename.indexOf('/') === 0) {
			filename = filename.substring(1);
    	}
		target.nextElementSibling.innerText = filename;
	}
};
Roth.slider = {
	"setVisible": (id, side, visible) => {
		let s = _$(id).children;
		let w = visible ? null : "0";
		let o = visible ? null : "hidden";
		side = side.toUpperCase();
		for (let i = 0; i < 5; i++) {
			if (i === 2 || (i < 2 && "BL".indexOf(side) < 0) || (i > 2 && "BR".indexOf(side) < 0))
				continue;
			s[i].style.width = w;  
			s[i].style.overflowX = o;
		}
	},
	"down": (e, left) => {
		let target = relative(e.target, left ? "P.SP.C0" : "P.SN.C0");
		let coord = getAbsCoord(target);
		let x = e.clientX - coord.x;
		let resizeStep = Length.toPx(target, target.dataset.resizeStep);
		Roth.mouse.object = {
			'target': target,
			'mx': Roth.mouse.x,
			'mw': target.clientWidth,
			'left': left,
			'callback': function() {
				let offset = Roth.mouse.x - Roth.mouse.object.mx;
				offset = Math.round(offset / resizeStep) * resizeStep;
				if (!Roth.mouse.object.left)
					offset *= -1;
				Roth.slider.setWidth(Roth.mouse.object.target, Roth.mouse.object.mw + offset);
			},
			'onmouseup': function () {
				Roth.mouse.object = undefined;
			}
		}
	},
	"toggle": (e) => {
		let target = getAncestorWithClass(e.target, "collapse", true).previousElementSibling;
		let minWidth = Length.toPx(target, target.style.minWidth);
		let maxWidth = Length.toPx(target, target.style.maxWidth);
		let width = target.clientWidth;
		let prevWidth = target.prevWidth || maxWidth;
		if (prevWidth === minWidth)
			prevWidth = maxWidth;
		Roth.slider.setWidth(target, width === minWidth ? prevWidth : minWidth);
	},
	"setWidth": (target, width) => {
		let minWidth = Length.toPx(target, target.style.minWidth);
		let maxWidth = Length.toPx(target, target.style.maxWidth);
		if (width < minWidth)
			width = minWidth;
		else if (width > maxWidth)
			width = maxWidth;
		if (width === minWidth) {
			target.classList.add("collapsed");
			target.prevWidth = target.clientWidth;
		}
		else
			target.classList.remove("collapsed");
		target.style.width = width + "px";
	}
};

Roth.list = {
	"initDraggableLists" : () => {
		let draggableLists = document.querySelectorAll(".draggable-list");
		draggableLists.forEach(list => {
			let items = list.querySelectorAll(".item");
			items.forEach(item => {
				item.addEventListener("dragstart", () => {
					setTimeout(() => item.classList.add("dragging"));
				});
				item.addEventListener("dragend", () => {
					item.classList.remove("dragging");
					resequence();
				});
			});
			let resequence = () => {
				let seqId = list.dataset.sequenceId;
				let allItems = list.querySelectorAll(".item");
				let i = 0;
				allItems.forEach(item => {
					let seq = item.querySelector(`input[id^="${seqId}"]`);
					if (seq)
						seq.value = i++;
				});
			};
			let getIndex = (item) => {
				let allItems = list.querySelectorAll(".item");
				return Array.from(allItems).indexOf(item);
			};
			let initList = (e) => {
				e.preventDefault();
				const draggingItem = document.querySelector(".dragging");
				// Getting all items except currently dragging and making array of them
				let siblings = [...list.querySelectorAll(".item:not(.dragging)")];
				let diIndex = getIndex(draggingItem);
	
				// Finding the sibling after which the dragging item should be placed
				let newSlot = siblings.find(sibling => {
					let t = sibling.offsetTop;
					let h = sibling.offsetHeight;
					let l = sibling.offsetLeft;
					let w = sibling.offsetWidth;
					
					if (list.classList.contains("horizontal")) {
						return (e.clientX >= (l + (w * 0.25))) && (e.clientX <= (l + (w * 0.75)));
					} else {
						return (e.clientY >= (t + (h * 0.25))) && (e.clientY <= (t + (h * 0.75)));
					}
				});
				if (!newSlot)
					return;
	
				let nsIndex = getIndex(newSlot);
				if (nsIndex < diIndex)
					newSlot.insertAdjacentElement('beforebegin', draggingItem);
				else if (nsIndex >= diIndex)
					newSlot.insertAdjacentElement('afterend', draggingItem);
			}
	
			list.addEventListener("dragover", initList);
			list.addEventListener("dragenter", e => e.preventDefault());
		});
	}
};
addEvent(window, 'load', function() { Roth.list.initDraggableLists(); });
Roth.chart = {
	chartCallbacks: {},
	addCallback: (name, callback) => {
		Roth.chart.chartCallbacks[name] = callback;
	},
	evalCallbacks: (config) => {
		let external = config.options.plugins.tooltip.external;
		if (external) {
			let callback = Roth.chart.chartCallbacks[external];
			if (callback)
				config.options.plugins.tooltip.external = callback;
			else
				console.log(`The callback '${external}' specified in tooltip.external doesn't exist, or is not registered.`);
		}
		for (const property in config.options.plugins?.tooltip?.callbacks) {
			let tip = config.options.plugins.tooltip.callbacks[property];
			if (tip) {
				let callback = Roth.chart.chartCallbacks[tip];
				if (callback)
					config.options.plugins.tooltip.callbacks[property] = callback;
				else
					console.log(`The callback '${tip}' specified in tooltip.callbacks[${property}] doesn't exist, or is not registered.`);
			}
		}
		for (const property in config.options?.scales) {
			let tick = config.options.scales[property].ticks?.callback;
			if (tick) {
				let callback = Roth.chart.chartCallbacks[tick];
				if (callback)
					config.options.scales[property].ticks.callback = callback;
				else
					console.log(`The callback '${tick}' specified in scales[${property}].tick.callback doesn't exist, or is not registered.`);
			}
		}
	},
	loadChart: (containerId, url, aspectRatio) => {
		Roth.ajax.jsonAction(url, null, null, (config) => {
			let container = _$(containerId);
			let canvasId = `${containerId}Canvas`;
			let canvas = _$(canvasId);
			if (canvas)
				canvas.remove();
			canvas = document.createElement("CANVAS");
			canvas.id = canvasId;
			container.appendChild(canvas);
			let context = canvas.getContext("2d");
			Roth.chart.evalCallbacks(config); // Convert known function references to functions.
			if (!aspectRatio)
				aspectRatio = container.clientWidth / container.clientHeight;
			config.options.aspectRatio = aspectRatio;
		    let chart = new Chart(context, config);
		    window['chart' + canvasId] = chart;
		});
	}
};
