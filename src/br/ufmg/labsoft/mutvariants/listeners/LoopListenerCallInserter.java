package br.ufmg.labsoft.mutvariants.listeners;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;

/**
 * Issue #5 - improvement
 */
public class LoopListenerCallInserter {
	private long loopSeq = 0L;

	public long getLoopSeq() {
		return this.loopSeq;
	}

	private String currentLoopCounterId() {
		return "_loopCounter" + this.loopSeq;
	}

	private void incrementLoopCounterId() {
		this.loopSeq++;
	}

	public void generateInstrumentationInLoop(NodeWithBody<?> stmt) {
		this.insertListenerCallInLoopBody(stmt.getBody());

		if (!((Statement)stmt).getParentNode().get().getClass().equals(LabeledStmt.class)) {
			this.insertLoopCounterInitializationBeforeLoop((Statement)stmt);
			this.incrementLoopCounterId();
		}
	}

	public void generateInstrumentationInLabeledStatement(LabeledStmt stmt) {
		this.insertLoopCounterInitializationBeforeLoop(stmt);
		this.incrementLoopCounterId();
	}

	private void insertLoopCounterInitializationBeforeLoop(Statement stmt) {
		VariableDeclarationExpr initLoopCounterExpr = this.generateLoopCounterAssignmentExpr();

		BlockStmt newParent = new BlockStmt();
		Statement clone = stmt.clone();
		newParent.addStatement(initLoopCounterExpr);
		newParent.addStatement(clone);
		stmt.replace(newParent);
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
		MethodCallExpr listenerCall = new MethodCallExpr(
				new FieldAccessExpr(new NameExpr(ListenerUtil.class.getSimpleName()), "loopListener"),
				"listen",
				NodeList.nodeList(new StringLiteralExpr(this.currentLoopCounterId()),
						this.generateLoopCounterIncrementExpr()));
		return listenerCall;
	}

	private VariableDeclarationExpr generateLoopCounterAssignmentExpr() {
		String loopId = currentLoopCounterId();
		VariableDeclarator vd = new VariableDeclarator(new PrimitiveType(Primitive.LONG), 
				loopId, new LongLiteralExpr(0L));
		return new VariableDeclarationExpr(vd);
	}

	private UnaryExpr generateLoopCounterIncrementExpr() {
		String loopId = currentLoopCounterId();
		return new UnaryExpr(new NameExpr(loopId),
				UnaryExpr.Operator.PREFIX_INCREMENT);
	}
}
