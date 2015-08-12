package Learning;

import java.util.Collection;
import java.util.List;

import Factors.Factor;
import Logging.Log;
import Templates.Template;
import Variables.State;

public class Scorer {
	{
		Log.off();
	}
	private Model model;

	public Scorer(Model model) {
		this.model = model;
	}

	/**
	 * Computes the score of this state according to the trained model. The
	 * computed score is returned but also updated in the state objects
	 * <i>score</i> field.
	 * 
	 * @param state
	 * @return
	 */
	public double score(State state) {
		// at this point, the function unroll(state) should be applied at least
		// once

		// compute the score of the state according to all templates and all
		// respective factors
		double score = 1;
		Collection<Template> templates = model.getTemplates();
		// boolean factorsApplied = false;
		// Log.d("Score state: %s", state);
		for (Template template : templates) {
			// Log.d("\tTemplate %s", template.getClass().getSimpleName());
			Vector weightVector = template.getWeightVector();
			List<Factor> factors = template.getFactors(state);
			for (Factor factor : factors) {
				// Log.d("\t\tFactor Features:\n\t\t%s",
				// factor.getFeatureVector());
				Vector featureVector = factor.getFeatureVector();
				double factorScore = Math.exp(featureVector
						.dotProduct(weightVector));
				score *= factorScore;
				// factorsApplied = true;
				// Log.d("\t\tfactor with score = %s applied", factorScore);
			}
		}
		state.setModelScore(score);
		return score;
	}

	public void unroll(State state) {
		/*
		 * TODO to safe some RAM this function could return the generated
		 * factors directly. This would safe some unnecessary loops and the
		 * templates would not need to safe the factors for every state. Still,
		 * templates would need to safe some kind of information to allow for
		 * the update of specific factors when the learner tries to update
		 * weights.
		 */
		for (Template t : model.getTemplates()) {
			t.applyTo(state);
		}
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
