package br.ufmg.labsoft.mutvariants.mutants;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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
public class AllBinaryExprMutationStrategy extends BinaryExprMutationStrategy {
	
	/**
	 * 
	 * @author jpaulo
	 *
	 */
	protected class MutationVisitor extends VoidVisitorAdapter<MutantsGenerator> {
		
		@Override
		public void visit(MethodDeclaration methodDecl, MutantsGenerator mGen) {
			mGen.currentMethod = methodDecl.getNameAsString();
			super.visit(methodDecl, mGen);
		}
		
		@Override
		public void visit(ForStmt stmt, MutantsGenerator mGen) {
	        stmt.getBody().accept(this, mGen);
	        
	        if (mGen.getMutateLoopConditions()) {
		        stmt.getCompare().ifPresent(l -> l.accept(this, mGen));
//		        stmt.getInitialization().forEach(p -> p.accept(this, mGen));
//		        stmt.getUpdate().forEach(p -> p.accept(this, mGen));
	        }
	        stmt.getComment().ifPresent(l -> l.accept(this, mGen));
		}

		@Override
		public void visit(WhileStmt stmt, MutantsGenerator mGen) {
			stmt.getBody().accept(this, mGen);
	        if (mGen.getMutateLoopConditions()) {
	        	stmt.getCondition().accept(this, mGen);
	        }
			stmt.getComment().ifPresent(l -> l.accept(this, mGen));
		}

		@Override
		public void visit(DoStmt stmt, MutantsGenerator mGen) {
			stmt.getBody().accept(this, mGen);
	        if (mGen.getMutateLoopConditions()) {
				stmt.getCondition().accept(this, mGen);
	        }
			stmt.getComment().ifPresent(l -> l.accept(this, mGen));
		}

		@Override
		public void visit(BinaryExpr be, MutantsGenerator mGen) {
			
			boolean isChangePoint = isChangePoint(be, mGen);
			String currMethod = mGen.currentMethod;
			super.visit(be, mGen);
			
			if (isChangePoint) {
				mGen.currentMethod = currMethod;
				Expression mutatedExpr = AllBinaryExprMutationStrategy.this.mutateBinaryExpression(be, mGen);
				if (mutatedExpr != null) {
					be.replace(mutatedExpr);
				}
			}
		}
	};

	/**
	 * 
	 */
	@Override
	public void generateMutants(ClassOrInterfaceDeclaration mutClass, MutantsGenerator mutGen) {

		MutationVisitor mutVisitor = new MutationVisitor();
		mutClass.accept(mutVisitor, mutGen);		
	}

}
