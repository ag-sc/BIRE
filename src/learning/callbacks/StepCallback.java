package learning.callbacks;

import corpus.Instance;
import sampling.Sampler;
import variables.AbstractState;

public interface StepCallback {

	default <InstanceT extends Instance, StateT extends AbstractState<InstanceT>> void onStartStep(
			Sampler<StateT, ?> sampler, int step, int e, int numberOfExplorers, StateT initialState) {
	}

	default <InstanceT extends Instance, StateT extends AbstractState<InstanceT>> void onEndStep(
			Sampler<StateT, ?> sampler, int step, int e, int numberOfExplorers, StateT initialState,
			StateT currentState) {

	}
}
