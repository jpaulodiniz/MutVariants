package br.ufmg.labsoft.mutvariants.entity;

/**
 * Issue #3
 * @author jpaulo
 *
 */
public class MutantInfo {

	private String mutantVariableName; //mutant id
	private String mutationOperator;

	private String mutatedClass;
	private String mutatedOperation; //method or constructor

	private String originaBinaryOperator;
	private String mutatedBinaryOperator;

	public String getMutantVariableName() {
		return mutantVariableName;
	}

	public void setMutantVariableName(String mutantVariableName) {
		this.mutantVariableName = mutantVariableName;
	}

	public String getMutationOperator() {
		return mutationOperator;
	}

	public void setMutationOperator(String mutationOperator) {
		this.mutationOperator = mutationOperator;
	}

	public String getMutatedClass() {
		return mutatedClass;
	}

	public void setMutatedClass(String mutatedClass) {
		this.mutatedClass = mutatedClass;
	}

	public String getMutatedOperation() {
		return mutatedOperation;
	}

	public void setMutatedOperation(String mutatedOperation) {
		this.mutatedOperation = mutatedOperation;
	}

	public String getOriginaBinaryOperator() {
		return originaBinaryOperator;
	}

	public void setOriginaBinaryOperator(String originaBinaryOperator) {
		this.originaBinaryOperator = originaBinaryOperator;
	}

	public String getMutatedBinaryOperator() {
		return mutatedBinaryOperator;
	}

	public void setMutatedBinaryOperator(String mutatedBinaryOperator) {
		this.mutatedBinaryOperator = mutatedBinaryOperator;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(mutantVariableName).append(',');
//		sb.append(mutationOperator).append(',');
		sb.append(mutatedClass).append(',');
		sb.append(mutatedOperation).append(',');
		sb.append(originaBinaryOperator).append(',');
		sb.append(mutatedBinaryOperator);

		return sb.toString();
	}

	public static String infoHeader() { //based on toString order

		StringBuilder sb = new StringBuilder();
		sb.append("Mutant Variable Name,");
//		sb.append("Mutation Operator,");
		sb.append("Mutated Class,");
		sb.append("Mutated Method,");
		sb.append("Original Binary Operator,");
		sb.append("Mutated Binary Operator");

		return sb.toString();
	}
}
