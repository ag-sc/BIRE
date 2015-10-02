package Learning.callbacks;

import Corpus.Document;
import Learning.Learner;
import Sampling.Sampler;

public interface ISamplerCallback {

	default void onStartSampler(Learner<?> learner, Sampler<?> sampler, int indexOfSampler, int numberOfSamplers,
			int step, int numberOfSteps, Document document, int indexOfDocument, int numberOfDocuments, int epoch,
			int numberOfEpochs) {
	}

	default void onEndSampler(Learner<?> learner, Sampler<?> sampler, int indexOfSampler, int numberOfSamplers,
			int step, int numberOfSteps, Document document, int indexOfDocument, int numberOfDocuments, int epoch,
			int numberOfEpochs) {
	}
}
