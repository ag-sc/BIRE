package factors.impl;

import java.util.HashSet;
import java.util.Set;

import factors.AbstractFactor;
import templates.AbstractTemplate;
import utility.VariableID;
import variables.AbstractState;

public class UnorderedVariablesFactor extends AbstractFactor {
	public Set<VariableID> entities;

	public UnorderedVariablesFactor(AbstractTemplate<? extends AbstractState> template, Set<VariableID> entities) {
		super(template);
		this.entities = entities;
	}

	@Override
	public Set<VariableID> getVariableIDs() {
		Set<VariableID> entities = new HashSet<>(this.entities);
		return entities;
	}

	@Override
	public String toString() {
		return "UnorderedEntititesFactor [factorID=" + factorID + ", template=" + template + ", entities=" + entities
				+ "]";
	}
}
