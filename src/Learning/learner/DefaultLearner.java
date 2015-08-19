package Learning.learner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Corpus.AnnotatedDocument;
import Learning.Learner;
import Learning.Model;
import Learning.ObjectiveFunction;
import Learning.Score;
import Learning.Scorer;
import Learning.Vector;
import Logging.Log;
import Sampling.Sampler;
import Sampling.SamplingHelper;
import Templates.Template;
import Variables.State;
import evaluation.SamplingProcedureRecord;
import evaluation.TaggedTimer;

public class DefaultLearner implements Learner {

	{
		// Log.off();
	}

	enum LearningProcedure {
		SAMPLE_RANK, PERCEPTRON;
	}

	private LearningProcedure learningProcedure = LearningProcedure.SAMPLE_RANK;
	private int steps;
	private double initialAlpha;
	private double finalAlpha;
	private double initialOmega = 1;
	private double finalOmega = 0;

	private Model model;
	// TODO Scorer should be part of the model
	private Scorer scorer;
	private ObjectiveFunction objective;
	private List<Sampler> samplers;
	private boolean sampleNextState;
	private SamplingProcedureRecord trainRecord;
	private SamplingProcedureRecord testRecord;

	public DefaultLearner(Model model, List<Sampler> samplers, int steps, double initialAlpha, double finalAlpha,
			double initialOmega, double finalOmega) {
		this.model = model;
		this.samplers = samplers;
		this.steps = steps;
		this.initialAlpha = initialAlpha;
		this.finalAlpha = finalAlpha;
		this.initialOmega = initialOmega;
		this.finalOmega = finalOmega;
		this.scorer = new Scorer(model);
		this.objective = new ObjectiveFunction();
	}

