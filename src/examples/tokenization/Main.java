package examples.tokenization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.FileUtils;
import evaluation.DataSplit;
import evaluation.EvaluationUtil;
import learning.AdvancedLearner;
import learning.DefaultLearner;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.Trainer;
import learning.optimizer.Adam;
import learning.optimizer.SGD;
import learning.regularizer.L2;
import learning.scorer.DefaultScorer;
import learning.scorer.Scorer;
import sampling.DefaultSampler;
import sampling.Explorer;
import sampling.stoppingcriterion.StepLimitCriterion;
import sampling.stoppingcriterion.StoppingCriterion;
import templates.AbstractTemplate;

public class Main {
	private static Logger log = LogManager.getFormatterLogger();

	/**
	 * This is a small example that shows, how to use the BIRE framework for the
	 * tokenization of sentences. While the system is theoretically able to do
	 * tokenization, keep in mind that this is just an example and that there
	 * are more efficient and accurate approaches for this specific task.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		/*
		 * Load training and test data.
		 */
		List<TokenizedSentence> sentences = getTokenizedSentences();
		DataSplit<TokenizedSentence> dataSplit = new DataSplit<>(sentences, 0.7, 1);
		List<TokenizedSentence> train = dataSplit.getTrain();
		List<TokenizedSentence> test = dataSplit.getTest();
		// List<Sentence> predict = getSentences();

		train.forEach(s -> log.debug("%s", s));
		test.forEach(s -> log.debug("%s", s));
		/*
		 * Setup all necessary components for training and testing.
		 */
		/*
		 * Define an objective function that guides the training procedure.
		 */
		ObjectiveFunction<TokenState, Tokenization> objective = new TokenizationObjectiveFunction();

		/*
		 * Define templates that are responsible to generate factors/features to
		 * score intermediate, generated states.
		 */
		List<AbstractTemplate<Sentence, TokenState, ?>> templates = new ArrayList<>();
		templates.add(new TokenizationTemplate());

		/*
		 * Create the scorer object that computes a score from the factors'
		 * features and the templates' weight vectors.
		 */
		// Scorer<TokenState> scorer = new SoftplusScorer<>();
		// Scorer scorer = new LinearScorer();
		Scorer scorer = new DefaultScorer();
		/*
		 * Define a model and provide it with the necessary templates.
		 */
		Model<Sentence, TokenState> model = new Model<>(scorer, templates);
		model.setMultiThreaded(true);
		model.setForceFactorComputation(false);
		model.setSequentialScoring(false);

		/*
		 * Create an Initializer that is responsible for providing an initial
		 * state for the sampling chain given a sentence.
		 */
		TokenizationInitializer initializer = new TokenizationInitializer();

		/*
		 * Define the explorers that will provide "neighboring" states given a
		 * starting state. The sampler will select one of these states as a
		 * successor state and, thus, perform the sampling procedure.
		 */
		List<Explorer<TokenState>> explorers = new ArrayList<>();
		explorers.add(new TokenBoundaryExplorer());
		/*
		 * Create a sampler that generates sampling chains with which it will
		 * trigger weight updates during training.
		 */

		/*
		 * If you set this value too small, the sampler can not reach the
		 * optimal solution. Large values, however, increase computation time.
		 */
		int numberOfSamplingSteps = 50;
		StoppingCriterion<TokenState> stoppingCriterion = new StepLimitCriterion<>(numberOfSamplingSteps);
		DefaultSampler<Sentence, TokenState, Tokenization> sampler = new DefaultSampler<>(model, objective, explorers,
				stoppingCriterion);
		// sampler.setTrainingSamplingStrategy(SamplingStrategies.greedyObjectiveStrategy());
		/*
		 * Define a learning strategy. The learner will receive state pairs
		 * which can be used to update the models parameters.
		 */
		// Learner<TokenState> learner = new DefaultLearner<>(model, 0.1);
		// Learner<TokenState> learner = new AdvancedLearner<>(model, new
		// SGD());
		// Learner<TokenState> learner = new AdvancedLearner<>(model, new
		// SGD(0.1, 0.0, 0, false), new L2(0.0));
		Learner<TokenState> learner = new AdvancedLearner<>(model, new Adam());

		log.info("####################");
		log.info("Start training");

		/*
		 * The trainer will loop over the data and invoke sampling and learning.
		 * Additionally, it can invoke predictions on new data.
		 */
		int numberOfEpochs = 1;
		Trainer trainer = new Trainer();
		trainer.train(sampler, initializer, learner, train, numberOfEpochs);

		model.setSequentialScoring(true);
		List<TokenState> trainingResults = trainer.test(sampler, initializer, train);
		List<TokenState> testResults = trainer.test(sampler, initializer, test);

		/*
		 * Since the test function does not compute the objective score of its
		 * predictions, we do that here, manually, before we print the results.
		 */
		for (TokenState state : trainingResults) {
			Tokenization goldResult = ((TokenizedSentence) state.getInstance()).getGoldResult();
			double s = objective.score(state, goldResult);
		}
		for (TokenState state : testResults) {
			Tokenization goldResult = ((TokenizedSentence) state.getInstance()).getGoldResult();
			double s = objective.score(state, goldResult);
		}
		/*
		 * Now, that the predicted states have there objective score computed
		 * and set to their internal variable, we can print the prediction
		 * outcome.
		 */
		log.info("Training results:");
		EvaluationUtil.printPredictionPerformance(trainingResults);
		log.info("Test results:");
		EvaluationUtil.printPredictionPerformance(testResults);
		/*
		 * Finally, print the models weights.
		 */
		log.debug("Model weights:");
		EvaluationUtil.printWeights(model, -1);
	}

	private static List<Sentence> getSentences() throws IOException {
		List<String> lines = FileUtils.readLines("res/examples/tokenization/test-sentences.txt");
		List<Sentence> sentences = new ArrayList<>();
		for (String line : lines) {
			Sentence sentence = new Sentence(line);
			sentences.add(sentence);
		}
		return sentences;
	}

	private static List<TokenizedSentence> getTokenizedSentences() throws IOException {
		/*
		 * Read a list of sentences from a file and tokenize them with a regular
		 * expression to create our training data.
		 */
		List<String> lines = FileUtils.readLines("res/examples/tokenization/training-sentences.txt");
		List<TokenizedSentence> tokenizedSentences = new ArrayList<>();
		Pattern p = Pattern.compile("\\w+|\\s+");
		for (String line : lines) {
			TokenizedSentence tokenizedSentence = new TokenizedSentence(line);
			Tokenization tokenization = new Tokenization();
			Matcher m = p.matcher(line);
			while (m.find()) {
				int from = m.start();
				int to = m.end();
				tokenization.tokenBoundaries.put(from, new BoundaryVariable(from));
				tokenization.tokenBoundaries.put(to, new BoundaryVariable(to));
			}
			tokenizedSentence.setTokenization(tokenization);
			tokenizedSentences.add(tokenizedSentence);
		}
		return tokenizedSentences;
	}
}
