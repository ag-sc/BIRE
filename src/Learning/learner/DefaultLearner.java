package Learning.learner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Corpus.AnnotatedDocument;
import Learning.Learner;
import Learning.Model;
import Learning.Score;
import Learning.Scorer;
import Learning.Vector;
import Learning.callbacks.IDocumentCallback;
import Learning.callbacks.IEpochCallback;
import Learning.callbacks.ISamplerCallback;
import Learning.callbacks.IStepCallback;
import Learning.objective.ObjectiveFunction;
import Logging.Log;
import Sampling.Sampler;
import Templates.Template;
import Variables.IState;
import evaluation.TaggedTimer;

public class DefaultLearner<StateT extends IState> implements Learner<StateT> {

	// private static Logger log = LogManager.getFormatterLogger();
	private static Logger log = LogManager.getFormatterLogger(DefaultLearner.class.getName());

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

	private Model<StateT> model;
	// TODO Scorer should be part of the model
	private Scorer<StateT> scorer;
	private ObjectiveFunction<StateT> objective;
	private List<Sampler<StateT>> samplers;

	private boolean useInitialAnnotations = false;
	/*
	 * Provide empty default callbacks to avoid null-checks
	 */
	private IEpochCallback epochCallback = new IEpochCallback() {
	};
	private IDocumentCallback documentCallback = new IDocumentCallback() {
	};
	private IStepCallback stepCallback = new IStepCallback() {
	};
	private ISamplerCallback samplerCallback = new ISamplerCallback() {
	};

	// private SamplingProcedureRecord trainRecord;
	// private SamplingProcedureRecord testRecord;

	// private ExecutorService pool;
	private final boolean multithreaded = true;
	private int numberOfThreads = 12;

	public DefaultLearner(Model<StateT> model, List<Sampler<StateT>> samplers, ObjectiveFunction<StateT> objective,
			int steps, double initialAlpha, double finalAlpha, double initialOmega, double finalOmega) {
		this.model = model;
		this.samplers = samplers;
		this.objective = objective;
		this.steps = steps;
		this.initialAlpha = initialAlpha;
		this.finalAlpha = finalAlpha;
		this.initialOmega = initialOmega;
		this.finalOmega = finalOmega;
		this.scorer = new Scorer<>(model);
	}

	public DefaultLearner(Model<StateT> model, List<Sampler<StateT>> samplers, ObjectiveFunction<StateT> objective,
			int steps, double initialAlpha, double finalAlpha, boolean sampleNextState) {
		this(model, samplers, objective, steps, initialAlpha, finalAlpha, 1, 0);
		this.scorer = new Scorer<>(model);
	}

