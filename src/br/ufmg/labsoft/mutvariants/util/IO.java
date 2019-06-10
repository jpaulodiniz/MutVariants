package br.ufmg.labsoft.mutvariants.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import br.ufmg.labsoft.mutvariants.entity.MutationInfo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.TypeDeclaration;

public class IO {

	public static CompilationUnit getCompilationUnitFromFile(File inputFile) {

        CompilationUnit c = null;
		try {
			c = JavaParser.parse(inputFile);
			return c;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		return null;
	}

	private static boolean writeCompilationUnit(CompilationUnit compUn, File outputDirectory, String fileName) {

		try {
			if (!outputDirectory.exists())
				outputDirectory.mkdirs();

			FileWriter fw = new FileWriter(new File(outputDirectory, fileName));
			fw.write(compUn.toString());
			fw.close();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Generates CompilationUnit with file name retrieved
	 * from the unique class or interface inside it
	 * @param compUn
	 * @param outputDirectory
	 * @return true if success
	 */
	public static boolean writeCompilationUnit(CompilationUnit compUn, File outputDirectory) {

		@SuppressWarnings("rawtypes")
		List<TypeDeclaration> list = compUn.findAll(TypeDeclaration.class); //AnnotationDeclaration, ClassOrInterfaceDeclaration, EnumDeclaration
		String className = null;
		
		if (list.size() == 1) { //a CompilationUnit may have only one class which can even be non public
			className = list.get(0).getNameAsString();
		}
		else {
			className = list.stream().filter(c -> c.getModifiers().contains(Modifier.PUBLIC)).findFirst().get().getNameAsString();
		}

		String packageDirectories = compUn.getPackageDeclaration().get().getNameAsString().replaceAll("\\.", File.separator);

		return writeCompilationUnit(compUn, new File(outputDirectory, packageDirectories), className + ".java");
	}

	/**
	 *
	 * @param path to a package o directory
	 * @return all .java files inside directory of path, except package-info.java
	 *
	 * Obs.: package-info.java present in
	 */
	@Deprecated
	public static File[] allJavaFilesIn2(String path) {

		File directory = new File(path);
		return directory.listFiles(
				(dir, name) -> name.endsWith(".java") && !name.equals("package-info.java")
		);
	}

	/**
	 * recursive
	 * @param rootPath to a package o directory
	 * @return all .java files inside directory of path, except package-info.java
	 *
	 * Obs.: package-info.java present in
	 */
	public static List<File> allJavaFilesIn(String rootPath) {

		File directory = new File(rootPath);

		List<File> allJavaFiles = new ArrayList<>();

		for (File path : directory.listFiles()) {
			if (path.isFile() && path.getName().endsWith(".java")
					&& !path.getName().equals("package-info.java")) {
				allJavaFiles.add(path);
			}
			else if (path.isDirectory()) {
				allJavaFiles.addAll(allJavaFilesIn(path.getPath()));
			}
		}

		return allJavaFiles;
	}
	
	public static Properties loadProperties(String pathToPropertiesFile) {
		
		Properties conf = new Properties();
		try {
			conf.load(new FileReader(pathToPropertiesFile));
			return conf;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void saveMutantsCatalog(String outputPath, String fileName,
			Map<String, List<String>> mutantsPerClass) {
		
		try {
			FileWriter fw = new FileWriter(new File(outputPath, fileName));
			BufferedWriter bw = new BufferedWriter(fw);
			
			for (Entry<String, List<String>> pair :  mutantsPerClass.entrySet()) {
				bw.write(pair.getKey());
				bw.write(":");
				bw.write(pair.getValue().toString());
				bw.newLine();
			}
//			bw.write(mutantsPerClass.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Issue #3
	 * @param outputPath
	 * @param fileName
	 * @param mutantsCatalog
	 */
	public static void saveMutantsCatalog(String outputPath, String fileName,
			List<MutationInfo> mutantsCatalog) {
		
		if (mutantsCatalog == null || mutantsCatalog.isEmpty()) return;
		
		try {
			FileWriter fw = new FileWriter(new File(outputPath, fileName));
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(MutationInfo.infoHeader());
			bw.newLine();
			
			for (MutationInfo mInfo : mutantsCatalog) {
				bw.write(mInfo.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Issue #4
	 * @param outputPath
	 * @param fileName
	 * @param groupsOfMutants
	 */
	public static void saveGroupsOfMutants(String outputPath,
			String fileName, List<List<String>> groupsOfMutants) {

		if (groupsOfMutants == null || groupsOfMutants.isEmpty()) return;
		
		try {
			FileWriter fw = new FileWriter(new File(outputPath, fileName));
			BufferedWriter bw = new BufferedWriter(fw);
			
			for (List<String> mGroup : groupsOfMutants) {
				bw.write(mGroup.get(0));
				
				for (int i=1; i < mGroup.size(); ++i) {
					bw.write(' ');
					bw.write(mGroup.get(i));
				}
				
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
