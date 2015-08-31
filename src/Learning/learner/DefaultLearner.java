package Learning.learner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Corpus.AnnotatedDocument;
import Learning.Learner;
import Learning.Model;
import Learning.ObjectiveFunction;
import Learning.Score;
import Learning.Scorer;
import Learning.Vector;
import Learning.callbacks.DocumentCallback;
import Learning.callbacks.EpochCallback;
import Learning.callbacks.SamplerCallback;
import Learning.callbacks.StepCallback;
import Logging.Log;
import Sampling.Sampler;
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
	private double initialOmega;
	private double finalOmega;

	private Model model;
	// TODO Scorer should be part of the model
	private Scorer scorer;
	private ObjectiveFunction objective;
	private List<Sampler> samplers;

	/*
	 * Provide empty default callbacks to avoid null-checks
	 */
	private EpochCallback epochCallback = new EpochCallback() {
	};
	private DocumentCallback documentCallback = new DocumentCallback() {
	};
	private StepCallback stepCallback = new StepCallback() {
	};
	private SamplerCallback samplerCallback = new SamplerCallback() {
	};

	private SamplingProcedureRecord trainRecord;
	private SamplingProcedureRecord testRecord;

	private ExecutorService pool;
	private final boolean multithreaded = true;
	private int numberOfThreads = 4;

	public DefaultLearner() {
		this.objective = new ObjectiveFunction();
	}

	public DefaultLearner(Model model, List<Sampler> samplers, int steps, double initialAlpha, double finalAlpha,
			double initialOmega, double finalOmega) {
		this();
		this.model = model;
		this.samplers = samplers;
		this.steps = steps;
		this.initialAlpha = initialAlpha;
		this.finalAlpha = finalAlpha;
		this.initialOmega = initialOmega;
		this.finalOmega = finalOmega;
		this.scorer = new Scorer(model);
	}

	public DefaultLearner(Model model, List<Sampler> samplers, int steps, double initialAlpha, double finalAlpha,
			boolean sampleNextState) {
		this(model, samplers, steps, initialAlpha, finalAlpha, 1, 0);
		this.scorer = new Scorer(model);
	}

	@Override
	public void train(List<AnnotatedDocument> documents, int numberOfEpochs) {
		if (multithreaded) {
			pool = Executors.newFixedThreadPool(numberOfThreads);
		}
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
			epochCallback.onStartEpoch(this, e, numberOfEpochs);

			for (int d = 0; d < documents.size(); d++) {
				AnnotatedDocument document = documents.get(d);
				documentCallback.onStartDocument(this, document, d, documents.size(), e, numberOfEpochs);

				State goldState = document.getGoldState();
				State currentState = generateInitialState(document);

				for (int s = 0; s < steps; s++) {
					stepCallback.onStartStep(this, s, steps, document, d, documents.size(), e, numberOfEpochs);
					for (int indexOfSampler = 0; indexOfSampler < samplers.size(); indexOfSampler++) {
						Sampler sampler = samplers.get(indexOfSampler);
						samplerCallback.onStartSampler(this, sampler, indexOfSampler, samplers.size(), s, steps,
								document, d, documents.size(), e, numberOfEpochs);

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

						unroll(currentState, nextStates);
						scoreWithObjective(goldState, currentState, nextStates);
						update(currentAlpha, goldState, currentState, nextStates);

						Log.d("(Re)Score:");
						scoreWithModel(Arrays.asList(currentState));

						currentState = selectNextState(nextStates, currentOmega);
						/*
						 * Log and record this sampling step
						 */
						Log.d("Next state: %s\n\tObjective funtion score = %s", currentState,
								currentState.getObjectiveScore().score);
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
						// currentState.markAsUnchanged();
						model.trimToState(currentState);

						Log.d("Changed: %s", currentState.getChangedEntities());
						for (Template t : model.getTemplates()) {
							Log.d("Templates: %s: %s", t.getClass().getSimpleName(), t.getFactors().keySet());

						}
						samplerCallback.onEndSampler(this, sampler, indexOfSampler, samplers.size(), s, steps, document,
								d, documents.size(), e, numberOfEpochs);
					}
					currentOmega -= omegaStep;
					currentAlpha -= alphaStep;
					stepCallback.onEndStep(this, s, steps, document, d, documents.size(), e, numberOfEpochs);
				}
				long stopTime = System.currentTimeMillis();
				Log.d("Final, sampled state:\n\t%s\nObjective funtion score = %s", currentState,
						currentState.getObjectiveScore());
				Log.d("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
				documentCallback.onEndDocument(this, document, d, documents.size(), e, numberOfEpochs);
			}
			epochCallback.onEndEpoch(this, e, numberOfEpochs);
		}
	}

	private void update(double currentAlpha, State goldState, State currentState, List<State> nextStates) {
		Log.d("Update model with %s states and alpha=%s", nextStates.size(), currentAlpha);
		for (State state : nextStates) {
			atomicUpdate(currentAlpha, goldState, currentState, state);
		}
	}

	private void unroll(State currentState, List<State> nextStates) {
		long unrollID = TaggedTimer.start("SC-UNROLL");
		Log.d("Unroll features for %s states...", nextStates.size() + 1);

		if (multithreaded) {
			Collection<Future<?>> futures = new LinkedList<Future<?>>();
			/*
			 * Enqueue a task in the thread pool that unrolls the features of a
			 * given state
			 */
			futures.add(pool.submit(new UnrollFeaturesTask(scorer, currentState)));
			for (State state : nextStates) {
				futures.add(pool.submit(new UnrollFeaturesTask(scorer, state)));
			}
			/*
			 * Wait for all states to be finished before proceeding
			 */
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			scorer.unroll(currentState);
			for (State state : nextStates) {
				scorer.unroll(state);
			}
		}
		TaggedTimer.stop(unrollID);
	}

	private void scoreWithModel(List<State> nextStates) {
		long scID = TaggedTimer.start("MODEL-SCORE");
		Log.d("Score %s states according to model...", nextStates.size() + 1);
		if (multithreaded) {
			Collection<Future<?>> futures = new LinkedList<Future<?>>();
			/*
			 * Enqueue a task in the thread pool that scores the state according
			 * to the model
			 */
			for (State state : nextStates) {
				futures.add(pool.submit(new ScoreWithModelTask(scorer, state)));
			}
			/*
			 * Wait for all states to be finished before proceeding
			 */
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			for (State state : nextStates) {
				scorer.score(state);
			}
		}
		TaggedTimer.stop(scID);
	}

	private void scoreWithObjective(State goldState, State currentState, List<State> nextStates) {
		long scID = TaggedTimer.start("OBJ-SCORE");
		Log.d("Score %s states according to objective...", nextStates.size() + 1);
		if (multithreaded) {
			Collection<Future<?>> futures = new LinkedList<Future<?>>();
			/*
			 * Enqueue a task in the thread pool that scores the state according
			 * to the objective
			 */
			futures.add(pool.submit(new ScoreWithObjectiveTask(goldState, currentState)));
			for (State state : nextStates) {
				futures.add(pool.submit(new ScoreWithObjectiveTask(goldState, state)));
			}
			/*
			 * Wait for all states to be finished before proceeding
			 */
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			objective.score(currentState, goldState);
			for (State state : nextStates) {
				objective.score(state, goldState);
			}
		}
		TaggedTimer.stop(scID);
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

		double O1 = state1.getObjectiveScore().score;
		double O2 = state2.getObjectiveScore().score;
		// double O1 = objective.score(state1, goldState).score;
		// double O2 = objective.score(state2, goldState).score;
		return O1 > O2;
	}

	public State selectNextState(List<State> states, double objectiveDrivenProbability) {
		if (Math.random() < objectiveDrivenProbability) {
			Log.d("Next state: Best by OBJECTIVE");
			Collections.sort(states, State.objectiveScoreComparator);
			return states.get(0);
		} else {
			Log.d("Next state: Best by MODEL");
			scoreWithModel(states);
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

					currentState = nextStates.get(0);

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

	public void setEpochCallback(EpochCallback epochCallback) {
		this.epochCallback = epochCallback;
	}

	public void setDocumentCallback(DocumentCallback documentCallback) {
		this.documentCallback = documentCallback;
	}

	public void setStepCallback(StepCallback stepCallback) {
		this.stepCallback = stepCallback;
	}

	public void setSamplerCallback(SamplerCallback samplerCallback) {
		this.samplerCallback = samplerCallback;
	}

	class UnrollFeaturesTask implements Runnable {
		private State state;
		private Scorer scorer;

		public UnrollFeaturesTask(Scorer scorer, State state) {
			this.scorer = scorer;
			this.state = state;
		}

		@Override
		public void run() {
			scorer.unroll(state);
		}
	}

	class ScoreWithModelTask implements Runnable {
		private State state;
		private Scorer scorer;

		public ScoreWithModelTask(Scorer scorer, State state) {
			this.scorer = scorer;
			this.state = state;
		}

		@Override
		public void run() {
			scorer.score(state);
		}
	}

	class ScoreWithObjectiveTask implements Runnable {
		private State state;
		private State goldState;

		public ScoreWithObjectiveTask(State goldState, State state) {
			this.goldState = goldState;
			this.state = state;
		}

		@Override
		public void run() {
			objective.score(state, goldState);
		}
	}

}
