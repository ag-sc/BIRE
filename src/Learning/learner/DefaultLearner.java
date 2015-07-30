package Learning.learner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import Corpus.AnnotatedDocument;
import Learning.Learner;
import Learning.Model;
import Learning.ObjectiveFunction;
import Learning.Score;
import Learning.Scorer;
import Logging.Log;
import Sampling.Sampler;
import Sampling.SamplingHelper;
import Templates.Template;
import Variables.State;
import evaluation.EvaluationUtil;
import evaluation.SamplingProcedureRecord;

public class DefaultLearner implements Learner {

	{
		// Log.off();
	}
	private int steps;
	private double initialAlpha;
	private double finalAlpha;

	private Model model;
	// TODO Scorer should be part of the model
	private Scorer scorer;
	private ObjectiveFunction objective;
	private List<Sampler> samplers;
	private boolean sampleNextState;
	private SamplingProcedureRecord trainRecord;
	private SamplingProcedureRecord testRecord;

	public DefaultLearner(Model model, List<Sampler> samplers, int steps,
			double initialAlpha, double finalAlpha, boolean sampleNextState) {
		this.model = model;
		this.steps = steps;
		this.initialAlpha = initialAlpha;
		this.finalAlpha = finalAlpha;
		this.samplers = samplers;
		this.sampleNextState = sampleNextState;

		this.scorer = new Scorer(model);
		this.objective = new ObjectiveFunction();
	}

	@Override
	public void train(List<AnnotatedDocument> documents, int numberOfEpochs) {
		trainRecord = new SamplingProcedureRecord(documents.size(), steps,
				samplers.size(), initialAlpha);

		/**
		 * This variable represents the probability for the learner to select
		 * the next state for the sampling procedure according to the (best)
		 * objective function score (in contrast to the (best) model score).
		 * This probability starts at 1 at the beginning of the learning process
		 * and decreases to 0, so that the learner, in the end, favors the
		 * decisions of the model.
		 */
		double initialOmega = 1;
		double finalOmega = 0;
		double omegaStep = (initialOmega - finalOmega)
				/ (steps * documents.size() * numberOfEpochs - 1);
		double currentOmega = initialOmega;

		double alphaStep = (initialAlpha - finalAlpha)
				/ (steps * documents.size() * numberOfEpochs - 1);
		double currentAlpha = initialAlpha;

		Log.d("#Epochs=%s, #Documents=%s, #Steps=%s", numberOfEpochs,
				documents.size(), steps);
		Log.d("iO=%s, fO=%s, Os=%s; iA=%s, fA=%s, As=%s", initialOmega,
				finalOmega, omegaStep, initialAlpha, finalAlpha, alphaStep);
		for (int e = 0; e < numberOfEpochs; e++) {
			for (int d = 0; d < documents.size(); d++) {
				AnnotatedDocument document = documents.get(d);
				State goldState = document.getGoldState();

				State currentState = generateInitialAnnotations(document,
						goldState);

				for (int s = 0; s < steps; s++) {

					for (Sampler sampler : samplers) {
						Log.d("Epoch: %s/%s", e + 1, numberOfEpochs);
						Log.d("Document(%s/%s):\n\t%s\n\t%s", d + 1,
								documents.size(), document.getContent(),
								goldState);
						Log.d("Step: %s/%s", s + 1, steps);
						Log.d("Alpha: %s; Omega: %s", currentAlpha,
								currentOmega);
						Log.d("Sampler: %s", sampler.getClass().getSimpleName());
						List<State> nextStates = sampler.getNextStates(
								currentState, scorer);
						Log.d("Current model:\n%s", model.toString());

						Log.d("Score:");
						scorer.unroll(currentState);
						scorer.score(currentState);
						for (State state : nextStates) {
							scorer.unroll(state);
							scorer.score(state);
						}

						// nextStates.sort(State.modelScoreComparator);
						// for (int k = 0; k < nextStates.size(); k++) {
						//
						// Log.d("%s: %s", k, nextStates.get(k));
						// }

						Log.d("Update model with %s states and alpha=%s",
								nextStates.size(), currentAlpha);
						for (State state : nextStates) {
							updateModelForState(currentAlpha, goldState,
									currentState, state);
						}

						Log.d("Rescore:");
						scorer.score(currentState);
						for (State state : nextStates) {
							// no new unrolling need since the state did not
							// change
							// since the last time, only the model's
							// (template's)
							// weights
							scorer.score(state);
						}
						nextStates.sort(State.modelScoreComparator);
						for (int k = 0; k < nextStates.size(); k++) {
							Log.d("%s: %s", k, nextStates.get(k));
						}

						currentState = selectNextState(nextStates, currentOmega);

						// for (Template t : model.getTemplates()) {
						// Log.d("Weight updates of template %s:", t
						// .getClass().getSimpleName());
						// EvaluationUtil
						// .printWeightsSorted(t.featureWeightUpdates
						// .getFeatures());
						// }
						/*
						 * Log and record this sampling step
						 */
						Score objectiveFunctionScore = objective.score(
								currentState, goldState);
						Log.d("Next state: %s\n\tObjective funtion score = %s",
								currentState, objectiveFunctionScore);
						trainRecord.recordSamplingStep(d, s, sampler,
								nextStates, currentState);

						// try {
						// Log.d("PAUSE: Wait for input...");
						// System.in.read();
						// } catch (IOException e1) {
						// e1.printStackTrace();
						// }
						/*
						 * Clean the model of all generated states and deletes
						 * the temporarily stored feature weight updates. The
						 * state-to-factor relations are not used anymore.
						 */
						model.clean();

					}
					currentOmega -= omegaStep;
					currentAlpha -= alphaStep;
				}
				Log.d("Final, sampled state:\n\t%s\nObjective funtion score = %s",
						currentState, currentState.getObjectiveFunctionScore());
			}
		}
	}

