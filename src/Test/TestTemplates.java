package Test;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;
import Factors.Factor;
import Logging.Log;
import Templates.ContextTemplate;
import Templates.MorphologicalTemplate;
import Templates.RelationTemplate;
import Templates.Template;
import Variables.EntityAnnotation;
import Variables.State;

public class TestTemplates {

	public static void main(String[] args) {
		// test template
		Corpus corpus = TestData.getDummyData();
		AnnotatedDocument doc = corpus.getDocuments().get(0);
		Template[] templates = { new MorphologicalTemplate(),
				new ContextTemplate(), new RelationTemplate() };

		State state = new State(doc.getGoldState());
		Log.d("Before templates");
		// printFactors(state);
		for (Template t : templates) {
			t.applyTo(state);
		}
		Log.d("After templates");
		// printFactors(state);
	}

	// private static void printFactors(State state) {
	// Log.d("Factors for state %s", state.getID());
	// for (EntityAnnotation e : state.getEntities()) {
	// Log.d("Entity %s (\"%s\"):", e.getID(), e.getText());
	// for (Factor f : e.getFactors()) {
	// Log.d("\tFactor for template \"%s\"", f.getTemplate()
	// .getClass().getSimpleName());
	// for (String feature : f.getFeatureVector().getFeatures()) {
	// Log.d("\t\t%s:\t\t%s", feature, f.getFeatureVector()
	// .getValueOfFeature(feature));
	// }
	// }
	// }
	// }
}