	// @Override
	public List<StateT> train(List<? extends AnnotatedDocument<StateT>> documents, int numberOfEpochs) {
		// if (multithreaded) {
		// pool = Executors.newFixedThreadPool(numberOfThreads);
		// }
		List<StateT> finalStates = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		/**
		 * This variable represents the probability for the learner to select
		 * the next state for the sampling procedure according to the (best)
		 * objective function score (in contrast to the (best) model score).
		 * This probability decreases during the training, so that the learner,
		 * in the end, favors the decisions of the model.
		 */
		double currentOmega = initialOmega;
		double omegaStep = (initialOmega - finalOmega) / (steps * documents.size() * numberOfEpochs - 1);

		double currentAlpha = initialAlpha;
		double alphaStep = (initialAlpha - finalAlpha) / (steps * documents.size() * numberOfEpochs - 1);
		log.info("#Epochs=%s, #Documents=%s, #Steps=%s", numberOfEpochs, documents.size(), steps);
		log.info("iO=%s, fO=%s, Os=%s; iA=%s, fA=%s, As=%s", initialOmega, finalOmega, omegaStep, initialAlpha,
				finalAlpha, alphaStep);
		for (int e = 0; e < numberOfEpochs; e++) {
			epochCallback.onStartEpoch(this, e, numberOfEpochs);

			for (int d = 0; d < documents.size(); d++) {
				AnnotatedDocument<StateT> document = documents.get(d);
				documentCallback.onStartDocument(this, document, d, documents.size(), e, numberOfEpochs);

				StateT goldState = document.getGoldState();

				StateT currentState = generateInitialState(document);

				for (int s = 0; s < steps; s++) {
					stepCallback.onStartStep(this, s, steps, document, d, documents.size(), e, numberOfEpochs);
					for (int indexOfSampler = 0; indexOfSampler < samplers.size(); indexOfSampler++) {
						Sampler<StateT> sampler = samplers.get(indexOfSampler);
						samplerCallback.onStartSampler(this, sampler, indexOfSampler, samplers.size(), s, steps,
								document, d, documents.size(), e, numberOfEpochs);

						log.info("############################################");
						log.info("Epoch: %s/%s", e + 1, numberOfEpochs);
						log.info("Document(%s/%s):\n\t%s\n\t%s", d + 1, documents.size(), document.getContent(),
								goldState);
						log.info("Step: %s/%s", s + 1, steps);
						log.info("Alpha: %s; Omega: %s", currentAlpha, currentOmega);
						log.info("Sampler: %s", sampler.getClass().getSimpleName());
						log.debug("Current model:\n%s", model.toString());
						long genID = TaggedTimer.start("GENERATE");
						List<StateT> nextStates = sampler.getNextStates(currentState, scorer);
						TaggedTimer.stop(genID);

						unroll(currentState, nextStates);
						scoreWithObjective(goldState, currentState, nextStates);
						update(currentAlpha, goldState, currentState, nextStates);

						log.debug("(Re)Score:");
						scoreWithModel(Arrays.asList(currentState));

						currentState = selectNextState(nextStates, currentOmega);
						/*
						 * Log and record this sampling step
						 */
						log.info("Next state: %s\n\tObjective funtion score = %s", currentState,
								currentState.getObjectiveScore().score);
						// trainRecord.recordSamplingStep(document, d, s,
						// sampler, nextStates, currentState);

						log.debug("Changed: %s", currentState.getChangedVariables());
						log.debug("Graph:\n%s", currentState.getFactorGraph());
						for (Template<StateT> t : model.getTemplates()) {
							log.debug("Templates: %s: %s", t.getClass().getSimpleName(), t.getFactors().keySet());
							log.debug("%s/%s recomputed factors", t.recomputed, t.all);
						}
						log.debug("######## Mark as unchanged and trim to state %s ########", currentState.getID());
						currentState.markAsUnchanged();
						model.trimToState(currentState);

						log.debug("Changed: %s", currentState.getChangedVariables());
						for (Template<StateT> t : model.getTemplates()) {
							log.debug("Templates: %s: %s", t.getClass().getSimpleName(), t.getFactors().keySet());
						}
						samplerCallback.onEndSampler(this, sampler, indexOfSampler, samplers.size(), s, steps, document,
								d, documents.size(), e, numberOfEpochs);
					}
					currentOmega -= omegaStep;
					currentAlpha -= alphaStep;
					stepCallback.onEndStep(this, s, steps, document, d, documents.size(), e, numberOfEpochs);
				}
				long stopTime = System.currentTimeMillis();
				log.info("Final, sampled state:\n\t%s\nObjective funtion score = %s", currentState,
						currentState.getObjectiveScore());
				log.info("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
				documentCallback.onEndDocument(this, document, d, documents.size(), e, numberOfEpochs);

				/*
				 * Store the final predicted state for the current document if
				 * the current epoch is the final one.
				 */
				if (e == numberOfEpochs - 1) {
					finalStates.add(currentState);
				}
			}
			epochCallback.onEndEpoch(this, e, numberOfEpochs);
		}
		return finalStates;
	}

	/**
	 * Updates the model with each of the possible next states relative to the
	 * provided current state.
	 * 
	 * @param currentAlpha
	 * @param goldState
	 * @param currentState
	 * @param nextStates
	 */
	private void update(double currentAlpha, StateT goldState, StateT currentState, List<StateT> nextStates) {
		log.debug("Update model with %s states and alpha=%s", nextStates.size(), currentAlpha);
		for (StateT state : nextStates) {
			atomicUpdate(currentAlpha, goldState, currentState, state);
		}
	}

	/**
	 * Applies the model's template to each of the given states, thus unrolling
	 * (computing) the features. The <i>multithreaded</i> flag determines if the
	 * computation is performed in parallel or sequentially.
	 * 
	 * @param currentState
	 * @param nextStates
	 */
	private void unroll(StateT currentState, List<StateT> nextStates) {
		long unrollID = TaggedTimer.start("SC-UNROLL");
		log.debug("Unroll features for %s states...", nextStates.size() + 1);
		List<StateT> allStates = new ArrayList<>(nextStates);
		allStates.add(currentState);

		Stream<StateT> stream = null;
		if (multithreaded) {
			stream = allStates.parallelStream();
			// Collection<Future<?>> futures = new LinkedList<Future<?>>();
			// /*
			// * Enqueue a task in the thread pool that unrolls the features of
			// a
			// * given state
			// */
			// futures.add(pool.submit(new UnrollFeaturesTask(scorer,
			// currentState)));
			// for (StateT state : nextStates) {
			// futures.add(pool.submit(new UnrollFeaturesTask(scorer, state)));
			// }
			// /*
			// * Wait for all states to be finished before proceeding
			// */
			// for (Future<?> future : futures) {
			// try {
			// future.get();
			// } catch (InterruptedException e1) {
			// e1.printStackTrace();
			// } catch (ExecutionException e1) {
			// e1.printStackTrace();
			// }
			// }
		} else {
			stream = allStates.stream();
			// scorer.unroll(currentState);
			// for (StateT state : nextStates) {
			// scorer.unroll(state);
			// }
		}
		stream.forEach(s -> scorer.unroll(s));
		TaggedTimer.stop(unrollID);
	}

	/**
	 * Computes the model scores for each of the given states. The
	 * <i>multithreaded</i> flag determines if the computation is performed in
	 * parallel or sequentially.
	 * 
	 * @param nextStates
	 */
	private void scoreWithModel(List<StateT> nextStates) {
		long scID = TaggedTimer.start("MODEL-SCORE");
		log.debug("Score %s states according to model...", nextStates.size());

		Stream<StateT> stream = null;
		if (multithreaded) {
			stream = nextStates.parallelStream();
		} else {
			stream = nextStates.stream();
		}
		stream.forEach(s -> scorer.score(s));
		// if (multithreaded) {
		// Collection<Future<?>> futures = new LinkedList<Future<?>>();
		// /*
		// * Enqueue a task in the thread pool that scores the state according
		// * to the model
		// */
		// for (StateT state : nextStates) {
		// futures.add(pool.submit(new ScoreWithModelTask(scorer, state)));
		// }
		// /*
		// * Wait for all states to be finished before proceeding
		// */
		// for (Future<?> future : futures) {
		// try {
		// future.get();
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// } catch (ExecutionException e1) {
		// e1.printStackTrace();
		// }
		// }
		// } else {
		// for (StateT state : nextStates) {
		// scorer.score(state);
		// }
		// }
		TaggedTimer.stop(scID);
	}

	/**
	 * Computes the objective scores for each of the given states. The
	 * <i>multithreaded</i> flag determines if the computation is performed in
	 * parallel or sequentially.
	 * 
	 * @param goldState
	 * @param currentState
	 * @param nextStates
	 */
	private void scoreWithObjective(StateT goldState, StateT currentState, List<StateT> nextStates) {
		long scID = TaggedTimer.start("OBJ-SCORE");
		log.debug("Score %s states according to objective...", nextStates.size() + 1);
		List<StateT> allStates = new ArrayList<>(nextStates);
		allStates.add(currentState);

		Stream<StateT> stream = null;
		if (multithreaded) {
			stream = allStates.parallelStream();
		} else {
			stream = allStates.stream();
		}
		stream.forEach(s -> objective.score(s, goldState));
		// if (multithreaded) {
		// Collection<Future<?>> futures = new LinkedList<Future<?>>();
		// /*
		// * Enqueue a task in the thread pool that scores the state according
		// * to the objective
		// */
		// futures.add(pool.submit(new ScoreWithObjectiveTask(goldState,
		// currentState)));
		// for (StateT state : nextStates) {
		// futures.add(pool.submit(new ScoreWithObjectiveTask(goldState,
		// state)));
		// }
		// /*
		// * Wait for all states to be finished before proceeding
		// */
		// for (Future<?> future : futures) {
		// try {
		// future.get();
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// } catch (ExecutionException e1) {
		// e1.printStackTrace();
		// }
		// }
		// } else {
		// objective.score(currentState, goldState);
		// for (StateT state : nextStates) {
		// objective.score(state, goldState);
		// }
		// }
		TaggedTimer.stop(scID);
	}

	/**
	 * Performs a model update according to a learning scheme (currently
	 * SampleRank or our custom perceptron learning). The update step is scaled
	 * with the provided alpha value.
	 * 
	 * @param alpha
	 * @param goldState
	 * @param currentState
	 * @param possibleNextState
	 */
	private void atomicUpdate(double alpha, StateT goldState, StateT currentState, StateT possibleNextState) {
		Log.methodOff();
		switch (learningProcedure) {
		case SAMPLE_RANK: {
			double weightedDifferences = 0;
			/*
			 * Collect differences of features for both states and remember
			 * respective template
			 */
			long diffID = TaggedTimer.start("UP-DIFF");
			Map<Template<StateT>, Vector> featureDifferences = new HashMap<>();
			for (Template<StateT> t : model.getTemplates()) {
				Vector differences = t.getFeatureDifferences(possibleNextState, currentState);
				featureDifferences.put(t, differences);
				weightedDifferences += differences.dotProduct(t.getWeightVector());
			}
			TaggedTimer.stop(diffID);

			if (weightedDifferences > 0 && preference(currentState, possibleNextState, goldState)) {
				log.trace("Next:\t%s", possibleNextState);
				log.trace("Current:\t%s", currentState);
				updateFeatures(featureDifferences, -alpha);
			} else if (weightedDifferences <= 0 && preference(possibleNextState, currentState, goldState)) {
				log.trace("Next:\t%s", possibleNextState);
				log.trace("Current:\t%s", currentState);
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

			log.trace("Next %s:\tO(g,c)=%s,\tO(g,n)=%s\t|\tM(c)=%s,\tM(n)=%s", possibleNextState.getID(), Oc, On, Mc,
					Mn);

			double learningSignal = 1;
			double learningStep = alpha * learningSignal;
			if (On > Oc) { // On-Oc > 0
				if (Mn <= Mc) { // Mn-Mc < 0
					log.trace("Model: DECREASE current; INCREASE next");
					model.update(currentState, -learningStep);
					model.update(possibleNextState, +learningStep);
				}
			} else if (On < Oc) { // On-Oc < 0
				if (Mn >= Mc) { // Mn-Mc > 0
					log.trace("Model: INCREASE current; DECREASE next");
					model.update(currentState, +learningStep);
					model.update(possibleNextState, -learningStep);
				}
			} else if (On == Oc) {
				// if (Mn > Mc) { // Mn-Mc > 0
				// log.info("Model: DECREASE next; INCREASE current");
				// model.update(currentState, -learningStep);
				// model.update(possibleNextState, +learningStep);
				// } else if (Mn < Mc) { // Mn-Mc > 0
				// log.info("Model: INCREASE next; DECREASE current");
				// model.update(currentState, +learningStep);
				// model.update(possibleNextState, -learningStep);
				// }
				// log.info("Current state %s and next State %s are equally
				// good.
				// Do nothing.",
				// currentState.getID(), possibleNextState.getID());
			}
			break;
		}
		}

	}

	/**
	 * The features present in the vectors in the featureDifferences map are
	 * update according to their respective difference and the given direction.
	 * Since the feature difference (times the direction) is used as the update
	 * step, differences of 0 are not applied since they do not change the
	 * weight anyway.
	 * 
	 * @param featureDifferences
	 * @param learningDirection
	 */
	private void updateFeatures(Map<Template<StateT>, Vector> featureDifferences, double learningDirection) {
		Log.methodOff();
		long upID = TaggedTimer.start("UP-UPDATE");
		log.trace("UPDATE: learning direction: %s", learningDirection);
		for (Template<StateT> t : model.getTemplates()) {
			log.trace("Template: %s", t.getClass().getSimpleName());
			Vector features = featureDifferences.get(t);
			for (Entry<String, Double> featureDifference : features.getFeatures().entrySet()) {
				// only update for real differences
				if (featureDifference.getValue() != 0) {
					double learningStep = learningDirection * featureDifference.getValue();
					log.trace("\t%s -> %s:\t%s", featureDifference.getValue(), learningStep,
							featureDifference.getKey());
					t.update(featureDifference.getKey(), learningStep);
				}
			}
		}
		TaggedTimer.stop(upID);

	}

	/**
	 * Compares the objective scores of the state1 and state2 using the provided
	 * goldState to decide if state1 is preferred over state2. Note: The
	 * objective scores are merely accessed but not recomputed. This step needs
	 * to be done before.
	 * 
	 * @param state1
	 * @param state2
	 * @param goldState
	 * @return
	 */
	private boolean preference(StateT state1, StateT state2, StateT goldState) {

		double O1 = state1.getObjectiveScore().score;
		double O2 = state2.getObjectiveScore().score;
		// double O1 = objective.score(state1, goldState).score;
		// double O2 = objective.score(state2, goldState).score;
		return O1 > O2;
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
	public StateT selectNextState(List<StateT> states, double objectiveDrivenProbability) {
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

	public List<StateT> test(List<? extends AnnotatedDocument<StateT>> documents, int samplingSteps) {
		List<StateT> finalStates = new ArrayList<>();
		for (int d = 0; d < documents.size(); d++) {
			AnnotatedDocument<StateT> document = documents.get(d);
			StateT goldState = document.getGoldState();

			StateT currentState = generateInitialState(document);

			for (int s = 0; s < samplingSteps; s++) {
				for (int indexOfSampler = 0; indexOfSampler < samplers.size(); indexOfSampler++) {
					Sampler<StateT> sampler = samplers.get(indexOfSampler);
					log.info("Document(%s/%s):\n\t%s\n\t%s", d + 1, documents.size(), document.getContent(), goldState);
					log.info("Step: %s/%s", s + 1, steps);
					log.info("Current model:\n%s", model.toString());
					log.info("Sampler: %s", sampler.getClass().getSimpleName());
					List<StateT> nextStates = sampler.getNextStates(currentState, scorer);

					log.debug("Score:");
					unroll(currentState, nextStates);
					
					// scorer.score(currentState);
					List<StateT> allStates = new ArrayList<>(nextStates);
					allStates.add(currentState);
					
					scoreWithModel(allStates);
					// scorer.unroll(currentState);
					// scorer.score(currentState);
					// for (StateT state : nextStates) {
					// scorer.unroll(state);
					// scorer.score(state);
					// }
					Collections.sort(nextStates, StateT.modelScoreComparator);

					currentState = nextStates.get(0);

					Score objectiveFunctionScore = objective.score(currentState, goldState);
					log.info("Next state: %s\n\tObjective funtion score = %s", currentState, objectiveFunctionScore);

					currentState.markAsUnchanged();
					model.trimToState(currentState);
				}
			}
			finalStates.add(currentState);
		}
		return finalStates;
	}

	private StateT generateInitialState(AnnotatedDocument<StateT> document) {
		// TODO check if this mode (initial knowledge) is requested or if the
		// learner should start from scratch
		return document.getInitialState().duplicate();
	}

	public boolean isUseInitialAnnotations() {
		return useInitialAnnotations;
	}

	public void setUseInitialAnnotations(boolean useInitialAnnotations) {
		this.useInitialAnnotations = useInitialAnnotations;
	}

	public Model<StateT> getModel() {
		return model;
	}

	public Scorer<StateT> getScorer() {
		return scorer;
	}
	//
	// public SamplingProcedureRecord getTrainRecord() {
	// return trainRecord;
	// }
	//
	// public SamplingProcedureRecord getTestRecord() {
	// return testRecord;
	// }

	public void setEpochCallback(IEpochCallback epochCallback) {
		this.epochCallback = epochCallback;
	}

	public void setDocumentCallback(IDocumentCallback documentCallback) {
		this.documentCallback = documentCallback;
	}

	public void setStepCallback(IStepCallback stepCallback) {
		this.stepCallback = stepCallback;
	}

	public void setSamplerCallback(ISamplerCallback samplerCallback) {
		this.samplerCallback = samplerCallback;
	}

	// class UnrollFeaturesTask implements Runnable {
	// private StateT state;
	// private Scorer<StateT> scorer;
	//
	// public UnrollFeaturesTask(Scorer<StateT> scorer, StateT state) {
	// this.scorer = scorer;
	// this.state = state;
	// }
	//
	// @Override
	// public void run() {
	// scorer.unroll(state);
	// }
	// }
	//
	// class ScoreWithModelTask implements Runnable {
	// private StateT state;
	// private Scorer<StateT> scorer;
	//
	// public ScoreWithModelTask(Scorer<StateT> scorer, StateT state) {
	// this.scorer = scorer;
	// this.state = state;
	// }
	//
	// @Override
	// public void run() {
	// scorer.score(state);
	// }
	// }
	//
	// class ScoreWithObjectiveTask implements Runnable {
	// private StateT state;
	// private StateT goldState;
	//
	// public ScoreWithObjectiveTask(StateT goldState, StateT state) {
	// this.goldState = goldState;
	// this.state = state;
	// }
	//
	// @Override
	// public void run() {
	// objective.score(state, goldState);
	// }
	// }

}
