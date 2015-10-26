package sampling;

import java.util.List;

import variables.AbstractState;

/**
 * THe explorer is the component, that suggests (all) possible successor states
 * given a starting state. These possible successor states are then evaluated by
 * the sampler in order to choose a suitable next state.
 * 
 * @author sjebbara
 *
 * @param <StateT>
 */
public interface Explorer<StateT extends AbstractState> {

	public List<StateT> getNextStates(StateT currentState);
}
