package variables;

import utility.VariableID;

public interface Variable<StateT extends AbstractState> {
	public VariableID getID();

}
