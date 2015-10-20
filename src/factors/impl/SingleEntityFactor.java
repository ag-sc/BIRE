package factors.impl;

import java.util.HashSet;
import java.util.Set;

import factors.AbstractFactor;
import templates.AbstractTemplate;
import utility.VariableID;
import variables.AbstractState;

public class SingleEntityFactor extends AbstractFactor {

	public VariableID entityID;

	public SingleEntityFactor(AbstractTemplate<? extends AbstractState> template, VariableID entityID) {
		super(template);
		this.entityID = entityID;
	}

	@Override
	public Set<VariableID> getEntityIDs() {
		Set<VariableID> entities = new HashSet<>();
		entities.add(entityID);
		return entities;
	}

	@Override
	public String toString() {
		return "SingleEntityFactor [factorID=" + factorID + ", template=" + template + ", entityID=" + entityID + "]";
	}

}
