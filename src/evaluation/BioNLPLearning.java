package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Corpus.AnnotatedDocument;
import Corpus.Constants;
import Corpus.Corpus;
import Corpus.Document;
import Corpus.parser.brat.BioNLPLoader;
import Corpus.parser.usage.UsageLoader;
import Learning.Learner;
import Learning.Model;
import Learning.callbacks.DocumentCallback;
import Learning.learner.DefaultLearner;
import Logging.Log;
import Sampling.ExhaustiveBoundarySampler;
import Sampling.ExhaustiveEntitySampler;
import Sampling.RelationSampler;
import Sampling.Sampler;
import Templates.ContextTemplate;
import Templates.MorphologicalTemplate;
import Templates.RelationTemplate;
import Templates.Template;

public class BioNLPLearning {

	public static final int USAGE = 0;
	public static final int BIONLP = 1;

	public static void main(String[] args) {
		File modelDir = null;
		File evalDir = null;
		Corpus corpus = null;

		int corpusID = BIONLP;
		switch (corpusID) {
		case USAGE:
			try {
				corpus = UsageLoader.loadDatasetFromBinaries(Constants.JAVA_BIN_USAGE_CORPUS_FILEPATH);
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("Preparsed corpus not accessible or corrupted. Parse again:");
				corpus = UsageLoader.convertDatasetToJavaBinaries(Constants.JAVA_BIN_USAGE_CORPUS_FILEPATH);
			}
			break;
		case BIONLP:
			try {
				corpus = BioNLPLoader.loadDatasetFromBinaries(Constants.JAVA_BIN_BIONLP_CORPUS_FILEPATH);
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("Preparsed corpus not accessible or corrupted. Parse again:");
				corpus = BioNLPLoader.convertDatasetToJavaBinaries(Constants.JAVA_BIN_BIONLP_CORPUS_FILEPATH);
			}
			modelDir = new File("res/bionlp/models");
			if (!modelDir.exists())
				modelDir.mkdirs();
			evalDir = new File("res/bionlp/eval");
			if (!evalDir.exists())
				evalDir.mkdirs();
			break;
		default:
			break;
		}

		Log.d("Corpus:\n%s", corpus);

		List<Sampler> samplers = new ArrayList<Sampler>();
		samplers.add(new ExhaustiveEntitySampler());
		samplers.add(new ExhaustiveBoundarySampler());
		samplers.add(new RelationSampler(20));

		// templates.add(new CheatingTemplate());

		// Leave-one-out

		List<AnnotatedDocument> allDocuments = corpus.getDocuments();
		for (int i = 0; i < allDocuments.size(); i++) {
			AnnotatedDocument doc = allDocuments.get(i);
			Log.d("%s: %s", i, doc.getGoldState());
		}
		// allDocuments = allDocuments.subList(137, 147);

		List<SamplingProcedureRecord> trainRecords = new ArrayList<SamplingProcedureRecord>();
		List<SamplingProcedureRecord> testRecords = new ArrayList<SamplingProcedureRecord>();

		int numberOfSamplingSteps = 10;
		int numberOfEpochs = 5;
		boolean leaveOneOut = false;
		if (leaveOneOut) {
			// Leave-One-Out evaluation
			for (int k = 0; k < allDocuments.size(); k++) {
				AnnotatedDocument document = allDocuments.get(k);
				Log.d("Leave-One-Out: %s/%s", k + 1, allDocuments.size());
				List<AnnotatedDocument> train = new ArrayList<AnnotatedDocument>(allDocuments);
				train.remove(document);
				List<AnnotatedDocument> test = Arrays.asList(document);

				List<Template> templates = new ArrayList<Template>();
				templates.add(new RelationTemplate());
				templates.add(new MorphologicalTemplate());
				templates.add(new ContextTemplate());
				Model model = new Model(templates);
				DefaultLearner learner = new DefaultLearner(model, samplers, numberOfSamplingSteps, 0.1, 0.01, 1, 0.5);
				// DataSplit split = new DataSplit(corpus, 0.7, 1);
				// Log.d("Train/test split:\n\tsplit: %s => #train: %s, #test:
				// %s",
				// split.getSplit(), split.getTrain().size(), split.getTest()
				// .size());
				Log.d("####################");
				Log.d("####################");
				Log.d("Start learning");
				learner.train(train, numberOfEpochs);
				try {
					model.saveModelToFile(
							new File(modelDir, EvaluationUtil.generateFilenameForModel(train.size())).getPath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				trainRecords.add(learner.getTrainRecord());
				// learner.train(split.getTrain().subList(0, 5));
				Log.d("###############");
				Log.d("Trained Model:\n%s", model.toDetailedString());
				Log.d("###############");
				learner.test(test, 10);
				// learner.test(split.getTest().subList(0, 2), 10);

				testRecords.add(learner.getTestRecord());
				// SamplingProcedureRecord testRecord = learner.getTestRecord();
				// Plots.plotScore(trainRecord);
				// plotScore(testRecord);

				Log.d("Learned weights of leave-one-out %s/%s:", k, allDocuments.size());
				EvaluationUtil.printWeights(learner, 10e-7);
			}
		} else {
			// N-Fold cross validation
			int n = 3;
			long[] seeds = { 1234, 2345, 3456 };
			for (int i = 0; i < n; i++) {
				Log.d("############################");
				Log.d("############################");
				Log.d("Cross Validation: %s/%s", i + 1, n);
				DataSplit split = new DataSplit(allDocuments, 0.8, seeds[i]);
				List<AnnotatedDocument> train = split.getTrain();
				List<AnnotatedDocument> test = split.getTest();

				List<Template> templates = new ArrayList<Template>();
				templates.add(new RelationTemplate());
				templates.add(new MorphologicalTemplate());
				templates.add(new ContextTemplate());
				Model model = new Model(templates);

				long startTime = System.currentTimeMillis();
				DefaultLearner learner = new DefaultLearner(model, samplers, numberOfSamplingSteps, 0.01, 0.01, 0, 0);

				/*
				 * Pause the learner after every few documents to display
				 * additional information
				 */
				// learner.setDocumentCallback(new DocumentCallback() {
				// @Override
				// public void onEndDocument(Learner learner, Document document,
				// int indexOfDocument,
				// int numberOfDocuments) {
				// if (indexOfDocument % 31 == 30) {
				// TaggedTimer.printTimings();
				// Log.d("current trainingTime: ~ %s (%s seconds)",
				// (System.currentTimeMillis() - startTime),
				// (System.currentTimeMillis() - startTime) / 1000);
				// try {
				// Log.d("PAUSE: Wait for ENTER...");
				// System.in.read();
				// } catch (IOException e1) {
				// e1.printStackTrace();
				// }
				// }
				// }
				// });
				Log.d("Train/test split: %s => #train: %s, #test: %s", split.getSplit(), train.size(), test.size());

				Log.d("####################");
				Log.d("Start learning");
				learner.train(train, numberOfEpochs);
				trainRecords.add(learner.getTrainRecord());
				// learner.train(split.getTrain().subList(0, 5));
				Log.d("###############");
				Log.d("Trained Model:\n%s", model.toDetailedString());
				Log.d("###############");
				try {
					model.saveModelToFile(
							new File(modelDir, EvaluationUtil.generateFilenameForModel(train.size())).getPath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				learner.test(test, numberOfSamplingSteps);
				// learner.test(split.getTest().subList(0, 2), 10);

				testRecords.add(learner.getTestRecord());
				// SamplingProcedureRecord testRecord = learner.getTestRecord();
				// Plots.plotScore(trainRecord);
				// plotScore(testRecord);
				// Log.d("Learned weights of cross-validation %s/%s:", i, n);
				// EvaluationUtil.printWeights(learner, 10e-7);
			}
		}

		try {
			EvaluationUtil.storeRecords(testRecords, evalDir);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			EvaluationUtil.storeRecords(trainRecords, evalDir);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("Overall performance:");
		EvaluationUtil.printPerformance(testRecords);
		TaggedTimer.printTimings();
	}
}
