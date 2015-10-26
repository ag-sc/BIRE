package templates;

import java.io.Serializable;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import changes.StateChange;
import factors.AbstractFactor;
import factors.FactorGraph;
import learning.Vector;
import variables.AbstractState;

public abstract class AbstractTemplate<StateT extends AbstractState> implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(AbstractTemplate.class.getName());

	protected Vector weights = new Vector();

	// /*
	// * The provided constructor values for the capacity and the loadFactor are
	// * the map's default values. The concurrencyLevel should be set to the
	// * number of concurrently accessing threads. Since the DefaultLearners
	// * default thread pool size is 4 this value is used here as well.
	// */
	// private transient Map<FactorID, AbstractFactor> factors = new
	// ConcurrentHashMap<>(16, 0.75f, 4);

	// public int recomputed = 0;
	// public int removed = 0;
	// public int all = 0;

	/**
	 * Updates the weight of the given feature by adding the given alpha value.
	 * 
	 * @param feature
	 * @param alpha
	 */
	public void update(String feature, double alpha) {
		weights.addToValue(feature, alpha);
	}
	//
	// /**
	// * Updates all features of this factor.
	// *
	// * @param factor
	// * @param alpha
	// */
	// public void update(AbstractFactor factor, double alpha) {
	// for (String feature : factor.getFeatureVector().getFeatureNames()) {
	// update(feature, alpha);
	// }
	// }

	// /**
	// * Apply an update of alpha to every factor (its respective features
	// * weights) that applies to the given state.
	// *
	// * @param state
	// * @param alpha
	// */
	// public void update(StateT state, double alpha) {
	// Set<AbstractFactor> factorsForState = getFactors(state);
	// log.debug("Update for state %s; %s factors.", state.getID(),
	// factorsForState.size());
	// for (AbstractFactor factor : factorsForState) {
	// update(factor, alpha);
	// }
	// }

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
	public void applyTo(StateT state, boolean force) {
		log.debug("Apply template \"%s\" to state %s. Force recomputation: %s", this.getClass().getSimpleName(),
				state.getID(), force);
		log.debug("%s", state);
		FactorGraph factorGraph = state.getFactorGraph();

		Set<AbstractFactor> allPossibleFactors = generateFactors(state);
		log.debug("%s possible Factors: %s", allPossibleFactors.size(), allPossibleFactors);

		factorGraph.updateFactors(this, allPossibleFactors);

		// TODO only update changed factors
		log.debug("(Re)compute Factors: %s", allPossibleFactors);
		allPossibleFactors.forEach(f -> computeFactor(state, f));

		// Multimap<VariableID, StateChange> changedEntities =
		// state.getChangedVariables();
		//
		// log.debug("Changed Entities: %s", changedEntities);
		// /*
		// * Collect all "old" variable sets of this template from the previous
		// * iterations that contain a relevantly changed variable. These need
		// to
		// * be deleted.
		// */
		// Set<AbstractFactor> allChangedFactors = new HashSet<>();
		// for (VariableID variableID : changedEntities.keySet()) {
		// if (anyRelevantChange(changedEntities.get(variableID))) {
		// allChangedFactors.addAll(factorGraph.getFactorsForVariableWithID(this,
		// variableID));
		// }
		// }
		//
		// Set<AbstractFactor> recomputeFactors;
		// if (force) {
		// /*
		// * (Re) compute all factors that can be applied to this state.
		// */
		// recomputeFactors = allPossibleFactors;
		// factorGraph.reset();
		// log.debug("(changed) Factors to be removed: ALL");
		// } else {
		// /*
		// * Collect all variable sets that apply to this new state that
		// * contain a relevantly changed variable. These need to be
		// * recomputed.
		// */
		// recomputeFactors = new HashSet<>();
		// for (AbstractFactor factor : allPossibleFactors) {
		// if (containsChangedVariable(factor, changedEntities)) {
		// recomputeFactors.add(factor);
		// }
		// }
		//
		// log.debug("(changed) Factors to be removed: %s", allChangedFactors);
		// for (AbstractFactor factor : allChangedFactors) {
		// // these factors are no longer needed
		// factorGraph.removeFactor(this, factor);
		// }
		// }
		// log.debug("%s variables to be (re)computed: %s",
		// recomputeFactors.size(),
		// recomputeFactors);
		//
		// for (AbstractFactor factor : recomputeFactors) {
		// // generate factor (features) for each relevant (changed)
		// // Factor
		// computeFactor(state, factor);
		// factorGraph.addFactor(this, factor.getID(), factor);
		// factors.put(factor.getID(), factor);
		// }
		// // log.debug("Graph:\n%s", factorGraph);
		// recomputed += recomputeFactors.size();
		// removed += allChangedFactors.size();
		// all += allPossibleFactors.size();
	}

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
	protected abstract void computeFactor(StateT state, AbstractFactor factor);

	/**
	 * Returns all sets of variables that can be extracted from the given state
	 * and which this template can use to generate factors.
	 * 
	 * @param state
	 * @return
	 */
	protected abstract Set<AbstractFactor> generateFactors(StateT state);

	// /**
	// * This method returns true if the given state change might demand a
	// * (re)computation of affected factors.
	// *
	// * @param value
	// * @return
	// */
	// protected boolean isRelevantChange(StateChange value) {
	// return true;
	// }
	// private boolean anyRelevantChange(Collection<StateChange> changes) {
	// return changes.stream().anyMatch(this::isRelevantChange);
	// }

	// /**
	// * Returns true if this VariableSet contains a variable that was changed
	// in
	// * a way that is relevant for this template (see: isRelevantChange()) and
	// * false otherwise.
	// *
	// * @param variableSet
	// * @param changedEntities
	// * @return
	// */
	// private boolean containsChangedVariable(AbstractFactor variableSet,
	// Multimap<VariableID, StateChange> changedEntities) {
	// for (VariableID entityID : variableSet.getEntityIDs()) {
	// if (changedEntities.containsKey(entityID)) {
	// if (anyRelevantChange(changedEntities.get(entityID))) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	//
	// /**
	// * Returns all factors that are associated to the provided state.
	// *
	// * @param state
	// * @return
	// */
	// public Set<AbstractFactor> getFactors(StateT state) {
	// Set<FactorID> factorIDsForState =
	// state.getFactorGraph().getFactorIDs(this);
	// Set<AbstractFactor> factorsForState = new HashSet<>();
	// for (FactorID factorID : factorIDsForState) {
	// factorsForState.add(factors.get(factorID));
	// }
	// return factorsForState;
	// }
	//
	// /**
	// * Drops all factors from the memory that are not part of this state (in
	// * order to save memory). This is useful at the end of a sampling step,
	// * where only one state is kept to proceed the training.
	// *
	// * @param state
	// */
	// public void trimToState(StateT state) {
	// Set<FactorID> factorsForState =
	// state.getFactorGraph().getFactorIDs(this);
	// factors.keySet().retainAll(factorsForState);
	// recomputed = 0;
	// removed = 0;
	// all = 0;
	// }

	// /**
	// * Returns all currently used factors of this template.
	// *
	// * @return
	// */
	// public Map<FactorID, AbstractFactor> getFactors() {
	// return factors;
	// }

	// /**
	// * Returns the actual factor for the specified factor id
	// *
	// * @param factorID
	// * @return
	// */
	// public AbstractFactor getFactor(FactorID factorID) {
	// return factors.get(factorID);
	// }

	@Override
	public String toString() {
		return "Template [weights=" + weights + "]";
	}
}
