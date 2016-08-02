package templates;

import java.io.Serializable;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.Instance;
import factors.Factor;
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
	 * Updates the weight of the given feature by adding the given alpha value.
	 * 
	 * @param feature
	 * @param gradient
	 * @param currentAlpha
	 */
	public void update(String feature, double update) {
		weights.addToValue(feature, update);
	}

	public Vector getWeights() {
		return weights;
	}

	public void setWeights(Vector weights) {
		this.weights = weights;
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

	@Override
	public String toString() {
		return "Template [weights=" + weights + "]";
	}

}
