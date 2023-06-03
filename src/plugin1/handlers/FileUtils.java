package plugin1.handlers;

import org.json.JSONOBject;

import java.io.File;
import java.io.FileNotFoundException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

	public static Map<String, String> getPathsFromConfig(String configFilePath) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(configFilePath)));
		JSONObject jsonObject = new JSONObject(content);

		String targetFilePath = jsonObject.getString("targetFilePath");
		String folderPath = jsonObject.getString("folderPath");

		Map<String, String> paths = new HashMap<>();
		paths.put("targetFilePath", targetFilePath);
		paths.put("folderPath", folderPath);
		
		return paths;
	}
	
}
