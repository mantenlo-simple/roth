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

import jakarta.servlet.jsp.JspException;

import com.roth.tags.html.util.OptionData;

public class SlideShow extends HtmlTag implements OptionTag {
	private static final long serialVersionUID = 5751705640152922123L;

	public void setBorder(String border) { setValue("border", border); }
	public void setHeight(int height) { setValue("height", height); }
	public void setInterval(int interval) { setValue("interval", interval); }
	public void setInTransition(String inTransition) { setValue("inTransition", inTransition); }
	public void setOutTransition(String outTransition) { setValue("outTransition", outTransition); }
	public void setTransition(String transition) { setValue("inTransition", transition); setValue("outTransition", transition); }
	public void setTransitionSpeed(double transitionSpeed) { setValue("transitionSpeed", transitionSpeed); }
	public void setWidth(int width) { setValue("width", width); }

	/*
       Valid Transition Values (case insensitive)
           ""
           "UP"
           "UPFADE"
           "DOWN"
           "DOWNFADE"
           "LEFT"
           "LEFTFADE"
           "RIGHT"
           "RIGHTFADE"
           "FADE"
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public void addOption(OptionData option) {
		if (getValue("_options") == null) setValue("_options", new ArrayList<OptionData>());
		((ArrayList<OptionData>)getValue("_options")).add(option);
	}
	
	@SuppressWarnings({ "unchecked" })
	protected ArrayList<OptionData> getOptions() {
		return (ArrayList<OptionData>)getValue("_options");
	}
	
	protected int getSlideCount() {
		ArrayList<OptionData> slides = getOptions();
		return slides == null ? 0 : slides.size();
	}
	
	protected String getSlideId(int index) {
		String id = (String)getValue("id");
		return "slide_" + id + "_" + index;
	}
	
	protected String getSlideIdSet() {
		String result = "";
		for (int i = 0; i < getSlideCount(); i++)
			result += (result.isEmpty() ? "" : ",") + "." + getSlideId(i);
		return result;
	}
	
	protected String getSlideCSS(int index, int totalInterval) {
		String id = (String)getValue("id");
		String slide = "slide_" + id + "_" + index;
		boolean first = index == 0;
		boolean last = index == getSlideCount() - 1;
		// Calculate percentage markers
		double range = 100 / getSlideCount();
		double start = first ? 0 : range * index;
		double end = last ? 100 : start + range;
		double transitionSpeed = getValue("transitionSpeed") == null ? 1 : (Double)getValue("transitionSpeed");
		double offset = 10 / getSlideCount() * transitionSpeed;
		
		OptionData data = getOptions().get(index);
		
		int height = (Integer)getValue("height");
		int width = (Integer)getValue("width");
		
		String it = data.getPageId() != null ? data.getPageId().toUpperCase() : 
			        getValue("inTransition") != null ? ((String)getValue("inTransition")).toUpperCase() : "";
		String ot = data.getValue() != null ? data.getValue().toUpperCase() : 
			        getValue("outTransition") != null ? ((String)getValue("outTransition")).toUpperCase() : "";
		
	    /*
		FADE -> incoming and outgoing trade places, all others behind
		     -> -1 <-> 0  Others: -2 
		
		NOFADE -> outgoing stays on top until finished leaving.
		
		SLIDE -> RIGHT/LEFT/UP/DOWN
		
		ROTATEEXCHANGE -> RIGHT/LEFT/UP/DOWN
		
		*/	        
	    // Calculate inTransition
      //  boolean ifade = it.contains("FADE");
			        
			        /*
		           out          in
		    right:   0 - 180 - 360
		     left: 360 - 180 -   0
		     none:   0 -   0 -   0
		     
		     down:   0 - 180 - 360
		       up: 360 - 180 -   0
		     none:   0 -   0 -   0
			        
		String iroty = it.contains("RRIGHT") ? 
			        
	    int iroty = (it.contains("RRIGHT") ? 360 it.contains("RLEFT") ? 0 : "rotateY(0deg)") + " " +
	    		      (it.contains("RDOWN") || it.contains("RUP") ? "rotateX(180deg)" : "rotateX(0deg)");
	    String orot = (it.contains("RRIGHT") || it.contains("RLEFT") ? "rotateY(0deg)" : "rotateY(0deg)") + " " +
 		              (it.contains("RDOWN") || it.contains("RUP") ? "rotateX(0deg)" : "rotateX(0deg)");
		
			
			
			    out:    
			        right 360 to 180
			        left 0 to 180
			        
			    in:
			    	right 180 to 0
			    	left 180 to 360
			        */
		String rotxpre = it.contains("RDOWN") || it.contains("RUP") ? "rotateX(180deg)" : "rotateX(0deg)";
		String rotxview1 = it.contains("RUP") ? "rotateX(360deg)" : "rotateX(0deg)";
		String rotxview2 = ot.contains("RUP") ? "rotateX(360deg)" : "rotateX(0deg)";
		String rotxpost = ot.contains("RDOWN") || ot.contains("RUP") ? "rotateX(180deg)" : "rotateX(0deg)";
		
