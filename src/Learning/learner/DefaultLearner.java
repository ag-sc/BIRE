package Learning.learner;

import java.util.List;

import Corpus.AnnotatedDocument;
import Learning.Learner;
import Learning.Model;
import Learning.ObjectiveFunction;
import Learning.Scorer;
import Logging.Log;
import Sampling.Sampler;
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
			State goldState = document.getGoldState();

			State currentState = generateInitialAnnotations(document, goldState);

			for (int j = 0; j < steps; j++) {
				Log.d("Step: %s", j);
				for (Sampler sampler : samplers) {
					Log.d("Sampler: %s", sampler.getClass().getSimpleName());
					List<State> nextStates = sampler.getNextStates(
							currentState, scorer);
					for (State state : nextStates) {
						// TODO if we want to work on a set of currentStates,
						// how can we update the model (do we compare each
						// current state with each next state?)
						updateModelForState(goldState, currentState, state);
					}

					// System.out.println("Rescore: ");
					// for (State state : nextStates) {
					// scorer.score(state);
					// System.out.println(state);
					// }

					State nextState = nextStates.get(0);

					// State nextState = SamplingHelper
					// .drawRandomlyFrom(nextStates);
					Log.d("Next state: %s", nextState);
					currentState = nextState;
				}
				Log.d("%s", model);
			}
		}

	}

	private void updateModelForState(State goldState, State currentState,
			State possibleNextState) {
		Log.methodOff();
		Log.d("Next %s:\tO(g,c)=%s,\tO(g,n)=%s\t|\tM(c)=%s,\tM(n)=%s",
				possibleNextState.getID(),
				objective.score(currentState, goldState),
				objective.score(possibleNextState, goldState),
				currentState.getModelScore(), possibleNextState.getModelScore());

		double On = objective.score(possibleNextState, goldState);
		double Oc = objective.score(currentState, goldState);

		double Mn = possibleNextState.getModelScore();
		double Mc = currentState.getModelScore();

		/*-
		 * 2 * (0.9 - 0.8)/(0.9 + 0.8) = 0.2/1.7
		 * 2 * (2.5 - 1.5)/(2.5 + 1.5) = 1/4
		 */
		// double learningSignal = 2 * (On - Oc) / (On + Oc) * 2 * (Mn - Mc) /
		// (Mn + Mc) / 2;
		// double learningSignal = Math.signum((On - Oc) * (Mn - Mc));
		double learningSignal = 1;
		double learningStep = alpha * learningSignal;
		if (On >= Oc) { // On-Oc > 0
			if (Mn < Mc) { // Mn-Mc < 0
				model.update(possibleNextState, learningStep);
				model.update(currentState, -learningStep);
			}
		} else if (On < Oc) { // On-Oc < 0
			if (Mn > Mc) { // Mn-Mc > 0
				model.update(currentState, learningStep);
				model.update(possibleNextState, -learningStep);
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
