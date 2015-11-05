package learning.callbacks;

import corpus.Document;
import sampling.AbstractSampler;
import variables.AbstractState;

public interface ISamplerCallback<T> {

	default <PriorT> void onStartSampler(T caller, AbstractSampler<PriorT, ?, ?> sampler, int indexOfSampler,
			int numberOfSamplers, int step, int numberOfSteps, Document<PriorT> document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

	default <PriorT> void onEndSampler(T caller, AbstractSampler<PriorT, ?, ?> sampler, int indexOfSampler,
			int numberOfSamplers, int step, int numberOfSteps, Document<PriorT> document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs, AbstractState nextState) {
	}
}
