package sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import evaluation.TaggedTimer;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.Scorer;
import learning.Trainer;
import learning.callbacks.EpochCallback;
import sampling.stoppingcriterion.StepLimitCriterion;
import sampling.stoppingcriterion.StoppingCriterion;
import variables.AbstractState;

public class DefaultSampler<StateT extends AbstractState, ResultT> implements Sampler<StateT, ResultT>, EpochCallback {

	enum SamplingStrategy {
		GREEDY, LINEAR_SAMPLING, SOFTMAX_SAMPLING;
	}

	private static Logger log = LogManager.getFormatterLogger(DefaultSampler.class.getName());
	protected Model<StateT> model;
	protected Scorer<StateT> scorer;
	protected ObjectiveFunction<StateT, ResultT> objective;
	private List<Explorer<StateT>> explorers;
	private StoppingCriterion<StateT> stoppingCriterion;

	protected final boolean multiThreaded = true;
	/**
	 * Defines the sampling strategy for the training phase. The test phase currently always uses the greedy variant.
	 */
	private SamplingStrategy samplingStrategy = SamplingStrategy.LINEAR_SAMPLING;
	private boolean useModelDuringTraining = true;

	/**
	 * The DefaultSampler implements the Sampler interface. This sampler divides
	 * the sampling procedure in the exploration of the search space (using
	 * Explorers) and the actual sampling that happens in this class. It is
	 * designed to be flexible in the actual sampling strategy and the stopping
	 * criterion.
	 * 
	 * @param model
	 * @param scorer
	 * @param objective
	 * @param explorers
	 * @param stoppingCriterion
	 */
	public DefaultSampler(Model<StateT> model, Scorer<StateT> scorer, ObjectiveFunction<StateT, ResultT> objective,
			List<Explorer<StateT>> explorers, StoppingCriterion<StateT> stoppingCriterion) {
		super();
		this.model = model;
		this.scorer = scorer;
		this.objective = objective;
		this.explorers = explorers;
		this.stoppingCriterion = stoppingCriterion;
	}

	/**
	 * The DefaultSampler implements the Sampler interface. This sampler divides
	 * the sampling procedure in the exploration of the search space (using
	 * Explorers) and the actual sampling that happens in this class. It is
	 * designed to be flexible in the actual sampling strategy and the stopping
	 * criterion. This constructor uses a simple step limit as the stopping
	 * criterion.
	 * 
	 * @param model
	 * @param scorer
	 * @param objective
	 * @param explorers
	 * @param samplingSteps
	 */
	public DefaultSampler(Model<StateT> model, Scorer<StateT> scorer, ObjectiveFunction<StateT, ResultT> objective,
			List<Explorer<StateT>> explorers, int samplingSteps) {
		super();
		this.model = model;
		this.scorer = scorer;
		this.objective = objective;
		this.explorers = explorers;
		this.stoppingCriterion = new StepLimitCriterion<>(samplingSteps);
	}

	@Override
	public void onEndEpoch(Trainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
		useModelDuringTraining = !useModelDuringTraining;
	}

