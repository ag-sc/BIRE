package sampling;

import java.util.List;

import variables.AbstractState;

public interface Explorer<StateT extends AbstractState> {

	public List<StateT> getNextStates(StateT currentState);
}
