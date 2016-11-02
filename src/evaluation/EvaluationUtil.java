package evaluation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import learning.Model;
import learning.Vector;
import templates.AbstractTemplate;
import variables.AbstractState;

public class EvaluationUtil {

	private static Logger log = LogManager.getFormatterLogger();

	public static Comparator<Entry<String, Double>> featureWeightComparator = new Comparator<Map.Entry<String, Double>>() {
		@Override
		public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
			return (int) -Math.signum(o1.getValue() - o2.getValue());
		}
	};

	private static final String MODEL_NAME_PATTERN = "Model_%s_%s-%s-%s_%s-%s-%s";

	public static DecimalFormat featureWeightFormat = new DecimalFormat("0.000000");
	public static DecimalFormat SCORE_FORMAT = new DecimalFormat("0.0000");

	public static String generateFilenameForModel(int numberOfTrainingSamples) {
		Calendar now = Calendar.getInstance();
		return String.format(MODEL_NAME_PATTERN, numberOfTrainingSamples, now.get(Calendar.YEAR),
				now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
	}

	public static String generateFilenameForModel(String name) {
		Calendar now = Calendar.getInstance();
		return String.format(MODEL_NAME_PATTERN, name, now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
				now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
	}

	public static void printPredictionPerformance(List<? extends AbstractState<?>> predictedStates) {
		double meanModelScore = 0;
		double meanObjectiveScore = 0;
		int count = 0;
		for (AbstractState<?> s : predictedStates) {
			meanModelScore += s.getModelScore();
			meanObjectiveScore += s.getObjectiveScore();

			log.info("ID: %s; model=%s; objective=%s", s.getID(), s.getModelScore(), s.getObjectiveScore());
			count++;
		}
		meanModelScore /= count;
		meanObjectiveScore /= count;
		log.info("Mean(n=%s):\tmodel=%s; objective=%s", count, meanModelScore, meanObjectiveScore);
	}

	public static double mean(List<Double> scores) {
		double score = 0;
		for (double s : scores) {
			score += s;
		}
		score /= scores.size();
		return score;
	}

	/**
	 * Prints all weights of the model model in descending order, discarding all
	 * weights with an absolute value smaller than minAbsValue. To print all
	 * values set minAbsValue <= 0
	 * 
	 * @param model
	 * @param minAbsValue
	 */
	public static void printWeights(Model<?, ?> model, double minAbsValue) {
		Map<String, Double> allWeights = new HashMap<String, Double>();
		for (AbstractTemplate<?, ?, ?> t : model.getTemplates()) {
			Vector weights = t.getWeights();
			for (String f : weights.getFeatureNames()) {
				double value = weights.getValueOfFeature(f);
				if (minAbsValue <= 0 || Math.abs(value) >= minAbsValue)
					allWeights.put(f, value);
			}
		}

		printWeightsSorted(allWeights);
	}

	public static void printWeightsSorted(Map<String, Double> allWeights) {
		ArrayList<Entry<String, Double>> features = new ArrayList<Entry<String, Double>>(allWeights.entrySet());
		Collections.sort(features, featureWeightComparator);
		for (Entry<String, Double> e : features) {
			log.info("%s: %s", featureWeightFormat.format(e.getValue()), e.getKey());
		}
	}

	public static void printScores(List<Double> scores) {
		for (double score : scores) {
			log.info("F1=%s", SCORE_FORMAT.format(score));
		}
	}

}
