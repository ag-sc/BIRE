package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Corpus.AnnotatedDocument;
import Corpus.Constants;
import Corpus.Corpus;
import Corpus.parser.brat.BioNLPLoader;
import Corpus.parser.usage.UsageLoader;
import Learning.Model;
import Learning.Score;
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

public class EvaluateParameters {

	private static final String BIONLP_EVAL_DIR_PATH = "res/bionlp/eval";
	private static final String BIONLP_MODELS_DIR_PATH = "res/bionlp/models";
	public static final int USAGE = 0;
	public static final int BIONLP = 1;
	private static final String RECORD_NAME_PATTERN = "%s_%s_NCrossVal-%s_(steps-%s_epochs-%s_initAlpha-%s_finalAlpha-%s_initOmega-%s_finalOmega_%s)";
	private static final String MODEL_NAME_PATTERN = "%s_NCrossVal-%s_(steps-%s_epochs-%s_initAlpha-%s_finalAlpha-%s_initOmega-%s_finalOmega_%s)";

	static class Params {
		int numberOfSamplingSteps;
		int numberOfEpochs;
		double initialAlpha;
		double finalAlpha;
		double initialOmega;
		double finalOmega;
		List<Sampler> samplers;

		public Params(int numberOfSamplingSteps, int numberOfEpochs, double initialAlpha, double finalAlpha,
				double initialOmega, double finalOmega, List<Sampler> samplers) {
			super();
			this.numberOfSamplingSteps = numberOfSamplingSteps;
			this.numberOfEpochs = numberOfEpochs;
			this.initialAlpha = initialAlpha;
			this.finalAlpha = finalAlpha;
			this.initialOmega = initialOmega;
			this.finalOmega = finalOmega;
			this.samplers = samplers;
		}

		@Override
		public String toString() {
			return "Params [numberOfSamplingSteps=" + numberOfSamplingSteps + ", numberOfEpochs=" + numberOfEpochs
					+ ", initialAlpha=" + initialAlpha + ", finalAlpha=" + finalAlpha + ", initialOmega=" + initialOmega
					+ ", finalOmega=" + finalOmega + "]";
		}

	}

	public static void main(String[] args) {
		// evaluate();
		visualizeAlphaOmegaGridSearch();
	}

