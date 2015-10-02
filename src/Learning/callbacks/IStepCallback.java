package Learning.callbacks;

import Corpus.Document;
import Learning.Learner;

public interface IStepCallback {

	default void onStartStep(Learner<?> learner, int step, int numberOfSteps, Document document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

	default void onEndStep(Learner<?> learner, int step, int numberOfSteps, Document document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
	}
}
