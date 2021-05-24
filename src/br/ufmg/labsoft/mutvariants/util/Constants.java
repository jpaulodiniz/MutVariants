package br.ufmg.labsoft.mutvariants.util;

public class Constants {

	/**
	 * Example: _mut in _mut#
	 */
	public static final String MUTANT_VARIABLE_PREFIX2 = "_mut";

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

	public static final String MUTANTS_CLASS_PACKAGE = "br.ufmg.labsoft.mutvariants";
	public static final String MUTANTS_CLASS_NAME    = "AllMutants";
}
