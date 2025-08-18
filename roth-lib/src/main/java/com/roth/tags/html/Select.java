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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.tags.html.util.OptionData;

public class Select extends InputTag implements OptionTag {
	private static final long serialVersionUID = 4445140149853564754L;

	public void setOptionsDataSource(Object optionsDataSource) { if (optionsDataSource != null) setValue("_optionsdatasource", optionsDataSource); }
	public void setKeyDataSource(String keyDataSource) { setValue("_keydatasource", keyDataSource); }
	public void setValueDataSource(String valueDataSource) { setValue("_valuedatasource", valueDataSource); }
	public void setNullable(boolean nullable) { setValue("_nullable", nullable); }
	
	public int doStartTag() throws JspException {
		return EVAL_BODY_INCLUDE;
	}
	
	private boolean isNullable() {
		return getBooleanValue("_nullable", false);
	}
	
	private String getFirstValue() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String,String> options = (Map)getValue("_optionsMap");
		return options.keySet().isEmpty() ? "" : options.keySet().iterator().next();
	}
	
	public int doEndTag() throws JspException {
		String value = (String)getValue("value");
		processOptions();
		if (value == null) {
			Object dsValue = getDataSourceValue();
			value = (dsValue != null) ? dsValue.toString() : isNullable() ? "" : getFirstValue();
			setValue("value", value);
		}
		//String id = (String)getValue("id");
		//if (id == null) { id = "abc"; setValue("id", id); }
		println(getSelect());
		release();
		return EVAL_PAGE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addOption(OptionData option) {
		if (getValue("_options") == null) setValue("_options", new ArrayList<OptionData>());
		((ArrayList<OptionData>)getValue("_options")).add(option);
	}
	
	@SuppressWarnings("unchecked")
    protected String getSelect() throws JspException {
	    Form ancestor = (Form)findAncestorWithClass(this, Form.class);
        if (!getBooleanValue("_override", false) && (ancestor != null) && (ancestor.getBooleanValue("_readonly", false)))
            setValue("_readonly", true);
	    boolean ro = getBooleanValue("_readonly", false); 
	    //String readonly = (ro) ? " disabled=\"disabled\"" : attr("name", getName());
	    
	    if (ro) {
	    	setValue("disabled", "disabled");
	    }
	    
	    String onchange = this.getRemoveValue("onchange");
	    String result = getSelect(getId(), getName(), (String)getValue("value"), (String)getValue("_label"), null, getOptions(), (String)getValue("_width"), onchange, getHTMLAttributes());
	    
		//String result = tag("div", attr("class", "jib"), tag("select", readonly + getHTMLAttributes(), getOptions()) + tag("span", null, Image.getImage("caret-down")));
		if (ro) {
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
	        result += getInput("hidden", true);
		}
		return result; //getWrap(result);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void processOptions() throws JspException {
		Map<String,String> options = new LinkedHashMap<>();
		if (getBooleanValue("_nullable", false))
			options.put("", "");
		
		Object o = getRemoveValue("_optionsdatasource");
		if (o != null) {
			if (o instanceof Map)
				options.putAll((Map<String,String>)o);
			else if (o instanceof Collection)
				for (Object item : (Collection)o)
					processOption(item, options);
			else if (o.getClass().isArray())
				for (Object item : (Object[])o) 
					processOption(item, options);
			else
				throw new JspException("The 'optionsDataSource' attribute must resolve to a Map, Collection, or Array.");
		} else {
			ArrayList<OptionData> vo = (ArrayList<OptionData>)getValue("_options");
			if (vo != null)
				for (OptionData op : vo)
					options.put(op.getValue(), op.getCaption());
		}
		
		setValue("_optionsMap", options);
	}
	
	protected void processOption(Object item, Map<String,String> options) throws JspException {
		String keyName = (String)getValue("_keydatasource");
		String valueName = (String)getValue("_valuedatasource");
		
		if ((keyName == null) || (valueName == null))
			throw new JspException("When optionsDataSource is a Collection or an Array, keyDataSource and valueDataSource must be supplied.");
		
		String key = null, value = null;
		try {
			key = Data.fieldOf(item, keyName).toString();
			value = Data.fieldOf(item, valueName).toString();
			options.put(key,  value);
		} catch (Exception e) { 
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			String userid = (request.getUserPrincipal() == null) ? null : request.getUserPrincipal().getName();
		    Log.logException(e, userid); 
		}
		options.put(key,  value);
	}
	
	protected String getOptions() throws JspException {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Map<String,String> options = (Map)getValue("_optionsMap");
		String dsv = Data.obj2Str(getDataSourceValue());
		if (Data.isEmpty(dsv))
			dsv = getStringValue("value");
		String result = "";
		for (Entry<String,String> entry : options.entrySet()) {
			result += getOption(entry.getKey(), entry.getValue(), entry.getKey().equals(dsv));
		}
		return result;
	}
	
	/*
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected String getOptions() throws JspException {
		Object o = getRemoveValue("_optionsdatasource");
		String result = "";
		String dsv = Data.obj2Str(getDataSourceValue());
		String val = (String)getValue("value");
		if (val != null) dsv = val;
		
		if (getBooleanValue("_nullable"))
			result += getOption("", "", Data.nvl(dsv).equals(""));
		
		// If optionDataSet is specified, then parse it for options...
		if (o != null) {
			if (o instanceof Map) {
				Map m = (Map)o;
				for (Object key : m.keySet())
					result += getOption(key.toString(), m.get(key).toString(), key.toString().equals(dsv));
			}
			else if (o instanceof Collection)
				for (Object item : (Collection)o) result += getOption(item, dsv);
			else if (o.getClass().isArray())
				for (Object item : (Object[])o) result += getOption(item, dsv);
			else
				throw new JspException("The 'optionsDataSource' attribute must resolve to a Map, Collection, or Array.");
			
			return result;
		}
		
		// Otherwise use the results of any option tags.
		ArrayList<OptionData> vo = (ArrayList<OptionData>)getValue("_options");
		if (vo != null)
			for (int i = 0; i < vo.size(); i++)
				result += getOption(vo.get(i).getValue(), vo.get(i).getCaption(), vo.get(i).getValue().equals(dsv));
		
		return result;
	}
	*/
	
	protected String getOption(String value, String caption, boolean selected) {
		String s = (selected) ? " selected=\"selected\"" : "";
		return tag("option", attr("value", value) + s, caption);
	}
	
	/*
	protected String getOption(Object item, String dsv) throws JspException {
		String keyName = (String)getValue("_keydatasource");
		String valueName = (String)getValue("_valuedatasource");
		
		if ((keyName == null) || (valueName == null))
			throw new JspException("When optionsDataSource is a Collection or an Array, keyDataSource and valueDataSource must be supplied.");
		
		String key = null, value = null;
		try {
			key = Data.fieldOf(item, keyName).toString();
			value = Data.fieldOf(item, valueName).toString();
		}
		
		catch (Exception e) { 
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			String userid = (request.getUserPrincipal() == null) ? null : request.getUserPrincipal().getName();
		    Log.logException(e, userid); 
			return ""; 
		}
		return getOption(key, value, key.equals(dsv));
	}
	*/
}
