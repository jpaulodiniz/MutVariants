package br.ufmg.labsoft.mutvariants.mutops;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
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

		if (node instanceof Statement && 
				//node is a block in blockStatementsToRemove OR is not a variable declaration
				(blockStatementsToRemove.contains(node.getClass()) ||
						(node instanceof ExpressionStmt && 
								!(ExpressionStmt.class.cast(node).getExpression() 
										instanceof VariableDeclarationExpr)) ) ) {
				
			// nested 'else if' blocks are not removed
			if (node instanceof IfStmt &&
					node.getParentNode().get() instanceof IfStmt) {
				return false;
			}

			/*
			 * blocks containing return statements are removed only
			 * if the method has direct return statement as child
			 */
			if (mGen.currentOperation != null && // not a static block, etc...
					mGen.currentOperation.endsWith("__nrs") &&
					blockStatementsToRemove.contains(node.getClass()) &&
					node.findFirst(ReturnStmt.class).isPresent()) {
				return false;
			}

			/*
			 * statements/blocks containing the initialization 
			 * of a final attribute are not removed 
			 */
			if (mGen.classFinalAttributesNonInitialized != null) {
				List<AssignExpr> assignExprs = node.findAll(AssignExpr.class).stream()
						.filter(ae -> ae.getOperator().equals(AssignExpr.Operator.ASSIGN)
								//with or without 'this.'
								&& (ae.getTarget().isNameExpr() || ae.getTarget().isFieldAccessExpr()))
						.collect(Collectors.toList());
				
				for (AssignExpr ae : assignExprs) {
					NameExpr nameExpr = null;
					if (ae.getTarget().isNameExpr()) {
						nameExpr = (NameExpr)ae.getTarget();
					}
					else if (ae.getTarget().isFieldAccessExpr()) { // this.
						nameExpr = ((FieldAccessExpr)ae.getTarget()).getNameAsExpression();
					}
					
					if (nameExpr != null &&
							mGen.classFinalAttributesNonInitialized.contains(nameExpr)) {
						return false;
					}
				}
			}
			
			/*
			 * statements/blocks containing the initialization 
			 * of a non initialized variable are not removed 
			 */
			if (mGen.methodVariablesNonInitialized != null) {
				List<AssignExpr> assignExprs = node.findAll(AssignExpr.class).stream()
						.filter(ae -> ae.getOperator().equals(AssignExpr.Operator.ASSIGN)
								&& ae.getTarget().isNameExpr())
						.collect(Collectors.toList());
				
				for (AssignExpr ae : assignExprs) {
					if (ae.getTarget().isNameExpr()) {
						NameExpr nameExpr = (NameExpr)ae.getTarget();
						if (mGen.methodVariablesNonInitialized.contains(nameExpr)) {
							// handling a 'commons-cli' case: "int i; for (i=...)"
							boolean forInitExprs = ae.getParentNode().isPresent()
									&& ae.getParentNode().get() instanceof ForStmt
									&& ((ForStmt)ae.getParentNode().get()).getInitialization().contains(ae);
//							System.out.println("DEBUG: " + forInitExprs + "\n" + ae);
							if (!forInitExprs) {
								return false;
							}
						}
					}
				}
			}

			return true;
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
	 * @return IfStatement or BlockStatement 
	 */
	private Statement generateMutants(Statement stmt, MutantsGenerator mGen) {

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
		mInfo.setMutatedOperation(mGen.currentOperation);
		mGen.addMutantInfoToCatalog(mInfo);
		
		Set<String> nested = this.findNestedMutantNames(stmt);
		if (!nested.isEmpty()) {
			mGen.addNestedMutantsInfo(mutantVariableName, nested);
		}

		/*
		 * handling issues with code like
		 * if (cond) expr1; 
		 * else expr2;
		 */
		if (stmt.getParentNode().get() instanceof IfStmt) {
			IfStmt parent = (IfStmt)stmt.getParentNode().get();
			if (parent.getElseStmt().isPresent()) {
				return new BlockStmt(NodeList.nodeList(mutIfStmt));
			}
		}

		return mutIfStmt;
	}
	
	/**
	 * nested mutant variable names nested in current block 'being removed'
	 * @param stmt
	 * @return
	 */
	private Set<String> findNestedMutantNames(Statement stmt) {
		return stmt.findAll(NameExpr.class).stream()
				.filter( x -> x.getNameAsString().startsWith(Constants.MUTANT_VARIABLE_PREFIX2 ))
				.map(x -> x.toString()).collect(Collectors.toSet());
	}

	private String extractStatementClassName(Statement stmt) {
		
		String className = stmt.getClass().getName();
		int idx = className.lastIndexOf('.') + 1;
		return className.substring(idx);
	}
}
