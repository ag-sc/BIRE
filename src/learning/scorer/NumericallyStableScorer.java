package learning.scorer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import javax.jws.soap.SOAPBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.MissingFactorException;
import factors.Factor;
import learning.Vector;
import variables.AbstractState;

public class NumericallyStableScorer implements Scorer {

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
	public NumericallyStableScorer() {
	}

	public void score(List<? extends AbstractState<?>> states, boolean multiThreaded) {
		Double[] rawScores = new Double[states.size()];

		/*
		 * Collect raw (unnormalized) scores
		 */
		if (multiThreaded)
			IntStream.range(0, states.size()).parallel().forEach(i -> rawScores[i] = rawScore(states.get(i)));
		else
			IntStream.range(0, states.size()).forEach(i -> rawScores[i] = rawScore(states.get(i)));

		/*
		 * Find maximum score
		 */
		double max = Arrays.asList(rawScores).stream().max((d1, d2) -> Double.compare(d1, d2)).get();
		/*
		 * Subtract maximum and compute exponential
		 */
		for (int j = 0; j < rawScores.length; j++) {
			// double factorScore = Math.exp(rawScores[j]);
			double factorScore = Math.exp(rawScores[j] - max);
			states.get(j).setModelScore(factorScore);
		}
	}

	public double rawScore(AbstractState<?> state) {
		Set<Factor<?>> factors = null;
		try {
			factors = state.getFactorGraph().getFactors();
		} catch (MissingFactorException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double score = 0;
		for (Factor<?> factor : factors) {
			Vector featureVector = factor.getFeatureVector();
			Vector weights = factor.getTemplate().getWeights();
			double dotProduct = featureVector.dotProduct(weights);
			score += dotProduct;
		}

		return score;
	}
}
