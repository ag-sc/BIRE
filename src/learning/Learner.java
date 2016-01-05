package learning;

import java.util.List;

import variables.AbstractState;

/**
 * Provides an interface to update a model given pairs of states.
 * 
 * @author sjebbara
 *
 * @param <StateT>
 */
public interface Learner<StateT extends AbstractState> {

	public void update(StateT currentState, StateT possibleNextState);

	public void update(StateT currentState, List<StateT> possibleNextStates);
}
