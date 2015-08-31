package Learning.callbacks;

import Learning.Learner;

public interface EpochCallback {

	default void onStartEpoch(Learner learner, int epoch, int numberOfEpochs) {
	}

	default void onEndEpoch(Learner learner, int epoch, int numberOfEpochs) {
	}
}
