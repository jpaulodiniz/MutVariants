package br.ufmg.labsoft.mutvariants.mutops;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import br.ufmg.labsoft.mutvariants.core.MutantsGenerator;
import br.ufmg.labsoft.mutvariants.util.JavaBinaryOperatorsGroups;
import br.ufmg.labsoft.mutvariants.util.TypeUtil;

public class ROR extends BinaryExpressionOperatorReplacement {

	public ROR() {
		super.bynaryOperatorGroup = JavaBinaryOperatorsGroups.relationalOperators;
	}

	@Override
	public boolean isChangePoint(Node node, MutantsGenerator mGen) {

		if (super.isChangePoint(node, mGen)) {
			BinaryExpr be = (BinaryExpr)node;
		
			Operator binaryOperator = be.getOperator();
		
			if (JavaBinaryOperatorsGroups.relationalOperators.contains(binaryOperator)) {
				
				if (JavaBinaryOperatorsGroups.equalityOperators.contains(binaryOperator)) {
	
					if (be.getRight().toString().equals("null") || be.getLeft().toString().equals("null")) {
	//					equalityOperatorNoNumbers = true;
						return false;
					}
					
					ResolvedType typeLeft = JavaParserFacade.get(mGen.getTypeSolver()).getType(be.getLeft());
					ResolvedType typeRight = JavaParserFacade.get(mGen.getTypeSolver()).getType(be.getRight());
	
					//ensures mutations on expressions like var.size() == 4 and not on var == obj
					if (!TypeUtil.isNumberPrimitiveOrWrapper(typeLeft) || !TypeUtil.isNumberPrimitiveOrWrapper(typeRight)) {
	//					equalityOperatorNoNumbers = true;
						return false;
					}
				}

				return true;
			}
		}

		return false;
	}
}
