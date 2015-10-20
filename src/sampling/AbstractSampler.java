package sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.AnnotatedDocument;
import evaluation.TaggedTimer;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.Scorer;
import variables.AbstractState;

public abstract class AbstractSampler<StateT extends AbstractState> extends Sampler<StateT> {

	private static Logger log = LogManager.getFormatterLogger(AbstractSampler.class.getName());

	enum LearningProcedure {
		SAMPLE_RANK, PERCEPTRON;
	}

	protected Model<StateT> model;
	protected Scorer<StateT> scorer;
	protected ObjectiveFunction<StateT> objective;

	protected final boolean multiThreaded = true;

	public AbstractSampler(Model<StateT> model, Scorer<StateT> scorer, ObjectiveFunction<StateT> objective) {
		super();
		this.model = model;
		this.scorer = scorer;
		this.objective = objective;
	}

	@Override
	public List<StateT> generateChain(AnnotatedDocument<StateT> document, Learner<StateT> learner, int steps) {
		List<StateT> generatedChain = new ArrayList<>();
		StateT goldState = document.getGoldState();
		StateT currentState = generateInitialState(document);

		for (int s = 0; s < steps; s++) {
			log.info("---------------------------");
			log.info("Step: %s/%s", s + 1, steps);
			log.info("Previous State: %s", currentState);
			log.info("---------------------------");
			generatedChain.add(currentState = performStep(learner, goldState, currentState));
			log.info("Sampled State:  %s", currentState);
			log.info("---------------------------");
		}
		return generatedChain;
	}

	@Override
	public List<StateT> generateChain(AnnotatedDocument<StateT> document, int steps) {
		return generateChain(document, null, steps);
	}

	protected StateT performStep(Learner<StateT> learner, StateT goldState, StateT currentState) {
		// long genID = TaggedTimer.start("GENERATE");
		List<StateT> nextStates = getNextStates(currentState);
		// TaggedTimer.stop(genID);

		unroll(currentState, nextStates);
		scoreWithObjective(currentState, nextStates, goldState);

		if (learner != null) {
			for (StateT state : nextStates) {
				learner.update(currentState, state, goldState);
			}
		}
		log.debug("(Re)Score:");
		scoreWithModel(Arrays.asList(currentState));

		// TODO the former parameter "omega" is now preliminarily replaced
		// with a default value of 0.5
		currentState = selectNextState(nextStates, 1);

		currentState.markAsUnchanged();
		// model.trimToState(currentState);
		return currentState;
	}

	protected abstract List<StateT> getNextStates(StateT currentState);

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
	 * @param goldState
	 * @param currentState
	 * @param nextStates
	 */
	protected void scoreWithObjective(StateT currentState, List<StateT> nextStates, StateT goldState) {
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
		stream.forEach(s -> objective.score(s, goldState));
		TaggedTimer.stop(scID);
	}

	/**
	 * Selects a successor state from the given list of states. The state with
	 * the best score is chosen, where the objective score is used as the
	 * measure with probability <i>p_o=objectiveDrivenProbability</i> and the
	 * model score with <i>p_m=1-objectiveDrivenProbability</i>. The scores are
	 * merely accessed and not recomputed. This steps needs to be performed
	 * beforehand.
	 * 
	 * @param states
	 * @param objectiveDrivenProbability
	 * @return
	 */
	protected StateT selectNextState(List<StateT> states, double objectiveDrivenProbability) {
		if (Math.random() < objectiveDrivenProbability) {
			log.info("Next state: Best by OBJECTIVE");
			Collections.sort(states, StateT.objectiveScoreComparator);
			return states.get(0);
		} else {
			log.info("Next state: Best by MODEL");
			scoreWithModel(states);
			Collections.sort(states, StateT.modelScoreComparator);
			return states.get(0);
		}
	}

	protected Model<StateT> getModel() {
		return model;
	}

	protected Scorer<StateT> getScorer() {
		return scorer;
	}
}
