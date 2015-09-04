package Templates;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Multimap;

import Changes.StateChange;
import Factors.Factor;
import Factors.FactorGraph;
import Learning.Vector;
import Logging.Log;
import Templates.variablesets.VariableSet;
import Variables.State;
import utility.EntityID;
import utility.FactorID;

public abstract class Template implements Serializable {
	{
		Log.off();
	}

	protected Vector weights = new Vector();

	/*
	 * The provided constructor values for the capacity and the loadFactor are
	 * the map's default values. The concurrencyLevel should be set to the
	 * number of concurrently accessing threads. Since the DefaultLearners
	 * default thread pool size is 4 this value is used here as well.
	 */
	private Map<FactorID, Factor> factors = new ConcurrentHashMap<>(16, 0.75f, 4);

	public int recomputed = 0;
	public int removed = 0;
	public int all = 0;

	/**
	 * Updates the weight of the given feature by adding the given alpha value.
	 * 
	 * @param feature
	 * @param alpha
	 */
	public void update(String feature, double alpha) {
		weights.addToValue(feature, alpha);
	}

	/**
	 * Updates all features of this factor.
	 * 
	 * @param factor
	 * @param alpha
	 */
	public void update(Factor factor, double alpha) {
		for (String feature : factor.getFeatureVector().getFeatureNames()) {
			update(feature, alpha);
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
		Set<Factor> factorsForState = getFactors(state);
		Log.d("Update for state %s; %s factors.", state.getID(), factorsForState.size());
		for (Factor factor : factorsForState) {
			update(factor, alpha);
		}
	}

	public Vector getWeightVector() {
		return weights;
	}

	/**
	 * This function computes factors (and their features) for each set of
	 * variables (annotations) in the given state that has changed and needs
	 * (re)computation. Furthermore, the specific implementation of the template
	 * interface, determines which changes are relevant to recompute factors.
	 * 
	 * @param state
	 */
	public void applyTo(State state) {
		Log.d("Apply template \"%s\" to state %s", this.getClass().getSimpleName(), state.getID());
		Log.d("%s", state);
		FactorGraph factorGraph = state.getFactorGraph();

		Set<VariableSet> allPossibleNewVariablesSet = getVariableSets(state);
		Log.d("%s possible new Variables for template: %s", allPossibleNewVariablesSet.size(),
				allPossibleNewVariablesSet);

		Multimap<EntityID, StateChange> changedEntities = state.getChangedEntities();

		Log.d("Changed Entities: %s", changedEntities);
		/*
		 * Collect all "old" variable sets of this template from the previous
		 * iterations that contain a relevantly changed variable. These need to
		 * be deleted.
		 */
		Set<VariableSet> allChangedVariableSets = new HashSet<>();
		for (EntityID entityID : changedEntities.keySet()) {
			if (anyRelevantChange(changedEntities.get(entityID))) {
				allChangedVariableSets.addAll(factorGraph.getVariableSetsForEntityID(this, entityID));
			}
		}
		Log.d("(changed) Variables to be removed: %s", allChangedVariableSets);

		/*
		 * Collect all variable sets that apply to this new state that contain a
		 * relevantly changed variable. These need to be recomputed.
		 */
		Set<VariableSet> recomputeVariableSets = new HashSet<>();
		for (VariableSet variableSet : allPossibleNewVariablesSet) {
			if (containsChangedVariable(variableSet, changedEntities)) {
				recomputeVariableSets.add(variableSet);
			}
		}
		Log.d("%s variables to be (re)computed: %s", recomputeVariableSets.size(), recomputeVariableSets);

		/*
		 * Remove old, invalidated factors for each relevant base entity. This
		 * includes all changed entities plus the base entities of all affected
		 * factors (overlap expected!)
		 */
		for (VariableSet variableSet : allChangedVariableSets) {
			// these factors are no longer needed
			factorGraph.removeFactorForVariableSet(this, variableSet);
		}

		for (VariableSet variableSet : recomputeVariableSets) {
			// generate factor (features) for each relevant (changed)
			// VariableSet
			Factor factor = generateFactor(state, variableSet);
			if (factor != null) {
				factorGraph.addFactor(this, factor.getID(), variableSet);
				factors.put(factor.getID(), factor);
			} else {
				Log.w("Template %s returned null-Factor for VariableSet %s and State %s",
						this.getClass().getSimpleName(), variableSet, state.getID());
			}
		}
		// Log.d("Graph:\n%s", factorGraph);
		recomputed += recomputeVariableSets.size();
		removed += allChangedVariableSets.size();
		all += allPossibleNewVariablesSet.size();
	}

	/**
	 * This method returns true if the given state change might demand a
	 * (re)computation of affected factors.
	 * 
	 * @param value
	 * @return
	 */
	protected abstract boolean isRelevantChange(StateChange value);

	/**
	 * Generates a factor for the given set of variables. The exact type
	 * (subclass) of the provided VariableSet must match the type that is
	 * returned via getVariableSets(State). See the implementation of this
	 * method for the used class.
	 * 
	 * @param state
	 * @param variables
	 * @return
	 */
	protected abstract Factor generateFactor(State state, VariableSet variables);

	/**
	 * Returns all sets of variables that can be extracted from the given state
	 * and which this template can use to generate factors.
	 * 
	 * @param state
	 * @return
	 */
	protected abstract Set<VariableSet> getVariableSets(State state);

	private boolean anyRelevantChange(Collection<StateChange> changes) {
		for (StateChange change : changes) {
			if (isRelevantChange(change)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this VariableSet contains a variable that was changed in
	 * a way that is relevant for this template (see: isRelevantChange()) and
	 * false otherwise.
	 * 
	 * @param variableSet
	 * @param changedEntities
	 * @return
	 */
	private boolean containsChangedVariable(VariableSet variableSet, Multimap<EntityID, StateChange> changedEntities) {
		for (EntityID entityID : variableSet.getEntityIDs()) {
			if (changedEntities.containsKey(entityID)) {
				if (anyRelevantChange(changedEntities.get(entityID))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns all factors that are associated to the provided state.
	 * 
	 * @param state
	 * @return
	 */
	public Set<Factor> getFactors(State state) {
		Set<FactorID> factorIDsForState = state.getFactorGraph().getFactorIDs(this);
		Set<Factor> factorsForState = new HashSet<>();
		for (FactorID factorID : factorIDsForState) {
			factorsForState.add(factors.get(factorID));
		}
		return factorsForState;
	}

	/**
	 * For Debug/Logging purposes only.
	 * 
	 * @return
	 */
	public int getFactorCount() {
		return factors.size();
	}

	/**
	 * Drops all factors from the memory that are not part of this state (in
	 * order to save memory). This is useful at the end of a sampling step,
	 * where only one state is kept to proceed the training.
	 * 
	 * @param state
	 */
	public void trimToState(State state) {
		Set<FactorID> factorsForState = state.getFactorGraph().getFactorIDs(this);
		factors.keySet().retainAll(factorsForState);
		recomputed = 0;
		removed = 0;
		all = 0;
	}

	// /**
	// * Returns a vector that contains the sum of all feature vectors for all
	// * factors of this template that are associated with the given state.
	// *
	// * @param state
	// * @return
	// */
	// public Vector getJointFeatures(State state) {
	// Vector sum = new Vector();
	// for (Factor f : getFactors(state)) {
	// sum.add(f.getFeatureVector());
	// }
	// return sum;
	// }

	/**
	 * Returns all currently used factors of this template.
	 * 
	 * @return
	 */
	public Map<FactorID, Factor> getFactors() {
		return factors;
	}

	/**
	 * Returns the actual factor for the specified factor id
	 * 
	 * @param factorID
	 * @return
	 */
	public Factor getFactor(FactorID factorID) {
		return factors.get(factorID);
	}

	/**
	 * Computes the differences of all features of both states. For this, the
	 * template uses features from all factors that are not associated to both
	 * states (since these differences would be always 0).
	 * 
	 * @param state1
	 * @param state2
	 * @return
	 */
	public Vector getFeatureDifferences(State state1, State state2) {
		Vector diff = new Vector();
		Set<FactorID> factors1 = state1.getFactorGraph().getFactorIDs(this);
		Set<FactorID> factors2 = state2.getFactorGraph().getFactorIDs(this);
		for (FactorID factorID : factors1) {
			if (!factors2.contains(factorID)) {
				Factor factor = factors.get(factorID);
				diff.add(factor.getFeatureVector());
			}
		}
		for (FactorID factorID : factors2) {
			if (!factors1.contains(factorID)) {
				Factor factor = factors.get(factorID);
				diff.sub(factor.getFeatureVector());
			}
		}
		return diff;
	}

	@Override
	public String toString() {
		return "Template [weights=" + weights + "]";
	}
}
