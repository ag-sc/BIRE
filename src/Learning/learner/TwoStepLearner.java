package Learning.learner;

import java.util.ArrayList;
import java.util.List;

import Corpus.AnnotatedDocument;
import Learning.Learner;
import Learning.Model;
import Learning.ObjectiveFunction;
import Learning.Scorer;
import Sampling.EntitySampler;
import Sampling.RelationSampler;
import Sampling.Sampler;
import Sampling.SamplingHelper;
import Variables.State;

public class TwoStepLearner implements Learner {

	private int statesPerStep;
	private int steps;
	// TODO learning rate alpha needs to be set correctly
	private double alpha = 0.1;

	private Model model;
	private Scorer scorer;
	private ObjectiveFunction objective;
	private List<Sampler> samplerList;

	public TwoStepLearner(int steps, int statesPerStep, double alpha) {
		this.alpha = alpha;
		this.steps = steps;
		this.statesPerStep = statesPerStep;
		samplerList = new ArrayList<Sampler>();
		samplerList.add(new EntitySampler(statesPerStep));
		samplerList.add(new RelationSampler(statesPerStep));
	}

	public void train(List<AnnotatedDocument> documents) {
		objective = new ObjectiveFunction();
		model = new Model();
		scorer = new Scorer(model);

		for (int i = 0; i < documents.size(); i++) {
			System.out.println("Document: " + i);
			AnnotatedDocument document = documents.get(i);
			State goldState = new State(document, document.getGoldEntities());

			State currentState = generateInitialAnnotations(document, goldState);

			for (int j = 0; j < steps; j++) {
				System.out.println("Step: " + j);
				for (Sampler sampler : samplerList) {
					List<State> nextStates = sampler.getNextStates(
							currentState, scorer);
					for (State state : nextStates) {
						updateModelForState(goldState, currentState, state);
					}

					// System.out.println("Rescore: ");
					// for (State state : nextStates) {
					// scorer.score(state);
					// System.out.println(state);
					// }

					// TODO Which state should be selected to use for next
					// sampler/next step? Or all/top k?
					// State nextState = nextStates.get(0);

					State nextState = SamplingHelper
							.drawRandomlyFrom(nextStates);
					System.out.println("Next state: " + nextState);
					currentState = nextState;
				}
			}
		}

	}

	private void updateModelForState(State goldState, State currentState,
			State state) {
		// TODO do we need to compute the learning step by using score and
		// learning rate? Or do we only use a fixed learning rate = alpha ?
		double delta = 0;

		if (objective.score(state, goldState) > objective.score(currentState,
				goldState)) {
			if (state.getModelScore() < currentState.getModelScore()) {
				model.update(state, alpha);
				model.update(currentState, -alpha);
			}
		} else {
			if (objective.score(state, goldState) < objective.score(
					currentState, goldState)) {
				if (state.getModelScore() > currentState.getModelScore()) {
					model.update(currentState, alpha);
					model.update(state, -alpha);
				}
			}
		}
	}

	public Model getModel() {
		return model;
	}

	public Scorer getScorer() {
		return scorer;
	}

	/**
	 * Generates an empty state for the given document. The new state's
	 * goldState is set to the specified state.
	 * 
	 * @param document
	 * @param goldState
	 * @return
	 */
	private State generateInitialAnnotations(AnnotatedDocument document,
			State goldState) {
		State state = new State(document);
		state.goldState = goldState;
		return state;
	}

}
