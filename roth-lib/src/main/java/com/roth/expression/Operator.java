package com.roth.expression;

import java.util.List;

import com.roth.base.util.Data;

/**
 * Operators used by com.roth.expression.Expression.  The "NOT" or "!" operator is
 * exclusively defined by the Expression class, so it is not represented here.
 * @author james
 *
 */
public enum Operator {
	AND("&&", true, true, "AND"),
	NAND("!&", true, true, null),
	OR("||", true, true, "OR"),
	NOR("!|", true, true, null),
	EQ("==", true, false, "="),
	NEQ("!=", true, false, "!="),
	LIKE("=~", true, false, "LIKE"),
	NLIKE("!~", true, false, "NOT LIKE"),
	GT(">", true, false, ">"),
	GTE(">=", true, false, ">="),
	LT("<", true, false, "<"),
	LTE("<=", true, false, "<="),
	IN("[+]", true, false, "IN"),
	NIN("[-]", true, false, "NOT IN"),
	EMPTY("<>", false, false, "IS NULL"),
	NEMPTY("<!>", false, false, "IS NOT NULL");
	
	private String alternate;
	private boolean binary;
	private boolean logical;
	private String sqlOperator;
	
	private Operator(String alternate, boolean binary, boolean logical, String sqlOperator) {
		this.alternate = alternate;
		this.binary = binary;
		this.logical = logical;
		this.sqlOperator = sqlOperator;
	}
	
	public String getAlternate() { return alternate; }
	public boolean isBinary() { return binary; }
	public boolean isLogical() { return logical; }
	public String getSqlOperator() { return sqlOperator; }
	
	public static Operator altValueOf(String source) {
		if (source == null)
			throw new NullPointerException();
		for (Operator operator : Operator.values())
			if (operator.toString().equalsIgnoreCase(source) || operator.alternate.equals(source))
				return operator;
		throw new IllegalArgumentException(source);
	}
	
	public static Operator not(Operator operator) {
		return switch (operator) {
		case AND -> NAND;
		case NAND -> AND;
		case OR -> NOR;
		case NOR -> OR;
		case EQ -> NEQ;
		case NEQ -> EQ;
		case LIKE -> NLIKE;
		case NLIKE -> LIKE;
		case GT -> LTE;
		case GTE -> LT;
		case LT -> GTE;
		case LTE -> GT;
		case IN -> NIN;
		case NIN -> IN;
		case EMPTY -> NEMPTY;
		case NEMPTY -> EMPTY;
		default -> throw new IllegalArgumentException();
		};
	}

	public boolean evaluate(String value) {
		return switch(this) {
		case EMPTY -> Data.isEmpty(value);
		case NEMPTY -> !Data.isEmpty(value);
		default -> throw new IllegalArgumentException("Unary String parameter is only applicable to comparison operator (EMPTY).");
		}; 
	}
	
	public boolean evaluate(boolean valueA, boolean valueB) {
		return switch(this) {
		case AND -> valueA && valueB;
		case NAND -> !(valueA && valueB);
		case OR -> valueA || valueB;
		case NOR -> !(valueA || valueB);
		default -> throw new IllegalArgumentException("Boolean parameters are only applicable to logical operators (AND, NAND, OR, NOR).");
		};
	}
	
	public boolean evaluate(String valueA, String valueB) {
		if (Data.isEmpty(valueA) || Data.isEmpty(valueB))
			throw new IllegalArgumentException("Values must be supplied for comparison.");
		int result = valueA.compareTo(valueB);
		return switch(this) {
		case EQ -> result == 0;
		case NEQ -> result != 0;
		case LIKE -> valueA.contains(valueB);
		case NLIKE -> !valueA.contains(valueB);
		case GT -> result > 0;
		case GTE -> result >= 0;
		case LT -> result < 0;
		case LTE -> result <= 0;
		default -> throw new IllegalArgumentException("String values are only applicable to comparison operators (EQ, NEQ, LIKE, NLIKE, GT, GTE, LT, LTE).");
		};
	}
	
	public boolean evaluate(String valueA, List<String> valuesB) {
		return switch(this) {
		case IN -> Data.in(valueA, valuesB);
		case NIN -> !Data.in(valueA, valuesB);
		default -> throw new IllegalArgumentException("List values are only applicable to comparison operators (IN, NIN).");
		};
	}
}