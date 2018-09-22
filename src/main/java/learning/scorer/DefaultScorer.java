package learning.scorer;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.Factor;
import learning.Vector;

public class DefaultScorer extends AbstractSingleStateScorer {

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
	public DefaultScorer() {
	}

	@Override
	protected double score(Collection<Factor<?>> factors) {
		double score = 1;
		for (Factor<?> factor : factors) {
			Vector featureVector = factor.getFeatureVector();
			Vector weights = factor.getTemplate().getWeights();
			double dotProduct = featureVector.dotProduct(weights);
			double factorScore = Math.exp(dotProduct);
			score *= factorScore;
		}
		return score;
	}
}
