package learning.callbacks;

import learning.Trainer;

public interface EpochCallback {

	default void onStartEpoch(Trainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
	}

	default void onEndEpoch(Trainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
	}
}
