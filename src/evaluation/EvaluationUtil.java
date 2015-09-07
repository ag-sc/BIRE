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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import utility.EntityID;
import Corpus.Document;
import Corpus.Token;
import Learning.Score;
import Learning.Vector;
import Learning.learner.DefaultLearner;
import Logging.Log;
import Templates.Template;
import Variables.ArgumentRole;
import Variables.EntityAnnotation;
import Variables.State;
import evaluation.SamplingProcedureRecord.SamplingStepRecord;

public class EvaluationUtil {

	private static final String MODEL_NAME_PATTERN = "Model_%s_%s-%s-%s_%s-%s-%s";
	private static final String RECORDS_NAME_PATTERN = "%s-Records_N=%s_%s-%s-%s_%s-%s-%s";
	private static final String RECORD_NAME_PATTERN = "Record_%s_%s-%s-%s_%s-%s-%s";

	public static String generateFilenameForModel(int numberOfTrainingSamples) {
		Calendar now = Calendar.getInstance();
		return String.format(MODEL_NAME_PATTERN, numberOfTrainingSamples,
				now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
				now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
	}

	public static String generateFilenameForModel(String name) {
		Calendar now = Calendar.getInstance();
		return String.format(MODEL_NAME_PATTERN, name, now.get(Calendar.YEAR),
				now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
				now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
	}

	public static String generateFilenameForRecords(boolean isTest,
			int numberOfRecords) {
		Calendar now = Calendar.getInstance();
		return String.format(RECORDS_NAME_PATTERN, isTest ? "Test" : "Train",
				numberOfRecords, now.get(Calendar.YEAR),
				now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
				now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
	}

	public static void storeRecords(List<SamplingProcedureRecord> records,
			File dir) throws FileNotFoundException, IOException {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, EvaluationUtil.generateFilenameForRecords(
				true, records.size()));
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				file));
		out.writeObject(records);
		out.close();
	}

	public static void storeRecord(SamplingProcedureRecord record, File dir,
			String name) throws IOException {
		Calendar now = Calendar.getInstance();
		String finalName = String.format(RECORD_NAME_PATTERN, name,
				now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
				now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE), now.get(Calendar.SECOND));

		File file = new File(dir, finalName);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				file));
		out.writeObject(record);
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

	public static SamplingProcedureRecord loadRecord(String file)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		SamplingProcedureRecord record = (SamplingProcedureRecord) in
				.readObject();
		in.close();
		return record;
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

				Log.d("Document %s: %s; model=%s; precision=%s, recall=%s, score=%s",
						d, step.document.getName(),
						step.acceptedState.modelScore,
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
		Log.d("Mean(n=%s):\tmodel=%s; precision=%s, recall=%s, score=%s",
				count, meanModelScore, meanPrecision, meanRecall, meanOFScore);
	}

	public static Score mean(SamplingProcedureRecord r) {
		double meanPrecision = 0;
		double meanRecall = 0;
		double meanOFScore = 0;
		for (int d = 0; d < r.numberOfDocuments; d++) {
			SamplingStepRecord step = r.samplingSteps[d][r.numberOfSteps - 1]
					.get(r.numberOfSamplers - 1);

			meanPrecision += step.acceptedState.objectiveFunctionScore.precision;
			meanRecall += step.acceptedState.objectiveFunctionScore.recall;
			meanOFScore += step.acceptedState.objectiveFunctionScore.score;

			// Log.d("Document %s: %s; model=%s; precision=%s, recall=%s,
			// score=%s", d, step.document.getName(),
			// step.acceptedState.modelScore,
			// step.acceptedState.objectiveFunctionScore.precision,
			// step.acceptedState.objectiveFunctionScore.recall,
			// step.acceptedState.objectiveFunctionScore.score);
		}
		meanPrecision /= r.numberOfDocuments;
		meanRecall /= r.numberOfDocuments;
		meanOFScore /= r.numberOfDocuments;
		return new Score(meanPrecision, meanRecall, meanOFScore);
	}

	public static Score mean(List<Score> scores) {

		double precision = 0;
		double recall = 0;
		double score = 0;
		for (Score s : scores) {
			precision += s.precision;
			recall += s.recall;
			score += s.score;
		}
		precision /= scores.size();
		recall /= scores.size();
		score /= scores.size();
		return new Score(precision, recall, score);
	}

	public static Comparator<Entry<String, Double>> featureWeightComparator = new Comparator<Map.Entry<String, Double>>() {
		@Override
		public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
			return (int) -Math.signum(o1.getValue() - o2.getValue());
		}
	};

	public static DecimalFormat featureWeightFormat = new DecimalFormat(
			"0.000000");

	/**
	 * Prints all weights of the learners model in descending order, discarding
	 * all weights with an absolute value smaller than minAbsValue. To print all
	 * values set minAbsValue <= 0
	 * 
	 * @param learner
	 * @param minAbsValue
	 */
	public static void printWeights(DefaultLearner learner, double minAbsValue) {
		Map<String, Double> allWeights = new HashMap<String, Double>();
		for (Template t : learner.getModel().getTemplates()) {
			Vector weights = t.getWeightVector();
			for (String f : weights.getFeatureNames()) {
				double value = weights.getValueOfFeature(f);
				if (minAbsValue <= 0 || Math.abs(value) >= minAbsValue)
					allWeights.put(f, value);
			}
		}

		printWeightsSorted(allWeights);
	}

	public static void printWeightsSorted(Map<String, Double> allWeights) {
		ArrayList<Entry<String, Double>> features = new ArrayList<Entry<String, Double>>(
				allWeights.entrySet());
		Collections.sort(features, featureWeightComparator);
		for (Entry<String, Double> e : features) {
			Log.d("%s: %s", featureWeightFormat.format(e.getValue()),
					e.getKey());
		}
	}

	public static DecimalFormat SCORE_FORMAT = new DecimalFormat("0.0000");

	public static void printScores(List<Score> scores) {
		for (Score score : scores) {
			Log.d("P=%s\t|\tR=%s\t|\tF1=%s",
					SCORE_FORMAT.format(score.precision),
					SCORE_FORMAT.format(score.recall),
					SCORE_FORMAT.format(score.score));
		}
	}

}
