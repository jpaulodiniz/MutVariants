package br.ufmg.labsoft.mutvariants.mutants;

import java.util.EnumSet;

import br.ufmg.labsoft.mutvariants.entity.MutantInfo;
import br.ufmg.labsoft.mutvariants.util.JavaBinaryOperatorsGroups;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

/**
 * Issue #1
 * @author jpaulo
 *
 */
public class AllBinaryExprSchemataLibMutationStrategy extends AllBinaryExprMutationStrategy {

	/**
	 * 
	 * @author jpaulo
	 *
	 */
	private class NullComparisonVisitor extends com.github.javaparser.ast.visitor.GenericVisitorAdapter<Boolean, Void> {
		
		@Override
		public Boolean visit(BinaryExpr be, Void v) {
			
			if (JavaBinaryOperatorsGroups.equalityOperators.contains(be.getOperator())) {
				if (be.getRight().toString().equals("null")) {
					return true;
				}
			}

			Boolean result = super.visit(be, v);
            return (result != null) ? result : false;
		}
	};
		
	/**
	 * @author jpaulo
	 * expressions like <code>obj != null && obj.something()</code> and
	 * <code>obj == null || obj.something()</code> are not considered changePoint
	 * because they cause null pointer exception even if the respective mutants 
	 * are not set to <code>true</code> 
	 */
	@Override
	public boolean isChangePoint(BinaryExpr be, MutantsGenerator mGen) {
		
//		if (Operator.AND.equals(original.getOperator())) return false;
		if (JavaBinaryOperatorsGroups.logicalOperators.contains(be.getOperator())) { // && ||

			NullComparisonVisitor nullVisitor = new NullComparisonVisitor();

			Expression left = be.getLeft();

			/*
			 * TODO [JP] improve it
			 * specific hardcoded for Chess system, avoiding vector out of bounds indexation
			 * even whithout any active mutant
			 */
			if (left.toString().startsWith("nTam >= 11")) return false;

			Boolean leftResult = left.accept(nullVisitor, null);
			if (leftResult != null && leftResult) {
				return false;
			}

			Expression right = be.getRight();
			Boolean rightResult = right.accept(nullVisitor, null);
			if (rightResult != null && rightResult) {
				return false;
			}

			return true; //TODO [JP] temp 
		}
		
		return super.isChangePoint(be, mGen);
	}
	
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
			
			//generation mutant information for mutants catalog
			MutantInfo mInfo = new MutantInfo();
			mInfo.setMutantVariableName(mutantVariableName);
			mInfo.setOriginaBinaryOperator(original.getOperator().asString());
			mInfo.setMutatedBinaryOperator(op.asString());
			mInfo.setMutatedClass(mGen.currentClassFQN);
			mInfo.setMutatedMethod(mGen.currentMethod);
			mGen.addMutantInfoToCatalog(mInfo);
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
