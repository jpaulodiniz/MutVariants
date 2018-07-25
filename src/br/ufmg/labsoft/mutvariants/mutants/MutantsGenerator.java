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
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import br.ufmg.labsoft.mutvariants.util.Constants;
import br.ufmg.labsoft.mutvariants.util.IO;
import br.ufmg.labsoft.mutvariants.util.JavaOperatorsGroups;
import br.ufmg.labsoft.mutvariants.util.TypeUtil;
//import gov.nasa.jpf.annotation.Conditional;

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
 * Version: 0.12
 *
 * @author jpaulo
 * Dependency: JavaParser API (javaparser.org)
 */
public class MutantsGenerator {

	private String currentClassName;
	private int mutantsCounterPerClass;
	private long mutantsCounterGlobal;

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
						formMutantVariableName(this.currentClassName, m), new BooleanLiteralExpr(false)));
			}

			FieldDeclaration fieldDecl = new FieldDeclaration(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), variables);
			fieldDecl.addMarkerAnnotation("Conditional");

//			NodeList<AnnotationExpr> annotations = new NodeList<>();
//			annotations.add(new MarkerAnnotationExpr("Conditional"));
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
//			mcu.addImport(Conditional.class);
//			mcu.addImport(new ImportDeclaration(new Name("gov.nasa.jpf.annotation.Conditional"), false, false));
			mcu.addImport("gov.nasa.jpf.annotation.Conditional");

			System.out.println(">>>> " + mutantsCounterPerCompUn + " mutants seeded (in Comp. Unit).");

			this.mutantsCounterGlobal += mutantsCounterPerCompUn;
			return mcu;
		}

		return original;
	}

	private static String formMutantVariableName(String className, long mutSeq) {

		return Constants.MUTANT_VARIABLE_PREFIX1 + className
				+ Constants.MUTANT_VARIABLE_PREFIX2 + mutSeq;
	}

	/**
	 * 
	 * @param original
	 * @return (_PREFIX_mut# ? (operand1 MUTATION operand2) : (operand1 ORIGINAL operand2))
	 * @throws Exception
	 */
	public EnclosedExpr mutateBinaryExpression(BinaryExpr original) { //throws RuntimeException

		boolean equalityOperatorNoNumbers = false;

		//avoiding mutating String concatenator operator +
		if (BinaryExpr.Operator.PLUS.equals(original.getOperator())) {

			ResolvedType type = JavaParserFacade.get(this.typeSolver).getType(original);
			if (TypeUtil.isString(type)) {
//				throw new RuntimeException("can't 1: " + original);
				return null;
			}
		}
		//checking == or != with numbers
		else if (EnumSet.of(BinaryExpr.Operator.EQUALS, BinaryExpr.Operator.NOT_EQUALS).contains(original.getOperator())) {

			ResolvedType typeLeft = JavaParserFacade.get(this.typeSolver).getType(original.getLeft());
			ResolvedType typeRight = JavaParserFacade.get(this.typeSolver).getType(original.getRight());

			//ensures mutations on expressions like var.size() == 4
			if (!TypeUtil.isNumberPrimitiveOrWrapper(typeLeft) || !TypeUtil.isNumberPrimitiveOrWrapper(typeRight)) {
//				equalityOperatorNoNumbers = true; //TODO review
//				throw new RuntimeException("can't 2: " + original);
				return null;
			}
		}

		BinaryExpr mutated = original.clone();
		mutated.setOperator(mutatedOperator(original.getOperator(), equalityOperatorNoNumbers));

		String mutantVariableName = formMutantVariableName(
//				original.getAncestorOfType(ClassOrInterfaceDeclaration.class).get().getNameAsString(), 
				this.currentClassName, 
				mutantsCounterPerClass++);
		
		ConditionalExpr mutantExpression = new ConditionalExpr(
				new NameExpr(mutantVariableName),
				new EnclosedExpr(mutated), new EnclosedExpr(original.clone()));

		return new EnclosedExpr(mutantExpression);
	}

	public static Operator mutatedOperator(Operator original, boolean onlyEqualityOperators) {

		EnumSet<Operator> tempEnumSet = null;

		if (JavaOperatorsGroups.arithmeticOperators.contains(original)) {
			tempEnumSet = JavaOperatorsGroups.arithmeticOperators.clone();
			//tempEnumSet.addAll(Constants.bitwiseOperators);
		}
		else if (JavaOperatorsGroups.logicalOperators.contains(original)) {
			tempEnumSet = JavaOperatorsGroups.logicalOperators.clone();
			//tempEnumSet.addAll(Constants.bitwiseOperators);
		}
		else if (JavaOperatorsGroups.relationalOperators.contains(original)) {
			tempEnumSet = onlyEqualityOperators ? JavaOperatorsGroups.equalityOperators.clone()
					: JavaOperatorsGroups.relationalOperators.clone();
		}
		else {
			throw new RuntimeException("[ERROR] Unexpected operator to mutate: " + original);
		}

		Operator[] tempArray = new Operator[tempEnumSet.size() - 1];
		tempEnumSet.remove(original);
		tempEnumSet.toArray(tempArray);

		int index = new Random().nextInt(tempArray.length);
		return tempArray[index];
	}

	/**
	 *
	 * @param inputPath
	 * TODO recursive ???
	 */
	public void mutatePackageOrDirectory(String inputPath, String outputPath) {

		this.mutantsCounterGlobal = 0;
		int countMutatedCompilationUnits = 0;

		for (File f : IO.allJavaFilesIn(inputPath)) {
			System.out.println(f);

			CompilationUnit original = IO.getCompilationUnitFromFile(f);
			CompilationUnit mutated = this.generateMutants(original);

			if (mutated.getImports().stream().anyMatch(i -> i.getNameAsString().startsWith("gov.nasa.jpf.annotation.Conditional"))) {
				++countMutatedCompilationUnits;
			}

			IO.writeCompilationUnit(mutated, new File(outputPath));
		}

		System.out.println(">>>>> " + countMutatedCompilationUnits + " mutated compilation units.");
		System.out.println(">>>>> " + this.mutantsCounterGlobal + " mutants seeded (total).");
	}
}
