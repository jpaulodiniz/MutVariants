package br.ufmg.labsoft.mutvariants.mutants;

import java.util.EnumSet;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

/**
 * 
 * @author jpaulo
 *
 */
public class AllBinaryExprSchemataLibMutationStrategy extends AllBinaryExprMutationStrategy {

	/**
	 * 
	 * @param original operand1 ORIGINAL operand2
	 * @param mGen
	 * @return (_PREFIX_mut# ? (operand1 MUTATION1 operand2) : (operand1 ORIGINAL operand2)) or 
	 * (_PREFIX_mut## ? (operand1 MUTATION2 operand2) : (_PREFIX_mut# ? (operand1 MUTATION1 operand2) : (operand1 ORIGINAL operand2)))
	 * @throws Exception
	 */
	@Override
	public Expression mutateBinaryExpression(BinaryExpr original, MutantsGenerator mGen) { //throws RuntimeException, boolean allAvailableMutants

		EnumSet<Operator> mOperators = 
				availableOperatorsForMutation(original.getOperator(), false);

		if (mOperators == null || mOperators.isEmpty()) return null;

		NodeList<Expression> mutantsList = new NodeList<>();

		for (Operator op : mOperators) {
			String mutantVariableName = mGen.nextMutantVariableName();
			mutantsList.add(new NameExpr(mutantVariableName));
		}

		NodeList<Expression> arguments = new NodeList<>();
		arguments.add(original.getLeft().clone());
		arguments.add(original.getRight().clone());

		MethodCallExpr mce = new MethodCallExpr((Expression)null, "tempname_", arguments);
		mce.getArguments().addAll(mutantsList);

		switch (original.getOperator()) {
		case AND:
		case OR:
			mce.setName("LCR_" + original.getOperator().toString().toLowerCase());
			break;
		case PLUS:
		case MINUS:
		case MULTIPLY:
		case DIVIDE:
		case REMAINDER:
			mce.setName("AOR_" + original.getOperator().toString().toLowerCase());
			break;
		case EQUALS:
		case NOT_EQUALS:
		case LESS:
		case LESS_EQUALS:
		case GREATER:
		case GREATER_EQUALS:
			mce.setName("ROR_" + original.getOperator().toString().toLowerCase());
			break;
		default:
			break;
		}
		
		return mce;
	}

}
