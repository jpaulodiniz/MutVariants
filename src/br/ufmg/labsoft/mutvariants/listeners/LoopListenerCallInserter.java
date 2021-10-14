package br.ufmg.labsoft.mutvariants.listeners;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;

/**
 * Issue #5
 */
public class LoopListenerCallInserter {
	public long loopSeq = 0L;

	public void generateInstrumentationInLoop(NodeWithBody<?> stmt) {
		/*
		 * keep this order (insert listener call, insert variable initialization, increment id)
		 * due to the process of visiting the AST and modifying deeper nodes first 
		 */
		this.insertListenerCallInLoopBody(stmt.getBody());
		this.insertLoopCounterInitializationBeforeLoop((Statement)stmt);
		this.incrementLoopCounterId();
	}

	private void insertLoopCounterInitializationBeforeLoop(Statement stmt) {
		AssignExpr initLoopCounterExpr = this.generateLoopCounterAssignmentExpr();

		if (stmt instanceof ForStmt) {
			ForStmt forStmt = (ForStmt)stmt;
			forStmt.getInitialization().addLast(initLoopCounterExpr);
		}
		else {
			BlockStmt newParent = new BlockStmt();
			Statement clone = stmt.clone();
			newParent.addStatement(initLoopCounterExpr);
			newParent.addStatement(clone);
			stmt.replace(newParent);
		}
	}

	private void insertListenerCallInLoopBody(Statement stmtBody) {
		if (stmtBody instanceof BlockStmt) {
			insertListenerCallInBlockStatement((BlockStmt)stmtBody);
		}
		else { //loop with just one statement, without a block { ... }
			BlockStmt newBlock = new BlockStmt(new NodeList<Statement>(stmtBody.clone()));
			stmtBody.replace(newBlock); //keep replacing before inserting listener call
			insertListenerCallInBlockStatement(newBlock);
		}
	}

	private void insertListenerCallInBlockStatement(BlockStmt blockStmt) {
		MethodCallExpr listenerCall = this.generateListenerCallExprForLoopCounter();
		blockStmt.addStatement(0, listenerCall);
	}

	/**
	 * insert <code>listener.listen('loopID', count)</code> as the first statement
	 */
	private MethodCallExpr generateListenerCallExprForLoopCounter() {
		//TODO dinamically retrieve the listener object instantiated in this package
		MethodCallExpr listenerCall = new MethodCallExpr(new NameExpr("ListenerUtil.listener"),
				"listen",
				NodeList.nodeList(new StringLiteralExpr(this.currentLoopCounterId()), 
						this.generateLoopCounterIncrementExpr()));
		return listenerCall;
	}

	private AssignExpr generateLoopCounterAssignmentExpr() {
		String loopId = currentLoopCounterId();
		return new AssignExpr(new NameExpr(loopId),
				new LongLiteralExpr(0L), AssignExpr.Operator.ASSIGN);
	}

	private UnaryExpr generateLoopCounterIncrementExpr() {
		String loopId = currentLoopCounterId();
		return new UnaryExpr(new NameExpr(loopId),
				UnaryExpr.Operator.PREFIX_INCREMENT);
	}

	private String currentLoopCounterId() {
		return "_loopCounter" + this.loopSeq;		
	}

	private void incrementLoopCounterId() {
		this.loopSeq++;
	}
}
