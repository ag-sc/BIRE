package Learning;

import java.util.ArrayList;
import java.util.List;

import Corpus.AnnotatedDocument;
import Sampling.DefaultListSampler;
import Sampling.Sampler;
import Variables.State;

public class DefaultLearner implements Learner {

	private int statesPerStep = 20;
	private int steps = 1;

	private Model model;
	private Scorer scorer;
	private ObjectiveFunction objective;

	public DefaultLearner(int steps, int statesPerStep) {
		this.steps = steps;
		this.statesPerStep = statesPerStep;
	}

	public void train(List<AnnotatedDocument> documents) {
		objective = new ObjectiveFunction();
		model = new Model();
		scorer = new Scorer(model);

		List<Sampler> samplerList = new ArrayList<Sampler>();
		samplerList.add(new DefaultListSampler(statesPerStep));

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
					State nextState = nextStates.get(0);
					System.out.println("Next state: " + nextState);
					currentState = nextState;
				}
			}
		}

	}

	private void updateModelForState(State goldState, State currentState,
			State state) {
		// TODO learning rate alpha needs to be set correctly
		double alpha = 0;
		if (objective.score(state, goldState) > objective.score(currentState,
				goldState)) {
			if (state.getScore() < currentState.getScore()) {
				model.update(state, alpha);
				model.update(currentState, -alpha);
			}
		} else {
			if (objective.score(state, goldState) < objective.score(
					currentState, goldState)) {
				if (state.getScore() > currentState.getScore()) {
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

	private State generateInitialAnnotations(AnnotatedDocument document,
			State goldState) {
		State state = new State(document);
		state.goldState = goldState;
		return state;
	}

}
