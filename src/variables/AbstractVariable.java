package variables;

import utility.VariableID;

public class AbstractVariable<StateT extends AbstractState> implements Variable<StateT> {

	protected final VariableID id;

	public AbstractVariable(VariableID id) {
		this.id = id;
	}

	@Override
	public VariableID getID() {
		return id;
	}

}
