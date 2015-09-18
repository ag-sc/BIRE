package Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import Corpus.Corpus;
import Corpus.SubDocument;
import Corpus.parser.brat.BioNLPLoader;
import Corpus.parser.brat.FileUtils;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;
import evaluation.BioNLPEvaluation;

public class InspectParsingAndWriting {

	/**
	 * Parses one specific document (annotation-text file pair) into the BIRE
	 * format. The parsed annotations (as part of the document's gold state) are
	 * then re-written to a file. The contents of the generated annotation file
	 * are supposed to be equal to the source annotation file (w.r.t. the BioNLP
	 * "strict equality" criterion). This means, the generated textual
	 * representations is not going to be identical in terms of characters, but
	 * equal, considering the portrayed entities/events.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		File goldDir = new File("/homes/sjebbara/datasets/BioNLP-ST-2013-GE/dev/");
		File predDir = new File("/homes/sjebbara/datasets/BioNLP-ST-2013-GE/eval test/predicted/");

		List<File> texts = FileUtils.getFiles(goldDir, "txt");
		List<File> annotationsA1 = FileUtils.getFiles(goldDir, "a1");
		List<File> annotationsA2 = FileUtils.getFiles(goldDir, "a2");

		Corpus<SubDocument> corpus = BioNLPLoader.convertDatasetToJavaBinaries(texts, annotationsA1, annotationsA2,
				null);

		List<SubDocument> documents = corpus.getDocuments();
		List<State> states = documents.stream().map(d -> d.getGoldState()).collect(Collectors.toList());
		Log.d("#####################");
		Set<File> files = BioNLPEvaluation.statesToBioNLPFiles(predDir, states, true);
		Log.d("Parsed and written %s documents: %s", files.size(), files);
		// Log.d("### Original:\n%s", FileUtils.readFile(annFile));
		// Log.d("### Predicted:\n%s", annotationsAsText);

		Log.d("#####################");
		State state1 = documents.get(4).getGoldState();
		State state2 = new State(state1);
		EntityAnnotation removedEntity = new ArrayList<>(state2.getEntities()).get(0);
		state2.removeEntity(removedEntity.getID());
		Log.d("State 2: remove entity %s", removedEntity);
		Log.d(state1.getDocument().getContent());
		Log.d("State 1: %s", state1);
		Log.d("State 2: %s", state2);
		Log.d("F1(State 1, State 1) =  %s", BioNLPEvaluation.strictEquality(state1, state1));
		Log.d("F1(State 2, State 2) =  %s", BioNLPEvaluation.strictEquality(state2, state2));
		Log.d("F1(State 1, State 2) =  %s", BioNLPEvaluation.strictEquality(state1, state2));
		Log.d("F1(State 2, State 1) =  %s", BioNLPEvaluation.strictEquality(state2, state1));
	}
}
