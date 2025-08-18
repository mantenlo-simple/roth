package com.roth.tags.html;

import java.util.ArrayList;
import java.util.List;

import com.roth.base.log.Log;

import jakarta.servlet.jsp.JspException;

class TreeNodeStack {
	private class TreeNode {
		List<?> list;
		Object[] array;
		int index;
		
		public TreeNode(List<?> list, Object[] array) {
			this.list = list;
			this.array = array;
			index = 0;
		}
	}
	
	private List<TreeNode> stack;
	
	public TreeNodeStack() {
		stack = new ArrayList<>();
	}
	
	public void push(Object dataSource) throws JspException {
		if (dataSource == null)
			return;
		List<?> list = null;
		Object[] array = null;
		if (dataSource instanceof List<?> temp)
			list = temp;
		else if (dataSource.getClass().isArray())
			array = (Object[])dataSource;
		else
			throw new JspException("The 'dataSource' attribute must resolve to a List or Array.");
		stack.add(new TreeNode(list, array));
	}
	
	public void pop() { 
		if (stack.isEmpty()) {
			Log.logException(new Exception("An attempt was made to pop from an empty stack."), null);
			return;
		}
		stack.remove(stack.size() - 1); 
	}

	public int getLevel() { return stack.size(); }
	
	public int getLevelSize() {
		int index = stack.size() - 1;
		if (index < 0)
			return index;
		TreeNode node = stack.get(index);
		return node.list != null ? node.list.size() 
				          : node.array != null ? node.array.length 
				          : 0;
	}
	public int getLevelIndex() { return !stack.isEmpty() ? stack.get(stack.size() - 1).index : -1; }
	
	public boolean hasNext() { 
		TreeNode node = stack.get(stack.size() - 1);
		return (node.list != null && node.index < node.list.size()) ||
			   (node.array != null && node.index < node.array.length);
	}
	
	public Object next() {
		TreeNode node = stack.get(stack.size() - 1);
		Object child = node.list != null && node.list.size() > node.index ? node.list.get(node.index) 
				     : node.array != null && node.array.length > node.index ? node.array[node.index] 
				     : null;
		node.index++;
		return child;
	}
}
