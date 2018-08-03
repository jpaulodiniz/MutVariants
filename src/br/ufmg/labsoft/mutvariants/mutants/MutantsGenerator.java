package br.ufmg.labsoft.mutvariants.mutants;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import br.ufmg.labsoft.mutvariants.util.Constants;
import br.ufmg.labsoft.mutvariants.util.IO;
import br.ufmg.labsoft.mutvariants.util.JavaBinaryOperatorsGroups;
import br.ufmg.labsoft.mutvariants.util.TypeUtil;

/*
 * TODO(s)
 * - don't mutate main methods
 * - start mutantCounter with 1 (current is 0)
 * - seek for interfaces with default methods and check whether there was something to mutate
 * - verify if Java bitwise operators could be mutated
 * - [OK] don't mutate Exception classes
 * - [OK] implement global mutantsCounter
 * - [OK] preserve package directory structure when mutate whole packages
 */

/**
 *
 * @author jpaulo
 */
public class MutantsGenerator {

	private String currentClassName;
	private int mutantsCounterPerClass;
	private long mutantsCounterGlobal;
	
	/**
	 * generates all possible mutants per spot
	 * E.g.: for a + b expression, all available mutations -, *, / and %
	 */
	private boolean allPossibleMutationsPerSpot = false;
	private boolean mutateLoopConditions = false;
	private double mutationRate = 1d; 
	private MutationStrategy mutStrategy;
	private TypeSolver typeSolver;

	public MutantsGenerator() {
		this(new OneBinaryExprPerStatementMutationStrategy(), new ReflectionTypeSolver());
	}

	public MutantsGenerator(MutationStrategy mutStrategy, TypeSolver typeSolver) {
		super();
		this.mutStrategy = mutStrategy;
		this.typeSolver = typeSolver;
	}

	public boolean getAllPossibleMutationsPerSpot() {
		return allPossibleMutationsPerSpot;
	}

	public boolean getMutateLoopConditions() {
		return mutateLoopConditions;
	}

	public void setMutateLoopConditions(boolean mutateLoopConditions) {
		this.mutateLoopConditions = mutateLoopConditions;
	}

