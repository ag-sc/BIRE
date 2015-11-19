package learning.callbacks;

import corpus.Instance;

public interface IDocumentCallback<T> {

	default <InstanceT extends Instance> void onStartDocument(T caller, InstanceT document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

	default <InstanceT extends Instance> void onEndDocument(T caller, InstanceT document, int indexOfDocument,
			int numberOfDocuments, int epoch, int numberOfEpochs) {
	}

}
