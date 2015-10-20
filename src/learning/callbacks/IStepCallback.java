package learning.callbacks;

import corpus.Document;

public interface IStepCallback<T> {

	default void onStartStep(T caller, int step, int numberOfSteps, Document document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

	default void onEndStep(T caller, int step, int numberOfSteps, Document document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
	}
}
