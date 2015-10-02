package Learning.callbacks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Corpus.Document;
import Learning.Learner;
import Logging.Log;
import Sampling.Sampler;

public class SimpleLogCallback implements IDocumentCallback, IEpochCallback, ISamplerCallback, IStepCallback {
	private static Logger log = LogManager.getFormatterLogger();

	public void onStartDocument(Learner<?> learner, Document document, int indexOfDocument, int numberOfDocuments,
			int epoch, int numberOfEpochs) {
		log.info(
				"onStartDocument(document: %s, indexOfDocument: %s, numberOfDocuments: %s, epoch: %s, numberOfEpochs: %s)",
				document.getName(), indexOfDocument, numberOfDocuments, epoch, numberOfEpochs);
	}

	public void onEndDocument(Learner<?> learner, Document document, int indexOfDocument, int numberOfDocuments,
			int epoch, int numberOfEpochs) {
		log.info(
				"onEndDocument(document: %s, indexOfDocument: %s, numberOfDocuments: %s, epoch: %s, numberOfEpochs: %s)",
				document.getName(), indexOfDocument, numberOfDocuments, epoch, numberOfEpochs);
	}

	public void onStartEpoch(Learner<?> learner, int epoch, int numberOfEpochs) {
		log.info("onStartEpoch(epoch: %s, numberOfEpochs: %s)", epoch, numberOfEpochs);
	}

	public void onEndEpoch(Learner<?> learner, int epoch, int numberOfEpochs) {
		log.info("onEndEpoch(epoch: %s, numberOfEpochs: %s)", epoch, numberOfEpochs);
	}

	public void onStartSampler(Learner<?> learner, Sampler<?> sampler, int indexOfSampler, int numberOfSamplers,
			int step, int numberOfSteps, Document document, int indexOfDocument, int numberOfDocuments, int epoch,
			int numberOfEpochs) {
		log.info(
				"onStartDocument(sampler: %s, indexOfSampler: %s, document: %s, numberOfSamplers: %s, step: %s, numberOfSteps: %s, indexOfDocument: %s, numberOfDocuments: %s, epoch: %s, numberOfEpochs: %s)",
				sampler.getClass().getName(), indexOfSampler, numberOfSamplers, step, numberOfSteps, document.getName(),
				indexOfDocument, numberOfDocuments, epoch, numberOfEpochs);
	}

	public void onEndSampler(Learner<?> learner, Sampler<?> sampler, int indexOfSampler, int numberOfSamplers, int step,
			int numberOfSteps, Document document, int indexOfDocument, int numberOfDocuments, int epoch,
			int numberOfEpochs) {
		log.info(
				"onEndSampler(sampler: %s, indexOfSampler: %s, document: %s, numberOfSamplers: %s, step: %s, numberOfSteps: %s, indexOfDocument: %s, numberOfDocuments: %s, epoch: %s, numberOfEpochs: %s)",
				sampler.getClass().getName(), indexOfSampler, numberOfSamplers, step, numberOfSteps, document.getName(),
				indexOfDocument, numberOfDocuments, epoch, numberOfEpochs);
	}

	public void onStartStep(Learner<?> learner, int step, int numberOfSteps, Document document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
		log.info(
				"onStartStep(step: %s, numberOfSteps: %s, indexOfDocument: %s, numberOfDocuments: %s, epoch: %s, numberOfEpochs: %s)",
				step, numberOfSteps, document.getName(), indexOfDocument, numberOfDocuments, epoch, numberOfEpochs);
	}

	public void onEndStep(Learner<?> learner, int step, int numberOfSteps, Document document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
		log.info(
				"onEndStep(step: %s, numberOfSteps: %s, indexOfDocument: %s, numberOfDocuments: %s, epoch: %s, numberOfEpochs: %s)",
				step, numberOfSteps, document.getName(), indexOfDocument, numberOfDocuments, epoch, numberOfEpochs);
	}

}
