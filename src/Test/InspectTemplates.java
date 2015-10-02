package Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import Variables.EntityType;
import Variables.MutableEntityAnnotation;
import Variables.State;
import evaluation.BioNLPLearning;
import evaluation.EvaluationUtil;
import utility.EntityID;

public class InspectTemplates {

	public static void main(String[] args) {
		Corpus<? extends AnnotatedDocument<State>> corpus = null;

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
		AnnotatedDocument<State> doc = corpus.getDocuments().get(1);
		Log.d("Content: %s (%s)", doc.getContent(), doc.getContent().length());
		Log.d("Tokens: %s", doc.getTokens());
		Log.d("State: %s", doc.getGoldState());

		List<Template<State>> templates = Arrays.asList(new MorphologicalTemplate(), new ContextTemplate(),
				new RelationTemplate());

		State state = doc.getGoldState().duplicate();
		applyTemplatesToState(templates, state, false);
		templates.forEach(t -> t.trimToState(state));
		state.markAsUnchanged();
		Log.d("");
		Log.d("########### Modify State ###########");
		Log.d("");
		MutableEntityAnnotation e = new ArrayList<>(state.getMutableEntities()).get(0);
		e.setType(new EntityType("Banana"));
		applyTemplatesToState(templates, state, false);

	}

	private static void applyTemplatesToState(List<Template<State>> templates, State state, boolean force) {
		Log.d("Mutables:   %s", state.getMutableEntities());
		Log.d("Immutables: %s", state.getImmutableEntities());
		for (Template<State> t : templates) {
			t.applyTo(state, force);
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
