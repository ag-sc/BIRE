package learning;

import variables.AbstractState;

public interface Learner<StateT extends AbstractState> {

	public void update(StateT goldState, StateT currentState, StateT possibleNextState);
}
