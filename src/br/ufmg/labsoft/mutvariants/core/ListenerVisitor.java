package br.ufmg.labsoft.mutvariants.core;

import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import br.ufmg.labsoft.mutvariants.listeners.LoopListenerCallInserter;

class ListenerVisitor extends VoidVisitorAdapter<LoopListenerCallInserter> {
	public void visit(WhileStmt stmt, LoopListenerCallInserter lci) {
		super.visit(stmt, lci);
		lci.generateInstrumentationInLoop(stmt);
	}

	public void visit(ForStmt stmt, LoopListenerCallInserter lci) {
		super.visit(stmt, lci);
		lci.generateInstrumentationInLoop(stmt);		
	}

	public void visit(ForeachStmt stmt, LoopListenerCallInserter lci) {
		super.visit(stmt, lci);
		lci.generateInstrumentationInLoop(stmt);
	}

	public void visit(DoStmt stmt, LoopListenerCallInserter lci) {
		super.visit(stmt, lci);
		lci.generateInstrumentationInLoop(stmt);
	}
}
