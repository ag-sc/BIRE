package learning.callbacks;

import corpus.Instance;
import learning.Trainer;
import variables.AbstractState;

public interface InstanceCallback {

	default public <InstanceT extends Instance> void onStartInstance(Trainer caller, InstanceT document,
			int indexOfDocument, int numberOfDocuments, int epoch, int numberOfEpochs) {

	}

	public <InstanceT extends Instance> void onEndInstance(Trainer caller, InstanceT document, int indexOfDocument,
			int numberOfDocuments, AbstractState<?> finalState, int epoch, int numberOfEpochs);

}
