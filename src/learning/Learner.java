package learning;

import java.util.List;

import variables.AbstractState;

public interface Learner<StateT extends AbstractState> {

	public void update(StateT currentState, StateT possibleNextState);

	public void update(StateT currentState, List<StateT> possibleNextStates);
}