	private void updateModelForState(double alpha, State goldState,
			State currentState, State possibleNextState) {

		double On = objective.score(possibleNextState, goldState).score;
		double Oc = objective.score(currentState, goldState).score;

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
		// double mr = (Mn - Mc) / Math.max(Mn, Mc);
		// double or = (On - Oc) / Math.max(On, Oc);
		// double l = (mr - or) / Math.max(mr, or);

		double learningSignal = 1;
		double learningStep = alpha * learningSignal;
		if (On > Oc) { // On-Oc > 0
			if (Mn <= Mc) { // Mn-Mc < 0
				Log.d("Model: DECREASE current; INCREASE next");
				model.update(currentState, -learningStep);
				model.update(possibleNextState, +learningStep);
			}
		} else if (On < Oc) { // On-Oc < 0
			if (Mn >= Mc) { // Mn-Mc > 0
				Log.d("Model: INCREASE current; DECREASE next");
				model.update(currentState, +learningStep);
				model.update(possibleNextState, -learningStep);
			}
		} else if (On == Oc) {
			// if (Mn > Mc) { // Mn-Mc > 0
			// Log.d("Model: DECREASE next; INCREASE current");
			// model.update(currentState, -learningStep);
			// model.update(possibleNextState, +learningStep);
			// } else if (Mn < Mc) { // Mn-Mc > 0
			// Log.d("Model: INCREASE next; DECREASE current");
			// model.update(currentState, +learningStep);
			// model.update(possibleNextState, -learningStep);
			// }
			// Log.d("Current state %s and next State %s are euqally good. Do nothing.",
			// currentState.getID(), possibleNextState.getID());
		}

	}

	public State selectNextState(List<State> statesOrderedByModel,
			double objectiveDrivenProbability) {
		List<State> statesOrderedByObjective = new ArrayList<State>(
				statesOrderedByModel);

		if (Math.random() < objectiveDrivenProbability) {
			Log.d("Next state: Best by OBJECTIVE");
			statesOrderedByObjective.sort(State.objectiveScoreComparator);
			return statesOrderedByObjective.get(0);
		} else {
			Log.d("Next state: Best by MODEL");
			return statesOrderedByModel.get(0);
		}
	}

	public void test(List<AnnotatedDocument> documents, int samplingSteps) {
		testRecord = new SamplingProcedureRecord(documents.size(),
				samplingSteps, samplers.size(), -1);

		for (int d = 0; d < documents.size(); d++) {
			AnnotatedDocument document = documents.get(d);
			State goldState = document.getGoldState();

			State currentState = generateInitialAnnotations(document, goldState);

			for (int s = 0; s < samplingSteps; s++) {
				for (Sampler sampler : samplers) {
					Log.d("Document(%s/%s):\n\t%s\n\t%s", d + 1,
							documents.size(), document.getContent(), goldState);
					Log.d("Step: %s/%s", s + 1, steps);
					Log.d("Current model:\n%s", model.toString());
					Log.d("Sampler: %s", sampler.getClass().getSimpleName());
					List<State> nextStates = sampler.getNextStates(
							currentState, scorer);

					Log.d("Score:");
					scorer.unroll(currentState);
					scorer.score(currentState);
					for (State state : nextStates) {
						scorer.unroll(state);
						scorer.score(state);
					}
					nextStates.sort(State.modelScoreComparator);
					for (State state : nextStates) {
						Log.d("%s", state);
					}

					if (sampleNextState) {
						currentState = SamplingHelper
								.drawRandomlyFrom(nextStates);
					} else {
						currentState = nextStates.get(0);
					}

					/*
					 * Log and record this sampling step
					 */
					Score objectiveFunctionScore = objective.score(
							currentState, goldState);
					Log.d("Next state: %s\n\tObjective funtion score = %s",
							currentState, objectiveFunctionScore);
					testRecord.recordSamplingStep(d, s, sampler, nextStates,
							currentState);

					/*
					 * Clean the model of all generated states. The
					 * state-to-factor relations are not used anymore
					 */
					model.clean();
				}
			}
		}

	}

	private State generateInitialAnnotations(AnnotatedDocument document,
			State goldState) {
		State state = new State(document);
		state.goldState = goldState;
		return state;
	}

	public Model getModel() {
		return model;
	}

	public Scorer getScorer() {
		return scorer;
	}

	public SamplingProcedureRecord getTrainRecord() {
		return trainRecord;
	}

	public SamplingProcedureRecord getTestRecord() {
		return testRecord;
	}

}
