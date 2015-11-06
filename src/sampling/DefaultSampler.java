package sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.LabeledDocument;
import corpus.Document;
import evaluation.TaggedTimer;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.Scorer;
import variables.AbstractState;

public class DefaultSampler<PriorT, StateT extends AbstractState, ResultT>
		extends AbstractSampler<PriorT, StateT, ResultT> {

	enum SamplingStrategy {
		GREEDY, LINEAR_SAMPLING, SOFTMAX_SAMPLING;
	}

	private static Logger log = LogManager.getFormatterLogger(DefaultSampler.class.getName());

	protected Model<StateT> model;
	protected Scorer<StateT> scorer;
	protected ObjectiveFunction<StateT, ResultT> objective;
	private Initializer<PriorT, StateT> initializer;
	private List<Explorer<StateT>> explorers;

	protected final boolean multiThreaded = true;
	private SamplingStrategy samplintStrategy = SamplingStrategy.GREEDY;

	public DefaultSampler(Model<StateT> model, Scorer<StateT> scorer, ObjectiveFunction<StateT, ResultT> objective,
			Initializer<PriorT, StateT> initializer, List<Explorer<StateT>> explorers) {
		super();
		this.model = model;
		this.scorer = scorer;
		this.objective = objective;
		this.initializer = initializer;
		this.explorers = explorers;
	}

	@Override
	public List<StateT> generateChain(LabeledDocument<PriorT, ResultT> document, int steps, Learner<StateT> learner) {
		List<StateT> generatedChain = new ArrayList<>();
		ResultT goldResult = document.getGoldResult();
		StateT currentState = initializer.getInitialState(document);

		for (int s = 0; s < steps; s++) {
			log.info("---------------------------");
			for (Explorer<StateT> explorer : explorers) {
				log.info("...............");
				log.info("Step: %s/%s; Explorer: %s", s + 1, steps, explorer.getClass().getSimpleName());
				generatedChain.add(currentState = performTrainingStep(learner, explorer, goldResult, currentState));
				log.info("Sampled State:  %s", currentState);
			}
		}
		return generatedChain;
	}

	@Override
	public List<StateT> generateChain(Document<PriorT> document, int steps) {
		List<StateT> generatedChain = new ArrayList<>();
		StateT currentState = initializer.getInitialState(document);

		for (int s = 0; s < steps; s++) {
			log.info("---------------------------");
			for (Explorer<StateT> explorer : explorers) {
				log.info("...............");
				log.info("Step: %s/%s; Explorer: %s", s + 1, steps, explorer.getClass().getSimpleName());
				generatedChain.add(currentState = performPredictionStep(explorer, currentState));
				log.info("Sampled State:  %s", currentState);
			}
		}
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

		currentState = selectNextState(nextStates, false, samplintStrategy);

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

		currentState = selectNextState(nextStates, true, SamplingStrategy.GREEDY);

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

	protected StateT selectNextState(List<StateT> states, boolean model, SamplingStrategy strategy) {
		switch (strategy) {
		case GREEDY:
			if (model) {
				Collections.sort(states, AbstractState.modelScoreComparator);
			} else {
				Collections.sort(states, AbstractState.objectiveScoreComparator);
			}
			return states.get(0);
		case LINEAR_SAMPLING:
			return drawRandomlyFrom(states, model, false);
		case SOFTMAX_SAMPLING:
			return drawRandomlyFrom(states, model, true);
		}
		return null;
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
}
