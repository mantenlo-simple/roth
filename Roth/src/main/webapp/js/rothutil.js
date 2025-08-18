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
let isIE = /Trident/.test(navigator.userAgent);
let isWebkit=/\b(iPad|iPhone|iPod)\b/.test(navigator.userAgent) && /WebKit/.test(navigator.userAgent) && !/Edge/.test(navigator.userAgent) && !window.MSStream;
let isFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
let isOpera = navigator.userAgent.indexOf("Opera") > -1;
let isChrome = /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor);
let isSafari = /Safari/.test(navigator.userAgent) && /Apple Computer/.test(navigator.vendor);
let isMobile = navigator.userAgent.indexOf("Mobi") != -1;
let uid = (function() { var id = 0; return function() { return id++; }; }) ();

function nvl(source, value) { return source ? source : value || ''; }
/* --------------------------------
	String Prototype Extensions
   -------------------------------- */
//All browsers but IE have these, so if IE, then add them.
if(isIE) { 
	String.prototype.startsWith = function(val) { return this.indexOf(val) == 0; };
	String.prototype.endsWith = function(val) { return this.indexOf(val) == this.length - val.length; };
	String.prototype.trim = function() { return this.replace(/^\s+|\s+$|^\n+|\n+$|^\r+|\r+$/g, ''); };
}
// No browser have these methods at this time.
   // TODO: Should replace contains with includes, which is supported in non-IE browsers.
if(!String.prototype.contains) 
	String.prototype.contains = function(val) { return this.indexOf(val) >= 0; };
if(!String.prototype.containsAny)	
	String.prototype.containsAny = function(val) {
		var result = false;
		for (var i = 0; i < val.length; i++)
			if (this.contains(val[i])) result = true;
		return result; 
	};
// These methods will doubtless not ever be added by any browser.
String.prototype.or = function(val) { if (!this.contains(val)) return this + ' ' + val; else return this + ''; };
String.prototype.nor = function(val) { if (this.contains(val)) return this.replace(val, '').replace('  ', ' ').trim(); else return this; };
String.prototype.xor = function(val) { if (this.contains(val)) return this.nor(val); else return this.or(val); };
String.prototype.reverse = function() {
	var result = '';
	for (var i = this.length - 1; i >= 0; i--)
		result += this.charAt(i);
	return result;
};
// splitCR
String.prototype.splitCR = function() { return this.split(/\r\n|\r|\n/g); };
// splitCSV (see Array.prototype.joinCSV)
String.prototype.splitCSV = function() {
	var list = new Array();
	var item = '';
	var quoted = false;
	var escaped = false;
	var i = 0;
	
	while (i < this.length) {
		var c = this.charAt(i);
		if (c == '"') {
			if (!quoted) quoted = true;
			else if (!escaped) {
				if (this.charAt(i + 1) == '"') escaped = true;
				else quoted = false;
			}
			else { item += c; escaped = false; }
		}
		else if (c == ',') {
			if (quoted) item += c;
			else { list.push(item); item = ''; }
		} 
		else
			item += c;
		
		i++;
	}
	
	if (item && (item != '')) list.push(item);
	
	return list;
};
/* --------------------------------
	Array Prototype Extensions
   -------------------------------- */
