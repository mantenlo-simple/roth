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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;

public class CalendarSelect extends InputTag {
	private static final long serialVersionUID = -8151239384929063345L;
	
	public void setCacheCal(boolean cacheCalendar) { setValue("cachecal", cacheCalendar); }
	public void setShowClear(boolean showClear) { setValue("showclear", showClear); }
	public void setShowTime(boolean showTime) { setValue("showtime", showTime); }
	public void setStartDate(LocalDateTime startDate) { setValue("startdate", startDate); }
	
	@Override
	@SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {
		HashMap<String,String> formats = (HashMap<String,String>)pageContext.getSession().getAttribute("formats");
		String format = getBooleanValue("showtime", false) ? Data.FMT_DATETIME : Data.FMT_DATE;
		String value = (String)getValue("value");
		if (value == null) {
			Object dsValue = getDataSourceValue();
			value = (dsValue == null) 
				      ? "" 
				      : (dsValue instanceof java.sql.Date)
				      ? Data.dateToStr((Date)dsValue, formats.get(format))
		    		  : (dsValue instanceof java.sql.Timestamp)
				      ? Data.dateToStr((Date)dsValue, formats.get(format))
		    		  : (dsValue instanceof java.util.Date)
				      ? Data.dateToStr((Date)dsValue, formats.get(format))
				      : (dsValue instanceof LocalDate)
				      ? Data.dateToStr((LocalDate)dsValue, formats.get(format))
		    		  : (dsValue instanceof LocalDateTime)
				      ? Data.dateToStr((LocalDateTime)dsValue, formats.get(format))
					  : dsValue.toString();
			/*
			if (dsValue == null)
				value = null;
			else if (dsValue instanceof Date)
				value = Data.dateToStr((Date)dsValue, formats.get(format));
			else if (dsValue instanceof LocalDate)
				value = Data.dateToStr((LocalDate)dsValue, formats.get(format));
			else if (dsValue instanceof LocalDateTime)
				value = Data.dateToStr((LocalDateTime)dsValue, formats.get(format));
			else
				throw new JspException("Invalid datasource type.");
			if (value != null)
				setValue("value", value);
			else
				removeValue("value");
			*/
			removeValue("value");
		}
		String id = (String)getValue("id");
		if (id == null) { id = generateId(); setValue("id", id); }
		//String mo = (!isReadOnly()) ? attr("onmouseover", "attachCalendar(this);") : ""; 
		//println(getDropdown("calendar", attr("keyid", id) + getCalendarAttr(format) + mo));
		
		
		Form ancestor = (Form)findAncestorWithClass(this, Form.class);
        if (!getBooleanValue("_override", false) && (ancestor != null) && (ancestor.getBooleanValue("_readonly", false)))
            setValue("_readonly", true);
	    boolean ro = getBooleanValue("_readonly", false); 
	    //String readonly = (ro) ? " disabled=\"disabled\"" : attr("name", getName());
	    
	    if (ro)
	    	setValue("readonly", "readonly");
	    else {
			setOnMouseOver("if (!this.nextElementSibling.calendarattached) attachCalendar(this.nextElementSibling);" + Data.nvl((String)getValue("onmouseover")));
			setOnKeyDown("if (!this.nextElementSibling.calendarattached) attachCalendar(this.nextElementSibling);" + Data.nvl((String)getValue("onkeydown")));
	    }
	    String icon = getIcon("calendar-alt", ro ? "" : "onmouseover=\"if (!this.calendarattached) attachCalendar(this);\" keyid=\"" + id + "\" calendar=\"" + getCalendarAttr(format) + "\"");
		println(getLookup(getId(), icon, (String)getValue("_datasource"), value, (String)getValue("_label"), (String)getValue("_title"), (String)getValue("_width"), this.getHTMLAttributes()));
		
		release();
		return EVAL_PAGE;
	}
	
	protected String getFlag(boolean source) {
		return source ? "Y" : "N";
	}
	
	protected String getCalendarAttr(String format) {
		String startDts = (getValue("startdate") == null) ? "null" : Data.dateToStr((Date)getValue("startdate"), format); 
		String result = getFlag(getBooleanValue("showtime", false)) + "|" +
		                getFlag(getBooleanValue("cachecal", false)) + "|" +
		                startDts + "|" +
		                getFlag(getBooleanValue("showclear", false));
		removeValue("showtime");
		removeValue("cachecal");
		removeValue("startdate");
		removeValue("showclear");
		return result; //attr("calendar", result);
	}
}
