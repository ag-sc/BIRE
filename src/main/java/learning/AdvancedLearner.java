package learning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import learning.optimizer.Optimizer;
import learning.regularizer.Regularizer;
import templates.AbstractTemplate;
import utility.VectorUtil;
import variables.AbstractState;

/**
 * This learner implements a margin rank learning scheme with modular parameter
 * optimization and regularization.
 * 
 * @author sjebbara
 *
 * @param <StateT>
 */
public class AdvancedLearner<StateT extends AbstractState<?>> implements Learner<StateT> {

//	public static class TrainingTriple<StateT> {
//		private StateT parentState;
//		private StateT candidateState;
//		private double sampleWeight;
//
//		public TrainingTriple(StateT parentState, StateT candidateState, double sampleWeight) {
//			super();
//			this.parentState = parentState;
//			this.candidateState = candidateState;
//			this.sampleWeight = sampleWeight;
//		}
//
//		public StateT getParentState() {
//			return parentState;
//		}
//
//		public StateT getCandidateState() {
//			return candidateState;
//		}
//
//		public double getSampleWeight() {
//			return sampleWeight;
//		}
//
//	}

	private double margin = 0.1;

	private static Logger log = LogManager.getFormatterLogger(AdvancedLearner.class.getName());

	private Model<?, StateT> model;
	private boolean normalize = true;
	public int updates = 0;

	private Optimizer optimizer;
	private Regularizer regularizer;

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
	public AdvancedLearner(Model<?, StateT> model, Optimizer optimizer) {
		super();
		this.model = model;
		this.optimizer = optimizer;
	}

	public AdvancedLearner(Model<?, StateT> model, Optimizer optimizer, Regularizer regularizer) {
		super();
		this.model = model;
		this.optimizer = optimizer;
		this.regularizer = regularizer;
	}

	public AdvancedLearner(Model<?, StateT> model, Optimizer optimizer, Regularizer regularizer, double margin) {
		super();
		this.model = model;
		this.optimizer = optimizer;
		this.regularizer = regularizer;
		this.margin = margin;
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
//	
//	@Override
//	public void update(List<TrainingTriple<StateT>> triples) {

		Map<AbstractTemplate<?, StateT, ?>, Vector> batchGradients = new HashMap<>();
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			batchGradients.put(t, new Vector());
		}
		/**
		 * Rank each possible next state against the current state
		 */
//		triples.forEach(t -> collectMarginRankGradients(t, batchGradients));

//		for (StateT t : possibleNextStates) {
//		}
		collectMarginRankGradients(currentState, possibleNextState, batchGradients, 1);

		applyWeightUpdate(batchGradients);
		updates++;
	}

	/**
	 * Updates the weights of each template according to the Margin Rank scheme.
	 * 
	 * @param t2
	 * @param currentState
	 * 
	 * @param currentState
	 * @param possibleNextState
	 * @param batchGradients
	 * @param i
	 */
	private void collectMarginRankGradients(
//			TrainingTriple<StateT> triple,

			final StateT currentState, final StateT possibleNextState,
			Map<AbstractTemplate<?, StateT, ?>, Vector> batchGradients, int sampleWeight) {
//		final StateT currentState = triple.getParentState();
//	final 	StateT possibleNextState = triple.getCandidateState();
//final double sampleWeight = triple.getSampleWeight();
		log.trace("Current:\t%s", currentState);
		log.trace("Next:\t%s", possibleNextState);

		double linearScore = 0;
		/*
		 * Collect differences of features for both states and remember respective
		 * template
		 */
		Map<AbstractTemplate<?, StateT, ?>, Vector> featureDifferences = new HashMap<>();
		StateT posState = null;
		StateT negState = null;

		if (preference(possibleNextState, currentState)) {
			// possibleNextState is POS
			// currentState is NEG
			posState = possibleNextState;
			negState = currentState;
		} else {
			// currentState is POS
			// possibleNextState is NEG
			posState = currentState;
			negState = possibleNextState;
		}
		for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
			Vector differences = VectorUtil.getFeatureDifferences(t, negState, posState);
			featureDifferences.put(t, differences);
			linearScore += differences.dotProduct(t.getWeights());
			if (regularizer != null) {
				linearScore -= regularizer.penalize(t.getWeights());
			}
		}

		if (linearScore + margin >= 0) {
			/*
			 * gradient for weight w[i] is simply featureDifference[i].
			 */
			for (AbstractTemplate<?, StateT, ?> t : model.getTemplates()) {
				log.trace("Template: %s", t.getClass().getSimpleName());
				Vector weightGradient = featureDifferences.get(t);
				if (regularizer != null) {
					weightGradient = regularizer.regularize(weightGradient, t.getWeights());
				}
				if (sampleWeight == 0) {
					weightGradient = new Vector();
				} else if (sampleWeight != 1) {
					weightGradient = weightGradient.mul(sampleWeight);
				}
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
			Vector oldWeights = t.getWeights();
			Vector newWeights = optimizer.getUpdates(oldWeights, templateGradients);
			t.setWeights(newWeights);
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
