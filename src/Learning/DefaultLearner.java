package Learning;

import java.util.List;

import Corpus.AnnotatedDocument;
import Sampling.DefaultSampler;
import Variables.State;

public class DefaultLearner implements Learner {

	private static final int NUMBER_OF_GENERATED_STATES_PER_STEP = 10;

	private Model model;
	private Scorer scorer;

	private int steps = 50;

	double alpha;

	ObjectiveFunction objective;

	public void train(List<AnnotatedDocument> documents) {
		objective = new ObjectiveFunction();
		model = new Model();
		scorer = new Scorer(model);

		DefaultSampler sampler = new DefaultSampler(
				NUMBER_OF_GENERATED_STATES_PER_STEP);

		for (int i = 0; i < documents.size(); i++) {
			System.out.println("Document: " + i);
			AnnotatedDocument document = documents.get(i);
			State goldState = new State(document, document.getGoldEntities());

			State currentState = generateInitialAnnotations(document, goldState);

			for (int j = 0; j < steps; j++) {
				System.out.println("Step: " + j);
				State nextState = sampler.getNextState(currentState, scorer);
				System.out.println(nextState);
				if (objective.score(nextState, goldState) > objective.score(
						currentState, goldState)) {
					if (nextState.getScore() < currentState.getScore()) {
						model.update(nextState, alpha);
						model.update(currentState, -alpha);
					}
				} else {
					if (objective.score(nextState, goldState) < objective
							.score(currentState, goldState)) {
						if (nextState.getScore() > currentState.getScore()) {
							model.update(currentState, alpha);
							model.update(nextState, -alpha);
						}

					}
				}
				currentState = nextState;
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
