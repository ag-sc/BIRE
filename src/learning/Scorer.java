package learning;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.AbstractFactor;
import variables.AbstractState;

public class Scorer<StateT extends AbstractState> {

	private static Logger log = LogManager.getFormatterLogger(Scorer.class.getName());
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

		Collection<AbstractFactor> factors = state.getFactorGraph().getFactors();
		for (AbstractFactor factor : factors) {
			Vector featureVector = factor.getFeatureVector();
			double factorScore = Math.exp(featureVector.dotProduct(factor.getTemplate().getWeightVector()));
			score *= factorScore;
		}
		state.setModelScore(score);
		return score;

	}

	public Model<StateT> getModel() {
		return model;
	}

	public void setModel(Model<StateT> model) {
		this.model = model;
	}

}
