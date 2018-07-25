package br.ufmg.labsoft.mutvariants.util;

import java.util.EnumSet;

import com.github.javaparser.ast.expr.BinaryExpr.Operator;

/**
 *  Syntactic Groups (EnumSets) of Java operators
 */
public class JavaOperatorsGroups {

	/**
	 * + - * / %
	 */
	public static final EnumSet<Operator> arithmeticOperators = EnumSet.of(Operator.PLUS, 
			Operator.MINUS, Operator.MULTIPLY, Operator.DIVIDE, Operator.REMAINDER);

	/**
	 * && ||
	 */
	public static final EnumSet<Operator> logicalOperators = EnumSet.of(Operator.AND, 
			Operator.OR);

	/**
	 * == !=
	 */
	public static final EnumSet<Operator> equalityOperators = EnumSet.of(Operator.EQUALS, Operator.NOT_EQUALS);
	
	/**
	 * == != < <= > >=
	 */
	public static final EnumSet<Operator> relationalOperators = EnumSet.of(Operator.EQUALS,
			Operator.NOT_EQUALS, Operator.LESS, Operator.LESS_EQUALS, 
			Operator.GREATER, Operator.GREATER_EQUALS);

	/**
	 * ^ & | << >> >>>
	 */
	public static final EnumSet<Operator> bitwiseOperators = EnumSet.of(Operator.XOR,
			Operator.BINARY_AND, Operator.BINARY_OR,
			Operator.LEFT_SHIFT, Operator.SIGNED_RIGHT_SHIFT, Operator.UNSIGNED_RIGHT_SHIFT);

/*	some useful Set Java commands
	public static void main(String... args) {
		System.out.println(arithmeticOperators.contains(Operator.AND));
		System.out.println(arithmeticOperators.contains(Operator.MINUS));
		System.out.println(EnumSet.complementOf(EnumSet.of(Operator.DIVIDE)));
		System.out.println(arithmeticOperators instanceof Set);
	}
*/
}
