package learning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import learning.optimizer.Optimizer;
import templates.AbstractTemplate;
import utility.VectorUtil;
import variables.AbstractState;

public class AdvancedLearner<StateT extends AbstractState<?>> implements Learner<StateT> {

	private static final double MARGIN = 1;

	private static Logger log = LogManager.getFormatterLogger(AdvancedLearner.class.getName());

	private Model<?, StateT> model;
	private boolean normalize = true;
	public int updates = 0;

	private Optimizer optimizer;

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
	public AdvancedLearner(Model<?, StateT> model, Optimizer optimizer) {
		super();
		this.model = model;
		this.optimizer = optimizer;
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
		System.out.println("##########################");
		Map<AbstractTemplate<?, StateT, ?>, Vector> batchGradients = new HashMap<>();
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			batchGradients.put(t, new Vector());
		}
		/**
		 * Rank each possible next state against the current state
		 */
		possibleNextStates.forEach(
				possibleNextState -> collectMarginRankGradients(currentState, possibleNextState, batchGradients));

		applyWeightUpdate(batchGradients);
		updates++;
//		try {
//			Thread.sleep(200);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// System.out.println("##########################");
	}
	//
	// /**
	// * Updates the weights of each template according to the SampleRank
	// scheme.
	// *
	// * @param currentState
	// * @param possibleNextState
	// * @param weightUpdates
	// */
	// private void collectSampleRankGradients(StateT currentState, StateT
	// possibleNextState,
	// Map<AbstractTemplate<?, StateT, ?>, Vector> weightUpdates) {
	// log.trace("Current:\t%s", currentState);
	// log.trace("Next:\t%s", possibleNextState);
	//
	// double weightedDifferenceSum = 0;
	// /*
	// * Collect differences of features for both states and remember
	// * respective template
	// */
	// Map<AbstractTemplate<?, StateT, ?>, Vector> featureDifferences = new
	// HashMap<>();
	// for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
	// Vector differences = VectorUtil.getFeatureDifferences(t,
	// possibleNextState, currentState);
	// featureDifferences.put(t, differences);
	// weightedDifferenceSum += differences.dotProduct(t.getWeights());
	// }
	//
	// if (weightedDifferenceSum > 0 && preference(currentState,
	// possibleNextState)) {
	// System.out.println("INCREASE");
	// System.out.println("DIFF: " + featureDifferences.values());
	// updateFeatures(featureDifferences, +1, weightUpdates);
	// } else if (weightedDifferenceSum <= 0 && preference(possibleNextState,
	// currentState)) {
	// System.out.println("DECREASE");
	// System.out.println("DIFF: " + featureDifferences.values());
	// updateFeatures(featureDifferences, -1, weightUpdates);
	// } else {
	// }
	// // System.out.println("DIFF: " + featureDifferences.values());
	// }

	/**
	 * Updates the weights of each template according to the Margin Rank scheme.
	 * 
	 * @param currentState
	 * @param possibleNextState
	 * @param batchGradients
	 */
	private void collectMarginRankGradients(StateT currentState, StateT possibleNextState,
			Map<AbstractTemplate<?, StateT, ?>, Vector> batchGradients) {
		log.trace("Current:\t%s", currentState);
		log.trace("Next:\t%s", possibleNextState);

		double weightedDifferenceSum = 0;
		/*
		 * Collect differences of features for both states and remember
		 * respective template
		 */
		Map<AbstractTemplate<?, StateT, ?>, Vector> featureDifferences = new HashMap<>();
		StateT posState = null;
		StateT negState = null;

		if (preference(possibleNextState, currentState)) {
			// possibleNextState is POS
			// currentState is NEG
			posState = possibleNextState;
			negState = currentState;

			System.out.println("CURRENT - NEXT");
			// System.out.println("POS: NEXT");
			// System.out.println("NEG: CURRENT");
		} else {
			// currentState is POS
			// possibleNextState is NEG
			System.out.println("NEXT - CURRENT");
			// System.out.println("POS: CURRENT");
			// System.out.println("NEG: NEXT");
			posState = currentState;
			negState = possibleNextState;
		}

		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			Vector differences = VectorUtil.getFeatureDifferences(t, negState, posState);
			featureDifferences.put(t, differences);
			weightedDifferenceSum += differences.dotProduct(t.getWeights());
		}

		if (weightedDifferenceSum + MARGIN > 0) {
			System.out.println("UPDATE:");
			System.out.println(weightedDifferenceSum);
			System.out.println("DIFF: " + featureDifferences.values());
			/*
			 * gradient for weight w[i] is simply featureDifference[i].
			 */
			for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
				log.trace("Template: %s", t.getClass().getSimpleName());
				Vector weightGradient = featureDifferences.get(t);
				Vector templateBatchGradients = batchGradients.get(t);
				templateBatchGradients.addToValue(weightGradient);
			}
		}
	}

	/**
	 * Applies the previously collected weight updates in one step.
	 * 
	 * @param weightGradients
	 * @param numberOfUpdates
	 */
	private void applyWeightUpdate(Map<AbstractTemplate<?, StateT, ?>, Vector> batchGradients) {
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			log.trace("Template: %s", t.getClass().getSimpleName());
			Vector templateGradients = batchGradients.get(t);
			System.out.println("GRAD: " + templateGradients);
			Vector oldWeights = t.getWeights();
			System.out.println("OLD: " + oldWeights);
			Vector newWeights = optimizer.getUpdates(oldWeights, templateGradients);
			System.out.println("NEW: " + newWeights);
			t.setWeights(newWeights);
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

}
