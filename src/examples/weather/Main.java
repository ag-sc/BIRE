package examples.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import evaluation.DataSplit;
import evaluation.EvaluationUtil;
import examples.weather.WeatherInstance.Humidity;
import examples.weather.WeatherInstance.Outlook;
import examples.weather.WeatherInstance.Temperature;
import examples.weather.WeatherInstance.Windy;
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
		List<PlayOutsideInstance> samples = getLabledSamples();
		
		/*
		 * Some code for n-fold cross validation
		 */
		Collections.shuffle(samples);
		int N = samples.size();
		int n = 7;
		double avrgAccuracy = 0;
		double step = ((float) N) / n;
		double k = 0;
		for (int i = 0; i < n; i++) {
			double j = k;
			k = j + step;

			List<PlayOutsideInstance> test = samples.subList((int) Math.floor(j), (int) Math.floor(k));
			List<PlayOutsideInstance> train = new ArrayList<>(samples);
			train.removeAll(test);

			
			log.info("Train data:");
			train.forEach(s -> log.info("%s", s));

			log.info("Test data:");
			test.forEach(s -> log.info("%s", s));
			/*
			 * In the following, we setup all necessary components for training
			 * and testing.
			 */
			/*
			 * Define an objective function that guides the training procedure.
			 */
			ObjectiveFunction<PlayOutsideState, Boolean> objective = new PlayOutsideObjectiveFunction();

			/*
			 * Define templates that are responsible to generate
			 * factors/features to score intermediate, generated states.
			 */
			List<AbstractTemplate<PlayOutsideState>> templates = new ArrayList<>();
			templates.add(new PlayOutsideTemplate(3));

			/*
			 * Define a model and provide it with the necessary templates.
			 */
			Model<PlayOutsideState> model = new Model<>(templates);
			/*
			 * Create the scorer object that computes a score from the features
			 * of a factor and the weight vectors of the templates.
			 */
			Scorer<PlayOutsideState> scorer = new DefaultScorer<>();

			/*
			 * Create an Initializer that is responsible for providing an
			 * initial state for the sampling chain given a sentence.
			 */
			Initializer<WeatherInstance, PlayOutsideState> initializer = weatherInstance -> new PlayOutsideState(
					weatherInstance, Math.random() < 0.64);

			/*
			 * Define the explorers that will provide "neighboring" states given
			 * a starting state. The sampler will select one of these states as
			 * a successor state and, thus, perform the sampling procedure.
			 */
			List<Explorer<PlayOutsideState>> explorers = new ArrayList<>();
			explorers.add(new PlayOutsideExplorer());
			/*
			 * Create a sampler that generates sampling chains with which it
			 * will trigger weight updates during training.
			 */

			/*
			 * If you set this value too small, the sampler can not reach the
			 * optimal solution. Large values, however, increase computation
			 * time.
			 */
			int numberOfSamplingSteps = 2;
			StoppingCriterion<PlayOutsideState> stoppingCriterion = new StepLimitCriterion<>(numberOfSamplingSteps);
			DefaultSampler<PlayOutsideState, Boolean> sampler = new DefaultSampler<>(model, scorer, objective,
					explorers, stoppingCriterion);

			/*
			 * Define a learning strategy. The learner will receive state pairs
			 * which can be used to update the models parameters.
			 */
			DefaultLearner<PlayOutsideState> learner = new DefaultLearner<>(model, 0.1);

			log.info("####################");
			log.info("Start training");

			/*
			 * The trainer will loop over the data and invoke sampling and
			 * learning. Additionally, it can invoke predictions on new data.
			 */
			int numberOfEpochs = 10;
			Trainer trainer = new Trainer();
			List<PlayOutsideState> trainingResults = trainer.train(sampler, initializer, learner, train,
					numberOfEpochs);
			List<PlayOutsideState> testResults = trainer.test(sampler, initializer, test);

			/*
			 * Since the test function does not compute the objective score of
			 * its predictions, we do that here, manually, before we print the
			 * results.
			 */
			double accuracy = 0;
			for (PlayOutsideState state : testResults) {
				Boolean goldResult = ((PlayOutsideInstance) state.getWeatherInstance()).getGoldResult();
				double s = objective.score(state, goldResult);
				accuracy += s;
			}
			accuracy /= testResults.size();
			/*
			 * Now, that the predicted states have there objective score
			 * computed and set to their internal variable, we can print the
			 * prediction outcome.
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
			avrgAccuracy += accuracy;
		}
		avrgAccuracy /= n;
		log.info("%s-fold cross validation: %s", n, avrgAccuracy);
	}

	private static List<PlayOutsideInstance> getLabledSamples() {
		List<PlayOutsideInstance> samples = new ArrayList<>();
		String text = "SUNNY,HOT,HIGH,FALSE,NO " + "SUNNY,HOT,HIGH,TRUE,NO " + "OVERCAST,HOT,HIGH,FALSE,YES "
				+ "RAINY,MILD,HIGH,FALSE,YES " + "RAINY,COOL,NORMAL,FALSE,YES " + "RAINY,COOL,NORMAL,TRUE,NO "
				+ "OVERCAST,COOL,NORMAL,TRUE,YES " + "SUNNY,MILD,HIGH,FALSE,NO " + "SUNNY,COOL,NORMAL,FALSE,YES "
				+ "RAINY,MILD,NORMAL,FALSE,YES " + "SUNNY,MILD,NORMAL,TRUE,YES " + "OVERCAST,MILD,HIGH,TRUE,YES "
				+ "OVERCAST,HOT,NORMAL,FALSE,YES " + "RAINY,MILD,HIGH,TRUE,NO";
		for (String instance : text.split(" ")) {
			String[] x = instance.split(",");
			samples.add(new PlayOutsideInstance(Outlook.valueOf(x[0]), Temperature.valueOf(x[1]),
					Humidity.valueOf(x[2]), Windy.valueOf(x[3]), x[4].equals("YES")));
		}
		return samples;
	}
	// private static List<PlayOutsideInstance> getLabledSamples() {
	// List<PlayOutsideInstance> samples = new ArrayList<>();
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.SUNNY), true));
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.WINDY), true));
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.COLD), true));
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.SUNNY,
	// WeatherCondition.WINDY), true));
	// samples.add(new PlayOutsideInstance(
	// Sets.newHashSet(WeatherCondition.COLD, WeatherCondition.SUNNY,
	// WeatherCondition.WINDY), true));
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.COLD,
	// WeatherCondition.SUNNY), true));
	//
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.RAINY), false));
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.COLD,
	// WeatherCondition.WINDY), false));
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.RAINY,
	// WeatherCondition.WINDY), false));
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.RAINY,
	// WeatherCondition.COLD), false));
	// samples.add(new
	// PlayOutsideInstance(Sets.newHashSet(WeatherCondition.RAINY,
	// WeatherCondition.SUNNY), false));
	// samples.add(new PlayOutsideInstance(
	// Sets.newHashSet(WeatherCondition.RAINY, WeatherCondition.COLD,
	// WeatherCondition.SUNNY), false));
	// samples.add(new PlayOutsideInstance(
	// Sets.newHashSet(WeatherCondition.RAINY, WeatherCondition.COLD,
	// WeatherCondition.WINDY), false));
	// return samples;
	// }

	// /**
	// * We generate our artificial data here.
	// *
	// * @param n
	// * @return
	// */
	// private static List<PlayOutsideInstance> generateLabledSamples(int n) {
	// List<PlayOutsideInstance> samples = new ArrayList<>();
	// for (int i = 0; i < n; i++) {
	// List<WeatherCondition> allWeatherConditions = new
	// ArrayList<>(Arrays.asList(WeatherCondition.values()));
	// Collections.shuffle(allWeatherConditions);
	// Set<WeatherCondition> weather = new HashSet<>();
	// do {
	// weather.add(allWeatherConditions.remove(0));
	//
	// } while (Math.random() < 0.5 && !allWeatherConditions.isEmpty());
	//
	// samples.add(new PlayOutsideInstance(weather,
	// satisfiesCondition(weather)));
	// }
	// return samples;
	// }

	// /**
	// * This simple function determines if a certain set of weather conditions
	// is
	// * acceptable for playing outside.
	// *
	// * @param weather
	// * @return
	// */
	// private static boolean satisfiesCondition(Set<WeatherCondition> weather)
	// {
	// ToDoubleFunction<WeatherCondition> f = w -> {
	// switch (w) {
	// case SUNNY:
	// return +2.0;
	// case WINDY:
	// return +0.5;
	// case COLD:
	// return -0.4;
	// case RAINY:
	// return -2.5;
	// default:
	// return 0;
	// }
	// };
	// double score = weather.stream().mapToDouble(f).sum();
	// return score > 0 ? true : false;
	// }

}
