package examples.weather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import evaluation.DataSplit;
import evaluation.EvaluationUtil;
import learning.DefaultLearner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.Trainer;
import learning.scorer.DefaultScorer;
import learning.scorer.Scorer;
import sampling.DefaultSampler;
import sampling.Explorer;
import sampling.Initializer;
import sampling.stoppingcriterion.StepLimitCriterion;
import sampling.stoppingcriterion.StoppingCriterion;
import templates.AbstractTemplate;

public class Main {
	private static Logger log = LogManager.getFormatterLogger();

	/**
	 * This example shows how to use the BIRE Framework for a simple toy
	 * problem. The task is to evaluate a weather observation (here a set of
	 * weather conditions e.g. {SUNNY, WINDY}) to predict its suitability to
	 * play outside. By that, the use of States, Variables, Explorers, Templates
	 * and objective functions is shown.
	 */
	public static void main(String[] args) {
		/*
		 * Load training and test data.
		 */
		List<PlayOutsideInstance> samples = generateLabledSamples(20);
		DataSplit<PlayOutsideInstance> dataSplit = new DataSplit<>(samples, 0.7, 0);
		List<PlayOutsideInstance> train = dataSplit.getTrain();
		List<PlayOutsideInstance> test = dataSplit.getTest();

		train.forEach(s -> log.debug("%s", s));
		test.forEach(s -> log.debug("%s", s));
		/*
		 * In the following, we setup all necessary components for training and
		 * testing.
		 */
		/*
		 * Define an objective function that guides the training procedure.
		 */
		ObjectiveFunction<PlayOutsideState, Boolean> objective = new PlayOutsideObjectiveFunction();

		/*
		 * Define templates that are responsible to generate factors/features to
		 * score intermediate, generated states.
		 */
		List<AbstractTemplate<PlayOutsideState>> templates = new ArrayList<>();
		templates.add(new PlayOutsideTemplate());

		/*
		 * Define a model and provide it with the necessary templates.
		 */
		Model<PlayOutsideState> model = new Model<>(templates);
		/*
		 * Create the scorer object that computes a score from the features of a
		 * factor and the weight vectors of the templates.
		 */
		Scorer<PlayOutsideState> scorer = new DefaultScorer<>();

		/*
		 * Create an Initializer that is responsible for providing an initial
		 * state for the sampling chain given a sentence.
		 */
		Initializer<WeatherInstance, PlayOutsideState> initializer = weatherInstance -> new PlayOutsideState(
				weatherInstance, Math.random() < 0.5 ? true : false);

		/*
		 * Define the explorers that will provide "neighboring" states given a
		 * starting state. The sampler will select one of these states as a
		 * successor state and, thus, perform the sampling procedure.
		 */
		List<Explorer<PlayOutsideState>> explorers = new ArrayList<>();
		explorers.add(new PlayOutsideExplorer());
		/*
		 * Create a sampler that generates sampling chains with which it will
		 * trigger weight updates during training.
		 */

		/*
		 * If you set this value too small, the sampler can not reach the
		 * optimal solution. Large values, however, increase computation time.
		 */
		int numberOfSamplingSteps = 5;
		StoppingCriterion<PlayOutsideState> stoppingCriterion = new StepLimitCriterion<>(numberOfSamplingSteps);
		DefaultSampler<PlayOutsideState, Boolean> sampler = new DefaultSampler<>(model, scorer, objective, explorers,
				stoppingCriterion);

		/*
		 * Define a learning strategy. The learner will receive state pairs
		 * which can be used to update the models parameters.
		 */
		DefaultLearner<PlayOutsideState> learner = new DefaultLearner<>(model, 0.1);

		log.info("####################");
		log.info("Start training");

		/*
		 * The trainer will loop over the data and invoke sampling and learning.
		 * Additionally, it can invoke predictions on new data.
		 */
		int numberOfEpochs = 10;
		Trainer trainer = new Trainer();
		List<PlayOutsideState> trainingResults = trainer.train(sampler, initializer, learner, train, numberOfEpochs);
		List<PlayOutsideState> testResults = trainer.test(sampler, initializer, test);

		/*
		 * Since the test function does not compute the objective score of its
		 * predictions, we do that here, manually, before we print the results.
		 */
		for (PlayOutsideState state : testResults) {
			Boolean goldResult = ((PlayOutsideInstance) state.getWeatherInstance()).getGoldResult();
			double s = objective.score(state, goldResult);
		}
		/*
		 * Now, that the predicted states have there objective score computed
		 * and set to their internal variable, we can print the prediction
		 * outcome.
		 */
		log.info("######## Results on TRAIN: ########");
		EvaluationUtil.printPredictionPerformance(trainingResults);
		log.info("######## Results on TEST: ########");
		EvaluationUtil.printPredictionPerformance(testResults);
		/*
		 * Finally, print the models weights.
		 */
		log.debug("Model weights:");
		EvaluationUtil.printWeights(model, -1);
	}

	private static List<PlayOutsideInstance> getLabledSamples() {
		List<PlayOutsideInstance> samples = new ArrayList<>();
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.SUNNY), true));
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.WINDY), true));
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.COLD), true));
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.SUNNY, WeatherCondition.WINDY), true));
		samples.add(new PlayOutsideInstance(
				Sets.newHashSet(WeatherCondition.COLD, WeatherCondition.SUNNY, WeatherCondition.WINDY), true));
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.COLD, WeatherCondition.SUNNY), true));

		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.RAINY), false));
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.COLD, WeatherCondition.WINDY), false));
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.RAINY, WeatherCondition.WINDY), false));
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.RAINY, WeatherCondition.COLD), false));
		samples.add(new PlayOutsideInstance(Sets.newHashSet(WeatherCondition.RAINY, WeatherCondition.SUNNY), false));
		samples.add(new PlayOutsideInstance(
				Sets.newHashSet(WeatherCondition.RAINY, WeatherCondition.COLD, WeatherCondition.SUNNY), false));
		samples.add(new PlayOutsideInstance(
				Sets.newHashSet(WeatherCondition.RAINY, WeatherCondition.COLD, WeatherCondition.WINDY), false));
		return samples;
	}

	/**
	 * We generate our artificial data here.
	 * 
	 * @param n
	 * @return
	 */
	private static List<PlayOutsideInstance> generateLabledSamples(int n) {
		List<PlayOutsideInstance> samples = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			List<WeatherCondition> allWeatherConditions = new ArrayList<>(Arrays.asList(WeatherCondition.values()));
			Collections.shuffle(allWeatherConditions);
			Set<WeatherCondition> weather = new HashSet<>();
			do {
				weather.add(allWeatherConditions.remove(0));

			} while (Math.random() < 0.5 && !allWeatherConditions.isEmpty());

			samples.add(new PlayOutsideInstance(weather, satisfiesCondition(weather)));
		}
		return samples;
	}

	/**
	 * This simple function determines if a certain set of weather conditions is
	 * acceptable for playing outside.
	 * 
	 * @param weather
	 * @return
	 */
	private static boolean satisfiesCondition(Set<WeatherCondition> weather) {
		ToDoubleFunction<WeatherCondition> f = w -> {
			switch (w) {
			case SUNNY:
				return +2.0;
			case WINDY:
				return +0.5;
			case COLD:
				return -0.4;
			case RAINY:
				return -2.5;
			default:
				return 0;
			}
		};
		double score = weather.stream().mapToDouble(f).sum();
		return score > 0 ? true : false;
	}

}
