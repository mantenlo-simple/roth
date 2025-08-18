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
//#[set path=com/roth/tags/html/resource/common]
function doAuthentication(redirect) {
	//Roth.execDialog('login', portalRoot + '/login.jsp', null, 'Login', 'login', 'userid');
	// php = Protocol Host Port
	//var php = location.protocol + '//' + location.host;
	var redirectRoot = location.pathname;
	redirectRoot = redirectRoot.substring(0, redirectRoot.indexOf('/', 1));
	redirectRoot += (redirectRoot == portalRoot) ? '/Auth' : '/auth.jsp';
	var themeId = !_$('_param_themeId') ? "" : _$('_param_themeId').value;
	var fontSize = parseFloat(window.getComputedStyle(document.body, null).getPropertyValue('font-size'));
	var dim = getWindowDimensions();
	var dlg = Roth.getDialog('login');
    dlg.caption = Roth.createIconCaption('sign-in-alt', '#[resource login]');
    dlg.message = ((!redirect) ? 'Your session has expired.<br/>Please login to continue.<br/>' + 
    	                         '<div class="jbreak" style="height: 0.5em;"></div>' : '') +
    	          '<iframe id="loginframe" style="" src="' + redirectRoot + '?' + (redirect ? 'redirect=' + encodeURIComponent(redirect) + '&' : '') + 'themeId=' + themeId + '&fontSize=' + fontSize + '" ' +
    	                   'style="border: none;" frameBorder="0" ' +  
    	                  // (isMobile ? '' :
    	                   'onload="Roth.sizeIframe(\'loginframe\', \'login\');"' + '/>';
    dlg.modal = true;
    dlg.show();
}
function authKeyDown(which, e) {
    if (window.event) e = window.event;
    if (e.keyCode == 13) {
    	if (which == _$('jsc_username')) {
        	_$('jsc_password').focus();
        }
        else if (which == _$('jsc_password')) {
            Roth.getDialog('wait').wait('Please wait while you are logged in...');
            document.forms[0].submit();
        }
    }
}
function getDaysToExpire() {
	if (!_$('pm_user.0')) return;
	var callback = function () {
		if ((this.readyState == 4) && (this.status == 200)) {
            var d = this.responseText;
            if (d.trim() != '') {
            	d = parseInt(d);
            	if (d > 10) {
            		var div = _$('pwdExpireWarning');
            		if (div) document.body.removeChild(div);
            		return;
            	}
            	var div = document.createElement('DIV');
            	div.id = 'pwdExpireWarning';
            	div.className = 'pwdExpire';
            	div.innerHTML = 'Your password will expire ' + (d == 1 ? 'tomorrow.' : 'in ' + d + ' days.');
            	document.body.appendChild(div);
            }
        }
	}
	executeAjax({callback: callback, method: 'GET', url: portalRoot + '/Profile/getDaysToExpire'});
}
addEvent(window, 'load', getDaysToExpire);
function openAdhoc() {
	Roth.getDialog('wait').wait();
	Roth.execDialog('adhoctool', developerRoot + '/Developer', 'dialog', 'Adhoc SQL Tool', 'sql', null, null, null, true, null,
		function() { 
		    window.stm = CodeMirror.fromTextArea(_$('adhocStatement'),
	                { mode: 'text/x-sql', 
	                  indentWithTabs: true, 
	                  smartIndent: true, 
	                  lineNumbers: true, 
	                  matchBrackets: true, 
	                  autofocus: true,
	                  reverseOnSave: true
		            });
	        window.stm.setSize(600, 300);
	        window.stm.refresh();
		});
}
function loadExtResource(filetype, filename, onload){
	if (filetype == 'js'){ //if filename is an external JavaScript file
		var fileref = document.createElement('script')
		fileref.setAttribute('type', 'text/javascript')
		fileref.setAttribute('src', filename)
		fileref.onload = onload;
	}
	else if (filetype == 'css'){ //if filename is an external CSS file
		var fileref = document.createElement('link')
		fileref.setAttribute('rel', 'stylesheet')
		fileref.setAttribute('type', 'text/css')
		fileref.setAttribute('href', filename)
	}
	if (typeof fileref != 'undefined')
		document.getElementsByTagName('head')[0].appendChild(fileref)
}
var isCMinit = false;
function initCodeMirror() {
	if (isCMinit)
		return;
	loadExtResource('css', portalRoot + '/codemirror/codemirror.css');
	loadExtResource('js', portalRoot + '/codemirror/codemirror.js', function() {
		loadExtResource('js', portalRoot + '/codemirror/xml.js');
		loadExtResource('js', portalRoot + '/codemirror/javascript.js');
		loadExtResource('js', portalRoot + '/codemirror/css.js');
		loadExtResource('js', portalRoot + '/codemirror/vbscript.js');
		loadExtResource('js', portalRoot + '/codemirror/htmlmixed.js');
		loadExtResource('js', portalRoot + '/codemirror/sql.js');
	});
	isCMinit = true;
}
var isQJSinit = false;
function initQuillJS() {
	loadExtResource('css', portalRoot + '/quill/quill.snow.css');
	loadExtResource('css', portalRoot + '/quill/quill.imageUploader.min.css');
	loadExtResource('css', portalRoot + '/quill/styles/vs.css');
	loadExtResource('js', portalRoot + '/quill/highlight.pack.js');
	loadExtResource('js', portalRoot + '/quill/quill.min.js', function() {
		loadExtResource('js', portalRoot + '/quill/quill.imageUploader.min.js');
	});
	isQJSinit = true;	
}
var isCJSinit = false;
function initChartJs(bundle) {
	if (isCJSinit)
		return;
	if (bundle)
		loadExtResource('js', portalRoot + '/chartjs/Chart.bundle.min.js');
	else
		loadExtResource('js', portalRoot + '/chartjs/Chart.min.js');
	isCJSinit = true;
}
var isTinyMCEinit = false;
function initTinyMCE() {
	if (isTinyMCEinit)
		return;
	loadExtResource('js', portalRoot + '/tinymce/js/prism.js');
	loadExtResource('js', portalRoot + '/tinymce/js/tinymce.min.js');
	isTinyMCEinit = true;
}
function _reflect(source) {
	return portalRoot + "/Home/reflect?src=" + encodeURIComponent(source);
}

