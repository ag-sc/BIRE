package templates;

import java.io.Serializable;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.AbstractFactor;
import factors.FactorGraph;
import learning.Vector;
import variables.AbstractState;

public abstract class AbstractTemplate<StateT extends AbstractState> implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(AbstractTemplate.class.getName());

	/**
	 * Initializes the feature weights for this template. The default values for
	 * the weights are sampled from a uniform distribution in the range of
	 * INIT_WEIGHT_RANGE.
	 */
	protected Vector weights = new Vector();
	/**
	 * A regularization parameter to punish big feature weights.
	 */
	protected double l2 = 0.01;

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
	 * This method only needs to be called by the framework.
	 * 
	 * @param state
	 */
	public void applyTo(StateT state, boolean force) {
		log.debug("Apply template \"%s\" to state %s. Force recomputation: %s", this.getClass().getSimpleName(),
				state.getID(), force);
		log.debug("%s", state);
		FactorGraph factorGraph = state.getFactorGraph();

		Collection<AbstractFactor> allPossibleFactors = generateFactors(state);
		log.debug("%s possible Factors: %s", allPossibleFactors.size(), allPossibleFactors);

		factorGraph.updateFactors(this, allPossibleFactors);

		// TODO only update changed factors
		log.debug("(Re)compute Factors: %s", allPossibleFactors);
		allPossibleFactors.forEach(f -> computeFactor(state, f));
	}

	/**
	 * Returns all possible factors that can be applied to the given state. Note
	 * that this function should only return "empty" factors but does not need
	 * to compute the features yet. The "empty" factor only needs to remember
	 * which variables of the state are relevant for the computation of the
	 * features.
	 * 
	 * @param state
	 * @return
	 */
	protected abstract Collection<AbstractFactor> generateFactors(StateT state);

	/**
	 * This method receives the previously created "empty" factors and computes
	 * the features for this factor. Therefore, the factor object implementation
	 * should remember which variables are needed and retrieve them from the
	 * given state.
	 * 
	 * @param state
	 * @param factor
	 */
	protected abstract void computeFactor(StateT state, AbstractFactor factor);

	@Override
	public String toString() {
		return "Template [weights=" + weights + "]";
	}
}
