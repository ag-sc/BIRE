package learning.callbacks;

import corpus.Document;
import learning.Learner;

public interface IDocumentCallback<T> {

	default void onStartDocument(T caller, Document document, int indexOfDocument, int numberOfDocuments,
			int epoch, int numberOfEpochs) {
	}

	default void onEndDocument(T caller, Document document, int indexOfDocument, int numberOfDocuments,
			int epoch, int numberOfEpochs) {
	}

}
