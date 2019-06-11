package br.ufmg.labsoft.mutvariants.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import br.ufmg.labsoft.mutvariants.mutops.AOR;
import br.ufmg.labsoft.mutvariants.mutops.LCR;
import br.ufmg.labsoft.mutvariants.mutops.MutationOperator;
import br.ufmg.labsoft.mutvariants.mutops.ROR;
import br.ufmg.labsoft.mutvariants.mutops.SBR;
import br.ufmg.labsoft.mutvariants.util.CompilationUnitSamples;
import br.ufmg.labsoft.mutvariants.util.IO;

public class Main {
	
	public static void main(String[] args) {
		
		//TODO change path and/or properties name
		Properties ioConf = IO.loadProperties("resources/triangle.properties");
//		Properties ioConf = IO.loadProperties("resources/monopoly.properties");
//		Properties ioConf = IO.loadProperties("resources/commons-cli.properties");
//		Properties ioConf = IO.loadProperties("resources/commons-validator.properties");

		String baseDir = ioConf.getProperty("base-dir");
		String inputDir = baseDir + ioConf.getProperty("input-dir");
		String outputDir = baseDir + ioConf.getProperty("output-dir-mut-schemata");

		CombinedTypeSolver typeSolvers = new CombinedTypeSolver();
		typeSolvers.add(new ReflectionTypeSolver());
		typeSolvers.add(new JavaParserTypeSolver(inputDir));

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

//		MutantsGenerator mg = new MutGenClassical(baseDir + ioConf.getProperty("output-dir-mut-classical"));
		MutantsGenerator mg = new MutantsGenerator();
		
		List<MutationOperator> mutationOperators = new ArrayList<>();
		mutationOperators.add(new AOR());
		mutationOperators.add(new ROR());
		mutationOperators.add(new LCR());
		mutationOperators.add(new SBR());
		mg.addMutationOperators(mutationOperators);

		mg.setAllPossibleMutationsPerChangePoint(true);
		mg.setMutateLoopConditions(false);
		mg.setMutationRate(1d);
		mg.setTypeSolver(typeSolvers);

		mg.mutatePackageOrDirectory(inputDir, outputDir);

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
}
