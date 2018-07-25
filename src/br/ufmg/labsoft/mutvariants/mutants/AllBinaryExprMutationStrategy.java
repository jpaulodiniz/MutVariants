package br.ufmg.labsoft.mutvariants.mutants;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * 
 * @author jpaulo
 *
 */
public class AllBinaryExprMutationStrategy implements MutationStrategy {

	private class MutationVisitor extends VoidVisitorAdapter<MutantsGenerator> {
		
		@Override
		public void visit(ForStmt stmt, MutantsGenerator mg) {
	        stmt.getBody().accept(this, mg);
//	        stmt.getCompare().ifPresent(l -> l.accept(this, mg));
//	        stmt.getInitialization().forEach(p -> p.accept(this, mg));
//	        stmt.getUpdate().forEach(p -> p.accept(this, mg));
	        stmt.getComment().ifPresent(l -> l.accept(this, mg));
		}

		@Override
		public void visit(WhileStmt stmt, MutantsGenerator mg) {
			stmt.getBody().accept(this, mg);
//			stmt.getCondition().accept(this, mg);
			stmt.getComment().ifPresent(l -> l.accept(this, mg));
		}

		@Override
		public void visit(DoStmt stmt, MutantsGenerator mg) {
			stmt.getBody().accept(this, mg);
//			stmt.getCondition().accept(this, mg);
			stmt.getComment().ifPresent(l -> l.accept(this, mg));
		}

		@Override
		public void visit(BinaryExpr be, MutantsGenerator mg) {
			super.visit(be, mg);

			Expression mutatedExpr = mg.mutateBinaryExpression(be);
			if (mutatedExpr != null) {
				be.replace(mutatedExpr);
			}
		}
	};

	/**
	 * @param original
	 * @return
	 */
	@Override
	public void generateMutants(ClassOrInterfaceDeclaration mutClass, MutantsGenerator mutGen) {

		MutationVisitor mutVisitor = new MutationVisitor();
		mutClass.accept(mutVisitor, mutGen);		
	}
}
