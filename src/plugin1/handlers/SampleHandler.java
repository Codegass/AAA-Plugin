package plugin1.handlers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;

public class SampleHandler extends AbstractHandler {

	private static CSVWriter writer;
	private static CSVReader reader;
	private final static File input = new File("/Users/chenhao/Documents/Research/AAA Plugin/target.csv");
	private final static String outputBase = "/Users/chenhao/Documents/Research/AAA Plugin/tag-sheet/";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		try {
			fun1();
		} catch (CsvValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CsvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageDialog.openInformation(window.getShell(), "Plugin1", "Hello, Eclipse world");

		return null;
	}

	private void fun1() throws IOException, CsvException {
		FileReader inputfile = new FileReader(input);
		// create CSVWriter object filewriter object as parameter
	
		RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
		reader = new CSVReaderBuilder(inputfile).withCSVParser(rfc4180Parser).build();
		
		List<String[]> lines = reader.readAll();
		int count = 0;
		System.out.println("CSV Reading Finished.");
		for (String[] line : lines) {
			featureExtraction(line, count++);
		}
	}
	
	
	private void featureExtraction(String[] line, int count) throws IOException {
		File fileName = new File(line[0]);
		System.out.println(fileName);
		String className = line[1].split(":")[0].trim();
		String methodName = line[1].split(":")[1].trim();
		
		System.out.println(className);
		System.out.println(methodName);
		
//		Set<String> targets = new HashSet<>();
//		Map<String, Integer> map = new HashMap<>();
		List<String> visited = new ArrayList<>();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath path = Path.fromOSString(fileName.getAbsolutePath());
		System.out.println(path);
		IFile file = workspace.getRoot().getFileForLocation(path);
		ICompilationUnit unit = (ICompilationUnit) JavaCore.create(file);
		System.out.println("ICompilationUnit: " + unit);
		CompilationUnit cu = parse(unit);
		System.out.println("CompilationUnit: " + cu);

		
		MethodVisitor visitor = new MethodVisitor();
		cu.accept(visitor);
		
		for (MethodDeclaration md : visitor.getMethods()) {
			if (md.getName().toString().equals(methodName)) {
				System.out.println("begin dfs");
				try {
					dfs(md, 1, visited);
				} catch (Exception e){
					e.printStackTrace();
				}
				System.out.println("end dfs");
				break;
			}
		}
		
		// open
		File outputFile = new File(outputBase + Integer.toString(count) + "_" + className + "." + methodName + ".csv");
		outputFile.createNewFile(); 
		FileWriter outputfile = new FileWriter(outputFile);
		writer = new CSVWriter(outputfile);
		// header
//		String[] absInfo = new String[] {"Test Type(unit test, somke test, intergration test, other)", "Contains Assertion", "Comment/Javadoc is meanningful", "Naming Convention is meanningful", "Test target related to dynamic binding", "Contains mock"};
//		String[] blankLine = new String[] {""};
		String[] header = new String[] {"testPackage", "testClassName", "testMethodName", "potentialTargetQualifiedName", "AAA(0,1,2)", "isTarget", "isMock", "Assert Distance", "Level", "Name Similarity"};
		// write
//		writer.writeNext(absInfo);
//		writer.writeNext(blankLine);
//		writer.writeNext(blankLine);
		writer.writeNext(header);
		// result
		List<String[]> result = generateOutput(className, methodName, cu.getPackage().getName().toString(), visited);
		for (String[] next : result) {
			writer.writeNext(next);
		}
		// close
		writer.close();
		
		System.out.println("end of feature extraction");
	}
	
