package br.ufmg.labsoft.mutvariants.mutops;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;

import br.ufmg.labsoft.mutvariants.core.MutantsGenerator;
import br.ufmg.labsoft.mutvariants.util.JavaBinaryOperatorsGroups;

public class LCR extends BinaryExpressionOperatorReplacement {

	public LCR() {
		super.bynaryOperatorGroup = JavaBinaryOperatorsGroups.logicalOperators;
	}

	@Override
	public boolean isChangePoint(Node node, MutantsGenerator mGen) {

		if (super.isChangePoint(node, mGen)) {
			BinaryExpr be = (BinaryExpr)node;

			Operator binaryOperator = be.getOperator();

			if (JavaBinaryOperatorsGroups.logicalOperators.contains(binaryOperator)) {
				return true;
			}
		}

		return false;
	}
}
