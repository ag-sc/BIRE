package Templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Variables.State;

public abstract class Template {
	{
		Log.off();
	}
	// private static final double DEFAULT_VALUE = 0.01;
	protected Vector weights = new Vector();
	private Map<State, List<Factor>> factors = new HashMap<State, List<Factor>>();

	public void update(Factor factor, double alpha) {
		for (String feature : factor.getFeatureVector().getFeatures()) {
			// if (weights.hasValueForFeature(feature)) {
			double oldWeight = weights.getValueOfFeature(feature);
			weights.update(feature, alpha);
			Log.d("Update weight %s by %s:\t %s --> %s", feature, alpha,
					oldWeight, weights.getValueOfFeature(feature));

			// } else {
			// // TODO adding the corresponding wight at this point is already
			// // too late. find a better place to keep weights and features
			// // consistent.
			// double initialWeight = getDefaultWeight();
			// Log.w("Template %s has no weight for feature %s, yet. Initialize weight as %s",
			// this.getClass().getSimpleName(), feature, initialWeight);
			// weights.set(feature, initialWeight);
			// }
		}
	}

	/**
	 * Apply an update of alpha to every factor (its respective features
	 * weights) that applies to the given state.
	 * 
	 * @param state
	 * @param alpha
	 */
	public void update(State state, double alpha) {
		List<Factor> factorsForState = getFactors(state);
		for (Factor factor : factorsForState) {
			update(factor, alpha);
		}
	}

	// /**
	// * This function should return a default weight for features that were
	// * previously not considered. This could be a single constant value, but
	// * also a more sophisticated way to initialize the models weights.
	// *
	// * @return
	// */
	// private double getDefaultWeight() {
	// return DEFAULT_VALUE;
	// }

	public Vector getWeightVector() {
		return weights;
	}

	public void applyTo(State state) {
		Log.d("Apply template \"%s\" to state %s", this.getClass()
				.getSimpleName(), state.getID());
		factors.put(state, generateFactors(state));
	}

	/**
	 * Iterate over annotations of given state and recompute factors and
	 * features. Update factors in annotations but only the factors associated
	 * with this template.
	 * 
	 * @param state
	 */
	protected abstract List<Factor> generateFactors(State state);

	@Override
	public String toString() {
		return "Template [weights=" + weights + "]";
	}

	public List<Factor> getFactors(State state) {
		List<Factor> factorsForState = factors.get(state);
		if (factorsForState == null) {
			factorsForState = new ArrayList<Factor>();
		}
		return factorsForState;
	}
}
