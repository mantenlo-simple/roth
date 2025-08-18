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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.tags.html.util.OptionData;

public class RadioGroup extends InputTag implements OptionTag {
	private static final long serialVersionUID = 7961706972930243089L;

	public void setOptionsDataSource(Object optionsDataSource) { if (optionsDataSource != null) setValue("_optionsdatasource", optionsDataSource); }
	public void setKeyDataSource(String keyDataSource) { setValue("_keydatasource", keyDataSource); }
	public void setValueDataSource(String valueDataSource) { setValue("_valuedatasource", valueDataSource); }
	public void setNullable(boolean nullable) { setValue("_nullable", nullable); }
	public void setVertical(boolean vertical) { setValue("_vertical", vertical); }
	
	public int doStartTag() throws JspException {
		return EVAL_BODY_INCLUDE;
	}
	
	public int doEndTag() throws JspException {
		String value = (String)getValue("value");
		if (value == null) {
			Object dsValue = getDataSourceValue();
			value = (dsValue == null) ? "" : dsValue.toString();
			setValue("value", value);
		}
		//String id = (String)getValue("id");
		//if (id == null) { id = generateId(); setValue("id", id); }
		println(getRadioGroup());
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
    protected String getRadioGroup() throws JspException {
	    Form ancestor = (Form)findAncestorWithClass(this, Form.class);
        if (!getBooleanValue("_override", false) && (ancestor != null) && (ancestor.getBooleanValue("_readonly", false)))
            setValue("readonly", true);
	    boolean ro = getBooleanValue("readonly", false); removeValue("readonly");
	    //String readonly = (ro) ? " disabled=\"disabled\"" : attr("name", getName());
	    String options = getOptions(ro);
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
	                  : dsValue.toString();
	        }
	        setValue("value", value);
		}
		//result += getInput("hidden", false);
		//return getWrap(result);
		String id = getId();
		removeValue("id");
		String onchange = (getValue("onchange") == null) ? "" : (String)getValue("onchange");
		return getRadioGroup(id, getName(), (String)getValue("value"), (String)getValue("_label"), (String)getValue("_title"), options, null, onchange, null); //getHTMLAttributes());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected String getOptions(boolean readonly) throws JspException {
		Object o = getRemoveValue("_optionsdatasource");
		String result = "";
		String dsv = Data.obj2Str(getDataSourceValue());
		String val = (String)getValue("value");
		if (val != null) dsv = val;
		
		// Nullable doesn't apply to a radio group ?
		//if (getBooleanValue("_nullable"))
		//	result += getOption("", "", Data.nvl(dsv).equals(""));
		
		// If optionDataSet is specified, then parse it for options...
		int x = 0;
		if (o != null) {
			if (o instanceof Map) {
				Map m = (Map)o;
				for (Object key : m.keySet())
					result += getOption(x++, key.toString(), m.get(key).toString(), key.toString().equals(dsv));
			}
			else if (o instanceof Collection)
				for (Object item : (Collection)o) result += getOption(x++, item, dsv, readonly);
			else if (o.getClass().isArray())
				for (Object item : (Object[])o) result += getOption(x++, item, dsv, readonly);
			else
				throw new JspException("The 'optionsDataSource' attribute must resolve to a Map, Collection, or Array.");
			
			return result;
		}
		
		// Otherwise use the results of any option tags.
		ArrayList<OptionData> vo = (ArrayList<OptionData>)getValue("_options");
		if (vo != null)
			for (int i = 0; i < vo.size(); i++)
				result += getOption(i, vo.get(i).getValue(), vo.get(i).getCaption(), vo.get(i).getValue().equals(dsv), readonly);
		
		return result;
	}
	
	protected String getOption(int index, String value, String caption, boolean selected, boolean readonly) {
		String s = (selected) ? " checked=\"checked\"" : "";
		String name = getName();
		//String opId = name + "_" + index;
		String ro = (readonly) ? " disabled=\"disabled\"" : "";
		//String id = (String)getValue("id");
		//String onchange = (getValue("onchange") == null) ? "" : "_$('" + getId() + "').onchange();";
		
		/* commenting this out because it was moved to the input
		String onchange = (getValue("onchange") == null) ? "" : (String)getValue("onchange"); */
		String onclick = (getValue("onclick") == null) ? "" : (String)getValue("onclick");
		
		//String onclick = "_$('" + id + "').value = '" + value + "';" + onchange + Data.nvl((String)getValue("onclick"));
		//return tag("input", attr("id", opId) + attr("type", "radio") + attr("name", "_nar" + id) + attr("value", value) + s + ro + attr("onclick", onclick), "") +
		//	   tag("label", attr("for", opId) + attr("class", "cb-toggle fa-stack"), 
		//				Image.getStackedImage("circle", "s", "circle", "r", "dot-circle", "r")
		//		  )+
		//	   tag("label", attr("for", opId), caption) + (getBooleanValue("_vertical") ? "<br/>" : "");
		if (selected)
			setValue("checked", "checked");
		if (readonly)
			setValue("disabled", "disabled");
		String br = getBooleanValue("_vertical", false) ? "<br/>" : " &nbsp; &nbsp;";
		return getRadio(getId(), Integer.toString(index), name, value, caption, null, onclick, s + ro) + br; // getHTMLAttributes());
	}
	
	protected String getOption(int index, Object item, String dsv, boolean readonly) throws JspException {
		String keyName = (String)getValue("_keydatasource");
		String valueName = (String)getValue("_valuedatasource");
		
		if ((keyName == null) || (valueName == null))
			throw new JspException("When optionsDataSource is a Collection or an Array, keyDataSource and valueDataSource must be supplied.");
		
		String key = null, value = null;
		try {
			key = (String)Data.fieldOf(item, keyName);
			value = (String)Data.fieldOf(item, valueName);
		}
		
		catch (Exception e) { 
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			String userid = (request.getUserPrincipal() == null) ? null : request.getUserPrincipal().getName();
		    Log.logException(e, userid); 
			return ""; 
		}
		String s = (key == dsv) ? " checked=\"checked\"" : "";
		String ro = (readonly) ? " disabled=\"disabled\"" : "";
		String onchange = (getValue("onchange") == null) ? "" : (String)getValue("onchange");
		return //getOption(index, key, value, key.equals(dsv), readonly);
				getRadio(getId(), Integer.toString(index), getName(), key, value, null, onchange, s + ro);
	}
}
