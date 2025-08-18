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

/*
   Menu Behavior Models
   - Hover - Drop menus should appear when hovering over the parent item.  
             The drop menu should continue to be visible while the mouse
             hovers over any descendant elements.  Once the mouse moves
             out of a drop menu, it should disappear in 0.75 seconds 
             (configurable?), unless the mouse returns to the drop menu.  
             If the mouse has moved back to an ancestor menu, that menu 
             should remain visible.
   - Click - Drop menus should appear and disappear when the parent item
             is clicked.  Drop menus should disappear if a sibling drop
             menu is activated. 
 */
var testa = 0;


var menuStats = new Object();
Roth.event = function(event) {
	var e = new Object();
	e.event = event || window.event;
	e.element = (e.event.srcElement) ? e.event.srcElement : e.event.target;
	return e;
};
const MENU_HOVER = 0;
const MENU_CLICK = 1;
Roth.menu = {
	"model": MENU_HOVER,
	"hideDelay": 750,
	"event": function(event) {
		var e = Roth.event(event);
		e.item = Roth.menu.evalElement(e.element);
		return e;
	},
	"doMouseDown": function(event) {
		return false;
		// Prevent propagation of event
	},
	"doMouseOver": function(event) {
		var e = Roth.menu.event(event);
		if (Roth.menu.model === MENU_CLICK)
			return;
		
		// If the item contains a drop menu, show it.
		
		// Regardless, prevent any ancestor drop menus from hiding.
		
		// If the item contains a drop menu, show it.
		if (e.item.drop)
			Roth.menu.showDrop(e.item);
		
		// If the item is inside a descendant of a drop menu parent item, keep all ancestor drop menus shown.
		var dropParent = e.item.dropParent;
		while (dropParent) {
			Roth.menu.showDrop(dropParent);
			dropParent = dropParent.dropParent;
		}
		
		// Hide any sibling drop menus 
		var siblings = getChildrenWithClass(e.item.parentNode, 'jmenuitem jdrop', 'jusername jdrop');
		for (var i = 0; i < siblings.length; i++)
			if (siblings[i] != e.item)
				Roth.menu.hideDrop(siblings[i]);
		
		// Prevent propagation of event
		if (e.event.cancelBubble) e.event.cancelBubble = true;
		else if (e.event.stopPropagation) e.event.stopPropagation();
	},
	"doMouseOut": function(event) {
		if (Roth.menu.model === MENU_CLICK)
			return;
		
		// Trigger delayed hide of drop menu (if applicable), and all ancestor drop menus.
		
		var e = Roth.menu.event(event);
		// if the item contains a drop menu, hide it.
		if (e.item.drop) 
			Roth.menu.hideDrop(e.item, true);
		 
		// If the item is inside a descendant of a drop menu parent item, hide all ancestor drop menus.
		var dropParent = e.item.dropParent;
		while (dropParent) {
			Roth.menu.hideDrop(dropParent, true);
			dropParent = dropParent.dropParent;
		}
		
		// Prevent propagation of event
		if (e.event.cancelBubble) e.event.cancelBubble = true;
		else if (e.event.stopPropagation) e.event.stopPropagation();
	},
	"doMouseUp": function(event) {
		var e = Roth.menu.event(event);
		var href = e.item.getAttribute('href');
		if (href) {
			if ((e.event.shiftKey ||  e.event.button == 1) && !href.startsWith('javascript:'))
				window.open(href, '_blank');
			else
			    window.location = href;
		}
		else if (e.item.drop)
			Roth.menu.toggleDrop(e.item, false);
		else 
			Roth.getDialog('error').error('This menu item is misconfigured.');
		
		// Prevent propagation of event
		if (e.event.cancelBubble) e.event.cancelBubble = true;
		else if (e.event.stopPropagation) e.event.stopPropagation();
	},
	"evalElement": function(element) {
		var item = element;
		//if (hasClassName(item, 'jusermenu')) return element;
		while (item && !hasClassName(item, 'jmenuitem', 'juseritem', 'jusername'))
			item = item.parentNode;
		if (!item.processed) {
			var menu = item;
			while (menu && !hasClassName(menu, 'jmenu', 'jusermenu'))
				menu = menu.parentNode;
			Roth.menu.evalMenu(menu, true);
		}
		return item;
	},
	"evalMenu": function(menu, origin) {
		var items = getChildrenWithClass(menu, 'jmenuitem', 'juseritem', 'jusername');
		var item;
		var i; var length = items.length;
		for (i = 0; i < length; i++) {
			item = items[i];
			var children = getChildrenWithClass(item, 'jmenudrop', 'juserdrop');
			item.drop = children ? children[0] : null;
			if (item.drop)
				Roth.menu.evalMenu(item.drop);
			item.dropParent = origin ? null : menu.parentNode;
			if (!item.onclick) item.onclick = Roth.menu.doClick;
			if (!item.onmouseout) item.onmouseout = Roth.menu.doMouseOut;
			if (!item.onmousedown) item.onmousedown = Roth.menu.doMouseDown;
			if (!item.onmouseup) item.onmouseup = Roth.menu.doMouseUp;
			item.processed = true;
		}
	},
	"toggleDrop": function(item, delayed) {
		if (!item.drop)	return;
		if (hasClassName(item, 'jdrop'))
			Roth.menu.hideDrop(item, delayed);
		else
			Roth.menu.showDrop(item);
	},
	"cancelHide": function(item) {
		if (item.hideTimeout) {
			clearTimeout(item.hideTimeout);
			item.hideTimeout = null;
			if (item.dropParent)
				Roth.menu.cancelHide(item.dropParent);
		}
	},
	"showDrop": function(item) {
		Roth.menu.cancelHide(item);
		addClassName(item, 'jdrop');
		var siblings = getChildrenWithClass(item.parentNode, 'jmenuitem jdrop', 'jusername jdrop');
		for (var i = 0; i < siblings.length; i++)
			if (siblings[i] != item)
				Roth.menu.hideDrop(siblings[i]);
		Roth.menu.lastShown = item.className.substring(0, 5);
		
		if (Roth.menu.lastShown == 'jmenu')
			Roth.menu.hideDrop(_$c('jusername')[0]);
		else {
			var items = _$c('jdrop');
			for (var i = 0; i < items.length; i++)
				if (items[i].className.contains('jmenu'))
					Roth.menu.hideDrop(items[i], false);
			
		}
	},
	"hideDrop": function(item, delayed) {
		var hideDelay = delayed ? Roth.menu.hideDelay : 0;
		if (item.hideTimeout)
			clearTimeout(item.hideTimeout);
		var timeout = setTimeout(function() { removeClassName(item, 'jdrop'); item.dropTimeout = null; }, hideDelay);
		item.hideTimeout = hideDelay > 0 ? timeout : null;
		// Also recursively hide any sub-menu drops that are open.
		if (item.drop) {
			var items = getChildrenWithClass(item.drop, 'jmenuitem jdrop', 'jusername jdrop');
			for (var i = 0; i < items.length; i++)
				if (items[i].drop)
					Roth.menu.hideDrop(items[i], delayed);
		}
	},
		
	"mainMenu": function() {
		let mainMenu = _$("mainMenu");
		return mainMenu.nodeName === 'DIV' ? getChild(mainMenu, '0') : getChild(mainMenu.nextElementSibling, '0');
	},
	"addItem": function(parent, position, id, caption, action) {
        let menu = (!parent) ? Roth.menu.mainMenu() : getChild(parent.nextElementSibling, '0');
        let item = document.createElement('LI');
        let anchor = document.createElement('A');
        anchor.id = id;
        anchor.href = action;
        anchor.onclick = function() { if (_$(id).href !== '#') _$c('footer')[0].click(); };
        item.appendChild(anchor);
        let span = document.createElement('SPAN');
        anchor.appendChild(span);
        let icon = document.createElement('DIV');
        icon.className = 'item-icon';
        span.appendChild(icon);
        let cap = document.createElement('SPAN');
        cap.innerText = caption;
        span.appendChild(cap);
        if (menu.children.length > position)
            menu.insertBefore(item, menu.children[position]);
        else
            menu.appendChild(item);
    },
    "removeItem": function(id) {
        let item = _$(id);
        if (item) item.parentNode.parentNode.removeChild(item.parentNode);
    },
    "addDropMenu": function(parent, position, id, caption) {
        let menu = (!parent) ? Roth.menu.mainMenu() : getChild(parent.nextElementSibling, '0');
        let item = document.createElement('LI');
        let anchor = document.createElement('A');
        anchor.id = id;
        anchor.href = '#';
        anchor.onclick = 'return false;';
        item.appendChild(anchor);
        let span = document.createElement('SPAN');
        anchor.appendChild(span);
        let icon = document.createElement('DIV');
        icon.className = 'item-icon';
        span.appendChild(icon);
        let cap = document.createElement('SPAN');
        cap.innerText = caption;
        span.appendChild(cap);
        let drop = document.createElement('DIV');
        drop.className = 'roth-dropmenu';
        item.appendChild(drop);
        let ul = document.createElement('UL');
        drop.appendchild(ul);
        if (menu.children.length > position)
            menu.insertBefore(drop, menu.children[position]);
        else
            menu.appendChild(drop);
    }	
	/*
	"addItem": function(parent, position, id, caption, action) {
        var menu = (!parent) ? _$c("jmenu")[0] : getChild(parent, '0');
        var item = document.createElement('SPAN');
        item.id = id;
        item.className = 'jmenuitem';
        item.innerHTML = caption;
        item.setAttribute('href', action);
        if (menu.children.length > position)
            menu.insertBefore(item, menu.children[position]);
        else
            menu.appendChild(item);
    },
    "removeItem": function(id) {
        var item = _$(id);
        if (item) item.parentNode.removeChild(item);
    },
    "addDropMenu": function(parent, position, id, caption) {
        var menu = (!parent) ? _$c("jmenu")[0] : getChild(parent, '0');
        var drop = document.createElement('DIV');
        drop.id = id;
        drop.className = 'jmenuitem';
        drop.innerHTML = caption + '<div class="jmenudrop"></div>';
        if (menu.children.length > position)
            menu.insertBefore(drop, menu.children[position]);
        else
            menu.appendChild(drop);
    }
    */
};
addEvent(document, 'click', function(event) { 
								var e = Roth.event(event); 
								var items = _$c('jdrop');
								if (items)
									for (var i = 0; i < items.length; i++)
										if ((!e.element.className.startsWith('jmenu') && !e.element.className.startsWith('juser')) || (!items[i].className.contains(Roth.menu.lastShown)))
											Roth.menu.hideDrop(items[i], false);
							});