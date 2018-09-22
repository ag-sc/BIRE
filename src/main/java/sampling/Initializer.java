package sampling;

import variables.AbstractState;

/**
 * A class implementing this interface is supposed to provide an initial state/a
 * starting point for the Sampler to start sampling from. If applicable, the
 * state should be initialized with (fixed) "prior knowledge" at this point.
 * 
 * @author sjebbara
 *
 * @param <StateT>
 */
public interface Initializer<InstanceT, StateT extends AbstractState<InstanceT>> {
	public StateT getInitialState(InstanceT document);
}
