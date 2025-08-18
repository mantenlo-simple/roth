package com.roth.tags.html;

import java.util.Date;

import com.roth.base.util.Data;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

public class SliderPanel extends BodyTagSupport {
	private static final long serialVersionUID = -608821442846061090L;
	
	public void setCssClass(String cssClass) { setValue("cssClass", cssClass); }
	public void setFixed(Boolean fixed) { setValue("fixed", fixed); }
	public void setId(String id) { setValue("id", id); }
	public void setMaxSize(String maxSize) { setValue("maxSize", maxSize); }
	public void setMinSize(String minSize) { setValue("minSize", minSize); }
	public void setPadding(String padding) { setValue("padding", padding); }
	public void setStep(String step) { setValue("step", step); }
	public void setVisible(Boolean visible) { setValue("visible", visible); }
	public void setSize(String size) { setValue("size", size); }
	
	@Override
	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
	
	@Override
	public int doEndTag() throws JspException {
		SliderGrid ancestor = (SliderGrid)findAncestorWithClass(this, SliderGrid.class);
		String cssClass = (String)getValue("cssClass");
		String id = getId();
		String maxSize = (String)getValue("maxSize");
		String minSize = (String)getValue("minSize");
		String step = (String)getValue("step");
		String size = (String)getValue("size");
		String padding = (String)getValue("padding");
		Boolean visible = (Boolean)getValue("visible");
		Boolean fixed = (Boolean)getValue("fixed");
		BodyContent body = getBodyContent();
		String bodyContent = body == null ? "" : body.getString();
		ancestor.addContent(bodyContent, cssClass, id, maxSize, minSize, step, size, fixed, padding, visible);
		release();
		return EVAL_PAGE;
	}
	
	public String getId() { 
		if (getValue("id") == null)
			setValue("id", generateId());
		return (String)getValue("id"); 
	}
	
	public String generateId() {
		Integer g = (Integer)pageContext.getSession().getServletContext().getAttribute("HtmlTagIdGen");
		if (g == null) g = -1;
		g++;
		pageContext.getSession().getServletContext().setAttribute("HtmlTagIdGen", g);
		String core = Data.dateToStr(new Date(), "DDDHHmmss");
		return "autogen" + core + g.toString();
	}
}
