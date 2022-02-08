package br.ufmg.labsoft.mutvariants.core;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import br.ufmg.labsoft.mutvariants.entity.MutationInfo;
import br.ufmg.labsoft.mutvariants.listeners.ListenerUtil;
import br.ufmg.labsoft.mutvariants.listeners.LoopListenerCallInserter;
import br.ufmg.labsoft.mutvariants.mutops.MutationOperator;
import br.ufmg.labsoft.mutvariants.util.CompilationUnitSamples;
import br.ufmg.labsoft.mutvariants.util.Constants;
import br.ufmg.labsoft.mutvariants.util.IO;

/*
 * TODO(s)
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

	private ListenerVisitor lv = new ListenerVisitor();
	private LoopListenerCallInserter lci = new LoopListenerCallInserter();

	private List<MutationInfo> mutantsCatalog; //Issue #3
	private List<List<String>> groupsOfMutants; //Issue #4
	private Map<String, Set<String>> nestedMutantInfo; //Issue #2

	public String currentClassFQN; //helper field: current class fully qualified name (FQN)

	public Set<NameExpr> classFinalAttributesNonInitialized;
	public Stack<Set<NameExpr>> blockVariablesNonInitialized; //SBR (Issue #2)

	public String currentOperation; //helper field - method or constructor - Issue #3
	private int mutantsCounterGlobal = 0; //helper field

	/**
	 * generates all possible mutants per change point
	 * E.g.: for a + b expression, apply all available mutations -, *, / and %
	 */
	private boolean allPossibleMutationsPerChangePoint = false;
	private boolean mutateLoopConditions = false;
	private boolean listenerCallsInstrumentation = false; // Issue #5
	private double mutationRate = 1d;
	private boolean mutantsAsVariables = false;
	private TypeSolver typeSolver;

	public MutantsGenerator() {
		this.mutationOperators = new HashSet<>();
		this.mutationVisitor = new MutationVisitor();
		this.mutantsCatalog = new ArrayList<>();
		this.groupsOfMutants = new ArrayList<>();
		this.nestedMutantInfo = new HashMap<>();
		this.blockVariablesNonInitialized = new Stack<>();

		this.lv = new ListenerVisitor();
		this.lci = new LoopListenerCallInserter();
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

	public boolean getListenerCallsInstrumentation() {
		return listenerCallsInstrumentation;
	}

	public void setListenerCallsInstrumentation(boolean listenerCallsInstrumentation) {
		this.listenerCallsInstrumentation = listenerCallsInstrumentation;
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

	public boolean getMutantsAsVariables() {
		return mutantsAsVariables;
	}

	public void setMutantsAsVariables(boolean mutantsAsVariables) {
		this.mutantsAsVariables = mutantsAsVariables;
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
		if (groupOfMutants.size() > 1) {
			this.groupsOfMutants.add(groupOfMutants);
		}
	}

	public Set<NameExpr> findFinalAttrsNonInitialized(TypeDeclaration<?> mClass) {
		Set<NameExpr> finalVariablesNonInitialized = new HashSet<>();

		List<FieldDeclaration> finalFields = (mClass.getFields()).stream()
				.filter(f -> f.getModifiers().contains(Modifier.FINAL))
				.collect(Collectors.toList());

		for (FieldDeclaration finalField : finalFields) {
			for (VariableDeclarator vd : finalField.getVariables()) {
				if (!vd.getInitializer().isPresent()) {
					finalVariablesNonInitialized.add(vd.getNameAsExpression());
				}
			}
		}

		return finalVariablesNonInitialized.isEmpty() ? null : finalVariablesNonInitialized;
	}

	/**
	 * find local variables in current block (not tested ones)
	 * that are not initialized in their declaration expressions
	 * @param mBlock mutation candidate block
	 * @return list of NameExpr of such variable declarations
	 */
	public Set<NameExpr> findVariablesNonInitialized(BlockStmt mBlock) {
		Set<NameExpr> variablesNonInitialized = new HashSet<>();

		List<VariableDeclarationExpr> variableDeclarations =
				mBlock.findAll(VariableDeclarationExpr.class,
						// declared only in current block, not nested ones
						v -> v.getParentNode().get().getParentNode().get().equals(mBlock));

		for (VariableDeclarationExpr varDecl : variableDeclarations) {

			for (VariableDeclarator vd : varDecl.getVariables()) {
				if (!vd.getInitializer().isPresent()) {
					variablesNonInitialized.add(vd.getNameAsExpression());
				}
			}
		}

		return variablesNonInitialized.isEmpty() ? null : variablesNonInitialized;
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

		this.currentOperation = null;

		mClass.accept(this.mutationVisitor, this);

//		if (this.mutantsPerClass.get(this.currentClassFQN) != null &&
//				!this.mutantsPerClass.get(this.currentClassFQN).isEmpty()) {
//		//adding declarations for all conditional mutant variables
//			NodeList<VariableDeclarator> variables = new NodeList<>();
//			for (String mutName : this.mutantsPerClass.get(this.currentClassFQN)) {
//				variables.add(new VariableDeclarator(new PrimitiveType(Primitive.BOOLEAN),
//						mutName, new BooleanLiteralExpr(false)));
//			}
//
//			FieldDeclaration fieldDecl = new FieldDeclaration(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), variables);
//			fieldDecl.addMarkerAnnotation(Conditional.class);
//
//			mClass.getMembers().add(0, fieldDecl);
//		}
	}

	public void generateMutants(CompilationUnit compUn) {
		List<ClassOrInterfaceDeclaration> classes = compUn.findAll(ClassOrInterfaceDeclaration.class,
				c -> !c.isInterface() && !c.isNestedType()); // neither interface nor nested class

		//mutate each class in compilation unit
		int mBefore = this.mutantsCounterGlobal;
		for (ClassOrInterfaceDeclaration aClass : classes) {
			this.generateMutants(aClass);
		}
		int mAfter = this.mutantsCounterGlobal;
		int mutantsCounterPerCompUn = mAfter - mBefore;

		if (this.getListenerCallsInstrumentation()) {
			long before = this.lci.getLoopSeq();
			for (ClassOrInterfaceDeclaration aClass : classes) {
				aClass.accept(this.lv, this.lci);
			}

			long after = this.lci.getLoopSeq();
			if (after > before) {
				compUn.addImport(ListenerUtil.class);
			}
		}

		if (mutantsCounterPerCompUn > 0) {
//			if (TODO) {
//				mcu.addImport(Constants.MUTANT_SCHEMATA_LIB, true, true);
//			}
			if (this.mutantsAsVariables) {
				compUn.addImport(Constants.MUTANTS_CLASS_PACKAGE + "."
						+ Constants.MUTANTS_CLASS_NAME, true, true);
			}
			else {
				compUn.addImport(ListenerUtil.class);
			}
			System.out.println(">>>> " + mutantsCounterPerCompUn + " mutants seeded (in Comp. Unit).");
		}
	}

	/**
	 *
	 * @return
	 */
	public Expression nextMutantAccessExpression() {
		String mutVar = this.nextMutantVariableName();

		if (this.mutantsAsVariables) {
			return new NameExpr(mutVar);
		}
		else {
			int mutantId = this.lastMutantGenerated();
			MethodCallExpr listenerCall = new MethodCallExpr(
					new FieldAccessExpr(new NameExpr(ListenerUtil.class.getSimpleName()), "mutListener"),
					"listen",
					NodeList.nodeList(new LongLiteralExpr(mutantId)));
			return listenerCall;
		}
	}

	public int lastMutantGenerated() {
		return this.mutantsCounterGlobal - 1;
	}

	public String currentMutantId() {
		return buildMutantVariableName(this.lastMutantGenerated());
	}

	private String nextMutantVariableName() {
		String mutantVariable = buildMutantVariableName(this.mutantsCounterGlobal++);
		return mutantVariable;
	}

	private String buildMutantVariableName(long mutSeq) {
		if (this.mutantsAsVariables) {
			StringBuilder mutantName = new StringBuilder();
			mutantName.append(Constants.MUTANT_VARIABLE_PREFIX2);
			mutantName.append(mutSeq);
			return mutantName.toString();
		}
		return String.valueOf(mutSeq);
	}

	public void mutateSourceFolders(List<String> inputPaths, List<String> outputPaths,
			String catalogueAndGroupsPath) {

		long ini = System.currentTimeMillis();

		this.mutantsCounterGlobal = 0;

		for (int i = 0; i < inputPaths.size(); i++) {
			mutatePackageOrDirectory(inputPaths.get(i), outputPaths.get(i));
		}

		IO.saveMutantsCatalog(catalogueAndGroupsPath, Constants.MUT_CATALOG_FILE_NAME, this.mutantsCatalog);
		IO.saveGroupsOfMutants(catalogueAndGroupsPath, Constants.GROUPS_OF_MUTANTS_FILE_NAME, this.groupsOfMutants);
		IO.saveNestedMutantsInfo(catalogueAndGroupsPath, Constants.NESTED_MUTANTS_INFO_FILE_NAME, this.nestedMutantInfo);

		long fin = System.currentTimeMillis();

		System.out.print("\n>>>>> " + this.mutantsCounterGlobal + " mutants seeded");
		System.out.println(" in " + (fin - ini) + "ms");
	}

	private void mutatePackageOrDirectory(String inputPath, String outputPath) {
		System.out.println("\n** Source folder: " + inputPath);

		long qtdFOMsSoFar = this.mutantsCounterGlobal;
		int countMutatedCompilationUnits = 0;

		for (File f : IO.allJavaFilesIn(inputPath)) {
			System.out.println(f);

			CompilationUnit original = IO.getCompilationUnitFromFile(f);
			CompilationUnit mutated = original.clone();
			this.generateMutants(mutated);

			boolean writeCompUn = false;
			if (mutated.getImports().stream().anyMatch(i -> i.getNameAsString().equals(
					Constants.MUTANTS_CLASS_PACKAGE + "." + Constants.MUTANTS_CLASS_NAME))) {
				writeCompUn = true;
			}
			else if (mutated.getImports().stream().anyMatch(i -> i.getNameAsString().equals(
					ListenerUtil.class.getCanonicalName()))) {
				writeCompUn = true;
			}

			if (writeCompUn) {
				++countMutatedCompilationUnits;
				IO.writeCompilationUnit(mutated, new File(outputPath));
			}
		}

		if (this.mutantsAsVariables) {
			CompilationUnit mutantsClass = CompilationUnitSamples.createPublicCompilationUnit(
					Constants.MUTANTS_CLASS_PACKAGE, Constants.MUTANTS_CLASS_NAME);
//			mutantsClass.addImport(Conditional.class); //add import Conditional TODO keep?

	//adding declarations for all conditional mutant variables
		// declaring all mutants in a single declaration line
		/*
			NodeList<VariableDeclarator> variables = new NodeList<>();
			for (MutationInfo mutInfo : this.mutantsCatalog) {
				variables.add(new VariableDeclarator(new PrimitiveType(Primitive.BOOLEAN),
						mutInfo.getMutantVariableName(), new BooleanLiteralExpr(false)));
			}
			FieldDeclaration fieldDecl = new FieldDeclaration(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), variables);
			fieldDecl.addMarkerAnnotation(Conditional.class);
			mutantsClass.findFirst(ClassOrInterfaceDeclaration.class).get()
					.getMembers().add(0, fieldDecl);
		*/
		// declaring one mutant per line
			for (MutationInfo mutInfo : this.mutantsCatalog.subList((int)qtdFOMsSoFar,
					(int)this.mutantsCounterGlobal)) {
				VariableDeclarator vd = new VariableDeclarator(new PrimitiveType(Primitive.BOOLEAN),
						mutInfo.getMutantId(), new BooleanLiteralExpr(false));
				FieldDeclaration fieldDecl = new FieldDeclaration(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), vd);
	//			fieldDecl.addMarkerAnnotation(Conditional.class);
				mutantsClass.findFirst(ClassOrInterfaceDeclaration.class).get()
						.getMembers().add(fieldDecl);
			}

			IO.writeCompilationUnit(mutantsClass, new File(outputPath));
		}

		System.out.print(countMutatedCompilationUnits + " compilation units mutated");
	}

	/**
	 * @param node
	 * @return
	 */
	public List<MutationOperator> checkChangePoint(Node node) {

		List<MutationOperator> availableMutOps = new ArrayList<>();

		for (MutationOperator mutOp : this.mutationOperators) {
			try {
				if (mutOp.isChangePoint(node, this)) {
					availableMutOps.add(mutOp);
				}
			}
			catch (Exception e) {
				System.err.println(e.toString() + ": " + node);
				System.err.flush();
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

	public void addNestedMutantsInfo(int outerMutant, int firstNested, int lastNested) {
		Set<String> nestedMutants = new HashSet<>();
		for (int n=firstNested; n <= lastNested; ++n) {
			nestedMutants.add(buildMutantVariableName(n));
		}
		this.nestedMutantInfo.put(buildMutantVariableName(outerMutant), nestedMutants);
	}

	@Deprecated
	public void addNestedMutantsInfo(String mutantVariableName, Set<String> nestedMutants) {
		this.nestedMutantInfo.put(mutantVariableName, nestedMutants);
	}
}
