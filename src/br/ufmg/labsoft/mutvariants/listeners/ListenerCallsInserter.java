package br.ufmg.labsoft.mutvariants.listeners;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.Statement;

import br.ufmg.labsoft.mutvariants.core.MutantsGenerator;

/**
 * Issue #5
 */
public class ListenerCallsInserter {

	/**
	 * @param stmtBody
	 * @param mGen
	 */
	public static void insertListenerCallInLoopBody(Statement stmtBody, MutantsGenerator mGen) {

		if (stmtBody instanceof BlockStmt) {
			insertListenerCallInBlockStatement((BlockStmt)stmtBody, mGen, true);
		}
		else { //loop with just one statement, without {}
			BlockStmt newBlock = new BlockStmt(new NodeList<Statement>(stmtBody.clone()));
			stmtBody.replace(newBlock); //keep replacing before inserting listener call
			insertListenerCallInBlockStatement(newBlock, mGen, true);
		}
	}

	/**
	 * @param methodDecl
	 * @param mGen
	 * @param mutCountBefore
	 * @param mutCountAfter
	 */
	public static void insertListenerCallInMethodBody(MethodDeclaration methodDecl, MutantsGenerator mGen,
			long mutCountBefore, long mutCountAfter) {

		if (mutCountAfter > mutCountBefore) { //mGen.generateListener() &&
			insertListenerCallInBlockStatement(methodDecl.getBody().get(), mGen);
		}
	}

	/**
	 * @param constructorDecl
	 * @param mGen
	 * @param mutCountBefore
	 * @param mutCountAfter
	 */
	public static void insertListenerCallInConstructorBody(ConstructorDeclaration constructorDecl,
			MutantsGenerator mGen, long mutCountBefore, long mutCountAfter) {

		if (mutCountAfter > mutCountBefore) {

			BlockStmt constrBody = constructorDecl.getBody();

			if (constrBody.getStatement(0) instanceof ExplicitConstructorInvocationStmt) {
				insertListenerCallInBlockStatement(constructorDecl.getBody(), 1, mGen);
			}
			else {
				insertListenerCallInBlockStatement(constructorDecl.getBody(), mGen);
			}
		}
	}

	private static void insertListenerCallInBlockStatement(BlockStmt blockStmt,
			MutantsGenerator mGen) {

		insertListenerCallInBlockStatement(blockStmt, 0, mGen, false);
	}

	private static void insertListenerCallInBlockStatement(BlockStmt blockStmt,
			int position, MutantsGenerator mGen) {

		insertListenerCallInBlockStatement(blockStmt, position, mGen, false);
	}

	private static void insertListenerCallInBlockStatement(BlockStmt blockStmt,
			MutantsGenerator mGen, boolean identifyBlockLine) {

		insertListenerCallInBlockStatement(blockStmt, 0, mGen, identifyBlockLine);
	}

	/**
	 * insert <code>listener.listen('methodID[stmtLine]')</code> as the first statement
	 * @param blockStmt
	 * @param position
	 * @param mGen
	 * @param identifyParentLine useful for different loops in the same method
	 */
	private static void insertListenerCallInBlockStatement(BlockStmt blockStmt,
			int position, MutantsGenerator mGen, boolean identifyParentLine) {

		String listenerId = mGen.currentClassFQN + "." + mGen.currentOperation;
		if (identifyParentLine) {
			int parentStmtLine = blockStmt.getParentNode().get().getBegin().get().line;
			listenerId += "_" + parentStmtLine;
		}

		//TODO dinamically retrieve the listener object instantiated in this package
		MethodCallExpr listenerCall = new MethodCallExpr(new NameExpr("ListenerUtil.listener"),
				"listen",
				NodeList.nodeList(new StringLiteralExpr(listenerId)));

		blockStmt.addStatement(position, listenerCall);
	}
}