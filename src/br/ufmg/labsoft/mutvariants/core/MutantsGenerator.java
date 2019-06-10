package br.ufmg.labsoft.mutvariants.core;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import br.ufmg.labsoft.mutvariants.entity.MutationInfo;
import br.ufmg.labsoft.mutvariants.mutops.MutationOperator;
import br.ufmg.labsoft.mutvariants.util.Constants;
import br.ufmg.labsoft.mutvariants.util.IO;

/*
 * TODO(s)
 * - start mutantCounter with 1 (current is 0)
 * - verify if Java bitwise operators could be mutated
 * - seek for interfaces with default methods and check whether there was something to mutate
 * - don't mutate main methods
 * - [+-] don't mutate Exception classes
 */

/**
 *
 * @author jpaulo
 */
public class MutantsGenerator {

	private Set<MutationOperator> mutationOperators;
	private MutationVisitor mutationVisitor;

	private List<MutationInfo> mutantsCatalog; //Issue #3
	private List<List<String>> groupsOfMutants; //Issue #4

	private Map<String, List<String>> mutantsPerClass; //key: class FQN; value: list of mutants
	public String currentClassFQN; //helper field: current class fully qualified name (FQN)
	public String currentMethod; //helper field: Issue #3
	private long mutantsCounterGlobal; //helper field

	/**
	 * generates all possible mutants per change point
	 * E.g.: for a + b expression, apply all available mutations -, *, / and %
	 */
	private boolean allPossibleMutationsPerChangePoint = false;
	private boolean mutateLoopConditions = false;
	private double mutationRate = 1d; 
	private TypeSolver typeSolver;

	public MutantsGenerator() {
		this.mutationOperators = new HashSet<>();
		this.mutationVisitor = new MutationVisitor();
		this.mutantsPerClass = new HashMap<>();
		this.mutationOperators = new HashSet<>();
		this.mutantsCatalog = new ArrayList<>();
		this.groupsOfMutants = new ArrayList<>();
	}

	public boolean isAllPossibleMutationsPerChangePoint() {
		return allPossibleMutationsPerChangePoint;
	}

	public void setAllPossibleMutationsPerChangePoint(boolean allPossibleMutationsPerChangePoint) {
		this.allPossibleMutationsPerChangePoint = allPossibleMutationsPerChangePoint;
	}

	public boolean getMutateLoopConditions() {
		return mutateLoopConditions;
	}

	public void setMutateLoopConditions(boolean mutateLoopConditions) {
		this.mutateLoopConditions = mutateLoopConditions;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public TypeSolver getTypeSolver() {
		return typeSolver;
	}

	public void setTypeSolver(TypeSolver typeSolver) {
		this.typeSolver = typeSolver;
	}

	public void addMutationOperators(List<MutationOperator> mutOps) {
	
		if (mutOps != null && !mutOps.isEmpty()) {
			this.mutationOperators.addAll(mutOps);
		}
	}

	/**
	 * Issue #3
	 */
	public void addMutantInfoToCatalog(MutationInfo mutantInfo) {

		this.mutantsCatalog.add(mutantInfo);
	}

	/**
	 * Issue #4
	 */
	public void addGroupOfMutants(List<String> groupOfMutants) {

		this.groupsOfMutants.add(groupOfMutants);
	}

	/**
	 * @param original
	 * @return
	 */
	private void generateMutants(ClassOrInterfaceDeclaration mClass) {

		this.currentClassFQN = mClass.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString()
				+ '.' + mClass.getNameAsString();
		if (this.currentClassFQN.endsWith("Exception")) {
			return;
		}
	
		//strategy
//		this.getMutStrategy().generateMutants(mClass, this);
		mClass.accept(this.mutationVisitor, this);

		if (this.mutantsPerClass.get(this.currentClassFQN) != null &&
				!this.mutantsPerClass.get(this.currentClassFQN).isEmpty()) {

		//adding declarations for all conditional mutant variables
			NodeList<VariableDeclarator> variables = new NodeList<>();
			for (String mutName : this.mutantsPerClass.get(this.currentClassFQN)) {
				variables.add(new VariableDeclarator(new PrimitiveType(Primitive.BOOLEAN),
						mutName, new BooleanLiteralExpr(false)));
			}

			FieldDeclaration fieldDecl = new FieldDeclaration(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), variables);
			fieldDecl.addMarkerAnnotation(Constants.VAREXJ_CONDITIONAL_NAME);

//			NodeList<AnnotationExpr> annotations = new NodeList<>();
//			annotations.add(new MarkerAnnotationExpr(Constants.VAREXJ_CONDITIONAL_NAME));
//			FieldDeclaration fieldDecl2 = new FieldDeclaration(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
//					annotations, variables);

//			FieldDeclaration fieldDecl3 = c.addFieldWithInitializer(
//					new PrimitiveType(Primitive.BOOLEAN), Constants.MUTANT_VARIABLE_PREFIX, new BooleanLiteralExpr(false),
//					Modifier.PUBLIC, Modifier.STATIC);

			mClass.getMembers().add(0, fieldDecl);	
		}
	}

