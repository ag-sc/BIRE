package Corpus.parser.brat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Corpus.AnnotatedDocument;
import Corpus.AnnotationConfig;
import Corpus.Corpus;
import Corpus.Token;
import Corpus.julie.Tokenization;
import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratAttributeAnnotation;
import Corpus.parser.brat.annotations.BratEventAnnotation;
import Corpus.parser.brat.annotations.BratRelationAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;

public class Brat2BIREConverter {

	/**
	 * Converts the Brat Annotations, which are stored in the
	 * {@link BratAnnotationManager}, to the BIRE format. For each sentence in
	 * the given BratDocument/sentence file, an AnnotatedDocument is returned.
	 * 
	 * @param bratDoc
	 * @param corpus
	 * @param sentFile
	 * @param tokenizedTextFilepath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<AnnotatedDocument> convert(
			BratAnnotatedDocument bratDoc, Corpus corpus,
			List<Tokenization> tokenizations) throws FileNotFoundException,
			IOException {
		List<AnnotatedDocument> documents = new ArrayList<AnnotatedDocument>();
		Log.d("Split BratDocument %s in %s AnnotatedDocuments",
				bratDoc.getTextFilename(), tokenizations.size());
		for (Tokenization tokenization : tokenizations) {
			String content = tokenization.originalSentence;
			State state = new State();
			Collection<BratAnnotation> allAnnotations = bratDoc
					.getAllAnnotations().values();
			for (BratAnnotation ann : allAnnotations) {
				if (ann instanceof BratTextBoundAnnotation) {
					BratTextBoundAnnotation tann = (BratTextBoundAnnotation) ann;
					if (isInSentence(tann, tokenization))
						convertTextBoundAnnotation(state,
								corpus.getCorpusConfig(), tokenization, tann);
				} else if (ann instanceof BratEventAnnotation) {
					BratEventAnnotation eann = (BratEventAnnotation) ann;
					if (isInSentence(eann.getTrigger(), tokenization))
						convertEventAnnotation(state, corpus.getCorpusConfig(),
								tokenization, eann);
				} else if (ann instanceof BratRelationAnnotation) {
					convertRelationAnnotation(state, corpus.getCorpusConfig(),
							(BratRelationAnnotation) ann);
				} else if (ann instanceof BratAttributeAnnotation) {
					convertAttributeAnnotation(state, corpus.getCorpusConfig(),
							(BratAttributeAnnotation) ann);
				}
			}
			AnnotatedDocument doc = new AnnotatedDocument(
					bratDoc.getTextFilename(), content, tokenization.tokens,
					state);
			doc.setCorpus(corpus);
			documents.add(doc);
		}
		return documents;
	}

	private static boolean isInSentence(BratTextBoundAnnotation ann,
			Tokenization tokenization) {
		return ann.getStart() >= tokenization.absoluteStartOffset
				&& ann.getEnd() <= tokenization.absoluteEndOffset;
	}

	private static void convertTextBoundAnnotation(State state,
			AnnotationConfig config, Tokenization tokenization,
			BratTextBoundAnnotation t) {
		EntityAnnotation entity = new EntityAnnotation(state, t.getID());
		EntityType entityType = config.getEntityType(t.getRole());
		int fromTokenIndex = findTokenForPosition(t.getStart(), tokenization);
		int toTokenIndex = findTokenForPosition(t.getEnd() - 1, tokenization);

		Log.d("---- Annotation: %s (%d-%d) ----", t.getText(), t.getStart(),
				t.getEnd());
		Log.d("\tSpanning tokens:");
		String total = "";
		for (int i = fromTokenIndex; i <= toTokenIndex; i++) {
			Log.d("\t\tTokens[%d]: %s", i, tokenization.tokens.get(i));
			total += tokenization.tokens.get(i).getText() + " ";
		}
		Log.d("\t# %s | %s", t.getText(), total);
		entity.init(entityType, fromTokenIndex, toTokenIndex);
		state.addEntityAnnotation(entity);
	}

	private static void convertEventAnnotation(State state,
			AnnotationConfig config, Tokenization tokenization,
			BratEventAnnotation e) {
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
				tokenization);
		int toTokenIndex = findTokenForPosition(e.getTrigger().getEnd() - 1,
				tokenization);
		Log.d("---- Annotation: %s (%d-%d) ----", e.getTrigger().getText(), e
				.getTrigger().getStart(), e.getTrigger().getEnd());

		 Log.d("\tSpanning tokens:");
		String total = " ";
		for (int i = fromTokenIndex; i <= toTokenIndex; i++) {
			Log.d("\t\tTokens[%d]: %s", i, tokenization.tokens.get(i));
			total += tokenization.tokens.get(i).getText() + " ";
		}
		Log.d("\t# %s | %s", e.getTrigger().getText(), total);
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

	private static int findTokenForPosition(int documentLevelCharacterPosition,
			Tokenization tokenization) {
		int sentenceLevelCharacterPosition = documentLevelCharacterPosition
				- tokenization.absoluteStartOffset;
		return binarySearch(sentenceLevelCharacterPosition, tokenization.tokens);
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
