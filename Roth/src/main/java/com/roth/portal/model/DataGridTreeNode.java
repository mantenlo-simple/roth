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
package com.roth.portal.model;

import java.util.ArrayList;

public abstract class DataGridTreeNode {
	private int index;
    private DataGridTreeNode parent;
    private ArrayList<DataGridTreeNode> children;
    
    public DataGridTreeNode() {
    	children = new ArrayList<DataGridTreeNode>();
    }
    
    public abstract Long getNodeId();
    public abstract Long getParentNodeId();
    public abstract String getNodeLineage();
        
    public void setIndex(int index) { this.index = index; }
    public boolean isLast() { return (parent == null) ? true : index == parent.children.size() - 1; }
    
    public DataGridTreeNode getParent() { return parent; }
    public void setParent(DataGridTreeNode parent) { this.parent = parent; }
    
    public boolean addChild(DataGridTreeNode child) {
    	Long _id = getNodeId();;
    	String _lineage = getNodeLineage();
    	
    	// If the child is on the same or higher level, or the level is lower, 
    	// but the lineage doesn't match, then, the child is not this group's child. 
    	if ((child.getLevel() <= getLevel()) || (child.getNodeLineage().indexOf(_lineage) != 0)) 
    		return false;
    	// If the child's parent group ID is this groups ID, then the child is this
    	// group's direct child.
    	if (_id.equals(child.getParentNodeId())) {
            child.setParent(this);
    		child.setIndex(children.size());
    		children.add(child);
    		return true;
    	}
    	// Otherwise, it is a child of a child on some level.
    	else
    		for (int i = 0; i < children.size(); i++)
    			if (children.get(i).addChild(child)) {
    				return true;
    			}
    	// If it didn't get added at some point, then it's an orphaned group.
    	return false;
    }    
    
    public int getLevel() {
    	String pl = getParentLineage(); 
    	if (pl.isEmpty()) return 0;
    	return pl.split(" ").length;
    }
    
    public String getParentLineage() {
    	if (getNodeLineage() == null) return "";
    	String pl = getNodeLineage().trim();
    	int i = pl.lastIndexOf(" ");
    	return (i < 0) ? "" : pl.substring(0, i);
    }
    
    public String getLineageIcons() {
    	String result = "";
    	DataGridTreeNode g = getParent();
    	if (g != null) {
    		String img = (isLast()) ? "&nbsp;&boxur;&nbsp;" : "&nbsp;&boxvr;&nbsp;";
    		result = img;
    	
	    	while (g.getParent() != null) {
	    		img = (g.isLast()) ? "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" : "&nbsp;&boxv;&nbsp;";
	    		result = img + result;
	    		g = g.getParent();
	    	}
    	}
    	return "<span class=\"jtreelines\">" + result + "</span>&nbsp;";
    }
}
