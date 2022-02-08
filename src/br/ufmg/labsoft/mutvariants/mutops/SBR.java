package br.ufmg.labsoft.mutvariants.mutops;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
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
import com.github.javaparser.ast.stmt.LabeledStmt;
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

	private static Set<Class<?>> loopStatements = Sets.newHashSet(WhileStmt.class, ForStmt.class,
			ForeachStmt.class, DoStmt.class);
	private static Set<Class<?>> blockStatementsToRemove = 
			Sets.newHashSet(IfStmt.class, WhileStmt.class, ForStmt.class, 
					ForeachStmt.class, DoStmt.class, SwitchStmt.class, LabeledStmt.class);

	private Stack<Integer> currentMutantIds = new Stack<>();

	@Override
	public boolean isChangePoint(Node node, MutantsGenerator mGen) {

		if (node instanceof Statement && 
				/*
				 * node is a statement in 'blockStatementsToRemove' OR
				 * is not a variable declaration statement
				 */
				(blockStatementsToRemove.contains(node.getClass()) ||
						(node instanceof ExpressionStmt && 
								!(ExpressionStmt.class.cast(node).getExpression() 
										instanceof VariableDeclarationExpr)) ) ) {

			/*
			 * loops inside a labeled statement are not removed
			 * jsoup has a labeled loop (OUTER) with a continue statement inside it
			 */
			if (loopStatements.contains(node.getClass()) &&
					node.getParentNode().get().getClass().equals(LabeledStmt.class)) {
				return false;
			}

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
			if (!mGen.blockVariablesNonInitialized.isEmpty()) {
				List<AssignExpr> assignExprs = node.findAll(AssignExpr.class).stream()
						.filter(ae -> ae.getOperator().equals(AssignExpr.Operator.ASSIGN)
								&& ae.getTarget().isNameExpr())
						.collect(Collectors.toList());
				
				for (AssignExpr ae : assignExprs) {
					if (ae.getTarget().isNameExpr()) {
						NameExpr nameExpr = (NameExpr)ae.getTarget();
						
						for (int i=mGen.blockVariablesNonInitialized.size() - 1; i >= 0; --i) {
							Set<NameExpr> nameExprs = mGen.blockVariablesNonInitialized.get(i);

							if (nameExprs != null && nameExprs.contains(nameExpr)) {
								/*
								 * handling 2 cases found in commons-cli:
								 * "int i; for (i=...)"
								 * "String line; while ((line = in.readLine()) != null))"
								 */
								Optional<Statement> ancestor = ae.findAncestor(Statement.class);
								if (ancestor.isPresent()) {
									Statement parentStatement = ancestor.get();
									if (!(parentStatement instanceof WhileStmt) &&
											!(parentStatement instanceof ForStmt)) {
//										System.out.println("DEBUG: FOR/WHILE\n" + ae);
										return false;
									}
								}
							}
						}
					}
				}
			}

			this.currentMutantIds.push(mGen.lastMutantGenerated());
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
		Expression mutAccessExpr = mGen.nextMutantAccessExpression();
		
		//generating !mutAccessExpr
		UnaryExpr mutExpr = new UnaryExpr(mutAccessExpr,
				UnaryExpr.Operator.LOGICAL_COMPLEMENT);

		//generating if (!mutAccessExpr) { <original_statement> }
		IfStmt mutIfStmt = new IfStmt(mutExpr, 
				new BlockStmt(NodeList.nodeList(originalStmt)), null);

		//generation mutant information for mutants catalog
		MutationInfo mInfo = new MutationInfo();
		mInfo.setMutationOperator(this.getName());
		mInfo.setMutantId(mGen.currentMutantId());
		mInfo.setInfoBeforeMutation(this.extractStatementClassName(stmt));
		mInfo.setInfoAfterMutation("removal");
		mInfo.setMutatedClass(mGen.currentClassFQN);
		mInfo.setMutatedOperation(mGen.currentOperation);
		mGen.addMutantInfoToCatalog(mInfo);

//		Set<String> nested = this.findNestedMutantNames(stmt);
//		if (!nested.isEmpty()) {
//			mGen.addNestedMutantsInfo(mGen.currentMutantId(), nested);
//		}

		int currentMutant = mGen.lastMutantGenerated();
		int firstNested = this.currentMutantIds.pop() + 1;
		if (currentMutant > firstNested) {
			int lastNested = currentMutant - 1;
			mGen.addNestedMutantsInfo(currentMutant, firstNested, lastNested);
		}

		/*
		 * Handling code like
		 *  if (cond) expr1;
		 *  else expr2;
		 * to prevent the mutant if to take the else for it.
		 * The correct mutated statement:
		 *  if (cond)
		 *  {
		 *    if (!mutId) { expr1; }
		 *  }
		 *  else expr2;
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
	 */
	@Deprecated
	private Set<String> findNestedMutantNames(Statement stmt) {
		return stmt.findAll(NameExpr.class).stream()
				.filter(x -> x.getNameAsString().startsWith(Constants.MUTANT_VARIABLE_PREFIX2))
				.map(x -> x.toString()).collect(Collectors.toSet());
	}

	private String extractStatementClassName(Statement stmt) {
		String className = stmt.getClass().getName();
		int idx = className.lastIndexOf('.') + 1;
		return className.substring(idx);
	}
}
