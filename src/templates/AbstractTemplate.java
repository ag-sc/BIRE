package templates;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.Instance;
import factors.Factor;
import factors.FactorGraph;
import factors.FactorPattern;
import learning.Vector;
import variables.AbstractState;

public abstract class AbstractTemplate<InstanceT extends Instance, StateT extends AbstractState<InstanceT>, FactorPatternT extends FactorPattern>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger();

	/**
	 * Initializes the feature weights for this template. The default values for
	 * the weights are sampled from a uniform distribution in the range of
	 * INIT_WEIGHT_RANGE.
	 */
	protected Vector weights = new Vector();
	/**
	 * A regularization parameter to punish big feature weights.
	 */
	protected double l2 = 0.0;

	/**
	 * Updates the weight of the given feature by adding the given alpha value.
	 * 
	 * @param feature
	 * @param gradient
	 * @param currentAlpha
	 */
	public void update(String feature, double gradient, double learningRate) {
		double weight = weights.getValueOfFeature(feature);
		double update = learningRate * (gradient - l2 * weight);
		weights.addToValue(feature, update);
	}

	public Vector getWeightVector() {
		return weights;
	}

	/**
	 * This function computes factors (and their features) for the given state.
	 * It computes all possible FactorPatterns to which this template could be
	 * applied. For new factor patterns a new factor is created. Finally, all
	 * possible factor patterns are returned.
	 *
	 * @param state
	 */
	public Set<FactorPatternT> applyTo(StateT state, boolean force) {
		log.debug("Apply template \"%s\" to state %s. Force recomputation: %s", this.getClass().getSimpleName(),
				state.getID(), force);
		log.debug("%s", state);
		FactorGraph factorGraph = state.getFactorGraph();

		Set<FactorPatternT> allPossibleFactors = generateFactorPatterns(state);
		log.debug("%s possible Factors: %s", allPossibleFactors.size(), allPossibleFactors);

		Set<FactorPatternT> newFactorPatterns = factorGraph.extractNewFactorPatterns(allPossibleFactors);
		log.debug("%s new Factors: %s", newFactorPatterns.size(), newFactorPatterns);

		log.debug("Compute %s factors ...", force ? "ALL" : "NEW");
		Set<Factor<FactorPatternT>> newFactors = null;
		if (force) {
			newFactors = allPossibleFactors.stream().map(p -> new Factor<>(p)).collect(Collectors.toSet());
		} else {
			newFactors = newFactorPatterns.stream().map(p -> new Factor<>(p)).collect(Collectors.toSet());
		}

		// TODO use parallelization here!
		newFactors.forEach(p -> computeFactor(state.getInstance(), p));
		factorGraph.addFactors(newFactors);

		return allPossibleFactors;
	}

	/**
	 * Returns all possible factor patterns that can be applied to the given
	 * state. Each factor pattern declares which variables are relevant for its
	 * computation but does NOT compute any features yet. Later, a selected set
	 * of factor patterns that were created here are passed to the
	 * computeFactor() method, for the actual computation of factors and feature
	 * values.
	 * 
	 * @param state
	 * @return
	 */
	public abstract Set<FactorPatternT> generateFactorPatterns(StateT state);

	/**
	 * This method receives the previously created "empty" factor patterns and
	 * computes the features for this factor. For this, each previously created
	 * factor pattern should include all the variables it needs to compute the
	 * respective factor.
	 * 
	 * @param state
	 * @param factor
	 */
	public abstract void computeFactor(InstanceT instance, Factor<FactorPatternT> factor);

	// public Factor<FactorPatternT> computeFactor(StateT state, FactorPatternT
	// factorPattern) {
	// Factor<FactorPatternT> newFactor = new Factor<>(factorPattern);
	// computeFactor(state.getInstance(), newFactor);
	// return newFactor;
	// }

	@Override
	public String toString() {
		return "Template [weights=" + weights + "]";
	}

}