//Array.prototype.isArray = true;
// indexOf (go figure why this isn't built into JavaScript...)
if (!Array.prototype.indexOf) Array.prototype.indexOf = function(val) {
	if (this.length == 0) return -1;
	for (var i = 0; i < this.length; i++) if (this[i] == val) return i;
	return -1;
};
// joinCSV (see String.prototype.splitCSV)
Array.prototype.joinCSV = function() {
	function quoteCSV(value) { 
		value = '' + value;
		value = value.replace(/"/g, '""'); // Escape any double-quotes, if they exist.
		return ((value.indexOf(',') > -1) || (value.indexOf('"') > -1)) ? '"' + value + '"' : value; 
	}
	if (this.length == 0) return '';
	var result = '';
	for (var i = 0; i < this.length; i++) result += ((i > 0) ? ',' : '') + quoteCSV(this[i]);
	return result;
};
/* --------------------------------
	Element Attribute Functions
   -------------------------------- */
function getAttrInt(element, name, defaultValue) {
	if (!defaultValue) defaultValue = 0;
	var attr = element.getAttribute(name); 
	return (!attr) ? defaultValue : parseInt(attr); 
}
/* --------------------------------
	Class Name Functions
   -------------------------------- */
// checkClassName
function checkClassName(element, className) {
	return (element && element.className && (element.className == className));
}
// hasClassName
function hasClassName(element, className) {
	if (!element.className || element.nodeName == 'svg')
		return false;
	return element.classList.contains(className);
}
// addClassName
function addClassName(element, className) {
	var ec = ' ' + element.className.replace(/^s*|s*$/g, '') + ' ';
	var nc = ec;
	className = className.trim();
	if (!hasClassName(element, className)) nc = ec + className;
	element.className = nc.trim();
	return element;
}
// removeClassName
function removeClassName(element, className) {
	var ec = ' ' + element.className.replace(/^s*|s*$/g, '') + ' ';
	var nc = ec;
	className = className.trim();
	if (hasClassName(element, className)) nc = ec.replace(' ' + className + ' ', ' ');
	element.className = nc.trim();
	return element;
}
/* --------------------------------
	Coordinate Functions
   -------------------------------- */
// getAbsCoord
function getAbsCoord(target) {
	var obj = target;
	var coord = new Object();
	coord.x = obj.offsetLeft - obj.scrollLeft;
	coord.y = obj.offsetTop - obj.scrollTop;

	while (obj.parentNode && (obj != document.body)) {
		if (obj.parentNode.style.position == 'absolute')
			obj = obj.parentNode;
		else if (obj.parentNode == obj.offsetParent) {
			obj = obj.offsetParent; 
			coord.x += obj.offsetLeft - obj.scrollLeft;
			coord.y += obj.offsetTop - obj.scrollTop;
		}
		else {
			obj = obj.parentNode;
			coord.x -= obj.scrollLeft;
			coord.y -= obj.scrollTop;
		}

		if (obj.style.position == 'absolute') obj = document.body;
	}

	return coord;
}
function dim(element) {
	let ewidth = null;
	let eheight = null;
	if (document.all) {
		ewidth = parseInt(element.currentStyle.width) + 
		         parseInt(element.currentStyle.borderLeftWidth, 10) + 
		         parseInt(element.currentStyle.borderRightWidth, 10) + 
		         parseInt(element.currentStyle.paddingLeft, 10) + 
		         parseInt(element.currentStyle.paddingRight, 10) + 
		         parseInt(element.currentStyle.marginLeft, 10) + 
		         parseInt(element.currentStyle.marginRight, 10);
		eheight = parseInt(element.currentStyle.height) + 
		         parseInt(element.currentStyle.borderTopWidth, 10) + 
		         parseInt(element.currentStyle.borderBottomWidth, 10) + 
		         parseInt(element.currentStyle.paddingTop, 10) + 
		         parseInt(element.currentStyle.paddingBottom, 10) + 
		         parseInt(element.currentStyle.marginTop, 10) + 
		         parseInt(element.currentStyle.marginBottom, 10);
	}
	else {
		ewidth = parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('width')) +
				 parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('border-left-width')) +
		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('border-right-width')) +
		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('padding-left')) +
 		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('padding-right')) +
 		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('margin-left')) +
		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('margin-right'));
		eheight = parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('height')) + 
				 parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('border-top-width')) +
		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('border-bottom-width')) +
		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('padding-top')) +
		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('padding-bottom')) +
		         parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('margin-top')) +
			     parseInt(document.defaultView.getComputedStyle(element,'').getPropertyValue('margin-bottom'));
	}
	return {width:ewidth,height:eheight};
}
/* --------------------------------
   Window Functions
   -------------------------------- */