	public void setAllPossibleMutationsPerSpot(boolean allPossibleMutationsPerSpot) {
		this.allPossibleMutationsPerSpot = allPossibleMutationsPerSpot;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public MutationStrategy getMutStrategy() {
		return mutStrategy;
	}

	public void setMutStrategy(MutationStrategy mutStrategy) {
		this.mutStrategy = mutStrategy;
	}

	public TypeSolver getTypeSolver() {
		return typeSolver;
	}

	public void setTypeSolver(TypeSolver typeSolver) {
		this.typeSolver = typeSolver;
	}

	/**
	 * @param original
	 * @return
	 */
	private void generateMutants(ClassOrInterfaceDeclaration mClass) {
		this.currentClassName = mClass.getNameAsString();
		if (this.currentClassName.endsWith("Exception")) {
			return;
		}
		
		//strategy
		this.getMutStrategy().generateMutants(mClass, this);

		if (this.mutantsCounterPerClass > 0) {
			//adding declarations for all conditional mutant variables

			NodeList<VariableDeclarator> variables = new NodeList<>();
			for (int m=0; m < this.mutantsCounterPerClass; ++m) {
				variables.add(new VariableDeclarator(new PrimitiveType(Primitive.BOOLEAN),
						buildMutantVariableName(this.currentClassName, m), new BooleanLiteralExpr(false)));
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
			this.mutantsCounterPerClass = 0;
			this.generateMutants(aClass);
			mutantsCounterPerCompUn += this.mutantsCounterPerClass;
		}

		if (mutantsCounterPerCompUn > 0) {
			//add import Conditional
//			mcu.addImport(new ImportDeclaration(new Name(Constants.VAREXJ_CONDITIONAL_FQN), false, false));
			mcu.addImport(Constants.VAREXJ_CONDITIONAL_FQN);

			System.out.println(">>>> " + mutantsCounterPerCompUn + " mutants seeded (in Comp. Unit).");

			this.mutantsCounterGlobal += mutantsCounterPerCompUn;
			return mcu;
		}

		return original;
	}

	private static String buildMutantVariableName(String className, long mutSeq) {

		return Constants.MUTANT_VARIABLE_PREFIX1 + className
				+ Constants.MUTANT_VARIABLE_PREFIX2 + mutSeq;
	}

	/**
	 * 
	 * @param original operand1 ORIGINAL operand2
	 * @return (_PREFIX_mut# ? (operand1 MUTATION1 operand2) : (operand1 ORIGINAL operand2)) or 
	 * (_PREFIX_mut## ? (operand1 MUTATION2 operand2) : (_PREFIX_mut# ? (operand1 MUTATION1 operand2) : (operand1 ORIGINAL operand2)))
	 * @throws Exception
	 */
	public EnclosedExpr mutateBinaryExpression(BinaryExpr original) { //throws RuntimeException, boolean allAvailableMutants

//		boolean equalityOperatorNoNumbers = false; //TODO review

		//avoiding mutating String concatenator operator +
		if (BinaryExpr.Operator.PLUS.equals(original.getOperator())) {

			ResolvedType type = JavaParserFacade.get(this.typeSolver).getType(original);
			if (TypeUtil.isString(type)) {
//				throw new RuntimeException("can't 1: " + original);
				return null;
			}
		}
		//checking == or != with numbers
		else if (JavaBinaryOperatorsGroups.equalityOperators.contains(original.getOperator())) {

			ResolvedType typeLeft = JavaParserFacade.get(this.typeSolver).getType(original.getLeft());
			ResolvedType typeRight = JavaParserFacade.get(this.typeSolver).getType(original.getRight());

			//ensures mutations on expressions like var.size() == 4
			if (!TypeUtil.isNumberPrimitiveOrWrapper(typeLeft) || !TypeUtil.isNumberPrimitiveOrWrapper(typeRight)) {
//				equalityOperatorNoNumbers = true;
//				throw new RuntimeException("can't 2: " + original);
				return null;
			}
		}

		EnumSet<Operator> mOperators = this.getAllPossibleMutationsPerSpot() ? 
				availableOperatorsForMutation(original.getOperator(), false) :
				EnumSet.of(operatorForMutation(original.getOperator(), false) ); 
				
		if (mOperators == null || mOperators.isEmpty()) return null;

		Expression mutantExpressionTemp = original.clone();

		for (Operator op : mOperators) {
			
			if (Math.random() > this.getMutationRate()) continue;

			BinaryExpr mutated = original.clone();
			mutated.setOperator(op);

			String mutantVariableName = buildMutantVariableName(
					this.currentClassName, //previously: original.getAncestorOfType(ClassOrInterfaceDeclaration.class).get().getNameAsString() 
					mutantsCounterPerClass++);

			mutantExpressionTemp = new ConditionalExpr(
					new NameExpr(mutantVariableName), 
					new EnclosedExpr(mutated),
					new EnclosedExpr(mutantExpressionTemp.clone()));
		}

		return new EnclosedExpr(mutantExpressionTemp);
	}
	
	public static Operator operatorForMutation(Operator original, boolean onlyEqualityOperators) {

		EnumSet<Operator> tempEnumSet = availableOperatorsForMutation(original, onlyEqualityOperators);

		Operator[] tempArray = new Operator[tempEnumSet.size()];
		tempEnumSet.toArray(tempArray);

		int index = new Random().nextInt(tempArray.length);
		return tempArray[index];
	}

	public static EnumSet<Operator> availableOperatorsForMutation(Operator original, boolean onlyEqualityOperators) {

		EnumSet<Operator> tempEnumSet = JavaBinaryOperatorsGroups.belongingGroup(original, onlyEqualityOperators);
		
		if (tempEnumSet == null)
			return null;

		tempEnumSet.remove(original);
		return tempEnumSet;
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
		
		System.out.println(">>>>> " + countMutatedCompilationUnits + " mutated compilation units.");
		System.out.println(">>>>> " + this.mutantsCounterGlobal + " mutants seeded (total).");
		System.out.println(">>>>> in " + (fin - ini) + "(ms)");
	}
}
