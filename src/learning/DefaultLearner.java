package learning;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import evaluation.TaggedTimer;
import factors.AbstractFactor;
import templates.AbstractTemplate;
import variables.AbstractState;

public class DefaultLearner<StateT extends AbstractState> implements Learner<StateT> {

	private static Logger log = LogManager.getFormatterLogger(DefaultLearner.class.getName());

	private double alpha;
	private Model<StateT> model;
	private boolean normalize = true;
	public double currentAlpha;
	public int updates = 0;

	public DefaultLearner(Model<StateT> model, double alpha) {
		super();
		this.model = model;
		this.currentAlpha = alpha;
	}

	/**
	 * Performs a model update according to a learning scheme (currently
	 * SampleRank). The update step is scaled with the provided alpha value.
	 * 
	 * @param currentState
	 * @param possibleNextState
	 */
	@Override
	public void update(StateT currentState, StateT possibleNextState) {
		update(currentState, Arrays.asList(possibleNextState));
	}

	/**
	 * Performs a model update according to a learning scheme (currently
	 * SampleRank). The update step is scaled with the provided alpha value and
	 * normalized with the number of states in this batch.
	 * 
	 * @param currentState
	 * @param possibleNextState
	 */
	@Override
	public void update(final StateT currentState, List<StateT> possibleNextState) {
		Map<AbstractTemplate<StateT>, Vector> weightUpdates = new HashMap<>();
		model.getTemplates().forEach(t -> weightUpdates.put(t, new Vector()));
		possibleNextState.forEach(s -> sampleRankUpdate(currentState, s, weightUpdates));
		if (normalize) {
			applyWeightUpdate(weightUpdates, possibleNextState.size());
		} else {
			applyWeightUpdate(weightUpdates, 1);
		}
		updates++;
	}

	private void sampleRankUpdate(StateT currentState, StateT possibleNextState,
			Map<AbstractTemplate<StateT>, Vector> weightUpdates) {
		log.trace("Current:\t%s", currentState);
		log.trace("Next:\t%s", possibleNextState);

		double weightedDifferenceSum = 0;
		/*
		 * Collect differences of features for both states and remember
		 * respective template
		 */
		long diffID = TaggedTimer.start("UP-DIFF");
		Map<AbstractTemplate<StateT>, Vector> featureDifferences = new HashMap<>();
		for (AbstractTemplate<StateT> t : model.getTemplates()) {
			Vector differences = getFeatureDifferences(t, possibleNextState, currentState);
			featureDifferences.put(t, differences);
			weightedDifferenceSum += differences.dotProduct(t.getWeightVector());
		}
		TaggedTimer.stop(diffID);

		if (weightedDifferenceSum > 0 && preference(currentState, possibleNextState)) {
			updateFeatures(featureDifferences, -currentAlpha, weightUpdates);
		} else if (weightedDifferenceSum <= 0 && preference(possibleNextState, currentState)) {
			updateFeatures(featureDifferences, +currentAlpha, weightUpdates);
		} else {
		}
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
	public Vector getFeatureDifferences(AbstractTemplate<StateT> template, StateT state1, StateT state2) {
		Vector diff = new Vector();
		Collection<AbstractFactor> factors1 = state1.getFactorGraph().getFactors();
		Collection<AbstractFactor> factors2 = state2.getFactorGraph().getFactors();

		for (AbstractFactor factor : factors1) {
			if (factor.getTemplate() == template) {
				diff.add(factor.getFeatureVector());
			}
		}
		for (AbstractFactor factor : factors2) {
			if (factor.getTemplate() == template) {
				diff.sub(factor.getFeatureVector());
			}
		}
		return diff;
	}

	/**
	 * The features present in the vectors in the featureDifferences map are
	 * update according to their respective difference and the given direction.
	 * Since the feature difference (times the direction) is used as the update
	 * step, differences of 0 are not applied since they do not change the
	 * weight anyway.
	 * 
	 * @param featureDifferences
	 * @param learningDirection
	 */
	private void updateFeatures(Map<AbstractTemplate<StateT>, Vector> featureDifferences, double learningDirection,
			Map<AbstractTemplate<StateT>, Vector> weightUpdates) {
		long upID = TaggedTimer.start("UP-COLLECT");
		log.trace("UPDATE: learning direction: %s", learningDirection);
		for (AbstractTemplate<StateT> t : model.getTemplates()) {
			log.trace("Template: %s", t.getClass().getSimpleName());
			Vector features = featureDifferences.get(t);
			Vector updates = weightUpdates.get(t);
			for (Entry<String, Double> featureDifference : features.getFeatures().entrySet()) {
				// only update for real differences
				if (featureDifference.getValue() != 0) {
					double learningStep = learningDirection * featureDifference.getValue();
					log.trace("\t%s -> %s:\t%s", featureDifference.getValue(), learningStep,
							featureDifference.getKey());
					updates.addToValue(featureDifference.getKey(), learningStep);
				}
			}
		}
		TaggedTimer.stop(upID);
	}

	/**
	 * Applies the previously collected weight updates in one step.
	 * 
	 * @param weightUpdates
	 * @param numberOfUpdates
	 */
	private void applyWeightUpdate(Map<AbstractTemplate<StateT>, Vector> weightUpdates, int numberOfUpdates) {
		long upID = TaggedTimer.start("UP-UPDATE");
		for (AbstractTemplate<StateT> t : model.getTemplates()) {
			log.trace("Template: %s", t.getClass().getSimpleName());
			Vector weightUpdatesForTemplate = weightUpdates.get(t);
			for (Entry<String, Double> weightUpdate : weightUpdatesForTemplate.getFeatures().entrySet()) {
				// only update for real differences
				if (weightUpdate.getValue() != 0) {
					double learningStep = weightUpdate.getValue() / numberOfUpdates;
					log.trace("\t%s -> %s:\t%s", weightUpdate.getValue(), learningStep, weightUpdate.getKey());
					t.update(weightUpdate.getKey(), learningStep);
				}
			}
		}
		TaggedTimer.stop(upID);
	}

	/**
	 * Compares the objective scores of the state1 and state2 using the
	 * precomputed objective scores to decide if state1 is preferred over
	 * state2. Note: The objective scores are merely accessed but not
	 * recomputed. This step needs to be done before.
	 * 
	 * @param state1
	 * @param state2
	 * @param goldState
	 * @return
	 */
	private boolean preference(StateT state1, StateT state2) {
		double O1 = state1.getObjectiveScore();
		double O2 = state2.getObjectiveScore();
		return O1 > O2;
	}

	public Model<StateT> getModel() {
		return model;
	}

}
