package Test;

import java.util.Collection;
import java.util.logging.Logger;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;
import Corpus.parser.brat.BioNLPLoader;
import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Templates.ContextTemplate;
import Templates.MorphologicalTemplate;
import Templates.RelationTemplate;
import Templates.Template;
import Variables.State;
import evaluation.EvaluationUtil;

public class InspectTemplates {

	public static void main(String[] args) {
		Corpus<? extends AnnotatedDocument> corpus = null;

		switch (1) {
		case 0:
			corpus = DummyData.getDummyData();
			break;
		case 1:
			corpus = BioNLPLoader.loadBioNLP2013Train(true);
			break;
		default:
			break;
		}
		AnnotatedDocument doc = corpus.getDocuments().get(1);
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
	}
}
