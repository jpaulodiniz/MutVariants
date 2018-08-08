package br.ufmg.labsoft.mutvariants;

import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import br.ufmg.labsoft.mutvariants.mutants.AllBinaryExprMutationStrategy;
import br.ufmg.labsoft.mutvariants.mutants.MutantsGenerator;
import br.ufmg.labsoft.mutvariants.mutants.MutationStrategy;
import br.ufmg.labsoft.mutvariants.util.CompilationUnitSamples;
import br.ufmg.labsoft.mutvariants.util.IO;

public class Main {
	
	public static void main(String[] args) {
		
		//TODO change path and/or properties name
		Properties ioConf = IO.loadProperties("resources/sample.properties");
		String baseDir = ioConf.getProperty("base-dir");
		String inputDir = ioConf.getProperty("input-dir");
		String outputDir = ioConf.getProperty("output-dir-mut-schemata");

		CombinedTypeSolver typeSolvers = new CombinedTypeSolver();
		typeSolvers.add(new ReflectionTypeSolver());
		typeSolvers.add(new JavaParserTypeSolver(baseDir + inputDir));

		String jarsStr = ioConf.getProperty("dependent-jars-full-path", "");
		if (!jarsStr.isEmpty()) {
			String[] jars = jarsStr.split(",");
			try {
				for (String jar : jars) {
					typeSolvers.add(new JarTypeSolver(jar));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		MutationStrategy mutStrategy = new AllBinaryExprMutationStrategy();
//		MutationStrategy mutStrategy = new OneBinaryExprPerStatementMutationStrategy(); 
//		MutationStrategy mutStrategy = new OneNameExprPerStatementMutStr();

//		MutantsGenerator mg = new MutGenClassic();
		MutantsGenerator mg = new MutantsGenerator();
		mg.setAllPossibleMutationsPerSpot(true);
		mg.setMutateLoopConditions(false);
		mg.setMutationRate(1d);
		mg.setMutStrategy(mutStrategy);
		mg.setTypeSolver(typeSolvers);
		
		mg.mutatePackageOrDirectory(baseDir + inputDir, baseDir + outputDir);

//		testMutateOneClass(mg);
	}

	@Deprecated
	public static void testMutateOneClass(MutantsGenerator mg) {

//		testMutatingJavaOperators();

		CompilationUnit original = CompilationUnitSamples.createCompilationUnit();

		//MUTANTS GENERATION
		CompilationUnit mutated = mg.generateMutants(original);

		//prompt output
		System.out.println();
		System.out.println(" *** ORIGINAL ***\n\n" + original);
		System.out.println("\n *** MUTATED ***\n\n" + mutated);
	}

	public static void testMutatingJavaOperators() {

		main_loop:
		for (Operator op : EnumSet.allOf(Operator.class)) {
			System.out.println();
			for (int i = 0; i < 3; ++i) {
				try {
					System.out.println(op + " into " + MutantsGenerator.operatorForMutation(op, false));
				} catch (Exception e) {
					System.err.println(e.getMessage());
					continue main_loop;
				}
			}
		}
	}
}
