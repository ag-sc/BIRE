package learning.callbacks;

import corpus.Instance;
import learning.Trainer;
import sampling.Sampler;
import variables.AbstractState;

public interface SamplerCallback {

	default <InstanceT extends Instance> void onStartSampler(Trainer caller, Sampler<?, ?> sampler, int indexOfSampler,
			int numberOfSamplers, int step, int numberOfSteps, InstanceT document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

	default <InstanceT extends Instance> void onEndSampler(Trainer caller, Sampler<?, ?> sampler, int indexOfSampler,
			int numberOfSamplers, int step, int numberOfSteps, InstanceT document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs, AbstractState nextState) {
	}
}
