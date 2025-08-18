package com.roth.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.roth.base.util.Data;
import com.roth.export.util.CsvRecord;

public class Expression implements Serializable {
	private static final long serialVersionUID = -6969992839102138311L;

	private static class StringListExpression implements Serializable {
		private static final long serialVersionUID = 3072671862582621456L;
		
		private String string;
		private List<String> list;
		private Expression expression;
		
		public StringListExpression(String string, List<String> list, Expression expression) {
			this.string = string;
			this.list = list;
			this.expression = expression;
		}
		
		public String toString(int level) {
			return string != null ? string
				 : list != null ? list.stream().map(Object::toString).collect(Collectors.joining(","))
				 : expression != null ? expression.toDebugString(level) : "";
		}
	}
	
	private IdentProvider provider;
	private String source;
	private boolean not = false;
	private StringListExpression left;  // may be a String or Expression
	private Operator operator;
	private StringListExpression right; // may be a String, List<String>, or Expression
	
	
	/*
	
	Possibilities:
	
		A NOT expression
		D expression logical_op expression
		C identifier compare_op expression
		B identifier compare_op value
		B identifier IN|NIN (valueList)
		E identifier EMPTY
	
	*/
	
	public Expression(String source, IdentProvider provider) {
		this.provider = provider;
		this.source = source;
		parse();
	}
	
