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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.base.util.GenericComparator;
import com.roth.portal.model.UserProperty;
import com.roth.tags.html.util.ColumnData;
import com.roth.tags.html.util.DataGridParams;

import jakarta.el.ELContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

public class DataGrid extends HtmlTag {
	private static final long serialVersionUID = -3578779147532909159L;
	
	private int rowIndex;
	private Object row;
	
	/*
	 * Additions:
	 * action - the action called by the data grid whenever a refresh, 
	 *          page change, sort, or search is performed.
	 * method - the method used to call the action.  The possible values
	 *          are 'POST' and 'AJAX', with the default being 'POST'.
	 * containerId - the container that will be refreshed on an AJAX
	 *               response.  If this is specified, then the method
	 *               must be 'AJAX'.  The onAjaxReponse and onAjaxError
	 *               event handlers are ignored if this is specified, 
	 *               as the data grid will rely on it's own handlers.
	 * onAjaxResponse - the event handler to call upon successful post,
	 *                  if method is 'AJAX', and containerId is not 
	 *                  specified.
	 * onAjaxError - the event handler to call upon failure to post,
	 *               if method is 'AJAX', and containerId is not specified.
	 *               
	 *                
	 * paging - whether paging will occur.
	 * externalPaging - whether the paging will be handled by the data grid
	 *                  or externally, in which case the data grid will
	 *                  assume that the entire data source is a page.
	 */
	
	// Attribute Setters
	public void setAction(String action) { setValue("action", action); }
	public void setColumnMoving(boolean columnMoving) { setValue("columnMoving", columnMoving); }
	public void setColumnSizing(boolean columnSizing) { setValue("columnSizing", columnSizing); }
	public void setContainerId(String containerId) { setValue("containerId", containerId); }
	public void setDataSource(String dataSource) { setValue("_dataSource", dataSource); }
	public void setDialogCaption(String dialogCaption) { setValue("_dialogCaption", dialogCaption); }
	public void setExporting(boolean exporting) { setValue("exporting", exporting); } 
	public void setExternalPaging(boolean externalPaging) { setValue("externalPaging", externalPaging); }
	public void setHeight(String height) { setValue("height", height); }
	public void setMethod(String method) { setValue("method", method); }
	public void setMultiSelect(boolean multiSelect) { setValue("multiSelect", multiSelect); }
	public void setPaging(boolean paging) { setValue("paging", paging); }
	public void setPageSize(int pageSize) { setValue("pageSize", pageSize); }
	//public void setRowDblClick(String rowDblClick) { setValue("rowDblClick", rowDblClick); }
	public void setRowSelect(boolean rowSelect) { setValue("rowSelect", rowSelect); }
	public void setSearching(boolean searching) { setValue("searching", searching); }
	public void setSizable(boolean sizable) { setValue("sizable", sizable); }
	public void setSorting(boolean sorting) { setValue("sorting", sorting); }
	//public void setUrl(String url) { setValue("url", url); } // <- What was this for?
	public void setWidth(String width) { setValue("width", width); }
	public void setWrapping(boolean wrapping) { setValue("wrapping", wrapping); }
	
	// Event Attribute Setters
	public void setOnAjaxResponse(String onAjaxResponse) { setValue("_onAjaxResponse", onAjaxResponse); }
	public void setOnAjaxError(String onAjaxError) { setValue("_onAjaxError", onAjaxError); }
	public void setOnDelete(String onDelete) { setValue("_onDelete", onDelete); }
	public void setOnOpen(String onOpen) { setValue("_onOpen", onOpen); }
    public void setOnSelect(String onSelect) { setValue("_onSelect", onSelect); }
	
	public Boolean getColMoving() {
		Object o = getValue("columnMoving");
		return (o == null) ? null : ((Boolean)o).booleanValue();
	}
	
	public Boolean getColSizing() {
		Object o = getValue("columnSizing");
		return (o == null) ? false : ((Boolean)o).booleanValue();
	}
	