//Get Window Dimensions (Returns the inner dimensions of the browser window) --
//[width,height,scrollX,scrollY]
function getWindowDimensions(whichDoc) {
	if (whichDoc == null) whichDoc = document; // Assume current document, if none specified.

	var isIEde = isIE && whichDoc.documentElement && (whichDoc.documentElement.clientWidth || whichDoc.documentElement.clientHeight);
	
	var dim = new Object();
	// Get Width & Height
	dim.width = (isIEde) ? whichDoc.documentElement.clientWidth : (isIE) ? whichDoc.body.clientWidth : whichDoc.defaultView.innerWidth;
	dim.height = (isIEde) ? whichDoc.documentElement.clientHeight : (isIE) ? whichDoc.body.clientHeight : whichDoc.defaultView.innerHeight;
	// Get Scroll Offsets
	dim.scrollY = (isIEde) ? whichDoc.documentElement.scrollTop : (isIE) ? whichDoc.body.scrollTop : whichDoc.defaultView.pageYOffset;
	dim.scrollX = (isIEde) ? whichDoc.documentElement.scrollLeft : (isIE) ? whichDoc.body.scrollLeft : whichDoc.defaultView.pageXOffset;
	
	return dim;
}
function getProperty(element, property) {
	if (element.currentStyle)
		return element.currentStyle[property];
	else
		return window.getComputedStyle(element, null).getPropertyValue(property);
}
/* --------------------------------
	Document Search Functions
   -------------------------------- */
// childElements
function childElements(parent) { 
	var e = new Array(); 
	
	for (var i = 0; i < parent.childNodes.length; i++) 
		if (parent.childNodes[i].nodeType == 1)
			e[e.length] = parent.childNodes[i]; 
			
	return e; 
}
/* --------------------------------
	Event Functions
   -------------------------------- */
var xb = {
	evtHash: [],

	ieGetUniqueID: function(_elem) {
		if (_elem === window) { return 'theWindow'; }
		else if (_elem === document) { return 'theDocument'; }
		else { return _elem.uniqueID; }
	},

	addEvent: function(_elem, _evtName, _fn, _useCapture) {
		if (typeof _elem.addEventListener != 'undefined') 
			_elem.addEventListener(_evtName, _fn, _useCapture);
		else if (typeof _elem.attachEvent != 'undefined') {
			var key = '{FNKEY::obj_' + xb.ieGetUniqueID(_elem) + '::evt_' + _evtName + '::fn_' + _fn + '}';
			var f = xb.evtHash[key];
			if (typeof f != 'undefined')  return;
			f = function() { _fn.call(_elem); };
			xb.evtHash[key] = f;
			_elem.attachEvent('on' + _evtName, f);

			// attach unload event to the window to clean up possibly IE memory leaks
			window.attachEvent('onunload', function() { _elem.detachEvent('on' + _evtName, f); });

			key = null;
			//f = null;   /* DON'T null this out, or we won't be able to detach it */
		}
		else
			_elem['on' + _evtName] = _fn;
	},

	removeEvent: function(_elem, _evtName, _fn, _useCapture) {
		if (typeof _elem.removeEventListener != 'undefined')
			_elem.removeEventListener(_evtName, _fn, _useCapture);
		else if (typeof _elem.detachEvent != 'undefined') {
			var key = '{FNKEY::obj_' + xb.ieGetUniqueID(_elem) + '::evt' + _evtName + '::fn_' + _fn + '}';
			var f = xb.evtHash[key];
			if (typeof f != 'undefined') {
				_elem.detachEvent('on' + _evtName, f);
				delete xb.evtHash[key];
			}

			key = null;
			//f = null;   /* DON'T null this out, or we won't be able to detach it */
		}
	}
};
// addEvent
function addEvent(element, event, func, useBroken) {
	if (useBroken) {
		if (!func) return;
		if (element.attachEvent) element.attachEvent("on" + event, func);
		else if (element.addEventListener) element.addEventListener(event, func, true);
	}
	else
		xb.addEvent(element, event, func, true);
}
// removeEvent
function removeEvent(element, event, func, useBroken) {
	if (useBroken) {
		if (element.detachEvent) element.detachEvent("on" + event, func);
		else if (element.removeEventListener) element.removeEventListener(event, func, true);
	}
	else
		xb.removeEvent(element, event, func, true);
}
function triggerEvent(element, event) {
	let e = new MouseEvent(event, {
	    view: window,
	    bubbles: true,
	    cancelable: true
	});
	return element.dispatchEvent(e);
}
/* --------------------------------
	Cookie Functions
   -------------------------------- */