	private static void dfs(MethodDeclaration mdc, int level, List<String> visited) {
		InvocationVisitor visitor = new InvocationVisitor();
		if (mdc != null) {System.out.println("mdc is not null");}
		mdc.accept(visitor);
		
		for (ASTNode node : visitor.getMethods()) {			
			if (node instanceof MethodInvocation){	
				try {
					MethodInvocation mi = (MethodInvocation) node;
					if (mi != null) { System.out.println("mi is not null");}
					IMethodBinding binding = mi.resolveMethodBinding();
					System.out.println("MethodInvocation: " + mi);
					if (binding == null) { System.out.println("bing is null");}

					if (isAssert(mi)) {
						visited.add(getSpace(level) + "ASSERT " + binding.getDeclaringClass().getQualifiedName() + "." + mi.getName().toString() + getParameters(binding));
						continue;
					}
					
					if (isJunitExpectedException(mi)) {
						visited.add(getSpace(level) + "ExpectedException " + binding.getDeclaringClass().getQualifiedName() + "." + mi.getName().toString() + getParameters(binding));
						continue;
					}
					
					if (isEasyMock(mi)) {
						visited.add(getSpace(level) + "MOCK " + binding.getDeclaringClass().getQualifiedName() + "." + mi.getName().toString() + getParameters(binding));
						continue;
					}
					
					ICompilationUnit unit = (ICompilationUnit) binding.getJavaElement().getAncestor( IJavaElement.COMPILATION_UNIT );
					
					if (unit == null) { continue; }
					
					CompilationUnit cu = parse(unit);
					MethodDeclaration md = (MethodDeclaration) cu.findDeclaringNode( binding.getKey() );
					
					// if MethodDeclaration is null, give another try
					if (md == null) {
						md = getMD(binding, cu);
					}
					// if MethodDeclaration is still null, skip
					if (md == null) {
						continue;
					}
					
					if (FileUtils.isProduction(unit.getPath().makeAbsolute().toFile())) {
						String qualifiedName = md.resolveBinding().getDeclaringClass().getQualifiedName() + "." + md.getName().toString();
						visited.add(getSpace(level) + qualifiedName + getParameters(binding));
					} else if (FileUtils.isTest(unit.getPath().makeAbsolute().toFile())){
						String qualifiedName = md.resolveBinding().getDeclaringClass().getQualifiedName() + "." + md.getName().toString();
						boolean isVisited = isVisited(level, visited, "TEST " + qualifiedName + getParameters(binding));
						visited.add(getSpace(level) + "TEST " + qualifiedName + getParameters(binding));
						if (!isVisited) {
							dfs(md, level + 1,  visited);
						}						
					}

				} catch (Exception ex) {
					// TODO: handle exception
//					ex.printStackTrace();
				}
				
				
			} else if (node instanceof ClassInstanceCreation){
				try {
					ClassInstanceCreation ci = (ClassInstanceCreation) node;
					IMethodBinding binding = ci.resolveConstructorBinding();
					ITypeBinding declaringClass = binding.getDeclaringClass();
										
					// jdk
					if (declaringClass.getQualifiedName().startsWith("java.")) {
						continue;
					}
					
					// anonymous
					if (declaringClass.isAnonymous()) {
						visited.add(getSpace(level) + "NEW anonymous" + getParameters(binding));
					} else {
						visited.add(getSpace(level) + "NEW " + declaringClass.getQualifiedName() + getParameters(binding));
					}
					
				} catch (Exception e) {
					System.out.println("constructor not resolvable");
				}
			} else if (node instanceof ExpressionMethodReference) {
				try {
					ExpressionMethodReference mr = (ExpressionMethodReference) node;
					
					IMethodBinding binding = mr.resolveMethodBinding();

					String qualifiedName = binding.getDeclaringClass().getQualifiedName() + "." + binding.getName();
					visited.add(getSpace(level) + qualifiedName + getParameters(binding));
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (node instanceof NormalAnnotation) {
				try {
					NormalAnnotation na = (NormalAnnotation) node;
					List<MemberValuePair> values = na.values();
		        	for (MemberValuePair v : values) {
		        		if (v.getName().toString().equals("expected")) {
							String qualifiedName = v.getValue().resolveTypeBinding().getQualifiedName();
							visited.add(getSpace(level) + "@EXPECTED " + qualifiedName);
		        		}
		        	}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	private static boolean isEasyMock(MethodInvocation mi) {
		if (mi.resolveMethodBinding().getDeclaringClass().getQualifiedName().startsWith("org.easymock")) {
			return true;
		}
		
		return false;
	}

	private static boolean isVisited(int level, List<String> visited, String str) {
		for (int i = level; i >=0; i--) {
			if (visited.contains(getSpace(i) + str)) {
				return true;
			}
		}
		
		return false;
	}

	private static String getParameters(IMethodBinding binding) {
		String parameters = "(";
		
		for (ITypeBinding p : binding.getParameterTypes()) {
			parameters = parameters + p.getName() + ", ";
		}
		
		if (parameters.length() > 1) {
			parameters = parameters.substring(0, parameters.length() - 2);
		}
		
		parameters = parameters + ")";
		
		return parameters;
	}
	
	private static  String getSpace(int count) {
		String space = "";
		for (int i = 1; i < count; i++) {
			space += "     ";
		}
		return space;
	}
	
	private static CompilationUnit getCU(MethodDeclaration mdc) {
		ICompilationUnit unit = (ICompilationUnit) mdc.resolveBinding().getJavaElement().getAncestor(IJavaElement.COMPILATION_UNIT);
		CompilationUnit cu = parse(unit);
		return cu;
	}
	
	private static MethodDeclaration getMD(IMethodBinding binding, CompilationUnit cu) {
		try {
			GenericMethodVisitor visitor = new GenericMethodVisitor(binding);
			cu.accept(visitor);
			return visitor.getMethod();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static CompilationUnit getCompilationUnit(IJavaProject project, IMember member) {
		ASTParser parser = ASTParser.newParser(AST.JLS14);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setProject(project);
		parser.setSource(member.getCompilationUnit());
		return (CompilationUnit) parser.createAST(null);
	}
	
	private static boolean isAssert(MethodInvocation mc) {
		if (mc.resolveMethodBinding().getDeclaringClass().getQualifiedName().contains(".Assert")) {
			return true;
		}
		
		return false;
	}
	
	private static boolean isJunitExpectedException(MethodInvocation mi) {
		if (mi.resolveMethodBinding().getDeclaringClass().getQualifiedName().equals("org.junit.rules.ExpectedException")) {
			return true;
		}
		
		return false;
	}

	private static List<String[]> generateOutput(String className, String methodName, String packageName, List<String> visited) {
		List<String[]> result = new ArrayList<>();
		for (String method : visited) {
			if (method.trim().startsWith("ASSERT")) {
				result.add(new String[] {packageName, className, methodName, method, "2"});
			} else {
				result.add(new String[] {packageName, className, methodName, method});
			}
			
		}
		return result;
	}

	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the Java
	 * source file
	 *
	 * @param unit
	 * @return
	 */

	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS14);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
}
