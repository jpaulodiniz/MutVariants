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

import br.ufmg.labsoft.mutvariants.mutants.MutantsGenerator;

/**
 * Issue #5
 */
public class ListenerUtil {

	/**
	 * a call to listen will be included in all places defined in Issue #5
	 */
	public static IMutatedCodeListener listener = new IMutatedCodeListener() {
		@Override
		public void listen(String operation) {
			// may be overridden by a 'client'
		}
	};

	/**
	 * @param stmt
	 * @param mGen
	 * @param mutCountBefore
	 * @param mutCountAfter
	 */
	public static void insertListenerCallInLoopBody(Statement stmtBody, MutantsGenerator mGen) {

		if (stmtBody instanceof BlockStmt) {
			insertListenerCallInBlockStatement((BlockStmt)stmtBody, mGen);
		}
		else { //loop with just one statement, without {}
			BlockStmt newBlock = new BlockStmt(new NodeList<Statement>(stmtBody.clone()));
			insertListenerCallInBlockStatement(newBlock, mGen);
			stmtBody.replace(newBlock);
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

	/**
	 * insert <code>listener.listen('methodID')</code> as the first statement
	 * @param blockStmt
	 * @param mGen
	 */
	private static void insertListenerCallInBlockStatement(BlockStmt blockStmt, MutantsGenerator mGen) {

		insertListenerCallInBlockStatement(blockStmt, 0, mGen);
	}
	/**
	 * insert <code>listener.listen('methodID')</code> as the first statement
	 * @param blockStmt
	 * @param mGen
	 */

	private static void insertListenerCallInBlockStatement(BlockStmt blockStmt, int position, MutantsGenerator mGen) {

		//TODO dinamically retrieve the listener object instantiated in this class
		MethodCallExpr listenerCall = new MethodCallExpr(new NameExpr("br.ufmg.labsoft.mutvariants.listeners.ListenerUtil.listener"), 
				"listen", 
				NodeList.nodeList(new StringLiteralExpr(mGen.currentClassFQN + "." + mGen.currentOperation)));

		blockStmt.addStatement(position, listenerCall);
	}
}
