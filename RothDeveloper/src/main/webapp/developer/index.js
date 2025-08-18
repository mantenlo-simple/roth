initCodeMirror();
var stmcache = new Object();
var rsltcache = new Object();
var oldDsn;
var tableId;
var tableName;
function setMainHeight() {
	if (_$c('fullwindow')[0]) {
        var offset = getAbsCoord(_$c('main')[0]).y + _$c('footer')[0].clientHeight + 16;
        _$c('main')[0].style.height = (document.body.clientHeight - offset) + 'px';
        var height = _$c('main_content')[0].clientHeight - 62;
        _$('dsnCont').style.height = height + 'px';
        _$('objCont').style.height = height + 'px';
    }
}
function setTabsetWidth(mainWidth) {
	
}

function openSettings() {
	Roth.execDialog('jndiSettings', contextRoot + '/Developer/loadSettings', null, 'JNDI Settings', 'cog');
	
	/*
	Settings:
		- Move row limit and max length to settings dialog?
		- Allow SystemAdmin to set which data sources are available to the Developer role.
		- Allow SystemAdmin to set whether available data sources are readonly to the Developer role.
	
	*/
}
if (sysAdmin)
	Roth.menu.addItem(null, 2, 'pm_2', 'Settings', 'javascript:openSettings();');

