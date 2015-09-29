package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Corpus.BioNLPCorpus;
import Corpus.SubDocument;
import Corpus.parser.brat.BioNLPLoader;
import Learning.Model;
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
import Variables.State;

public class BioNLPLearning {

	public static void main(String[] args) {
		// int trainSize = 190;
		int trainSize = 0;
		int testSize = 0;
		if (args != null && args.length == 2) {
			trainSize = Integer.parseInt(args[0]);
			testSize = Integer.parseInt(args[1]);
		}
		BioNLPCorpus trainCorpus = BioNLPLoader.loadBioNLP2013Train(false);
		BioNLPCorpus devCorpus = BioNLPLoader.loadBioNLP2013Dev(false);

		Log.d("Train corpus config: %s", trainCorpus.getCorpusConfig());
		Log.d("Dev corpus config: %s", devCorpus.getCorpusConfig());

		File modelDir = new File("res/bionlp/models");
		File evalDir = new File("res/bionlp/eval");
		File outputDir = new File("res/bionlp/gen");

		if (!modelDir.exists())
			modelDir.mkdirs();

		if (!evalDir.exists())
			evalDir.mkdirs();

		if (!outputDir.exists())
			outputDir.mkdirs();

		List<Sampler> samplers = new ArrayList<Sampler>();
		samplers.add(new ExhaustiveEntitySampler());
		samplers.add(new ExhaustiveBoundarySampler());
		samplers.add(new RelationSampler(20));

		// templates.add(new CheatingTemplate());

		// Leave-one-out

		// List<SubDocument> allDocuments = trainCorpus.getDocuments();
		// for (int i = 0; i < allDocuments.size(); i++) {
		// SubDocument doc = allDocuments.get(i);
		// Log.d("%s: %s", i, doc.getGoldState().toDetailedString());
		// }
		// Collections.shuffle(allDocuments, new Random(0));
		// allDocuments = allDocuments.subList(0, 10);

		List<SamplingProcedureRecord> trainRecords = new ArrayList<SamplingProcedureRecord>();
		List<SamplingProcedureRecord> testRecords = new ArrayList<SamplingProcedureRecord>();

		int numberOfSamplingSteps = 10;
		int numberOfEpochs = 5;
		// N-Fold cross validation
		int n = 1;
		// long[] seeds = { 1234, 2345, 3456 };
		for (int i = 0; i < n; i++) {
			Log.d("############################");
			Log.d("############################");
			Log.d("Cross Validation: %s/%s", i + 1, n);
			// DataSplit<SubDocument> split = new DataSplit<>(allDocuments, 0.8,
			// seeds[i]);
			// List<SubDocument> train = split.getTrain();
			// List<SubDocument> test = split.getTest();
			List<SubDocument> train = null;
			List<SubDocument> test = null;
			if (trainSize > 0) {
				trainSize = Math.min(trainSize, trainCorpus.getParentDocuments().size());
				train = trainCorpus.getSubDocuments(trainCorpus.getParentDocuments().subList(0, trainSize));
			} else {
				train = trainCorpus.getDocuments();
			}
			if (testSize > 0) {
				testSize = Math.min(testSize, devCorpus.getParentDocuments().size());
				test = devCorpus.getSubDocuments(devCorpus.getParentDocuments().subList(0, testSize));
			} else {
				test = devCorpus.getDocuments();
			}

			// Log.d("train:");
			// train.forEach(d -> Log.d("%s: %s", d.getName(),
			// d.getGoldState()));
			// Log.d("test:");
			// test.forEach(d -> Log.d("%s: %s", d.getName(),
			// d.getGoldState()));

			List<Template> templates = new ArrayList<Template>();
			templates.add(new RelationTemplate());
			templates.add(new MorphologicalTemplate());
			templates.add(new ContextTemplate());
			Model model = new Model(templates);

			long startTime = System.currentTimeMillis();
			DefaultLearner learner = new DefaultLearner(model, samplers, numberOfSamplingSteps, 0.01, 0.01, 0, 0);

			/*
			 * Pause the learner after every few documents to display additional
			 * information
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
			Log.d("Train/test: => #train: %s, #test: %s", train.size(), test.size());

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
			List<State> predictions = learner.test(test, numberOfSamplingSteps);
			Set<File> files = BioNLPEvaluation.statesToBioNLPFiles(outputDir, predictions, true);
			Log.d("Produced annotaion files: %s", files);

			testRecords.add(learner.getTestRecord());
			// SamplingProcedureRecord testRecord = learner.getTestRecord();
			// Plots.plotScore(trainRecord);
			// plotScore(testRecord);
			// Log.d("Learned weights of cross-validation %s/%s:", i, n);
			// EvaluationUtil.printWeights(learner, 10e-7);
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
