package br.ufmg.labsoft.mutvariants.mutops;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;

import br.ufmg.labsoft.mutvariants.entity.MutationInfo;
import br.ufmg.labsoft.mutvariants.core.MutantsGenerator;

public abstract class BinaryExpressionOperatorReplacement implements MutationOperator {

	protected EnumSet<Operator> bynaryOperatorGroup;

	private EnumSet<Operator> availableOperatorsForMutation(Operator original) {

		EnumSet<Operator> tempEnumSet = this.bynaryOperatorGroup.clone();
		tempEnumSet.remove(original);

		return tempEnumSet;
	}

	/**
	 * ensures that condition expressions of statements like For, While, etc won't be mutated
	 */
	@Override
	public boolean isChangePoint(Node node, MutantsGenerator mGen) {
		
		if (node instanceof BinaryExpr) {
			return node.findAncestor(ExpressionStmt.class).isPresent() || //expression statement
					node.findAncestor(IfStmt.class).isPresent(); //conditional expression inside if
		}

		return false;
	}

	@Override
	public Node generateMutants(Node original, MutantsGenerator mGen) {

		if (original instanceof BinaryExpr) {
			return this.generateMutants((BinaryExpr)original, mGen);
		}
		else {
			return original;
		}
	}

	protected Expression generateMutants(BinaryExpr original, MutantsGenerator mGen) {

		EnumSet<Operator> mOperators = availableOperatorsForMutation(original.getOperator());

//		EnumSet<Operator> mOperators = mGen.isAllPossibleMutationsPerChangePoint() ? 
//				availableOperatorsForMutation(original.getOperator()) :
//				EnumSet.of(operatorForMutation(original.getOperator(), false) ); 

		if (mOperators == null || mOperators.isEmpty()) return null; //<<, >>, >>>, &, |, ... TODO review 

		List<String> groupOfMutants = new ArrayList<>(); //Issue #4

		Expression mutantExpressionTemp = original.clone();

		for (Operator op : mOperators) {

			if (Math.random() > mGen.getMutationRate()) continue;

			BinaryExpr mutated = original.clone();
			mutated.setOperator(op);

			String mutantVariableName = mGen.nextMutantVariableName();
			groupOfMutants.add(mutantVariableName);

			//generation mutant information for mutants catalog
			MutationInfo mInfo = new MutationInfo();
			mInfo.setMutantVariableName(mutantVariableName);
			mInfo.setMutationOperator(this.getName());
			mInfo.setInfoBeforeMutation(original.getOperator().asString());
			mInfo.setInfoAfterMutation(op.asString());
			mInfo.setMutatedClass(mGen.currentClassFQN);
			mInfo.setMutatedOperation(mGen.currentOperation);
			mGen.addMutantInfoToCatalog(mInfo);

			mutantExpressionTemp = new ConditionalExpr(
					new NameExpr(mutantVariableName), 
					new EnclosedExpr(mutated),
					new EnclosedExpr(mutantExpressionTemp.clone()));
		}

		mGen.addGroupOfMutants(groupOfMutants);

		return new EnclosedExpr(mutantExpressionTemp);
	}
}
