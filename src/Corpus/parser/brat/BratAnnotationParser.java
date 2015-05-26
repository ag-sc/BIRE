package Corpus.parser.brat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratAttributeAnnotation;
import Corpus.parser.brat.annotations.BratEventAnnotation;
import Corpus.parser.brat.annotations.BratRelationAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;

public class BratAnnotationParser {

	private static final String COMMENT_INDICATOR = "#";
	BratAnnotationManager manager = new BratAnnotationManager();

	/*
	 * Collect all entity/relation types and their frequencies save to separate
	 * file
	 */
	public BratAnnotationManager parseFile(String filepath) {
		try {

			BufferedReader reader = new BufferedReader(new FileReader(filepath));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(COMMENT_INDICATOR)) {
					System.out.println("Skip comment: \"" + line + "\"");
				} else {
					parseLine(line);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return manager;
	}

	/**
	 * Delegates the parsing to the appropriate method that is capable of
	 * parsing this specific type of annotation. The decision to which method
	 * this line is delegated is made with the {@link Pattern} object in the
	 * BratAnnotation-Objects.
	 * 
	 * @param line
	 */
	private void parseLine(String line) {
		Matcher textMatcher = BratTextBoundAnnotation.pattern.matcher(line);

		if (textMatcher.matches()) {
			parseTextBoundAnnotation(line);
			return;
		}
		Matcher eventMatcher = BratEventAnnotation.pattern.matcher(line);
		if (eventMatcher.matches()) {
			parseEventAnnotation(line);
			return;
		}
		Matcher relationMatcher = BratRelationAnnotation.pattern.matcher(line);
		if (relationMatcher.matches()) {
			parseRelationAnnotation(line);
			return;
		}
		Matcher attributeMatcher = BratAttributeAnnotation.pattern
				.matcher(line);
		if (attributeMatcher.matches()) {
			parseAttributeAnnotation(line);
			return;
		}
	}

	private BratTextBoundAnnotation parseTextBoundAnnotation(String line) {
		StringTokenizer tabTokenizer = new StringTokenizer(line, "\t");

		String id = tabTokenizer.nextToken();

		StringTokenizer spaceTokenizer = new StringTokenizer(
				tabTokenizer.nextToken());
		String role = spaceTokenizer.nextToken();
		String start = spaceTokenizer.nextToken();
		String end = spaceTokenizer.nextToken();

		String text = tabTokenizer.nextToken();

		BratTextBoundAnnotation annotation = manager
				.getOrCreateTextBoundByID(id);
		annotation.init(role, Integer.parseInt(start), Integer.parseInt(end),
				text);
		return annotation;
	}

	private BratEventAnnotation parseEventAnnotation(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);

		String id = tokenizer.nextToken();
		String[] split = tokenizer.nextToken().split(":");
		String role = split[0];
		String triggerID = split[1];

		BratTextBoundAnnotation trigger = manager
				.getOrCreateTextBoundByID(triggerID);

		Map<String, BratAnnotation> arguments = extractArgumentsAsMap(tokenizer);

		BratEventAnnotation annotation = manager.getOrCreateEventByID(id);
		annotation.init(id, role, trigger, arguments);
		return annotation;
	}

	private BratRelationAnnotation parseRelationAnnotation(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);

		String id = tokenizer.nextToken();
		String role = tokenizer.nextToken();
		Map<String, BratAnnotation> arguments = extractArgumentsAsMap(tokenizer);

		BratRelationAnnotation annotation = manager.getOrCreateRelationByID(id);
		annotation.init(role, arguments);
		return annotation;
	}

	private BratAttributeAnnotation parseAttributeAnnotation(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);

		String id = tokenizer.nextToken();
		String role = tokenizer.nextToken();
		List<BratAnnotation> arguments = new ArrayList<BratAnnotation>();
		while (tokenizer.hasMoreElements()) {
			String argumentID = tokenizer.nextToken();
			BratAnnotation argument = manager.getOrCreateByID(argumentID);
			arguments.add(argument);
		}

		BratAttributeAnnotation annotation = manager
				.getOrCreateAttributeByID(id);
		annotation.init(role, arguments);

		return annotation;
	}

	private Map<String, BratAnnotation> extractArgumentsAsMap(
			StringTokenizer tokenizer) {
		Map<String, BratAnnotation> arguments = new HashMap<String, BratAnnotation>();
		while (tokenizer.hasMoreElements()) {
			String[] split = tokenizer.nextToken().split(":");
			BratAnnotation annotation = manager.getOrCreateByID(split[1]);
			arguments.put(split[0], annotation);
		}
		return arguments;
	}

}
