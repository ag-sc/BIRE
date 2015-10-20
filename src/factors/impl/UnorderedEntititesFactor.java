package factors.impl;

import java.util.HashSet;
import java.util.Set;

import factors.AbstractFactor;
import templates.AbstractTemplate;
import utility.VariableID;
import variables.AbstractState;

public class UnorderedEntititesFactor extends AbstractFactor {
	public Set<VariableID> entities;

	public UnorderedEntititesFactor(AbstractTemplate<? extends AbstractState> template, Set<VariableID> entities) {
		super(template);
		this.entities = entities;
	}

	@Override
	public Set<VariableID> getEntityIDs() {
		Set<VariableID> entities = new HashSet<>(this.entities);
		return entities;
	}

	@Override
	public String toString() {
		return "UnorderedEntititesFactor [factorID=" + factorID + ", template=" + template + ", entities=" + entities
				+ "]";
	}
}
