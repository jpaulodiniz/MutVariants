package br.ufmg.labsoft.mutvariants.util;

import com.github.javaparser.ast.body.MethodDeclaration;

public class MethodUtil {

	public static boolean isMainMethod(MethodDeclaration methodDecl) {
		return methodDecl.getNameAsString().equals("main") && 
				methodDecl.isPublic() && 
				methodDecl.isStatic() &&
				methodDecl.getType().isVoidType() &&
				methodDecl.getParameters().size() == 1 &&
				methodDecl.getParameter(0).getType().asString().equals("String[]");
	}
}
