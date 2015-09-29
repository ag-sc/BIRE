package Corpus.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

	public static String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder builder = new StringBuilder();
		String line;
		boolean addPreviousLinebreak = false;
		while ((line = reader.readLine()) != null) {
			if (addPreviousLinebreak)
				builder.append("\n");
			builder.append(line);
			addPreviousLinebreak = true;
		}
		reader.close();
		return builder.toString();
	}

	public static List<String> readLines(String filepath) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		reader.close();
		return lines;
	}

	public static void writeFile(File file, String content) throws IOException {
		writeFile(file, content, false);
	}

	public static void writeFile(File file, String content, boolean append) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
		writer.write(content);
		writer.close();
	}

	public static String getFilenameWithoutExtension(String name) {

		return name.replaceFirst("[.][^.]+$", "");
	}

	public static void makeParents(File file) throws IOException {
		File destDir = file.getParentFile();
		if (!destDir.exists() && !destDir.mkdirs()) {
			throw new IOException("Couldn't create dir: " + destDir);
		}
	}

	public static List<File> getFiles(File dir, String fileExtension) {
		File[] files = dir.listFiles((f, s) -> s.endsWith(fileExtension));
		return Arrays.asList(files);
	}
}
