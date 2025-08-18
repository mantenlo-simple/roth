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
package com.roth.servlet.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.servlet.ActionServlet.Bean;
import com.roth.servlet.ActionServlet.Post;

public class ParameterSegment implements Serializable, Comparable<ParameterSegment> {
	private static final long serialVersionUID = 631468227829178784L;

	private String segment;
	private ParameterSegment parent;
	private String name;
	private String index;
	private Integer intIndex;
	private List<ParameterSegment> children;
	private Object object;
	
	public ParameterSegment(String segment) {
		init(segment, null);
	}
	
	protected ParameterSegment(String segment, ParameterSegment parent) {
		init(segment, parent);
	}
	
	protected void init(String segment, ParameterSegment parent) {
		this.segment = segment;
		this.parent = parent;
		int bracket = segment.indexOf("[");
		name = bracket < 0 ? name : segment.substring(0, bracket);
		try {
			if (bracket > -1)
				index = segment.substring(bracket + 1, segment.indexOf("]"));
			intIndex = Data.strToInteger(index);
		}
		catch (Exception e) { Log.logException(e, null); }
	}

	/**
	 * Get the segment string from the original parameter key.
	 * @return
	 */
	public String getSegment() { return segment; }
	public Object getObject() { return object; }	
	
	/**
	 * Get the first-level segment from the original parameter key.
	 * @return
	 */
	protected ParameterSegment getFirstLevel() { return parent == null ? this : parent.getFirstLevel(); }
	
	protected ParameterSegment getSecondLevel() { return parent == null ? null : parent.parent == null ? parent : parent.getSecondLevel(); }
	
	/**
	 * Get the parent segment.
	 * @return the parent segment if one exists, otherwise null.
	 */
	public ParameterSegment getParent() { return parent; }
	
	@Override
	public int compareTo(ParameterSegment anotherSegment) {
		int result = name.compareTo(anotherSegment.name);
		if (result != 0)
			return result;
		// Note that numeric indexes are sorted in descending order.
		return intIndex != null ? -intIndex.compareTo(anotherSegment.intIndex) : index != null ? index.compareTo(anotherSegment.index) : 0;
	}
	
	/**
	 * Adds a child ParameterSegment object created from segment.
	 * @param segment
	 * @return the processed object.
	 */
	public ParameterSegment addChild(String segment) {
		if (children == null)
			children = new ArrayList<>();
		ParameterSegment child = new ParameterSegment(segment);
		children.add(child);
		return child;
	}
	
	/**
	 * Get a child segment at index.
	 * @param index
	 * @return the child at index, or null if index out of bounds or no children.
	 */
	public ParameterSegment getChild(int index) {
		if (children == null)
			throw new IndexOutOfBoundsException("This ParameterSegment has no childre.");
		return children.get(index);
	}
	
	/**
	 * Finds and returns the child segment, if it exists.  If it does not exist, then it will add the child and return it.
	 * @param segment
	 * @return
	 */
	public ParameterSegment getChild(String segment) {
		ParameterSegment child = findSegment(children, segment);
		if (child == null)
			child = addChild(segment);
		return child;
	}
	
	/**
	 * Finds and returns a segment in a list.
	 * @param list
	 * @param segment
	 * @return the found segment, or null if not found.
	 */
	public static ParameterSegment findSegment(List<ParameterSegment> list, String segment) {
		for (ParameterSegment item : list)
			if (item.getSegment().equals(segment))
				return item;
		return null;
	}
	
	/**
	 * Sort this Parameter segment's children.  This calls sortSegments, which sorts recursively.
	 */
	public void sortChildren() {
		if (children == null)
			return;
		sortSegments(children);
	}
	
	/**
	 * Sorts a list of ParameterSegment.  This function sorts recursively through the tree.
	 * @param list
	 */
	public static void sortSegments(List<ParameterSegment> list) {
		Collections.sort(list);
		for (ParameterSegment item : list)
			item.sortChildren();
	}
	
	/**
	 * Get the full parameter string to this segment.
	 * @return
	 */
	public String getParameterString() {
		return parent == null ? segment : parent.getParameterString() + "." + segment;
	}
	
	public void getParameterStrings(Map<String, ParameterSegment> map) {
		if (children == null)
			map.put(getParameterString(), this);
		else
			for (ParameterSegment child : children)
				child.getParameterStrings(map);
	}
	
	protected static Bean findBean(Post post, String scope, String name) {
		for (Bean bean : post.beans()) {
			if (bean.scope().equals(name) && bean.name().equals(name))
				return bean;
		}
		return null;
	}
	
	public void process(String value, Post post, ParameterProcessor processor) {
		String scope = getFirstLevel().name.replaceAll("Scope", "");
		String name = getSecondLevel().name; // This can risk a NullPointerException, but this function should not be called from a scope-level segment.
		if (name.contains("["))
			name = name.substring(0, name.indexOf("["));
		Bean bean = findBean(post, scope, name);
		if (object == null) {
			try {
				object = processor.getAttr(scope, name);
				if (object == null)
					object = processor.setAttr(scope, name, processor.initObject(bean));
				
			}
			catch (Exception e) {}
		}
	}
}