	public static void evaluate() {
		File modelDir = null;
		File evalDir = null;
		Corpus corpus = null;

		int corpusID = BIONLP;
		switch (corpusID) {
		case USAGE:
			try {
				corpus = UsageLoader.loadDatasetFromBinaries(Constants.getUSAGEJavaBinFilepath());
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("Preparsed corpus not accessible or corrupted. Parse again:");
				corpus = UsageLoader.convertDatasetToJavaBinaries(Constants.getUSAGEJavaBinFilepath());
			}
			break;
		case BIONLP:
			corpus = BioNLPLoader.loadBioNLP2013Train();
			modelDir = new File(BIONLP_MODELS_DIR_PATH);
			if (!modelDir.exists())
				modelDir.mkdirs();
			evalDir = new File(BIONLP_EVAL_DIR_PATH);
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
		// allDocuments = allDocuments.subList(137, 140);

		alphaOmegaGridSearch(modelDir, evalDir, samplers, allDocuments);
	}

	private static void alphaOmegaGridSearch(File modelDir, File evalDir, List<Sampler> samplers,
			List<AnnotatedDocument> allDocuments) {
		int defaultStep = 10;
		int defaultEpoch = 5;

		List<Params> alpha = new ArrayList<>();
		alpha.add(new Params(defaultStep, defaultEpoch, 0.1, 0.01, 0, 0, samplers));
		alpha.add(new Params(defaultStep, defaultEpoch, 0.1, 0.1, 0, 0, samplers));
		alpha.add(new Params(defaultStep, defaultEpoch, 0.01, 0.01, 0, 0, samplers));

		List<Params> omega = new ArrayList<>();
		omega.add(new Params(defaultStep, defaultEpoch, 0.1, 0.01, 1, 1, samplers));
		omega.add(new Params(defaultStep, defaultEpoch, 0.1, 0.01, 1, 0, samplers));
		omega.add(new Params(defaultStep, defaultEpoch, 0.1, 0.01, 1, 0.5, samplers));
		omega.add(new Params(defaultStep, defaultEpoch, 0.1, 0.01, 0.5, 0, samplers));
		omega.add(new Params(defaultStep, defaultEpoch, 0.1, 0.01, 0, 0, samplers));

		// N-Fold cross validation
		int n = 5;
		long time = System.currentTimeMillis();
		evaluateParamConfigs(alpha, n, allDocuments, "alpha", modelDir, evalDir);
		evaluateParamConfigs(omega, n, allDocuments, "omega", modelDir, evalDir);
		// Log.d("Overall performance:");
		// EvaluationUtil.printPerformance(testRecords);
		Log.d("############################");
		Log.d("############################");
		Log.d("############# ALL DONE #############");
		Log.d("Total time: %s", String.valueOf((System.currentTimeMillis() - time)));

		Log.d("############################");
		TaggedTimer.printTimings();
	}

	private static void basicParamsGridSearch(File modelDir, File evalDir, List<Sampler> samplers,
			List<AnnotatedDocument> allDocuments) {
		int defaultStep = 10;
		int defaultEpoch = 5;
		List<Params> step = new ArrayList<>();
		step.add(new Params(5, defaultEpoch, 0.01, 0.001, 1, 0, samplers));
		step.add(new Params(10, defaultEpoch, 0.01, 0.001, 1, 0, samplers));
		step.add(new Params(15, defaultEpoch, 0.01, 0.001, 1, 0, samplers));

		List<Params> alpha = new ArrayList<>();
		alpha.add(new Params(defaultStep, defaultEpoch, 0.1, 0.01, 1, 0, samplers));
		alpha.add(new Params(defaultStep, defaultEpoch, 0.01, 0.001, 1, 0, samplers));
		alpha.add(new Params(defaultStep, defaultEpoch, 0.001, 0.0001, 1, 0, samplers));

		List<Params> epoch = new ArrayList<>();
		epoch.add(new Params(defaultStep, 1, 0.01, 0.001, 1, 0, samplers));
		epoch.add(new Params(defaultStep, 5, 0.01, 0.001, 1, 0, samplers));
		epoch.add(new Params(defaultStep, 10, 0.01, 0.001, 1, 0, samplers));

		List<Params> omega = new ArrayList<>();
		omega.add(new Params(defaultStep, defaultEpoch, 0.01, 0.001, 1, 0, samplers));
		omega.add(new Params(defaultStep, defaultEpoch, 0.01, 0.001, 1, 0.5, samplers));
		omega.add(new Params(defaultStep, defaultEpoch, 0.01, 0.001, 0, 0, samplers));

		// N-Fold cross validation
		int n = 5;
		long time = System.currentTimeMillis();
		evaluateParamConfigs(step, n, allDocuments, "step", modelDir, evalDir);
		evaluateParamConfigs(alpha, n, allDocuments, "alpha", modelDir, evalDir);
		evaluateParamConfigs(epoch, n, allDocuments, "epoch", modelDir, evalDir);
		evaluateParamConfigs(omega, n, allDocuments, "omega", modelDir, evalDir);
		// Log.d("Overall performance:");
		// EvaluationUtil.printPerformance(testRecords);
		Log.d("############################");
		Log.d("############################");
		Log.d("############# ALL DONE #############");
		Log.d("Total time: %s", String.valueOf((System.currentTimeMillis() - time)));
	}

	private static void evaluateParamConfigs(List<Params> paramsList, int nCrossValidation,
			List<AnnotatedDocument> documents, String descriptor, File modelDir, File evalDir) {
		Log.d("############################");
		Log.d("############################");
		Log.d("Evalutate param group: %s", descriptor);
		Random rand = new Random(0);
		long[] seeds = new long[nCrossValidation];

		for (int i = 0; i < nCrossValidation; i++) {
			seeds[i] = rand.nextLong();
		}
		for (Params params : paramsList) {
			Log.d("############################");
			Log.d("Evalutate param: %s", params);
			for (int i = 0; i < nCrossValidation; i++) {
				Log.d("############################");
				Log.d("############################");
				Log.d("Cross Validation: %s/%s", i + 1, nCrossValidation);
				DataSplit split = new DataSplit(documents, 0.8);
				List<AnnotatedDocument> train = split.getTrain();
				List<AnnotatedDocument> test = split.getTest();

				List<Template> templates = new ArrayList<Template>();
				templates.add(new RelationTemplate());
				templates.add(new MorphologicalTemplate());
				templates.add(new ContextTemplate());

				Model model = new Model(templates);

				DefaultLearner learner = new DefaultLearner(model, params.samplers, params.numberOfSamplingSteps,
						params.initialAlpha, params.finalAlpha, params.initialOmega, params.finalOmega);
				Log.d("Train/test split: %s => #train: %s, #test: %s", split.getSplit(), train.size(), test.size());

				Log.d("####################");
				Log.d("Start learning");
				learner.train(train, params.numberOfEpochs);
				try {
					model.saveModelToFile(new File(modelDir,
							EvaluationUtil.generateFilenameForModel(String.format(MODEL_NAME_PATTERN, descriptor, i,
									params.numberOfSamplingSteps, params.numberOfEpochs, params.initialAlpha,
									params.finalAlpha, params.initialOmega, params.finalOmega))).getPath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					EvaluationUtil.storeRecord(learner.getTrainRecord(), evalDir,
							String.format(RECORD_NAME_PATTERN, descriptor, "train", i, params.numberOfSamplingSteps,
									params.numberOfEpochs, params.initialAlpha, params.finalAlpha, params.initialOmega,
									params.finalOmega));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				learner.test(test, params.numberOfSamplingSteps);
				try {
					EvaluationUtil.storeRecord(learner.getTestRecord(), evalDir,
							String.format(RECORD_NAME_PATTERN, descriptor, "test", i, params.numberOfSamplingSteps,
									params.numberOfEpochs, params.initialAlpha, params.finalAlpha, params.initialOmega,
									params.finalOmega));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void visualizeAlphaOmegaGridSearch() {
		List<String>[] alpha = new List[3];
		alpha[0] = new ArrayList<>();
		alpha[1] = new ArrayList<>();
		alpha[2] = new ArrayList<>();

		alpha[0].add(
				"Record_alpha_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_19-17-23");
		alpha[0].add(
				"Record_alpha_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_19-30-38");
		alpha[0].add(
				"Record_alpha_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_19-43-28");

		alpha[1].add(
				"Record_alpha_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_17-59-32");
		alpha[1].add(
				"Record_alpha_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_18-12-29");
		alpha[1].add(
				"Record_alpha_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_18-25-20");

		alpha[2].add(
				"Record_alpha_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.1_initOmega-0.0_finalOmega_0.0)_2015-8-31_18-38-25");
		alpha[2].add(
				"Record_alpha_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.1_initOmega-0.0_finalOmega_0.0)_2015-8-31_18-51-15");
		alpha[2].add(
				"Record_alpha_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.1_initOmega-0.0_finalOmega_0.0)_2015-8-31_19-4-18");

		Log.d("# Compute alpha scores...");
		List<Score> alphaScores = computeMeanScores(alpha);

		List<String>[] omega = new List[5];
		omega[0] = new ArrayList<>();
		omega[1] = new ArrayList<>();
		omega[2] = new ArrayList<>();
		omega[3] = new ArrayList<>();
		omega[4] = new ArrayList<>();

		omega[0].add(
				"Record_omega_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_22-26-49");
		omega[0].add(
				"Record_omega_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_22-39-32");
		omega[0].add(
				"Record_omega_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.0_finalOmega_0.0)_2015-8-31_22-52-24");

		omega[1].add(
				"Record_omega_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.5_finalOmega_0.0)_2015-8-31_21-48-1");
		omega[1].add(
				"Record_omega_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.5_finalOmega_0.0)_2015-8-31_22-1-7");
		omega[1].add(
				"Record_omega_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-0.5_finalOmega_0.0)_2015-8-31_22-13-58");

		omega[2].add(
				"Record_omega_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.0)_2015-8-31_20-32-56");
		omega[2].add(
				"Record_omega_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.0)_2015-8-31_20-45-31");
		omega[2].add(
				"Record_omega_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.0)_2015-8-31_20-58-2");

		omega[3].add(
				"Record_omega_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.5)_2015-8-31_21-10-23");
		omega[3].add(
				"Record_omega_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.5)_2015-8-31_21-22-47");
		omega[3].add(
				"Record_omega_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.5)_2015-8-31_21-35-15");

		omega[4].add(
				"Record_omega_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_1.0)_2015-8-31_19-55-39");
		omega[4].add(
				"Record_omega_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_1.0)_2015-8-31_20-7-57");
		omega[4].add(
				"Record_omega_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_1.0)_2015-8-31_20-20-28");

		Log.d("# Compute omega scores...");
		List<Score> omegaScores = computeMeanScores(omega);

		Log.d("#################");
		Log.d("##### Alpha #####");
		Log.d("0.01->0.01, 0.1->0.01, 0.1->0.1");
		EvaluationUtil.printScores(alphaScores);

		Log.d("#################");
		Log.d("##### Omega #####");
		Log.d("0->0, 0.5->0, 1->0, 1->0.5, 1->1");
		EvaluationUtil.printScores(omegaScores);
	}

	public static void visualizeBasicParamGridSearch() {
		List<String>[] alpha = new List[3];
		alpha[0] = new ArrayList<>();
		alpha[1] = new ArrayList<>();
		alpha[2] = new ArrayList<>();

		alpha[0].add(
				"Record_alpha_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.001_finalAlpha-1.0E-4_initOmega-1.0_finalOmega_0.0)_2015-8-8_10-15-23");
		alpha[0].add(
				"Record_alpha_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.001_finalAlpha-1.0E-4_initOmega-1.0_finalOmega_0.0)_2015-8-8_11-21-6");
		alpha[0].add(
				"Record_alpha_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.001_finalAlpha-1.0E-4_initOmega-1.0_finalOmega_0.0)_2015-8-8_12-26-35");

		alpha[1].add(
				"Record_alpha_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_7-4-30");
		alpha[1].add(
				"Record_alpha_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_8-11-5");
		alpha[1].add(
				"Record_alpha_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_9-12-7");

		alpha[2].add(
				"Record_alpha_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.0)_2015-8-8_3-57-13");
		alpha[2].add(
				"Record_alpha_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.0)_2015-8-8_4-58-29");
		alpha[2].add(
				"Record_alpha_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.1_finalAlpha-0.01_initOmega-1.0_finalOmega_0.0)_2015-8-8_6-2-36");

		Log.d("# Compute alpha scores...");
		List<Score> alphaScores = computeMeanScores(alpha);

		List<String>[] epoch = new List[3];
		epoch[0] = new ArrayList<>();
		epoch[1] = new ArrayList<>();
		epoch[2] = new ArrayList<>();
		epoch[0].add(
				"Record_epoch_test_NCrossVal-0_(steps-10_epochs-1_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_12-41-0");
		epoch[0].add(
				"Record_epoch_test_NCrossVal-1_(steps-10_epochs-1_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_12-55-50");
		epoch[0].add(
				"Record_epoch_test_NCrossVal-2_(steps-10_epochs-1_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_13-10-16");

		epoch[1].add(
				"Record_epoch_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_14-14-15");
		epoch[1].add(
				"Record_epoch_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_15-17-29");
		epoch[1].add(
				"Record_epoch_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_16-23-32");

		epoch[2].add(
				"Record_epoch_test_NCrossVal-0_(steps-10_epochs-10_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_18-29-29");
		epoch[2].add(
				"Record_epoch_test_NCrossVal-1_(steps-10_epochs-10_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_20-32-16");
		epoch[2].add(
				"Record_epoch_test_NCrossVal-2_(steps-10_epochs-10_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_22-35-26");

		Log.d("# Compute epoch scores...");
		List<Score> epochScores = computeMeanScores(epoch);

		List<String>[] omega = new List[3];
		omega[0] = new ArrayList<>();
		omega[1] = new ArrayList<>();
		omega[2] = new ArrayList<>();
		omega[0].add(
				"Record_omega_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-0.0_finalOmega_0.0)_2015-8-9_6-5-2");
		omega[0].add(
				"Record_omega_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-0.0_finalOmega_0.0)_2015-8-9_7-12-45");
		omega[0].add(
				"Record_omega_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-0.0_finalOmega_0.0)_2015-8-9_8-20-3");

		omega[1].add(
				"Record_omega_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_23-41-38");
		omega[1].add(
				"Record_omega_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-9_0-45-5");
		omega[1].add(
				"Record_omega_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-9_1-49-45");

		omega[2].add(
				"Record_omega_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.5)_2015-8-9_2-54-54");
		omega[2].add(
				"Record_omega_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.5)_2015-8-9_3-54-55");
		omega[2].add(
				"Record_omega_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.5)_2015-8-9_4-56-51");

		Log.d("# Compute omega scores...");
		List<Score> omegaScores = computeMeanScores(omega);

		List<String>[] step = new List[3];
		step[0] = new ArrayList<>();
		step[1] = new ArrayList<>();
		step[2] = new ArrayList<>();
		step[0].add(
				"Record_step_test_NCrossVal-0_(steps-5_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-7_17-40-41");
		step[0].add(
				"Record_step_test_NCrossVal-1_(steps-5_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-7_18-4-48");
		step[0].add(
				"Record_step_test_NCrossVal-2_(steps-5_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-7_18-28-30");

		step[1].add(
				"Record_step_test_NCrossVal-0_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-7_19-33-45");
		step[1].add(
				"Record_step_test_NCrossVal-1_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-7_20-36-51");
		step[1].add(
				"Record_step_test_NCrossVal-2_(steps-10_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-7_21-38-17");

		step[2].add(
				"Record_step_test_NCrossVal-0_(steps-15_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-7_23-24-40");
		step[2].add(
				"Record_step_test_NCrossVal-1_(steps-15_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_1-9-11");
		step[2].add(
				"Record_step_test_NCrossVal-2_(steps-15_epochs-5_initAlpha-0.01_finalAlpha-0.001_initOmega-1.0_finalOmega_0.0)_2015-8-8_2-52-7");

		Log.d("# Compute step scores...");
		List<Score> stepScores = computeMeanScores(step);

		Log.d("#################");
		Log.d("##### Alpha #####");
		Log.d("0.001->0.0001, 0.01->0.001, 0.1->0.01");
		EvaluationUtil.printScores(alphaScores);

		Log.d("#################");
		Log.d("##### Epoch #####");
		Log.d("1, 5, 10");
		EvaluationUtil.printScores(epochScores);

		Log.d("#################");
		Log.d("##### Omega #####");
		Log.d("0->0, 1->0, 1->0.5");
		EvaluationUtil.printScores(omegaScores);

		Log.d("################");
		Log.d("##### Step #####");
		Log.d("5, 10, 15");
		EvaluationUtil.printScores(stepScores);
	}

	private static List<Score> computeMeanScores(List<String>[] paramFilenames) {
		List<Score> paramScores = new ArrayList<>();
		for (List<String> crossValNames : paramFilenames) {
			List<SamplingProcedureRecord> crossValRecords = new ArrayList<>();

			Log.d("load cross validation files for param config...");
			for (String s : crossValNames) {
				try {
					crossValRecords.add(EvaluationUtil.loadRecord(new File(BIONLP_EVAL_DIR_PATH, s).getPath()));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Log.d("compute mean for individual model...");
			List<Score> crossValScores = new ArrayList<>();
			for (SamplingProcedureRecord r : crossValRecords) {
				crossValScores.add(EvaluationUtil.mean(r));
			}

			Log.d("compute mean over cross validations...");
			paramScores.add(EvaluationUtil.mean(crossValScores));
		}
		return paramScores;
	}
}
