/**
 * 
 */
 
const Elements = {
	set: (obj, props) => {
		for (const [key, value] of Object.entries(props)) {
			obj[key] = value;
		}
	},
	newElement: (args) => {
		const {type, text, className, style, props, children} = args;
		const e = document.createElement(type);
		if (text)
			e.appendChild(document.createTextNode(text));
		if (className)
			e.className = className;
		if (style)
			Elements.set(e.style, style);
		if (props)
			Elements.set(e, props);
		if (children)
			for (const c of children)
				e.appendChild(c);
		return e;
	},
	newBreak: (height = "0px") => {
		return Elements.newElement({type: "div", className: "rbreak", style: {height}});
	},
	newCanvas: (args) => {
		return Elements.newElement({...args, type: "canvas"});
	},
	newSpan: (args) => {
		return Elements.newElement({...args, type: "span"});
	},
	newDiv: (args) => {
		return Elements.newElement({...args, type: "div"});
	},
	newIcon: (iconName) => { 
		// If icon name is just the name, then Solid is used.  Otherwise a single character followed by a dot, followed by 
		// the icon name will allow selection of style: e.g. "r.check" will do Regular check, and "s.check" will do Solid check.
		const icon = iconName.split(".");
		const className = (icon.length > 1 ? "fa" + icon[0] : "fas") + " fa-" + (icon.length > 1 ? icon[1] : icon[0]);
		return Elements.newElement({type: "i", className});
	},
	newButton: (args) => {
		const {text, iconName, props} = args;
		// props: {id: "", type: "", href: "", onclick: ""}
		const children = [];
		if (text)
			children.push(Elements.newSpan({text}));
		if (iconName)
			children.push(Elements.newIcon(iconName));
		return Elements.newElement({type: "a", className: "roth-button", props, children: [
			Elements.newSpan({children})
		]});
	},
	newForm: (args) => { // Example: newForm({action: `${contextRoot}\Path`, method: "AJAX", props: {onajax: () => { do stuff; }}});
		let {action, method, props, children} = args;
		children.push(Elements.newHidden({name: "_csrf-token", value: ""}));  // FIX THIS!
		if (method === "AJAX")
			props = {...props, action, method: "POST", ajax: "AJAX"};
		else
			props = {...props, action, method};
		return Elements.newElement({type: "form", props, children});
	},
	newHidden: (args) => { // Example: Elements.newHidden({name: "_csrf-token", value: ""});
		return Elements.newElement({type: "input", props: {type: "hidden", ...args}})
	},
	newTextBox: (args) => {
		const {password, width = "100%", text, title, props} = args;
		if (!width)
			width = "100%";
		return Elements.newDiv({className: "roth-wrap", style: {width}, children: [
			Elements.newElement({type: "label", for: "", text, title}),
			Elements.newDiv({className: "roth-input", children: [
				Elements.newElement({type: "input", props: {type: password ? "password" : "text", ...props}})
			]})
		]});
	},
	newDataGrid: (args) => { // Work in progress.
		const dg = Elements.newElement({type: "div", className: "rgrd", children: [	
			Elements.newElement({type: "div", className: "rgrdh"}),
			Elements.newElement({type: "div", className: "rgrdm", children: [
				Elements.newElement({type: "div", className: "rscr", props: {
					onscroll: event => { getChild(getPrevSibling(this.parentNode), 0).scrollLeft = this.scrollLeft; }
				}}),
			]}),
			Elements.newElement({type: "div", className: "rgrdf"})
		]});
		return dg;
	} 
};

const observer = new MutationObserver(mutations => {
  mutations.forEach(mutation => {
    if (mutation.type === 'childList') {
      mutation.addedNodes.forEach(node => {
        if (node.classList && node.classList.contains('your-class')) {
          // Do something with the new element
          console.log('Element with class "your-class" added:', node);
        }
      });
    }
  });
});

observer.observe(document.body, { childList: true, subtree: true });