	@Override
	public List<StateT> generateChain(StateT initialState, ResultT goldResult, Learner<StateT> learner) {
		List<StateT> generatedChain = new ArrayList<>();

		StateT currentState = initialState;
		int step = 0;

		do {
			log.info("---------------------------");
			for (Explorer<StateT> explorer : explorers) {
				log.info("...............");
				log.info("TRAINING Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
				currentState = performTrainingStep(learner, explorer, goldResult, currentState);
				generatedChain.add(currentState);
				log.info("Sampled State:  %s", currentState);
			}
			step++;
		} while (!stoppingCriterion.checkCondition(generatedChain, step));

		log.info("Stop sampling after step %s", step);
		return generatedChain;
	}

	@Override
	public List<StateT> generateChain(StateT initialState) {
		List<StateT> generatedChain = new ArrayList<>();
		StateT currentState = initialState;

		int step = 0;
		do {
			log.info("---------------------------");
			for (Explorer<StateT> explorer : explorers) {
				log.info("...............");
				log.info("PREDICTION Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
				currentState = performPredictionStep(explorer, currentState);
				generatedChain.add(currentState);
				log.info("Sampled State:  %s", currentState);
			}
			step++;
		} while (!stoppingCriterion.checkCondition(generatedChain, step));
		log.info("Stop sampling after step %s", step);
		return generatedChain;
	}

	/**
	 * Generates states, computes features, scores states, and updates the
	 * model. After that a successor state is selected.
	 * 
	 * @param learner
	 * @param explorer
	 * @param goldResult
	 * @param currentState
	 * @return
	 */

	protected StateT performTrainingStep(Learner<StateT> learner, Explorer<StateT> explorer, ResultT goldResult,
			StateT currentState) {
		log.debug("TRAINING:");
		/**
		 * Generate possible successor states.
		 */
		List<StateT> nextStates = explorer.getNextStates(currentState);
		List<StateT> allStates = new ArrayList<>(nextStates);
		allStates.add(currentState);
		/**
		 * Compute objective function scores
		 */
		scoreWithObjective(allStates, goldResult);
		/**
		 * Apply templates to states and, thus generate factors and features
		 */
		unroll(allStates);
		/**
		 * Score all states according to the model.
		 */
		scoreWithModel(allStates);
		/**
		 * Sample one possible successor from model distribution
		 */
		StateT candidateState = null;
		switch (samplingStrategy) {
		case GREEDY:
			nextStates.sort(AbstractState.modelScoreComparator);
			candidateState = nextStates.get(0);
			break;
		case LINEAR_SAMPLING:
			candidateState = SamplingUtils.drawFromDistribution(nextStates, true, false);
			break;
		case SOFTMAX_SAMPLING:
			candidateState = SamplingUtils.drawFromDistribution(nextStates, true, true);
			break;
		}
		/**
		 * Update model with selected state
		 */
		learner.update(currentState, candidateState);
		/**
		 * Recompute model score to reflect last update in score.
		 */
		log.debug("(Re)Score:");
		scoreWithModel(Arrays.asList(currentState, candidateState));

		/**
		 * Choose to accept or reject selected state
		 */
		return SamplingUtils.accept(candidateState, currentState, true) ? candidateState : currentState;
	}

	/**
	 * Generates states, computes features and scores states. After that a
	 * successor state is selected.
	 * 
	 * @param explorer
	 * @param currentState
	 * @return
	 */
	protected StateT performPredictionStep(Explorer<StateT> explorer, StateT currentState) {
		log.debug("PREDICTION:");
		/**
		 * Generate possible successor states.
		 */
		List<StateT> nextStates = explorer.getNextStates(currentState);
		List<StateT> allStates = new ArrayList<>(nextStates);
		allStates.add(currentState);
		/**
		 * Apply templates to states and thus generate factors and features
		 */
		unroll(allStates);
		/**
		 * Score all states according to the model.
		 */
		scoreWithModel(allStates);
		/**
		 * Select a successor state from the list of possible successors.
		 */
		currentState = selectNextState(currentState, nextStates, true, SamplingStrategy.GREEDY);
		return currentState;
	}

	/**
	 * Applies the model's template to each of the given states, thus unrolling
	 * (computing) the features. The <i>multiThreaded</i> flag determines if the
	 * computation is performed in parallel or sequentially.
	 * 
	 * @param currentState
	 * @param nextStates
	 */
	protected void unroll(List<StateT> allStates) {
		long unrollID = TaggedTimer.start("SC-UNROLL");
		log.debug("Unroll features for %s states...", allStates.size() + 1);

		Stream<StateT> stream = null;
		if (multiThreaded) {
			stream = allStates.parallelStream();
		} else {
			stream = allStates.stream();
		}
		stream.forEach(s -> model.unroll(s));
		TaggedTimer.stop(unrollID);
	}

	/**
	 * Computes the model scores for each of the given states. The
	 * <i>multiThreaded</i> flag determines if the computation is performed in
	 * parallel or sequentially.
	 * 
	 * @param nextStates
	 */
	protected void scoreWithModel(List<StateT> nextStates) {
		long scID = TaggedTimer.start("MODEL-SCORE");
		log.debug("Score %s states according to model...", nextStates.size());

		Stream<StateT> stream = null;
		if (multiThreaded) {
			stream = nextStates.parallelStream();
		} else {
			stream = nextStates.stream();
		}
		stream.forEach(s -> scorer.score(s));
		TaggedTimer.stop(scID);
	}

	/**
	 * Computes the objective scores for each of the given states. The
	 * <i>multiThreaded</i> flag determines if the computation is performed in
	 * parallel or sequentially.
	 * 
	 * @param goldResult
	 * @param currentState
	 * @param nextStates
	 */
	protected void scoreWithObjective(List<StateT> allStates, ResultT goldResult) {
		long scID = TaggedTimer.start("OBJ-SCORE");
		log.debug("Score %s states according to objective...", allStates.size() + 1);
		Stream<StateT> stream = null;
		if (multiThreaded) {
			stream = allStates.parallelStream();
		} else {
			stream = allStates.stream();
		}
		stream.forEach(s -> objective.score(s, goldResult));
		TaggedTimer.stop(scID);
	}

	protected StateT selectNextState(StateT currentState, List<StateT> states, boolean useModelDistribution,
			SamplingStrategy strategy) {
		if (states.isEmpty()) {
			return currentState;
		}
		StateT selectedNextState = null;
		switch (strategy) {
		case GREEDY:
			if (useModelDistribution) {
				Collections.sort(states, AbstractState.modelScoreComparator);
			} else {
				Collections.sort(states, AbstractState.objectiveScoreComparator);
			}
			selectedNextState = states.get(0);
			break;
		case LINEAR_SAMPLING:
			selectedNextState = SamplingUtils.drawFromDistribution(states, useModelDistribution, false);
			break;
		case SOFTMAX_SAMPLING:
			selectedNextState = SamplingUtils.drawFromDistribution(states, useModelDistribution, true);
			break;
		}
		/*
		 * Decide if selected state should be accepted as next state.
		 */
		return SamplingUtils.accept(selectedNextState, currentState, useModelDistribution) ? selectedNextState
				: currentState;
	}

	protected Model<StateT> getModel() {
		return model;
	}

	protected Scorer<StateT> getScorer() {
		return scorer;
	}

	public StoppingCriterion<StateT> getStoppingCriterion() {
		return stoppingCriterion;
	}

	/**
	 * Set the stopping criterion for the sampling chain. This Function can be
	 * used to change the stopping criterion for the test phase.
	 * 
	 * @param stoppingCriterion
	 */
	public void setStoppingCriterion(StoppingCriterion<StateT> stoppingCriterion) {
		this.stoppingCriterion = stoppingCriterion;
	}

	public void setStepLimit(int samplingLimit) {
		this.stoppingCriterion = new StepLimitCriterion<>(samplingLimit);
	}

	public SamplingStrategy getSamplingStrategy() {
		return samplingStrategy;
	}

	/**
	 * Sets the sampling strategy for the training phase. The candidate state
	 * that is used for training is selected from all possible successor states
	 * using this strategy.
	 * 
	 * @param samplingStrategy
	 */
	public void setSamplingStrategy(SamplingStrategy samplingStrategy) {
		this.samplingStrategy = samplingStrategy;
	}

}