// setCookie
function setCookie(name, value, expires, path, domain, secure) {
	// set time, it's in milliseconds
	var today = new Date();
	today.setTime(today.getTime());
	
	// If the expires variable is set, adjust it to milliseconds.
	if (expires) expires = expires * 1000 * 60 * 60 * 24;
	var expDts = new Date(today.getTime() + (expires));
	
	document.cookie = name + '=' + encodeURIComponent(value) +
					  ((!expires) ? '' : (expires < 0) ? ';expires=Thu, 01-Jan-1970 00:00:01 GMT' : ';expires=' + expDts.toGMTString()) +
					  ((path) ? ';path=' + path : '') +
					  ((domain) ? ';domain=' + domain : '') +
					  ((secure) ? ';secure' : '');
}
// getCookie
function getCookie(name) {
	if (document.cookie.length > 0) {
		var start = -1;
		if (document.cookie.substr(0, name.length + 1) == (name + '=')) start = 0;
		else start = document.cookie.indexOf('; ' + name + '=');
		
		if (start != -1) {
			start = start + name.length + ((start == 0) ? 1 : 3);
			var end = document.cookie.indexOf(';',  start);
			if (end == -1) end = document.cookie.length;
			return decodeURIComponent(document.cookie.substring(start, end));
		}
	}
	return "";
}
// deleteCookie
function deleteCookie(name, path, domain) {
	setCookie(name, '', -1, path, domain);
}
/* --------------------------------
	Miscellaneous Functions
   -------------------------------- */
function upperFirst(source) {
    return source.substring(0, 1).toUpperCase() + source.substring(1);
}
function camelcase(source) {
    var result = "";
    var last = 0;
    var seg;
    
    for (var i = 0; i < source.length; i++)
        if (source.charAt(i) == '_') {
            seg = source.substring(last, i);
            result += (result.length == 0) ? seg : upperFirst(seg);
            last = i + 1;
        }
        else if (source.charAt(i) == '$') {
	        seg = source.substring(last, i);
	        result += ((result.length == 0) ? seg : upperFirst(seg)) + '$';
	        last = i + 1;
	    }
    
    seg = source.substring(last);
    result += (result.length == 0) ? seg : upperFirst(seg);
    return upperFirst(result);
}
// isTabEvent
function isTabEvent(e) {
	if (window.event) e = window.event;
	return e.keyCode == 9;
}
// nocaps
function nocaps(which) { which.value = which.value.toLowerCase(); }
// caps
function caps(which) { which.value = which.value.toUpperCase(); }
// digits
function digits(num) {
	var n = num;
	var d = 1;
	while (n > 10) { d++; n /= 10; }
	return d;
}
// lesserOf
function lesserOf(a, b) { return (a < b) ? a : b; }
function lesserOfPos(a, b) { return (a < b && a > 0) ? a : (b > 0) ? b : a; }
// greaterOf
function greaterOf(a, b) { return (a > b) ? a : b; }
// clearSelection
function clearSelection() {
	if (document.selection && document.selection.empty) document.selection.empty() ;
	else if (window.getSelection) {
		var sel = window.getSelection();
		if (sel && sel.removeAllRanges) sel.removeAllRanges() ;
	}
}
// getCaratPos
function getCaratPos(which) {
	var pos = 0;
	if (which.selectionStart)
		pos = which.selectionStart;
	else if (document.selection) {
		var r = document.selection.createRange();
		r.setEndPoint("EndToStart", which.createTextRange());
		pos = r.text.length;
	}
	return pos;
}
// setCaratPos
function setCaratPos(which, pos) {
	if (which.selectionStart)
		which.setSelectionRange(pos, pos);
	else if (document.selection) {
		var r = document.selection.createRange();
		r.move('character', pos);
		range.select();
	}
}
// getSelText
function getSelText(which) {
	if (which.selectionStart)
		result = which.value.substring(which.selectionStart, which.selectionEnd);
	else if (document.selection)
		result = document.selection.createRange().text;
	else if (which.selectionEnd)
		result = which.value.substring(0, which.selectionEnd);
	return result;
}
// isAllSel
function isAllSel(which) {
	return which.value == getSelText(which);
}
// getTextWidth
function getTextWidth(text, offset) {
	if (!offset) offset = 0;
	var t = document.createElement('DIV');
	t.style.position = 'absolute';
	t.style.visibility = 'hidden';
	t.style.whiteSpace = 'nowrap';
	t.innerHTML = text;
	document.body.appendChild(t);
	var result = t.clientWidth + offset;
	document.body.removeChild(t);
	return result;
}
function getTextHeight(text, offset) {
	if (!offset) offset = 0;
	var t = document.createElement('DIV');
	t.style.position = 'absolute';
	t.style.visibility = 'hidden';
	t.style.whiteSpace = 'nowrap';
	t.innerHTML = text;
	document.body.appendChild(t);
	var result = t.clientHeight + offset;
	document.body.removeChild(t);
	return result;
}
//=============================================================================
//AJAX Functions 
//=============================================================================
//Execute Ajax ----------------------------------------------------------------
function executeAjax(args) {
    let { method = "POST", url, parameters, callback, data, timeout, username, password, type, responseType } = args;
	let req = new XMLHttpRequest();
	if (!parameters) 
		parameters = '';
	if (data) 
		req.data = data;
	if (type && req.overrideMimeType)
		req.overrideMimeType(type);
	if (responseType)
		req.responseType = responseType;
	if (req) {
		if (timeout)
			setAjaxTimeout(req);
		req.onreadystatechange = callback;
		if (method !== 'POST')
			url = processQueryParameters(url, parameters);
		req.open(method, url, true);
		if (method === 'POST') {
			let ptype = typeof parameters;
			if (parameters && ptype == 'string') {
				req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded;charset=UTF-8');
				// parameters = processQueryParameters('', parameters);
			}
			//req.setRequestHeader('Content-length', parameters.length);
			if (username)
				req.setRequestHeader('Authorization', `Basic ${btoa(`${username}:${password}`)}`);
			req.send(parameters);
		} else
			req.send();
	}
	return req;
}
function setAjaxTimeout(req) {
	req.timeoutCallback = function() { 
		req.abort(); 
		var dlg = Roth.getDialog('ajaxerror');
		dlg.caption = Roth.createImageCaption("error", "ERROR");
		dlg.show('Your request timed out.'); 
	};
	setTimeout(req.timeoutCallback, timeout);
}
function processQueryParameters(url, paramString) {
	if (!paramString || paramString === "")
		return url;
	let params = paramString.split('&');
	let result = '';
	params.forEach(p => {
		let name = p.substring(0, p.indexOf('='));
		let value = p.substring(p.indexOf('=') + 1);
		if (!value.includes('%3D'))
			value = encodeURIComponent(value);
		result += `${result === '' ? '' : '&'}${name}=${value}`;
	});
	return `${url}${url === "" ? "" : (url.includes('?') ? '&' : '?')}${result}`;
}