	public Boolean getColSorting() {
		Object o = getValue("sorting");
		return (o == null) ? null : ((Boolean)o).booleanValue();
	}
	
	@SuppressWarnings("unchecked")
	public void addColumn(ColumnData column) {
		if (getValue("columns") == null) setValue("columns", new ArrayList<ColumnData>());
		((ArrayList<ColumnData>)getValue("columns")).add(column);
	}
	
	@SuppressWarnings("unchecked")
	public void addButton(String button) {
		if (getValue("buttons") == null) setValue("buttons", new ArrayList<String>());
		((ArrayList<String>)getValue("buttons")).add(button);
	}
	
	protected String getJspPageName() {
		String result = pageContext.getPage().toString()
		                           .replaceAll("\\.", "/")
		                           .replaceAll("_jsp", ".jsp")
		                           .replaceAll("_005f", "_")
		                           .replaceAll("org/apache/jsp/", "");
		int pos = result.indexOf("@");
		result = result.substring(0, pos);
		return pageContext.getServletContext().getContextPath() + "/" + result;
	}
	
	public String getDialogCaption() {
		String result = getStringValue("_dialogCaption", "");
		return result;
	}
	
	protected void processParams() {
		String pageName = getJspPageName();
		String paramsName = pageName + "[" + getValue("id") + "]";
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String username = (request.getUserPrincipal() == null) ? "<unavailable>" : request.getUserPrincipal().getName();
		UserProperty prop = null;
		if (request.getUserPrincipal() != null)
			try { prop = UserProperty.getUserProperty(username, "_j:dataGrid" + paramsName); }
			catch (SQLException e) { Log.logException(e, username); }
		setValue("_paramid", Data.digest(paramsName, "MD5"));
		DataGridParams p = (DataGridParams)pageContext.getSession().getAttribute("_j:dataGrid" + paramsName);
		if (p == null) {
			p = new DataGridParams();
			p.setPaging(getBooleanValue("paging", false));
			if (p.getPaging() && (getValue("pageSize") != null))
			    p.setPageSize((Integer)getValue("pageSize"));
			pageContext.getSession().setAttribute("_j:dataGrid" + paramsName, p);
		}
		p.setDefaultPaging(getBooleanValue("paging", false));
		if ((prop != null) && (prop.getPropertyValue() != null)) 
			try { p.parseParams(prop.getPropertyValue()); }
			catch (Exception e) { Log.logException(e, username); }
		String paramId = pageContext.getRequest().getParameter("__j:dataGrid.id");
		String params = pageContext.getRequest().getParameter("__j:dataGrid.params");
		if ((paramId != null) && paramId.equals(Data.digest(paramsName, "MD5")))
			try { p.parseParams(params); }
			catch (Exception e) { Log.logException(e, username); }
		if ((prop != null) && ((p.getSave() == 3) || p.getDoSave())) 
			try {
			    prop.setPropertyValue(p.composeParams(false));
				UserProperty.setUserProperty(prop); 
			}
			catch (Exception e) { Log.logException(e, username); }
		setValue("_params", p);
		setValue("paging", p.getPaging());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int doStartTag() throws JspException {
		if (getId() == null) setId(generateId());
		processParams();
		DataGridParams p = (DataGridParams)getValue("_params");
		Object data = getDataSourceValue();
		if (data != null) setValue("dataSource", data);
		
		if ((data != null) && p.getDoSort(data.hashCode())) {
			GenericComparator comp = new GenericComparator(p.getSortColumn(), p.getSortOrder());
			if (data instanceof List)
				Collections.sort((List)data, comp);
			else if (data.getClass().isArray())
				Arrays.sort((Object[])data, comp);
			p.setHashCode(data.hashCode());
		}
		
		Integer size = Data.sizeOf(data);
		p.setRecordCount(size);
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String username = (request.getUserPrincipal() == null) ? "<unavailable>" : request.getUserPrincipal().getName();
		
		if ((size > 0) && (p.getSearchFlag() != null)) {
			boolean found = false;
			int i = -1; int d = 1;
			if (p.getSearchFlag().equals("new")) i = -1;
			else if (p.getSearchFlag().equals("next")) i = p.getSearchRowIndex();
			else if (p.getSearchFlag().equals("previous")) {
				i = p.getSearchRowIndex();
				d = -1;
			}
			while ((!found) && (((d > 0) && (i < size)) || ((d < 0) && (i > 0)))) {
				i += d;
				if ((i >= 0) && (i < size))
					try {
						Object field = Data.fieldOf(Data.itemOf(data, i), p.getSearchColumn());
						found = field == null ? false : field.toString().toUpperCase().contains((p.getSearchValue().toUpperCase())); 
					}
					catch (Exception e) { Log.logException(e, username); }
			}
			if (found) {
				p.setSearchRowIndex(i);
				p.setPageForRow(i);
			}
			p.setSearchFlag(null);
		}
		
		boolean paging = getBooleanValue("paging", false);
		setValue("start", (paging) ? p.getPageStart() : 0);
		setValue("end", (paging) ? p.getPageEnd() : size - 1);
		
		rowIndex = -1;
		pageContext.setAttribute("rowStart", (Integer)getValue("start"));
		pageContext.setAttribute("rowIndex", rowIndex);
		return EVAL_BODY_INCLUDE; 
	}
	
	@SuppressWarnings("unchecked")
	public int doAfterBody() throws JspException {
		if (rowIndex == -1) {
			DataGridParams p = (DataGridParams)getValue("_params");
			p.setColumnData((ArrayList<ColumnData>)getValue("columns"));
		}
	    rowIndex++;
	    Object data = getValue("dataSource");
		//Integer size = Data.sizeOf(data);
		//DataGridParams p = (DataGridParams)getValue("_params");
		//p.setRecordCount(size);
		//boolean paging = getBooleanValue("paging");
		int start = (Integer)getValue("start");
		int end = (Integer)getValue("end");
		if (rowIndex < start) rowIndex = start;
		
		if (rowIndex <= end) {
			row = Data.itemOf(data, rowIndex);
			pageContext.setAttribute("rowIndex", rowIndex);
			pageContext.setAttribute("rowData", row);
			pageContext.setAttribute("rowNext", Data.itemOf(data, rowIndex + 1));
			pageContext.setAttribute("rowPrev", Data.itemOf(data, rowIndex - 1));
			return EVAL_BODY_AGAIN;
		}
		else
			return SKIP_BODY;
	}
	
	public int doEndTag() throws JspException {
		//String dgps = pageContext.getRequest().getParameter("__jDataGridParams");
		//setValue("dgp", new DataGridParams(dgps));
		// Note: getForm will not actually create a form unless there are buttons.
		try { println(getForm(getGrid(getHeader(), getBody()))); }
		catch (JspException e) { println("<div>" + e.getMessage() + "</div>"); }
		pageContext.removeAttribute("rowStart");
		pageContext.removeAttribute("rowIndex");
		pageContext.removeAttribute("rowData");
		pageContext.removeAttribute("rowNext");
		pageContext.removeAttribute("rowPrev");
		release();
		return EVAL_PAGE;
	}
	
	/** BEGIN BORROWED SECTION (from InputTag -- need to put this somewhere common; perhaps HtmlTag?) **/
	protected Object getDataSourceValue() throws JspException {
		ELContext c = pageContext.getELContext();
		String[] objs = getValue("_dataSource").toString().split("\\.");
		Object result = null;
		for (int i = 0; i < objs.length; i++) {
			String name = getDsvSegName(objs[i]);
			Integer idx = getDsvSegIndex(objs[i]);
			result = c.getELResolver().getValue(c, result, name);
			if (idx != null) result = Data.itemOf(result, idx);
		}
		return result;
	}
	
	protected Map<?,?> getDataMap(ColumnData coldata) throws JspException {
		Object o = coldata.getDataMap();
		if (o == null)
			return null;
		if (!(o instanceof Map))
			throw new JspException("The dataMap attribute requires a map object.");
		return (Map<?,?>)o;
	}
	
	protected String getDsvSegName(String source) {
		int i = source.indexOf("[");
		return (i < 0) ? source : source.substring(0, i);
	}
	
	protected Integer getDsvSegIndex(String source) throws JspException {
		int i = source.indexOf("[");
		if (i < 0) return null;
		int x = source.indexOf("]", i);
		if (x < 0) throw new JspException("Malformed EL expression index notation.");
		return (i < 0) ? null : Integer.valueOf(source.substring(i + 1, x));
	}
	/** END BORROWED SECTION **/
	
	@SuppressWarnings("rawtypes")
	protected int getTotalHeight() {
		Object data = getValue("dataSource");
		int t = (data == null)
		      ? 1
		      : (data instanceof List)
		      ? ((List)data).size()
		      : (data.getClass().isArray())
		      ? ((Object[])data).length
		      : 1;
		return t * 21 + 1;
	}
	
	@SuppressWarnings("unchecked")
	protected String getTotalWidth() {
		String t = "calc(";
		String o = "";
		ArrayList<ColumnData> coldata = (ArrayList<ColumnData>)getValue("columns");
		for (int i = 0; i < coldata.size(); i++) {
			t += o + coldata.get(i).getWidth();
			if (Data.isEmpty(o))
				o = "+";
		}
		return t + ")";
	}
	
	
	@SuppressWarnings("unchecked")
	protected String getForm(String content) {
		// If there are no buttons, there is no point in wrapping
		// this in a form, so just return the content as-is.
		if (getValue("buttons") == null) return content;
		//String formAttr = attr("method", "POST") +
		//                  attr("ajax", "AJAX") +
		//                  attr("onajaxerror", "") +
		//                  attr("onajaxresponse", "");
		String buttons = tag("div", attr("class", "rdgbreak"), "");
		for (String s : ((ArrayList<String>)getValue("buttons")))
			buttons += s + " ";
		return content + buttons; //tag("form", formAttr, content + buttons);
	}
	
	// Support Functions
	protected String getGrid(String header, String body) {
		// Width of outer table
		String width = getRemoveValue("width");
		if (width != null) {
			try { Integer.valueOf(width); width += "px"; } catch (Exception e) { /* Eat it. */ } // If no size unit was used, append "px".
			width = "width: " + width + "; ";
		}
		else
			width = "width: 100%; ";
		// Height of outer table
		String height = getRemoveValue("height");
		if (height != null) {
			try { Integer.valueOf(height); height += "px"; } catch (Exception e) { /* Eat it. */ } // If no size unit was used, append "px".
			height = "height: " + height + "; ";
		}
		else
			height = "height: 100%; ";
		// If width and/or height has been set, generate style attribute for outer table
		String tableStyle = (width == null) && (height == null) ? "" : " style=\"" + Data.nvl(width) + Data.nvl(height) + "\"";
		// Define outer table
		String jsScrollHandler = "getChild(getPrevSibling(this.parentNode), 0).scrollLeft = this.scrollLeft;";

		String a = attr("class", "rgrd") + attr("paramid", (String)getValue("_paramid"));
		if (getValue("id") != null) a += attr("id", (String)getValue("id"));
		if (getValue("containerId") != null) a += attr("containerid", (String)getValue("containerId"));
		if (getValue("action") != null) a += attr("action", pageContext.getServletContext().getContextPath() + (String)getValue("action"));
		if (getValue("_onOpen") != null) a += attr("onopen", (String)getValue("_onOpen"));
		if (getValue("_onDelete") != null) a += attr("ondelete", (String)getValue("_onDelete"));
		
		boolean paging = getBooleanValue("paging", false);
		boolean searching = getBooleanValue("searching", false);
		DataGridParams p = (DataGridParams)getValue("_params");

		if (paging) {
			a += attr("pagesize", Integer.toString(p.getPageSize())) +
			     attr("pageindex", Integer.toString(p.getPageIndex()));
		}
		if (searching && (p.getSearchColumn() != null)) {
			a += attr("searchcolumn", p.getSearchColumn()) + 
			     attr("searchvalue", p.getSearchValue()) +
			     attr("searchrowindex", Integer.toString(p.getSearchRowIndex()));
		}
		a += attr("fields", getFields(p)); 
		a += attr("save", Integer.toString(p.getSave()));
		
		//String refresh = ""; //(getValue("action") == null) ? "" : getRefreshImage();
		String scrollView = (p.getSearchColumn() == null) ? ""
			              : "<script type=\"text/javascript\">addEvent(window, 'load', function() { Roth.table.search.adjustScroll('" + getValue("id") + "'); });</script>";
		String wrappingStyle = getBooleanValue("wrapping", false) && getValue("id") != null 
				             ? tag("style", attr("type", "text/css"), 
				            		 "#" + getValue("id") + " .rtbl td .rtd { white-space: normal; } " +
				            		 "#" + getValue("id") + " .rtbl td { vertical-align: top; }") 
				             : "";
		String config = getConfig();
		String pager = getPager();
		String sizer = getSizer();
		return scrollView + wrappingStyle +
			       tag("div", a + tableStyle, 
					   tag("div", attr("class", "rgrdh"),
						   //tag("td", null, "") +
						   tag("div", attr("class", "rscrh"), header)
					     //+ tag("td", null, "")
							   ) +
					   tag("div", attr("class", "rgrdm"),
					       //tag("td", attr("class", "rgrdg"), "") +
					         tag("div", attr("class", "rscr") + //height +
					    		      attr("onscroll", jsScrollHandler), 
					    	   body)) +
					   tag("div", attr("class", "rgrdf" + (!(config + pager + sizer).isEmpty() ? "" : " rgrdf-empty")), 
						   //tag("td", null, refresh) + 
						   tag("div", attr("style", "padding-top: 0px; overflow: hidden;"), 
							   tag("div", attr("class", "rgrdp"), pager + config + "&nbsp;")) +
						   tag("div", null, sizer)));
	}
	
	protected String getHeader() {
		String jsMouseDownHandler = "return Roth.table.column._onMouseDown(event);";
		String jsMouseMoveHandler = "_$rtrmmv(event);";
		String headers = "";
		String sort = attr("sort", Boolean.toString(getBooleanValue("sorting", false)));
		DataGridParams p = (DataGridParams)getValue("_params");
		ArrayList<ColumnData> coldata = p.getColumnData();

		for (int i = 0; i < coldata.size(); i++) {
			if (!coldata.get(i).getVisible()) continue;
			ColumnData cd = coldata.get(i);
			String className = ((cd.getMovable()) ? "move" : ""); 
		    className += ((className.isEmpty()) ? "" : " ") + ((cd.getSizable()) ? "resize" : "");
		    className += ((className.isEmpty()) ? "" : " ") + ((cd.getSortable()) ? "sort" : "");
			String classAttr = (className.isEmpty()) ? "" : attr("class", className);
			String dataSource = coldata.get(i).getDataSource();
			String caption = localize(coldata.get(i).getCaption());
			String sortInd = ((dataSource == null) || !dataSource.equals(p.getSortColumn()))
			               ? "" : " " + Image.getImage((p.getSortOrder() == 1) ? "caret-up" : "caret-down");
			String sizer = getColumnSizer();
			headers += tag("th", classAttr + attr("datasource", dataSource), tag("div", attr("class", "rtd"), caption + sortInd + sizer));
		}
		
		headers += tag("th", null, "");
		
		return tag("table", attr("class", "rtbl"), 
				   getColGroup(true) 
				 + tag("tbody", attr("onselectstart", "return false;"), 
				       tag("tr", sort 
				    		   + attr("onmousedown", jsMouseDownHandler)
				    		   + attr("onmousemove", jsMouseMoveHandler), headers))); 
	}
	
	protected String getColumnSizer() {
		if (!getColSizing())
			return "";
		String eventHandlers = attr("onmousedown", "Roth.table.col.sizer.mousedown(event);") +
				               attr("onmouseup", "Roth.table.col.sizer.mouseup(event);");
		String result = tag("div", attr("class", "rtdsizer") + eventHandlers, Icon.getIcon("grip-vertical"));
		return result;
	}
	
	protected String getBody() throws JspException {
		Object data = getValue("dataSource");
		Object row;
		String rows = "";
		Integer size = Data.sizeOf(data);
		DataGridParams p = (DataGridParams)getValue("_params");
		ArrayList<ColumnData> coldata = p.getColumnData();
		p.setRecordCount(size);
		Object rowSelect = getRemoveValue("rowSelect");
		
		
		if ((size != null) && (size > 0)) {
			boolean paging = getBooleanValue("paging", false);
			int start = (paging) ? p.getPageStart() : 0;
			int end = (paging) ? p.getPageEnd() : size - 1;

			for (int i = start; i <= end; i++) {
				row = Data.itemOf(data, i);
				
				String rowattr = ""; 
				String rowOnClick = (String)getValue("rowOnClick_" + i);
				if (rowOnClick == null) rowOnClick = "";
				rowOnClick += ((rowSelect == null) || !(Boolean)rowSelect) ? "" : "rowSelect(this, event); ";
				if (!rowOnClick.isEmpty()) rowattr += attr("onclick", rowOnClick);
				String rowOnDblClick = (String)getValue("rowOnDblClick_" + i);
				if (rowOnDblClick != null) rowattr += attr("ondblclick", rowOnDblClick);
				String rowClass = (i % 2 == 0 ? "even" : "odd") + " " + Data.nvl((String)getValue("rowClass_" + i));
				rowattr += attr("class", rowClass);
				String rowStyle = (String)getValue("rowStyle_" + i);
				if (rowStyle != null) rowattr += attr("style", rowStyle);
				String cells = "";
				String keyparam = "";
				
				HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
				String userid = request.getUserPrincipal() == null ? "<unavailable>" : request.getUserPrincipal().getName();
				@SuppressWarnings("unchecked")
				Map<String,String> formats = (HashMap<String,String>)request.getSession().getAttribute("formats"); 
				
				for (int x = 0; x < coldata.size(); x++) {
					if (!coldata.get(x).getVisible() && !coldata.get(x).getKey()) continue;
					String dataSource = coldata.get(x).getDataSource();
					Map<?,?> map = getDataMap(coldata.get(x));
					try {
						Object value = getValue(i + "_" + dataSource + "_value");
						Object f = ((dataSource != null) && (value == null))
							     ? Data.fieldOf(row, dataSource)
							     : null;
						String field = (dataSource == null) 
						             ? ""
						             : (value != null)
						             ? (String)value
						             : map != null
						             ? Data.obj2Str(map.get(f), formats, coldata.get(x).getFormatName(), coldata.get(x).getPattern(), request.getLocale())
						             : (f != null)
						             ? Data.obj2Str(f, formats, coldata.get(x).getFormatName(), coldata.get(x).getPattern(), request.getLocale()) //f.toString()
						             : "";
                        String keyval = ((dataSource == null) || (dataSource.indexOf("_") == 0)) ? "" : Data.obj2Str(Data.fieldOf(row, dataSource), formats);
										             
						if (coldata.get(x).getKey()) 
							keyparam += ((keyparam.length() == 0) ? "" : "&") + dataSource + "=" + Data.nvl(keyval);
						
						if (!coldata.get(x).getVisible()) continue;
						             
						String a = "";
						// Check for class and style attributes for the column
						String cssClass = (String)getValue(i + "_" + dataSource + "_class");
						if (cssClass != null) a += attr("class", cssClass);
						String style = (String)getValue(i + "_" + dataSource + "_style");
						if (style != null) a += attr("style", style);
						if ((dataSource != null) && dataSource.equals(p.getSearchColumn()) && (Data.nvl(keyval).toUpperCase().contains(p.getSearchValue().toUpperCase())))
							a += attr("class", (i == p.getSearchRowIndex()) ? "rsrchi" : "rsrch");
						
						if (a.isEmpty()) a = null;
						cells += tag("td", a, tag("div", attr("class", "rtd"), field));
					}
					catch (Exception e) {
						Log.logException(e, userid);
						throw new JspException("Invalid datasource '" + dataSource + "';"); 
					}
				}
				if (keyparam.length() > 0) keyparam = attr("key", keyparam);
				rows += tag("tr", rowattr + keyparam + attr("recordIndex", Integer.toString(i)), cells);
			}
		}
		else if (size == 0)
			return "<br/> &nbsp; &nbsp; No data found.";
		else 
			throw new JspException("The dataSource must be a List or an Array.");

		return tag("table", attr("class", "rtbl") + attr("onmousedown", "var e = event || window.event; var which = (e.target) ? e.target : e.srcElement; return (which.nodeName == 'INPUT') || (which.nodeName == 'SELECT') || (which.nodeName == 'TEXTAREA');"), 
				   getColGroup(false)
				 + tag("tbody", attr("onselectstart", "return false;"), rows));
	}
	
	protected String getColGroup(boolean header) {
		String cols = "";
		DataGridParams p = (DataGridParams)getValue("_params");
		ArrayList<ColumnData> coldata = p.getColumnData();
		
		for (int i = 0; i < coldata.size(); i++) {
			if (!coldata.get(i).getVisible()) continue;
			String width = coldata.get(i).getWidth();
			// The "*width" attribute is for IE's benefit.  It doesn't do
			// boxes the same way as the other browsers, and to ensure that
			// everything ends up exactly the same way, we need to subtract
			// 7 pixels from IE's width. 
			
			//int ieOffset = (header) ? 9 : 7;
			//cols += tag("col", attr("style", "width: " + width + "px; *width: " + (width - ieOffset) + "px;"), null);
			cols += tag("col", attr("style", "width: " + width + ";"), null);
		}
		
		if (header) cols += tag("col", attr("style", "width: 40px;"), null);
		
		return tag("colgroup", null, cols);
	}
	
	protected String getPager() {
		boolean paging = getBooleanValue("paging", false);
		String action = (String)getValue("action");
		if (!paging || (action == null)) return "";
		DataGridParams p = (DataGridParams)getValue("_params");
		boolean isFirst = p.getPageIndex() == 1;
		boolean isLast = p.getPageIndex() == p.getPageCount();
		String pager = " " + 
		               getPagerImage("step-backward", "First Page", "first", isFirst) + "&nbsp;" +
		               getPagerImage("play fa-flip-horizontal", "Previous Page", "previous", isFirst) + "&nbsp;&nbsp;" +
		               "Page " + 
		               "<input type=\"text\" onkeydown=\"return Roth.table.pager._onKeyDown(event, this);\" value=\"" + p.getPageIndex() + "\" max=\"" + p.getPageCount() + "\" title=\"Type in a page number and press Enter to go directly to that page.\"/>" +
		               " of " + p.getPageCount() + "&nbsp;&nbsp;" +
		               getPagerImage("play", "Next Page", "next", isLast) + "&nbsp;" +
		               getPagerImage("step-forward", "Last Page", "last", isLast) + "&nbsp;&nbsp;|&nbsp;&nbsp;";
		return pager;
	}
	
	protected String getPagerImage(String imageName, String title, String page, boolean disabled) {
		if (disabled)
			return tag("span", attr("class", "disabled"), Image.getImage(imageName));
		
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String responseXsrf = (String)request.getAttribute("_csrf-token");
		String a = (disabled) ? null : attr("onclick", "tableAction('" + getValue("id") + "', '" + responseXsrf.replace("'", "\\'").replace("\"", "&quot;") + "', 'page=" + page + "')");
		if (title != null) a += attr("title", title);
		return Image.getImage(imageName, null, a);
	}
	
	protected String getRefreshImage() {
		return Image.getImage("refresh", null, attr("onclick", "tableAction('" + getValue("id") + "')"));
	}
	
	protected String getConfig() {
		if ((String)getValue("action") == null) return "";
		String result = ""; //(getBooleanValue("paging")) ? Image.getImage("separator") : "";
		result +=       getConfigImage("cog", null, "Settings", "Roth.table.config._onClick(window.event, this)") + "&nbsp;";
		if (getBooleanValue("searching", false)) {
			result +=   "&nbsp;|&nbsp;&nbsp;" +
		                getConfigImage("search", null, "Search", "Roth.table.search._onClick(window.event, this)") + "&nbsp;";
			DataGridParams p = (DataGridParams)getValue("_params");
			if (p.getSearchColumn() != null)
				result += getConfigImage("search-minus", null, "Find Previous", "Roth.table.search._onClick(window.event, this, 'previous')") + "&nbsp;" +
			              getConfigImage("search-plus", null, "Find Next", "Roth.table.search._onClick(window.event, this, 'next')") + "&nbsp;" +
			              getConfigImage("times", null, "Clear Search", "Roth.table.search._onClick(window.event, this, 'clear')") + "&nbsp;";
		}
		boolean exporting = getBooleanValue("exporting", false);
		if (exporting)
			result += "&nbsp;|&nbsp;&nbsp;" + 
			          getConfigImage("download", null, "Export As CSV", "Roth.table.exportData('" + getValue("id") + "', '" + getValue("_dataSource") + "', 'csv')");
		return result;
	}
	
	protected String getConfigImage(String imageName, String overlayName, String title, String event) {
		String a = "";
		if (title != null) a += attr("title", title);
		if (event != null) a += attr("onclick", event);
		a += attr("onmousedown", "return false;");
		return Image.getImage(imageName, overlayName, a);
	}
	
	protected String getSizer() {
		Object sizable = getRemoveValue("sizable");
		if ((sizable == null) || !(Boolean)sizable) return "";
		String jsMouseDownHandler = "return Roth.table.sizer._onMouseDown(event);";
		return tag("div", attr("class", "rgrds") + attr("onmousedown", jsMouseDownHandler), "");
	}
	
	@SuppressWarnings("unchecked")
	protected String getFields(DataGridParams p) {
		String result = "";
		ArrayList<ColumnData> coldata = (ArrayList<ColumnData>)getValue("columns");
		
		for (int i = 0; i < coldata.size(); i++) {
			ColumnData d = coldata.get(i);
			if ((d.getDataSource() == null) || (d.getDataSource().startsWith("_"))) continue;
			String caption = (d.getCaption().contains("<") || d.getCaption().contains(">"))
			               ? "" : d.getCaption();
			if (result.length() > 0) result += ",";
			result += d.getDataSource() + "|" + caption + "|";
			if (p.getVisibleColumns() == null) result += d.getVisible();
			else result += Data.in(d.getDataSource(), p.getVisibleColumns());
		}
		
		return result;
	}
	@Override
	public String[][] getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String[] getEntities() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getTemplate() {
		// TODO Auto-generated method stub
		return null;
	}
}
