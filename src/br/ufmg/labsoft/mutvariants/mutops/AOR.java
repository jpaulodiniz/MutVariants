package br.ufmg.labsoft.mutvariants.mutops;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import br.ufmg.labsoft.mutvariants.core.MutantsGenerator;
import br.ufmg.labsoft.mutvariants.util.JavaBinaryOperatorsGroups;
import br.ufmg.labsoft.mutvariants.util.TypeUtil;

public class AOR extends BinaryExpressionOperatorReplacement {

	public AOR() {
		super.bynaryOperatorGroup = JavaBinaryOperatorsGroups.arithmeticOperators;
	}

	@Override
	public boolean isChangePoint(Node node, MutantsGenerator mGen) {

		if (super.isChangePoint(node, mGen)) {
			BinaryExpr be = (BinaryExpr)node;
		
			Operator binaryOperator = be.getOperator();

			if (bynaryOperatorGroup.contains(binaryOperator)) {

				//avoiding mutating String concatenator operator +
				if (BinaryExpr.Operator.PLUS.equals(binaryOperator)) {

					ResolvedType type = JavaParserFacade.get(mGen.getTypeSolver()).getType(be);
					if (TypeUtil.isString(type)) {
						return false;
					}
				}

				return true;
			}
		}
		
		return false;
	}

	@Override
	public String getName() {
		return "AOR";
	}
}
