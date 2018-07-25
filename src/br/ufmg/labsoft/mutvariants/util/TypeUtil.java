package br.ufmg.labsoft.mutvariants.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;

public class TypeUtil {

	private static final Set<String> noNumberPrimitives = new HashSet<>(Arrays.asList("char", "boolean"));

	private static final Set<String> numberWrappers = new HashSet<>(Arrays.asList("java.lang.Integer", 
			"java.lang.Double", "java.lang.Long", "java.lang.Float", "java.lang.Byte", "java.lang.Short"));

	/**
	 *
	 * @param symbolSolvedType
	 * @return true whether symbolSolvedType is a primitive number
	 * or a wrapper that is a subclass of java.util.Number
	 */
	public static boolean isNumberPrimitiveOrWrapper(ResolvedType symbolSolvedType) {

		String describedType = symbolSolvedType.describe();
		return symbolSolvedType.isPrimitive() && !noNumberPrimitives.contains(describedType)
				|| symbolSolvedType.isReference() && numberWrappers.contains(describedType);
	}

	/**
	 *
	 * @param symbolSolvedType
	 * @return true whether symbolSolvedType is java.lang.String
	 */
	public static boolean isString(ResolvedType symbolSolvedType) {

		try {
			return symbolSolvedType.isReferenceType() && "java.lang.String".equals(symbolSolvedType.describe());
		}
		catch (UnsolvedSymbolException e) {
			e.printStackTrace();
		}

		return false; //TODO review
	}
}
