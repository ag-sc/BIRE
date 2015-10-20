package learning.callbacks;

public interface IEpochCallback<T> {

	default void onStartEpoch(T caller, int epoch, int numberOfEpochs) {
	}

	default void onEndEpoch(T caller, int epoch, int numberOfEpochs) {
	}
}
