package br.ufmg.labsoft.mutvariants.mutops;

import com.github.javaparser.ast.Node;

import br.ufmg.labsoft.mutvariants.core.MutantsGenerator;

public interface MutationOperator {

	public boolean isChangePoint(Node node, MutantsGenerator mGen);

	public Node generateMutants(Node original, MutantsGenerator mGen);
	
	public String getName();
}
