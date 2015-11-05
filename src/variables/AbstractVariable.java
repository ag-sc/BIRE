package variables;

import utility.VariableID;

public class AbstractVariable<StateT extends AbstractState> implements Variable<StateT> {

	protected final StateT state;
	protected final VariableID id;

	public AbstractVariable(StateT state, VariableID id) {
		this.state = state;
		this.id = id;
	}

	@Override
	public VariableID getID() {
		return id;
	}

	@Override
	public StateT getState() {
		return state;
	}

}