	/**
	 * @param original
	 * @return
	 */
	public CompilationUnit generateMutants(CompilationUnit original) {
	
		int mutantsCounterPerCompUn = 0;

		CompilationUnit mcu = original.clone(); //mcu: mutated compilation unit
		List<ClassOrInterfaceDeclaration> classes = mcu.findAll(ClassOrInterfaceDeclaration.class, 
				c -> !c.isInterface() && !c.isNestedType()); // && !c.isInnerClass() && !c.isStatic()

		//mutate each class in compilation unit
		for (ClassOrInterfaceDeclaration aClass : classes) {
			this.generateMutants(aClass);
			if (this.mutantsPerClass.get(this.currentClassFQN) != null) {
				mutantsCounterPerCompUn += this.mutantsPerClass.get(this.currentClassFQN).size();
			}
		}

		if (mutantsCounterPerCompUn > 0) {
			//add import Conditional
			mcu.addImport(Constants.VAREXJ_CONDITIONAL_FQN);

//			if (TODO) {
//				mcu.addImport(Constants.MUTANT_SCHEMATA_LIB, true, true);
//			}

			System.out.println(">>>> " + mutantsCounterPerCompUn + " mutants seeded (in Comp. Unit).");

			return mcu;
		}

		return original;
	}

	/**
	 * 
	 * @return
	 */
	public String nextMutantVariableName() {

		String mutantVariable = buildMutantVariableName(this.currentClassFQN, this.mutantsCounterGlobal++);
	
		if (this.mutantsPerClass.get(this.currentClassFQN) == null) {
			this.mutantsPerClass.put(this.currentClassFQN, new ArrayList<>());
		}
		this.mutantsPerClass.get(this.currentClassFQN).add(mutantVariable.toString());
	
		return mutantVariable;
	}

	private static String buildMutantVariableName(String classFQN, long mutSeq) {

		StringBuilder mutantName = new StringBuilder();
//		mutantName.append(Constants.MUTANT_VARIABLE_PREFIX1);
//		mutantName.append(simplify(classFQN));
		mutantName.append(Constants.MUTANT_VARIABLE_PREFIX2);
		mutantName.append(mutSeq);
	
		return mutantName.toString();
	}

	/**
	 *
	 * @param inputPath
	 */
	public void mutatePackageOrDirectory(String inputPath, String outputPath) {

		long ini = System.currentTimeMillis();
	
		this.mutantsCounterGlobal = 0;
		int countMutatedCompilationUnits = 0;

		for (File f : IO.allJavaFilesIn(inputPath)) {
			System.out.println(f);

			CompilationUnit original = IO.getCompilationUnitFromFile(f);
			CompilationUnit mutated = this.generateMutants(original);

			if (mutated.getImports().stream().anyMatch(i -> i.getNameAsString().startsWith(Constants.VAREXJ_CONDITIONAL_FQN))) {
				++countMutatedCompilationUnits;
			}

			IO.writeCompilationUnit(mutated, new File(outputPath));
		}

		long fin = System.currentTimeMillis();
	
		System.out.print("\n>>>>> " + this.mutantsCounterGlobal + " mutants seeded");
		System.out.print(" in " + countMutatedCompilationUnits + " mutated compilation units");
		System.out.println(", in " + (fin - ini) + "ms");
	
		IO.saveMutantsCatalog(outputPath, Constants.MUT_CATALOG_FILE_NAME, this.mutantsCatalog);
		IO.saveGroupsOfMutants(outputPath, Constants.GROUPS_OF_MUTANTS_FILE_NAME, this.groupsOfMutants);
	}

	/**
	 * TODO implement lambda
	 * @param node
	 * @return
	 */
	public List<MutationOperator> checkChangePoint(Node node) {
	
		List<MutationOperator> availableMutOps = new ArrayList<>();
	
		for (MutationOperator mutOp : mutationOperators) {
			if (mutOp.isChangePoint(node, this)) {
				availableMutOps.add(mutOp);
			}
		}
	
		return availableMutOps;
	}

	/**
	 * @param node
	 * @param mutOps
	 */
	public void generateMutants(Node node, List<MutationOperator> mutOps) {

		if (!mutOps.isEmpty()) {
			for (MutationOperator mutOp : mutOps) {
				Node mutatedNode = mutOp.generateMutants(node, this);
				if (mutatedNode != null) {
					node.replace(mutatedNode);
				}
			}
		}
	}
}
