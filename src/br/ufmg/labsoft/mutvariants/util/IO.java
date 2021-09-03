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
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;

import br.ufmg.labsoft.mutvariants.entity.MutationInfo;

public class IO {

	public static List<String> getPaths(String base, String paths) {

		List<String> pathsList = new ArrayList<String>();
		String[] tempPaths = paths.split(",");

		for (String tempPath : tempPaths) {
			pathsList.add(base + tempPath);
		}

		return pathsList;
	}

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

		// AnnotationDeclaration, ClassOrInterfaceDeclaration, EnumDeclaration
		NodeList<TypeDeclaration<?>> types = compUn.getTypes();
		String fileName = null;

		if (types.size() == 1) {
			//if a CompilationUnit has only one class, it cab be non public
			fileName = compUn.getTypes().get(0).getNameAsString();
		}
		else {
			//otherwise, a CompilationUnit may have a public class 
			fileName = types.stream().filter(c -> c.getModifiers().contains(Modifier.PUBLIC)).findFirst().get().getNameAsString();
		}

		String packageDirectories = compUn.getPackageDeclaration().get().getNameAsString().replaceAll("\\.", File.separator);

		return writeCompilationUnit(compUn, new File(outputDirectory, packageDirectories), fileName + ".java");
	}

	/**
	 * recursive
	 * @param rootPath to a package o directory
	 * @return all .java files inside directory of path,
	 * except package-info.java and module-info.java
	 */
	public static List<File> allJavaFilesIn(String rootPath) {

		File directory = new File(rootPath);

		List<File> allJavaFiles = new ArrayList<>();

		for (File path : directory.listFiles()) {
			if (path.isFile() && path.getName().endsWith(".java")
					&& !path.getName().equals("package-info.java")
					&& !path.getName().equals("module-info.java")) {
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

	@Deprecated
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

	/**
	 * Issue #2
	 * @param outputPath
	 * @param nestedMutantsInfoFileName
	 * @param nestedMutantInfo
	 */
	public static void saveNestedMutantsInfo(String outputPath, String fileName, 
			Map<String, Set<String>> nestedMutantInfo) {

		if (nestedMutantInfo == null || nestedMutantInfo.isEmpty()) return;

		try {
			FileWriter fw = new FileWriter(new File(outputPath, fileName));
			BufferedWriter bw = new BufferedWriter(fw);

			for (Entry<String, Set<String>> entry : nestedMutantInfo.entrySet()) {
				bw.write(entry.getKey());
				bw.write(":");

				for (String mut : entry.getValue()) {
					bw.write(" ");
					bw.write(mut);
				}

				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
