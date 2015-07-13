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

	{
		// Log.off();
	}
	private int steps;
	private double alpha;

	private Model model;
	private Scorer scorer;
	private ObjectiveFunction objective;
	private List<Sampler> samplers;
	private boolean sampleNextState;

	public DefaultLearner(Model model, List<Sampler> samplers, int steps,
			double alpha, boolean sampleNextState) {
		this.model = model;
		this.steps = steps;
		this.alpha = alpha;
		this.samplers = samplers;
		this.sampleNextState = sampleNextState;

		this.scorer = new Scorer(model);
		this.objective = new ObjectiveFunction();
	}

	public void train(List<AnnotatedDocument> documents) {
		for (int i = 0; i < documents.size(); i++) {
			AnnotatedDocument document = documents.get(i);
			State goldState = document.getGoldState();

			Log.d("Document(%s):\n\t%s\n\t%s", i, document.getContent(),
					goldState);

			State currentState = generateInitialAnnotations(document, goldState);

			for (int j = 0; j < steps; j++) {
				Log.d("Step: %s", j);
				for (Sampler sampler : samplers) {
//					Log.d("Current model:\n%s", model.toDetailedString());
					Log.d("Sampler: %s", sampler.getClass().getSimpleName());
					List<State> nextStates = sampler.getNextStates(
							currentState, scorer);

					Log.d("Score:");
					for (State state : nextStates) {
						scorer.score(state);
					}
					nextStates.sort(State.comparator);
					for (State state : nextStates) {
						Log.d("%s", state);
					}

					Log.d("Update model with %s states", nextStates.size());
					for (State state : nextStates) {
						updateModelForState(alpha / nextStates.size(),
								goldState, currentState, state);
					}

					Log.d("Rescore:");
					for (State state : nextStates) {
						scorer.score(state);
						// Log.d("%s", state);
					}
					nextStates.sort(State.comparator);
					for (State state : nextStates) {
						Log.d("%s", state);
					}

					if (sampleNextState)
						currentState = SamplingHelper
								.drawRandomlyFrom(nextStates);
					else
						// TODO check if this implementation makes it possible
						// to get stuck in local minima
						currentState = nextStates.get(0);

					Log.d("Next state: %s\n\tObjective funtion score = %s",
							currentState,
							objective.score(currentState, goldState));
				}
			}
			Log.d("Final, sampled state:\n\t%s\nObjective funtion score = %s",
					currentState, objective.score(currentState, goldState));
		}

	}

	private void updateModelForState(double alpha, State goldState,
			State currentState, State possibleNextState) {

		double On = objective.score(possibleNextState, goldState);
		double Oc = objective.score(currentState, goldState);

		double Mn = possibleNextState.getModelScore();
		double Mc = currentState.getModelScore();

		Log.d("Next %s:\tO(g,c)=%s,\tO(g,n)=%s\t|\tM(c)=%s,\tM(n)=%s",
				possibleNextState.getID(), Oc, On, Mc, Mn);

		/*-
		 * On/Oc   Mn/Mc
		 * 2 * (0.9 - 0.8)/(0.9 + 0.8) = 0.2/1.7
		 * 2 * (2.5 - 1.5)/(2.5 + 1.5) = 1/4
		 */
		// double learningSignal = 2 * (On - Oc) / (On + Oc) * 2 * (Mn - Mc) /
		// (Mn + Mc) / 2;
		// double learningSignal = Math.signum((On - Oc) * (Mn - Mc));
		double learningSignal = 1;
		double learningStep = alpha * learningSignal;
		if (On > Oc) { // On-Oc > 0
			if (Mn <= Mc) { // Mn-Mc < 0
				Log.d("Model: INCREASE next; DECREASE current");
				model.update(currentState, -learningStep);
				model.update(possibleNextState, +learningStep);
			}
		} else if (On < Oc) { // On-Oc < 0
			if (Mn >= Mc) { // Mn-Mc > 0
				Log.d("Model: DECREASE next; INCREASE current");
				model.update(currentState, +learningStep);
				model.update(possibleNextState, -learningStep);
			}
		} else if (On == Oc) {
			if (Mn > Mc) { // Mn-Mc > 0
				Log.d("Model: DECREASE next; INCREASE current");
				model.update(currentState, -learningStep);
				model.update(possibleNextState, +learningStep);
			} else if (Mn < Mc) { // Mn-Mc > 0
				Log.d("Model: INCREASE next; DECREASE current");
				model.update(currentState, +learningStep);
				model.update(possibleNextState, -learningStep);
			}
			// Log.d("Current state %s and next State %s are euqally good. Do nothing.",
			// currentState.getID(), possibleNextState.getID());
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
