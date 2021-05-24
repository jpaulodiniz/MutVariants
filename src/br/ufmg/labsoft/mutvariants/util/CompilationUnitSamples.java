package br.ufmg.labsoft.mutvariants.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;

public class CompilationUnitSamples {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static CompilationUnit createPublicCompilationUnit(String pkg, String className) {
		CompilationUnit compUn = new CompilationUnit(pkg);
		compUn.addClass(Constants.MUTANTS_CLASS_NAME, Modifier.PUBLIC);
		return compUn;
	}

	/**
	 * creates a simple compilation unit
	 */
	public static CompilationUnit createSampleCompilationUnit() {
		CompilationUnit compilationUnit = JavaParser.parse("package learning.javaparser;"
				+ "import org.junit.Test;"
				+ "public class A {"
				+ " private static int CONST = 100;"
				+ " public void method1() {"
				+ "  int x = 0;"
				+ "  int a = x + 1, jj = x;"
				+ "  int b[] = {7, 8, 9};"
				+ "  b[a + 1] = 10;"
				+ "  b[1] = b[a + 2];"
				+ "  if (x > a) {"
				+ "   x = a + 10;"
				+ "   a = x + 10 * CONST;"
				+ "  }"
				+ " }"
				+ " public void method2(int a) {"
				+ "  if (a < 0 || a > 10) {"
				+ "   System.out.println(\"Out of bounds\" + a - CONST);"
				+ "   return;"
				+ "  }"
				+ "  int b = 5;"
				+ "  while (b < 10) {"
				+ "   System.out.println(b);"
				+ "   b++;"
				+ "  }"
				+ "  for (;;)"
				+ "   b++;"
				+ " }"
				+ "}");

		return compilationUnit;
	}

}