	public DefaultLearner(Model model, List<Sampler> samplers, int steps, double initialAlpha, double finalAlpha,
			boolean sampleNextState) {
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
		int hack = 0;
		long startTime = System.currentTimeMillis();
		trainRecord = new SamplingProcedureRecord(documents.size(), steps, samplers.size(), initialAlpha);
		/**
		 * This variable represents the probability for the learner to select
		 * the next state for the sampling procedure according to the (best)
		 * objective function score (in contrast to the (best) model score).
		 * This probability decreases during the training, so that the learner,
		 * in the end, favors the decisions of the model.
		 */
		double omegaStep = (initialOmega - finalOmega) / (steps * documents.size() * numberOfEpochs - 1);
		double currentOmega = initialOmega;

		double alphaStep = (initialAlpha - finalAlpha) / (steps * documents.size() * numberOfEpochs - 1);
		double currentAlpha = initialAlpha;
		Log.d("#Epochs=%s, #Documents=%s, #Steps=%s", numberOfEpochs, documents.size(), steps);
		Log.d("iO=%s, fO=%s, Os=%s; iA=%s, fA=%s, As=%s", initialOmega, finalOmega, omegaStep, initialAlpha, finalAlpha,
				alphaStep);
		for (int e = 0; e < numberOfEpochs; e++) {
			for (int d = 0; d < documents.size(); d++) {
				AnnotatedDocument document = documents.get(d);
				State goldState = document.getGoldState();

				State currentState = generateInitialState(document);

				for (int s = 0; s < steps; s++) {

					for (Sampler sampler : samplers) {
						Log.d("############################################");
						Log.d("Epoch: %s/%s", e + 1, numberOfEpochs);
						Log.d("Document(%s/%s):\n\t%s\n\t%s", d + 1, documents.size(), document.getContent(),
								goldState);
						Log.d("Step: %s/%s", s + 1, steps);
						Log.d("Alpha: %s; Omega: %s", currentAlpha, currentOmega);
						Log.d("Sampler: %s", sampler.getClass().getSimpleName());
						Log.d("Current model:\n%s", model.toString());

						long genID = TaggedTimer.start("GENERATE");
						List<State> nextStates = sampler.getNextStates(currentState, scorer);
						TaggedTimer.stop(genID);

						// TODO "unroll" can be parallelized using threads
						scorer.unroll(currentState);
						// scorer.score(currentState);
						for (State state : nextStates) {
							scorer.unroll(state);
							// scorer.score(state);
						}

						Log.d("Update model with %s states and alpha=%s", nextStates.size(), currentAlpha);
						for (State state : nextStates) {
							atomicUpdate(currentAlpha, goldState, currentState, state);
						}

						Log.d("(Re)Score:");
						scorer.score(currentState);

						currentState = selectNextState(nextStates, currentOmega);

						/*
						 * Log and record this sampling step
						 */
						Score objectiveFunctionScore = objective.score(currentState, goldState);
						Log.d("Next state: %s\n\tObjective funtion score = %s", currentState, objectiveFunctionScore);
						trainRecord.recordSamplingStep(document, d, s, sampler, nextStates, currentState);

						// try {
						// Log.d("Model after update:\n%s",
						// model.toDetailedString());
						// Log.d("########################");
						// Log.d("PAUSE: Wait for ENTER...");
						// Log.d("########################");
						// System.in.read();
						// } catch (IOException e1) {
						// e1.printStackTrace();
						// }
						Log.d("Changed: %s", currentState.getChangedEntities());
						Log.d("Graph:\n%s", currentState.getFactorGraph());
						for (Template t : model.getTemplates()) {
							Log.d("Templates: %s: %s", t.getClass().getSimpleName(), t.getFactors().keySet());
							Log.d("%s/%s recomputed factors", t.recomputed, t.all);
						}
						Log.d("######## Mark as unchanged and trim to state %s ########", currentState.getID());
						currentState.markAsUnchanged();
						model.trimToState(currentState);

						Log.d("Changed: %s", currentState.getChangedEntities());
						for (Template t : model.getTemplates()) {
							Log.d("Templates: %s: %s", t.getClass().getSimpleName(), t.getFactors().keySet());

						}
						// hack++;
						// if (hack > 1000) {
						// hack = 0;
						// TaggedTimer.printTimings();
						// Log.d("current trainingTime: %s (%s seconds)",
						// (System.currentTimeMillis() - startTime),
						// (System.currentTimeMillis() - startTime) / 1000);
						// try {
						// Log.d("PAUSE: Wait for ENTER...");
						// System.in.read();
						// } catch (IOException e1) {
						// e1.printStackTrace();
						// }
						// }

					}
					currentOmega -= omegaStep;
					currentAlpha -= alphaStep;
				}
				long stopTime = System.currentTimeMillis();
				Log.d("Final, sampled state:\n\t%s\nObjective funtion score = %s", currentState,
						currentState.getObjectiveFunctionScore());
				Log.d("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
			}
		}
	}

	private void atomicUpdate(double alpha, State goldState, State currentState, State possibleNextState) {
		Log.methodOff();
		switch (learningProcedure) {
		case SAMPLE_RANK: {
			double weightedDifferences = 0;
			/*
			 * Collect differences of features for both states and remember
			 * respective template
			 */
			long diffID = TaggedTimer.start("UP-DIFF");
			Map<Template, Vector> featureDifferences = new HashMap<Template, Vector>();
			for (Template t : model.getTemplates()) {
				Vector differences = t.getFeatureDifferences(possibleNextState, currentState);
				// Vector allNextFeatures =
				// t.getJointFeatures(possibleNextState);
				// Vector allCurrentFeatures = t.getJointFeatures(currentState);
				// Vector differences = Vector.substract(allNextFeatures,
				// allCurrentFeatures);
				featureDifferences.put(t, differences);
				weightedDifferences += differences.dotProduct(t.getWeightVector());
			}
			TaggedTimer.stop(diffID);

			if (weightedDifferences > 0 && preference(currentState, possibleNextState, goldState)) {
				Log.d("Next:\t%s", possibleNextState);
				Log.d("Current:\t%s", currentState);
				updateFeatures(featureDifferences, -alpha);
			} else if (weightedDifferences <= 0 && preference(possibleNextState, currentState, goldState)) {
				Log.d("Next:\t%s", possibleNextState);
				Log.d("Current:\t%s", currentState);
				updateFeatures(featureDifferences, +alpha);
			} else {
			}
			break;
		}
		case PERCEPTRON: {
			double On = objective.score(possibleNextState, goldState).score;
			double Oc = objective.score(currentState, goldState).score;

			double Mn = possibleNextState.getModelScore();
			double Mc = currentState.getModelScore();

			Log.d("Next %s:\tO(g,c)=%s,\tO(g,n)=%s\t|\tM(c)=%s,\tM(n)=%s", possibleNextState.getID(), Oc, On, Mc, Mn);

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
				// Log.d("Current state %s and next State %s are equally good.
				// Do nothing.",
				// currentState.getID(), possibleNextState.getID());
			}
			break;
		}
		}

	}

