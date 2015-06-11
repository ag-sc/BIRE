package Corpus.parser.brat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Corpus.AnnotationConfig;
import Corpus.Document;
import Corpus.Token;
import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratAttributeAnnotation;
import Corpus.parser.brat.annotations.BratEventAnnotation;
import Corpus.parser.brat.annotations.BratRelationAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;
import Variables.EntityAnnotation;
import Variables.EntityManager;
import Variables.EntityType;

public class Brat2BIREConverter {

	/**
	 * Converts the Brat Annotations, which are stored in the
	 * {@link BratAnnotationManager}, to the BIRE format.
	 * 
	 * @param bratDoc
	 * @param config
	 */
	public Document brat2BireAnnotations(BratAnnotatedDocument bratDoc,
			AnnotationConfig config) {
		String content = bratDoc.getText();
		List<Token> tokens = extractTokens(content);
		Map<Integer, Token> offsetToToken = new HashMap<Integer, Token>();
		for (Token token : tokens) {
			offsetToToken.put(token.getFrom(), token);
			System.out.println(token.getFrom() + ": " + token.getText());
		}

		EntityManager manager = new EntityManager();
		Map<String, BratAnnotation> annotations = bratDoc.getAllAnnotations();
		for (BratAnnotation ann : annotations.values()) {
			if (ann instanceof BratTextBoundAnnotation) {
				convertTextBoundAnnotation(manager, config, offsetToToken,
						(BratTextBoundAnnotation) ann);
			} else if (ann instanceof BratEventAnnotation) {
				convertEventAnnotation(manager, config, offsetToToken,
						(BratEventAnnotation) ann);
			} else if (ann instanceof BratRelationAnnotation) {
				convertRelationAnnotation(manager, config,
						(BratRelationAnnotation) ann);
			} else if (ann instanceof BratAttributeAnnotation) {
				convertAttributeAnnotation(manager, config,
						(BratAttributeAnnotation) ann);
			}
		}
		Document doc = new Document(null, content, tokens, manager);
		return doc;
	}

	private List<Token> extractTokens(String content) {
		List<Token> tokens = new ArrayList<Token>();
		Matcher matcher = Pattern.compile("\\S+").matcher(content);

		int index = 0;
		while (matcher.find()) {
			String text = matcher.group();
			int from = matcher.start();
			int to = matcher.end();
			boolean check = true;
			while (check) {
				check = false;
				if (text.endsWith(".") || text.endsWith(",")) {
					text = text.substring(0, text.length() - 1);
					to--;
					check = true;
				}
				if (text.startsWith("(") && text.endsWith(")")) {
					text = text.substring(1, text.length() - 1);
					from++;
					to--;
					check = true;
				}
			}

			Token token = new Token(index, from, to, text);
			tokens.add(token);
			index++;
		}
		return tokens;
	}

	private void convertTextBoundAnnotation(EntityManager manager,
			AnnotationConfig config, Map<Integer, Token> offsetToToken,
			BratTextBoundAnnotation t) {
		EntityAnnotation entity = new EntityAnnotation(manager, t.getID());
		EntityType entityType = config.getEntityType(t.getRole());

		Token token = offsetToToken.get(t.getStart());
		System.out.println(t.getStart() + "(" + t.getText() + "): ");
		System.out.println("|------" + token.getIndex() + ", "
				+ token.getText());
		entity.init(entityType, token.getIndex(), token.getIndex());
		// entities.put(t.getID(), entity);
		manager.addEntityAnnotation(entity);
	}

	private void convertEventAnnotation(EntityManager manager,
			AnnotationConfig config, Map<Integer, Token> offsetToToken,
			BratEventAnnotation e) {
		Map<String, String> arguments = new HashMap<String, String>();
		for (Entry<String, BratAnnotation> entry : e.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			// Entities in the BIRE annotation implementation only keep weak
			// references (IDs) to other entities.
			arguments.put(entry.getKey(), ann.getID());
		}
		EntityAnnotation entity = new EntityAnnotation(manager, e.getID());
		EntityType entityType = config.getEntityType(e.getRole());

		Token token = offsetToToken.get(e.getTrigger().getStart());
		System.out.println(e.getTrigger().getStart() + "(" + e.getTrigger().getText() + "): ");
		System.out.println("|------" + token.getIndex() + ", "
				+ token.getText());

		entity.init(entityType, arguments, token.getIndex(), token.getIndex());
		manager.addEntityAnnotation(entity);
	}

	private void convertRelationAnnotation(EntityManager manager,
			AnnotationConfig config, BratRelationAnnotation t) {
		Map<String, String> arguments = new HashMap<String, String>();
		for (Entry<String, BratAnnotation> entry : t.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			arguments.put(entry.getKey(), ann.getID());
		}
		EntityAnnotation entity = new EntityAnnotation(manager, t.getID());
		EntityType entityType = config.getEntityType(t.getRole());
		/*
		 * TODO relations are not motivated by tokens in the text and thus have
		 * no start, end and text attribute. Here, these values are filled with
		 * placeholder values
		 */
		entity.init(entityType, arguments, -1, -1);
		manager.addEntityAnnotation(entity);
	}

	private void convertAttributeAnnotation(EntityManager manager,
			AnnotationConfig config, BratAttributeAnnotation t) {
		// TODO attributes are currently neglected
	}
}
