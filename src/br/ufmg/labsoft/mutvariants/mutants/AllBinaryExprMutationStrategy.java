package br.ufmg.labsoft.mutvariants.mutants;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import br.ufmg.labsoft.mutvariants.listeners.ListenerUtil;

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
		public void visit(SingleMemberAnnotationExpr annotExpr, MutantsGenerator mGen) {
		}

		@Override
		public void visit(MethodDeclaration methodDecl, MutantsGenerator mGen) {
			mGen.currentOperation = methodDecl.getNameAsString() + "_" + methodDecl.getBegin().get().line;

			long mutCountBefore = mGen.getMutantsCounterGlobal();

			super.visit(methodDecl, mGen);

			long mutCountAfter = mGen.getMutantsCounterGlobal();
			//if mGen.generateListener()) TODO create attr and replicate this if
			ListenerUtil.insertListenerCallInMethodBody(methodDecl, mGen, mutCountBefore, mutCountAfter);
		}

		@Override
		public void visit(ConstructorDeclaration constructorDecl, MutantsGenerator mGen) {
			mGen.currentOperation = constructorDecl.getNameAsString() + "_" + constructorDecl.getBegin().get().line;

			long mutCountBefore = mGen.getMutantsCounterGlobal();
			super.visit(constructorDecl, mGen);

			long mutCountAfter = mGen.getMutantsCounterGlobal();
			ListenerUtil.insertListenerCallInConstructorBody(constructorDecl, mGen, mutCountBefore, mutCountAfter);
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

			ListenerUtil.insertListenerCallInLoopBody(stmt.getBody(), mGen);
		}

		@Override
		public void visit(ForeachStmt stmt, MutantsGenerator mGen) {
	        stmt.getBody().accept(this, mGen);
//	        stmt.getIterable().accept(this, mGen);
//	        stmt.getVariable().accept(this, mGen);
	        stmt.getComment().ifPresent(l -> l.accept(this, mGen));
	        
			ListenerUtil.insertListenerCallInLoopBody(stmt.getBody(), mGen);
		}

		@Override
		public void visit(WhileStmt stmt, MutantsGenerator mGen) {
			stmt.getBody().accept(this, mGen);
	        if (mGen.getMutateLoopConditions()) {
	        	stmt.getCondition().accept(this, mGen);
	        }
			stmt.getComment().ifPresent(l -> l.accept(this, mGen));

			ListenerUtil.insertListenerCallInLoopBody(stmt.getBody(), mGen);
		}

		@Override
		public void visit(DoStmt stmt, MutantsGenerator mGen) {
			stmt.getBody().accept(this, mGen);
	        if (mGen.getMutateLoopConditions()) {
				stmt.getCondition().accept(this, mGen);
	        }
			stmt.getComment().ifPresent(l -> l.accept(this, mGen));

			ListenerUtil.insertListenerCallInLoopBody(stmt.getBody(), mGen);
		}

		@Override
		public void visit(BinaryExpr be, MutantsGenerator mGen) {

			boolean isChangePoint = isChangePoint(be, mGen);
			String currOperation = mGen.currentOperation;
			super.visit(be, mGen);

			if (isChangePoint) {
				mGen.currentOperation = currOperation;
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