/*
function executeAjax(callback, url, parameters, data, timeout, type, username, password, responseType) {
	var req = null;
	if (!parameters) parameters = '';

	req = (window.XMLHttpRequest) ? new XMLHttpRequest() : (window.ActiveXObject) ? new ActiveXObject("Microsoft.XMLHTTP") : null;
	
	if (data) req.data = data;
	
	if (type && req.overrideMimeType)
		req.overrideMimeType(type);
	
	if (responseType)
		req.responseType = responseType;
	
	if (req) {
		if (timeout) {
			req.timeoutCallback = function() { 
				req.abort(); 
				var dlg = Roth.getDialog('ajaxerror');
				dlg.caption = Roth.createImageCaption("error", "ERROR");
				dlg.show('Your request timed out.'); 
			};
			setTimeout(req.timeoutCallback, timeout);
		}
		req.onreadystatechange = callback;
		req.open("POST", url, true);
		
		var ptype = typeof parameters;
		if (parameters && ptype == 'string')
			req.setRequestHeader("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
		//req.setRequestHeader("Content-length", parameters.length);
		if (username)
			req.setRequestHeader("Authorization", "Basic " + btoa(username + ":" + password));
		req.send(parameters);
	}
	
	return req;
}
*/

//If the browser doesn't already support getElementsByClassName, then add it --
if (!document.getElementsByClassName)
	document.getElementsByClassName = function(cl) {
		var retnode = [];
		var myclass = new RegExp('\\b'+cl+'\\b');
		var elem = this.getElementsByTagName('*');
		
		for (var i = 0; i < elem.length; i++) {
			var classes = elem[i].className;
			if (myclass.test(classes)) retnode.push(elem[i]);
		}
		
		return retnode;
	};
