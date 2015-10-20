package learning;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.AnnotatedDocument;
import sampling.Sampler;
import variables.AbstractState;

public class Trainer<StateT extends AbstractState> {

	private static Logger log = LogManager.getFormatterLogger(Trainer.class.getName());

	enum LearningProcedure {
		SAMPLE_RANK, PERCEPTRON;
	}

	private LearningProcedure learningProcedure = LearningProcedure.SAMPLE_RANK;
	private double initialAlpha;
	private double finalAlpha;
	private double initialOmega;
	private double finalOmega;

	private Model<StateT> model;
	private Scorer<StateT> scorer;
	private Sampler<StateT> sampler;

	public Trainer(Model<StateT> model, Scorer<StateT> scorer, Sampler<StateT> sampler) {
		super();
		this.model = model;
		this.scorer = scorer;
		this.sampler = sampler;
	}

	public List<StateT> train(Learner<StateT> learner, List<? extends AnnotatedDocument<StateT>> documents,
			int numberOfEpochs, int steps) {
		List<StateT> finalStates = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		/**
		 * This variable represents the probability for the learner to select
		 * the next state for the sampling procedure according to the (best)
		 * objective function score (in contrast to the (best) model score).
		 * This probability decreases during the training, so that the learner,
		 * in the end, favors the decisions of the model.
		 */
		double omegaStep = (initialOmega - finalOmega) / (steps * documents.size() * numberOfEpochs - 1);

		double alphaStep = (initialAlpha - finalAlpha) / (steps * documents.size() * numberOfEpochs - 1);
		log.info("#Epochs=%s, #Documents=%s, #Steps=%s", numberOfEpochs, documents.size(), steps);
		log.info("iO=%s, fO=%s, Os=%s; iA=%s, fA=%s, As=%s", initialOmega, finalOmega, omegaStep, initialAlpha,
				finalAlpha, alphaStep);
		for (int e = 0; e < numberOfEpochs; e++) {
			log.info("##############################");
			log.info("Epoch: %s/%s", e + 1, numberOfEpochs);
			log.info("##############################");
			for (int d = 0; d < documents.size(); d++) {
				AnnotatedDocument<StateT> document = documents.get(d);
				log.info("===========================");
				log.info("Epoch: %s/%s; Document: %s/%s", e + 1, numberOfEpochs, d + 1, documents.size());
				log.info("Content   : %s", document.getContent());
				log.info("Gold State: %s", document.getGoldState());
				log.info("===========================");

				List<StateT> generatedChain = sampler.generateChain(document, learner, steps);
				StateT finalState = generatedChain.get(generatedChain.size() - 1);
				long stopTime = System.currentTimeMillis();

				log.info("++++++++++++++++");
				log.info("Gold State:   %s", document.getGoldState());
				log.info("Final State:  %s", finalState);
				log.info("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
				log.info("++++++++++++++++");

				/*
				 * Store the final predicted state for the current document if
				 * the current epoch is the final one.
				 */
				if (e == numberOfEpochs - 1) {
					finalStates.add(finalState);
				}
				log.info("===========================");
			}
			log.info("##############################");
		}
		return finalStates;
	}

	public List<StateT> test(List<? extends AnnotatedDocument<StateT>> documents, int steps) {
		List<StateT> finalStates = new ArrayList<>();
		for (int d = 0; d < documents.size(); d++) {
			AnnotatedDocument<StateT> document = documents.get(d);
			List<StateT> generatedChain = sampler.generateChain(document, steps);
			StateT finalState = generatedChain.get(generatedChain.size() - 1);
			finalStates.add(finalState);
		}
		return finalStates;
	}

	public Model<StateT> getModel() {
		return model;
	}

	public Scorer<StateT> getScorer() {
		return scorer;
	}

}
