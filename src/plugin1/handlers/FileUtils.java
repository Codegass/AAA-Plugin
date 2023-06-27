package plugin1.handlers;

import java.io.File;

public class FileUtils {
	private static final String[] testKeyWords = { "tests", "test", "testing", "tester" };
	
	public static boolean isTest(File file) {
		for (String keyword : testKeyWords)
			if (file.getAbsolutePath().toLowerCase().contains(keyword + File.separatorChar) && file.getAbsolutePath().endsWith(".java")) return true;
		return false;
	}

	public static boolean isProduction(File file) {
		for (String keyword : testKeyWords)
			if (file.getAbsolutePath().toLowerCase().contains(keyword + File.separatorChar)) return false;
		return file.getAbsolutePath().endsWith(".java");
	}
	
}