/*
function initAdhoc() {
	setMainHeight();
	var mainWidth = _$c('main_content')[0].clientWidth;
	setTabsetWidth(mainWidth);
	_$('dsnCont').style.height = _$c('main_content')[0].clientHeight - 16 + 'px';

	if (!window.stm)
		window.stm = new Object();
	else
		window.stm[window.stmidx].save();
	window.stmidx = 'datasource';
	var id = window.stmidx;
	var stmid = 'adhocStatement';
    window.stm[id] = CodeMirror.fromTextArea(_$(stmid),
            { mode: 'text/x-sql', 
              indentWithTabs: true, 
              smartIndent: true, 
              lineNumbers: true, 
              matchBrackets: true, 
              autofocus: true,
              reverseOnSave: true
            });
    //window.stm[id].setSize(940, 266);
    window.stm[id].setSize(mainWidth - 32, 266);
    window.stm[id].refresh();
    _$('result').style.width = mainWidth - 48 + 'px';
	document.title = mainWidth;
}
*/
function initAdhoc() {
    //if (restore)
    	//restoreData();
    window.stm = new Object();
    window.stmidx = 'datasource';
    var id = window.stmidx;
    window.stm[id] = CodeMirror.fromTextArea(_$('adhocStatement'),
            { mode: 'text/x-sql', 
              indentWithTabs: true, 
              smartIndent: true, 
              lineNumbers: true, 
              matchBrackets: true, 
              autofocus: true,
              reverseOnSave: true
            });
    //window.stm[id].setSize(_$c('main_content')[0].clientWidth - 32, 266);
    window.stm[id].setSize('100%', '100%');
    window.stm[id].on("change", updateLocalStorage);
    window.stm[id].refresh();
}
addEvent(window, 'load', initAdhoc);
function resizeHeight() {
	//if (_$c('fullwindow')[0]) {
		//_$('dsnCont').style.height = _$c('main_content')[0].clientHeight - 84 + 'px';
       // _$('objCont').style.height = _$('dsnCont').style.height;
	//}
	var id = window.stmidx;
    //window.stm[id].setSize(_$c('main_content')[0].clientWidth - 32, 266);
    window.stm[id].refresh();
}
function resizeWidth() {
    //if (_$c('fullwindow')[0]) {
        //_$('dsnCont').style.height = _$c('main_content')[0].clientHeight - 84 + 'px';
        //_$('objCont').style.height = _$('dsnCont').style.height;
    //}
}
function resizeAdhoc(refresh) {
	//setMainHeight();
	resizeHeight();
	//var mainWidth = _$c('main_content')[0].clientWidth;
	//_$('dsnTabs').style.width = mainWidth - (_$('dsnOpts').clientWidth + 16) + 'px';
    var id = window.stmidx;
    //window.stm[id].setSize(mainWidth - 32, 266);
    //if (refresh) 
    	window.stm[id].refresh();
    //_$('result').style.width = mainWidth - 48 + 'px';
}
if (_$c('fullwindow')[0]) {
    addEvent(window, 'resize', resizeAdhoc);
//    addEvent(window, 'load', resizeAdhoc);
}
function adhocKeyDown(e) {
	if (!e) e = window.event;
	// F9 or Ctrl-Enter
	if ((e.keyCode == 120) || (e.ctrlKey && (e.keyCode == 13)))
		execStatement(e);
}
addEvent(window, 'keydown', adhocKeyDown);
function addDsnTab() {
	var jndiname = _$v('jndiname');
	if (!jndiname) {
		Roth.getDialog('error').error('"JNDI Name" is required.');
		return false;
	}
	_$('formjndiname').value = jndiname;
	var tabset = _$('datasources');
    var ul = getChild(tabset, '0');
    for (var i = 0; i < ul.children.length; i++)
    	if (getChild(ul.children[i], '0.1').innerHTML == jndiname) {
    		Roth.getDialog('error').error('A tab for this datasource is already open.');
    		return false;
    	}
	var c = Roth.tabset.getTabCount('datasources');
    Roth.tabset.newTab('datasources', 'datasource_' + c, jndiname, 'database', true);
    Roth.tabset.newPage('dsnCont', 'datasource_' + c, true);
    Roth.tabset.setSelected('datasources', 'datasource_' + c);
    initAdhoc();
    //Roth.ajax.htmlAction('datasource_' + c, '/RothDeveloper/Developer/load', 'jndiname=' + jndiname + '&index=' + c, null, null, function() { initAdhoc(c, jndiname); });
    return true;
}
var execControl = new Object();
function execStatement(e, inproc) {
	//var src = (e.target) ? e.target : e.srcElement;
    var form = _$('execForm');
    if (!inproc) {
    	execControl.e = e;
    	execControl.value = null;
    	execControl.params = false;
    	execControl.password = false;
        _$('params').value = '';
        _$('password').value = '';
    }
    if (validateAdhoc(execControl.e)) {
        Roth.getDialog('wait').wait();
        submitForm(form, '');
    }
}
var dsitbl = null;
function selDsiTbl(which) {
	if (which == dsitbl)
		return false;
	which.className = which.className.or('dsiTblSel');
	which.style.background = '';
	if (dsitbl) dsitbl.className = dsitbl.className.nor('dsiTblSel');
	dsitbl = which;
	return true;
}
function getStatement(e, message) {
    if (!e) e = window.event;
    var value = e.shiftKey ? window.stm['datasource'].getSelection().trim() : getAdhocStmt();
    if (!message)
    	message = 'A statement is required.';
    if (value == '') { 
        Roth.getDialog('error').error(message); 
        return false; 
    }
    var result = value.reverse();
    var s = value; 
    var i = s.indexOf(' ');
    s = s.substring(0, i).toUpperCase().trim();
    if (s != 'SELECT' && s != 'WITH') {
        Roth.getDialog('error').error('Only a SELECT or WITH statement may be used.'); 
        return null; 
    }
    return encodeURIComponent(result);
}
function getAdhocStmt(all) {
	if (all) {
    	window.stm['datasource'].save();
    	return _$v('adhocStatement');
    }
    var curs = window.stm['datasource'].getCursor();
    var stmt = window.stm['datasource'].getLine(curs.line);
    // get prev lines
    var sc = false;
    var ln = curs.line - 1;
    while ((ln >= 0) && !sc) {
        var line = window.stm['datasource'].getLine(ln);
        sc = line.trim().endsWith(';');
        if (!sc) stmt = line + '\n' + stmt;
        ln--;
    }
    // get next lines
    sc = stmt.contains(';');
    ln = curs.line + 1;
    while ((ln < window.stm['datasource'].lineCount()) && !sc) {
        var line = window.stm['datasource'].getLine(ln);
        stmt += '\n' + line;
        sc = line.contains(';');
        ln++;
    }
    stmt = stmt.trim();
    if (!all && stmt.endsWith(';'))
    	stmt = stmt.substr(0, stmt.length - 1);
    return stmt.trim();
}
function validateAdhoc(e) {
    if (!e) e = window.event;
    /* Obsolete error check.  Remove after sure that it's not needed.
    if (!_$v('jndiNameSelect') || (_$v('jndiNameSelect').trim() == '')) {
        Roth.getDialog('error').error('A data source is required.');
        return false;
    }
    */
    if (!execControl.value) {
        _$v('jndiname', _$v('jndiNameSelect'));
        var value = e.shiftKey ? window.stm['datasource'].getSelection().trim() : getAdhocStmt();
        if (value == '') { 
            Roth.getDialog('error').error('A statement is required.'); 
            return false; 
        }
        _$v('adhocStatementSend', value.reverse());
        execControl.value = value;
    }
    var s = execControl.value.trim();
    var p = lesserOfPos(s.indexOf(' '), s.indexOf('\n'));
    //s = s.substring(0, 6).trim().toUpperCase();
    s = s.substring(0, p).trim().toUpperCase();
    if ((s != 'SELECT') && (s != 'INSERT') && (s != 'UPDATE') && (s != 'DELETE') &&
        (s != 'CREATE') && (s != 'ALTER') && (s != 'DECLARE') && (s != 'DROP') &&
        (s != 'WITH') && (s != 'DESCRIBE') && (s != 'EXPLAIN') && (s != 'SHOW')) {
        Roth.getDialog('error').error('Invalid statement.');
        return false;
    }
    if (!execControl.params) {
    	execControl.params = true;
        var params = detectParams(execControl.value);
        if (params && (_$('params').value == '')) {
        	var callback = function() {
        		var x = 0;
        		var s = _$('name' + x);
        		while (s) {
        			var n = _$v('name' + x);
        			if (paramCache[n]) {
            			_$v('type' + x, paramCache[n].type);
            			if (paramCache[n].type == 'DATE') _$v('valued' + x, paramCache[n].value);
            			else if (paramCache[n].type == 'DATETIME') _$v('valuedt' + x, paramCache[n].value);
            			else _$v('valuesn' + x, paramCache[n].value);
            			var d = paramCache[n].type == 'DATE';
                        var dt = paramCache[n].type == 'DATETIME';
            			_$('vn' + x).style.display = !d && !dt ? 'block' : 'none';
                        _$('vd' + x).style.display = d ? 'block' : 'none';
                        _$('vdt' + x).style.display = dt ? 'block' : 'none';
            		}
        			x++;
        			s = _$('name' + x);
        		}
        	};
        	Roth.execDialog('paramDlg', '/RothDeveloper/developer/_params.jsp', 'params=' + params.joinCSV(), 'Parameters', 'filter', null, null, null, null, null, callback);
        	return false;
        }
    }
    if (!execControl.password) {
    	execControl.password = true;
        if ((s != 'SELECT') && (s != 'WITH') && (s != 'DESCRIBE') && (s != 'EXPLAIN') && (s != 'SHOW') && (_$('password').value == '')) { 
            Roth.execDialog('pwdDlg', '/RothDeveloper/developer/_password.jsp', null, 'Password', 'key', '_password'); 
            return false; 
        }
    }
    return true;
}
var paramCache = new Object();
function detectParams(statement) {
	var params = null;
	var push = function() {
		if (!params) params = new Array();
		if (params.find(v => v === p))
			return;
        var x = params.length;
        params[x] = p;
        p = '';
        param = false;
	}
	var escape = false;
	var param = false;
	var prid = false; // prior char was ident
	var prie = false; // prior char was colon
	var p = '';
	for (var i = 0; i < statement.length; i++) {
		var c = statement.charAt(i);
		var colon = c == ':';
		var ident = ('' + c).match(/^[a-z0-9|_]+$/i);
		if (colon && !escape && !prid && !prie) escape = true;
		else if (colon && escape) escape = false; // Not a param either
		else if (ident && escape) { escape = false; param = true; }
		if (param && ident) p += c;
		else if (!ident) param = false;
		if (!param && (p != '')) push();
		prid = ident;
		prie = colon;
	}
	if (p != '') push();
	return params;
}
function setOrientation() {
	let jndiName = _$v('jndiname');
	window.location = `/RothDeveloper/Developer?orientation=${editOrientation}&jndiName=${jndiName}`;
}
function jndiChange(jndiName) {
	_$('jndiname').value = jndiName;
	let label = _$('jndinameLabel');
	if (label)
		label.innerText = jndiName;
	getChild(_$('dsiTabs'), '0.0.1').click();
    _$('objCont').innerHTML = '';
    Roth.ajax.selectUpdate({action: contextRoot + "/Developer/getSchemaOptions", params: "jndiName=" + jndiName, selectId: "__schemaSel", callback: () => {
		window.loading = true;
		restoreLocalStorage();
		window.loading = undefined;
	}});
}
function schemaChange(schema) {
	_$('schema').value = schema;
	let label = _$('schemaLabel');
	if (label)
		label.innerText = schema;
	if (!window.loading && !window.updating) 
		updateLocalStorage();
}
let updateLocalStorage = () => {
	if (window.loading)
		return;
	window.updating = true;
	let jndiName = _$('jndiname').value;
	let schema = _$('schema').value || '';
	let limit = _$('limit').value || 500;
	let maxlen = _$('maxlen').value || 50;
	localStorage.setItem(`${jndiName}_sql`, getAdhocStmt(true) || '');
	localStorage.setItem(`${jndiName}_result`, _$('result').children.length > 0 ? _$('result').children[0].innerText : '')
	localStorage.setItem(`${jndiName}_limit`, limit);
	localStorage.setItem(`${jndiName}_maxlen`, maxlen);
	localStorage.setItem(`${jndiName}_schema`, schema);
	window.updating = undefined;
};
let restoreLocalStorage = () => {
	if (window.updating)
		return;
	window.loading = true;
	let jndiName = _$('jndiname').value;
	let statement = localStorage.getItem(`${jndiName}_sql`) || '';
	_$v('adhocStatement', statement);
	window.stm[window.stmidx].setValue(statement);
	window.stm[window.stmidx].refresh();  
	_$('result').innerHTML = '';
	_$('result').appendChild(document.createElement('PRE'));
	_$('result').children[0].innerText = localStorage.getItem(`${jndiName}_result`);
	_$('limit').value = localStorage.getItem(`${jndiName}_limit`);
	setValue(_$("rowLimit"), _$v('limit'));
	_$('maxlen').value = localStorage.getItem(`${jndiName}_maxlen`);
	setValue(_$("maxLength"), _$v('maxlen'));
	_$('schema').value = localStorage.getItem(`${jndiName}_schema`);
	setValue(_$("schemaSel"), _$v('schema'));
	setValue(_$("__schemaSel"), _$v('schema'));
	window.loading = false;
}
addEvent(window, 'load', () => { window.loading = true; restoreLocalStorage(); window.loading = undefined; });