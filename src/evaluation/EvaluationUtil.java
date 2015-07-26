package evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Learning.Vector;
import Learning.learner.DefaultLearner;
import Logging.Log;
import Templates.Template;
import evaluation.SamplingProcedureRecord.SamplingStepRecord;

public class EvaluationUtil {

	private static final String MODEL_NAME_PATTERN = "Model_N=%s_%s-%s-%s_%s-%s-%s";
	private static final String RECORD_NAME_PATTERN = "%s-Records_N=%s_%s-%s-%s_%s-%s-%s";

	public static String generateFilenameForModel(int numberOfTrainingSamples) {
		Calendar now = Calendar.getInstance();
		return String.format(MODEL_NAME_PATTERN, numberOfTrainingSamples,
				now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
				now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
	}

	public static String generateFilenameForRecords(boolean isTest,
			int numberOfRecords) {
		Calendar now = Calendar.getInstance();
		return String.format(RECORD_NAME_PATTERN, isTest ? "Test" : "Train",
				numberOfRecords, now.get(Calendar.YEAR),
				now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
				now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
	}

	public static void storeRecords(List<SamplingProcedureRecord> records,
			File file) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				file));
		out.writeObject(records);
		out.close();
	}

	public static List<SamplingProcedureRecord> loadRecords(String file)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		List<SamplingProcedureRecord> records = (List<SamplingProcedureRecord>) in
				.readObject();
		in.close();
		return records;
	}

	public static void printPerformance(
			List<SamplingProcedureRecord> testRecords) {
		double meanModelScore = 0;
		double meanPrecision = 0;
		double meanRecall = 0;
		double meanOFScore = 0;
		int count = 0;
		for (SamplingProcedureRecord r : testRecords) {
			for (int d = 0; d < r.numberOfDocuments; d++) {
				SamplingStepRecord step = r.samplingSteps[d][r.numberOfSteps - 1]
						.get(r.numberOfSamplers - 1);

				meanModelScore += step.acceptedState.modelScore;
				meanPrecision += step.acceptedState.objectiveFunctionScore.precision;
				meanRecall += step.acceptedState.objectiveFunctionScore.recall;
				meanOFScore += step.acceptedState.objectiveFunctionScore.score;

				Log.d("Document: %s; model=%s; precision=%s, recall=%s, score=%s",
						d, step.acceptedState.modelScore,
						step.acceptedState.objectiveFunctionScore.precision,
						step.acceptedState.objectiveFunctionScore.recall,
						step.acceptedState.objectiveFunctionScore.score);
				count++;
			}
		}
		meanModelScore /= count;
		meanPrecision /= count;
		meanRecall /= count;
		meanOFScore /= count;
		Log.d("Mean:\nmodel=%s; precision=%s, recall=%s, score=%s",
				meanModelScore, meanPrecision, meanRecall, meanOFScore);
	}

	public static Comparator<Entry<String, Double>> featureWeightComparator = new Comparator<Map.Entry<String, Double>>() {
		@Override
		public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
			return (int) -Math.signum(o1.getValue() - o2.getValue());
		}
	};

	public static DecimalFormat featureWeightFormat = new DecimalFormat(
			"0.000000");

	public static void printWeights(DefaultLearner learner) {
		Map<String, Double> allWeights = new HashMap<String, Double>();
		for (Template t : learner.getModel().getTemplates()) {
			Vector weights = t.getWeightVector();
			for (String f : weights.getFeatures()) {
				allWeights.put(f, weights.getValueOfFeature(f));
			}
		}

		ArrayList<Entry<String, Double>> features = new ArrayList<Entry<String, Double>>(
				allWeights.entrySet());
		features.sort(featureWeightComparator);
		for (Entry<String, Double> e : features) {
			Log.d("%s: %s", featureWeightFormat.format(e.getValue()),
					e.getKey());
		}
	}

	static class FeatureWeightComparator implements Comparator<String> {

		private Map<String, Double> weights;

		public FeatureWeightComparator(Map<String, Double> weights) {
			this.weights = weights;
		}

		@Override
		public int compare(String o1, String o2) {
			return (int) Math.signum(weights.get(o1) - weights.get(o2));
		}

	}
}
