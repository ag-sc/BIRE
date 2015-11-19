package sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import evaluation.TaggedTimer;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.Scorer;
import sampling.stoppingcriterion.StepLimitCriterion;
import sampling.stoppingcriterion.StoppingCriterion;
import variables.AbstractState;

public class DefaultSampler<StateT extends AbstractState, ResultT> implements AbstractSampler<StateT, ResultT> {

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
	private SamplingStrategy samplingStrategy = SamplingStrategy.GREEDY;

	public DefaultSampler(Model<StateT> model, Scorer<StateT> scorer, ObjectiveFunction<StateT, ResultT> objective,
			List<Explorer<StateT>> explorers, StoppingCriterion<StateT> stoppingCriterion) {
		super();
		this.model = model;
		this.scorer = scorer;
		this.objective = objective;
		this.explorers = explorers;
		this.stoppingCriterion = stoppingCriterion;
	}

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
		while (!stoppingCriterion.checkCondition(generatedChain, step)) {
			log.info("---------------------------");
			for (Explorer<StateT> explorer : explorers) {
				log.info("...............");
				log.info("TRAINING Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
				currentState = performTrainingStep(learner, explorer, goldResult, currentState);
				generatedChain.add(currentState);
				log.info("Sampled State:  %s", currentState);
			}
			step++;
		}
		log.info("Stop sampling after step %s", step);
		return generatedChain;
	}

	@Override
	public List<StateT> generateChain(StateT initialState) {
		List<StateT> generatedChain = new ArrayList<>();
		StateT currentState = initialState;

		int step = 0;
		while (!stoppingCriterion.checkCondition(generatedChain, step)) {
			log.info("---------------------------");
			for (Explorer<StateT> explorer : explorers) {
				log.info("...............");
				log.info("PREDICTION Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
				currentState = performPredictionStep(explorer, currentState);
				generatedChain.add(currentState);
				log.info("Sampled State:  %s", currentState);
			}
			step++;
		}
		log.info("Stop sampling after step %s", step);
		return generatedChain;
	}

	protected StateT performTrainingStep(Learner<StateT> learner, Explorer<StateT> explorer, ResultT goldResult,
			StateT currentState) {
		log.debug("TRAINING:");
		List<StateT> nextStates = explorer.getNextStates(currentState);

		unroll(currentState, nextStates);

		scoreWithObjective(currentState, nextStates, goldResult);
		learner.update(currentState, nextStates);
		log.debug("(Re)Score:");
		List<StateT> allStates = new ArrayList<>(nextStates);
		allStates.add(currentState);
		scoreWithModel(allStates);

		currentState = selectNextState(currentState, nextStates, false, samplingStrategy);

		return currentState;
	}

	protected StateT performPredictionStep(Explorer<StateT> explorer, StateT currentState) {
		log.debug("PREDICTION:");
		List<StateT> nextStates = explorer.getNextStates(currentState);
		unroll(currentState, nextStates);
		log.debug("Score:");
		List<StateT> allStates = new ArrayList<>(nextStates);
		allStates.add(currentState);
		scoreWithModel(allStates);
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
	protected void unroll(StateT currentState, List<StateT> nextStates) {
		long unrollID = TaggedTimer.start("SC-UNROLL");
		log.debug("Unroll features for %s states...", nextStates.size() + 1);
		List<StateT> allStates = new ArrayList<>(nextStates);
		allStates.add(currentState);

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
	protected void scoreWithObjective(StateT currentState, List<StateT> nextStates, ResultT goldResult) {
		long scID = TaggedTimer.start("OBJ-SCORE");
		log.debug("Score %s states according to objective...", nextStates.size() + 1);
		List<StateT> allStates = new ArrayList<>(nextStates);
		allStates.add(currentState);

		Stream<StateT> stream = null;
		if (multiThreaded) {
			stream = allStates.parallelStream();
		} else {
			stream = allStates.stream();
		}
		stream.forEach(s -> objective.score(s, goldResult));
		TaggedTimer.stop(scID);
	}

	protected StateT selectNextState(StateT currentState, List<StateT> states, boolean useModel,
			SamplingStrategy strategy) {
		StateT selectedNextState = null;
		switch (strategy) {
		case GREEDY:
			if (useModel) {
				Collections.sort(states, AbstractState.modelScoreComparator);
			} else {
				Collections.sort(states, AbstractState.objectiveScoreComparator);
			}
			selectedNextState = states.get(0);
			break;
		case LINEAR_SAMPLING:
			selectedNextState = drawRandomlyFrom(states, useModel, false);
			break;
		case SOFTMAX_SAMPLING:
			selectedNextState = drawRandomlyFrom(states, useModel, true);
			break;
		}
		/*
		 * Decide if selected state should be accepted as next state.
		 */
		if (accept(selectedNextState, currentState, useModel)) {
			return selectedNextState;
		} else {
			return currentState;
		}
	}

	// TODO implement a "temperature" approach (simulated annealing)
	private boolean accept(StateT selectedNextState, StateT currentState, boolean useModel) {
		double Ec = 0;
		double En = 0;
		if (useModel) {
			Ec = currentState.getObjectiveScore();
			En = selectedNextState.getObjectiveScore();
		} else {
			Ec = currentState.getModelScore();
			En = selectedNextState.getModelScore();
		}
		// double k = 1;
		// double T = 1;
		// // Simulated Annealing
		// double p = Math.exp(-(En - Ec) / (k * T));
		// // always accept when p>0 otherwise accept with probability p
		// return Math.random() < p;

		return En >= Ec;
		// return true;
	}

	protected Model<StateT> getModel() {
		return model;
	}

	protected Scorer<StateT> getScorer() {
		return scorer;
	}

	/**
	 * Selects a state from the given list according to the probability
	 * distribution defined by the states' (model/objective) scores. Each score
	 * is divided by the total sum of all scores, in order to create a
	 * probability distribution across states. If "softmax" is true, the
	 * probability distribution is computed using the softmax formula.
	 * 
	 * @param nextStates
	 * @param model
	 * @param softmax
	 * @return
	 */
	public static <StateT extends AbstractState> StateT drawRandomlyFrom(List<StateT> nextStates, boolean model,
			boolean softmax) {
		// compute total sum of scores
		Function<StateT, Double> toScore = null;
		if (model) {
			toScore = s -> s.getModelScore();
		} else {
			toScore = d -> d.getObjectiveScore();
		}
		Function<Double, Double> toProbability = null;
		if (softmax) {
			toProbability = d -> Math.exp(d);
		} else {
			toProbability = d -> d;
		}
		double totalSum = 0;
		for (StateT s : nextStates) {
			if (model) {
				totalSum += toProbability.apply(toScore.apply(s));
			} else {
				totalSum += toProbability.apply(toScore.apply(s));
			}
		}

		double index = Math.random() * totalSum;
		double sum = 0;
		int i = 0;
		while (sum < index) {
			if (model) {
				sum += toProbability.apply(toScore.apply(nextStates.get(i++)));
			} else {
				sum += toProbability.apply(toScore.apply(nextStates.get(i++)));
			}
		}
		return nextStates.get(Math.max(0, i - 1));
	}

	public StoppingCriterion<StateT> getStoppingCriterion() {
		return stoppingCriterion;
	}

	public void setStoppingCriterion(StoppingCriterion<StateT> stoppingCriterion) {
		this.stoppingCriterion = stoppingCriterion;
	}
}
