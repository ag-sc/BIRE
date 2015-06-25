package Learning.learner;

import java.util.List;

import Corpus.AnnotatedDocument;
import Learning.Learner;
import Learning.Model;
import Learning.ObjectiveFunction;
import Learning.Scorer;
import Logging.Log;
import Sampling.Sampler;
import Sampling.SamplingHelper;
import Variables.State;

public class DefaultLearner implements Learner {

	private int steps;
	private double alpha = 0.1;

	private Model model;
	private Scorer scorer;
	private ObjectiveFunction objective;
	private List<Sampler> samplers;

	public DefaultLearner(int steps, double alpha, List<Sampler> samplers) {
		this.steps = steps;
		this.alpha = alpha;
		this.samplers = samplers;
	}

	public void train(List<AnnotatedDocument> documents) {
		objective = new ObjectiveFunction();
		model = new Model();
		scorer = new Scorer(model);

		for (int i = 0; i < documents.size(); i++) {
			Log.d("Document: %s", i);
			AnnotatedDocument document = documents.get(i);
			State goldState = new State(document, document.getGoldEntities());

			State currentState = generateInitialAnnotations(document, goldState);

			for (int j = 0; j < steps; j++) {
				Log.d("Step: %s", j);
				for (Sampler sampler : samplers) {
					Log.d("Sampler: %s", sampler.getClass().getSimpleName());
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
					Log.d("Next state: %s", nextState);
					currentState = nextState;
				}
				System.out.println(model);
			}
		}

	}

	private void updateModelForState(State goldState, State currentState,
			State possibleNextState) {
		Log.off();
		Log.d("Next %s:\tO(g,c)=%s,\tO(g,n)=%s\t|\tM(c)=%s,\tM(n)=%s",
				possibleNextState.getID(),
				objective.score(currentState, goldState),
				objective.score(possibleNextState, goldState),
				currentState.getModelScore(), possibleNextState.getModelScore());
		if (objective.score(possibleNextState, goldState) > objective.score(
				currentState, goldState)) {
			if (possibleNextState.getModelScore() < currentState
					.getModelScore()) {
				model.update(possibleNextState, alpha);
				model.update(currentState, -alpha);
			}
		} else if (objective.score(possibleNextState, goldState) < objective
				.score(currentState, goldState)) {
			if (possibleNextState.getModelScore() > currentState
					.getModelScore()) {
				model.update(currentState, alpha);
				model.update(possibleNextState, -alpha);
			}
		} else {
			// TODO What to do if ObjectiveFunction scores are equal, but model
			// scores not?
			Log.d("Current state %s and next State %s are euqally good. Do nothing.",
					currentState.getID(), possibleNextState.getID());
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