function initTinyMCEcontainer(containerId) {
	let mceInputs = _$(containerId).querySelectorAll(".roth-tinymce");
	mceInputs.forEach(e => {
		console.log("attempting to init textarea#" + e.id);
		initTinyMCEeditor({id: e.id});
	});
}
function initTinyMCEeditor(obj) {
	let {id, containerId} = obj;
	if (containerId) {
		initTinyMCEcontainer(containerId);
		return;
	}
	var useDarkMode = false; //window.matchMedia('(prefers-color-scheme: dark)').matches;
	
	console.log("initing textarea#" + id);
	let tinymce = window.tinymce;
	tinymce.remove('textarea#' + id);
	tinymce.init({
		selector: 'textarea#' + id,
		entity_encoding : 'named',
		plugins: 'print preview paste importcss searchreplace autolink autosave save directionality code visualblocks visualchars fullscreen image link media template codesample table charmap hr pagebreak nonbreaking anchor toc insertdatetime advlist lists wordcount imagetools textpattern noneditable help charmap quickbars emoticons',
		menubar: 'edit view insert format table help', // 'file edit view insert format tools table help',
		toolbar: 'undo redo | bold italic underline strikethrough | fontselect fontsizeselect formatselect | alignleft aligncenter alignright alignjustify | outdent indent |  numlist bullist | forecolor backcolor removeformat | pagebreak | charmap emoticons | fullscreen  preview print | insertfile image media template link anchor codesample | ltr rtl',
		toolbar_sticky: true,
		image_advtab: true,
		importcss_append: true,
		image_caption: true,
		//images_upload_url: 'upload',
		document_base_url: ('/'),
		relative_urls: true,
		convert_urls: false,
		images_upload_handler: (file, progressHandler, failureHandler, successHandler) => {
			return new Promise((resolve, reject) => {
				const formData = new FormData();
				let tokens = document.getElementsByName("_csrf-token");
				formData.append("_csrf-token", tokens[tokens.length - 1].value);
				formData.append("image", file.blob());

				fetch("/Tricosa/Image/upload", {
					method: "POST",
					body: formData
				})
				.then(response => response.json())
				.then(result => {
					if (result.data.error)
						throw new Error(result.data.error);
					if (successHandler) {
						if (progressHandler)
							progressHandler(100);
						successHandler(result.data.url);
					}
					else
						resolve(result.data.url);
				})
				.catch(error => {
					reject("Upload failed");
					if (failureHandler)
						failureHandler(error);
					else
						Roth.getDialog('error').error(error);
				});
			});
		},
		quickbars_selection_toolbar: 'bold italic | quicklink h2 h3 blockquote quickimage quicktable',
		noneditable_noneditable_class: 'mceNonEditable',
		height: '100%',
		width: '100%',
		resize: false,
		toolbar_mode: 'sliding',
		contextmenu: 'link image imagetools table',
		skin: useDarkMode ? 'oxide-dark' : 'tinymce-5',
		content_css: useDarkMode ? 'dark' : 'default',
		content_style: 'body { font-family:Helvetica,Arial,sans-serif; font-size:14px }',
		codesample_global_prismjs: true,
		codesample_languages: [
			{ text: 'CSS', value: 'css' },
			{ text: "XML", value: "xml" },
        	{ text: "HTML", value: "html" },
			{ text: 'Java', value: 'java' },
			{ text: 'JavaScript', value: 'javascript' },
			{ text: 'JSON', value: 'json' },
			{ text: 'React', value: 'jsx' },
			{ text: 'SQL', value: 'sql' }
		],
	});
}
/*



var useDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;

tinymce.init({
  selector: 'textarea#open-source-plugins',
  plugins: 'print preview paste importcss searchreplace autolink autosave save directionality code visualblocks visualchars fullscreen image link media template codesample table charmap hr pagebreak nonbreaking anchor toc insertdatetime advlist lists wordcount imagetools textpattern noneditable help charmap quickbars emoticons',
  imagetools_cors_hosts: ['picsum.photos'],
  menubar: 'file edit view insert format tools table help',
  toolbar: 'undo redo | bold italic underline strikethrough | fontselect fontsizeselect formatselect | alignleft aligncenter alignright alignjustify | outdent indent |  numlist bullist | forecolor backcolor removeformat | pagebreak | charmap emoticons | fullscreen  preview save print | insertfile image media template link anchor codesample | ltr rtl',
  toolbar_sticky: true,
  autosave_ask_before_unload: true,
  autosave_interval: '30s',
  autosave_prefix: '{path}{query}-{id}-',
  autosave_restore_when_empty: false,
  autosave_retention: '2m',
  image_advtab: true,
  link_list: [
    { title: 'My page 1', value: 'https://www.tiny.cloud' },
    { title: 'My page 2', value: 'http://www.moxiecode.com' }
  ],
  image_list: [
    { title: 'My page 1', value: 'https://www.tiny.cloud' },
    { title: 'My page 2', value: 'http://www.moxiecode.com' }
  ],
  image_class_list: [
    { title: 'None', value: '' },
    { title: 'Some class', value: 'class-name' }
  ],
  importcss_append: true,
  file_picker_callback: function (callback, value, meta) {
    // Provide file and text for the link dialog 
    if (meta.filetype === 'file') {
      callback('https://www.google.com/logos/google.jpg', { text: 'My text' });
    }

    // Provide image and alt text for the image dialog 
    if (meta.filetype === 'image') {
      callback('https://www.google.com/logos/google.jpg', { alt: 'My alt text' });
    }

    // Provide alternative source and posted for the media dialog 
    if (meta.filetype === 'media') {
      callback('movie.mp4', { source2: 'alt.ogg', poster: 'https://www.google.com/logos/google.jpg' });
    }
  },
  templates: [
        { title: 'New Table', description: 'creates a new table', content: '<div class="mceTmpl"><table width="98%%"  border="0" cellspacing="0" cellpadding="0"><tr><th scope="col"> </th><th scope="col"> </th></tr><tr><td> </td><td> </td></tr></table></div>' },
    { title: 'Starting my story', description: 'A cure for writers block', content: 'Once upon a time...' },
    { title: 'New list with dates', description: 'New List with dates', content: '<div class="mceTmpl"><span class="cdate">cdate</span><br /><span class="mdate">mdate</span><h2>My List</h2><ul><li></li><li></li></ul></div>' }
  ],
  template_cdate_format: '[Date Created (CDATE): %m/%d/%Y : %H:%M:%S]',
  template_mdate_format: '[Date Modified (MDATE): %m/%d/%Y : %H:%M:%S]',
  height: 600,
  image_caption: true,
  quickbars_selection_toolbar: 'bold italic | quicklink h2 h3 blockquote quickimage quicktable',
  noneditable_noneditable_class: 'mceNonEditable',
  toolbar_mode: 'sliding',
  contextmenu: 'link image imagetools table',
  skin: useDarkMode ? 'oxide-dark' : 'oxide',
  content_css: useDarkMode ? 'dark' : 'default',
  content_style: 'body { font-family:Helvetica,Arial,sans-serif; font-size:14px }'
 });

*/