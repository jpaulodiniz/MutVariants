package br.ufmg.labsoft.mutvariants;

import java.io.File;
import java.util.EnumSet;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import br.ufmg.labsoft.mutvariants.mutants.AllBinaryExprMutationStrategy;
import br.ufmg.labsoft.mutvariants.mutants.MutantsGenerator;
import br.ufmg.labsoft.mutvariants.mutants.MutationStrategy;
import br.ufmg.labsoft.mutvariants.util.CompilationUnitSamples;
import br.ufmg.labsoft.mutvariants.util.IO;

public class Main {
	
	private static String baseDir = "/home/.../Desktop/"; //TODO change path

	public static void main(String[] args) {
		
		MutationStrategy mutStrategy = new AllBinaryExprMutationStrategy(); //or OneBinaryExprPerStatementMutationStrategy 

		CombinedTypeSolver typeSolvers = new CombinedTypeSolver();
		typeSolvers.add(new ReflectionTypeSolver());
		typeSolvers.add(new JavaParserTypeSolver(baseDir + "systems/original/.../src/main/java")); //TODO change path
//		try {
//			typeSolvers.add(new JarTypeSolver(rootDir + "systems/commons-cli-1.4/commons-cli-1.4.jar")); //TODO change paths
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		MutantsGenerator mg = new MutantsGenerator(mutStrategy, typeSolvers);
		mg.mutatePackageOrDirectory(baseDir + "systems/original/.../src/main/java", //TODO change path	
				baseDir + "systems/mutated/main/v04"); //TODO change path
//		mutateOneClass(mg);
	}

	@Deprecated
	public static void mutateOneClass(MutantsGenerator mg) {

//		testMutatingJavaOperators();

//		CompilationUnit original = tryMutationFromSample();
		CompilationUnit original = tryMutationFromFile();

		//MUTANTS GENERATION
		CompilationUnit mutated = mg.generateMutants(original);

		//prompt output
		System.out.println();
		System.out.println(" *** ORIGINAL ***\n\n" + original);
		System.out.println("\n *** MUTATED ***\n\n" + mutated);

		File outputDirectory = new File(baseDir + "systems/mutated");
		IO.writeCompilationUnit(mutated, outputDirectory); //, inputFile.getName()
	}

	@Deprecated
	private static CompilationUnit tryMutationFromSample() {

		return CompilationUnitSamples.createCompilationUnit();
//		return CompilationUnitSamples.createCompilationUnit2();
	}

	@Deprecated
	private static CompilationUnit tryMutationFromFile() {
		File inputFile = new File(baseDir + "systems/original/.../.java");

		return IO.getCompilationUnitFromFile(inputFile);
	}

	public static void testMutatingJavaOperators() {

		main_loop:
		for (Operator op : EnumSet.allOf(Operator.class)) {
			System.out.println();
			for (int i = 0; i < 3; ++i) {
				try {
					System.out.println(op + " into " + MutantsGenerator.mutatedOperator(op, false));
				} catch (Exception e) {
					System.err.println(e.getMessage());
					continue main_loop;
				}
			}
		}
	}
}
