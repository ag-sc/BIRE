package Test;

import java.util.Collection;

import evaluation.EvaluationUtil;
import Corpus.AnnotatedDocument;
import Corpus.Constants;
import Corpus.Corpus;
import Corpus.parser.brat.DatasetLoader;
import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Templates.ContextTemplate;
import Templates.MorphologicalTemplate;
import Templates.RelationTemplate;
import Templates.Template;
import Variables.State;

public class TestTemplates {

	public static void main(String[] args) {
		// test template
		Corpus corpus = null;

		try {
			corpus = DatasetLoader
					.loadDatasetFromBinaries(Constants.JAVA_BIN_CORPUS_FILEPATH);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("Preparsed corpus not accessible or corrupted. Parse again:");
			corpus = DatasetLoader
					.convertDatasetToJavaBinaries(Constants.JAVA_BIN_CORPUS_FILEPATH);
		}

		AnnotatedDocument doc = corpus.getDocuments().get(0);
		Log.d("Content: %s", doc.getContent());
		Log.d("Tokens: %s", doc.getTokens());
		Log.d("State: %s", doc.getGoldState());

		Template[] templates = { new MorphologicalTemplate(),
				new ContextTemplate(), new RelationTemplate() };

		State state = doc.getGoldState();
		// printFactors(state);
		for (Template t : templates) {
			t.applyTo(state);
			Collection<Factor> factors = t.getAllFactors();
			Log.d("Template %s, %s factors: ", t.getClass().getSimpleName(),
					factors.size());
			int i = 0;
			for (Factor factor : factors) {
				Log.d("\tFactor %s for Entity: %s", i, factor.getEntity()
						.toDetailedString());
				Vector v = factor.getFeatureVector();
				for (String f : v.getFeatures()) {
					Log.d("\t%s:\t%s", EvaluationUtil.featureWeightFormat
							.format(v.getValueOfFeature(f)), f);
				}
				i++;
			}
		}
	}
}
