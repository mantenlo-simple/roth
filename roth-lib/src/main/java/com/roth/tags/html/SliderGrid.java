package com.roth.tags.html;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.roth.base.util.Data;

import jakarta.servlet.jsp.JspException;

public class SliderGrid extends HtmlTag {
	private static final long serialVersionUID = -608821442846061090L;

	/**
	 * Allowable values:<br/>
	 * [H|V]|[HORZ|VERT]|[HORIZONTAL|VERTICAL] (case-insensitive)
	 * @param orientation
	 */
	public void setOrientation(String orientation) { setValue("orientation", orientation); }
	
	private void setProperties(String panel, String id, String cssClass, String size, String maxSize, String minSize, String step, 
			                   String padding, Boolean fixed, Boolean visible, String content) {
		String p = panel.toLowerCase();
		if (!Data.in(p, new String[] { "left", "center", "right" }))
			throw new IllegalArgumentException("Invalid panel reference.");
		setValue(p + "id", Data.nvl(id, ""));
		setValue(p + "class", Data.nvl(cssClass, ""));
		setValue(p + "padding", Data.nvl(padding, "0"));
		setValue(p + "content", Data.nvl(content, ""));
		boolean horizontal = getStringValue("orientation", "horizontal").equalsIgnoreCase("horizontal");
		String sizePrefix = horizontal ? "width: " : "height: ";
		if (Data.in(panel.toLowerCase(), new String[] { "left", "right" })) {
			setValue(p + "size", sizePrefix + Data.nvl(size, "300px") + ";");
			setValue(p + "max", "max-" + sizePrefix + Data.nvl(maxSize, "300px") + ";");
			setValue(p + "min", "min-" + sizePrefix + Data.nvl(minSize, "40px") + ";");
			setValue(p + "step", Data.nvl(step, "1px"));
			setValue(p + "visible", Data.nvl(visible, true) ? "" : horizontal ?  "style=\"width: 0; overflow-x: hidden;\"" : "style=\"height: 0; overflow-y: hidden;\"");
			setValue(p + "fixed", Data.nvl(fixed, false) ? "fixed" : "");
			setValue(p + "collapsed", Data.nvl(size, "300px").equals(Data.nvl(minSize, "40px")) ? "collapsed" : "");
		}
	}
	
	
	/*
	private void setLeftMax(String leftMax) { setValue("leftMax", leftMax); }
	private void setLeftMin(String leftMin) { setValue("leftMin", leftMin); }
	private void setLeftStep(String leftStep) { setValue("leftStep", leftStep); }
	private void setLeftVisible(Boolean leftVisible) { setValue("leftVisible", leftVisible); }
	private void setLeftWidth(String leftWidth) { setValue("leftWidth", leftWidth); }
	private void setRightMax(String rightMax) { setValue("rightMax", rightMax); }
	private void setRightMin(String rightMin) { setValue("rightMin", rightMin); }
	private void setRightStep(String rightStep) { setValue("rightStep", rightStep); }
	private void setRightVisible(Boolean rightVisible) { setValue("rightVisible", rightVisible); }
	private void setRightWidth(String rightWidth) { setValue("rightWidth", rightWidth); }
	
	private void setCenterContent(String centerContent) { setValue("centerContent", centerContent); }
	private void setCenterCssClass(String centerCssClass) { setValue("centerCssClass", centerCssClass); }
	private void setCenterId(String centerId) { setValue("centerId", centerId); }
	private void setCenterPadding(String centerPadding) { setValue("centerPadding", centerPadding); }
	private void setLeftContent(String leftContent) { setValue("leftContent", leftContent); }
	private void setLeftCssClass(String leftCssClass) { setValue("leftCssClass", leftCssClass); }
	private void setLeftId(String leftId) { setValue("leftId", leftId); }
	private void setLeftPadding(String leftPadding) { setValue("leftPadding", leftPadding); }
	private void setRightContent(String rightContent) { setValue("rightContent", rightContent); }
	private void setRightCssClass(String rightCssClass) { setValue("rightCssClass", rightCssClass); }
	private void setRightId(String rightId) { setValue("rightId", rightId); }
	private void setRightPadding(String rightPadding) { setValue("rightPadding", rightPadding); }
	*/
	
