package Templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Variables.State;

public abstract class Template implements Serializable {
	{
		Log.off();
	}
	// private static final double DEFAULT_VALUE = 0.01;
	protected Vector weights = new Vector();
	/**
	 * This map holds the relations between generated factors and states. It
	 * gets cleared after each batch of generated states of a sampler is
	 * evaluated and a new current state is accepted.
	 */
	private transient Map<String, List<Factor>> factors = new HashMap<String, List<Factor>>();

	public void update(Factor factor, double alpha) {
		// TODO adjusting the learning step according to each individual
		// feature's contribution the the computed score could improve the
		// learning procedure
		for (String feature : factor.getFeatureVector().getFeatures()) {
			weights.update(feature, alpha);
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
		Log.d("Update for state %s; %s factors.", state.getID(),
				factorsForState.size());
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
		factors.put(state.getID(), generateFactors(state));
	}

	/**
	 * Iterate over annotations of given state and recompute factors and
	 * features. Update factors in annotations but only the factors associated
	 * with this template.
	 * 
	 * @param state
	 */
	// TODO the return value of this function could be changed (e.g. to a
	// Map<Factor, EntityAnnotation>), so that the template can manage
	// factor-entity relations. This might help in the future, when updates are
	// only performed on factors, who's entities changed
	protected abstract List<Factor> generateFactors(State state);

	@Override
	public String toString() {
		return "Template [weights=" + weights + "]";
	}

	public List<Factor> getFactors(State state) {
		List<Factor> factorsForState = factors.get(state.getID());
		if (factorsForState == null) {
			factorsForState = new ArrayList<Factor>();
		}
		return factorsForState;
	}

	/**
	 * For Debug/Logging purposes only.
	 * 
	 * @return
	 */
	public int getFactorCount() {
		int count = 0;
		for (String stateIDs : factors.keySet()) {
			List<Factor> factorsForState = factors.get(stateIDs);
			count += factorsForState.size();
		}
		return count;
	}

	public Set<String> getStates() {
		return factors.keySet();
	}

	public void clean() {
		factors.clear();
	}

	public Collection<Factor> getAllFactors() {
		Collection<Factor> allFactors = new ArrayList<Factor>();
		for (List<Factor> list : factors.values()) {
			allFactors.addAll(list);
		}
		return allFactors;
	}
}
