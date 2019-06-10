package br.ufmg.labsoft.mutvariants.entity;

/**
 * Issue #3
 * @author jpaulo
 *
 */
public class MutationInfo {
	
	private String mutantVariableName; //mutant id
	private String mutationOperator;

	private String mutatedClass;
	private String mutatedMethod;

	private String infoBeforeMutation;
	private String infoAfterMutation;
	
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

	public String getMutatedMethod() {
		return mutatedMethod;
	}

	public void setMutatedMethod(String mutatedMethod) {
		this.mutatedMethod = mutatedMethod;
	}

	public String getInfoBeforeMutation() {
		return infoBeforeMutation;
	}

	public void setInfoBeforeMutation(String infoBeforeMutation) {
		this.infoBeforeMutation = infoBeforeMutation;
	}

	public String getInfoAfterMutation() {
		return infoAfterMutation;
	}

	public void setInfoAfterMutation(String infoAfterMutation) {
		this.infoAfterMutation = infoAfterMutation;
	}

	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(mutantVariableName).append(',');
		sb.append(mutationOperator).append(',');
		sb.append(mutatedClass).append(',');
		sb.append(mutatedMethod).append(',');
		sb.append(infoBeforeMutation).append(',');
		sb.append(infoAfterMutation);

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
