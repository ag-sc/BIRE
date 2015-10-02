package Learning;

import java.util.Collection;
import java.util.Set;

import Factors.Factor;
import Logging.Log;
import Templates.Template;
import Variables.IState;

public class Scorer<StateT extends IState> {
	{
		Log.off();
	}

	private Model<StateT> model;

	public Scorer(Model<StateT> model) {
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
	public double score(StateT state) {
		// at this point, the function unroll(state) should be applied at least
		// once

		// compute the score of the state according to all templates and all
		// respective factors
		double score = 1;
		Collection<Template<StateT>> templates = model.getTemplates();
		// boolean factorsApplied = false;
		// Log.d("Score state: %s", state);
		for (Template<StateT> template : templates) {
			// Log.d("\tTemplate %s", template.getClass().getSimpleName());
			Vector weightVector = template.getWeightVector();
			Set<Factor> factors = template.getFactors(state);
			for (Factor factor : factors) {
				// Log.d("\t\tFactor Features:\n\t\t%s",
				// factor.getFeatureVector());
				Vector featureVector = factor.getFeatureVector();
				double factorScore = Math.exp(featureVector.dotProduct(weightVector));
				score *= factorScore;
				// factorsApplied = true;
				// Log.d("\t\tfactor with score = %s applied", factorScore);
			}
		}
		state.setModelScore(score);
		return score;
	}

	public void unroll(StateT state) {
		for (Template<StateT> t : model.getTemplates()) {
			t.applyTo(state, false);
		}
	}

	public Model<StateT> getModel() {
		return model;
	}

	public void setModel(Model<StateT> model) {
		this.model = model;
	}

}
