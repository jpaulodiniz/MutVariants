package br.ufmg.labsoft.mutvariants.mutants;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * 
 * @author jpaulo
 *
 */
public interface MutationStrategy {

//	public void generateMutants(ClassOrInterfaceDeclaration aClass);
	public void generateMutants(ClassOrInterfaceDeclaration aClass, MutantsGenerator mutGen);
}
