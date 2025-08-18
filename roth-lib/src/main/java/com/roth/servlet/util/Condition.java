package com.roth.servlet.util;

public class Condition {
	boolean not;
	String operator;
	String identifier;
	String value;
	boolean block;
	
	/**
	 * Conditions may have the following formats:<br/>
	 * <pre style="color: #aaf;">
	 * [!/NOT] IDENTIFIER[,IDENTIFIER[,...]] [OPERATOR VALUE] [{]
	 * </pre>
	 * Usage Inline:<br/>
	 * <pre style="color: #aaf;">
	 * &sol;*if Firefox*&sol;
	 * </pre>
	 * Usage Block:<br/>
	 * <pre style="color: #aaf;">
	 * &sol;*if not AppleWebKit {*&sol;
	 * ...
	 * &sol;*}*&sol;
	 * </pre>
	 * Usage Block:<br/>
	 * <pre style="color: #aaf;">
	 * &sol;*if not AppleWebKit {*&sol;
	 * ...
	 * &sol;*} else {*&sol;
	 * ...
	 * &sol;*}*&sol;
	 * </pre>
	 * @param condition
	 */
	public Condition(String condition) {
		String expression = condition.replace("/*if", "").replace("*/", "").trim().toLowerCase();
		block = expression.endsWith("{");
		if (block)
			expression = expression.replace("{", "").trim();
		String[] exp = expression.split(" ");
		// Part 0
		identifier = exp[0];
		if (identifier.equals("!") || identifier.equals("not")) {
			not = true;
			identifier = null;
		}
		else if (identifier.startsWith("!")) {
			not = true;
			identifier = identifier.substring(1);
		}
		// Part 1
		if (exp.length > 1) {
			if (identifier == null)
				identifier = exp[1];
			else if (isOperator(exp[1]))
				operator = exp[1];
			else
				value = exp[1];
		}
		// Part 2
		if (exp.length > 2) {
			if (operator == null && isOperator(exp[2]))
				operator = exp[2];
			else if (value == null)
				value = exp[2];
			else
				throw new IllegalArgumentException("The condition is not valid.");
		}
		// Part 3
		if (exp.length > 3) {
			if (value == null)
				value = exp[3];
			else
				throw new IllegalArgumentException("The condition is not valid.");
		}
		if (exp.length > 4)
			throw new IllegalArgumentException("The condition is not valid.");
		// if (operator == null) operator = "eq";
	}
	
	protected static boolean isOperator(String source) {
		return source.equals("eq") || source.equals("=") ||
		       source.equals("lt") || source.equals("<") ||
		       source.equals("gt") || source.equals(">") ||
		       source.equals("lte") || source.equals("<=") ||
		       source.equals("gte") || source.equals(">=");
	}
	
	protected static boolean isNumeric(String source) {
		try { Float.parseFloat(source); return true; }
		catch (Exception e) { return false; }
	}
	
	protected static String processOperator(String source) {
		if (source.equals("eq") || source.equals("neq") ||
	        source.equals("lt") || source.equals("gt") ||
	        source.equals("lte") || source.equals("gte"))
	        return source;
		else if (source.equals("=")) return "eq";
		else if (source.equals("!=")) return "neq";
		else if (source.equals("<")) return "lt";
		else if (source.equals(">")) return "gt";
		else if (source.equals("<=")) return "lte";
		else if (source.equals(">=")) return "gte";
		else return null;
	}
	
	protected boolean matchBrowser(String a, String b) {
		return a.equals(b);
	}
	
	protected boolean matchVersion(String a, String b) {
		return (operator.equals("eq")) ? a.equals(b)
			 : (operator.equals("neq")) ? !a.equals(b)
			 : (operator.equals("lt")) ? a.compareTo(b) < 0
			 : (operator.equals("gt")) ? a.compareTo(b) > 0
		     : (operator.equals("lte")) ? a.compareTo(b) <= 0
		     : (operator.equals("gte")) ? a.compareTo(b) >= 0
		     : 1 == 0;  // Sonar Lint doesn't like false here; seems kinda dumb, but whatever.
	}
	
	public boolean satisfiesCondition(Browser browser) {
		String version = browser.matchIdentifier(identifier);
		boolean intentifierMatches = version != null;
		if (version != null && operator != null) {
			if (value == null)
				throw new IllegalArgumentException("An operator without a version is not allowed.");
			boolean versionMatches = intentifierMatches && matchVersion(version, Browser.formatVersion(value));
			return not ? !versionMatches : versionMatches; 
		} 
		return not ? !intentifierMatches : intentifierMatches;
		// return version != null;
	}
	
	public String toString() {
		return "Condition... Block: " + block + " / Identifier: " + identifier + " / Operator: " + operator + " / Version: " + value + " / Not: " + not;
	}

	public boolean isNot() { return not; }
	//public void setNot(boolean not) { this.not = not; }

	public String getOperator() { return operator; }
	//public void setOperator(String operator) { this.operator = operator; }

	public String getIdentifier() { return identifier; }
	//public void setIdentifier(String identifier) { this.identifier = identifier; }

	public String getVersion() { return value; }
	//public void setVersion(Float version) { this.version = version; }

	public boolean isBlock() { return block; }
	//public void setBlock(boolean block) { this.block = block; }
}
