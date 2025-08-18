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
package com.roth.tags.html;

import java.util.ArrayList;

import com.roth.base.util.Data;
import com.roth.tags.el.Resource;
import com.roth.tags.html.util.OptionData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

public class Button extends ActionTag implements OptionTag {
	private static final long serialVersionUID = 1621185449973978731L;

	/*
	private static final String TEMPLATE = """
		<a id="%s" href="%s" onclick="%s" class="roth-button" role="button" title="%s">
			<span>
				<span>%s</span>%s
			</span>
		</a>
		""";
	*/
	
	private static final String[][] ATTRIBUTES = {{"id", "href", "onclick", "title"}};
	private static final String[] ENTITIES = {"BODY", "ICON"};
	private static final String TEMPLATE = """
		<a class="roth-button" role="button" {ATTRIBUTES_0}>
			<span>
				<span>{BODY}</span>{ICON}
			</span>
		</a>
		""";
	
	@Override
	public String[][] getAttributes() { return ATTRIBUTES; }
	@Override
	public String[] getEntities() { return ENTITIES; }
	@Override
	public String getTemplate() { return TEMPLATE; }
	
	/*
	 * Attribute names with underscores indicate the name for getValue, but only 
	 * the part before the underscore is used as the actual attribute name for 
	 * rendering.
	 * 
	 * Entity names indicate what entities to expect via getValue.  This would
	 * include the body or other items like icons, etc.
	 * 
	 * Template is the HTML structure of the component described by the tag.
	 */

	
	// Attribute Setters
	public void setCaption(String caption) { setValue("caption", localize(caption)); }
	public void setHtmlCallback(boolean htmlCallback) { setValue("_htmlCallback", htmlCallback); }
	public void setIconName(String iconName) { setValue("iconName", iconName.toLowerCase()); }
	public void setIconAlign(String iconAlign) { setValue("iconAlign", iconAlign.toLowerCase()); }
	public void setOverlayName(String overlayName) { setValue("overlayName", overlayName.toLowerCase()); }
	public void setRolesAllowed(String rolesAllowed) { setValue("_rolesAllowed", rolesAllowed); }
	public void setRowDblClick(boolean rowDblClick) { setValue("_rowDblClick", rowDblClick); }
	public void setSendParams(boolean sendParams) { setValue("_sendParams", sendParams); }
	public void setType(String type) { processType(type); }
	
	// Event Handler Setters
	public void setOnValidate(String onValidate) { setValue("_onValidate", onValidate); }
	
	public int doStartTag() throws JspException {
		return EVAL_BODY_INCLUDE;
	}
	
	// doEndTag
	public int doEndTag() throws JspException {
		if (!hasAccess())
			return EVAL_PAGE;
		String onval = (String)getValue("_onValidate");
		String onclick = (String)getValue("onclick");
		if (onval != null) setValue("onclick", onval + Data.nvl(onclick));
		DataGrid g = (DataGrid)findAncestorWithClass(this, DataGrid.class);
		if (g == null) print(getButton());
		else {
			if (getBooleanValue("_htmlCallback", false)) processHtmlCallback();
			int rowIndex = ((Integer)pageContext.getAttribute("rowIndex")).intValue();
			if (rowIndex == -1) g.addButton(getButton());
			else if (getValue("iconName").equals("open") || getValue("iconName").equals("view") ||
					 getValue("iconName").equals("edit") || getBooleanValue("_rowDblClick", false)) 
				g.setValue("rowOnDblClick_" + rowIndex, Data.nvl(onval) + ((String)getValue("onclick")).replaceAll("this\\.href", "'" + getActionUrl() + "'"));
		}
		release();
        return EVAL_PAGE;
	}
	
	protected String getButton() {
		String type = getStringValue("type");
		evalHrefClickMap();
		String href = getActionUrl();
		String onclick = getStringValue("onclick");
		String returnFalse = (Data.nvl(onclick).trim().endsWith(";") ? "" : ";") + "return false;";
		if (href.equals("#"))
			onclick += returnFalse;
		String caption = getStringValue("caption");
		String imageName = getStringValue("iconName");
		String image = imageName == null ? null : 
			           imageName.contains(",") ? getStackedIcon(imageName.split(",")) : 
			           getIcon(imageName);
		String title = getStringValue("title");
		return getButton(type, getId(), href, onclick, caption, image, title, getHTMLAttributes());
	}
	
	protected void processType(String type) {
		setValue("type", type);
		setValue("iconName", Resource.getStringSansLocale(pageContext, "com/roth/tags/html/resource/icon_types", type));
		setValue("caption", Resource.getString(pageContext, "com/roth/tags/html/resource/icon", type));
		DataGrid g = (DataGrid)findAncestorWithClass(this, DataGrid.class);
		
		if (g != null) {
			String dialogCaption = g.getDialogCaption();
			if (dialogCaption != null)
				dialogCaption = ", '" + dialogCaption + "'";
			if (type.equals("view")) setValue("onclick", "Roth.table.open('" + g.getId() + "', this.href, 'view'" + dialogCaption + "); return false;");
			else if (type.equals("open")) setValue("onclick", "Roth.table.open('" + g.getId() + "', this.href, 'open'" + dialogCaption + "); return false;");
			else if (type.equals("add")) setValue("onclick", "Roth.table.open('" + g.getId() + "', this.href, 'add'" + dialogCaption + "); return false;");
			else if (type.equals("edit")) setValue("onclick", "Roth.table.open('" + g.getId() + "', this.href, 'edit'" + dialogCaption + "); return false;");
			else if (type.equals("delete")) setValue("onclick", "Roth.table.confirmDelete('" + g.getId() + "', this.href); return false;");
		}
	}
	
	protected void processHtmlCallback() {
		DataGrid g = (DataGrid)findAncestorWithClass(this, DataGrid.class);
		if (g != null) setValue("onclick", "Roth.table.htmlAction('" + g.getId() + "', this.href, " + getBooleanValue("_sendParams", false) + "); return false;");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addOption(OptionData option) {
		if (getValue("_options") == null) setValue("_options", new ArrayList<OptionData>());
		((ArrayList<OptionData>)getValue("_options")).add(option);
	}
	
	@SuppressWarnings("unchecked")
	protected String getOptions() throws JspException {
		String result = "";
		
		// Otherwise use the results of any option tags.
		ArrayList<OptionData> vo = (ArrayList<OptionData>)getValue("_options");
		if (vo != null)
			for (int i = 0; i < vo.size(); i++)
				result += getOption(vo.get(i).getValue(), vo.get(i).getCaption());
		
		return result;
	}
	
	protected String getOption(String value, String caption) {
		return tag("option", attr("value", value), caption);
	}
	
	protected boolean hasAccess() {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String rolesAllowed = getStringValue("_rolesAllowed");
		if (rolesAllowed == null)
			return true;
		if (request.getUserPrincipal() == null)
			return false;
		else {
			String[] roles = rolesAllowed.split(",");
			for (String role : roles)
				if (request.isUserInRole(role))
					return true;
			return false;
		}
	}
}
