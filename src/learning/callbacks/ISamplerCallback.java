package learning.callbacks;

import corpus.Document;
import sampling.Sampler;
import variables.AbstractState;

public interface ISamplerCallback<T> {

	default void onStartSampler(T caller, Sampler<?> sampler, int indexOfSampler, int numberOfSamplers,
			int step, int numberOfSteps, Document document, int indexOfDocument, int numberOfDocuments, int epoch,
			int numberOfEpochs) {
	}

	default void onEndSampler(T caller, Sampler<?> sampler, int indexOfSampler, int numberOfSamplers,
			int step, int numberOfSteps, Document document, int indexOfDocument, int numberOfDocuments, int epoch,
			int numberOfEpochs, AbstractState nextState) {
	}
}
