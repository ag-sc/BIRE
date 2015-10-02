package Learning.callbacks;

import Corpus.Document;
import Learning.Learner;

public interface IDocumentCallback {

	default void onStartDocument(Learner<?> learner, Document document, int indexOfDocument, int numberOfDocuments,
			int epoch, int numberOfEpochs) {
	}

	default void onEndDocument(Learner<?> learner, Document document, int indexOfDocument, int numberOfDocuments,
			int epoch, int numberOfEpochs) {
	}

}
