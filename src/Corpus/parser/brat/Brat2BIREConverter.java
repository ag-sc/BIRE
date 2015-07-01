package Corpus.parser.brat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Corpus.AnnotatedDocument;
import Corpus.AnnotationConfig;
import Corpus.Corpus;
import Corpus.Token;
import Corpus.julie.SentenceSplitter;
import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratAttributeAnnotation;
import Corpus.parser.brat.annotations.BratEventAnnotation;
import Corpus.parser.brat.annotations.BratRelationAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;
import de.julielab.jtbd.JTBDExtendedAPI;
import de.julielab.jtbd.TokenizedSentence;
import de.julielab.jtbd.Unit;

public class Brat2BIREConverter {

	/**
	 * Converts the Brat Annotations, which are stored in the
	 * {@link BratAnnotationManager}, to the BIRE format.
	 * 
	 * @param bratDoc
	 * @param corpus
	 * @param tokenizedTextFilepath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static AnnotatedDocument convert(BratAnnotatedDocument bratDoc,
			Corpus corpus, String splittedTextFilepath,
			String julieModelFilepath) throws FileNotFoundException,
			IOException {
		String content = bratDoc.getText();
		List<Token> tokens = extractTokens(splittedTextFilepath,
				julieModelFilepath);
		Log.d("Tokens: %s", tokens);
		State state = new State();
		Map<String, BratAnnotation> annotations = bratDoc.getAllAnnotations();
		for (BratAnnotation ann : annotations.values()) {
			if (ann instanceof BratTextBoundAnnotation) {
				convertTextBoundAnnotation(state, corpus.getCorpusConfig(),
						tokens, (BratTextBoundAnnotation) ann);
			} else if (ann instanceof BratEventAnnotation) {
				convertEventAnnotation(state, corpus.getCorpusConfig(), tokens,
						(BratEventAnnotation) ann);
			} else if (ann instanceof BratRelationAnnotation) {
				convertRelationAnnotation(state, corpus.getCorpusConfig(),
						(BratRelationAnnotation) ann);
			} else if (ann instanceof BratAttributeAnnotation) {
				convertAttributeAnnotation(state, corpus.getCorpusConfig(),
						(BratAttributeAnnotation) ann);
			}
		}
		AnnotatedDocument doc = new AnnotatedDocument(corpus, content, tokens,
				state);
		return doc;
	}

	private static List<Token> extractTokens(String splittedTextFilepath,
			String julieModelFilepath) throws FileNotFoundException,
			IOException {
		Log.d("tokenize with julie:");
		List<TokenizedSentence> tokenizedSentences = null;
		tokenizedSentences = JTBDExtendedAPI.tokenize(splittedTextFilepath,
				julieModelFilepath);
		List<String> sentences = SentenceSplitter.getSentencesAsList(new File(
				splittedTextFilepath));

		Log.d("julie-tokenized sentences:");
		int index = 0;
		List<Token> tokens = new ArrayList<Token>();
		int accumulatedSentenceLength = 0;
		for (int i = 0; i < tokenizedSentences.size(); i++) {
			TokenizedSentence ts = tokenizedSentences.get(i);
			Log.d("%s (#characters: %s)", ts.getOriginalSentence(), ts
					.getOriginalSentence().length());
			Log.d("\t%s", ts.getTokens());
			for (Unit u : ts.getTokens()) {
				String text = u.rep;
				int from = accumulatedSentenceLength + u.begin;
				int to = accumulatedSentenceLength + u.end;
				tokens.add(new Token(index, from, to, text));
				index++;
			}
			accumulatedSentenceLength += sentences.get(i).length() + 1;
		}
		return tokens;
	}

	// private List<Token> extractTokens(String content) {
	// List<Token> tokens = new ArrayList<Token>();
	// Matcher matcher = Pattern.compile("\\S+").matcher(content);
	//
	// int index = 0;
	// while (matcher.find()) {
	// String text = matcher.group();
	// int from = matcher.start();
	// int to = matcher.end();
	// boolean check = true;
	// while (check) {
	// check = false;
	// if (text.endsWith(".") || text.endsWith(",")) {
	// text = text.substring(0, text.length() - 1);
	// to--;
	// check = true;
	// }
	// if (text.startsWith("(") && text.endsWith(")")) {
	// text = text.substring(1, text.length() - 1);
	// from++;
	// to--;
	// check = true;
	// }
	// }
	//
	// Token token = new Token(index, from, to, text);
	// tokens.add(token);
	// index++;
	// }
	// return tokens;
	// }

	private static void convertTextBoundAnnotation(State state,
			AnnotationConfig config, List<Token> tokens,
			BratTextBoundAnnotation t) {
		EntityAnnotation entity = new EntityAnnotation(state, t.getID());
		EntityType entityType = config.getEntityType(t.getRole());
		int fromTokenIndex = findTokenForPosition(t.getStart(), tokens);
		int toTokenIndex = findTokenForPosition(t.getEnd() - 1, tokens);

		Log.d("---- Annotation: %s (%d-%d) ----", t.getText(), t.getStart(),
				t.getEnd());
		Log.d("\tSpanning from token at %d to token at %d", fromTokenIndex,
				toTokenIndex);
		Log.d("\tSpanning tokens:");
		String total = "";
		for (int i = fromTokenIndex; i <= toTokenIndex; i++) {
			Log.d("\t\tTokens[%d]: %s", i, tokens.get(i));
			total += tokens.get(i).getText() + " ";
		}
		Log.d("\t%s | %s", t.getText(), total);
		entity.init(entityType, fromTokenIndex, toTokenIndex);
		state.addEntityAnnotation(entity);
	}

	private static void convertEventAnnotation(State state,
			AnnotationConfig config, List<Token> tokens, BratEventAnnotation e) {
		Map<String, String> arguments = new HashMap<String, String>();
		for (Entry<String, BratAnnotation> entry : e.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			// Entities in the BIRE annotation implementation only keep weak
			// references (IDs) to other entities.
			arguments.put(entry.getKey(), ann.getID());
		}

		EntityAnnotation entity = new EntityAnnotation(state, e.getID());
		EntityType entityType = config.getEntityType(e.getRole());

		int fromTokenIndex = findTokenForPosition(e.getTrigger().getStart(),
				tokens);
		int toTokenIndex = findTokenForPosition(e.getTrigger().getEnd() - 1,
				tokens);
		Log.d("---- Annotation: %s (%d-%d) ----", e.getTrigger().getText(), e
				.getTrigger().getStart(), e.getTrigger().getEnd());

		Log.d("\tSpanning from token at %d to token at %d", fromTokenIndex,
				toTokenIndex);
		// System.out.println(String.format("Spanning tokens:"));
		String total = " ";
		for (int i = fromTokenIndex; i <= toTokenIndex; i++) {
			Log.d("\t\tTokens[%d]: %s", i, tokens.get(i));
			total += tokens.get(i).getText() + " ";
		}
		Log.d("\t%s | %s", e.getTrigger().getText(), total);
		entity.init(entityType, arguments, fromTokenIndex, toTokenIndex);
		state.addEntityAnnotation(entity);
	}

	private static void convertRelationAnnotation(State state,
			AnnotationConfig config, BratRelationAnnotation t) {
		Map<String, String> arguments = new HashMap<String, String>();
		for (Entry<String, BratAnnotation> entry : t.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			arguments.put(entry.getKey(), ann.getID());
		}
		EntityAnnotation entity = new EntityAnnotation(state, t.getID());
		EntityType entityType = config.getEntityType(t.getRole());
		/*
		 * TODO relations are not motivated by tokens in the text and thus have
		 * no start, end and text attribute. Here, these values are filled with
		 * placeholder values
		 */
		entity.init(entityType, arguments, -1, -1);
		state.addEntityAnnotation(entity);
	}

	private static void convertAttributeAnnotation(State state,
			AnnotationConfig config, BratAttributeAnnotation t) {
	}

	private static int findTokenForPosition(int characterPosition,
			List<Token> tokens) {
		return binarySearch(characterPosition, tokens);
	}

	/**
	 * Binary search on a list of Tokens. This functions tries to find the token
	 * in who's span of characters the specified character position falls in.
	 * When a token is found, the token's index in the given list is returned.
	 * Note that the upper bound of a tokens character span, which is given by
	 * token.geEnd(), is actually "the position of the last character + 1".
	 * 
	 * 
	 * @param characterPosition
	 * @param tokens
	 * @return
	 */
	private static int binarySearch(int characterPosition, List<Token> tokens) {
		int low = 0;
		int high = tokens.size() - 1;
		while (low <= high) {
			// Key is in a[lo..hi] or not present.
			int middle = low + (high - low) / 2;
			Token midToken = tokens.get(middle);
			if (characterPosition < midToken.getFrom())
				high = middle - 1;
			else if (characterPosition >= midToken.getTo())
				low = middle + 1;
			else
				return middle;
		}
		return -1;
	}
}
