package com.roth.tags.html;

import jakarta.servlet.jsp.JspException;

public class Pane extends HtmlTag {
	private static final long serialVersionUID = -608821442846061090L;

	/**
	 * The minimum size (height or width respective of orientation) that the pane is allowed to be sized to.
	 * If allowCollapse="true", the pane will be hidden when the mouse moves to less than 1/2 of minSize.
	 * Note: there is no maxSize, as that will be automatically limited by the sizes of the other panes.
	 * @param minSize
	 */
	public void setMinSize(String minSize) { setValue("minSize", minSize); }
	public void setSize(String size) { setValue("size", size); }
	public void setFixed(boolean fixed) { setValue("fixed", fixed); }
	
	@Override
	public int doStartTag() throws JspException {
		SplitPanes ancestor = (SplitPanes)findAncestorWithClass(this, SplitPanes.class);
		if (ancestor == null)
			throw new JspException("The Pane tag must be used within the body of a SplitPanes tag.");
		boolean vertical = ancestor.getOrientation().equals(SplitPanes.ORIENT_VERT);
		boolean fixed = getBooleanValue("fixed", false);
		int index = ancestor.newPage();
		setValue("index", index);
		int vIndex = ancestor.getVariablePane();
		boolean variable = index == vIndex;
		String trAttr = attr("class", "splitPanesTr");
		String tdAttr = attr("class", "splitPanesTd");
		String id = attr("id", getId());
		String ms = attr("ms", (String)getValue("minSize"));
		String size = (String)getValue("size");
		
		String content = "";
		if (!fixed && index > vIndex)
			content += getSplitter(vertical, ancestor.getId(), getId(), 1);
		if (vertical) {
			trAttr += id + ms + attr("style", "height: " + size + ";"); //(variable ? "auto" : size) + ";");
			tdAttr += attr("style", "height: " + (variable ? "100%" : size) + ";");
			content += tagStart("div", trAttr);
		}
		else
			tdAttr += id + ms + attr("style", "width: " + size + ";"); // (variable ? "auto" : size) + ";");
		content += tagStart("div", tdAttr);
		println(content);
		return EVAL_BODY_INCLUDE;
	}
	
	@Override
	public int doEndTag() throws JspException {
		SplitPanes ancestor = (SplitPanes)findAncestorWithClass(this, SplitPanes.class);
		boolean vertical = ancestor.getOrientation().equals(SplitPanes.ORIENT_VERT);
		boolean fixed = getBooleanValue("fixed", false);
		int index = (int)getValue("index");
		int vIndex = ancestor.getVariablePane();
		String content = tagEnd("div");
		if (vertical)
			content += tagEnd("div");
		if (!fixed && index < vIndex)
			content += getSplitter(vertical, ancestor.getId(), getId(), -1);
		println(content);
		release();
		return EVAL_PAGE;
	}
	
	protected String getSplitter(boolean vertical, String parentId, String paneId, int direction) {
		String onmousedown = attr("onmousedown", "Roth.split.onsize(this, '" + parentId + "', '" + paneId + "', " + direction + "); return false;");
		if (vertical)
			return tag("div", attr("class", "sizerV"), tag("div", attr("class", "splitSizerV") + onmousedown, ""));
		else
			return tag("div", attr("class", "sizerH"), tag("div", attr("class", "splitSizerH") + onmousedown, ""));
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
