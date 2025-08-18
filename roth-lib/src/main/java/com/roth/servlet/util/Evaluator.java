package com.roth.servlet.util;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {
	private Condition cond;	
	private List<List<String>> stack = new ArrayList<>();
	
	public Evaluator() {
		cond = null;
		stack = new ArrayList<>();
	}
	
	public Condition getCond() { return cond; }
	public void setCond(Condition cond) { this.cond = cond; }
	
	public List<List<String>> getStack() { return stack; }
}
