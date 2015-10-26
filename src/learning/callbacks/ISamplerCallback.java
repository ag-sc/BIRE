package learning.callbacks;

import corpus.Document;
import sampling.AbstractSampler;
import variables.AbstractState;

public interface ISamplerCallback<T> {

	default void onStartSampler(T caller, AbstractSampler<?> sampler, int indexOfSampler, int numberOfSamplers,
			int step, int numberOfSteps, Document document, int indexOfDocument, int numberOfDocuments, int epoch,
			int numberOfEpochs) {
	}

	default void onEndSampler(T caller, AbstractSampler<?> sampler, int indexOfSampler, int numberOfSamplers,
			int step, int numberOfSteps, Document document, int indexOfDocument, int numberOfDocuments, int epoch,
			int numberOfEpochs, AbstractState nextState) {
	}
}
