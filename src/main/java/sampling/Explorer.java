package sampling;

import java.util.List;

import variables.AbstractState;

/**
 * The explorer is a component of the DefaultSampler, that suggests (all)
 * possible successor states given a starting state. These possible successor
 * states are then evaluated by the sampler in order to choose a suitable next
 * state. The DefaultSampler assumes that the list of successor states in never
 * empty. Thus, you should always include an exact copy of the current state in
 * the list of successor states. By that, the sampler has always the chance to
 * do nothing, to prevent "destructive" sampling.
 * 
 * @author sjebbara
 *
 * @param <StateT>
 */
public interface Explorer<StateT extends AbstractState<?>> {

	public List<StateT> getNextStates(StateT currentState);

}
