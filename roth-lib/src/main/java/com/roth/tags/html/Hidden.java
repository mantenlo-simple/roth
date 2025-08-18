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
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;

import jakarta.servlet.jsp.JspException;

import com.roth.base.util.Data;

public class Hidden extends InputTag {
	private static final long serialVersionUID = 8411090813950494288L;

	@SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {
		String value = (String)getRemoveValue("value");
		if (value == null) {
			Object dsValue = getDataSourceValue();
			HashMap<String,String> formats = (HashMap<String,String>)pageContext.getSession().getAttribute("formats");
			value = (dsValue == null) 
			      ? "" 
			      : (dsValue instanceof java.sql.Date)
			      ? Data.dateToStr((Date)dsValue, formats.get(Data.FMT_DATE))
	    		  : (dsValue instanceof java.sql.Time)
			      ? Data.dateToStr((Date)dsValue, formats.get(Data.FMT_TIME))
	    		  : (dsValue instanceof java.sql.Timestamp)
			      ? Data.dateToStr((Date)dsValue, formats.get(Data.FMT_DATETIME))
	    		  : (dsValue instanceof java.util.Date)
			      ? Data.dateToStr((Date)dsValue, formats.get(Data.FMT_DATETIME))
	    		  : (dsValue instanceof LocalDate)
			      ? Data.dateToStr((LocalDate)dsValue, formats.get(Data.FMT_DATETIME))
	    		  : (dsValue instanceof LocalDateTime)
			      ? Data.dateToStr((LocalDateTime)dsValue, formats.get(Data.FMT_DATETIME))
	    		  : (dsValue instanceof LocalTime)
			      ? Data.dateToStr((LocalTime)dsValue, formats.get(Data.FMT_DATETIME))
			      : dsValue.toString();
		}
		setValue("value", value);
		println(getInput("hidden"));
		release();
		return EVAL_PAGE;
	}
}
