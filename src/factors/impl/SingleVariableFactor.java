package factors.impl;

import java.util.HashSet;
import java.util.Set;

import factors.AbstractFactor;
import templates.AbstractTemplate;
import utility.VariableID;
import variables.AbstractState;

public class SingleVariableFactor extends AbstractFactor {

	public VariableID entityID;

	/**
	 * This is a generic implementation for a Factor that only needs a single
	 * variable to compute its features. Therefore, this factor only stores a
	 * single VariableID, which can later be used to retrieve the actual
	 * variable to compute the features.
	 * 
	 * @param template
	 * @param entityID
	 */
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
		return "SingleVariableFactor [factorID=" + factorID + ", template=" + template.getClass().getSimpleName()
				+ ", entityID=" + entityID + ", features=" + features + "]";
	}

}
