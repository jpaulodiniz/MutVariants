package br.ufmg.labsoft.mutvariants.mutants;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import br.ufmg.labsoft.mutvariants.entity.MutantInfo;
import br.ufmg.labsoft.mutvariants.util.JavaBinaryOperatorsGroups;
import br.ufmg.labsoft.mutvariants.util.TypeUtil;

/**
 * 
 * @author jpaulo
 *
 */
public abstract class BinaryExprMutationStrategy implements MutationStrategy {

	/**
	 * 
	 * @param original
	 * @param onlyEqualityOperators
	 * @return
	 */
	public static Operator operatorForMutation(Operator original, boolean onlyEqualityOperators) {

		EnumSet<Operator> tempEnumSet = availableOperatorsForMutation(original, onlyEqualityOperators);

		Operator[] tempArray = new Operator[tempEnumSet.size()];
		tempEnumSet.toArray(tempArray);

		int index = new Random().nextInt(tempArray.length);
		return tempArray[index];
	}

	/**
	 * 
	 * @param original
	 * @param onlyEqualityOperators
	 * @return
	 */
	public static EnumSet<Operator> availableOperatorsForMutation(Operator original, boolean onlyEqualityOperators) {

		EnumSet<Operator> tempEnumSet = JavaBinaryOperatorsGroups.belongingGroup(original, onlyEqualityOperators);
		
		if (tempEnumSet == null)
			return null;

		tempEnumSet.remove(original);
		return tempEnumSet;
	}
	
	/**
	 * Checks whether a binary expression is a change point for mutation in two cases:
	 * 1) operator + performing a sum or string concatenation 
	 * 2) (in)equality operators: == or != doing comparison between numbers or objects  
	 * @param be
	 * @return true if + adds two numbers OR == and != compares two numbers
	 */
	public boolean isChangePoint(BinaryExpr be, MutantsGenerator mGen) {
		
		//boolean equalityOperatorNoNumbers = false; //TODO review

		//avoiding mutating String concatenator operator +
		if (BinaryExpr.Operator.PLUS.equals(be.getOperator())) {

			ResolvedType type = null;
			
			try {
				type = JavaParserFacade.get(mGen.getTypeSolver()).getType(be); 
			}
			catch (UnsolvedSymbolException use) {
				System.err.println("USE_Str: " + be + "\n" + use.getMessage());
				return false;
//				throw use;
			}
			catch (UnsupportedOperationException uoe) {
				System.err.println("UOE_Str: " + be + "\n" + uoe.getMessage());
				return false;
			}
			catch (RuntimeException re) {
				System.err.println("RE_Str: " + be + "\n" + re.getMessage());
				return false;
//				throw re;
			}
			
			if (TypeUtil.isString(type)) {
//				throw new RuntimeException("can't 1: " + original);
				return false;
			}
		}
		//checking == or != with numbers
		else if (JavaBinaryOperatorsGroups.equalityOperators.contains(be.getOperator())) {

			if (be.getRight().toString().equals("null") || be.getLeft().toString().equals("null")) {
//				equalityOperatorNoNumbers = true;
				return false;
			}
			
			ResolvedType typeLeft = null;
			ResolvedType typeRight = null;

			try {
				typeLeft = JavaParserFacade.get(mGen.getTypeSolver()).getType(be.getLeft());
			}
			catch (UnsolvedSymbolException use) {
				System.err.println("USE_L: " + be + "\n" + use.getMessage());
				return false;
//				throw use;
			}
			catch (RuntimeException re) {
				System.err.println("RE_L: " + be + "\n" + re.getMessage());
				return false;
//				throw re;
			}

			try {
				typeRight = JavaParserFacade.get(mGen.getTypeSolver()).getType(be.getRight());
			}
			catch (UnsolvedSymbolException use) {
				System.err.println("USE_R: " + be + "\n" + use.getMessage());
				return false;
//				throw use;
			}
			catch (RuntimeException re) {
				System.err.println("RE_R: " + be + "\n" + re.getMessage());
				return false;
//				throw re;
			}
			
			//ensures mutations on expressions like var.size() == 4
			if (!TypeUtil.isNumberPrimitiveOrWrapper(typeLeft) || !TypeUtil.isNumberPrimitiveOrWrapper(typeRight)) {
//				equalityOperatorNoNumbers = true;
//				throw new RuntimeException("can't 2: " + original);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param original operand1 ORIGINAL operand2
	 * @param mGen
	 * @return (_PREFIX_mut# ? (operand1 MUTATION1 operand2) : (operand1 ORIGINAL operand2)) or 
	 * (_PREFIX_mut## ? (operand1 MUTATION2 operand2) : (_PREFIX_mut# ? (operand1 MUTATION1 operand2) : (operand1 ORIGINAL operand2)))
	 * @throws Exception
	 */
	public Expression mutateBinaryExpression(BinaryExpr original, MutantsGenerator mGen) { //throws RuntimeException, boolean allAvailableMutants

		EnumSet<Operator> mOperators = mGen.isAllPossibleMutationsPerChangePoint() ? 
				availableOperatorsForMutation(original.getOperator(), false) :
				EnumSet.of(operatorForMutation(original.getOperator(), false) ); 
				
		if (mOperators == null || mOperators.isEmpty()) return null; //<<, >>, >>>, &, |, ... TODO review 
		
		List<String> groupOfMutants = new ArrayList<>(); //Issue #4

		Expression mutantExpressionTemp = original.clone();

		for (Operator op : mOperators) {
			
			if (Math.random() > mGen.getMutationRate()) continue;

			BinaryExpr mutated = original.clone();
			mutated.setOperator(op);

			String mutantVariableName = mGen.nextMutantVariableName();
			groupOfMutants.add(mutantVariableName);

			//generation mutant information for mutants catalog
			MutantInfo mInfo = new MutantInfo();
			mInfo.setMutantVariableName(mutantVariableName);
			mInfo.setOriginaBinaryOperator(original.getOperator().asString());
			mInfo.setMutatedBinaryOperator(op.asString());
			mInfo.setMutatedClass(mGen.getCurrentClassFQN());
			mInfo.setMutatedOperation(mGen.getCurrentOperation());
			mGen.addMutantInfoToCatalog(mInfo);

			mutantExpressionTemp = new ConditionalExpr(
					new NameExpr(mutantVariableName), 
					new EnclosedExpr(mutated),
					new EnclosedExpr(mutantExpressionTemp.clone()));
		}
		
		mGen.addGroupOfMutants(groupOfMutants);

		return new EnclosedExpr(mutantExpressionTemp);
	}
}
