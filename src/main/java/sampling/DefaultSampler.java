package sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import sampling.samplingstrategies.AcceptStrategies;
import sampling.samplingstrategies.AcceptStrategy;
import sampling.samplingstrategies.SamplingStrategies;
import sampling.samplingstrategies.SamplingStrategy;
import sampling.stoppingcriterion.StepLimitCriterion;
import sampling.stoppingcriterion.StoppingCriterion;
import utility.Utils;
import variables.AbstractState;

public class DefaultSampler<InstanceT, StateT extends AbstractState<InstanceT>, ResultT>
		implements Sampler<StateT, ResultT> {

	public interface StepCallback {

		default <InstanceT, StateT extends AbstractState<InstanceT>> void onStartStep(Sampler<StateT, ?> sampler,
				int step, int e, int numberOfExplorers, StateT initialState) {
		}

		default <InstanceT, StateT extends AbstractState<InstanceT>> void onEndStep(Sampler<StateT, ?> sampler,
				int step, int e, int numberOfExplorers, StateT initialState, StateT currentState) {

		}
	}

	public interface SamplingCallback {

		default <InstanceT, StateT extends AbstractState<InstanceT>> void onScoredProposalStates(
				List<StateT> bestStates) {
		}

	}

	private static Logger log = LogManager.getFormatterLogger(DefaultSampler.class);

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
	private List<SamplingCallback> samplingCallbacks = new ArrayList<>();

	public List<StepCallback> getStepCallbacks() {
		return stepCallbacks;
	}

	public void addStepCallbacks(List<StepCallback> stepCallbacks) {
		this.stepCallbacks.addAll(stepCallbacks);
	}

	public void addStepCallback(StepCallback stepCallback) {
		this.stepCallbacks.add(stepCallback);
	}

	public void addSamplingCallback(SamplingCallback samplingCallback) {
		this.samplingCallbacks.add(samplingCallback);
	}

	/**
	 * The DefaultSampler implements the Sampler interface. This sampler divides the
	 * sampling procedure in the exploration of the search space (using Explorers)
	 * and the actual sampling that happens in this class. It is designed to be
	 * flexible in the actual sampling strategy and the stopping criterion.
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
	 * The DefaultSampler implements the Sampler interface. This sampler divides the
	 * sampling procedure in the exploration of the search space (using Explorers)
	 * and the actual sampling that happens in this class. It is designed to be
	 * flexible in the actual sampling strategy and the stopping criterion. This
	 * constructor uses a simple step limit as the stopping criterion.
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
			int e = 1;
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
			int e = 1;
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
	 * Generates states, computes features, scores states, and updates the model.
	 * After that a successor state is selected.
	 * 
	 * @param learner
	 * @param explorer
	 * @param goldResult
	 * @param currentState
	 * @return
	 */

	protected StateT performTrainingStep(Learner<StateT> learner, Explorer<StateT> explorer, ResultT goldResult,
			StateT currentState) {
		if (log.getLevel().equals(Level.DEBUG)) {
			log.debug("TRAINING Step:");
			log.debug("Current State:\n%s", currentState);
		}
		/**
		 * Generate possible successor states.
		 */
		List<StateT> nextStates = explorer.getNextStates(currentState);

//		List<StateT> allStates = new ArrayList<>(nextStates);

		if (nextStates.size() > 0) {
//			allStates.add(currentState);
			/**
			 * Score all states with Objective/Model only if sampling strategy needs that.
			 * If not, score only selected candidate and current.
			 */
			if (trainSamplingStrategy.usesObjective()) {
				/**
				 * Compute objective function scores
				 */
//				scoreWithObjective(allStates, goldResult);

				{
					scoreWithObjective(nextStates, goldResult);
//					objective.score(currentState, goldResult);
				}
			}
			if (trainSamplingStrategy.usesModel()) {
				/**
				 * Apply templates to states and, thus generate factors and features
				 */
				/**
				 * TODO: Test
				 * 
				 */
				{
//					model.score(Arrays.asList(currentState), currentState.getInstance());
					model.score(nextStates);
				}
//				model.score(allStates, currentState.getInstance());
			}

			return sampleRank(learner, goldResult, currentState, nextStates);
			// return modifiedSampleRank(learner, goldResult, currentState,
			// nextStates, allStates);
		}

		return currentState;
	}

	private StateT modifiedSampleRank(Learner<StateT> learner, ResultT goldResult, StateT currentState,
			List<StateT> nextStates, List<StateT> allStates) {
		/**
		 * Sample one possible successor
		 */
		StateT candidateState = trainSamplingStrategy.sampleCandidate(nextStates);
		/**
		 * NEW UNTESTED!
		 */

		if (trainSamplingStrategy.usesModel()) {

			/**
			 * If states were not scored before score only selected candidate and current
			 * state.
			 */
			/**
			 * Compute objective function scores
			 */
			scoreWithObjective(Arrays.asList(currentState, candidateState), goldResult);
			/**
			 * Update model with selected state
			 */
			learner.update(currentState, candidateState);
			/**
			 * Choose to accept or reject selected state
			 */
			boolean isAccepted = trainAcceptStrategy.isAccepted(candidateState, currentState);
			for (SamplingCallback callBack : samplingCallbacks) {
				callBack.onScoredProposalStates(allStates);
			}
			return isAccepted ? candidateState : currentState;
		} else {
			/**
			 * Choose to accept or reject selected state
			 */
			boolean isAccepted = trainAcceptStrategy.isAccepted(candidateState, currentState);
			// System.out.println("isAccepted = " + isAccepted);
			if (isAccepted) {

				/**
				 * Apply templates to current and candidate state only
				 */
				// System.out.println("Apply Model score: ");
				// System.out.println(currentState);
				// System.out.println(candidateState);
				// System.out.println("######");
				model.score(Arrays.asList(currentState, candidateState));

				/**
				 * Update model with selected state
				 */
				learner.update(currentState, candidateState);
			}
			for (SamplingCallback callBack : samplingCallbacks) {
				callBack.onScoredProposalStates(allStates);
			}
			return isAccepted ? candidateState : currentState;
		}
	}

	private StateT sampleRank(Learner<StateT> learner, ResultT goldResult, StateT currentState,
			List<StateT> nextStates) {

		/**
		 * Sample one possible successor
		 */
		StateT candidateState = trainSamplingStrategy.sampleCandidate(nextStates);

		/**
		 * If states were not scored before score only selected candidate and current
		 * state.
		 */
		if (!trainSamplingStrategy.usesObjective()) {
			/**
			 * Compute objective function scores
			 */
//			scoreWithObjective(Arrays.asList(currentState, candidateState), goldResult);
//			scoreWithObjective(Arrays.asList(candidateState), goldResult);
			objective.score(candidateState, goldResult);
		}
		if (!trainSamplingStrategy.usesModel()) {
			/**
			 * Apply templates to current and candidate state only
			 */
//			model.score(Arrays.asList(currentState, candidateState), currentState.getInstance());
			model.score(Arrays.asList(candidateState));
		}
		/**
		 * Update model with selected state
		 */
		learner.update(currentState, candidateState);
		/**
		 * Choose to accept or reject selected state
		 */
		boolean isAccepted = trainAcceptStrategy.isAccepted(candidateState, currentState);

		return isAccepted ? candidateState : currentState;
	}

	/**
	 * Generates states, computes features and scores states. After that a successor
	 * state is selected.
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
//			List<StateT> allStates = new ArrayList<>(nextStates);
//			allStates.add(currentState);

			/**
			 * Apply templates to states and thus generate factors and features
			 */
			model.score(nextStates);

			/**
			 * Select a candidate state from the list of possible successors.
			 */
			StateT candidateState = predictionSamplingStrategy.sampleCandidate(nextStates);
			/**
			 * Decide to accept or reject the selected state
			 */

			boolean isAccepted = predictionAcceptStrategy.isAccepted(candidateState, currentState);
			return isAccepted ? candidateState : currentState;
		} else {
			return currentState;
		}
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
		log.debug("Score %s states according to objective...", allStates.size() + 1);
		Stream<StateT> stream = Utils.getStream(allStates, multiThreaded);
		// long t = System.currentTimeMillis();
		stream.forEach(s -> objective.score(s, goldResult));
		// System.out.println("Score: states: ");
		// allStates.forEach(System.out::println);
		// System.out.println("#######");
		// System.out.println("Time: " + (System.currentTimeMillis() - t));

	}

	protected Model<?, StateT> getModel() {
		return model;
	}

	public StoppingCriterion<StateT> getStoppingCriterion() {
		return stoppingCriterion;
	}

	/**
	 * Set the stopping criterion for the sampling chain. This function can be used
	 * to change the stopping criterion for the test phase.
	 * 
	 * @param stoppingCriterion
	 */
	public void setStoppingCriterion(StoppingCriterion<StateT> stoppingCriterion) {
		this.stoppingCriterion = stoppingCriterion;
	}

	public void setStepLimit(int samplingLimit) {
		this.stoppingCriterion = new StepLimitCriterion<>(samplingLimit);
	}

	/**
	 * Sets the sampling strategy for the training phase. The candidate state that
	 * is used for training is selected from all possible successor states using
	 * this strategy.
	 * 
	 * @param samplingStrategy
	 */
	public void setTrainSamplingStrategy(SamplingStrategy<StateT> samplingStrategy) {
		this.trainSamplingStrategy = samplingStrategy;
	}

	public SamplingStrategy<StateT> getPredictionSamplingStrategy() {
		return predictionSamplingStrategy;
	}

	public void setPredictionSamplingStrategy(SamplingStrategy<StateT> predictionSamplingStrategy) {
		this.predictionSamplingStrategy = predictionSamplingStrategy;
	}

	public AcceptStrategy<StateT> getPredictionAcceptStrategy() {
		return predictionAcceptStrategy;
	}

	public void setPredictionAcceptStrategy(AcceptStrategy<StateT> predictionAcceptStrategy) {
		this.predictionAcceptStrategy = predictionAcceptStrategy;
	}

	public SamplingStrategy<StateT> getTrainSamplingStrategy() {
		return trainSamplingStrategy;
	}

	/**
	 * Sets the strategy for accepting a sampled candidate state as the next state
	 * in the training phase.
	 * 
	 * @return
	 */
	public void setTrainAcceptStrategy(AcceptStrategy<StateT> acceptStrategy) {
		this.trainAcceptStrategy = acceptStrategy;
	}

	/**
	 * Sets the sampling strategy for the training phase. The candidate state that
	 * is used for training is selected from all possible successor states using
	 * this strategy.
	 * 
	 * @param samplingStrategy
	 */
	public void setTestSamplingStrategy(SamplingStrategy<StateT> samplingStrategy) {
		this.predictionSamplingStrategy = samplingStrategy;
	}

	/**
	 * Sets the strategy for accepting a sampled candidate state as the next state
	 * in the training phase.
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

	@Override
	public List<StateT> collectBestNStates(StateT initialState, int N) {
		List<StateT> generatedChain = new ArrayList<>();
		List<StateT> collectedStates = new ArrayList<>();
		StateT currentState = initialState;

		int step = 0;
		do {
			log.info("---------------------------");
			for (Explorer<StateT> explorer : explorers) {
				currentState = performPredictionStep(explorer, currentState);

				/**
				 * Generate possible successor states.
				 */
				List<StateT> nextStates = explorer.getNextStates(currentState);
				if (nextStates.size() > 0) {

					/**
					 * Apply templates to states and thus generate factors and features
					 */
					model.score(nextStates);

					/**
					 * Select a candidate state from the list of possible successors.
					 */
					StateT candidateState = predictionSamplingStrategy.sampleCandidate(nextStates);

					collectedStates = selectBestNStates(collectedStates, nextStates, N);

					/**
					 * Decide to accept or reject the selected state
					 */

					boolean isAccepted = predictionAcceptStrategy.isAccepted(candidateState, currentState);
					currentState = isAccepted ? candidateState : currentState;
				}

				generatedChain.add(currentState);
				log.info("Sampled State:  %s", currentState);
			}
			step++;
		} while (!stoppingCriterion.checkCondition(generatedChain, step));
		log.info("Stop collecting after step %s", step);
		return collectedStates;
	}

	private List<StateT> selectBestNStates(List<StateT> collectedStates, List<StateT> nextStates, int N) {

		/**
		 * TODO: Quite naive way of getting best states. Maybe there is a faster way.
		 */
		final boolean isEmpty = collectedStates.isEmpty();

		int i = 1;
		double smallestScore = isEmpty ? 0 : collectedStates.get(collectedStates.size() - i).getModelScore();
		Set<StateT> merged = new HashSet<>(collectedStates);

		for (StateT stateT : nextStates) {

			/*
			 * if smaller N add anyway otherwise just if score is bigger than smallest
			 * score.
			 */
			if (merged.size() < N || stateT.getModelScore() >= smallestScore) {
				merged.add(stateT);
				i++;

				final double nextSmallest;

				if (isEmpty) {
					nextSmallest = 0;
				} else {
					nextSmallest = collectedStates.get(Math.max(0, collectedStates.size() - i)).getModelScore();
				}

				smallestScore = Math.min(nextSmallest, stateT.getModelScore());
			}
		}

		collectedStates = new ArrayList<>(merged);

		Collections.sort(collectedStates, AbstractState.modelScoreComparator);

		return collectedStates.subList(0, Math.min(N, collectedStates.size()));

	}

}
