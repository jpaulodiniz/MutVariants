package br.ufmg.labsoft.mutvariants.mutops;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.google.common.collect.Sets;

import br.ufmg.labsoft.mutvariants.core.MutantsGenerator;
import br.ufmg.labsoft.mutvariants.entity.MutationInfo;
import br.ufmg.labsoft.mutvariants.util.Constants;

/*
 * http://www.javadoc.io/doc/com.github.javaparser/javaparser-core/3.9.0
 * package com.github.javaparser.ast.stmt
 * https://github.com/javaparser/javaparser/blob/master/javaparser-core/src/main/java/com/github/javaparser/ast/visitor/VoidVisitor.java
 * 23 statements
 * Don't mutate:
 * empty, 
 */
public class SBR implements MutationOperator {

	private static Set<Class<?>> blockStatementsToRemove = 
			Sets.newHashSet(IfStmt.class, WhileStmt.class, ForStmt.class, 
					ForeachStmt.class, DoStmt.class, SwitchStmt.class);

	@Override
	public boolean isChangePoint(Node node, MutantsGenerator mGen) {

		if (node instanceof Statement) {

			if (blockStatementsToRemove.contains(node.getClass()) || 
					(node instanceof ExpressionStmt && 
							!(ExpressionStmt.class.cast(node).getExpression() 
									instanceof VariableDeclarationExpr)) ) {
				
				// nested 'else if' blocks are not mutated
				if (node instanceof IfStmt &&
						node.getParentNode().get() instanceof IfStmt) {
					return false;
				}
				
				return true;
			}
		}

		return false;
	}

	@Override
	public Node generateMutants(Node node, MutantsGenerator mGen) {

		if (node instanceof Statement) {
			return this.generateMutants((Statement)node, mGen);
		}
		else {
			return node;
		}
	}

	/**
	 * 
	 * @param stmt
	 * @param mGen
	 * @return
	 */
	private IfStmt generateMutants(Statement stmt, MutantsGenerator mGen) {

		Statement originalStmt = stmt.clone(); 
		String mutantVariableName = mGen.nextMutantVariableName();

		//!_mut#
		UnaryExpr mutExpr = new UnaryExpr(new NameExpr(mutantVariableName), 
				UnaryExpr.Operator.LOGICAL_COMPLEMENT);

		//if (!_mut#) { <original_statement> }
		IfStmt mutIfStmt = new IfStmt(mutExpr, 
				new BlockStmt(NodeList.nodeList(originalStmt)), null);

		//generation mutant information for mutants catalog
		MutationInfo mInfo = new MutationInfo();
		mInfo.setMutationOperator(this.getName());
		mInfo.setMutantVariableName(mutantVariableName);
		mInfo.setInfoBeforeMutation(this.extractStatementClassName(stmt));
		mInfo.setInfoAfterMutation("removal");
		mInfo.setMutatedClass(mGen.currentClassFQN);
		mInfo.setMutatedMethod(mGen.currentMethod);
		mGen.addMutantInfoToCatalog(mInfo);
		
		List<String> nested = this.findNestedMutantNames(stmt);
		if (!nested.isEmpty()) {
			mGen.addNestedMutantsInfo(mutantVariableName, nested);
		}

		return mutIfStmt;
	}
	
	/**
	 * nested mutant variable names nested in current block 'being removed'
	 * @param stmt
	 * @return
	 */
	private List<String> findNestedMutantNames(Statement stmt) {
		return stmt.findAll(NameExpr.class).stream()
				.filter( x -> x.getNameAsString().startsWith(Constants.MUTANT_VARIABLE_PREFIX2 ))
				.map(x -> x.toString()).collect(Collectors.toList());
	}

	private String extractStatementClassName(Statement stmt) {
		
		String className = stmt.getClass().getName();
		int idx = className.lastIndexOf('.') + 1;
		return className.substring(idx);
	}

	@Override
	public String getName() {
		return "SBR";
	}
}
