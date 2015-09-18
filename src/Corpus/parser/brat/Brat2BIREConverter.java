package Corpus.parser.brat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import Corpus.AnnotationConfig;
import Corpus.Corpus;
import Corpus.SubDocument;
import Corpus.julie.Tokenization;
import Corpus.parser.ParsingUtils;
import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratAttributeAnnotation;
import Corpus.parser.brat.annotations.BratEventAnnotation;
import Corpus.parser.brat.annotations.BratRelationAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;
import Logging.Log;
import Variables.ArgumentRole;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;
import utility.EntityID;

public class Brat2BIREConverter {

	private static final Comparator<BratTextBoundAnnotation> textBoundAnnotationComparator = new Comparator<BratTextBoundAnnotation>() {

		@Override
		public int compare(BratTextBoundAnnotation a1, BratTextBoundAnnotation a2) {
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
	public static List<SubDocument> convert(BratAnnotatedDocument bratDoc, Corpus<SubDocument> corpus,
			List<Tokenization> tokenizations) throws FileNotFoundException, IOException {
		List<SubDocument> documents = new ArrayList<SubDocument>();
		Log.d("Split BratDocument %s in %s documents", bratDoc.getDocumentName(), tokenizations.size());
		Collection<BratAnnotation> allAnnotations = bratDoc.getAllAnnotations().values();
		List<BratTextBoundAnnotation> textAnnotations = new ArrayList<BratTextBoundAnnotation>();
		List<BratEventAnnotation> eventAnnotations = new ArrayList<BratEventAnnotation>();

		for (BratAnnotation ann : allAnnotations) {
			if (ann instanceof BratTextBoundAnnotation) {
				textAnnotations.add((BratTextBoundAnnotation) ann);
			} else if (ann instanceof BratEventAnnotation) {
				eventAnnotations.add((BratEventAnnotation) ann);
			}
		}
		Collections.sort(textAnnotations, textBoundAnnotationComparator);

		int sentenceNumber = 0;
		for (Tokenization tokenization : tokenizations) {
			Log.d("Tokens: %s", tokenization.tokens);
			SubDocument doc = new SubDocument(corpus, bratDoc.getDocumentName(),
					bratDoc.getDocumentName() + "-" + sentenceNumber, tokenization.originalSentence,
					tokenization.tokens, tokenization.absoluteStartOffset);

			State state = new State(doc);
			for (BratTextBoundAnnotation tann : textAnnotations) {
				if (isInSentence(tann, tokenization)) {
					convertTextBoundAnnotation(state, corpus.getCorpusConfig(), tokenization, tann);
					// if (doc.getName().equals("PMC-3062687-05-Results-2")) {
					// if (tann.getID().equals("T54")) {
					// Log.d("%s:\t%s", tann.getID(), tann);
					// System.in.read();
					// }
					// }
				}
			}
			for (BratEventAnnotation eann : eventAnnotations) {
				if (isInSentence(eann.getTrigger(), tokenization)) {
					convertEventAnnotation(state, corpus.getCorpusConfig(), tokenization, eann);
					// if (doc.getName().equals("PMC-3062687-05-Results-2")) {
					// if (eann.getID().equals("E6")) {
					// Log.d("%s:\t%s", eann.getID(), eann);
					// System.in.read();
					// }
					// }
				}
			}

			if (checkConsistency(state)) {
				doc.setGoldState(state);
				doc.setInitialState(new State(doc));
				documents.add(doc);
			} else {
				Log.w("Skip inconsistent document \"%s\"", bratDoc.getDocumentName());
			}
			sentenceNumber++;
		}
		return documents;
	}

	private static boolean checkConsistency(State state) {
		boolean isConsistent = true;
		Set<EntityID> existingEntities = state.getEntityIDs();
		for (EntityAnnotation e : state.getEntities()) {
			for (EntityID id : e.getArguments().values()) {
				if (!existingEntities.contains(id)) {
					Log.w("Entity %s references missing entity %s", e.getID(), id);
					isConsistent = false;
				}
			}
			// FIXME apparently some annotations span across multiple sentences
			// (PMID-8051172: E4 Trigger:T10 Theme:T5, where T5 and T10 are not
			// part of the same sentence)
			for (int index = e.getBeginTokenIndex(); index < e.getEndTokenIndex(); index++) {
				if (!state.getAnnotationsForToken(index).contains(e.getID())) {
					Log.w("Entity %s references token %s, but state %s holds no reference to that", e.getID(), index,
							state.getID());
					isConsistent = false;
				}
			}
		}
		return isConsistent;
	}

	private static boolean isInSentence(BratTextBoundAnnotation ann, Tokenization tokenization) {
		return tokenization.absoluteStartOffset <= ann.getStart() && ann.getEnd() < tokenization.absoluteEndOffset;
	}

	private static void convertTextBoundAnnotation(State state, AnnotationConfig config, Tokenization tokenization,
			BratTextBoundAnnotation t) {
		EntityType entityType = config.getEntityType(t.getRole());
		if (entityType == null)
			throw new IllegalStateException(String.format("No entity type provided for \"%s\".", t.getRole()));
		int fromTokenIndex = findTokenForPosition(t.getStart(), tokenization, true);
		int toTokenIndex = findTokenForPosition(t.getEnd(), tokenization, false) + 1;

		// only add if there is no other annotation occupying this token. This
		// should prevent adding a BratEventAnnotation along with the
		// corresponding TextBoundAnnotation
		// if (state.getAnnotationsForToken(fromTokenIndex).isEmpty()) {
		// Log.d("\tSpanning tokens:");
		// String total = "";
		// for (int i = fromTokenIndex; i < toTokenIndex; i++) {
		// Log.d("\t\tTokens[%d]: %s", i, tokenization.tokens.get(i));
		// total += tokenization.tokens.get(i).getText() + " ";
		// }
		// Log.d("\t# %s | %s", t.getText(), total);
		EntityAnnotation entity = new EntityAnnotation(state, t.getID(), entityType, fromTokenIndex, toTokenIndex);

		Log.d("Text Annotation: \"%s\" -> \"%s\"", t.getText(), entity.getText());
		Log.d("-- Character span %s-%s (%s-%s) -> token span %s-%s", t.getStart(), t.getEnd(),
				t.getStart() - tokenization.absoluteStartOffset, t.getEnd() - tokenization.absoluteStartOffset,
				fromTokenIndex, toTokenIndex);
		if (!t.getText().equals(entity.getText())) {
			Log.w("Text representations of character-level and token-level do not match: \"%s\" != \"%s\"", t.getText(),
					entity.getText());
		}
		state.addEntity(entity);
		// } else {
		// Log.d("Skipping annotation %s (\"%s\")", t.getID(), t.getText());
		// }
	}

	private static void convertEventAnnotation(State state, AnnotationConfig config, Tokenization tokenization,
			BratEventAnnotation e) {
		Multimap<ArgumentRole, EntityID> arguments = HashMultimap.create();
		for (Entry<String, BratAnnotation> entry : e.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			// Entities in the BIRE annotation implementation only keep weak
			// references (IDs) to other entities.
			arguments.put(new ArgumentRole(entry.getKey()), new EntityID(ann.getID()));
		}

		EntityType entityType = config.getEntityType(e.getRole());
		if (entityType == null)
			throw new IllegalStateException(String.format("No entity type provided for \"%s\".", e.getRole()));

		int fromTokenIndex = findTokenForPosition(e.getTrigger().getStart(), tokenization, true);
		int toTokenIndex = findTokenForPosition(e.getTrigger().getEnd(), tokenization, false) + 1;
		// Log.d("---- Annotation: %s (%d-%d) ----", e.getTrigger().getText(),
		// e.getTrigger().getStart(),
		// e.getTrigger().getEnd());

		// Log.d("\tSpanning tokens:");
		// String total = " ";
		// for (int i = fromTokenIndex; i < toTokenIndex; i++) {
		// Log.d("\t\tTokens[%d]: %s", i, tokenization.tokens.get(i));
		// total += tokenization.tokens.get(i).getText() + " ";
		// }
		// Log.d("\t# %s | %s", e.getTrigger().getText(), total);
		// remove all previously assigned annotations to these tokens. This
		// should prevent to add BratEventAnnotations along with their
		// corresponding and overlapping TextBoudnAnnotations.
		// for (int i = fromTokenIndex; i <= toTokenIndex; i++) {
		// Set<String> entities = state.getAnnotationsForToken(i);
		// for (String id : entities) {
		// Log.d("Token %s already annotated by %s.\n\tRemove previous
		// annotation!",
		// i, id);
		// state.removeEntityAnnotation(state.getEntity(id));
		// }
		// entities.clear();
		// }
		EntityAnnotation entity = new EntityAnnotation(state, e.getID(), entityType, arguments, fromTokenIndex,
				toTokenIndex);
		Log.d("Event Annotation: \"%s\" -> \"%s\"", e.getTrigger().getText(), entity.getText());
		Log.d("-- Character span %s-%s (%s-%s) -> token span %s-%s", e.getTrigger().getStart(), e.getTrigger().getEnd(),
				e.getTrigger().getStart() - tokenization.absoluteStartOffset,
				e.getTrigger().getEnd() - tokenization.absoluteStartOffset, fromTokenIndex, toTokenIndex);
		if (!e.getTrigger().getText().equals(entity.getText())) {
			Log.w("Text representations of character-level and token-level do not match: \"%s\" -> \"%s\"",
					e.getTrigger().getText(), entity.getText());
		}
		state.addEntity(entity);
	}

	private static void convertRelationAnnotation(State state, AnnotationConfig config, BratRelationAnnotation t) {
		Multimap<ArgumentRole, EntityID> arguments = HashMultimap.create();
		for (Entry<String, BratAnnotation> entry : t.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			arguments.put(new ArgumentRole(entry.getKey()), new EntityID(ann.getID()));
		}
		EntityType entityType = config.getEntityType(t.getRole());
		/*
		 * TODO relations are not motivated by tokens in the text and thus have
		 * no start, end and text attribute. Here, these values are filled with
		 * placeholder values
		 */
		EntityAnnotation entity = new EntityAnnotation(state, t.getID(), entityType, arguments, -1, -1);
		state.addEntity(entity);
	}

	private static void convertAttributeAnnotation(State state, AnnotationConfig config, BratAttributeAnnotation t) {
	}

	private static int findTokenForPosition(int documentLevelCharacterPosition, Tokenization tokenization,
			boolean belowUpperBound) {
		int sentenceLevelCharacterPosition = documentLevelCharacterPosition - tokenization.absoluteStartOffset;
		return ParsingUtils.binarySpanSearch(tokenization.tokens, sentenceLevelCharacterPosition, belowUpperBound);
	}

}