	private void updateFeatures(Map<Template, Vector> featureDifferences, double learningDirection) {
		Log.methodOff();
		long upID = TaggedTimer.start("UP-UPDATE");
		Log.d("UPDATE: learning direction: %s", learningDirection);
		for (Template t : model.getTemplates()) {
			Log.d("Template: %s", t.getClass().getSimpleName());
			Vector features = featureDifferences.get(t);
			for (Entry<String, Double> featureDifference : features.getFeatures().entrySet()) {
				// only update for real differences
				if (featureDifference.getValue() != 0) {
					double learningStep = learningDirection * featureDifference.getValue();
					Log.d("\t%s -> %s:\t%s", featureDifference.getValue(), learningStep, featureDifference.getKey());
					t.update(featureDifference.getKey(), learningStep);
				}
			}
		}
		TaggedTimer.stop(upID);

	}

	private boolean preference(State state1, State state2, State goldState) {

		double O1 = objective.score(state1, goldState).score;
		double O2 = objective.score(state2, goldState).score;
		return O1 > O2;
	}

	public State selectNextState(List<State> states, double objectiveDrivenProbability) {
		if (Math.random() < objectiveDrivenProbability) {
			Log.d("Next state: Best by OBJECTIVE");
			Collections.sort(states, State.objectiveScoreComparator);
			return states.get(0);
		} else {
			Log.d("Next state: Best by MODEL");
			for (State state : states) {
				scorer.score(state);
			}
			Collections.sort(states, State.modelScoreComparator);
			return states.get(0);
		}
	}

	public void test(List<AnnotatedDocument> documents, int samplingSteps) {
		testRecord = new SamplingProcedureRecord(documents.size(), samplingSteps, samplers.size(), -1);

		for (int d = 0; d < documents.size(); d++) {
			AnnotatedDocument document = documents.get(d);
			State goldState = document.getGoldState();

			State currentState = generateInitialState(document);

			for (int s = 0; s < samplingSteps; s++) {
				for (Sampler sampler : samplers) {
					Log.d("Document(%s/%s):\n\t%s\n\t%s", d + 1, documents.size(), document.getContent(), goldState);
					Log.d("Step: %s/%s", s + 1, steps);
					Log.d("Current model:\n%s", model.toString());
					Log.d("Sampler: %s", sampler.getClass().getSimpleName());
					List<State> nextStates = sampler.getNextStates(currentState, scorer);

					Log.d("Score:");
					scorer.unroll(currentState);
					scorer.score(currentState);
					for (State state : nextStates) {
						scorer.unroll(state);
						scorer.score(state);
					}
					Collections.sort(nextStates, State.modelScoreComparator);

					if (sampleNextState) {
						currentState = SamplingHelper.drawRandomlyFrom(nextStates);
					} else {
						currentState = nextStates.get(0);
					}

					/*
					 * Log and record this sampling step
					 */
					Score objectiveFunctionScore = objective.score(currentState, goldState);
					Log.d("Next state: %s\n\tObjective funtion score = %s", currentState, objectiveFunctionScore);
					testRecord.recordSamplingStep(document, d, s, sampler, nextStates, currentState);

					model.trimToState(currentState);
				}
			}
		}

	}

	private State generateInitialState(AnnotatedDocument document) {
		State state = new State(document);
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
