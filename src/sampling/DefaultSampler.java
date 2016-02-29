package sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import evaluation.TaggedTimer;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.scorer.Scorer;
import sampling.samplingstrategies.AcceptStrategies;
import sampling.samplingstrategies.AcceptStrategy;
import sampling.samplingstrategies.SamplingStrategies;
import sampling.samplingstrategies.SamplingStrategy;
import sampling.stoppingcriterion.StepLimitCriterion;
import sampling.stoppingcriterion.StoppingCriterion;
import variables.AbstractState;

public class DefaultSampler<StateT extends AbstractState, ResultT> implements Sampler<StateT, ResultT> {

	private static Logger log = LogManager.getFormatterLogger();
	protected Model<StateT> model;
	protected Scorer<StateT> scorer;
	protected ObjectiveFunction<StateT, ResultT> objective;
	private List<Explorer<StateT>> explorers;
	private StoppingCriterion<StateT> stoppingCriterion;

	protected final boolean multiThreaded = true;
	/**
	 * Defines the sampling strategy for the training phase. The test phase
	 * currently always uses the greedy variant.
	 */
	private SamplingStrategy<StateT> samplingStrategy = SamplingStrategies.linearSamplingStrategy();

	private AcceptStrategy<StateT> acceptStrategy = AcceptStrategies.strictModelAccept();

	/**
	 * Greedy sampling strategy for test phase.
	 */
	private final SamplingStrategy<StateT> greedySamplingStrategy = SamplingStrategies.greedyStrategy();

	/**
	 * Strict accept strategy for test phase.
	 */
	private final AcceptStrategy<StateT> strictAcceptStrategy = AcceptStrategies.strictModelAccept();

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
			StateT candidateState = samplingStrategy.sampleCandidate(nextStates);
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
			return acceptStrategy.isAccepted(candidateState, currentState) ? candidateState : currentState;
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
			unroll(allStates);
			/**
			 * Score all states according to the model.
			 */
			scoreWithModel(allStates);
			/**
			 * Select a candidate state from the list of possible successors.
			 */
			StateT candidateState = greedySamplingStrategy.sampleCandidate(nextStates);
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
			currentState = strictAcceptStrategy.isAccepted(candidateState, currentState) ? candidateState
					: currentState;
			return currentState;
		} else {
			return currentState;
		}
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
		stream.forEach(s -> model.applyTo(s));
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

	public SamplingStrategy<StateT> getSamplingStrategy() {
		return samplingStrategy;
	}

	/**
	 * Sets the sampling strategy for the training phase. The candidate state
	 * that is used for training is selected from all possible successor states
	 * using this strategy.
	 * 
	 * @param samplingStrategy
	 */
	public void setSamplingStrategy(SamplingStrategy<StateT> samplingStrategy) {
		this.samplingStrategy = samplingStrategy;
	}

	public AcceptStrategy<StateT> getAcceptStrategy() {
		return acceptStrategy;
	}

	/**
	 * Sets the strategy for accepting a sampled candidate state as the next
	 * state in the training phase.
	 * 
	 * @return
	 */
	public void setAcceptStrategy(AcceptStrategy<StateT> acceptStrategy) {
		this.acceptStrategy = acceptStrategy;
	}

	public List<Explorer<StateT>> getExplorers() {
		return explorers;
	}

	public void setExplorers(List<Explorer<StateT>> explorers) {
		this.explorers = explorers;
	}

}
