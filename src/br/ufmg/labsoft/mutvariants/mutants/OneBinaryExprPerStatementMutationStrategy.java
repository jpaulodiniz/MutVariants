package br.ufmg.labsoft.mutvariants.mutants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

/**
 * 
 * @author jpaulo
 *
 */
public class OneBinaryExprPerStatementMutationStrategy implements MutationStrategy {

	/**
	 * @param original
	 * @return
	 */
	@Override
	public void generateMutants(ClassOrInterfaceDeclaration mClass, MutantsGenerator mGen) {

		List<IfStmt> ifStatements = mClass.findAll(IfStmt.class);
		List<ExpressionStmt> exprStatements = mClass.findAll(ExpressionStmt.class);
		List<ReturnStmt> returnStatements = mClass.findAll(ReturnStmt.class);

		for (IfStmt ifStmt : ifStatements) {
			List<BinaryExpr> binaryExpressions = new ArrayList<>();
			Expression ifCondition = ifStmt.getCondition();

			binaryExpressions.addAll(ifCondition.findAll(BinaryExpr.class));
			this.mutateOneBinaryExpression(binaryExpressions, mGen);
		}

		for (ExpressionStmt exprStmt : exprStatements) {
			List<BinaryExpr> binaryExpressions = exprStmt.findAll(BinaryExpr.class);
			this.mutateOneBinaryExpression(binaryExpressions, mGen);
		}

		for (ReturnStmt returnStmt : returnStatements) {
			List<BinaryExpr> binaryExpressions = returnStmt.findAll(BinaryExpr.class);
			this.mutateOneBinaryExpression(binaryExpressions, mGen);
		}
		
		//TODO DoStmt, WhileStmt, ForStmt, ForeachStmt ???
	}
	
	/**
	 * mutate one single binary expression, chosen randomly
	 * from a list of binary expressions of a same statement
	 * Example: Statement: a + b + c -> List: [(a + (b + c)), (b + c)]
	 * @param binaryExpressions from same statement
	 * @return true if success
	 */
	private boolean mutateOneBinaryExpression(List<BinaryExpr> binaryExpressions, MutantsGenerator mGen) {

		if (!binaryExpressions.isEmpty()) {
			
			if (binaryExpressions.size() > 1) {
				Collections.shuffle(binaryExpressions);
			}

			for (BinaryExpr originalExpr : binaryExpressions) {
				EnclosedExpr mutatedExpr = mGen.mutateBinaryExpression(originalExpr);
				if (mutatedExpr != null) {
					return originalExpr.replace(mutatedExpr);
				}
			}
		}

		return false;
	}

}
