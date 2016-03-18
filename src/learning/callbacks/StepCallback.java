package learning.callbacks;

import corpus.Instance;
import sampling.DefaultSampler;
import variables.AbstractState;

public interface StepCallback {

	default <InstanceT extends Instance, StateT extends AbstractState<InstanceT>> void onStartStep(
			DefaultSampler<InstanceT, StateT, ?> defaultSampler, int step, int e, int size, StateT initialState) {
	}

	default <InstanceT extends Instance, StateT extends AbstractState<InstanceT>> void onEndStep(
			DefaultSampler<InstanceT, StateT, ?> defaultSampler, int step, int e, int size, StateT initialState,
			StateT currentState) {

	}
}
