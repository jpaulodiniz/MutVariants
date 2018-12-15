package br.ufmg.labsoft.mutvariants.mutants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

/**
 * 
 * @author jpaulo
 *
 */
public class OneBinaryExprPerStatementMutationStrategy extends BinaryExprMutationStrategy {

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
		
		if (mGen.getMutateLoopConditions()) {
			List<ForStmt> forStatements = mClass.findAll(ForStmt.class);
			List<WhileStmt> whileStatements = mClass.findAll(WhileStmt.class);
			List<DoStmt> doStatements = mClass.findAll(DoStmt.class);

			for (ForStmt forStmt : forStatements) {
				List<BinaryExpr> binaryExpressions = new ArrayList<>();
				Expression forComparison = forStmt.getCompare().get();

				binaryExpressions.addAll(forComparison.findAll(BinaryExpr.class));
				this.mutateOneBinaryExpression(binaryExpressions, mGen);
			}

			for (WhileStmt whileStmt : whileStatements) {
				List<BinaryExpr> binaryExpressions = new ArrayList<>();
				Expression whileCondition = whileStmt.getCondition();

				binaryExpressions.addAll(whileCondition.findAll(BinaryExpr.class));
				this.mutateOneBinaryExpression(binaryExpressions, mGen);
			}

			for (DoStmt doStmt : doStatements) {
				List<BinaryExpr> binaryExpressions = new ArrayList<>();
				Expression doCondition = doStmt.getCondition();

				binaryExpressions.addAll(doCondition.findAll(BinaryExpr.class));
				this.mutateOneBinaryExpression(binaryExpressions, mGen);
			}
		}
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
				Expression mutatedExpr = this.mutateBinaryExpression(originalExpr, mGen);
				if (mutatedExpr != null) {
					return originalExpr.replace(mutatedExpr);
				}
			}
		}

		return false;
	}
}