function getPrevSibling(which) {
    which = which.previousSibling;
    while (which.nodeType != 1) which = which.previousSibling;
    return which;
}
function getNextSibling(which) {
    which = which.nextSibling;
    while (which.nodeType != 1) which = which.nextSibling;
    return which;
}
function getChild(which, index) {
    var idxs = (index + '').split('.');
    var w = which;
    for (var ii = 0; ii < idxs.length; ii++) {
        var x = -1;
        var children = w.children || w.childNodes;
        for (var i = 0; i < children.length; i++) {
            if (children[i].nodeType == 1) x++;
            if (x == idxs[ii]) w = children[i];
        }
    }
    return (w != which) ? w : null;
}
function getChildrenWithClass(which, className) {
	if (!which.children)
		return null;
	var result = new Array();
	for (var i = 0; i < which.children.length; i++) {
		for (var j = 1; j < arguments.length; j++)
		    if (checkClassName(which.children[i], arguments[j]))
	        	result.push(which.children[i]);
	}
	return result;
}
function getAncestor(which, value, compareFunction, startWithSelf) {
    let w = startWithSelf ? which : which.parentElement;
    while (w && !compareFunction(w, value)) w = w.parentElement;
    return w;
}
function getAncestorNode(which, nodeName, startWithSelf) {
	return getAncestor(which, nodeName, (w, n) => w.nodeName === n, startWithSelf);
}
function getAncestorWithClass(which, className, startWithSelf) {
	return getAncestor(which, className, (w, c) => w.classList && w.classList.contains(c), startWithSelf);
}
let relative = (element, path) => {
	// "P" - Parent, "SP" - Sibling-Previous, "SN" - Sibling-Next, "CX" Child (X is the index)
	// Example: P.SN.C0 == parentNode.nextElementSibling.children[0];
	path.split(".").forEach(p => {
		element = p === "P" ? element.parentElement
				: p === "SP" ? element.previousElementSibling
				: p === "SN" ? element.nextElementSibling
				: p.charAt(0) === 'C' ? element.children[parseInt(p.substring(1))]
				: undefined;
	});
	return element;
}
function getValue(which) {
	if (!which) return null;
    switch (which.nodeName) {
        case "INPUT": 
            switch (which.type) {
                case "hidden":
                case "text":
                case "password": return which.value;
                case "checkbox":
                case "radio": return which.checked;
            } break;
        case "TEXTAREA": return which.value;
        case "SELECT":
        	var value = '';
            for (var i = 0; i < which.options.length; i++)
                if (which.options[i].selected) {
                	var sel = which.options[i].value;
                	value += ((value == '') ? '' : ',') + (sel.contains(',') ? '"' + sel + '"' : sel);
                }
            return value == '' ? null : value;
    }
    return null;
}
function getDefaultValue(which) {
	if (!which) return null;
    switch (which.nodeName) {
        case "INPUT": 
            switch (which.type) {
                case "hidden":
                case "text":
                case "password": return which.defaultValue;
                case "checkbox":
                case "radio": return which.defaultChecked;
            } break;
        case "TEXTAREA": return which.defaultValue;
        case "SELECT":
        	var value = '';
            for (var i = 0; i < which.options.length; i++)
                if (which.options[i].defaultSelected) {
                	var sel = which.options[i].value;
                	value += ((value == '') ? '' : ',') + (sel.contains(',') ? '"' + sel + '"' : sel);
                } 
            return value == '' ? null : value;
    }
    return null;
}	
function setValue(which, value) {
	if (!which) return;
    switch (which.nodeName) {
        case "INPUT":
            switch (which.type) {
                case "hidden": 
					which.value = value;
					if (_$('__' + which.id))
						setValue(_$('__' + which.id), value);
					break;
                case "text":
                	which.value = value;
                	if (which.parentNode.classList.contains('roth-radio-group')) {
						for (let i = 0; i < which.parentNode.children.length; i++) {
							let c = which.parentNode.children[i];
							if (c.classList && c.classList.contains('roth-radio') && c.children[0].value == value)
								c.children[0].click();
						}
					}
					break;
                case "password": which.value = value; break;
                case "checkbox":
                case "radio": which.checked = value; break;
            } break;
        case "TEXTAREA": which.value = value; break;
        case "SELECT":
            for (var i = 0; i < which.options.length; i++)
            	if (which.options[i].value == value)
            		which.options[i].selected = true;
            break;
    }
}
function getCssRules(selectorText) {
	var result = null;
	var s = document.styleSheets;
	for (var x = 0; x < s.length; x++) {
		var r = s[x].rules;
		for (var y = 0; y < r.length; y++)
			if (r[y].selectorText == selectorText) {
				if (result == null) result = new Array();
				result[result.length] = r[y];
			}
	}
	return result;
}
function getCssRule(selectorText, reference) {
	var result = null;
	var rules = getCssRules(selectorText);
	if (typeof reference == 'number')
		result = rules[reference];
	else if (typeof reference == 'string')
		for (var i = 0; i < rules.length; i++) {
			var p = rules[i].cssText.indexOf(' ' + reference + ':');
			if (p >= 0) result = rules[i];
		}
	return result;
}
function enableScript(containerId, indexes, src) {
	if (!indexes)
		es(containerId, null, src);
	else if (typeof indexes == 'number')
		es(containerId, indexes);
	else if ((typeof indexes == 'object') && indexes.length) {
		indexes.sort();
		for (var i = indexes.length - 1; i >= 0; i--)
			es(containerId, indexes[i]);
	}
	function es(containerId, index, src) {
		if (src)
			esi('_script_' + src, src);
		else {
		    var s = _$(containerId).children[index];
			var id = containerId + '_script_' + index;
		    if (!_$(id)) esi(id, s.src, s.innerHTML);
			_$(containerId).removeChild(s);
		}
		function esi(id, src, innerHTML) {
			var script = document.createElement('SCRIPT'); 
			script.type = 'text/javascript'; 
			script.src = (src) ? src : innerHTML;
			script.id = id;
			document.body.appendChild(script);
		}
	}
}
function setSelection(field, start, end) {
    if (field.createTextRange) {
        var selRange = field.createTextRange();
        selRange.collapse(true);
        selRange.moveStart('character', start);
        selRange.moveEnd('character', end);
        selRange.select();
        field.focus();
    } 
    else if (field.setSelectionRange) {
        field.focus();
        field.setSelectionRange(start, end);
    } 
    else if (typeof field.selectionStart != 'undefined') {
        field.selectionStart = start;
        field.selectionEnd = end;
        field.focus();
    }
}
function _getSelection(field) {
	var result = new Object();
	result.start = field.selectionStart;
	result.end = field.selectionEnd;
	return result;
}
// _$ - alias for document.getElementById
function _$(id) { return document.getElementById(id); }
// _$c - alias for document.getElementsByClassName
function _$c(className, element) {
	if (!element)
		return document.getElementsByClassName(className);
	else {
		
	}
}
// _$v - get value for element specified by id, if the value parameter is specified, it will override the current value
function _$v(id, value) { if (value) setValue(_$(id), value); return getValue(_$(id)); }
// _$d - get default value for element specified by id
function _$d(id) { return getDefaultValue(_$(id)); }
// _$vc - has value changed for element specified by id?
function _$vc(id) { return _$v(id) != _$d(id); }
// _$fc - has any value on the form specified by id changed?
function _$fc(id, callback) {
	var form = _$(id);
    var count = 0;
    for (var i = 0; i < form.elements.length; i++)
        if (getValue(form.elements[i]) != getDefaultValue(form.elements[i])) 
        	count++;
    if (callback && (count > 0)) {
    	var dlg = Roth.getDialog('confirm');
    	dlg.callback = callback;
    	dlg.confirm('Changes have been made to this form.<br/>All changes will be lost if you continue.<br/><br/>Do you wish to continue anyway?');
    }
    else if (callback)
    	callback();
    return count > 0;
}
// _$r - alias for getCssRule or getCssRules (former if reference supplied, otherwise latter)
function _$r(selectorText, reference) { return reference ? getCssRule(selectorText, reference) : getCssRules(selectorText); }
// _$s - alias for enableScript
function _$s(containerId, indexes) { enableScript(container, indexes); }
// _$t* - traverse element tree
// _$ta - get ancestor
// _$tc - get child(ren)/descendant
// _$ts - get sibling
function _$ta(which, reference) { return getAncestorNode(which, reference); }
function _$tc(which, index) { return getChild(which, index); }
function _$ts(which, direction) { return (direction == 0) ? which : (direction < 0) ? getPrevSibling(which) : getNextSibling(which); }
function _$tr(element, event) { return triggerEvent(element, event); }
// _$0 - is set.
function _$0(source) { return source !== undefined && source !== null; }
function clickGridCheckbox(event, which) {
	if (event.target.nodeName === 'TR' || event.target.nodeName === 'TD' || event.target.className === 'rtd')
		getChild(which, '0.0.0.1').click(); 
	else 
		event.stopPropagation();
}