		String rotypre = it.contains("RRIGHT") || it.contains("RLEFT") ? "rotateY(180deg)" : "rotateY(0deg)";
		String rotyview1 = it.contains("RLEFT") ? "rotateY(360deg)" : "rotateY(0deg)";
		String rotyview2 = ot.contains("RLEFT") ? "rotateY(360deg)" : "rotateY(0deg)";
		String rotypost = ot.contains("RRIGHT") || ot.contains("RLEFT") ? "rotateY(180deg)" : "rotateY(0deg)";
			        
			        
		//String ixrot = it.contains("RDOWN") || it.contains("RUP") ? "1" : "0";
        //String iyrot = it.contains("RRIGHT") || it.contains("RLEFT") ? "1" : "0";
        String itop = (it.contains("SDOWN") ? "-" + height : it.contains("SUP") ? height : "0") + "px";
		String ileft = (it.contains("SRIGHT") ? "-" + width : it.contains("SLEFT") ? width : "0") + "px";
		//String iopacity = it.contains("FADE") ? "0" : "1";
		
        
        // Calculate outTransition
      //  boolean ofade = ot.contains("FADE");
		//String oxrot = ot.contains("RDOWN") || ot.contains("RUP") ? "1" : "0";
        //String oyrot = ot.contains("RRIGHT") || ot.contains("RLEFT") ? "1" : "0";
        String otop = (ot.contains("SUP") ? "-" + height : ot.contains("SDOWN") ? height : "0") + "px";
		String oleft = (ot.contains("SLEFT") ? "-" + width : ot.contains("SRIGHT") ? width : "0") + "px";
		//String oopacity = ot.contains("FADE") ? "0" : "1";
		
		String preview = " top: " + itop + "; left: " + ileft + "; opacity: 0; z-index: -1;";
		//preview += " transform: rotate3d(" + ixrot + "," + iyrot + ",0,180deg);";
		preview += " transform: " + rotxpre + " " + rotypre + ";";
		String view = " top: 0px; left: 0px; opacity: 1; z-index: 0; ";
		String postview = " top: " + otop + "; left: " + oleft + "; opacity: 0; z-index: -3;";
		//postview += " transform: rotate3d(" + oxrot + "," + oyrot + ",0,180deg);";
		preview += " transform: " + rotxpost + " " + rotypost + ";";
		
		return "@keyframes " + slide + " { " +
		       // The first starts at 0%, but others don't, so enter a 0% for all but the first. 
		       (first ? "" : " " + "0% { " + postview + " } ") +
		       // The first can't go negative, so it's preview will come just before the 100%.
		       // -, 18, 38, 58, 78
		       (first ? "" : " " + String.format("%.2f", start - offset) + "% { " + preview + " } ") +
		       // 0, 20, 40, 60, 80
		       " " + String.format("%.2f", start) + "% { " + view + " transform: " + rotxview1 + " " + rotyview1 + ";" + " } " +
               // 18, 38, 58, 78, 98               
               " " + String.format("%.2f", end - offset) + "% { " + view + " transform: " + rotxview2 + " " + rotyview2 + ";" + " } " +
               // 20, 40, 60, 80, 100
			   " " + String.format("%.2f", end) + "% { " + postview + " } " +
               // Here's where the first slide preview comes in.
               // 98, -, -, -, -
			   (first ? " " + String.format("%.2f", 100 - offset) + "% { " + preview + " } " : "") +
			   // The last ends at 100%, but others don't.  The first needs to hit 100% at view, and the others at postview.
		       (last ? "" : first ?  " 100% { " + view + " } " : " 100% { " + postview + " } ") +
		       " } ." + slide + " { animation: " + slide + " " + totalInterval + "s linear infinite; backface-visibility: hidden; " +
		       		" background: url(" + data.getIconName() + "); opacity: 1; z-index: " + (first ? "0" : "-3") + "; } ";
	}
	
	protected String getSlideCSSSet() {
		String result = "";
		int totalInterval = (Integer)getValue("interval") * getSlideCount();
		for (int i = 0; i < getSlideCount(); i++)
			result += getSlideCSS(i, totalInterval);
		return result;
	}
	
	protected String getCSS() {
		String id = (String)getValue("id");
		String size = "height: " + getValue("height") + "px; width: " + getValue("width") + "px;";
		String border = (String)getValue("border");
		return "<style type=\"text/css\">" +
			   ".slideshow_" + id + " { position: relative; float: left; overflow: hidden; perspective: 500px; " +
			   size + " border: " + (border == null ? "none" : border) + "; } " +
			   ".slideshow_" + id + " > " + getSlideIdSet() + " { position: absolute; " + size + " } " +
			   getSlideCSSSet() + "</style>";
	}
	
	protected String getSlideHTML(int index) {
		String id = (String)getValue("id");
		String slide = "slide_" + id + "_" + index;
		return "<div class=\"" + slide + "\"></div>";
	}
	
	protected String getSlideHTMLSet() {
		String result = "";
		for (int i = 0; i < getSlideCount(); i++)
			result += getSlideHTML(i);
		return result;
	}
	
	protected String getHTML() {
		String id = (String)getValue("id");
		return "<div class=\"slideshow_" + id + "\">" + getSlideHTMLSet() + "</div>";
	}
	
	// doStartTag
	public int doStartTag() throws JspException {
		return EVAL_BODY_INCLUDE;
	}
	
	// doEndTag
	public int doEndTag() throws JspException {
		print(getCSS() + getHTML());
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
