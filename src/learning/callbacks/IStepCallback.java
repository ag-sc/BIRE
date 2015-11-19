package learning.callbacks;

import corpus.Instance;

public interface IStepCallback<T> {

	default <InstanceT extends Instance> void onStartStep(T caller, int step, int numberOfSteps, Instance document,
			int indexOfDocument, int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

	default <InstanceT extends Instance> void onEndStep(T caller, int step, int numberOfSteps, Instance document,
			int indexOfDocument, int numberOfDocuments, int epoch, int numberOfEpochs) {
	}
}
