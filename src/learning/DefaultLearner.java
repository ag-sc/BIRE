package learning;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.Instance;
import exceptions.MissingFactorException;
import factors.Factor;
import learning.callbacks.EpochCallback;
import learning.callbacks.InstanceCallback;
import templates.AbstractTemplate;
import variables.AbstractState;

public class DefaultLearner<StateT extends AbstractState<?>>
		implements Learner<StateT>, InstanceCallback, EpochCallback {

	private static Logger log = LogManager.getFormatterLogger(DefaultLearner.class.getName());

	private double alpha;
	private Model<?, StateT> model;
	private boolean normalize = true;
	public double currentAlpha;
	public int updates = 0;

	/**
	 * This implementation of the learner implements the SampleRank learning
	 * scheme. Very generally speaking, given a pair of states, the learner
	 * changes the weights of the model such that the scores of the model are
	 * aligned with the preference of the objective function. As a slight
	 * modification, this implementation allows mini-batch updates.
	 * 
	 * @param model
	 * @param alpha
	 */
	public DefaultLearner(Model<?, StateT> model, double alpha) {
		super();
		this.model = model;
		this.alpha = alpha;
		this.currentAlpha = this.alpha;
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
	 * @param possibleNextStates
	 */
	@Override
	public void update(final StateT currentState, List<StateT> possibleNextStates) {
		Map<AbstractTemplate<?, StateT, ?>, Vector> weightGradients = new HashMap<>();
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			weightGradients.put(t, new Vector());
		}
		/**
		 * Rank each possible next state against the current state
		 */
		possibleNextStates.forEach(
				possibleNextState -> collectSampleRankGradients(currentState, possibleNextState, weightGradients));
		if (normalize) {
			applyWeightUpdate(weightGradients, possibleNextStates.size());
		} else {
			applyWeightUpdate(weightGradients, 1);
		}
		updates++;
	}

	/**
	 * Updates the weights of each template according to the SampleRank scheme.
	 * 
	 * @param currentState
	 * @param possibleNextState
	 * @param weightUpdates
	 */
	private void collectSampleRankGradients(StateT currentState, StateT possibleNextState,
			Map<AbstractTemplate<?, StateT, ?>, Vector> weightUpdates) {
		log.trace("Current:\t%s", currentState);
		log.trace("Next:\t%s", possibleNextState);

		double weightedDifferenceSum = 0;
		/*
		 * Collect differences of features for both states and remember
		 * respective template
		 */
		Map<AbstractTemplate<?, StateT, ?>, Vector> featureDifferences = new HashMap<>();
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			Vector differences = getFeatureDifferences(t, possibleNextState, currentState);
			featureDifferences.put(t, differences);
			weightedDifferenceSum += differences.dotProduct(t.getWeights());
		}

		if (weightedDifferenceSum > 0 && preference(currentState, possibleNextState)) {
			updateFeatures(featureDifferences, -1, weightUpdates);
		} else if (weightedDifferenceSum <= 0 && preference(possibleNextState, currentState)) {
			updateFeatures(featureDifferences, +1, weightUpdates);
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
	public Vector getFeatureDifferences(AbstractTemplate<?, StateT, ?> template, StateT state1, StateT state2) {
		Vector diff = new Vector();
		Collection<Factor<?>> factors1 = null;
		Collection<Factor<?>> factors2 = null;
		try {
			factors1 = state1.getFactorGraph().getFactors();
			factors2 = state2.getFactorGraph().getFactors();
		} catch (MissingFactorException e) {
			e.printStackTrace();
		}
		for (Factor<?> factor : factors1) {
			if (factor.getTemplate() == template) {
				diff.add(factor.getFeatureVector());
			}
		}
		for (Factor<?> factor : factors2) {
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
	private void updateFeatures(Map<AbstractTemplate<?, StateT, ?>, Vector> featureDifferences,
			double learningDirection, Map<AbstractTemplate<?, StateT, ?>, Vector> weightUpdates) {
		log.trace("UPDATE: learning direction: %s", learningDirection);

		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			log.trace("Template: %s", t.getClass().getSimpleName());
			Vector features = featureDifferences.get(t);
			Vector gradients = weightUpdates.get(t);

			for (Entry<String, Double> featureDifference : features.getFeatures().entrySet()) {
				// only update for real differences
				if (featureDifference.getValue() != 0) {
					double weightGradient = learningDirection * featureDifference.getValue();
					log.trace("\t%s -> %s:\t%s", featureDifference.getValue(), weightGradient,
							featureDifference.getKey());
					gradients.addToValue(featureDifference.getKey(), weightGradient);
				}
			}
		}
	}

	/**
	 * Applies the previously collected weight updates in one step.
	 * 
	 * @param weightGradients
	 * @param numberOfUpdates
	 */
	private void applyWeightUpdate(Map<AbstractTemplate<?, StateT, ?>, Vector> weightGradients, int numberOfUpdates) {
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			log.trace("Template: %s", t.getClass().getSimpleName());
			Vector weightUpdatesForTemplate = weightGradients.get(t);
			for (Entry<String, Double> weightGradient : weightUpdatesForTemplate.getFeatures().entrySet()) {
				// only update for actual differences
				if (weightGradient.getValue() != 0) {
					double gradient = weightGradient.getValue() / numberOfUpdates;
					log.trace("\t%s -> %s:\t%s", weightGradient.getValue(), gradient, currentAlpha,
							weightGradient.getKey());
					t.update(weightGradient.getKey(), gradient, currentAlpha);
				}
			}
		}
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

	public Model<?, StateT> getModel() {
		return model;
	}

	@Override
	public <InstanceT extends Instance> void onStartInstance(Trainer caller, InstanceT instance, int indexOfInstance,
			int numberOfInstances, int epoch, int numberOfEpochs) {
		double fraction = ((float) (indexOfInstance + epoch * numberOfInstances))
				/ (numberOfEpochs * numberOfInstances);
		currentAlpha = (alpha * 0.99) * (1 - fraction) + alpha * 0.01;
	}

	@Override
	public void onStartEpoch(Trainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
		double fraction = ((float) epoch) / (numberOfEpochs);
		currentAlpha = (alpha * 0.9) * (1 - fraction) + alpha * 0.01;
	}

	@Override
	public <InstanceT extends Instance> void onEndInstance(Trainer caller, InstanceT document, int indexOfDocument,
			int numberOfDocuments, AbstractState<?> finalState, int epoch, int numberOfEpochs) {

	}
}
