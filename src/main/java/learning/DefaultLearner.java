package learning;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import templates.AbstractTemplate;
import utility.VectorUtil;
import variables.AbstractState;

public class DefaultLearner<StateT extends AbstractState<?>> implements Learner<StateT> {

	private static Logger log = LogManager.getFormatterLogger(DefaultLearner.class.getName());

	private double alpha;
	private Model<?, StateT> model;
	private boolean normalize = true;
	public double currentAlpha;
	public int updates = 0;
	/**
	 * A regularization parameter to punish big feature weights.
	 */
	protected double l2 = 0.01F;

	/**
	 * This implementation of the learner implements the SampleRank learning scheme.
	 * Very generally speaking, given a pair of states, the learner changes the
	 * weights of the model such that the scores of the model are aligned with the
	 * preference of the objective function. As a slight modification, this
	 * implementation allows mini-batch updates.
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

//	/**
//	 * Performs a model update according to a learning scheme (currently
//	 * SampleRank). The update step is scaled with the provided alpha value.
//	 * 
//	 * @param currentState
//	 * @param possibleNextState
//	 */
	@Override
	public void update(StateT currentState, StateT possibleNextState) {
//		update(currentState, Arrays.asList(possibleNextState));
//	}
//
//	/**
//	 * Performs a model update according to a learning scheme (currently
//	 * SampleRank). The update step is scaled with the provided alpha value and
//	 * normalized with the number of states in this batch.
//	 * 
//	 * @param currentState
//	 * @param possibleNextStates
//	 */
//	@Override
//	public void update(final StateT currentState, List<StateT> possibleNextStates) {
//		update(possibleNextStates.stream().map(s -> new TrainingTriple<>(currentState, s, 1))
//				.collect(Collectors.toList()));
//	}
//
//	@Override
//	public void update(List<TrainingTriple<StateT>> triples) {
		Map<AbstractTemplate<?, StateT, ?>, Vector> weightGradients = new HashMap<>();
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			weightGradients.put(t, new Vector());
		}
		/**
		 * Rank each possible next state against the current state
		 */
//		triples.forEach(t -> collectSampleRankGradients(t.getParentState(), t.getCandidateState(), weightGradients));
		collectSampleRankGradients(currentState, possibleNextState, weightGradients);
//		if (normalize) {
//			applyWeightUpdate(weightGradients, triples.size());
//		} else {
		applyWeightUpdate(weightGradients, 1);
//		}
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
		 * Collect differences of features for both states and remember respective
		 * template
		 */
		Map<AbstractTemplate<?, StateT, ?>, Vector> featureDifferences = new HashMap<>();
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			Vector differences = VectorUtil.getFeatureDifferences(t, possibleNextState, currentState);
			featureDifferences.put(t, differences);
			weightedDifferenceSum += differences.dotProduct(t.getWeights());
		}

		if (weightedDifferenceSum > 0 && preference(currentState, possibleNextState)) {
			aggregateGradients(featureDifferences, +1, weightUpdates);
		} else if (weightedDifferenceSum <= 0 && preference(possibleNextState, currentState)) {
			aggregateGradients(featureDifferences, -1, weightUpdates);
		} else {
		}
	}

	/**
	 * The features present in the vectors in the featureDifferences map are update
	 * according to their respective difference and the given direction. Since the
	 * feature difference (times the direction) is used as the update step,
	 * differences of 0 are not applied since they do not change the weight anyway.
	 * 
	 * @param featureDifferences
	 * @param learningDirection
	 */
	private void aggregateGradients(Map<AbstractTemplate<?, StateT, ?>, Vector> featureDifferences,
			double learningDirection, Map<AbstractTemplate<?, StateT, ?>, Vector> weightUpdates) {
		log.trace("UPDATE: learning direction: %s", learningDirection);

		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			log.trace("Template: %s", t.getClass().getSimpleName());
			Vector features = featureDifferences.get(t);
			Vector gradients = weightUpdates.get(t);

			for (Entry<String, Double> featureDifference : features.getFeatures().entrySet()) {
				double weightGradient = learningDirection * featureDifference.getValue();
				log.trace("\t%s -> %s:\t%s", featureDifference.getValue(), weightGradient, featureDifference.getKey());
//				Vector.getName(featureDifference.getKey()));
				gradients.addToValue(featureDifference.getKey(), weightGradient);
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
			Vector weights = t.getWeights();
			for (Entry<String, Double> weightGradient : weightUpdatesForTemplate.getFeatures().entrySet()) {
				double gradient = weightGradient.getValue() / numberOfUpdates
						- l2 * weights.getValueOfFeature(weightGradient.getKey());
				log.trace("\t%s -> %s:\t%s", weightGradient.getValue(), gradient, currentAlpha,
						weightGradient.getKey());
//				Vector.getName(weightGradient.getKey()));
				t.update(weightGradient.getKey(), -gradient * currentAlpha);
			}
		}
	}

	/**
	 * Compares the objective scores of the state1 and state2 using the precomputed
	 * objective scores to decide if state1 is preferred over state2. Note: The
	 * objective scores are merely accessed but not recomputed. This step needs to
	 * be done before.
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

}