/*! getEmPixels  | Author: Tyson Matanich (http://matanich.com), 2013 | License: MIT */
(function (document, documentElement) {
    // Enable strict mode
    "use strict";

    // Form the style on the fly to result in smaller minified file
    var important = "!important;";
    var style = "position:absolute" + important + "visibility:hidden" + important + "width:1em" + important + "font-size:1em" + important + "padding:0" + important;

    window.getEmPixels = function (element) {

        var extraBody;

        if (!element) {
            // Emulate the documentElement to get rem value (documentElement does not work in IE6-7)
            element = extraBody = document.createElement("body");
            extraBody.style.cssText = "font-size:1em" + important;
            documentElement.insertBefore(extraBody, document.body);
        }

        // Create and style a test element
        var testElement = document.createElement("i");
        testElement.style.cssText = style;
        element.appendChild(testElement);

        // Get the client width of the test element
        var value = testElement.clientWidth;

        if (extraBody) {
            // Remove the extra body element
            documentElement.removeChild(extraBody);
        }
        else {
            // Remove the test element
            element.removeChild(testElement);
        }

        // Return the em value in pixels
        return value;
    };
}(document, document.documentElement));
let quillinited = false;
let initQuill = (containerid) => {
	if (typeof Quill === 'undefined')
		return;
	if (!quillinited) {
		let Font = Quill.import('formats/font');
		Font.whitelist = ['roboto'];
		Quill.register(Font, true);
		Quill.register("modules/imageUploader", ImageUploader);
		quillinited = true;
	}

	let toolbarOptions = [
		[{ 'font': [] }],
		[{ 'size': ['small', false, 'large', 'huge'] }],  // custom dropdown
		[{ 'color': [] }, { 'background': [] }],          // dropdown with defaults from theme
		['bold', 'italic', 'underline', 'strike'],        // toggled buttons
		[{ 'script': 'sub'}, { 'script': 'super' }],      // superscript/subscript
		['blockquote', 'code-block'],
		// [{ 'header': 1 }, { 'header': 2 }],               // custom button values
		[{ 'list': 'ordered'}, { 'list': 'bullet' }],
		[{ 'indent': '-1'}, { 'indent': '+1' }],          // outdent/indent
		[{ 'align': [] }],
		['link', 'image'],
		['clean']                                         // remove formatting button
	];
	let container = _$(containerid);
	if (!container)
		return;
	let quillInputs = container.querySelectorAll(".roth-input.quill");
	quillInputs.forEach(e => {
		let id = e.children[1].id;
		let fullEditor = new Quill('#' + id + '_quill', {
			modules: {
				syntax: true,
				toolbar: toolbarOptions,
				imageUploader: {
					upload: file => {
						return new Promise((resolve, reject) => {
							const formData = new FormData();
							let tokens = document.getElementsByName("_csrf-token");
							formData.append("_csrf-token", tokens[tokens.length - 1].value);
							formData.append("image", file);
	
							fetch("/Tricosa/Image/upload", {
								method: "POST",
								body: formData
							})
							.then(response => response.json())
							.then(result => {
								if (result.data.error)
									throw new Error(result.data.error);
								resolve(result.data.url);
							})
							.catch(error => {
								reject("Upload failed");
								Roth.getDialog('error').error(error);
							});
						});
					},
			    },
			},
			theme: 'snow'
		});
		if (_$(id).value !== '')
			_$(id).previousElementSibling.children[0].innerHTML = _$(id).value;
		fullEditor.on('text-change', function() { 
			_$(id).value = _$(id).previousElementSibling.children[0].innerHTML; 
		});
	});
}