	/**
	 * Cleans up the source string, eliminating extraneous spaces,
	 * and validating that all parentheses have matches. 
	 * @param source
	 * @return
	 */
	private static String cleanAndValidate(String source) {
		String result = source.trim();
		// Eliminate any double spaces
		while (result.indexOf("  ") > -1)
			result = result.replace("  ", " ");
		// Eliminate extraneous spaces around parentheses and commas (to aid in parsing later)
		result = result.replace("( ", "(").replace(" )", ")").replace(" ,", ",").replace(", ", ",");
		// Verify that all parentheses have matches
		int left = 0;
		int right = 0;
		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i) == '(')
				left++;
			else if (source.charAt(i) == ')')
				right++;
		}
		if (left != right)
			throw new IllegalArgumentException("Parentheses don't match.");
		return result;
	}
	
	private static StringBuilder newSection(StringBuilder section, List<String> sections, String operator, List<String> operators) {
		sections.add(section.toString());
		operators.add(operator);
		return new StringBuilder("");
	}
	
	private String recombine(List<String> sections, List<String> operators) {
		StringBuilder result = new StringBuilder("");
		for (int i = 1; i < operators.size(); i++)
			result.append(sections.get(i) + " " + operators.get(i) + " ");
		result.append(sections.get(sections.size() - 1));
		return result.toString();
	}
	
	private static boolean checkForLogicalOperator(String source, int index, String operator) {
		return source.length() - operator.length() >= index && source.substring(index, index + operator.length()).equalsIgnoreCase(operator);
	}
	
	private static int checkForLogicalOperator(String source, int index, Operator operator) {
		String optionA = String.format(" %s ", operator.toString());
		String optionB = String.format(" %s ", operator.getAlternate());
		return checkForLogicalOperator(source, index, optionA) ? optionA.length() 
			 : checkForLogicalOperator(source, index, optionB) ? optionB.length()
		     : -1;
	}
	
	private static final Operator[] BINARY_LOGICAL_OPERATORS = new Operator[] { Operator.AND, Operator.NAND, Operator.OR, Operator.NOR };
	private static final String BINARY_LOGICAL_MATCH;
	static {
		StringBuilder result = new StringBuilder("(?s).*\\b(");
		boolean first = true;
		for (Operator op : BINARY_LOGICAL_OPERATORS) {
			if (!first)
				result.append("|");
			first = false;
			result.append(op.toString() + "|" + Pattern.quote(op.getAlternate()));
		}
		result.append(")\\b.*");
		BINARY_LOGICAL_MATCH = result.toString();
	}
	
	private record CheckResult (Operator operator, int length) { }
	
	private static CheckResult checkForLogicalOperator(String source, int index) {
		for (Operator op : BINARY_LOGICAL_OPERATORS) {
			int len = checkForLogicalOperator(source, index, op);
			if (len > -1)
				return new CheckResult(op, len);
		}
		return null;
	}
	
	/**
	 * A parsing class that keeps track of parentheses, quotes, and escape characters.
	 * @author james
	 *
	 */
	private static class ParseState {
		int paren = 0;
		boolean quoted = false;
		int escaped = -1;
		
		void evalChar(int i, char c) {
			if (!quoted && c == '(')
				paren++;
			if (!quoted && c == ')')
				paren--;
			if (escaped == -1 && c == '"')
				quoted = !quoted;
			if (quoted && c == '\\')
				escaped = i;
			if (i == escaped + 1)
				escaped = -1;
		}
		
		/**
		 * Determines whether a space character is part of a value or can be considered a 
		 * delimiter in the expression.
		 * @return
		 */
		boolean canDelimit() {
			return !quoted && paren == 0;
		}
	}
	
	/**
	 * Analyze the source expression string.  Break it into individual atomic expressions,
	 * or clustered expressions surrounded by parentheses. 
	 * @param source
	 * @param sections
	 * @param operators
	 */
	private static void analyze(String source, List<String> sections, List<String> operators) {
		String parse = cleanAndValidate(source);
		ParseState state = new ParseState();
		StringBuilder section = new StringBuilder("");
		int i = 0;
		while (i < parse.length()) {
			state.evalChar(i, source.charAt(i));
			boolean readOp = state.canDelimit();
			CheckResult checkResult;
			if (readOp && (checkResult = checkForLogicalOperator(parse, i)) != null) {
				section = newSection(section, sections, checkResult.operator.toString(), operators);
				i += checkResult.length;
			}
			else {
				section.append(parse.charAt(i));
				i++;
			}
		}
		sections.add(section.toString());
	}
	
	/**
	 * Process singular segment.  The analyze method only came up with one; either surrounded 
	 * by parentheses or containing no logical operators.
	 * @param exp
	 */
	private void processSingular(String exp) {
		if (exp.toUpperCase().matches(BINARY_LOGICAL_MATCH)) {
			// More than one expression; due to parentheses; remove the parentheses and re-parse
			source = exp.substring(1, exp.length() - 1).trim();
			parse();
		}
		else {
			// Only one expression
			if (exp.startsWith("(") && exp.endsWith(")"))
				exp = exp.substring(1, exp.length() - 1).trim();
			String[] segments = splitSegments(exp);
			if (segments.length > 3)
				throw new IllegalArgumentException("The expression contains too many parts.");
			left = new StringListExpression(segments[0], null, null);
			operator = Operator.altValueOf(segments[1]);
			if (operator == Operator.EMPTY && segments.length > 2)
				throw new IllegalArgumentException("The expression contains too many parts.");
			right = operator == Operator.EMPTY ? null : evalRight(segments[2]);
		}
	}
	
	/**
	 * Parse the expression.  
	 */
	private void parse() {
		List<String> sections = new ArrayList<>();
		List<String> operators = new ArrayList<>();
		analyze(source, sections, operators);
		if (sections.size() == 1) {
			String exp = sections.get(0).trim();
			not = exp.toUpperCase().startsWith("NOT ") || exp.startsWith("!");
			if (not)
				exp = exp.startsWith("!") ? exp.substring(1).trim() : exp.substring(4);
			processSingular(exp);
		}
		else {
			left = new StringListExpression(null, null, new Expression(sections.get(0), provider));
			operator = Operator.altValueOf(operators.get(0));
			right = new StringListExpression(null, null, new Expression(recombine(sections, operators), provider));
		}
	}
	
	/**
	 * Split the segments within an atomic expression.
	 * @param source
	 * @return
	 */
	private static String[] splitSegments(String source) {
		ParseState state = new ParseState();
		List<String> segments = new ArrayList<>();
		StringBuilder segment = new StringBuilder("");
		int i = 0;
		while (i < source.length()) {
			state.evalChar(i, source.charAt(i));
			char c = source.charAt(i);
			if (c == ' ' && state.canDelimit()) {
				segments.add(segment.toString());
				segment = new StringBuilder("");
			}
			else
				segment.append(c);
			i++;
		}
		segments.add(segment.toString());
		return segments.toArray(new String[] {});
	}
	
	/**
	 * Evaluate the right-hand side of an expression when it is not another expression.
	 * This will evaluate a string or list of strings.
	 * @param source
	 * @return
	 */
	private StringListExpression evalRight(String source) {
		if (Data.in(operator, new Operator[] {Operator.IN, Operator.NIN})) {
			CsvRecord rec = new CsvRecord(source.substring(1, source.length() - 1));
			List<String> list = new ArrayList<>();
			for (int i = 0; i < rec.size(); i++)
				list.add(rec.getString(i));
			return new StringListExpression(null, list, null);
		}
		else if (source.startsWith("\"") && source.endsWith("\""))
			return new StringListExpression(source.substring(1, source.length() - 1), null, null);
		else
			return new StringListExpression(source, null, null);
	}
	
	/**
	 * Generate an indent for tree view in log.
	 * @param level
	 * @return
	 */
	private String genIndent(int level) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < level; i++)
			result.append("    ");
		return result.toString();
	}
	
	/**
	 * Get the appropriate tree mark.  The isLogical parameter is true when the expression is
	 * a logical one (each side another expression), and false when not a logical expression.
	 * The only exception is an expression using the EMPTY operator.
	 * @param isLogical
	 * @return
	 */
	private String getMark(boolean isLogical) {
		return String.format("  %s ", isLogical ? "├" : "└");
	}
	
	/**
	 * Reprocess the tree output for the log to place the "│" character in the breaks where necessary.
	 * @param source
	 * @return
	 */
	private String reprocess(String source) {
		String[] lines = Data.splitLF(source);
		for (int i = 2; i < lines.length - 2; i++) {
			String a = lines[i - 1];
			String b = lines[i];
			
			for (int x = 0; x < b.length(); x++)
				if (a.length() > x && Data.in("" + a.charAt(x), new String[] {"├", "│"}) && b.charAt(x) == ' ')
					b = b.substring(0, x) + "│" + b.substring(x + 1);
			lines[i] = b;
		}
		return Data.join(lines, "\n");
	}
	
	/**
	 * Print the expression in a log-friendly fashion.
	 * @return
	 */
	public String toDebugString() {
		return toDebugString(0);
	}
	
	/**
	 * Recursive method to print each level and node of the tree.
	 * @param level
	 * @return
	 */
	private String toDebugString(int level) {
		String output = this.hashCode() + "\n";

		String indent = genIndent(level);
		String mark = getMark(operator.isLogical());

		String n = not ? indent + getMark(true) + "NOT\n" : "";
		String l = left.toString(level + 1);
		String r = right == null ? "" : right.toString(level + 1);
		
		if (operator.isLogical())
			output += String.format("%s%sL:%s%n%sO:[%s]%n%sR:%s%n", n, indent + mark, l, indent + mark, operator.toString(), indent + getMark(false), r);
		else if (operator == Operator.EMPTY)
			output += String.format("%s%sL:[%s] O:[%s]", n, indent + mark, l, operator.toString());
		else
			output += String.format("%s%sL:[%s] O:[%s] R:[%s]", n, indent + mark, l, operator.toString(), r);
		
		return reprocess(output);
	}
	
	/**
	 * Evaluate the items in a list and return all items matching the expression.
	 * @param <T>
	 * @param source
	 * @return
	 */
	public <T> List<T> evaluate(List<T> source) {
		if (source == null || source.isEmpty())
			return source;
		List<T> result = new ArrayList<>();
		//for (T item : source)
			//if (provider.)
		return result;
	}
	
	/**
	 * Generate a SQL filter string from the expression.
	 * @return
	 */
	public String generateSQLFilter() {
		
		return null;
	}
}
