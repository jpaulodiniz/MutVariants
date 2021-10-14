package br.ufmg.labsoft.mutvariants.listeners;

/**
 * Issue #5
 */
public interface IMutatedCodeListener {

	void listen(String operation);
	void listen(String loopVar, long count);
}