package Test;

import java.util.Collection;

import evaluation.BioNLPEvaluation;
import evaluation.EvaluationUtil;
import Corpus.AnnotatedDocument;
import Corpus.Constants;
import Corpus.Corpus;
import Corpus.parser.brat.BioNLPLoader;
import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Templates.ContextTemplate;
import Templates.MorphologicalTemplate;
import Templates.RelationTemplate;
import Templates.Template;
import Variables.EntityAnnotation;
import Variables.State;

public class TestTemplates {

	public static void main(String[] args) {
		Corpus corpus = null;

		switch (2) {
		case 0:
			corpus = TestData.getDummyData();
			break;
		case 1:
			corpus = BioNLPLoader.convertDatasetToJavaBinaries(Constants.JAVA_BIN_BIONLP_CORPUS_FILEPATH);
			break;
		case 2:
			try {
				corpus = BioNLPLoader.loadDatasetFromBinaries(Constants.JAVA_BIN_BIONLP_CORPUS_FILEPATH);
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("Preparsed corpus not accessible or corrupted. Parse again:");
				corpus = BioNLPLoader.convertDatasetToJavaBinaries(Constants.JAVA_BIN_BIONLP_CORPUS_FILEPATH);
			}
			break;
		default:
			break;
		}
		AnnotatedDocument doc = corpus.getDocuments().get(0);
		Log.d("Content: %s (%s)", doc.getContent(), doc.getContent().length());
		Log.d("Tokens: %s", doc.getTokens());
		Log.d("State: %s", doc.getGoldState());

		Template[] templates = { new MorphologicalTemplate(), new ContextTemplate(), new RelationTemplate() };

		State state = new State(doc.getGoldState());
		for (Template t : templates) {
			t.applyTo(state);
			Collection<Factor> factors = t.getFactors(state);
			Log.d("Template %s, %s factors: ", t.getClass().getSimpleName(), factors.size());
			int i = 0;
			for (Factor factor : factors) {
				Log.d("\tFactor %s for VariableSet: %s", i,
						state.getFactorGraph().getVariableSetForFactor(factor.getID()));
				Vector v = factor.getFeatureVector();
				for (String f : v.getFeatureNames()) {
					Log.d("\t%s:\t%s", EvaluationUtil.featureWeightFormat.format(v.getValueOfFeature(f)), f);
				}
				i++;
			}
		}

		Log.d(state.getDocument().getContent());
		Log.d("%s", state);
		Log.d("%s", BioNLPEvaluation.strictEquality(state, state));
	}
}
