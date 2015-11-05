package learning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import evaluation.TaggedTimer;
import factors.AbstractFactor;
import templates.AbstractTemplate;
import variables.AbstractState;

public class DefaultLearner<StateT extends AbstractState> implements Learner<StateT> {

	private static Logger log = LogManager.getFormatterLogger(DefaultLearner.class.getName());

	enum LearningProcedure {
		SAMPLE_RANK, PERCEPTRON;
	}

	private LearningProcedure learningProcedure = LearningProcedure.SAMPLE_RANK;
	private double alpha;
	private Model<StateT> model;
	private Scorer<StateT> scorer;
	private boolean normalize = true;
	public double currentAlpha;
	public int updates = 0;

	public DefaultLearner(Model<StateT> model, Scorer<StateT> scorer, double alpha) {
		super();
		this.model = model;
		this.scorer = scorer;
		this.currentAlpha = alpha;
	}

	/**
	 * Performs a model update according to a learning scheme (currently
	 * SampleRank or our custom perceptron learning). The update step is scaled
	 * with the provided alpha value and normalized with the number of states in
	 * this batch.
	 * 
	 * @param goldState
	 * @param currentState
	 * @param possibleNextState
	 */
	@Override
	public void update(StateT currentState, StateT possibleNextState) {
		update(currentState, Arrays.asList(possibleNextState));
	}

	@Override
	public void update(final StateT currentState, List<StateT> possibleNextState) {
		Map<AbstractTemplate<StateT>, Vector> weightUpdates = new HashMap<>();
		model.getTemplates().forEach(t -> weightUpdates.put(t, new Vector()));
		switch (learningProcedure) {
		case SAMPLE_RANK:
			// Collect weight updates for each possible state/current state pair
			possibleNextState.forEach(s -> sampleRankUpdate(currentState, s, weightUpdates));
			break;
		// case PERCEPTRON:
		// perceptronUpdate(goldState, currentState, possibleNextState);
		// break;
		default:
			log.warn("Cannot perform unknown update procedure \"%s\"", learningProcedure);
			break;
		}
		// weightUpdates.values().forEach(v ->
		// v.getFeatures().entrySet().forEach(e -> System.out.println(e)));
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
		Set<AbstractFactor> factors1 = state1.getFactorGraph().getFactors();
		Set<AbstractFactor> factors2 = state2.getFactorGraph().getFactors();

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

	// private void perceptronUpdate(StateT goldState, StateT currentState,
	// StateT possibleNextState) {
	// double On = objective.score(possibleNextState, goldState).score;
	// double Oc = objective.score(currentState, goldState).score;
	//
	// double Mn = possibleNextState.getModelScore();
	// double Mc = currentState.getModelScore();
	//
	// log.trace("Next %s:\tO(g,c)=%s,\tO(g,n)=%s\t|\tM(c)=%s,\tM(n)=%s",
	// possibleNextState.getID(), Oc, On, Mc, Mn);
	//
	// double learningSignal = 1;
	// double learningStep = alpha * learningSignal;
	// if (On > Oc) { // On-Oc > 0
	// if (Mn <= Mc) { // Mn-Mc < 0
	// log.trace("Model: DECREASE current; INCREASE next");
	// model.update(currentState, -learningStep);
	// model.update(possibleNextState, +learningStep);
	// }
	// } else if (On < Oc) { // On-Oc < 0
	// if (Mn >= Mc) { // Mn-Mc > 0
	// log.trace("Model: INCREASE current; DECREASE next");
	// model.update(currentState, +learningStep);
	// model.update(possibleNextState, -learningStep);
	// }
	// } else if (On == Oc) {
	// // if (Mn > Mc) { // Mn-Mc > 0
	// // log.info("Model: DECREASE next; INCREASE current");
	// // model.update(currentState, -learningStep);
	// // model.update(possibleNextState, +learningStep);
	// // } else if (Mn < Mc) { // Mn-Mc > 0
	// // log.info("Model: INCREASE next; DECREASE current");
	// // model.update(currentState, +learningStep);
	// // model.update(possibleNextState, -learningStep);
	// // }
	// // log.info("Current state %s and next State %s are equally
	// // good.
	// // Do nothing.",
	// // currentState.getID(), possibleNextState.getID());
	// }
	// }

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
		// double O1 = objective.score(state1, goldState).score;
		// double O2 = objective.score(state2, goldState).score;
		return O1 > O2;
	}

	public Model<StateT> getModel() {
		return model;
	}

	public Scorer<StateT> getScorer() {
		return scorer;
	}

}
