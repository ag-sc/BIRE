package Corpus.parser.brat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static String readFile(String filepath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
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

	public static void writeFile(String filepath, String content)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
		writer.write(content);
		writer.close();
	}
}
