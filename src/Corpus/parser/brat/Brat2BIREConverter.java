package Corpus.parser.brat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Corpus.AnnotatedDocument;
import Corpus.AnnotationConfig;
import Corpus.Corpus;
import Corpus.julie.Tokenization;
import Corpus.parser.ParsingUtils;
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

	private static final Comparator<BratTextBoundAnnotation> textBoundAnnotationComparator = new Comparator<BratTextBoundAnnotation>() {

		@Override
		public int compare(BratTextBoundAnnotation a1,
				BratTextBoundAnnotation a2) {
			return a1.getStart() - a2.getStart();
		}

	};

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
		Collection<BratAnnotation> allAnnotations = bratDoc.getAllAnnotations()
				.values();
		List<BratTextBoundAnnotation> textAnnotations = new ArrayList<BratTextBoundAnnotation>();
		List<BratEventAnnotation> eventAnnotations = new ArrayList<BratEventAnnotation>();

		for (BratAnnotation ann : allAnnotations) {
			if (ann instanceof BratTextBoundAnnotation) {
				textAnnotations.add((BratTextBoundAnnotation) ann);
			} else if (ann instanceof BratEventAnnotation) {
				eventAnnotations.add((BratEventAnnotation) ann);
			}
		}
		textAnnotations.sort(textBoundAnnotationComparator);

		for (Tokenization tokenization : tokenizations) {
			Log.d("Tokens: %s", tokenization.tokens);
			String content = tokenization.originalSentence;
			State state = new State();

			for (BratTextBoundAnnotation tann : textAnnotations) {
				if (isInSentence(tann, tokenization))
					convertTextBoundAnnotation(state, corpus.getCorpusConfig(),
							tokenization, tann);
			}
			for (BratEventAnnotation eann : eventAnnotations) {
				if (isInSentence(eann.getTrigger(), tokenization))
					convertEventAnnotation(state, corpus.getCorpusConfig(),
							tokenization, eann);
			}

			if (checkConsistency(state)) {
				AnnotatedDocument doc = new AnnotatedDocument(
						bratDoc.getTextFilename(), content,
						tokenization.tokens, state);
				doc.setCorpus(corpus);
				documents.add(doc);
			} else {
				Log.w("Skip inconsistent document \"%s\"",
						bratDoc.getAnnotationFilename());
			}
		}
		return documents;
	}

	// public static List<AnnotatedDocument> convert(
	// BratAnnotatedDocument bratDoc, Corpus corpus,
	// List<Tokenization> tokenizations) throws FileNotFoundException,
	// IOException {
	// List<AnnotatedDocument> documents = new ArrayList<AnnotatedDocument>();
	// Log.d("Split BratDocument %s in %s AnnotatedDocuments",
	// bratDoc.getTextFilename(), tokenizations.size());
	// for (Tokenization tokenization : tokenizations) {
	// String content = tokenization.originalSentence;
	// State state = new State();
	// Collection<BratAnnotation> allAnnotations = bratDoc
	// .getAllAnnotations().values();
	// for (BratAnnotation ann : allAnnotations) {
	// if (ann instanceof BratTextBoundAnnotation) {
	// BratTextBoundAnnotation tann = (BratTextBoundAnnotation) ann;
	// // if (isInSentence(tann, tokenization))
	// convertTextBoundAnnotation(state, corpus.getCorpusConfig(),
	// tokenization, tann);
	// } else if (ann instanceof BratEventAnnotation) {
	// BratEventAnnotation eann = (BratEventAnnotation) ann;
	// // if (isInSentence(eann.getTrigger(), tokenization))
	// convertEventAnnotation(state, corpus.getCorpusConfig(),
	// tokenization, eann);
	// } else if (ann instanceof BratRelationAnnotation) {
	// convertRelationAnnotation(state, corpus.getCorpusConfig(),
	// (BratRelationAnnotation) ann);
	// } else if (ann instanceof BratAttributeAnnotation) {
	// convertAttributeAnnotation(state, corpus.getCorpusConfig(),
	// (BratAttributeAnnotation) ann);
	// }
	// }
	// checkConsistency(state);
	// AnnotatedDocument doc = new AnnotatedDocument(
	// bratDoc.getTextFilename(), content, tokenization.tokens,
	// state);
	// doc.setCorpus(corpus);
	// documents.add(doc);
	// }
	// return documents;
	// }

	private static boolean checkConsistency(State state) {
		boolean isConsistent = true;
		Set<String> existingEntities = state.getEntityIDs();
		for (EntityAnnotation e : state.getEntities()) {
			for (String id : e.getArguments().values()) {
				if (!existingEntities.contains(id)) {
					Log.w("Entity %s references missing entity %s", e.getID(),
							id);
					isConsistent = false;
				}
			}
			// FIXME apparently some annotations span across multiple sentences
			// (PMID-8051172: E4 Trigger:T10 Theme:T5, where T5 and T10 are not
			// part of the same sentence)
			for (int index = e.getBeginTokenIndex(); index <= e
					.getEndTokenIndex(); index++) {
				if (!state.getAnnotationsForToken(index).contains(e.getID())) {
					Log.w("Entity %s references token %s, but state %s holds no reference to that",
							e.getID(), index, state.getID());
					isConsistent = false;
				}
			}
		}
		return isConsistent;
	}

	private static boolean isInSentence(BratTextBoundAnnotation ann,
			Tokenization tokenization) {
		return ann.getStart() >= tokenization.absoluteStartOffset
				&& ann.getEnd() <= tokenization.absoluteEndOffset;
	}

	private static void convertTextBoundAnnotation(State state,
			AnnotationConfig config, Tokenization tokenization,
			BratTextBoundAnnotation t) {
		EntityType entityType = config.getEntityType(t.getRole());
		int fromTokenIndex = findTokenForPosition(t.getStart(), tokenization);
		int toTokenIndex = findTokenForPosition(t.getEnd() - 1, tokenization);

		// only add if there is no other annotation occupying this token. This
		// should prevent adding a BratEventAnnotation along with the
		// corresponding TextBoundAnnotation
		// if (state.getAnnotationsForToken(fromTokenIndex).isEmpty()) {
		Log.d("---- Annotation: %s (%d-%d) ----", t.getText(), t.getStart(),
				t.getEnd());
		Log.d("\tSpanning tokens:");
		String total = "";
		for (int i = fromTokenIndex; i <= toTokenIndex; i++) {
			Log.d("\t\tTokens[%d]: %s", i, tokenization.tokens.get(i));
			total += tokenization.tokens.get(i).getText() + " ";
		}
		Log.d("\t# %s | %s", t.getText(), total);
		EntityAnnotation entity = new EntityAnnotation(state, t.getID());
		entity.init(entityType, fromTokenIndex, toTokenIndex);
		state.addEntityAnnotation(entity);
		// } else {
		// Log.d("Skipping annotation %s (\"%s\")", t.getID(), t.getText());
		// }
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
		// remove all previously assigned annotations to these tokens. This
		// should prevent to add BratEventAnnotations along with their
		// corresponding and overlapping TextBoudnAnnotations.
		// for (int i = fromTokenIndex; i <= toTokenIndex; i++) {
		// Set<String> entities = state.getAnnotationsForToken(i);
		// for (String id : entities) {
		// Log.d("Token %s already annotated by %s.\n\tRemove previous annotation!",
		// i, id);
		// state.removeEntityAnnotation(state.getEntity(id));
		// }
		// entities.clear();
		// }
		EntityAnnotation entity = new EntityAnnotation(state, e.getID());
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
		return ParsingUtils.binarySearch(sentenceLevelCharacterPosition,
				tokenization.tokens, true);
	}

}
