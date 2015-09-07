package Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.LoadClass;

import utility.EntityID;
import Corpus.AnnotatedDocument;
import Corpus.AnnotationConfig;
import Corpus.Corpus;
import Corpus.DefaultCorpus;
import Corpus.Document;
import Corpus.julie.JavaSentenceSplitter;
import Corpus.julie.Tokenization;
import Corpus.parser.brat.BioNLPLoader;
import Corpus.parser.brat.Brat2BIREConverter;
import Corpus.parser.brat.BratAnnotatedDocument;
import Corpus.parser.brat.BratAnnotationParser;
import Corpus.parser.brat.BratConfigReader;
import Corpus.parser.brat.Utils;
import Logging.Log;
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

		String filename = "PMID-9119025";
		File annFile = new File("res/bionlp/ann/" + filename + ".ann");
		File textFile = new File("res/bionlp/text/" + filename + ".txt");
		Corpus corpus;
		try {
			corpus = BioNLPLoader.loadDocument(textFile, annFile);

			List<AnnotatedDocument> documents = corpus.getDocuments();
			String annotationsAsText = "";
			for (AnnotatedDocument document : documents) {
				annotationsAsText += BioNLPEvaluation
						.stateToBioNLPString(document.getGoldState());
				annotationsAsText += "\n";
			}
			Log.d("#####################");
			Log.d("### Original:\n%s", Utils.readFile(annFile));
			Log.d("### Predicted:\n%s", annotationsAsText);

			State state1 = documents.get(4).getGoldState();
			State state2 = new State(state1);
			state2.removeEntity(new EntityID("T2"));
			Log.d("#####################");
			Log.d(state1.getDocument().getContent());
			Log.d("State 1: %s", state1);
			Log.d("State 2: %s", state2);
			Log.d("F1(State 1, State 1) =  %s",
					BioNLPEvaluation.strictEquality(state1, state1));
			Log.d("F1(State 2, State 2) =  %s",
					BioNLPEvaluation.strictEquality(state2, state2));
			Log.d("F1(State 1, State 2) =  %s",
					BioNLPEvaluation.strictEquality(state1, state2));
			Log.d("F1(State 2, State 1) =  %s",
					BioNLPEvaluation.strictEquality(state2, state1));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
