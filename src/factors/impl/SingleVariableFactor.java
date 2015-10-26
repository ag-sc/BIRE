package factors.impl;

import java.util.HashSet;
import java.util.Set;

import factors.AbstractFactor;
import templates.AbstractTemplate;
import utility.VariableID;
import variables.AbstractState;

public class SingleVariableFactor extends AbstractFactor {

	public VariableID entityID;

	public SingleVariableFactor(AbstractTemplate<? extends AbstractState> template, VariableID entityID) {
		super(template);
		this.entityID = entityID;
	}

	@Override
	public Set<VariableID> getVariableIDs() {
		Set<VariableID> entities = new HashSet<>();
		entities.add(entityID);
		return entities;
	}

	@Override
	public String toString() {
		return "SingleEntityFactor [factorID=" + factorID + ", template=" + template + ", entityID=" + entityID + "]";
	}

}
