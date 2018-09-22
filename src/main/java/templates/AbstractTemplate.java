package templates;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import variables.AbstractState;

public abstract class AbstractTemplate<InstanceT, StateT extends AbstractState<InstanceT>, FactorScopeT extends FactorScope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger();

	/**
	 * Weights for the computation of factor scores. These weights are shared
	 * across all factors of this template.
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
	 * Returns all possible factor scopes that can be applied to the given
	 * state. Each FactorScope declares which variables are relevant for its
	 * computation but does NOT compute any features yet. Later, a selected set
	 * of factor variables that were created here are passed to the
	 * computeFactor() method, for the actual computation of factors and feature
	 * values.
	 * 
	 * @param state
	 * @return
	 */
	public abstract List<FactorScopeT> generateFactorScopes(StateT state);

	/**
	 * This method receives the previously created "empty" factor scopes and
	 * computes the features for this factor. For this, each previously created
	 * FactorScopes should include all the variables it needs to compute the
	 * respective factor.
	 * 
	 * @param state
	 * @param factor
	 */
	public abstract void computeFactor(Factor<FactorScopeT> factor);

	@Override
	public String toString() {
		return "Template [weights=" + weights + "]";
	}

}
