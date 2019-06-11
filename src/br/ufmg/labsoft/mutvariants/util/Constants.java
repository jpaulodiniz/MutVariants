package br.ufmg.labsoft.mutvariants.util;

public class Constants {

	/**
	 * Example: _M_ in _M_<class_name>_mut#
	 */
	public static final String MUTANT_VARIABLE_PREFIX1 = "_M_"; 

	/**
	 * Example: _mut in _M_<class_name>_mut#
	 */
	public static final String MUTANT_VARIABLE_PREFIX2 = "_mut";

	/**
	 * name of VarexJ conditional annotation
	 */
	public static final String VAREXJ_CONDITIONAL_NAME = "Conditional";

	/**
	 * fully qualified name of VarexJ conditional annotation
	 */
	public static final String VAREXJ_CONDITIONAL_FQN = "gov.nasa.jpf.annotation.Conditional";
	
	/**
	 * name of the mutants generated catalog file
	 */
	public static final String MUT_CATALOG_FILE_NAME = "mutants-catalog.txt";

	/**
	 * Issue #4
	 */
	public static final String GROUPS_OF_MUTANTS_FILE_NAME = "groups-of-mutants.txt";

	/**
	 * Issue #2
	 */
	public static final String NESTED_MUTANTS_INFO_FILE_NAME = "nested-mutants.txt";

	/**
	 * Issue #1
	 * FQN class containing methods for mutation operators in binary expressions
	 */
	public static final String MUTANT_SCHEMATA_LIB = "br.ufmg.labsoft.mutvariants.schematalib.SchemataLibMethods";
}
