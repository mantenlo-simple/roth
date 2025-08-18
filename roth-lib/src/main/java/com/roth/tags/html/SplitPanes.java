package com.roth.tags.html;

import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;

public class SplitPanes extends HtmlTag {
	private static final long serialVersionUID = -608821442846061090L;

	public static final String ORIENT_VERT = "vertical";
	public static final String ORIENT_HORZ = "horizontal";
	
	private static final String[] VALID_ORIENTATIONS = {ORIENT_VERT, ORIENT_HORZ};
	
	/**
	 * Whether to allow panes to be collapsed to zero size (i.e. hidden).
	 * @param allowCollapse
	 */
	public void setAllowCollapse(boolean allowCollapse) { setValue("allowCollapse", allowCollapse); }
	/**
	 * Whether to allow the user to toggle the orientation from vertical to horizontal or vice versa.
	 * @param allowToggle
	 */
	public void setAllowToggle(boolean allowToggle) { setValue("allowToggle", allowToggle); }
	public void setHeight(String height) { setValue("height", height); }
	/**
	 * Valid values: vertical, horizontal.  The default orientation is vertical.  
	 * @param orientation
	 */
	public void setOrientation(String orientation) {
		String _orientation = orientation.toLowerCase();
		if (Data.in(_orientation, VALID_ORIENTATIONS))
			setValue("orientation", _orientation);
		else
			throw new IllegalArgumentException("The supplied orientation value [" + orientation + "] is not valid.");
	}
	/**
	 * Index (zero-based) of the pane that is variable in size.  All other panes are fixed sizes, which are driven by the sizer bars.  
	 * The default variable pane index is the last one.
	 * @param variablePane
	 */
	public void setVariablePane(int variablePane) { setValue("variablePane", variablePane); }
	public void setWidth(String width) { setValue("width", width); }
	
	protected String getOrientation() { return (String)getValue("orientation"); }
	protected int getVariablePane() { return getIntegerValue("variablePane", 0); }
	
	protected int getPage() { return (int)getValue("pageCount"); }
	protected int newPage() { 
		int pageCount = getPage();
		setValue("pageCount", pageCount + 1);
		return pageCount; 
	}	
	
	@Override
	public int doStartTag() throws JspException {
		setValue("pageCount", 0);
		String style = "";
		String attr = Data.nvl((String)getValue("height"));
		if (!Data.isEmpty(attr))
			style += "height:" + attr + ";";
		attr = Data.nvl((String)getValue("width"));
		if (!Data.isEmpty(attr))
			style += "width:" + attr + ";";
		if (!Data.isEmpty(style))
			style = attr("style", style);
		String output = tagStart("div", attr("id", getId()) + attr("class", "splitPanes") + style);
		if (getOrientation().equals(ORIENT_HORZ))
			output += tagStart("div", attr("class", "splitPanesTr"));
		println(output);
		return EVAL_BODY_INCLUDE;
	}
	
	@Override
	public int doEndTag() throws JspException {
		String output = "";
		if (getOrientation().equals(ORIENT_HORZ))
			output += tagEnd("div");
		
		output += tagStart("input", attr("type", "hidden") + attr("id", getId() + "_variable") + attr("name", "_na") + attr("value", Data.integerToStr(getVariablePane())), true);
		output += tagEnd("div");
		println(output);
		release();
		return EVAL_PAGE;
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
