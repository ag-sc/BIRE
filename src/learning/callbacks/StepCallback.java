package learning.callbacks;

import corpus.Instance;
import learning.Trainer;

public interface StepCallback {

	default <InstanceT extends Instance> void onStartStep(Trainer caller, int step, int numberOfSteps,
			Instance document, int indexOfDocument, int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

	default <InstanceT extends Instance> void onEndStep(Trainer caller, int step, int numberOfSteps, Instance document,
			int indexOfDocument, int numberOfDocuments, int epoch, int numberOfEpochs) {
	}
}
