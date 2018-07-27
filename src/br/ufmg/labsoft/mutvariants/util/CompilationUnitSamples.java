package br.ufmg.labsoft.mutvariants.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class CompilationUnitSamples {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * creates a simple compilation unit by JP
	 */
	public static CompilationUnit createCompilationUnit() {
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
				//+ "  if (c) {"
				+ "  if (a < 0 || a > 10) {"
				+ "   System.out.println(\"Out of bounds\" + a - CONST);"
				+ "   return;"
				+ "  }"
				+ "  int b = 5;"
				+ "  while (b < 10) {"
				+ "   System.out.println(b);"
				+ "   b++;"
				+ "  }"
				+ " }"
				+ "}");

		return compilationUnit;
	}

}
