package br.ufmg.labsoft.mutvariants.core;

import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import br.ufmg.labsoft.mutvariants.listeners.ListenerCallsInserter;
import br.ufmg.labsoft.mutvariants.mutops.MutationOperator;

class MutationVisitor extends VoidVisitorAdapter<MutantsGenerator> {

	@Override
	public void visit(ClassOrInterfaceDeclaration classDecl, MutantsGenerator mGen) {
		// previous is necessary due to attributes in nested classes
		Set<NameExpr> previous = mGen.classFinalAttributesNonInitialized;
		mGen.classFinalAttributesNonInitialized = mGen.findFinalAttrsNonInitialized(classDecl);
		super.visit(classDecl, mGen);
		mGen.classFinalAttributesNonInitialized = previous;
	}

	/*
	 * specific for SBR mutop (TODO: refactor)
	 */
	@Override
	public void visit(BlockStmt block, MutantsGenerator mGen) {
		
		Set<NameExpr> vni = mGen.findVariablesNonInitialized(block);
		mGen.blockVariablesNonInitialized.push(vni);

		super.visit(block, mGen);

		mGen.blockVariablesNonInitialized.pop();
	}

	@Override
	public void visit(MethodDeclaration methodDecl, MutantsGenerator mGen) {
		
		if (!methodDecl.getBody().isPresent()) { // not abstract, not default in an interface
			return;
		}

		mGen.currentOperation = methodDecl.getNameAsString() + "_" + methodDecl.getBegin().get().line;

		Type returnType = methodDecl.getType();
		boolean retSafe = true;
		
		if (!(returnType instanceof VoidType)) { // function

			// has a return statement as direct child
			retSafe = methodDecl.getBody().get().getChildNodes().stream()
					.anyMatch(st -> st instanceof ReturnStmt);
		}

		if (!retSafe) {
			mGen.currentOperation += "__nrs";
		}

		super.visit(methodDecl, mGen);
		mGen.currentOperation = null;
	}

	@Override
	public void visit(ConstructorDeclaration constrDecl, MutantsGenerator mGen) {
		mGen.currentOperation = constrDecl.getNameAsString() + "_" + constrDecl.getBegin().get().line;
		super.visit(constrDecl, mGen);
		mGen.currentOperation = null;
	}

	@Override
	public void visit(BinaryExpr expr, MutantsGenerator mGen) {
		//check whether expr is a change point
		List<MutationOperator> mutOps = mGen.checkChangePoint(expr);
		//keep visiting
		super.visit(expr, mGen);
		//mutate
		mGen.generateMutants(expr, mutOps);
	}

	@Override
	public void visit(ExpressionStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		mGen.generateMutants(stmt, mutOps);
	}
	
	@Override
	public void visit(VariableDeclarator vd, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(vd);
		super.visit(vd, mGen);
		mGen.generateMutants(vd, mutOps);
	}

	@Override
	public void visit(IfStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(WhileStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		if (mGen.getListenerCallsInstrumentation()) {
        	ListenerCallsInserter.insertListenerCallInLoopBody(stmt.getBody(), mGen);
        }
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(ForStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		if (mGen.getListenerCallsInstrumentation()) {
			ListenerCallsInserter.insertListenerCallInLoopBody(stmt.getBody(), mGen);
        }
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(ForeachStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		if (mGen.getListenerCallsInstrumentation()) {
			ListenerCallsInserter.insertListenerCallInLoopBody(stmt.getBody(), mGen);
        }
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(DoStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		if (mGen.getListenerCallsInstrumentation()) {
			ListenerCallsInserter.insertListenerCallInLoopBody(stmt.getBody(), mGen);
        }
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(SwitchStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		mGen.generateMutants(stmt, mutOps);
	}
}
