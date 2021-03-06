package sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.Instance;
import evaluation.TaggedTimer;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.callbacks.StepCallback;
import sampling.samplingstrategies.AcceptStrategies;
import sampling.samplingstrategies.AcceptStrategy;
import sampling.samplingstrategies.SamplingStrategies;
import sampling.samplingstrategies.SamplingStrategy;
import sampling.stoppingcriterion.StepLimitCriterion;
import sampling.stoppingcriterion.StoppingCriterion;
import utility.Utils;
import variables.AbstractState;

public class DefaultSampler<InstanceT extends Instance, StateT extends AbstractState<InstanceT>, ResultT>
		implements Sampler<StateT, ResultT> {

	private static Logger log = LogManager.getFormatterLogger();
	protected Model<InstanceT, StateT> model;
	protected ObjectiveFunction<StateT, ResultT> objective;
	private List<Explorer<StateT>> explorers;
	private StoppingCriterion<StateT> stoppingCriterion;

	protected final boolean multiThreaded = false;
	/**
	 * Defines the sampling strategy for the training phase. The test phase
	 * currently always uses the greedy variant.
	 */
	private SamplingStrategy<StateT> trainSamplingStrategy = SamplingStrategies.linearModelSamplingStrategy();

	private AcceptStrategy<StateT> trainAcceptStrategy = AcceptStrategies.strictModelAccept();

	/**
	 * Greedy sampling strategy for test phase.
	 */
	private SamplingStrategy<StateT> predictionSamplingStrategy = SamplingStrategies.greedyModelStrategy();

	/**
	 * Strict accept strategy for test phase.
	 */
	private AcceptStrategy<StateT> predictionAcceptStrategy = AcceptStrategies.strictModelAccept();

	private List<StepCallback> stepCallbacks = new ArrayList<>();

	public List<StepCallback> getStepCallbacks() {
		return stepCallbacks;
	}

	public void addStepCallbacks(List<StepCallback> stepCallbacks) {
		this.stepCallbacks.addAll(stepCallbacks);
	}

	public void addStepCallback(StepCallback stepCallback) {
		this.stepCallbacks.add(stepCallback);
	}

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
	public DefaultSampler(Model<InstanceT, StateT> model, ObjectiveFunction<StateT, ResultT> objective,
			List<Explorer<StateT>> explorers, StoppingCriterion<StateT> stoppingCriterion) {
		super();
		this.model = model;
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
	public DefaultSampler(Model<InstanceT, StateT> model, ObjectiveFunction<StateT, ResultT> objective,
			List<Explorer<StateT>> explorers, int samplingSteps) {
		super();
		this.model = model;
		this.objective = objective;
		this.explorers = explorers;
		this.stoppingCriterion = new StepLimitCriterion<>(samplingSteps);
	}

	@Override
	public List<StateT> generateChain(StateT initialState, ResultT goldResult, Learner<StateT> learner) {
		List<StateT> generatedChain = new ArrayList<>();

		StateT currentState = initialState;
		int step = 0;

		do {
			log.info("---------------------------");
			int e = 0;
			for (Explorer<StateT> explorer : explorers) {
				log.info("...............");
				log.info("TRAINING Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
				for (StepCallback c : stepCallbacks) {
					c.onStartStep(this, step, e, explorers.size(), initialState);
				}

				currentState = performTrainingStep(learner, explorer, goldResult, currentState);
				generatedChain.add(currentState);
				log.info("Sampled State:  %s", currentState);
				for (StepCallback c : stepCallbacks) {
					c.onEndStep(this, step, e, explorers.size(), initialState, currentState);
				}
				e++;
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
			int e = 0;
			for (Explorer<StateT> explorer : explorers) {
				log.info("...............");
				log.info("PREDICTION Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
				for (StepCallback c : stepCallbacks) {
					c.onStartStep(this, step, e, explorers.size(), initialState);
				}
				currentState = performPredictionStep(explorer, currentState);
				generatedChain.add(currentState);
				log.info("Sampled State:  %s", currentState);
				for (StepCallback c : stepCallbacks) {
					c.onEndStep(this, step, e, explorers.size(), initialState, currentState);
				}
				e++;
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
		log.debug("TRAINING Step:");
		log.debug("Current State:\n%s", currentState);
		/**
		 * Generate possible successor states.
		 */
		List<StateT> nextStates = explorer.getNextStates(currentState);
		List<StateT> allStates = new ArrayList<>(nextStates);
		if (nextStates.size() > 0) {
			allStates.add(currentState);
			/**
			 * Score all states with Objective/Model only if sampling strategy
			 * needs that. If not, score only selected candidate and current.
			 */
			if (trainSamplingStrategy.usesObjective()) {
				/**
				 * Compute objective function scores
				 */
				scoreWithObjective(allStates, goldResult);
			}
			if (trainSamplingStrategy.usesModel()) {
				/**
				 * Apply templates to states and, thus generate factors and
				 * features
				 */
				model.score(allStates, currentState.getInstance(), currentState.getFactorGraph().getFactorPool());
				// model.applyToStates(allStates,
				// currentState.getFactorGraph().getFactorPool(),
				// currentState.getInstance());
				// /**
				// * Score all states according to the model.
				// */
				// scorer.score(allStates, multiThreaded);
			}
			/**
			 * Sample one possible successor
			 */
			StateT candidateState = trainSamplingStrategy.sampleCandidate(nextStates);

			/**
			 * If states were not scored before score only selected candidate
			 * and current state.
			 */
			if (!trainSamplingStrategy.usesObjective()) {
				/**
				 * Compute objective function scores
				 */
				scoreWithObjective(Arrays.asList(currentState, candidateState), goldResult);
			}
			if (!trainSamplingStrategy.usesModel()) {
				/**
				 * Apply templates to current and candidate state only
				 */
				model.score(Arrays.asList(currentState, candidateState), currentState.getInstance(),
						currentState.getFactorGraph().getFactorPool());
				// model.applyToStates(
				// currentState.getFactorGraph().getFactorPool(),
				// currentState.getInstance());
				// /**
				// * Score current and candidate state according to model.
				// */
				// scorer.score(Arrays.asList(currentState, candidateState),
				// multiThreaded);
			}
			/**
			 * Update model with selected state
			 */
			// nextStates.sort(AbstractState.modelScoreComparator);
			// List<StateT> train = new ArrayList<>();
			// train.addAll(nextStates.subList(0, Math.min(10,
			// nextStates.size())));
			// train.addAll(nextStates.subList(Math.max(0, nextStates.size() -
			// 10),
			// nextStates.size()));
			// learner.update(currentState, nextStates);
			learner.update(currentState, candidateState);
			/**
			 * Recompute model score to reflect last update in score.
			 */
			// log.debug("(Re)Score:");
			// scoreWithModel(Arrays.asList(currentState, candidateState));

			// EvaluationUtil.printWeights(model, 0);
			// nextStates.sort(AbstractState.modelScoreComparator);
			// double sum = 0;
			// for (StateT state : nextStates) {
			// sum += state.getModelScore();
			// }
			// if (sum == 0)
			// sum = 1;
			// System.out.println(String.format("CURRENT: %s", currentState));
			// System.out.println(String.format("CANDIDATE: %s: %s",
			// candidateState.getModelScore() / sum, candidateState));
			// for (StateT state : nextStates) {
			// System.out.println(String.format("%s: %s", state.getModelScore()
			// /
			// sum, state));
			// }
			// StateT candidateState =
			// greedyObjectiveStrategy.sampleCandidate(nextStates);
			// System.out
			// .println(String.format("NEW CANDIDATE: %s: %s",
			// candidateState.getModelScore() / sum, candidateState));
			/**
			 * Choose to accept or reject selected state
			 */
			// return candidateState;
			return trainAcceptStrategy.isAccepted(candidateState, currentState) ? candidateState : currentState;
		}
		return currentState;
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
		if (nextStates.size() > 0) {
			List<StateT> allStates = new ArrayList<>(nextStates);
			allStates.add(currentState);
			/**
			 * Apply templates to states and thus generate factors and features
			 */

			model.score(allStates, currentState.getInstance(), currentState.getFactorGraph().getFactorPool());
			// model.applyToStates(allStates,
			// currentState.getFactorGraph().getFactorPool(),
			// currentState.getInstance());
			// /**
			// * Score all states according to the model.
			// */
			// scorer.score(allStates, multiThreaded);
			/**
			 * Select a candidate state from the list of possible successors.
			 */
			StateT candidateState = predictionSamplingStrategy.sampleCandidate(nextStates);
			/**
			 * Decide to accept or reject the selected state
			 */

			// TokenState s = (TokenState) currentState;
			// if (s.sentence.text.startsWith("He")) {
			// EvaluationUtil.printWeights(model, 0);
			// nextStates.sort(AbstractState.modelScoreComparator);
			// double sum = 0;
			// for (StateT state : nextStates) {
			// sum += state.getModelScore();
			// }
			// log.debug("CURRENT: %s", currentState);
			// log.debug("CANDIDATE: %s: %s", candidateState.getModelScore() /
			// sum, candidateState);
			// for (StateT state : nextStates) {
			// log.debug("%s: %s", state.getModelScore() / sum, state);
			// }
			// log.debug("");
			// }
			currentState = predictionAcceptStrategy.isAccepted(candidateState, currentState) ? candidateState
					: currentState;
			return currentState;
		} else {
			return currentState;
		}
	}
	//
	// /**
	// * Applies the model's template to each of the given states, thus
	// unrolling
	// * (computing) the features. The <i>multiThreaded</i> flag determines if
	// the
	// * computation is performed in parallel or sequentially.
	// *
	// * @param currentState
	// * @param nextStates
	// */
	// protected void unroll(List<StateT> allStates) {
	// long unrollID = TaggedTimer.start("SC-UNROLL");
	// log.debug("Unroll features for %s states...", allStates.size() + 1);
	//
	// Stream<StateT> stream = Utils.getStream(allStates, multiThreaded);
	// stream.forEach(s -> model.applyTo(s));
	// TaggedTimer.stop(unrollID);
	// }

	// /**
	// * Computes the model scores for each of the given states. The
	// * <i>multiThreaded</i> flag determines if the computation is performed in
	// * parallel or sequentially.
	// *
	// * @param nextStates
	// */
	// protected void scoreWithModel(List<StateT> nextStates) {
	// long scID = TaggedTimer.start("MODEL-SCORE");
	// log.debug("Score %s states according to model...", nextStates.size());
	//
	// Stream<StateT> stream = Utils.getStream(nextStates, multiThreaded);
	// stream.forEach(s -> scorer.score(s));
	// TaggedTimer.stop(scID);
	// }

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
		Stream<StateT> stream = Utils.getStream(allStates, multiThreaded);
		stream.forEach(s -> objective.score(s, goldResult));
		TaggedTimer.stop(scID);
	}

	protected Model<?, StateT> getModel() {
		return model;
	}

	public StoppingCriterion<StateT> getStoppingCriterion() {
		return stoppingCriterion;
	}

	/**
	 * Set the stopping criterion for the sampling chain. This function can be
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

	public SamplingStrategy<StateT> getTrainingSamplingStrategy() {
		return trainSamplingStrategy;
	}

	/**
	 * Sets the sampling strategy for the training phase. The candidate state
	 * that is used for training is selected from all possible successor states
	 * using this strategy.
	 * 
	 * @param samplingStrategy
	 */
	public void setTrainingSamplingStrategy(SamplingStrategy<StateT> samplingStrategy) {
		this.trainSamplingStrategy = samplingStrategy;
	}

	public AcceptStrategy<StateT> getTrainingAcceptStrategy() {
		return trainAcceptStrategy;
	}

	/**
	 * Sets the strategy for accepting a sampled candidate state as the next
	 * state in the training phase.
	 * 
	 * @return
	 */
	public void setTrainingAcceptStrategy(AcceptStrategy<StateT> acceptStrategy) {
		this.trainAcceptStrategy = acceptStrategy;
	}

	public SamplingStrategy<StateT> getTestSamplingStrategy() {
		return predictionSamplingStrategy;
	}

	/**
	 * Sets the sampling strategy for the training phase. The candidate state
	 * that is used for training is selected from all possible successor states
	 * using this strategy.
	 * 
	 * @param samplingStrategy
	 */
	public void setTestSamplingStrategy(SamplingStrategy<StateT> samplingStrategy) {
		this.predictionSamplingStrategy = samplingStrategy;
	}

	public AcceptStrategy<StateT> getTestAcceptStrategy() {
		return predictionAcceptStrategy;
	}

	/**
	 * Sets the strategy for accepting a sampled candidate state as the next
	 * state in the training phase.
	 * 
	 * @return
	 */
	public void setTestAcceptStrategy(AcceptStrategy<StateT> acceptStrategy) {
		this.predictionAcceptStrategy = acceptStrategy;
	}

	public List<Explorer<StateT>> getExplorers() {
		return explorers;
	}

	public void setExplorers(List<Explorer<StateT>> explorers) {
		this.explorers = explorers;
	}

}
