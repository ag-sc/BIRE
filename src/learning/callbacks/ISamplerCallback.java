package learning.callbacks;

import corpus.Instance;
import sampling.AbstractSampler;
import variables.AbstractState;

public interface ISamplerCallback<T> {

	default <InstanceT extends Instance> void onStartSampler(T caller, AbstractSampler<?, ?> sampler,
			int indexOfSampler, int numberOfSamplers, int step, int numberOfSteps, InstanceT document,
			int indexOfDocument, int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

	default <InstanceT extends Instance> void onEndSampler(T caller, AbstractSampler<?, ?> sampler, int indexOfSampler,
			int numberOfSamplers, int step, int numberOfSteps, InstanceT document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs, AbstractState nextState) {
	}
}