	void addContent(String content, String cssClass, String id, String maxSize, String minSize, String step, String size, Boolean fixed, String padding, Boolean visible) throws JspException {
		String panel = getValue("leftcontent") == null ? "left"
				     : getValue("centercontent") == null ? "center"
				     : getValue("rightcontent") == null ? "right"
				     : null;
		if (panel == null)
			throw new IllegalStateException("No more than three conent SliderPanels may be defined within a SliderGrid.");
		setProperties(panel, id, cssClass, size, maxSize, minSize, step, padding, fixed, visible, content);
		/*
		if (getValue("leftContent") == null) {
			setLeftContent(content);
			if (cssClass != null) setLeftCssClass(cssClass);
			setLeftId(id);
			if (maxWidth != null) setLeftMax(maxWidth);
			if (minWidth != null) setLeftMin(minWidth);
			if (step != null) setLeftStep(step);
			if (width != null) setLeftWidth(width);
			if (padding != null) setLeftPadding(padding);
			if (visible != null) setLeftVisible(visible);
		}
		else if (getValue("centerContent") == null) {
			setCenterContent(content);
			if (cssClass != null) setCenterCssClass(cssClass);
			setCenterId(id);
			if (padding != null) setCenterPadding(padding);
			if (maxWidth != null || minWidth != null || step != null || width != null)
				throw new JspException("The center panel is sized relative to the side panels, and cannot have values for maxWidth, minWidth, step, and width.");
		}
		else if (getValue("rightContent") == null) {
			setRightContent(content);
			if (cssClass != null) setRightCssClass(cssClass);
			setRightId(id);
			if (maxWidth != null) setRightMax(maxWidth);
			if (minWidth != null) setRightMin(minWidth);
			if (step != null) setRightStep(step);
			if (width != null) setRightWidth(width);
			if (padding != null) setRightPadding(padding);
			if (visible != null) setRightVisible(visible);
		}
		else
			throw new IllegalStateException("No more than three conent SliderPanels may be defined within a SliderGrid.");
		*/
	}

	@Override
	public int doStartTag() throws JspException {
		return EVAL_BODY_INCLUDE;
	}
	
	@Override
	public int doAfterBody() throws JspException {
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() throws JspException {
		/*
		Map<String,String> params = new HashMap<>();
		params.put("id", getId());
		params.put("leftmax", getStringValue("leftMax", "300px"));
		params.put("leftmin", getStringValue("leftMin", "40px"));
		params.put("leftstep", getStringValue("leftStep", "1px").toString());
		params.put("leftwidth", getStringValue("leftWidth", "300px"));
		params.put("leftvisible", getBooleanValue("leftVisible", true) ? "" : "style=\"width: 0; overflow-x: hidden;\"");
		params.put("leftcollapsed", getStringValue("leftWidth", "300px").equals(getStringValue("leftMin", "40px")) ? "collapsed" : "");
		params.put("rightmax", getStringValue("rightMax", "300px"));
		params.put("rightmin", getStringValue("rightMin", "40px"));
		params.put("rightstep", getStringValue("rightStep", "1px").toString());
		params.put("rightwidth", getStringValue("rightWidth", "300px"));
		params.put("rightvisible", getBooleanValue("rightVisible", true) ? "" : "style=\"width: 0; overflow-x: hidden;\"");
		params.put("rightcollapsed", getStringValue("rightWidth", "300px").equals(getStringValue("rightMin", "40px")) ? "collapsed" : "");
		params.put("leftcontent", getStringValue("leftContent", ""));
		params.put("leftclass", getStringValue("leftCssClass", ""));
		params.put("leftid", getStringValue("leftId", ""));
		params.put("leftpadding", getStringValue("leftPadding", "8px 8px 8px 0"));
		params.put("centercontent", getStringValue("centerContent", ""));
		params.put("centerclass", getStringValue("centerCssClass", ""));
		params.put("centerid", getStringValue("centerId", ""));
		params.put("centerpadding", getStringValue("centerPadding", "8px"));
		params.put("rightcontent", getStringValue("rightContent", ""));
		params.put("rightclass", getStringValue("rightCssClass", ""));
		params.put("rightid", getStringValue("rightId", ""));
		params.put("rightpadding", getStringValue("rightPadding", "8px 0 8px 8px"));
		println(HtmlTag.applyTemplate("slidergrid", params));
		*/
		translateOrientation();
		println(HtmlTag.applyTemplate("slidergrid", getValueMap()));
		release();
		return EVAL_PAGE;
	}
	
	private void translateOrientation() throws JspException {
		String o = getStringValue("orientation", "horizontal");
		if (Data.in(o.toLowerCase(), new String[] { "h", "horz", "horizontal" }))
			setValue("orientation", "");
		else if (Data.in(o.toLowerCase(), new String[] { "v", "vert", "vertical" }))
			setValue("orientation", "slider-grid-vertical");
		else
			throw new JspException("Invalid orientation value.  Must be one of h/horz/horizontal/v/vert/vertical (case-insensitive).");
	}
	
	private Map<String,String> getValueMap() {
		Map<String,String> map = new HashMap<>();
		Enumeration<String> keys = getValues();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			map.put(key, (String)getValue(key));
		}
		return map;
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
