package br.ufmg.labsoft.mutvariants.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import br.ufmg.labsoft.mutvariants.mutops.MutationOperator;

class MutationVisitor extends VoidVisitorAdapter<MutantsGenerator> {

	public void defaultMutationVisiting(Node node, MutantsGenerator mGen) {
		
		//check whether expr is a change point
		List<MutationOperator> mutOps = mGen.checkChangePoint(node);

		//keep visiting
		Method visitMethod = null;
		try {
//			visitMethod = super.getClass().getDeclaredMethod("visit", node.getClass(), Object.class);
			visitMethod = this.getClass().getSuperclass().getDeclaredMethod("visit", node.getClass(), Object.class);
			System.out.println();
			System.out.println(this.getClass().getGenericSuperclass());
			System.out.println(this.getClass().getSuperclass());
			System.out.println(super.getClass());

//			System.out.println();
//			for (Method m : this.getClass().getSuperclass().getMethods()) {
//				System.out.println(m);
//			}
			System.out.println(visitMethod);
			
//			visitMethod.invoke(super.getClass().newInstance(), node.getClass().cast(node), mGen);

		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
		}

		node.getClass().cast(node);

		//mutate
		mGen.generateMutants(node, mutOps);
	}
	
	@Override
	public void visit(BinaryExpr expr, MutantsGenerator mGen) {
//		//check whether expr is a change point
//		List<MutationOperator> mutOps = mGen.checkChangePoint(expr);
//		//keep visiting
//		super.visit(expr, mGen);
//		//mutate
//		mGen.generateMutants(expr, mutOps);
		this.defaultMutationVisiting(expr, mGen);
	}

	@Override
	public void visit(ExpressionStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		mGen.generateMutants(stmt, mutOps);
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
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(ForStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(ForeachStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(DoStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		mGen.generateMutants(stmt, mutOps);
	}

	public void visit(SwitchStmt stmt, MutantsGenerator mGen) {
		List<MutationOperator> mutOps = mGen.checkChangePoint(stmt);
		super.visit(stmt, mGen);
		mGen.generateMutants(stmt, mutOps);
	}
}
