package learning.scorer;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.Factor;
import learning.Vector;
import variables.AbstractState;

public class LinearScorer extends Scorer {

	private static Logger log = LogManager.getFormatterLogger();

	/**
	 * The scorer scores a state w.r.t. the model. It retrieves all factors
	 * related to the state and multiplies their individual scores. These
	 * individual factor scores are basically the exponential of the dot product
	 * of the feature values and the weights of the template of the factor:
	 * <i>exp(factor.features * factor.template.weights)</i> for all factors.
	 * 
	 * @param model
	 */
	public LinearScorer() {
	}

	/**
	 * Computes the score of this state according to the trained model. The
	 * computed score is returned but also updated in the state objects
	 * <i>score</i> field.
	 * 
	 * @param state
	 * @return
	 */
	public double score(Set<Factor<?>> factors) {
		// at this point, the function unroll(state) should be applied at least
		// once

		// compute the score of the state according to all templates and all
		// respective factors

		double score = 0;
		for (Factor<?> factor : factors) {
			Vector featureVector = factor.getFeatureVector();
			Vector weights = factor.getTemplate().getWeightVector();
			double dotProduct = featureVector.dotProduct(weights);
			double factorScore = dotProduct;
			score += factorScore;
		}
		return score;
	}

}
